package com.revolsys.collection.range;

import org.jeometry.common.number.Numbers;

public class LongPaddedRange extends AbstractRange<String> {
  private byte digitCount;

  private long from;

  private long to;

  public LongPaddedRange(final long value) {
    this(value, value);
  }

  public LongPaddedRange(final long from, final long to) {
    if (from < to) {
      this.from = from;
      this.to = to;
    } else {
      this.from = to;
      this.to = from;
    }
    this.digitCount = Numbers.digitCount(from);
    final byte toDigitCount = Numbers.digitCount(to);
    if (toDigitCount > this.digitCount) {
      this.digitCount = toDigitCount;
    }
  }

  public LongPaddedRange(final long from, final long to, final byte digitCount) {
    if (from < to) {
      this.from = from;
      this.to = to;
    } else {
      this.from = to;
      this.to = from;
    }
    this.digitCount = digitCount;
    final byte fromDigitCount = Numbers.digitCount(from);
    if (fromDigitCount > this.digitCount) {
      this.digitCount = fromDigitCount;
    }
    final byte toDigitCount = Numbers.digitCount(to);
    if (toDigitCount > this.digitCount) {
      this.digitCount = toDigitCount;
    }
  }

  @Override
  public AbstractRange<?> expand(final Object value) {
    final Long longValue = Numbers.toLong(value);
    if (longValue == null) {
      return null;
    } else {
      return super.expand(longValue);
    }
  }

  @Override
  public String getFrom() {
    return toString(this.from);
  }

  @Override
  public String getTo() {
    return toString(this.to);
  }

  @Override
  protected LongPaddedRange newRange(final Object from, final Object to) {
    return new LongPaddedRange((Long)from, (Long)to);
  }

  @Override
  public String next(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Long longValue = Numbers.toLong(value);
      if (longValue == null) {
        return null;
      } else {
        final long number = longValue.longValue();
        if (number == Long.MAX_VALUE) {
          return null;
        } else {
          return toString(number + 1);
        }
      }
    }
  }

  @Override
  public String previous(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Long longValue = Numbers.toLong(value);
      if (longValue == null) {
        return null;
      } else {
        final long number = longValue.longValue();
        if (number == Long.MIN_VALUE) {
          return null;
        } else {
          return toString(number - 1);
        }
      }
    }
  }

  @Override
  public long size() {
    return this.to - this.from + 1;
  }

  private String toString(final long number) {
    return Numbers.toStringPadded(number, this.digitCount);
  }
}
