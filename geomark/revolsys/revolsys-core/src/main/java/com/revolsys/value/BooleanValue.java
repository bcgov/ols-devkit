package com.revolsys.value;

public interface BooleanValue extends ValueHolder<Boolean> {
  default boolean isFalse() {
    return !isTrue();
  }

  default boolean isTrue() {
    return getValue();
  }

  default void run(final boolean newValue, final Runnable runnable) {
    final boolean oldValue = setValue(newValue);
    try {
      runnable.run();
    } finally {
      setValue(oldValue);
    }
  }
}
