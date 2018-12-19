package thad.gradle.ev3;

import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.platform.base.BinaryTasks;


import jaci.gradle.deploy.DeployExtension;
import jaci.gradle.deploy.artifact.ArtifactsExtension;

public class Ev3Rules extends RuleSource {
  @BinaryTasks
  public void createNativeLibraryDeployTasks(final ModelMap<Task> tasks, final ExtensionContainer ext, final NativeBinarySpec binary) {
    DeployExtension deployExt = ext.getByType(DeployExtension.class);
      ArtifactsExtension artifacts = deployExt.getArtifacts();

      artifacts.withType(Ev3NativeArtifact.class).matching((art) -> {
        return art.getComponent().equalsIgnoreCase(binary.getComponent().getName()) &&
        art.getTargetPlatform().equalsIgnoreCase(binary.getTargetPlatform().getName());
      }).all((art) -> {
        art._bin = binary;
      });
  }
}
