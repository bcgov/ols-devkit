package com.revolsys.collection.range;

import org.jeometry.common.number.Floats;

import com.revolsys.util.Emptyable;
import com.revolsys.util.Property;

public class FloatMinMax extends FloatRange implements Cloneable, Emptyable {
  public static FloatMinMax newWithIgnore(final float ignoreValue, final float... numbers) {
    final FloatMinMax minMax = new FloatMinMax();
    minMax.addWithIgnore(ignoreValue, numbers);
    return minMax;
  }

  public FloatMinMax() {
    setFrom(Float.MAX_VALUE);
    setTo(-Float.MAX_VALUE);
  }

  public FloatMinMax(final float number) {
    super(number, number);
  }

  public FloatMinMax(final float... numbers) {
    this();
    add(numbers);
  }

  public FloatMinMax(final float min, final float max) {
    this();
    add(min);
    add(max);
  }

  public void add(final float... numbers) {
    for (final float number : numbers) {
      add(number);
    }
  }

  /**
   * Add the number
   * @param number
   * @return True if the min or max was updated.
   */
  public boolean add(final float number) {
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

  public void add(final FloatMinMax minMax) {
    if (!minMax.isEmpty()) {
      final float min = minMax.getMin();
      add(min);

      final float max = minMax.getMax();
      add(max);
    }
  }

  public boolean add(final Number number) {
    if (number == null) {
      return false;
    } else {
      return add(number.floatValue());
    }
  }

  public void addWithIgnore(final float ignoreValue, final float... numbers) {
    for (final float number : numbers) {
      if (!Floats.equal(number, ignoreValue)) {
        add(number);
      }
    }
  }

  public void clear() {
    setFrom(Float.MAX_VALUE);

    setTo(-Float.MAX_VALUE);
  }

  public FloatMinMax clip(float min, float max) {
    if (min > max) {
      return clip(max, min);
    } else {
      if (min > getMax() || getMin() > max) {
        return new FloatMinMax();
      } else {
        if (min < getMin()) {
          min = getMin();
        }
        if (max > getMax()) {
          max = getMax();
        }
        return new FloatMinMax(min, max);
      }
    }
  }

  public FloatMinMax clip(final FloatMinMax minMax) {
    if (isEmpty() || minMax.isEmpty()) {
      return new FloatMinMax();
    } else {
      final float min = minMax.getMin();
      final float max = minMax.getMax();
      return clip(min, max);
    }
  }

  @Override
  public FloatMinMax clone() {
    if (isEmpty()) {
      return new FloatMinMax();
    } else {
      final float min = getMin();
      final float max = getMax();
      return new FloatMinMax(min, max);
    }
  }

  public boolean contains(final float number) {
    return number >= getMin() && number <= getMax();
  }

  public boolean contains(final float min, final float max) {
    if (min > max) {
      return contains(max, min);
    } else {
      return min >= getMin() && max <= getMax();
    }
  }

  public boolean contains(final FloatMinMax minMax) {
    if (isEmpty() || !Property.hasValue(minMax)) {
      return false;
    } else {
      final float min = minMax.getMin();
      final float max = minMax.getMax();
      return contains(min, max);
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    } else if (object instanceof FloatMinMax) {
      final FloatMinMax minMax = (FloatMinMax)object;
      if (getMin() == minMax.getMin()) {
        if (getMax() == minMax.getMax()) {
          return true;
        }
      }
    }
    return false;
  }

  public float getMax() {
    return getTo();
  }

  public float getMin() {
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
    return getMin() == Float.MAX_VALUE;
  }

  public boolean overlaps(final float min, final float max) {
    final float min2 = getMin();
    final float max2 = getMax();
    return Floats.overlaps(min2, max2, min, max);
  }

  public boolean overlaps(final FloatMinMax minMax) {
    if (isEmpty() || !Property.hasValue(minMax)) {
      return false;
    } else {
      final float min = minMax.getMin();
      final float max = minMax.getMax();
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
