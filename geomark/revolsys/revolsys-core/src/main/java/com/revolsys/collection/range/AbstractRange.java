package com.revolsys.collection.range;

import java.util.Iterator;
import java.util.List;

import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.list.Lists;
import com.revolsys.util.Emptyable;

public abstract class AbstractRange<V>
  implements Iterable<V>, Emptyable, Comparable<AbstractRange<? extends Object>> {

  public static int compare(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (value2 == null) {
      return 1;
    } else {
      if (value1 instanceof Number) {
        if (value2 instanceof Number) {
          final Number number1 = (Number)value1;
          final Number number2 = (Number)value2;
          return Long.compare(number1.longValue(), number2.longValue());
        } else {
          return -1;
        }
      } else {
        if (value2 instanceof Number) {
          return 1;
        } else {
          return value1.toString().compareTo(value2.toString());
        }
      }
    }
  }

  public int compareFromValue(final Object value) {
    final V from = getFrom();
    return compare(from, value);
  }

  @Override
  public int compareTo(final AbstractRange<? extends Object> range) {
    final Object rangeFrom = range.getFrom();
    final int fromCompare = compareFromValue(rangeFrom);
    if (fromCompare == 0) {
      final Object rangeTo = range.getTo();
      final int toCompare = compareToValue(rangeTo);
      return toCompare;
    }
    return fromCompare;
  }

  public int compareToValue(final Object value) {
    final V to = getTo();
    return compare(to, value);
  }

  public boolean contains(final Object value) {
    final int fromCompare = compareFromValue(value);
    if (fromCompare <= 0) {
      final int toCompare = compareToValue(value);
      if (toCompare >= 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    } else if (other == null) {
      return false;
    } else if (other instanceof AbstractRange) {
      final AbstractRange<?> range = (AbstractRange<?>)other;
      final V from = getFrom();
      final Object rangeFrom = range.getFrom();
      if (!DataType.equal(from, rangeFrom)) {
        return false;
      } else if (!DataType.equal(getTo(), range.getTo())) {
        return false;
      } else {
        return true;
      }
    } else {

      return false;
    }
  }

  /**
   * Construct a new expanded range if the this range and the other overlap or touch
   *
   * @param range
   * @return
   */
  public AbstractRange<?> expand(final AbstractRange<?> range) {
    final V from = getFrom();
    final V to = getTo();
    final Object rangeFrom = range.getFrom();
    final Object rangeTo = range.getTo();

    final int fromCompare = compareFromValue(rangeFrom);
    final int toCompare = compareToValue(rangeTo);
    if (fromCompare == 0) {
      if (toCompare >= 0) {
        return this;
      } else {
        return range;
      }
    } else if (toCompare == 0) {
      if (fromCompare < 0) {
        return this;
      } else {
        return range;
      }
    } else if (fromCompare < 0) {
      if (toCompare > 0) {
        return this;
      } else if (compareToValue(rangeFrom) > 0) {
        return newRange(from, rangeTo);
      } else if (DataType.equal(to, previous(rangeFrom)) || DataType.equal(to, rangeFrom)) {
        return newRange(from, rangeTo);
      }
    } else if (fromCompare > 0) {
      if (toCompare < 0) {
        return range;
      } else if (compareFromValue(rangeTo) < 0) {
        return newRange(rangeFrom, to);
      } else if (DataType.equal(previous(from), rangeTo) || DataType.equal(from, rangeTo)) {
        return newRange(rangeFrom, to);
      }
    }
    return null;
  }

  /**
   * Construct a newn expanded range to include the specified value if possible.
   * <ul>
   * <li>If the range contains this value return this instance.</li>
   * <li>If the value = from - 1 return a new range from value-to</li>
   * <li>If the value = to + 1 return a new range from from-value</li>
   * <li>Otherwise return null as it is not a consecutive range</li>
   * @param value
   * @return
   */
  public AbstractRange<?> expand(final Object value) {
    if (value instanceof AbstractRange) {
      final AbstractRange<?> range = (AbstractRange<?>)value;
      return expand(range);
    } else {
      if (value == null || contains(value)) {
        return this;
      } else {
        final V from = getFrom();
        final V to = getTo();
        final V next = next(value);
        if (next == null) {
          return null;
        } else if (compareFromValue(next) == 0) { // value == from -1
          return newRange(value, to);
        } else {
          final V previous = previous(value);
          if (previous == null) {
            return null;
          } else if (compareToValue(previous) == 0) { // value == to + 1
            return newRange(from, value);
          } else {
            return null;
          }
        }
      }
    }
  }

  public abstract V getFrom();

  public abstract V getTo();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getFrom().hashCode();
    result = prime * result + getTo().hashCode();
    return result;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public Iterator<V> iterator() {
    return new RangeIterator<>(this);
  }

  protected AbstractRange<?> newRange(final Object from, final Object to) {
    throw new UnsupportedOperationException();
  }

  public V next(final Object value) {
    return null;
  }

  public V previous(final Object value) {
    return null;
  }

  public long size() {
    return 1;
  }

  public List<V> toList() {
    return Lists.toArray(this);
  }

  @Override
  public String toString() {
    if (size() == 0) {
      return "";
    } else {
      final V from = getFrom();
      final V to = getTo();
      if (from.equals(to)) {
        return from.toString();
      } else {
        return from + "~" + to;
      }
    }
  }
}
