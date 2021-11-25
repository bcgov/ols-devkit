package com.revolsys.collection.range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.util.Property;
import com.revolsys.util.Strings;

/**
 *
 * Ranges are immutable
 */
public class CrossProductRange extends AbstractRange<String> {
  private List<AbstractRange<?>> ranges;

  private int size;

  public CrossProductRange(final AbstractRange<?>... ranges) {
    this(Arrays.asList(ranges));
  }

  public CrossProductRange(final Collection<? extends AbstractRange<?>> ranges) {
    if (Property.hasValue(ranges)) {
      this.ranges = new ArrayList<>(ranges);
    } else {
      throw new IllegalArgumentException("List of ranges must not be empty");
    }
  }

  @Override
  public AbstractRange<?> expand(final AbstractRange<?> range) {
    return null;
  }

  @Override
  public AbstractRange<?> expand(final Object value) {
    return null;
  }

  @Override
  public String getFrom() {
    final StringBuilder from = new StringBuilder();
    for (final AbstractRange<?> range : this.ranges) {
      final Object rangeFrom = range.getFrom();
      from.append(rangeFrom);
    }
    return from.toString();
  }

  public AbstractRange<?> getRange(final int i) {
    return this.ranges.get(i);
  }

  public List<AbstractRange<?>> getRanges() {
    return this.ranges;
  }

  @Override
  public String getTo() {
    final StringBuilder to = new StringBuilder();
    for (final AbstractRange<?> range : this.ranges) {
      final Object rangeTo = range.getTo();
      to.append(rangeTo);
    }
    return to.toString();
  }

  @Override
  public Iterator<String> iterator() {
    return new CrossProductRangeIterator(this);
  }

  @Override
  public long size() {
    return this.size;
  }

  @Override
  public String toString() {
    return Strings.toString("+", this.ranges);
  }
}
