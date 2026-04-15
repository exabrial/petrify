package com.github.exabrial.petrify.maven;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.github.exabrial.petrify.compiler.model.ClassifierGrove;
import com.github.exabrial.petrify.compiler.model.ClassifierVine;
import com.github.exabrial.petrify.compiler.model.Grove;
import com.github.exabrial.petrify.compiler.model.ModelMetadata;
import com.github.exabrial.petrify.compiler.model.RegressorGrove;
import com.github.exabrial.petrify.compiler.model.RegressorVine;
import com.github.exabrial.petrify.compiler.model.Vine;
import com.github.exabrial.petrify.imprt.lightgbm.LightGbmArborist;
import com.github.exabrial.petrify.imprt.onnx.OnnxArborist;
import com.github.exabrial.petrify.imprt.onnx.OnnxVintner;
import com.github.exabrial.petrify.imprt.scikit.ScikitVintner;

@Mojo(name = "fossilize", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class FossilizeMojo extends AbstractMojo {
	private static final String IMPORTER_LIGHTGBM = "lightgbm";
	private static final String IMPORTER_ONNX = "onnx";
	private static final String IMPORTER_SCIKIT = "scikit";

	private static final String MODEL_TYPE_CLASSIFIER = "classifier";
	private static final String MODEL_TYPE_REGRESSOR = "regressor";

	private static final String PETRIFY_CLASSES_DIR = "petrify-classes";
	private static final String PETRIFY_CLASSPATH_ENTRY = "<classpathentry exported=\"true\" kind=\"lib\" path=\"target/"
			+ PETRIFY_CLASSES_DIR + "\"/>";
	private static final String CLASSPATH_OUTPUT_ENTRY = "kind=\"output\"";
	private static final String CLASSPATH_ENTRY_TAG = "<classpathentry";

	@SuppressWarnings("deprecation")
	@Component
	private BuildContext buildContext;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(required = true)
	private FossilConfig[] fossils;

	@Parameter(defaultValue = "${project.build.outputDirectory}")
	private String outputDirectory;

	@Parameter(property = "petrify.skip", defaultValue = "false")
	private boolean skip;

	@Parameter(property = "petrify.disableEclipseIntegration", defaultValue = "false")
	private boolean disableEclipseIntegration;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("execute() petrify.skip is true, skipping");
		} else {
			createOutputDirectories();
			refreshOutputDirectories();

			for (final FossilConfig fossil : fossils) {
				final String modelDirectory = resolveModelDirectory(fossil);
				project.addCompileSourceRoot(modelDirectory);
				processFossil(fossil);
			}
			refreshOutputDirectories();

			if (isEclipseIntegrationEnabled()) {
				addPetrifyClasspathEntry();
			}
		}
	}

	protected void processFossil(final FossilConfig fossil) throws MojoExecutionException, MojoFailureException {
		final String modelDirectory = resolveModelDirectory(fossil);
		final Path modelPath = Path.of(modelDirectory, fossil.getModelFile());
		final String resolvedClassName = fossil.resolveClassName();
		final String packageName = fossil.getTargetPackageName();
		final Path outputClassFile = resolveOutputPath(outputDirectory, packageName, resolvedClassName);

		if (!buildContext.isIncremental() && isUpToDate(modelPath, outputClassFile)) {
			getLog().debug("processFossil() up-to-date, skipping modelFile:" + fossil.getModelFile() + " class:" + resolvedClassName);
			buildContext.refresh(outputClassFile.toFile());
		} else {
			deleteStaleClassFile(outputClassFile);
			final Path eclipseClassFile;
			if (isEclipseIntegrationEnabled()) {
				eclipseClassFile = resolveEclipseOutputPath(packageName, resolvedClassName);
				deleteStaleClassFile(eclipseClassFile);
			} else {
				eclipseClassFile = null;
			}

			validate(fossil, modelPath, resolvedClassName);

			getLog().info("processFossil() compiling modelFile:" + fossil.getModelFile() + " class:" + packageName + "." + resolvedClassName
					+ " importer:" + fossil.getImporter() + " modelType:" + fossil.getModelType());
			final byte[] modelBytes = readModelFile(modelPath);
			final byte[] classBytes = compile(fossil, modelBytes, packageName, resolvedClassName);
			writeClassFile(outputClassFile, classBytes);
			if (eclipseClassFile != null) {
				writeClassFile(eclipseClassFile, classBytes);
			}
		}
	}

	protected void validate(final FossilConfig fossil, final Path modelPath, final String resolvedClassName)
			throws MojoFailureException {
		if (fossil.getModelFile() == null || fossil.getModelFile().isEmpty()) {
			throw new MojoFailureException("modelFile is required");
		} else if (fossil.getImporter() == null || fossil.getImporter().isEmpty()) {
			throw new MojoFailureException("importer is required for modelFile:" + fossil.getModelFile());
		} else if (fossil.getModelType() == null || fossil.getModelType().isEmpty()) {
			throw new MojoFailureException("modelType is required for modelFile:" + fossil.getModelFile());
		} else if (fossil.getTargetPackageName() == null || fossil.getTargetPackageName().isEmpty()) {
			throw new MojoFailureException("targetPackageName is required for modelFile:" + fossil.getModelFile());
		} else if (!Files.exists(modelPath)) {
			throw new MojoFailureException("modelFile not found:" + modelPath);
		} else if (resolvedClassName == null || resolvedClassName.isEmpty()) {
			throw new MojoFailureException("className could not be resolved for modelFile:" + fossil.getModelFile());
		} else {
			switch (fossil.getImporter()) {
				case IMPORTER_LIGHTGBM, IMPORTER_ONNX, IMPORTER_SCIKIT -> {
				}
				default -> throw new MojoFailureException(
						"unknown importer:" + fossil.getImporter() + " for modelFile:" + fossil.getModelFile());
			}
			switch (fossil.getModelType()) {
				case MODEL_TYPE_CLASSIFIER, MODEL_TYPE_REGRESSOR -> {
				}
				default -> throw new MojoFailureException(
						"unknown modelType:" + fossil.getModelType() + " for modelFile:" + fossil.getModelFile());
			}
		}
	}

	protected byte[] compile(final FossilConfig fossil, final byte[] modelBytes, final String packageName,
			final String resolvedClassName) throws MojoExecutionException {
		final BuildTimePetrify petrify = new BuildTimePetrify();
		petrify.setTarget(packageName, resolvedClassName);

		final String importer = fossil.getImporter();
		final String modelType = fossil.getModelType();
		switch (importer) {
			case IMPORTER_LIGHTGBM -> {
				compileLightgbm(petrify, modelBytes, modelType, fossil);
			}
			case IMPORTER_ONNX -> {
				compileOnnx(petrify, modelBytes, modelType, fossil);
			}
			case IMPORTER_SCIKIT -> {
				compileScikit(petrify, modelBytes, modelType, fossil);
			}
			default -> throw new MojoExecutionException("compile() unknown importer:" + importer);
		}
		return petrify.getFossilBytes();
	}

	protected void compileLightgbm(final BuildTimePetrify petrify, final byte[] modelBytes, final String modelType,
			final FossilConfig fossilConfig) {
		final LightGbmArborist arborist = new LightGbmArborist();
		switch (modelType) {
			case MODEL_TYPE_CLASSIFIER -> {
				final ClassifierGrove grove = arborist.toGrove(modelBytes);
				applyConfigMetadata(grove, fossilConfig);
				petrify.fossilize(null, grove);
			}
			case MODEL_TYPE_REGRESSOR -> {
				final RegressorGrove grove = arborist.toGrove(modelBytes);
				applyConfigMetadata(grove, fossilConfig);
				petrify.fossilize(null, grove);
			}
		}
	}

	protected void compileOnnx(final BuildTimePetrify petrify, final byte[] modelBytes, final String modelType,
			final FossilConfig fossilConfig) {
		switch (modelType) {
			case MODEL_TYPE_CLASSIFIER -> {
				compileOnnxClassifier(petrify, modelBytes, fossilConfig);
			}
			case MODEL_TYPE_REGRESSOR -> {
				compileOnnxRegressor(petrify, modelBytes, fossilConfig);
			}
		}
	}

	protected void compileOnnxClassifier(final BuildTimePetrify petrify, final byte[] modelBytes,
			final FossilConfig fossilConfig) {
		try {
			final OnnxArborist arborist = new OnnxArborist();
			final ClassifierGrove grove = arborist.toGrove(modelBytes);
			applyConfigMetadata(grove, fossilConfig);
			petrify.fossilize(null, grove);
		} catch (final Exception treeException) {
			try {
				final OnnxVintner vintner = new OnnxVintner();
				final ClassifierVine vine = vintner.toVine(modelBytes);
				applyConfigMetadata(vine, fossilConfig);
				petrify.fossilize(null, vine);
			} catch (final Exception vineException) {
				treeException.addSuppressed(vineException);
				throw treeException;
			}
		}
	}

	protected void compileOnnxRegressor(final BuildTimePetrify petrify, final byte[] modelBytes,
			final FossilConfig fossilConfig) {
		try {
			final OnnxArborist arborist = new OnnxArborist();
			final RegressorGrove grove = arborist.toGrove(modelBytes);
			applyConfigMetadata(grove, fossilConfig);
			petrify.fossilize(null, grove);
		} catch (final Exception treeException) {
			try {
				final OnnxVintner vintner = new OnnxVintner();
				final RegressorVine vine = vintner.toVine(modelBytes);
				applyConfigMetadata(vine, fossilConfig);
				petrify.fossilize(null, vine);
			} catch (final Exception vineException) {
				treeException.addSuppressed(vineException);
				throw treeException;
			}
		}
	}

	protected void compileScikit(final BuildTimePetrify petrify, final byte[] modelBytes, final String modelType,
			final FossilConfig fossilConfig) {
		final ScikitVintner vintner = new ScikitVintner();
		switch (modelType) {
			case MODEL_TYPE_CLASSIFIER -> {
				final ClassifierVine vine = vintner.toVine(modelBytes);
				applyConfigMetadata(vine, fossilConfig);
				petrify.fossilize(null, vine);
			}
			case MODEL_TYPE_REGRESSOR -> {
				final RegressorVine vine = vintner.toVine(modelBytes);
				applyConfigMetadata(vine, fossilConfig);
				petrify.fossilize(null, vine);
			}
		}
	}

	protected boolean isEclipseIntegrationEnabled() {
		final String buildContextClassName = buildContext.getClass().getName();
		return !disableEclipseIntegration && buildContextClassName.startsWith("org.eclipse.m2e");
	}

	protected void applyConfigMetadata(final Grove grove, final FossilConfig fossilConfig) {
		if (fossilConfig.getModelName() != null) {
			if (grove.metadata == null) {
				grove.metadata = new ModelMetadata();
			}
			grove.metadata.modelName = fossilConfig.getModelName();
		}
		if (fossilConfig.getModelVersion() != null) {
			if (grove.metadata == null) {
				grove.metadata = new ModelMetadata();
			}
			grove.metadata.modelVersion = fossilConfig.getModelVersion();
		}
		final String[] resolvedFeatureNames = fossilConfig.resolveFeatureNames();
		if (resolvedFeatureNames != null) {
			if (grove.metadata == null) {
				grove.metadata = new ModelMetadata();
			}
			grove.metadata.featureNames = resolvedFeatureNames;
		} else if (fossilConfig.isIgnoreFeatureNamesFromModel() && grove.metadata != null) {
			grove.metadata.featureNames = null;
		}
	}

	protected void applyConfigMetadata(final Vine vine, final FossilConfig fossilConfig) {
		if (fossilConfig.getModelName() != null) {
			if (vine.metadata == null) {
				vine.metadata = new ModelMetadata();
			}
			vine.metadata.modelName = fossilConfig.getModelName();
		}
		if (fossilConfig.getModelVersion() != null) {
			if (vine.metadata == null) {
				vine.metadata = new ModelMetadata();
			}
			vine.metadata.modelVersion = fossilConfig.getModelVersion();
		}
		final String[] resolvedFeatureNames = fossilConfig.resolveFeatureNames();
		if (resolvedFeatureNames != null) {
			if (vine.metadata == null) {
				vine.metadata = new ModelMetadata();
			}
			vine.metadata.featureNames = resolvedFeatureNames;
		} else if (fossilConfig.isIgnoreFeatureNamesFromModel() && vine.metadata != null) {
			vine.metadata.featureNames = null;
		}
	}

	protected String resolveEclipseBaseDirectory() {
		return project.getBuild().getDirectory() + File.separator + PETRIFY_CLASSES_DIR;
	}

	protected Path resolveEclipseOutputPath(final String packageName, final String className) {
		return resolveOutputPath(resolveEclipseBaseDirectory(), packageName, className);
	}

	protected void addPetrifyClasspathEntry() throws MojoExecutionException {
		final Path classpathFile = project.getBasedir().toPath().resolve(".classpath");
		if (!Files.exists(classpathFile)) {
			getLog().debug("addPetrifyClasspathEntry() .classpath not found, skipping");
		} else {
			try {
				Thread.sleep(100);
				final String content = Files.readString(classpathFile, StandardCharsets.UTF_8);
				if (content.contains(PETRIFY_CLASSES_DIR)) {
					getLog().debug("addPetrifyClasspathEntry() .classpath already contains petrify-classes entry");
				} else {
					final int outputEntryIdx = content.indexOf(CLASSPATH_OUTPUT_ENTRY);
					if (outputEntryIdx < 0) {
						getLog().warn("addPetrifyClasspathEntry() could not find output entry in .classpath");
					} else {
						final int insertionPoint = content.lastIndexOf(CLASSPATH_ENTRY_TAG, outputEntryIdx);
						if (insertionPoint < 0) {
							getLog().warn("addPetrifyClasspathEntry() could not find insertion point in .classpath");
						} else {
							final String updatedContent = content.substring(0, insertionPoint) + PETRIFY_CLASSPATH_ENTRY + "\n\t"
									+ content.substring(insertionPoint);
							Files.writeString(classpathFile, updatedContent, StandardCharsets.UTF_8);
							buildContext.refresh(classpathFile.toFile());
							getLog().info("addPetrifyClasspathEntry() added petrify-classes library entry to .classpath");
						}
					}
				}
			} catch (final IOException | InterruptedException ioException) {
				throw new MojoExecutionException("addPetrifyClasspathEntry() failed to update .classpath", ioException);
			}
		}
	}

	protected String resolveModelDirectory(final FossilConfig fossil) {
		final String result;
		if (fossil.getModelDirectory() != null && !fossil.getModelDirectory().isEmpty()) {
			result = fossil.getModelDirectory();
		} else {
			result = project.getBasedir() + File.separator + "src" + File.separator + "main" + File.separator + "models";
		}
		return result;
	}

	protected Path resolveOutputPath(final String baseDirectory, final String packageName, final String className) {
		final String packageDir = packageName.replace('.', File.separatorChar);
		return Path.of(baseDirectory, packageDir, className + ".class");
	}

	protected boolean isUpToDate(final Path modelPath, final Path outputClassFile) {
		boolean result;
		if (Files.exists(outputClassFile)) {
			try {
				final long modelModified = Files.getLastModifiedTime(modelPath).toMillis();
				final long classModified = Files.getLastModifiedTime(outputClassFile).toMillis();
				result = classModified >= modelModified;
			} catch (final IOException ioException) {
				result = false;
			}
		} else {
			result = false;
		}
		return result;
	}

	protected void deleteStaleClassFile(final Path classFile) {
		if (Files.exists(classFile)) {
			try {
				Files.delete(classFile);
				buildContext.refresh(classFile.toFile());
				getLog().debug("deleteStaleClassFile() deleted classFile:" + classFile);
			} catch (final IOException ioException) {
				getLog().warn("deleteStaleClassFile() failed to delete classFile:" + classFile, ioException);
			}
		}
	}

	protected byte[] readModelFile(final Path modelPath) throws MojoExecutionException {
		try {
			return Files.readAllBytes(modelPath);
		} catch (final IOException ioException) {
			throw new MojoExecutionException("readModelFile() failed to read modelFile:" + modelPath, ioException);
		}
	}

	protected void createOutputDirectories() throws MojoExecutionException {
		try {
			Files.createDirectories(Path.of(outputDirectory));
			if (isEclipseIntegrationEnabled()) {
				Files.createDirectories(Path.of(resolveEclipseBaseDirectory()));
			}
		} catch (final IOException ioException) {
			throw new MojoExecutionException("createOutputDirectories() failed to create output directories", ioException);
		}
	}

	protected void refreshOutputDirectories() {
		buildContext.refresh(new File(outputDirectory));
		if (isEclipseIntegrationEnabled()) {
			buildContext.refresh(new File(resolveEclipseBaseDirectory()));
		}
	}

	protected void writeClassFile(final Path outputClassFile, final byte[] classBytes) throws MojoExecutionException {
		try {
			Files.createDirectories(outputClassFile.getParent());
			try (final OutputStream outputStream = buildContext.newFileOutputStream(outputClassFile.toFile())) {
				outputStream.write(classBytes);
			}
			getLog().info("writeClassFile() wrote bytes:" + classBytes.length + " to:" + outputClassFile);
		} catch (final IOException ioException) {
			throw new MojoExecutionException("writeClassFile() failed to write classFile:" + outputClassFile, ioException);
		}
	}
}
