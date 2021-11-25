package com.revolsys.collection.range;

import org.jeometry.common.number.Shorts;

public class ShortRange extends AbstractNumberRange<Short> {
  private short from;

  private short to;

  public ShortRange() {
  }

  public ShortRange(final short value) {
    this(value, value);
  }

  public ShortRange(final short from, final short to) {
    if (from < to) {
      this.from = from;
      this.to = to;
    } else {
      this.from = to;
      this.to = from;
    }
  }

  @Override
  public AbstractRange<?> expand(final Object value) {
    final Short shortValue = Shorts.toShort(value);
    if (shortValue == null) {
      return null;
    } else {
      return super.expand(shortValue);
    }
  }

  @Override
  public Short getFrom() {
    return this.from;
  }

  @Override
  public Short getTo() {
    return this.to;
  }

  @Override
  protected AbstractNumberRange<?> newNumberRange(final long from, final long to) {
    if (from < Integer.MIN_VALUE || to > Integer.MAX_VALUE) {
      return new LongRange(from, to);
    } else if (from < Short.MIN_VALUE || to > Short.MAX_VALUE) {
      return new IntRange((int)from, (int)to);
    } else {
      return new ShortRange((short)from, (short)to);
    }
  }

  @Override
  protected ShortRange newRange(final Object from, final Object to) {
    return new ShortRange((Short)from, (Short)to);
  }

  @Override
  public Short next(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Short shortValue = Shorts.toShort(value);
      if (shortValue == null) {
        return null;
      } else {
        final short number = shortValue.shortValue();
        if (number == Short.MAX_VALUE) {
          return null;
        } else {
          return (short)(number + 1);
        }
      }
    }
  }

  @Override
  public Short previous(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Short shortValue = Shorts.toShort(value);
      if (shortValue == null) {
        return null;
      } else {
        final short number = shortValue.shortValue();
        if (number == Short.MIN_VALUE) {
          return null;
        } else {
          return (short)(number - 1);
        }
      }
    }
  }

  protected void setFrom(final short from) {
    this.from = from;
  }

  protected void setTo(final short to) {
    this.to = to;
  }

  @Override
  public long size() {
    return (long)Math.ceil(this.to) - (long)Math.floor(this.from) + 1;
  }
}
