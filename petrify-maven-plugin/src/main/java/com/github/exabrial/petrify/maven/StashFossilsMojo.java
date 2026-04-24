package com.github.exabrial.petrify.maven;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Removes generated fossil class files from {@code ${project.build.outputDirectory}} after the JAR has been packaged.
 *
 * <p>
 * This goal runs at {@code post-integration-test} by default, which is after {@code package} (the JAR already contains the fossils) but
 * before {@code verify} (where static-analysis tools like SpotBugs would otherwise attempt to analyze the large generated classes). The
 * list of files to remove is read from the manifest written by the {@code fossilize} goal.
 *
 * <p>
 * When the plugin is loaded as an extension ({@code <extensions>true</extensions>}), this goal is injected automatically by
 * {@link PetrifyLifecycleParticipant}; manual configuration is not required.
 */
@Mojo(name = "stash-fossils", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class StashFossilsMojo extends AbstractMojo {
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(defaultValue = "${project.build.outputDirectory}")
	private String outputDirectory;

	@Parameter(property = "petrify.skip", defaultValue = "false")
	private boolean skip;

	@Override
	public void execute() throws MojoExecutionException {
		if (skip) {
			getLog().info("execute() petrify.skip is true, skipping");
		} else {
			final Path manifestFile = resolveManifestPath();
			if (Files.exists(manifestFile)) {
				stashFossils(manifestFile);
			} else {
				getLog().debug("execute() no manifest found at:" + manifestFile + ", nothing to stash");
			}
		}
	}

	protected void stashFossils(final Path manifestFile) throws MojoExecutionException {
		try {
			final List<String> relativePaths = Files.readAllLines(manifestFile, StandardCharsets.UTF_8);
			int stashedCount = 0;
			for (final String relativePath : relativePaths) {
				if (!relativePath.isBlank()) {
					final Path classFile = Path.of(outputDirectory, relativePath);
					if (Files.exists(classFile)) {
						Files.delete(classFile);
						stashedCount++;
						getLog().debug("stashFossils() deleted classFile:" + classFile);
					}
				}
			}
			getLog().info("stashFossils() removed fossils:" + stashedCount + " from:" + outputDirectory);
		} catch (final IOException ioException) {
			throw new MojoExecutionException("stashFossils() failed to read manifest or delete fossil classes", ioException);
		}
	}

	protected Path resolveManifestPath() {
		return Path.of(project.getBuild().getDirectory(), FossilizeMojo.PETRIFY_MANIFEST_DIR, FossilizeMojo.PETRIFY_MANIFEST_FILE);
	}
}
