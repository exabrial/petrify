package com.github.exabrial.petrify.maven;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;

/**
 * Lifecycle participant that automatically injects the {@code stash-fossils} goal into the build when the {@code fossilize} goal is
 * configured and the plugin is loaded as an extension ({@code <extensions>true</extensions>}).
 *
 * <p>
 * The injected {@code stash-fossils} execution runs at {@code post-integration-test}, removing generated fossil classes from
 * {@code target/classes} after the JAR has been packaged but before static-analysis tools like SpotBugs run at {@code verify}.
 */
@Named
@Singleton
public class PetrifyLifecycleParticipant extends AbstractMavenLifecycleParticipant {
	private static final String GROUP_ID = "com.github.exabrial";
	private static final String ARTIFACT_ID = "petrify-maven-plugin";
	private static final String FOSSILIZE_GOAL = "fossilize";
	private static final String STASH_GOAL = "stash-fossils";
	private static final String STASH_EXECUTION_ID = "petrify-stash-fossils";
	private static final String STASH_PHASE = "post-integration-test";

	@Override
	public void afterProjectsRead(final MavenSession session) throws MavenExecutionException {
		for (final MavenProject project : session.getProjects()) {
			final Plugin petrifyPlugin = findPetrifyPlugin(project);
			if (petrifyPlugin != null && hasFossilizeGoal(petrifyPlugin) && !hasStashGoal(petrifyPlugin)) {
				injectStashExecution(petrifyPlugin);
			}
		}
	}

	private Plugin findPetrifyPlugin(final MavenProject project) {
		Plugin result = null;
		for (final Plugin plugin : project.getBuildPlugins()) {
			if (GROUP_ID.equals(plugin.getGroupId()) && ARTIFACT_ID.equals(plugin.getArtifactId())) {
				result = plugin;
				break;
			}
		}
		return result;
	}

	private boolean hasFossilizeGoal(final Plugin plugin) {
		boolean result = false;
		for (final PluginExecution execution : plugin.getExecutions()) {
			if (execution.getGoals().contains(FOSSILIZE_GOAL)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private boolean hasStashGoal(final Plugin plugin) {
		boolean result = false;
		for (final PluginExecution execution : plugin.getExecutions()) {
			if (execution.getGoals().contains(STASH_GOAL)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void injectStashExecution(final Plugin plugin) {
		final PluginExecution stashExecution = new PluginExecution();
		stashExecution.setId(STASH_EXECUTION_ID);
		stashExecution.setPhase(STASH_PHASE);
		stashExecution.addGoal(STASH_GOAL);
		plugin.addExecution(stashExecution);
	}
}
