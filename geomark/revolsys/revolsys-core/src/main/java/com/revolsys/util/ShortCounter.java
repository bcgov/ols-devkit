package com.revolsys.util;

import java.util.Map;

public final class ShortCounter extends Number {
  private static final short ONE = (short)1;

  private static final short MINUS_ONE = (short)-1;

  private static final short ZERO = (short)0;

  private static final long serialVersionUID = 1L;

  public static <K> short decrement(final Map<K, ShortCounter> counts, final K key) {
    if (key == null) {
      return ZERO;
    } else {
      synchronized (counts) {
        ShortCounter counter = counts.get(key);
        if (counter == null) {
          counter = new ShortCounter(MINUS_ONE);
          counts.put(key, counter);
          return MINUS_ONE;
        } else {
          final short count = counter.decrementAndGet();
          if (count == ZERO) {
            counts.remove(counter);
          }
          return count;
        }
      }
    }
  }

  public static <K> short increment(final Map<K, ShortCounter> counts, final K key) {
    if (key == null) {
      return ZERO;
    } else {
      synchronized (counts) {
        ShortCounter counter = counts.get(key);
        if (counter == null) {
          counter = new ShortCounter(ONE);
          counts.put(key, counter);
          return ONE;
        } else {
          return counter.incrementAndGet();
        }
      }
    }
  }

  private short count = 0;

  public ShortCounter() {
  }

  public ShortCounter(final short count) {
    this.count = count;
  }

  public synchronized short decrementAndGet() {
    return --this.count;
  }

  @Override
  public double doubleValue() {
    return this.count;
  }

  @Override
  public float floatValue() {
    return this.count;
  }

  public final short get() {
    return this.count;
  }

  public synchronized short getAndDecrement() {
    return --this.count;
  }

  public synchronized short getAndIncrement() {
    return ++this.count;
  }

  public synchronized short incrementAndGet() {
    return ++this.count;
  }

  @Override
  public int intValue() {
    return this.count;
  }

  @Override
  public long longValue() {
    return this.count;
  }

  @Override
  public short shortValue() {
    return this.count;
  }

  @Override
  public String toString() {
    return Short.toString(this.count);
  }
}
