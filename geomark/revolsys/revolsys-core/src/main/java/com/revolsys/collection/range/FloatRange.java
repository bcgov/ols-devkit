package com.revolsys.collection.range;

import org.jeometry.common.number.Floats;

public class FloatRange extends AbstractRange<Float> {
  private float from;

  private float to;

  public FloatRange() {
  }

  public FloatRange(final float value) {
    this(value, value);
  }

  public FloatRange(final float from, final float to) {
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
    final Float floatValue = Floats.toFloat(value);
    if (floatValue == null) {
      return null;
    } else {
      return super.expand(floatValue);
    }
  }

  @Override
  public Float getFrom() {
    return this.from;
  }

  public Float getRange() {
    return this.to - this.from;
  }

  @Override
  public Float getTo() {
    return this.to;
  }

  @Override
  protected FloatRange newRange(final Object from, final Object to) {
    return new FloatRange((Float)from, (Float)to);
  }

  @Override
  public Float next(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Float floatValue = Floats.toFloat(value);
      if (floatValue == null) {
        return null;
      } else {
        final float number = floatValue.floatValue();
        if (number == Float.MAX_VALUE) {
          return null;
        } else {
          return number + 1;
        }
      }
    }
  }

  @Override
  public Float previous(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Float floatValue = Floats.toFloat(value);
      if (floatValue == null) {
        return null;
      } else {
        final float number = floatValue.floatValue();
        if (number == Float.MIN_VALUE) {
          return null;
        } else {
          return number - 1;
        }
      }
    }
  }

  protected void setFrom(final float from) {
    this.from = from;
  }

  protected void setTo(final float to) {
    this.to = to;
  }

  @Override
  public long size() {
    return (long)Math.ceil(this.to) - (long)Math.floor(this.from) + 1;
  }
}
