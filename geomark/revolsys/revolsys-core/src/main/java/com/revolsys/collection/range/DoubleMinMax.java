package com.revolsys.collection.range;

import org.jeometry.common.number.Doubles;

import com.revolsys.util.Emptyable;
import com.revolsys.util.Property;

public class DoubleMinMax extends DoubleRange implements Cloneable, Emptyable {
  public static DoubleMinMax newWithIgnore(final double ignoreValue, final double... numbers) {
    final DoubleMinMax minMax = new DoubleMinMax();
    minMax.addWithIgnore(ignoreValue, numbers);
    return minMax;
  }

  public DoubleMinMax() {
    setFrom(Double.POSITIVE_INFINITY);
    setTo(Double.NEGATIVE_INFINITY);
  }

  public DoubleMinMax(final double number) {
    super(number, number);
  }

  public DoubleMinMax(final double... numbers) {
    this();
    add(numbers);
  }

  public DoubleMinMax(final double min, final double max) {
    this();
    add(min);
    add(max);
  }

  public void add(final double... numbers) {
    for (final double number : numbers) {
      add(number);
    }
  }

  /**
   * Add the number
   * @param number
   * @return True if the min or max was updated.
   */
  public boolean add(final double number) {
    boolean updated = false;
    if (number < getMin()) {
      setFrom(number);
      updated = true;
    }
    if (number > getMax()) {
      setTo(number);
      updated = true;
    }
    return updated;
  }

  public void add(final DoubleMinMax minMax) {
    if (!minMax.isEmpty()) {
      final double min = minMax.getMin();
      add(min);

      final double max = minMax.getMax();
      add(max);
    }
  }

  public boolean add(final Number number) {
    if (number == null) {
      return false;
    } else {
      return add(number.doubleValue());
    }
  }

  public void addWithIgnore(final double ignoreValue, final double... numbers) {
    for (final double number : numbers) {
      if (!Doubles.equal(number, ignoreValue)) {
        add(number);
      }
    }
  }

  public void clear() {
    setFrom(Double.POSITIVE_INFINITY);
    setTo(Double.NEGATIVE_INFINITY);
  }

  public DoubleMinMax clip(double min, double max) {
    if (min > max) {
      return clip(max, min);
    } else {
      if (min > getMax() || getMin() > max) {
        return new DoubleMinMax();
      } else {
        if (min < getMin()) {
          min = getMin();
        }
        if (max > getMax()) {
          max = getMax();
        }
        return new DoubleMinMax(min, max);
      }
    }
  }

  public DoubleMinMax clip(final DoubleMinMax minMax) {
    if (isEmpty() || minMax.isEmpty()) {
      return new DoubleMinMax();
    } else {
      final double min = minMax.getMin();
      final double max = minMax.getMax();
      return clip(min, max);
    }
  }

  @Override
  public DoubleMinMax clone() {
    if (isEmpty()) {
      return new DoubleMinMax();
    } else {
      final double min = getMin();
      final double max = getMax();
      return new DoubleMinMax(min, max);
    }
  }

  public boolean contains(final double number) {
    return number >= getMin() && number <= getMax();
  }

  public boolean contains(final double min, final double max) {
    if (min > max) {
      return contains(max, min);
    } else {
      return min >= getMin() && max <= getMax();
    }
  }

  public boolean contains(final DoubleMinMax minMax) {
    if (isEmpty() || !Property.hasValue(minMax)) {
      return false;
    } else {
      final double min = minMax.getMin();
      final double max = minMax.getMax();
      return contains(min, max);
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    } else if (object instanceof DoubleMinMax) {
      final DoubleMinMax minMax = (DoubleMinMax)object;
      if (getMin() == minMax.getMin()) {
        if (getMax() == minMax.getMax()) {
          return true;
        }
      }
    }
    return false;
  }

  public double getMax() {
    return getTo();
  }

  public double getMin() {
    return getFrom();
  }

  @Override
  public int hashCode() {
    if (isEmpty()) {
      return Integer.MAX_VALUE;
    } else {
      return 31 * (int)(getMin() + getMax());
    }
  }

  @Override
  public boolean isEmpty() {
    return getMin() == Double.POSITIVE_INFINITY;
  }

  public boolean overlaps(final double min, final double max) {
    final double min2 = getMin();
    final double max2 = getMax();
    return Doubles.overlaps(min2, max2, min, max);
  }

  public boolean overlaps(final DoubleMinMax minMax) {
    if (isEmpty() || !Property.hasValue(minMax)) {
      return false;
    } else {
      final double min = minMax.getMin();
      final double max = minMax.getMax();
      return overlaps(min, max);
    }
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "EMPTY";
    } else {
      return getMin() + "-" + getMax();
    }
  }
}
