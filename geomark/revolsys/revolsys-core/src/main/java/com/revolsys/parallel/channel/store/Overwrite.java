package com.revolsys.parallel.channel.store;

import com.revolsys.parallel.channel.ChannelValueStore;

public class Overwrite<T> extends ChannelValueStore<T> {
  private int state = EMPTY;

  private T value;

  @Override
  protected Object clone() {
    return new Overwrite<T>();
  }

  @Override
  protected T get() {
    this.state = EMPTY;
    final T o = this.value;
    this.value = null;
    return o;
  }

  @Override
  protected int getState() {
    return this.state;
  }

  @Override
  protected void put(final T value) {
    this.state = NONEMPTYFULL;
    this.value = value;
  }

  @Override
  public String toString() {
    if (this.value == null) {
      return "[]";
    } else {
      return "[" + this.value + "]";
    }
  }
}
