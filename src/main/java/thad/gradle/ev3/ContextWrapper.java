package thad.gradle.ev3;

import groovy.lang.Closure;
import jaci.gradle.deploy.context.DeployContext;

public class ContextWrapper extends Closure<Object> {
  private static final long serialVersionUID = 4460019495922979964L;

  @FunctionalInterface
  public interface ContextFunction {
    void call(DeployContext arg);
  }

  private ContextFunction func;

  public ContextWrapper(ContextFunction func) {
    super(null);
    this.func = func;
  }

  void doCall(DeployContext arg) {
    func.call(arg);
  }
}
