package thad.gradle.ev3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.GsonBuilder;

import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.nativeplatform.HeaderExportingSourceSet;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeDependencySet;

import edu.wpi.first.toolchain.ToolchainDiscoverer;
import edu.wpi.first.toolchain.ToolchainExtension;
import jaci.gradle.PathUtils;
import jaci.gradle.deploy.artifact.NativeArtifact;
import jaci.gradle.deploy.context.DeployContext;
import jaci.gradle.deploy.sessions.IPSessionController;
import jaci.gradle.nativedeps.DelegatedDependencySet;

public class Ev3NativeArtifact extends NativeArtifact {

    public Ev3NativeArtifact(String name, Project project) {
        super(name, project);

        setTargetPlatform(Ev3Plugin.platform);

        setBuildType("<<GR_AUTO>>");

        getPostdeploy().add(new ContextWrapper(ctx -> {
            String artifFileName = getFilename();
            String binFile = jaci.gradle.PathUtils.combine(ctx.getWorkingDir(),
                    artifFileName == null ? getFile().get().getName() : artifFileName);
            ctx.execute("chmod +x \"" + binFile + "\"");

      if (debug) {
        ctx.execute("chmod +x /home/robot/robotCommand");
        ctx.execute("killall -9 gdbserver");
        ctx.execute("killall -9 " +  getFilename() == null ? getFile().get().getName() : getFilename());
        ctx.execute(". /home/robot/robotCommand");
      }
    }));
  }

  private List<String> arguments = new ArrayList<String>();

  public List<String> getArguments() {
      return arguments;
  }

  public void setArguments(List<String> arguments) {
      this.arguments = arguments;
  }


  private boolean debug = false;

  public boolean getDebug() {
      return debug;
  }

  public void setDebug(boolean debug) {
      this.debug = debug;
  }

  private int debugPort = 8348;

  public int getDebugPort() {
      return debugPort;
  }

  public void setDebugPort(int port) {
      this.debugPort = port;
  }

//   def robotCommand = { DeployContext ctx, Ev3NativeArtifact self ->
//     "${self.debug ? "nohup gdbserver 1.1.1.1:${self.debugPort}" : ''} \"<<BINARY>>\" ${self.arguments.join(" ")} ${self.debug ? "1>/dev/null 2>/dev/null &" : ''}"
//   }

  NativeBinarySpec _bin;

  @Override
  public String getBuildType() {
      String sup = super.getBuildType();
      if (!sup.equals("<<GR_AUTO>>"))
          return sup;

      return getDebug() ? "debug" : "release";
  }

