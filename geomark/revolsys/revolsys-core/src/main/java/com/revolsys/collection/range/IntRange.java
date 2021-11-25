package com.revolsys.collection.range;

import org.jeometry.common.number.Integers;

public class IntRange extends AbstractNumberRange<Integer> {
  public static boolean contains(int from, int to, final int number) {
    if (from > to) {
      final int n = from;
      from = to;
      to = n;
    }
    return from <= number && number <= to;
  }

  private int from;

  private int to;

  public IntRange() {
  }

  public IntRange(final int value) {
    this(value, value);
  }

  public IntRange(final int from, final int to) {
    if (from < to) {
      this.from = from;
      this.to = to;
    } else {
      this.from = to;
      this.to = from;
    }
  }

  public boolean contains(final int number) {
    return this.from <= number && number <= this.to;
  }

  public boolean equalsRange(final int from, final int to) {
    return this.from == from && this.to == to;
  }

  @Override
  public AbstractRange<?> expand(final Object value) {
    final Integer intValue = Integers.toInteger(value);
    if (intValue == null) {
      return null;
    } else {
      return super.expand(intValue);
    }
  }

  @Override
  public Integer getFrom() {
    return this.from;
  }

  @Override
  public Integer getTo() {
    return this.to;
  }

  @Override
  protected AbstractNumberRange<?> newNumberRange(final long from, final long to) {
    if (from < Integer.MIN_VALUE || to > Integer.MAX_VALUE) {
      return new LongRange(from, to);
    } else {
      return new IntRange((int)from, (int)to);
    }
  }

  @Override
  protected IntRange newRange(final Object from, final Object to) {
    return new IntRange((Integer)from, (Integer)to);
  }

  @Override
  public Integer next(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Integer intValue = Integers.toInteger(value);
      if (intValue == null) {
        return null;
      } else {
        final int number = intValue.intValue();
        if (number == Integer.MAX_VALUE) {
          return null;
        } else {
          return number + 1;
        }
      }
    }
  }

  @Override
  public Integer previous(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Integer intValue = Integers.toInteger(value);
      if (intValue == null) {
        return null;
      } else {
        final int number = intValue.intValue();
        if (number == Integer.MIN_VALUE) {
          return null;
        } else {
          return number - 1;
        }
      }
    }
  }

  protected void setFrom(final int from) {
    this.from = from;
  }

  protected void setTo(final int to) {
    this.to = to;
  }

  @Override
  public long size() {
    return this.to - this.from + 1;
  }
}
