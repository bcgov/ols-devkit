package com.revolsys.util;

public class NegativeCounter implements Counter {

  private final String name;

  private final Counter counter;

  public NegativeCounter(final String name, final Counter counter) {
    this.name = name;
    this.counter = counter;
  }

  @Override
  public long add() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long add(final long count) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long add(final Number count) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long decrement() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long get() {
    return -this.counter.get();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.name + "=" + get();
  }
}
