package com.revolsys.collection.range;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.revolsys.util.Emptyable;
import com.revolsys.util.Strings;

public class LocalTimeRangeSet implements Iterable<LocalTimeRange>, Emptyable {

  public static LocalTimeRangeSet newRangeSet(final List<?> ranges) {
    final LocalTimeRangeSet rangeSet = new LocalTimeRangeSet();
    if (ranges != null) {
      for (final Object rangeSpec : ranges) {
        final LocalTimeRange range = LocalTimeRange.newRange(rangeSpec);
        if (range != null) {
          rangeSet.addRange(range);
        }
      }
    }
    return rangeSet;
  }

  public static LocalTimeRangeSet parseRangeSet(final String rangeSetSpec) {
    final LocalTimeRangeSet rangeSet = new LocalTimeRangeSet();
    if (rangeSetSpec != null) {
      for (final String rangeSpec : rangeSetSpec.split("[\\|,]")) {
        final LocalTimeRange range = LocalTimeRange.parseRange(rangeSpec);
        if (range != null) {
          rangeSet.addRange(range);
        }
      }
    }
    return rangeSet;
  }

  private final List<LocalTimeRange> ranges = new LinkedList<>();

  public LocalTimeRangeSet() {
  }

  public LocalTimeRangeSet(final LocalTimeRangeSet rangeSet) {
    addRanges(rangeSet);
  }

  public boolean addRange(final LocalTime from, final LocalTime to) {
    final LocalTimeRange addRange = new LocalTimeRange(from, to);
    return addRange(addRange);
  }

  public boolean addRange(final LocalTimeRange range) {
    // TODO deal with overlaps
    this.ranges.add(range);
    return true;
  }

  public boolean addRanges(final LocalTimeRangeSet ranges) {
    boolean added = false;
    if (ranges != null) {
      for (final LocalTimeRange range : ranges.getRanges()) {
        added |= addRange(range);
      }
    }
    return added;
  }

  public void clear() {
    this.ranges.clear();
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof LocalTimeRangeSet) {
      final LocalTimeRangeSet range2 = (LocalTimeRangeSet)o;
      return this.ranges.equals(range2.ranges);
    } else {
      return false;
    }
  }

  public boolean equalsRange(final LocalTime from, final LocalTime to) {
    if (this.ranges.size() == 1) {
      final LocalTimeRange range = this.ranges.get(0);
      return range.equalsRange(from, to);
    }
    return false;
  }

  public List<LocalTimeRange> getRanges() {
    return new ArrayList<>(this.ranges);
  }

  @Override
  public boolean isEmpty() {
    return this.ranges.isEmpty();
  }

  // public boolean removeAll(final Iterable<LocalTimeRange> ranges) {
  // boolean removed = false;
  //
  // for (final LocalTimeRange range : ranges) {
  // removed |= removeRange(range);
  // }
  // return removed;
  // }

  // public boolean removeRange(final LocalTimeRange range) {
  // boolean removed = false;
  // for (final ListIterator<LocalTimeRange> iterator =
  // this.ranges.listIterator(); iterator
  // .hasNext();) {
  // LocalTimeRange range2 = iterator.next();
  // LocalTimeRange
  // if (range.containsRange(range2)) {
  // iterator.remove();
  // removed = true;
  // } else if (range.overlaps(range2)) {
  // range2 = range2.clip(range);
  // iterator.set(range2);
  // removed = true;
  // }
  // }
  // return removed;
  // }

  @Override
  public Iterator<LocalTimeRange> iterator() {
    return this.ranges.iterator();
  }

  @Override
  public String toString() {
    return Strings.toString(",", this.ranges);
  }
}
