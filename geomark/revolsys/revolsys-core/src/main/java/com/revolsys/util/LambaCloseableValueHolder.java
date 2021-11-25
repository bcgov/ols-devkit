package com.revolsys.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LambaCloseableValueHolder<R> extends CloseableValueHolder<R> {

  private Supplier<R> valueFactory;

  private Consumer<R> valueCloseFunction;

  public LambaCloseableValueHolder(final Supplier<R> valueFactory,
    final Consumer<R> valueCloseFunction) {
    this.valueFactory = valueFactory;
    this.valueCloseFunction = valueCloseFunction;
  }

  @Override
  protected void closeAfter() {
    this.valueFactory = null;
    this.valueCloseFunction = null;
  }

  @Override
  protected void valueClose(final R value) {
    if (this.valueCloseFunction != null) {
      this.valueCloseFunction.accept(value);
    }
  }

  @Override
  protected R valueNew() {
    if (this.valueFactory == null) {
      throw new IllegalStateException("Value is closed");
    }
    return this.valueFactory.get();
  }
}
