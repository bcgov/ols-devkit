package com.revolsys.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class CloseableValueHolder<R> extends SimpleValueWrapper<R> {

  public static <R2> CloseableValueHolder<R2> lambda(final Supplier<R2> valueFactory,
    final Consumer<R2> valueCloseFunction) {
    return new LambaCloseableValueHolder<>(valueFactory, valueCloseFunction);
  }

  private final ValueWrapper<R> closeable = newCloseable();

  private int referenceCount = 0;

  public CloseableValueHolder() {
  }

  @Override
  public synchronized void close() {
    if (!isClosed()) {
      this.referenceCount = Integer.MIN_VALUE;
      try {
        closeBefore();
      } finally {
        try {
          final R value = this.value;
          this.value = null;
          if (value != null) {
            valueClose(value);
          }
        } finally {
          closeAfter();
        }
      }
    }
  }

  protected void closeAfter() {
  }

  protected void closeBefore() {
  }

  @Override
  public synchronized ValueWrapper<R> connect() {
    if (isClosed()) {
      throw new IllegalStateException("Resource closed");
    } else {
      getValue();
      return this.closeable;
    }
  }

  protected synchronized void disconnect() {
    if (!isClosed()) {
      final boolean wasOpen = this.referenceCount > 0;
      R value = null;
      this.referenceCount--;
      if (this.referenceCount <= 0) {
        value = this.value;
        this.value = null;
      }
      if (value != null && wasOpen) {
        valueClose(value);
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @Override
  public synchronized R getValue() {
    if (isClosed()) {
      throw new IllegalStateException("Value closed");
    } else {
      this.referenceCount++;
      if (this.value == null) {
        final R value = valueNew();
        if (value == null) {
          return null;
        } else {
          this.value = value;
        }
      }
      return this.value;
    }
  }

  public boolean isClosed() {
    return this.referenceCount == Integer.MIN_VALUE;
  }

  protected ValueHolderWrapper<R> newCloseable() {
    return new ValueHolderWrapper<>() {
      @Override
      public void close() {
        disconnect();
      }

      public com.revolsys.util.SimpleValueWrapper<R> getValueHolder() {
        return CloseableValueHolder.this;
      }
    };
  }

  protected abstract void valueClose(final R value);

  @Override
  public void valueConsume(final Consumer<R> action) {
    try {
      super.valueConsume(action);
    } finally {
      disconnect();
    }
  }

  @Override
  public synchronized void valueConsumeSync(final Consumer<R> action) {
    try {
      super.valueConsume(action);
    } finally {
      disconnect();
    }
  }

  @Override
  public <V> V valueFunction(final Function<R, V> action) {
    try {
      return super.valueFunction(action);
    } finally {
      disconnect();
    }
  }

  @Override
  public <V> V valueFunction(final Function<R, V> action, final V defaultValue) {
    try {
      return super.valueFunction(action, defaultValue);
    } finally {
      disconnect();
    }
  }

  @Override
  public synchronized <V> V valueFunctionSync(final Function<R, V> action) {
    try {
      return super.valueFunction(action);
    } finally {
      disconnect();
    }
  }

  @Override
  public synchronized <V> V valueFunctionSync(final Function<R, V> action, final V defaultValue) {
    try {
      return super.valueFunction(action, defaultValue);
    } finally {
      disconnect();
    }
  }

  protected abstract R valueNew();

}
