package com.revolsys.util;

public interface Counter {
  long add();

  long add(final long count);

  long add(final Number count);

  long decrement();

  long get();

  String getName();

  default Counter newNegative() {
    return new NegativeCounter(getName(), this);
  }
}
