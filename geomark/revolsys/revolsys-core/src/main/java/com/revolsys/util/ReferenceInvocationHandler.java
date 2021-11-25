package com.revolsys.util;

import java.lang.ref.Reference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReferenceInvocationHandler<R> implements InvocationHandler {

  private final Reference<R> reference;

  public ReferenceInvocationHandler(final Reference<R> reference) {
    this.reference = reference;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args)
    throws Throwable {

    final R resource = this.reference.get();
    if (resource == null) {
      throw new IllegalStateException("Resource is closed");
    } else {
      try {
        return method.invoke(resource, args);
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

}
