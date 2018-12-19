package thad.gradle.ev3;

import groovy.lang.Closure;

public class EmptyClosureWrapper extends Closure<Object> {

  private static final long serialVersionUID = 69660595215665502L;

  @FunctionalInterface
  public interface ClosureFunction {
    void call();
  }

  private ClosureFunction func;

  public EmptyClosureWrapper(ClosureFunction func) {
    super(null);
    this.func = func;
  }

  void doCall() {
    func.call();
  }
}
