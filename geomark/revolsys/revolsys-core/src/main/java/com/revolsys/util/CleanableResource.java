package com.revolsys.util;

import java.lang.ref.Cleaner.Cleanable;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.aop.framework.ProxyFactory;

import com.revolsys.io.BaseCloseable;

public class CleanableResource<R> implements BaseCloseable {

  public static <RI extends BaseCloseable> CleanableResource<RI> newResource(
    final Supplier<RI> resourceFactory) {
    return new CleanableResource<>(resourceFactory, BaseCloseable::close);
  }

  private Cleanable cleaner;

  private Reference<R> reference = new PhantomReference<>(null, null);

  private Supplier<R> resourceFactory;

  private final Consumer<? super R> closeAction;

  public CleanableResource(final Supplier<R> resourceFactory,
    final Consumer<? super R> closeAction) {
    this.resourceFactory = resourceFactory;
    this.closeAction = closeAction;
  }

  @Override
  public synchronized void close() {
    try {
      this.reference.enqueue();
      if (this.cleaner != null) {
        this.cleaner.clean();
      }
    } finally {
      this.cleaner = null;
      this.resourceFactory = null;
      this.reference = null;
    }
  }

  @SuppressWarnings("unchecked")
  public synchronized R get() {
    R proxy = this.reference.get();
    if (proxy == null) {
      final R resource = this.resourceFactory.get();
      if (resource != null) {
        final ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetClass(resource.getClass());
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.setTarget(resource);
        proxy = (R)proxyFactory.getProxy();
        final Consumer<? super R> closeAction = this.closeAction;
        this.cleaner = MemoryCleaner.register(proxy, () -> closeAction.accept(resource));
        this.reference = new WeakReference<>(proxy);
      }
    }
    return proxy;
  }

}
