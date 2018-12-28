package thad.gradle.ev3;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.ExtensionAware;

import edu.wpi.first.gradlerio.frc.DebugInfoTask;
import jaci.gradle.deploy.DeployExtension;
import jaci.gradle.deploy.artifact.Artifact;
import jaci.gradle.deploy.artifact.ArtifactsExtension;
import jaci.gradle.deploy.target.TargetsExtension;
import thad.gradle.ev3.toolchain.Ev3ToolchainPlugin;

public class Ev3Plugin implements Plugin<Project> {

  public static final String platform = "linuxev3";

  @Override
  public void apply(Project project) {
    project.getPluginManager().withPlugin("edu.wpi.first.Toolchain", (AppliedPlugin toolchain) -> {
      // Toolchain plugin has been applied
      project.getPluginManager().apply(Ev3ToolchainPlugin.class);
      project.getPluginManager().apply(Ev3Rules.class);
      project.getPluginManager().apply(Ev3NativeCompileRules.class);

    });

    project.getPluginManager().withPlugin("jaci.gradle.EmbeddedTools", (plugin) -> {
      project.getTasks().withType(DebugInfoTask.class, task -> {
        System.out.println("Adding tasks");
        task.getExtraArtifacts().add(new DebugInfoClosureWrapper((art, cfg) -> {
          if (art instanceof Ev3NativeArtifact) {
            art.getTargets().all(o -> {
              String target = (String)o;
              Map<String, Object> items = new HashMap<>();
              items.put("artifact", art.getName());
              items.put("target", target);
              items.put("component", ((Ev3NativeArtifact)art).getComponent());
              items.put("debugfile", art.getName() + "_" + target + ".debugconfig");
              items.put("language", "cpp");
              cfg.add(items);
            });
          }
        }));
      });

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
