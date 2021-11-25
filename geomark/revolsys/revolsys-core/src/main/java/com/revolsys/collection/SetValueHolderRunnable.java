package com.revolsys.collection;

/**
 * A value holder that implements Runnable. When run it will change the value to new value.
 */
public class SetValueHolderRunnable<T> extends SimpleValueHolder<T> implements Runnable {
  private final T newValue;

  public SetValueHolderRunnable(final T value, final T newValue) {
    super(value);
    this.newValue = newValue;
  }

  @Override
  public void run() {
    setValue(this.newValue);
  }
}
