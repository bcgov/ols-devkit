package com.revolsys.collection.range;

import org.jeometry.common.number.Doubles;

public class DoubleRange extends AbstractRange<Double> {
  private double from;

  private double to;

  public DoubleRange() {
  }

  public DoubleRange(final double value) {
    this(value, value);
  }

  public DoubleRange(final double from, final double to) {
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
    final Double doubleValue = Doubles.toDouble(value);
    if (doubleValue == null) {
      return null;
    } else {
      return super.expand(doubleValue);
    }
  }

  @Override
  public Double getFrom() {
    return this.from;
  }

  public double getFromDouble() {
    return this.from;
  }

  public Double getRange() {
    return this.to - this.from;
  }

  @Override
  public Double getTo() {
    return this.to;
  }

  public double getToDouble() {
    return this.to;
  }

  @Override
  protected DoubleRange newRange(final Object from, final Object to) {
    return new DoubleRange((Double)from, (Double)to);
  }

  @Override
  public Double next(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Double doubleValue = Doubles.toDouble(value);
      if (doubleValue == null) {
        return null;
      } else {
        final double number = doubleValue.doubleValue();
        if (number == Double.MAX_VALUE) {
          return null;
        } else {
          return number + 1;
        }
      }
    }
  }

  @Override
  public Double previous(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Double doubleValue = Doubles.toDouble(value);
      if (doubleValue == null) {
        return null;
      } else {
        final double number = doubleValue.doubleValue();
        if (number == Double.MIN_VALUE) {
          return null;
        } else {
          return number - 1;
        }
      }
    }
  }

  protected void setFrom(final double from) {
    this.from = from;
  }

  protected void setTo(final double to) {
    this.to = to;
  }

  @Override
  public long size() {
    return (long)Math.ceil(this.to) - (long)Math.floor(this.from) + 1;
  }
}
