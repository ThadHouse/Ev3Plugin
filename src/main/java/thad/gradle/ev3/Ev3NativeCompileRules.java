package thad.gradle.ev3;

import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.platform.base.BinaryContainer;

public class Ev3NativeCompileRules extends RuleSource {
  public static final String[] linuxCrossCompilerArgs = { "-std=c++14", "-Wformat=2", "-pedantic", "-Wno-psabi", "-g",
      "-Wno-unused-parameter", "-Wno-error=deprecated-declarations", "-fPIC", "-rdynamic", "-pthread" };
  public static final String[] linuxCrossCCompilerArgs = { "-Wformat=2", "-Wno-psabi", "-g", "-Wno-unused-parameter",
      "-fPIC", "-rdynamic", "-pthread" };
  public static final String[] linuxCrossLinkerArgs = { "-rdynamic", "-pthread", "-ldl" };
  public static final String[] linuxCrossReleaseCompilerArgs = { "-O2" };
  public static final String[] linuxCrossDebugCompilerArgs = { "-Og" };

  @Mutate
  public void addBinaryFlags(BinaryContainer binaries) {
    binaries.withType(NativeBinarySpec.class, bin -> {
      if (bin.getTargetPlatform().getName().equals(Ev3Plugin.platform)) {
        bin.getCppCompiler().args(linuxCrossCompilerArgs);
        bin.getcCompiler().args(linuxCrossCCompilerArgs);
        bin.getLinker().args(linuxCrossLinkerArgs);
        if (bin.getBuildType().getName().contains("debug")) {
            bin.getCppCompiler().args(linuxCrossDebugCompilerArgs);
            bin.getcCompiler().args(linuxCrossDebugCompilerArgs);
            bin.getCppCompiler().define("DEBUG");
        } else {
          bin.getCppCompiler().args(linuxCrossReleaseCompilerArgs);
          bin.getcCompiler().args(linuxCrossReleaseCompilerArgs);
        }
      }
    });
  }
}
