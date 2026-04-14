package com.github.exabrial.petrify.maven;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
import com.github.exabrial.petrify.compiler.model.RegressorGrove;
import com.github.exabrial.petrify.compiler.model.RegressorVine;
import com.github.exabrial.petrify.imprt.lightgbm.LightGbmArborist;
import com.github.exabrial.petrify.imprt.onnx.OnnxArborist;
import com.github.exabrial.petrify.imprt.onnx.OnnxVintner;
import com.github.exabrial.petrify.imprt.scikit.ScikitVintner;

@Mojo(name = "fossilize", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class FossilizeMojo extends AbstractMojo {
	private static final String IMPORTER_ONNX = "onnx";
	private static final String IMPORTER_LIGHTGBM = "lightgbm";
	private static final String IMPORTER_SCIKIT = "scikit";

	private static final String MODEL_TYPE_CLASSIFIER = "classifier";
	private static final String MODEL_TYPE_REGRESSOR = "regressor";

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

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("execute() petrify.skip is true, skipping");
			return;
		} else {
			for (final FossilConfig fossil : fossils) {
				final String modelDirectory = resolveModelDirectory(fossil);
				project.addCompileSourceRoot(modelDirectory);
				processFossil(fossil);
			}
		}
	}

	protected void processFossil(final FossilConfig fossil) throws MojoExecutionException, MojoFailureException {
		final String modelDirectory = resolveModelDirectory(fossil);
		final Path modelPath = Path.of(modelDirectory, fossil.getModelFile());
		final String resolvedClassName = fossil.resolveClassName();
		final String packageName = fossil.getTargetPackageName();

		validate(fossil, modelPath, resolvedClassName);

		final Path outputClassFile = resolveOutputPath(packageName, resolvedClassName);
		if (!buildContext.isIncremental() && isUpToDate(modelPath, outputClassFile)) {
			getLog().info("processFossil() skipping up-to-date model:" + fossil.getModelFile() + " class:" + resolvedClassName);
			buildContext.refresh(outputClassFile.toFile());
		} else {
			getLog().info("processFossil() compiling model:" + fossil.getModelFile() + " class:" + packageName + "." + resolvedClassName
					+ " importer:" + fossil.getImporter() + " modelType:" + fossil.getModelType());

			final byte[] modelBytes = readModelFile(modelPath);
			final byte[] classBytes = compile(fossil, modelBytes, packageName, resolvedClassName);
			writeClassFile(outputClassFile, classBytes);
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
				case IMPORTER_ONNX, IMPORTER_LIGHTGBM, IMPORTER_SCIKIT -> {
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
				compileLightgbm(petrify, modelBytes, modelType);
			}
			case IMPORTER_ONNX -> {
				compileOnnx(petrify, modelBytes, modelType);
			}
			case IMPORTER_SCIKIT -> {
				compileScikit(petrify, modelBytes, modelType);
			}
			default -> throw new MojoExecutionException("unknown importer:" + importer);
		}
		return petrify.getFossilBytes();
	}

	protected void compileLightgbm(final BuildTimePetrify petrify, final byte[] modelBytes, final String modelType) {
		final LightGbmArborist arborist = new LightGbmArborist();
		switch (modelType) {
			case MODEL_TYPE_CLASSIFIER -> {
				final ClassifierGrove grove = arborist.toGrove(modelBytes);
				petrify.fossilize(null, grove);
			}
			case MODEL_TYPE_REGRESSOR -> {
				final RegressorGrove grove = arborist.toGrove(modelBytes);
				petrify.fossilize(null, grove);
			}
		}
	}

	protected void compileOnnx(final BuildTimePetrify petrify, final byte[] modelBytes, final String modelType) {
		switch (modelType) {
			case MODEL_TYPE_CLASSIFIER -> {
				compileOnnxClassifier(petrify, modelBytes);
			}
			case MODEL_TYPE_REGRESSOR -> {
				compileOnnxRegressor(petrify, modelBytes);
			}
		}
	}

	protected void compileOnnxClassifier(final BuildTimePetrify petrify, final byte[] modelBytes) {
		try {
			final OnnxArborist arborist = new OnnxArborist();
			final ClassifierGrove grove = arborist.toGrove(modelBytes);
			petrify.fossilize(null, grove);
		} catch (final Exception treeException) {
			try {
				final OnnxVintner vintner = new OnnxVintner();
				final ClassifierVine vine = vintner.toVine(modelBytes);
				petrify.fossilize(null, vine);
			} catch (final Exception vineException) {
				treeException.addSuppressed(vineException);
				throw treeException;
			}
		}
	}

	protected void compileOnnxRegressor(final BuildTimePetrify petrify, final byte[] modelBytes) {
		try {
			final OnnxArborist arborist = new OnnxArborist();
			final RegressorGrove grove = arborist.toGrove(modelBytes);
			petrify.fossilize(null, grove);
		} catch (final Exception treeException) {
			try {
				final OnnxVintner vintner = new OnnxVintner();
				final RegressorVine vine = vintner.toVine(modelBytes);
				petrify.fossilize(null, vine);
			} catch (final Exception vineException) {
				treeException.addSuppressed(vineException);
				throw treeException;
			}
		}
	}

	protected void compileScikit(final BuildTimePetrify petrify, final byte[] modelBytes, final String modelType) {
		final ScikitVintner vintner = new ScikitVintner();
		switch (modelType) {
			case MODEL_TYPE_CLASSIFIER -> {
				final ClassifierVine vine = vintner.toVine(modelBytes);
				petrify.fossilize(null, vine);
			}
			case MODEL_TYPE_REGRESSOR -> {
				final RegressorVine vine = vintner.toVine(modelBytes);
				petrify.fossilize(null, vine);
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

	protected Path resolveOutputPath(final String packageName, final String className) {
		final String packageDir = packageName.replace('.', File.separatorChar);
		return Path.of(outputDirectory, packageDir, className + ".class");
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

	protected byte[] readModelFile(final Path modelPath) throws MojoExecutionException {
		try {
			return Files.readAllBytes(modelPath);
		} catch (final IOException ioException) {
			throw new MojoExecutionException("failed to read modelFile:" + modelPath, ioException);
		}
	}

	protected void writeClassFile(final Path outputClassFile, final byte[] classBytes) throws MojoExecutionException {
		try {
			Files.createDirectories(outputClassFile.getParent());
			try (final OutputStream outputStream = buildContext.newFileOutputStream(outputClassFile.toFile())) {
				outputStream.write(classBytes);
			}
			buildContext.refresh(outputClassFile.toFile());
			getLog().info("writeClassFile() wrote " + classBytes.length + " bytes to:" + outputClassFile);
		} catch (final IOException ioException) {
			throw new MojoExecutionException("failed to write classFile:" + outputClassFile, ioException);
		}
	}
}
