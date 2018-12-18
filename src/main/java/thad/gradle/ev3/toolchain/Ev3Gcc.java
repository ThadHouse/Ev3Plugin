package thad.gradle.ev3.toolchain;

import edu.wpi.first.toolchain.GccToolChain;
import edu.wpi.first.toolchain.ToolchainOptions;

public class Ev3Gcc extends GccToolChain {
  public Ev3Gcc(ToolchainOptions options) {
    super(options);
  }

  @Override
  protected String getTypeName() {
    return "Ev3Gcc";
  }
}
