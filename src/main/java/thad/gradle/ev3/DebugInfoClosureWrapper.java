package thad.gradle.ev3;

import java.util.List;
import java.util.Map;

import groovy.lang.Closure;
import jaci.gradle.deploy.artifact.Artifact;

public class DebugInfoClosureWrapper extends Closure<Object> {

  private static final long serialVersionUID = -9086425133213688655L;

  @FunctionalInterface
  public interface DebugInfoFunction {
    void call(Artifact art, List<Map<String, Object>> cfg);
  }

  private DebugInfoFunction func;


  public DebugInfoClosureWrapper(DebugInfoFunction func) {
    super(null);
    this.func = func;
  }

  void doCall(Artifact art, List<Map<String, Object>> cfg) {
    func.call(art, cfg);
  }
}
