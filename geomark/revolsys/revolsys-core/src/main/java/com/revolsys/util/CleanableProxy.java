package com.revolsys.util;

import java.lang.ref.Cleaner.Cleanable;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.revolsys.io.BaseCloseable;

public class CleanableProxy<R> implements BaseCloseable {

  public static <RI extends BaseCloseable> CleanableProxy<RI> newProxy(
    final Supplier<RI> resourceFactory, final Class<?>... interfaces) {
    return new CleanableProxy<>(resourceFactory, BaseCloseable::close, interfaces);
  }

  private Cleanable cleaner;

  private Reference<R> proxy = new PhantomReference<>(null, null);

  private Supplier<R> resourceFactory;

  private Class<?>[] resourceInterfaces;

  private final Consumer<? super R> closeAction;

  public CleanableProxy(final Supplier<R> resourceFactory, final Consumer<? super R> closeAction,
    final Class<?>[] interfaces) {
    this.resourceFactory = resourceFactory;
    this.resourceInterfaces = interfaces;
    this.closeAction = closeAction;
  }

  @Override
  public synchronized void close() {
    try {
      this.proxy.enqueue();
      if (this.cleaner != null) {
        this.cleaner.clean();
      }
    } finally {
      this.cleaner = null;
      this.resourceFactory = null;
      this.resourceInterfaces = null;
      this.proxy = null;
    }
  }

  public synchronized R get() {
    final R resource = this.proxy.get();
    if (resource == null) {
      final R newResource = this.resourceFactory.get();
      if (newResource != null) {
        final Class<?> resourceClass = newResource.getClass();
        final ClassLoader classLoader = resourceClass.getClassLoader();
        final SimpleInvocationHandler<R> handler = new SimpleInvocationHandler<>(newResource);
        @SuppressWarnings("unchecked")
        final R proxy = (R)Proxy.newProxyInstance(classLoader, this.resourceInterfaces, handler);
        final Consumer<? super R> closeAction = this.closeAction;
        this.cleaner = MemoryCleaner.register(proxy, () -> closeAction.accept(newResource));
        this.proxy = new WeakReference<>(proxy);
        return proxy;
      }
    }
    return resource;
  }

}
