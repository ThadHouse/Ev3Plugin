package thad.gradle.ev3;

import org.gradle.api.Project;

import jaci.gradle.deploy.artifact.NativeArtifact;

public class Ev3NativeArtifact extends NativeArtifact {

  public Ev3NativeArtifact(String name, Project project) {
    super(name, project);

    setTargetPlatform("linuxev3");

    setBuildType("<<GR_AUTO>>");

    getPostdeploy().add(new ContextWrapper((ctx) -> {
      String artifFileName = getFilename();
      String binFile = jaci.gradle.PathUtils.combine(ctx.getWorkingDir(), artifFileName == null ? getFile().get().getName() : artifFileName);
      ctx.execute("chmod +x \"" + binFile + "\"");
    }));
  }

  // Todo: Add debugging support
  private boolean debug;

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public boolean getDebug() {
    return this.debug;
  }

  @Override
  public String getBuildType() {
      String sup = super.getBuildType();
      if (!sup.equals("<<GR_AUTO>>"))
          return sup;

      return getDebug() ? "debug" : "release";
  }



}