   @Override
    public void deploy(DeployContext ctx) {
        super.deploy(ctx);

        // if (robotCommand) {
        //     String rCmd = null;
        //     if (robotCommand instanceof Closure)
        //         rCmd = (robotCommand as Closure).call([ctx, this]).toString()
        //     else if (robotCommand instanceof String)
        //         rCmd = (robotCommand as String)

        //     if (rCmd != null) {
        //         def binFile = PathUtils.combine(ctx.workingDir, filename ?: file.get().name)
        //         rCmd = rCmd.replace('<<BINARY>>', binFile)
        //         ctx.execute("echo '${rCmd}' > /home/robot/robotCommand")
        //     }
        // }
        boolean isWin = OperatingSystem.current().isWindows();

        String filebasename = getName() + "_" + ctx.getDeployLocation().getTarget().getName();//"${name}_${ctx.deployLocation.target.name}"
        File projectBuildDir = getProject().getBuildDir();
        File conffile = new File(projectBuildDir, "debug/" + filebasename + ".debugconfig");
        File gdbfile = new File(projectBuildDir, "debug/" + filebasename + ".gdbcommands");
        File cmdfile = new File(projectBuildDir, "debug/" + filebasename + (isWin ? ".bat" : ""));

        if (getDebug()) {
            String fname = getFilename();
            String binFile = PathUtils.combine(ctx.getWorkingDir(), fname != null ? fname : getFile().get().getName());
            String rCmd = "nohup gdbserver 1.1.1.1:" + getDebugPort() + " \"" + binFile + "\"" + String.join(" ", getArguments()) + " 1>/dev/null 2>/dev/null &";
            ctx.execute("echo \'" + rCmd + "\' > /home/robot/robotCommand");
            conffile.getParentFile().mkdirs();

            ctx.getLogger().withLock(new LoggerClosureWrapper((logger) -> {
                logger.log("====================================================================");
                logger.log("DEBUGGING ACTIVE ON PORT " + getDebugPort() + "!");
                logger.log("Launch debugger with " + (isWin ? "" : "./") + Paths.get(getProject().getRootDir().toURI()).relativize(Paths.get(cmdfile.toURI())).toString());
                logger.log("====================================================================");
            }));



            // Setup

            List<File> srcpaths = new ArrayList<>();
            List<File> headerpaths = new ArrayList<>();
            List<File> libpaths = new ArrayList<>();
            List<File> debugpaths = new ArrayList<>();
            List<File> libsrcpaths = new ArrayList<>();

            _bin.getInputs().withType(HeaderExportingSourceSet.class, (ss) -> {
                srcpaths.addAll(ss.getSource().getSrcDirs());
                srcpaths.addAll(ss.getExportedHeaders().getSrcDirs());
            });


            for (NativeDependencySet ds : _bin.getLibs()) {
                headerpaths.addAll(ds.getIncludeRoots().getFiles());
                libpaths.addAll(ds.getRuntimeFiles().getFiles());
                if (ds instanceof DelegatedDependencySet) {
                    DelegatedDependencySet dds = (DelegatedDependencySet)ds;
                    libsrcpaths.addAll(dds.getSourceFiles().getFiles());
                    debugpaths.addAll(dds.getDebugFiles().getFiles());
                }
            }

            if (ctx.getController() instanceof IPSessionController) {
                IPSessionController ip = (IPSessionController)ctx.getController();
                String filepath = getFile().get().getAbsolutePath().replaceAll("\\\\", "/");
                String target = ip.getHost() + ":" + getDebugPort();

                ToolchainDiscoverer toolchainD = getProject().getExtensions().getByType(ToolchainExtension.class).getByName("ev3").discover();
                String gdbpath = toolchainD.gdbFile().get().getAbsolutePath();
                String sysroot = toolchainD.sysroot().orElse(null).getAbsolutePath();

                // .debugconfig

                Map<String, Object> dbcfg = new HashMap<>();
                dbcfg.put("launchfile", filepath);
                dbcfg.put("target", target);
                dbcfg.put("gdb", gdbpath);
                dbcfg.put("sysroot", sysroot);
                dbcfg.put("srcpaths", srcpaths.stream().map(it -> it.getAbsolutePath()).toArray());
                dbcfg.put("headerpaths", headerpaths.stream().map(it -> it.getAbsolutePath()).toArray());
                dbcfg.put("libpaths", libpaths.stream().map(it -> it.getAbsolutePath()).toArray());
                dbcfg.put("debugpaths", debugpaths.stream().map(it -> it.getAbsolutePath()).toArray());
                dbcfg.put("libsrcpaths", libsrcpaths.stream().map(it -> it.getAbsolutePath()).toArray());
                dbcfg.put("arch", "elf32-littlearm");
                dbcfg.put("component", this.getComponent());

                GsonBuilder gbuilder = new GsonBuilder();
                gbuilder.setPrettyPrinting();
                try(BufferedWriter writer = Files.newBufferedWriter(conffile.toPath())) {
                    writer.write(gbuilder.create().toJson(dbcfg));
                    writer.flush();
                } catch (IOException ex) {
                }
            } else {
                ctx.getLogger().log("Session Controller isn't IP Compatible. No debug file written.");
            }
        } else {
            // Not debug, clear debug files
            if (conffile.exists()) conffile.delete();
            if (gdbfile.exists()) gdbfile.delete();
            if (cmdfile.exists()) cmdfile.delete();
        }
    }
}
