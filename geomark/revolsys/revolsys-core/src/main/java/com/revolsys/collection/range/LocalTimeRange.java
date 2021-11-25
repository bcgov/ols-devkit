package com.revolsys.collection.range;

import java.time.LocalTime;

public class LocalTimeRange extends AbstractRange<LocalTime> {

  public static LocalTimeRange newRange(final Object rangeSpec) {
    if (rangeSpec == null) {
      return null;
    } else if (rangeSpec instanceof String) {
      return parseRange((String)rangeSpec);
    } else if (rangeSpec instanceof LocalTimeRange) {
      return (LocalTimeRange)rangeSpec;
    } else {
      throw new IllegalArgumentException("Not a valid time range: " + rangeSpec);
    }
  }

  public static LocalTimeRange parseRange(final String timeRange) {
    final int dashIndex = timeRange.indexOf('-');
    if (dashIndex == -1) {
      final LocalTime time = LocalTime.parse(timeRange);
      return new LocalTimeRange(time);
    } else {
      final String fromString = timeRange.substring(0, dashIndex);
      final String toString = timeRange.substring(dashIndex + 1);
      final LocalTime from = LocalTime.parse(fromString);
      final LocalTime to = LocalTime.parse(toString);
      return new LocalTimeRange(from, to);
    }
  }

  private LocalTime from;

  private LocalTime to;

  private boolean wrapping;

  public LocalTimeRange() {
  }

  public LocalTimeRange(final LocalTime value) {
    this(value, value);
  }

  public LocalTimeRange(final LocalTime from, final LocalTime to) {
    this.from = from;
    this.to = to;
    this.wrapping = to.isBefore(from);
  }

  public boolean contains(final LocalTime time) {
    if (this.from.equals(time)) {
      return true;
    } else if (this.to.equals(time)) {
      return true;
    } else if (this.wrapping) {
      return time.isAfter(this.from) || time.isBefore(this.to);
    } else {
      return time.isAfter(this.from) && time.isBefore(this.to);
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof LocalTimeRange) {
      final LocalTimeRange range = (LocalTimeRange)other;
      final LocalTime from = range.getFrom();
      final LocalTime to = range.getTo();
      return equalsRange(from, to);
    }
    return false;
  }

  public boolean equalsRange(final LocalTime from, final LocalTime to) {
    return this.from.equals(from) && this.to.equals(to);
  }

  @Override
  public AbstractRange<?> expand(final Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalTime getFrom() {
    return this.from;
  }

  @Override
  public LocalTime getTo() {
    return this.to;
  }

  public boolean inMiddle(final LocalTime time) {
    if (this.from.equals(time)) {
      return false;
    } else if (this.to.equals(time)) {
      return false;
    } else if (this.wrapping) {
      return time.isAfter(this.from) || time.isBefore(this.to);
    } else {
      return time.isAfter(this.from) && time.isBefore(this.to);
    }
  }

  @Override
  protected LocalTimeRange newRange(final Object from, final Object to) {
    return new LocalTimeRange((LocalTime)from, (LocalTime)to);
  }

  public boolean overlaps(final LocalTimeRange range) {
    // TODO Auto-generated method stub
    return false;
  }

  protected void setFrom(final LocalTime from) {
    this.from = from;
  }

  protected void setTo(final LocalTime to) {
    this.to = to;
  }

  @Override
  public String toString() {
    final LocalTime from = getFrom();
    final LocalTime to = getTo();
    if (from.equals(to)) {
      return from.toString();
    } else {
      return from + "-" + to;
    }
  }

}
