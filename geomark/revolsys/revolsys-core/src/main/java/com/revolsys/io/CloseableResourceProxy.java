package com.revolsys.io;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

public class CloseableResourceProxy<R extends BaseCloseable> implements BaseCloseable {

  private class CloseableResourceHandler implements InvocationHandler {

    private int referenceCount = 1;

    private R resource;

    private CloseableResourceHandler(final R resource) {
      this.resource = resource;
    }

    private void disconnect() {
      if (this.resource != null) {
        final R resourceToClose;
        synchronized (CloseableResourceProxy.this) {
          this.referenceCount--;
          if (this.referenceCount <= 0) {
            CloseableResourceProxy.this.resourceHandler = null;
            CloseableResourceProxy.this.resourceProxy = null;
            resourceToClose = this.resource;
            this.resource = null;
            this.referenceCount = 0;
          } else {
            resourceToClose = null;
          }
        }
        if (resourceToClose != null) {
          resourceToClose.close();
        }
      }
    }

    private void increment() {
      this.referenceCount++;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
      throws Throwable {

      if (args == null && "close".equals(method.getName())) {
        disconnect();
        return null;
      } else {
        final R resource;
        synchronized (CloseableResourceProxy.this) {
          resource = this.resource;
        }
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

    @SuppressWarnings("unchecked")
    private R newProxy() {
      final Class<?> resourceClass = this.resource.getClass();
      final ClassLoader classLoader = resourceClass.getClassLoader();
      final Object proxy = Proxy.newProxyInstance(classLoader,
        CloseableResourceProxy.this.resourceInterfaces, this);
      return (R)proxy;
    }

    private void resourceClose() {
      final R resourceToClose;
      synchronized (CloseableResourceProxy.this) {
        resourceToClose = this.resource;
        this.resource = null;
        this.referenceCount = 0;
      }
      if (resourceToClose != null) {
        resourceToClose.close();
      }
    }
  }

  public static <RS extends BaseCloseable> CloseableResourceProxy<RS> newProxy(
    final Supplier<RS> resourceFactory, final Class<?>... interfaces) {
    return new CloseableResourceProxy<>(resourceFactory, interfaces);
  }

  private Supplier<R> resourceFactory;

  private CloseableResourceHandler resourceHandler;

  private R resourceProxy;

  private final Class<?>[] resourceInterfaces;

  public CloseableResourceProxy(final Supplier<R> resourceFactory, final Class<?>[] interfaces) {
    this.resourceFactory = resourceFactory;
    this.resourceInterfaces = interfaces;
  }

  @Override
  public synchronized void close() {
    final CloseableResourceHandler handler;
    handler = this.resourceHandler;
    this.resourceFactory = null;
    this.resourceHandler = null;
    if (handler != null) {
      handler.resourceClose();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  public synchronized R getResource() {
    if (this.resourceFactory == null) {
      throw new IllegalStateException("Resource closed");
    } else {
      if (this.resourceHandler == null) {
        final R resource = this.resourceFactory.get();
        if (resource == null) {
          return null;
        } else {
          this.resourceHandler = new CloseableResourceHandler(resource);
          this.resourceProxy = this.resourceHandler.newProxy();
        }
      } else {
        this.resourceHandler.increment();
      }
    }
    return this.resourceProxy;
  }

}
