package com.revolsys.collection.range;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.iterator.MultiIterator;
import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.End;
import com.revolsys.util.Emptyable;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class RangeSet extends AbstractSet<Object>
  implements Iterable<Object>, Emptyable, Cloneable, Comparable<RangeSet> {

  private static void addPart(final RangeSet set, final List<AbstractRange<?>> crossProductRanges,
    final String fromValue, final String rangeSpec, final int partStart, final int partEnd) {
    final String toValue = rangeSpec.substring(partStart, partEnd);
    final AbstractRange<?> partRange = Ranges.newRange(fromValue, toValue);
    if (crossProductRanges == null) {
      set.addRange(partRange);
    } else {
      crossProductRanges.add(partRange);
    }
  }

  public static RangeSet newRangeSet(final int value) {
    final RangeSet range = new RangeSet();
    range.add(value);
    return range;
  }

  public static RangeSet newRangeSet(final int from, final int to) {
    final RangeSet range = new RangeSet();
    range.addRange(from, to);
    return range;
  }

  public static RangeSet newRangeSet(final String rangeSpec) {
    final RangeSet set = new RangeSet();
    if (Property.hasValue(rangeSpec)) {
      int partStart = 0;
      int partEnd = 0;
      boolean inRange = false;
      String rangeFirstPart = null;
      List<AbstractRange<?>> crossProductRanges = null;
      final int charCount = rangeSpec.length();
      for (int i = 0; i < charCount; i++) {
        final char character = rangeSpec.charAt(i);
        if (!Character.isWhitespace(character)) {
          switch (character) {
            case '+':
              if (crossProductRanges == null) {
                crossProductRanges = new ArrayList<>();
              }
              addPart(set, crossProductRanges, rangeFirstPart, rangeSpec, partStart, partEnd);
              partStart = i + 1;
              inRange = false;
            break;
            case '~':
              if (inRange) {
                throw new RangeInvalidException(
                  "The ~ character cannot be used twice in a range, see *~* in "
                    + rangeSpec.substring(0, i) + "*~*" + rangeSpec.substring(i + 1));
              } else {
                rangeFirstPart = rangeSpec.substring(partStart, partEnd);
                partStart = i + 1;
                inRange = true;
              }
            break;
            case ',':
              addPart(set, crossProductRanges, rangeFirstPart, rangeSpec, partStart, partEnd);
              if (crossProductRanges != null) {
                set.add(new CrossProductRange(crossProductRanges));
              }
              partStart = i + 1;
              inRange = false;
              rangeFirstPart = null;
              crossProductRanges = null;
            break;
          }
        }
        partEnd = i + 1;
      }
      if (partStart < charCount) {
        addPart(set, crossProductRanges, rangeFirstPart, rangeSpec, partStart, partEnd);
      }
      if (crossProductRanges != null) {
        set.addRange(new CrossProductRange(crossProductRanges));
      }
    }
    return set;
  }

  private final List<AbstractRange<?>> ranges = new LinkedList<>();

  private int size;

  public RangeSet() {
  }

  public RangeSet(final Iterable<Integer> values) {
    addValues(values);
  }

  public RangeSet(final RangeSet rangeSet) {
    addRanges(rangeSet);
  }

  @Override
  public boolean add(final Object value) {
    if (value == null) {
      return false;
    } else if (value instanceof AbstractRange<?>) {
      final AbstractRange<?> range = (AbstractRange<?>)value;
      return addRange(range);
    } else if (value instanceof RangeSet) {
      final RangeSet ranges = (RangeSet)value;
      return addRanges(ranges);
    } else {
      for (final ListIterator<AbstractRange<?>> iterator = this.ranges.listIterator(); iterator
        .hasNext();) {
        final AbstractRange<?> range = iterator.next();
        AbstractRange<?> newRange = range.expand(value);
        if (range == newRange) {
          return false;
        } else if (newRange != null) {
          // this.size++;
          if (iterator.hasNext()) {
            final int nextIndex = iterator.nextIndex();
            final AbstractRange<?> nextRange = this.ranges.get(nextIndex);
            final AbstractRange<?> expandedRange = newRange.expand(nextRange);
            if (expandedRange != null) {
              newRange = expandedRange;
              iterator.next();
              iterator.remove();
              iterator.previous();
            }
          }
          iterator.set(newRange);
          return true;
        } else if (range.compareFromValue(value) > 0) {
          iterator.previous();
          iterator.add(Ranges.newRange(value));
          return true;

        }
      }
      this.ranges.add(Ranges.newRange(value));
      this.size++;
      return true;
    }
  }

  public boolean addRange(final AbstractRange<?> addRange) {
    boolean added = false;
    if (addRange != null && addRange.size() > 0) {
      for (final ListIterator<AbstractRange<?>> iterator = this.ranges.listIterator(); iterator
        .hasNext();) {
        final AbstractRange<?> range = iterator.next();
        AbstractRange<?> newRange = range.expand(addRange);
        if (range == newRange) {
          return false;
        } else if (newRange != null) {
          this.size -= range.size();
          this.size += newRange.size();
          if (iterator.hasNext()) {
            final int nextIndex = iterator.nextIndex();
            final AbstractRange<?> nextRange = this.ranges.get(nextIndex);
            final AbstractRange<?> expandedRange = newRange.expand(nextRange);
            if (expandedRange != null) {
              this.size -= newRange.size();
              this.size -= nextRange.size();

              newRange = expandedRange;
              iterator.next();
              iterator.remove();
              iterator.previous();
              this.size += newRange.size();
            }
          }
          iterator.set(newRange);
          added = true;
        } else if (!added && range.compareFromValue(addRange.getFrom()) > 0
          && (iterator.previousIndex() == 0 || range.compareFromValue(addRange.getTo()) > 0)) {
          this.ranges.add(iterator.previousIndex(), addRange);
          this.size += addRange.size();
          return true;
        }
      }
      if (!added) {
        this.ranges.add(addRange);
        this.size += addRange.size();
      }
    }
    return added;
  }

  public boolean addRange(final char from, final char to) {
    final AbstractRange<?> addRange = Ranges.newRange(from, to);
    return addRange(addRange);
  }

  public boolean addRange(final double from, final double to) {
    final DoubleRange addRange = new DoubleRange(from, to);
    return addRange(addRange);
  }

  public boolean addRange(final int from, final int to) {
    final IntRange addRange = new IntRange(from, to);
    return addRange(addRange);
  }

  public boolean addRange(final Object from, final Object to) {
    final AbstractRange<?> addRange = Ranges.newRange(from, to);
    return addRange(addRange);
  }

  public boolean addRanges(final RangeSet ranges) {
    boolean added = false;
    if (ranges != null) {
      for (final AbstractRange<?> range : ranges.getRanges()) {
        added |= addRange(range);
      }
    }
    return added;
  }

  public boolean addValues(final Iterable<? extends Number> values) {
    boolean added = false;
    for (final Number value : values) {
      added |= add(value);
    }
    return added;
  }

  @Override
  public void clear() {
    this.ranges.clear();
    this.size = 0;
  }

  @Override
  public RangeSet clone() {
    return new RangeSet(this);
  }

  @Override
  public int compareTo(final RangeSet rangeSet) {
    if (isEmpty()) {
      if (rangeSet.isEmpty()) {
        return 0;
      } else {
        return -1;
      }
    } else if (rangeSet.isEmpty()) {
      return 1;
    } else {
      final Object from1 = getFrom();
      final Object from2 = rangeSet.getFrom();
      if (from1 instanceof Integer) {
        if (from2 instanceof Integer) {
          return ((Integer)from1).compareTo((Integer)from2);
        } else {
          return -1;
        }

      } else if (from2 instanceof Integer) {
        return 1;
      } else {
        return from1.toString().compareTo(from2.toString());
      }
    }
  }

  @Override
  public boolean contains(final Object object) {
    if (object != null) {
      for (final ListIterator<AbstractRange<?>> iterator = this.ranges.listIterator(); iterator
        .hasNext();) {
        final AbstractRange<?> range = iterator.next();
        if (range.contains(object)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean equalEnd(final RangeSet ranges, final End end) {
    final Object value1 = getEndValue(end);
    final Object value2 = ranges.getEndValue(end);
    return DataType.equal(value1, value2);
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof RangeSet) {
      final RangeSet range2 = (RangeSet)o;
      return this.ranges.equals(range2.ranges);
    } else {
      return false;
    }
  }

  public boolean equalsRange(final int from, final int to) {
    if (this.ranges.size() == 1) {
      final AbstractRange<?> range = this.ranges.get(0);
      if (range instanceof IntRange) {
        final IntRange intRange = (IntRange)range;
        return intRange.equalsRange(from, to);
      }
    }
    return false;
  }

  public Object getEndValue(final End end) {
    if (!isEmpty()) {
      if (End.isFrom(end)) {
        final AbstractRange<?> range = this.ranges.get(0);
        return range.getFrom();
      } else if (End.isTo(end)) {
        final AbstractRange<?> range = this.ranges.get(this.ranges.size() - 1);
        return range.getTo();
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <V> V getFrom() {
    if (size() == 0) {
      return null;
    } else {
      final AbstractRange<?> range = this.ranges.get(0);
      final Object value = range.getFrom();
      return (V)value;
    }
  }

  public List<AbstractRange<?>> getRanges() {
    return new ArrayList<>(this.ranges);
  }

  @SuppressWarnings("unchecked")
  public <V> V getTo() {
    if (size() == 0) {
      return null;
    } else {
      final AbstractRange<?> range = this.ranges.get(this.ranges.size() - 1);
      final Object to = range.getTo();
      return (V)to;
    }
  }

  @Override
  public boolean isEmpty() {
    return this.size == 0;
  }

  @Override
  public Iterator<Object> iterator() {
    return new MultiIterator<>(this.ranges);
  }

  @Override
  public boolean remove(final Object value) {
    for (final ListIterator<AbstractRange<?>> iterator = this.ranges.listIterator(); iterator
      .hasNext();) {
      final AbstractRange<?> range = iterator.next();
      if (range.contains(value)) {
        final Object from = range.getFrom();
        final Object to = range.getTo();
        if (DataType.equal(from, value)) {
          if (DataType.equal(to, value)) {
            iterator.remove();
          } else {
            final Object next = range.next(value);
            final AbstractRange<?> newRange = range.newRange(next, to);
            iterator.set(newRange);
          }
          this.size--;
          return true;
        } else if (DataType.equal(to, value)) {
          final Object previous = range.previous(value);
          final AbstractRange<?> newRange = range.newRange(from, previous);
          iterator.set(newRange);
          this.size--;
          return true;
        } else {
          final Object previous = range.previous(value);
          final AbstractRange<?> newRange1 = range.newRange(from, previous);
          iterator.set(newRange1);

          final Object next = range.next(value);
          final AbstractRange<?> newRange2 = range.newRange(next, to);
          iterator.add(newRange2);
          this.size--;
          return true;
        }
      }
    }

    return false;
  }

  public boolean removeAll(final Iterable<Object> values) {
    boolean removed = false;
    for (final Object value : values) {
      removed |= remove(value);
    }
    return removed;
  }

  @SuppressWarnings("unchecked")
  public <V> V removeFirst() {
    if (size() == 0) {
      return null;
    } else {
      final AbstractRange<?> range = this.ranges.get(0);
      final Object value = range.getFrom();
      remove(value);
      return (V)value;
    }
  }

  public boolean removeRange(final AbstractRange<?> range) {
    boolean removed = false;
    for (final Object object : range) {
      removed |= remove(object);
    }
    return removed;
  }

  public boolean removeRange(final Object from, final Object to) {
    boolean removed = false;
    for (final ListIterator<AbstractRange<?>> iterator = this.ranges.listIterator(); iterator
      .hasNext();) {
      final AbstractRange<?> range = iterator.next();

      final Object rangeFrom = range.getFrom();
      final Object rangeTo = range.getTo();
      if (range.compareFromValue(from) >= 0) {

        if (range.compareFromValue(to) > 0) {
          return removed;
        } else if (range.compareToValue(to) <= 0) {
          iterator.remove();
        } else {
          final Object next = range.next(to);
          final AbstractRange<?> newRange = range.newRange(next, rangeTo);
          this.size -= range.size();
          this.size += newRange.size();
          iterator.set(newRange);
        }
      } else if (range.compareToValue(from) >= 0) {
        if (range.compareToValue(to) > 0) {
          final Object next = range.next(to);
          final Object previous = range.previous(from);
          final AbstractRange<?> newRange1 = range.newRange(rangeFrom, previous);
          iterator.set(newRange1);
          final AbstractRange<?> newRange2 = range.newRange(next, rangeTo);
          iterator.add(newRange2);
          this.size -= range.size();
          this.size += newRange1.size();
          this.size += newRange2.size();

        } else {
          final Object previous = range.previous(from);
          final AbstractRange<?> newRange = range.newRange(rangeFrom, previous);
          this.size -= range.size();
          this.size += newRange.size();
          iterator.set(newRange);
          if (to == rangeTo) {
            return true;
          }
          removed = true;
        }
      }
    }
    return removed;
  }

  public boolean removeRange(final RangeSet ranges) {
    boolean removed = false;
    for (final AbstractRange<?> range : ranges.ranges) {
      removed |= removeRange(range);
    }
    return removed;
  }

  @Override
  public int size() {
    return this.size;
  }

  public List<Object> toList() {
    return Lists.toArray(this);
  }

  @Override
  public String toString() {
    return Strings.toString(",", this.ranges);
  }
}
