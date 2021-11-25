package com.revolsys.util;

public class ThreadLocalCounter implements Counter {
  private final ThreadLocal<Long> counts = new ThreadLocal<>();

  private final String name;

  public ThreadLocalCounter(final String name) {
    this.name = name;
  }

  public ThreadLocalCounter(final String name, final Number count) {
    this.name = name;
    add(count);
  }

  @Override
  public long add() {
    return add(1L);
  }

  @Override
  public long add(final long count) {
    final long newCount = get() + count;
    set(newCount);
    return newCount;
  }

  @Override
  public long add(final Number count) {
    return add(count.longValue());
  }

  public void clear() {
    this.counts.set(null);
  }

  @Override
  public long decrement() {
    return add(-1);
  }

  @Override
  public long get() {
    final Long count = this.counts.get();
    if (count == null) {
      return 0;
    } else {
      return count;
    }
  }

  @Override
  public String getName() {
    return this.name;
  }

  public void set(final long count) {
    this.counts.set(count);
  }

  @Override
  public String toString() {
    return this.name + "=" + get();
  }
}
