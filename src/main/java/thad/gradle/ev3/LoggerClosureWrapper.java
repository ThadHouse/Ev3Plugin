package thad.gradle.ev3;

import groovy.lang.Closure;
import jaci.gradle.log.ETLogger;

public class LoggerClosureWrapper extends Closure<Object> {

  private static final long serialVersionUID = 69660595215665502L;

  @FunctionalInterface
  public interface ClosureFunction {
    void call(ETLogger logger);
  }

  private ClosureFunction func;

  public LoggerClosureWrapper(ClosureFunction func) {
    super(null);
    this.func = func;
  }

  void doCall(ETLogger logger) {
    func.call(logger);
  }
}
