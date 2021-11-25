package com.revolsys.util;

public class MultipleCounter implements Counter {
  private final Counter counter;

  private final Counter total;

  public MultipleCounter(final Counter total, final Counter counter) {
    this.total = total;
    this.counter = counter;
  }

  @Override
  public long add() {
    this.counter.add();
    return this.total.add();
  }

  @Override
  public long add(final long count) {
    this.counter.add(count);
    return this.total.add(count);
  }

  @Override
  public long add(final Number count) {
    this.counter.add(count);
    return this.total.add(count);
  }

  @Override
  public long decrement() {
    this.counter.decrement();
    return this.total.decrement();
  }

  @Override
  public long get() {
    return this.total.get();
  }

  @Override
  public String getName() {
    return this.total.getName();
  }

  @Override
  public String toString() {
    return this.total.toString();
  }

}
