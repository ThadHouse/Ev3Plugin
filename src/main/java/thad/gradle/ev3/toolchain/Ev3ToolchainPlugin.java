package thad.gradle.ev3.toolchain;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem;

import edu.wpi.first.toolchain.ToolchainDiscoverer;
import edu.wpi.first.toolchain.AbstractToolchainInstaller;
import edu.wpi.first.toolchain.DefaultToolchainInstaller;
import edu.wpi.first.toolchain.ToolchainDescriptor;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.ToolchainPlugin;
import edu.wpi.first.toolchain.ToolchainRegistrar;
import thad.gradle.ev3.Ev3Plugin;

public class Ev3ToolchainPlugin implements Plugin<Project> {

  private Ev3ToolchainExtension ev3Ext;
  private Project project;

  @Override
  public void apply(Project project) {
    this.project = project;

    ev3Ext = project.getExtensions().create("ev3Toolchain", Ev3ToolchainExtension.class);

    ToolchainExtension toolchainExt = project.getExtensions().getByType(ToolchainExtension.class);

    ToolchainDescriptor descriptor = new ToolchainDescriptor("ev3", "ev3Gcc", new ToolchainRegistrar<Ev3Gcc>(Ev3Gcc.class, project));

    descriptor.setToolchainPlatforms(Ev3Plugin.platform);
    descriptor.setOptional(false);
    descriptor.getDiscoverers().all((ToolchainDiscoverer disc) -> {
        disc.configureVersions(ev3Ext.versionLow, ev3Ext.versionHigh);
    });

    toolchainExt.add(descriptor);

    project.afterEvaluate((Project proj) -> {
        populateDescriptor(descriptor);
    });

    project.getPluginManager().apply(Ev3ToolchainRules.class);
  }

  public static File toolchainInstallLoc(String vers) {
    return new File(ToolchainPlugin.pluginHome(), vers);
}

public String composeTool(String toolName) {
    String ev3Version = ev3Ext.toolchainVersion.split("-")[0].toLowerCase();
    String exeSuffix = OperatingSystem.current().isWindows() ? ".exe" : "";
    return "arm-" + ev3Version + "-linux-gnueabi-" + toolName + exeSuffix;
}

public void populateDescriptor(ToolchainDescriptor descriptor) {
    String ev3Version = ev3Ext.toolchainVersion.split("-")[0].toLowerCase();
    File installLoc = toolchainInstallLoc(ev3Version);

    descriptor.getDiscoverers().add(new ToolchainDiscoverer("GradleUserDir", installLoc, this::composeTool));
    descriptor.getDiscoverers().addAll(ToolchainDiscoverer.forSystemPath(project, this::composeTool));

    try {
        descriptor.getInstallers().add(installerFor(OperatingSystem.LINUX, installLoc, ev3Version));
        descriptor.getInstallers().add(installerFor(OperatingSystem.WINDOWS, installLoc, ev3Version));
        descriptor.getInstallers().add(installerFor(OperatingSystem.MAC_OS, installLoc, ev3Version));
    } catch (MalformedURLException e) {
        throw new GradleException("Malformed Toolchain URL", e);
    }
}

private AbstractToolchainInstaller installerFor(OperatingSystem os, File installDir, String subdir) throws MalformedURLException {
    URL url = toolchainDownloadUrl(toolchainRemoteFile());
    return new DefaultToolchainInstaller(os, url, installDir, subdir);
}

private String toolchainRemoteFile() {
    String[] desiredVersion = ev3Ext.toolchainVersion.split("-");

    String platformId = OperatingSystem.current().isWindows() ? "Windows" : OperatingSystem.current().isMacOsX() ? "Mac" : "Linux";
    String ext = OperatingSystem.current().isWindows() ? "zip" : "tar.gz";
    return desiredVersion[0] + "-" + platformId + "-Toolchain-" + desiredVersion[1] + "." + ext;
}

private URL toolchainDownloadUrl(String file) throws MalformedURLException {
    return new URL("https://github.com/ThadHouse/ev3dev-toolchain/releases/download/" + ev3Ext.toolchainTag + "/" + file);
}


}
