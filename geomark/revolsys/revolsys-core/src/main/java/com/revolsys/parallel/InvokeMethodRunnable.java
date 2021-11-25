package com.revolsys.parallel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jeometry.common.logging.Logs;

public class InvokeMethodRunnable implements Runnable {
  private final Object object;

  private final Method method;

  private final Object[] parameters;

  public InvokeMethodRunnable(final Method method, final Object... parameters) {
    this(null, method, parameters);
  }

  public InvokeMethodRunnable(final Object object, final Method method,
    final Object... parameters) {
    this.object = object;
    this.method = method;
    this.parameters = parameters;
  }

  @Override
  public void run() {
    try {
      this.method.invoke(this.object, this.parameters);
    } catch (final InvocationTargetException e) {
      Logs.error(this.method.getClass(), e.getTargetException());
    } catch (final Throwable e) {
      Logs.error(this.method.getClass(), e);
    }
  }
}
