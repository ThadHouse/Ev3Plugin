package thad.gradle.ev3;

import groovy.lang.Closure;

public class ExtensionWrapper<O> extends Closure<O> {
  private static final long serialVersionUID = -1425538208792974956L;

  @FunctionalInterface
  public interface ExtensionFunction<O> {
    O call(String name, Closure<Object> arg);
  }

  private ExtensionFunction<O> func;

  public ExtensionWrapper(ExtensionFunction<O> func) {
    super(null);
    this.func = func;
  }

  O doCall(String name, Closure<Object> config) {
    return func.call(name, config);
  }
}
