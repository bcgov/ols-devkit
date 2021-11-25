package com.revolsys.util;

import java.util.function.Consumer;
import java.util.function.Function;

import com.revolsys.io.BaseCloseable;

public interface ValueWrapper<R> extends BaseCloseable {

  default ValueWrapper<R> connect() {
    return this;
  }

  R getValue();

  default void valueConsume(final Consumer<R> action) {
    final R value = getValue();
    if (value != null) {
      action.accept(value);
    }
  }

  default void valueConsumeSync(final Consumer<R> action) {
    synchronized (this) {
      valueConsume(action);
    }
  }

  default <V> V valueFunction(final Function<R, V> action) {
    final R value = getValue();
    if (value == null) {
      return null;
    } else {
      return action.apply(value);
    }
  }

  default <V> V valueFunction(final Function<R, V> action, final V defaultValue) {
    final R value = getValue();
    if (value == null) {
      return defaultValue;
    } else {
      return action.apply(value);
    }
  }

  default <V> V valueFunctionSync(final Function<R, V> action) {
    synchronized (this) {
      return valueFunction(action);
    }
  }

  default <V> V valueFunctionSync(final Function<R, V> action, final V defaultValue) {
    synchronized (this) {
      return valueFunction(action, defaultValue);
    }
  }

}
