package com.github.exabrial.petrify.maven;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
import com.github.exabrial.petrify.internal.model.CompiledModel;

/**
 * Compiles machine-learning models into JVM bytecode at build time.
 *
 * <p>
 * For each {@code <fossil>} entry in the plugin configuration, reads the source model file, parses it with the selected
 * {@link Importer}, compiles it into a fossil class implementing the appropriate {@code Fossil} interface for its {@link ModelType},
 * and writes the resulting {@code .class} file into {@code ${project.build.outputDirectory}}.
 *
 * <p>
 * Binds to {@link LifecyclePhase#GENERATE_RESOURCES} by default so generated classes are available on the compile classpath.
 */
@Mojo(name = "fossilize", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class FossilizeMojo extends AbstractMojo {
	static final String PETRIFY_MANIFEST_DIR = "petrify";
	static final String PETRIFY_MANIFEST_FILE = "manifest.txt";
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

	/**
	 * One or more models to compile. Each entry describes a single source model file and how to translate it into a fossil class.
	 * Required; the goal fails if this list is empty.
	 *
	 * @see FossilConfig
	 */
	@Parameter(required = true)
	private FossilConfig[] fossils;

	/**
	 * Destination directory for generated fossil class files. Defaults to the project's standard build output directory
	 * ({@code target/classes}).
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}")
	private String outputDirectory;

	/**
	 * When {@code true}, the goal becomes a no-op. Useful for disabling fossil compilation in a specific profile or build. Controlled by
	 * the {@code petrify.skip} system property.
	 */
	@Parameter(property = "petrify.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * When {@code true}, disables the Eclipse-specific integration that writes an extra copy of each compiled class into a sibling
	 * {@code petrify-classes} directory and registers it in {@code .classpath}. The integration activates automatically when Eclipse m2e
	 * is detected as the build context; set this flag to override that detection. Controlled by the
	 * {@code petrify.disableEclipseIntegration} system property.
	 */
	@Parameter(property = "petrify.disableEclipseIntegration", defaultValue = "false")
	private boolean disableEclipseIntegration;

	private final List<String> manifestEntries = new ArrayList<>();

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
			writeManifest();
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
			recordExistingManifestEntries(packageName, resolvedClassName);
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
			manifestEntries.add(toRelativePath(packageName, resolvedClassName));
			if (eclipseClassFile != null) {
				writeClassFile(eclipseClassFile, classBytes);
			}
		}
	}

	protected void validate(final FossilConfig fossil, final Path modelPath, final String resolvedClassName)
			throws MojoFailureException {
		if (fossil.getModelFile() == null || fossil.getModelFile().isEmpty()) {
			throw new MojoFailureException("modelFile is required");
		} else if (fossil.getImporter() == null) {
			throw new MojoFailureException("importer is required for modelFile:" + fossil.getModelFile());
		} else if (fossil.getModelType() == null) {
			throw new MojoFailureException("modelType is required for modelFile:" + fossil.getModelFile());
		} else if (fossil.getTargetPackageName() == null || fossil.getTargetPackageName().isEmpty()) {
			throw new MojoFailureException("targetPackageName is required for modelFile:" + fossil.getModelFile());
		} else if (!Files.exists(modelPath)) {
			throw new MojoFailureException("modelFile not found:" + modelPath);
		} else if (resolvedClassName == null || resolvedClassName.isEmpty()) {
			throw new MojoFailureException("className could not be resolved for modelFile:" + fossil.getModelFile());
		}
	}

	protected byte[] compile(final FossilConfig fossil, final byte[] modelBytes, final String packageName,
			final String resolvedClassName) throws MojoExecutionException {
		final BuildTimePetrify petrify = new BuildTimePetrify();
		petrify.setTarget(packageName, resolvedClassName);

		final Importer importer = fossil.getImporter();
		final ModelType modelType = fossil.getModelType();
		switch (importer) {
			case lightgbm -> {
				compileLightgbm(petrify, modelBytes, modelType, fossil);
			}
			case onnx -> {
				compileOnnx(petrify, modelBytes, modelType, fossil);
			}
			case scikit -> {
				compileScikit(petrify, modelBytes, modelType, fossil);
			}
		}

		for (final CompiledModel innerClass : petrify.getInnerClasses()) {
			final Path innerOutputPath = resolveOutputPath(outputDirectory, packageName, innerClass.className());
			writeClassFile(innerOutputPath, innerClass.classBytes());
			manifestEntries.add(toRelativePath(packageName, innerClass.className()));
			if (isEclipseIntegrationEnabled()) {
				final Path innerEclipsePath = resolveEclipseOutputPath(packageName, innerClass.className());
				writeClassFile(innerEclipsePath, innerClass.classBytes());
			}
		}

		return petrify.getFossilClass().classBytes();
	}

	protected void compileLightgbm(final BuildTimePetrify petrify, final byte[] modelBytes, final ModelType modelType,
			final FossilConfig fossilConfig) {
		final LightGbmArborist arborist = new LightGbmArborist();
		switch (modelType) {
			case classifier -> {
				final ClassifierGrove grove = arborist.toGrove(modelBytes);
				applyConfigMetadata(grove, fossilConfig);
				petrify.fossilize(null, grove);
			}
			case regressor -> {
				final RegressorGrove grove = arborist.toGrove(modelBytes);
				applyConfigMetadata(grove, fossilConfig);
				petrify.fossilize(null, grove);
			}
		}
	}

	protected void compileOnnx(final BuildTimePetrify petrify, final byte[] modelBytes, final ModelType modelType,
			final FossilConfig fossilConfig) {
		switch (modelType) {
			case classifier -> {
				compileOnnxClassifier(petrify, modelBytes, fossilConfig);
			}
			case regressor -> {
				compileOnnxRegressor(petrify, modelBytes, fossilConfig);
			}
		}
	}

	protected void compileOnnxClassifier(final BuildTimePetrify petrify, final byte[] modelBytes, final FossilConfig fossilConfig) {
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

	protected void compileOnnxRegressor(final BuildTimePetrify petrify, final byte[] modelBytes, final FossilConfig fossilConfig) {
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

	protected void compileScikit(final BuildTimePetrify petrify, final byte[] modelBytes, final ModelType modelType,
			final FossilConfig fossilConfig) {
		final ScikitVintner vintner = new ScikitVintner();
		switch (modelType) {
			case classifier -> {
				final ClassifierVine vine = vintner.toVine(modelBytes);
				applyConfigMetadata(vine, fossilConfig);
				petrify.fossilize(null, vine);
			}
			case regressor -> {
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

	protected void writeManifest() throws MojoExecutionException {
		final Path manifestDir = Path.of(project.getBuild().getDirectory(), PETRIFY_MANIFEST_DIR);
		final Path manifestFile = manifestDir.resolve(PETRIFY_MANIFEST_FILE);
		try {
			Files.createDirectories(manifestDir);
			Files.write(manifestFile, manifestEntries, StandardCharsets.UTF_8);
			getLog().info("writeManifest() wrote entries:" + manifestEntries.size() + " to:" + manifestFile);
		} catch (final IOException ioException) {
			throw new MojoExecutionException("writeManifest() failed to write manifest:" + manifestFile, ioException);
		}
	}

	protected String toRelativePath(final String packageName, final String className) {
		return packageName.replace('.', File.separatorChar) + File.separator + className + ".class";
	}

	protected void recordExistingManifestEntries(final String packageName, final String resolvedClassName) {
		final String packageDir = packageName.replace('.', File.separatorChar);
		final Path packagePath = Path.of(outputDirectory, packageDir);
		if (Files.isDirectory(packagePath)) {
			try (final DirectoryStream<Path> stream = Files.newDirectoryStream(packagePath,
					resolvedClassName + "*.class")) {
				for (final Path classFile : stream) {
					manifestEntries.add(packageDir + File.separator + classFile.getFileName().toString());
				}
			} catch (final IOException ioException) {
				getLog().warn("recordExistingManifestEntries() failed to enumerate existing class files in:" + packagePath, ioException);
			}
		}
	}
}
