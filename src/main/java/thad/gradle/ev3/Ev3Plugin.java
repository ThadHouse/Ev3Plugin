package thad.gradle.ev3;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.TaskProvider;

import jaci.gradle.deploy.DeployExtension;
import jaci.gradle.deploy.artifact.Artifact;
import jaci.gradle.deploy.artifact.ArtifactDeployTask;
import jaci.gradle.deploy.artifact.ArtifactsExtension;
import jaci.gradle.deploy.target.TargetsExtension;
import thad.gradle.ev3.toolchain.Ev3ToolchainPlugin;

public class Ev3Plugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.getPluginManager().withPlugin("edu.wpi.first.Toolchain", (AppliedPlugin toolchain) -> {
      // Toolchain plugin has been applied
      project.getPluginManager().apply(Ev3ToolchainPlugin.class);
      project.getPluginManager().apply(Ev3Rules.class);

    });

    project.getPluginManager().withPlugin("jaci.gradle.EmbeddedTools", (plugin) -> {
      TaskProvider<Ev3DebugInfoTask> debugInfoLazy = project.getTasks().register("writeDebugInfoEv3", Ev3DebugInfoTask.class);

        project.getTasks().withType(ArtifactDeployTask.class).configureEach((t) -> {
            t.dependsOn(debugInfoLazy);
        });

        try {
          TaskProvider grTask = project.getTasks().named("writeDebugInfo");
          debugInfoLazy.configure(arg -> {
            arg.dependsOn(grTask);
          });
        } catch (Exception ex) {

        }


      DeployExtension deployExtension = project.getExtensions().getByType(DeployExtension.class);
      ExtensionAware artifactExtensionAware = (ExtensionAware)deployExtension.getArtifacts();
      ExtensionAware targetExtensionAware = (ExtensionAware)deployExtension.getTargets();
      ArtifactsExtension artifactExtension = deployExtension.getArtifacts();
      TargetsExtension targetExtension = deployExtension.getTargets();

      artifactExtensionAware.getExtensions().add("ev3Artifact", new ExtensionWrapper<Artifact>((name, config) -> {
        return artifactExtension.artifact(name, Ev3NativeArtifact.class, config);
      }));

      targetExtensionAware.getExtensions().add("ev3", new ExtensionWrapper<Object>((name, config) -> {
        return targetExtension.target(name, Ev3.class, config);
      }));
    });
  }

}
