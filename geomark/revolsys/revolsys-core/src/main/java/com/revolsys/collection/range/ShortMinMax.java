package com.revolsys.collection.range;

import org.jeometry.common.number.Shorts;

import com.revolsys.util.Emptyable;
import com.revolsys.util.Property;

public class ShortMinMax extends ShortRange implements Cloneable, Emptyable {
  public static ShortMinMax newWithIgnore(final short ignoreValue, final short... numbers) {
    final ShortMinMax minMax = new ShortMinMax();
    minMax.addWithIgnore(ignoreValue, numbers);
    return minMax;
  }

  public ShortMinMax() {
    setFrom(Short.MAX_VALUE);
    setTo(Short.MIN_VALUE);
  }

  public ShortMinMax(final short number) {
    super(number, number);
  }

  public ShortMinMax(final short... numbers) {
    this();
    add(numbers);
  }

  public ShortMinMax(final short min, final short max) {
    this();
    add(min);
    add(max);
  }

  public boolean add(final Number number) {
    if (number == null) {
      return false;
    } else {
      return add(number.shortValue());
    }
  }

  public void add(final short... numbers) {
    for (final short number : numbers) {
      add(number);
    }
  }

  /**
   * Add the number
   * @param number
   * @return True if the min or max was updated.
   */
  public boolean add(final short number) {
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

  public void add(final ShortMinMax minMax) {
    if (!minMax.isEmpty()) {
      final short min = minMax.getMin();
      add(min);

      final short max = minMax.getMax();
      add(max);
    }
  }

  public void addWithIgnore(final short ignoreValue, final short... numbers) {
    for (final short number : numbers) {
      if (number != ignoreValue) {
        add(number);
      }
    }
  }

  public void clear() {
    setFrom(Short.MAX_VALUE);

    setTo(Short.MIN_VALUE);
  }

  public ShortMinMax clip(short min, short max) {
    if (min > max) {
      return clip(max, min);
    } else {
      if (min > getMax() || getMin() > max) {
        return new ShortMinMax();
      } else {
        if (min < getMin()) {
          min = getMin();
        }
        if (max > getMax()) {
          max = getMax();
        }
        return new ShortMinMax(min, max);
      }
    }
  }

  public ShortMinMax clip(final ShortMinMax minMax) {
    if (isEmpty() || minMax.isEmpty()) {
      return new ShortMinMax();
    } else {
      final short min = minMax.getMin();
      final short max = minMax.getMax();
      return clip(min, max);
    }
  }

  @Override
  public ShortMinMax clone() {
    if (isEmpty()) {
      return new ShortMinMax();
    } else {
      final short min = getMin();
      final short max = getMax();
      return new ShortMinMax(min, max);
    }
  }

  public boolean contains(final short number) {
    return number >= getMin() && number <= getMax();
  }

  public boolean contains(final short min, final short max) {
    if (min > max) {
      return contains(max, min);
    } else {
      return min >= getMin() && max <= getMax();
    }
  }

  public boolean contains(final ShortMinMax minMax) {
    if (isEmpty() || !Property.hasValue(minMax)) {
      return false;
    } else {
      final short min = minMax.getMin();
      final short max = minMax.getMax();
      return contains(min, max);
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    } else if (object instanceof ShortMinMax) {
      final ShortMinMax minMax = (ShortMinMax)object;
      if (getMin() == minMax.getMin()) {
        if (getMax() == minMax.getMax()) {
          return true;
        }
      }
    }
    return false;
  }

  public short getMax() {
    return getTo();
  }

  public short getMin() {
    return getFrom();
  }

  public int getRange() {
    return getMax() - getMin();
  }

  @Override
  public int hashCode() {
    if (isEmpty()) {
      return Short.MAX_VALUE;
    } else {
      return 31 * (getMin() + getMax());
    }
  }

  @Override
  public boolean isEmpty() {
    return getMin() == Short.MAX_VALUE;
  }

  public boolean overlaps(final short min, final short max) {
    final short min2 = getMin();
    final short max2 = getMax();
    return Shorts.overlaps(min2, max2, min, max);
  }

  public boolean overlaps(final ShortMinMax minMax) {
    if (isEmpty() || !Property.hasValue(minMax)) {
      return false;
    } else {
      final short min = minMax.getMin();
      final short max = minMax.getMax();
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
