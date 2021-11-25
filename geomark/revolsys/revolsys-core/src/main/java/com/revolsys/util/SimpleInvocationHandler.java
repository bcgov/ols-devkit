package com.revolsys.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SimpleInvocationHandler<R> implements InvocationHandler {

  private final R object;

  public SimpleInvocationHandler(final R object) {
    this.object = object;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args)
    throws Throwable {
    try {
      return method.invoke(this.object, args);
    } catch (final InvocationTargetException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof Error) {
        throw (Error)cause;
      } else if (cause instanceof RuntimeException) {
        throw (RuntimeException)cause;
      } else {
        throw e;
      }
    }
  }

}
