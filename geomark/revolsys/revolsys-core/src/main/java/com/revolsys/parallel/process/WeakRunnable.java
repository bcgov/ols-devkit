package com.revolsys.parallel.process;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class WeakRunnable<V> implements Runnable {

  private final Reference<V> reference;

  private final Consumer<V> action;

  public WeakRunnable(final V value, final Consumer<V> action) {
    this.reference = new WeakReference<>(value);
    this.action = action;
  }

  public boolean isValid() {
    return this.reference.get() != null;
  }

  @Override
  public void run() {
    final V value = this.reference.get();
    this.action.accept(value);
  }

}
