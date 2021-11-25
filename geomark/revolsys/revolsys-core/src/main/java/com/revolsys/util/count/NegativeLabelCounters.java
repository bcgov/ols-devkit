package com.revolsys.util.count;

import java.util.Set;

import com.revolsys.util.Counter;

public class NegativeLabelCounters extends AbstractLabelCounters {
  private final LabelCounters labelCounters;

  public NegativeLabelCounters(final LabelCounters labelCounters) {
    this(labelCounters.getMessage(), labelCounters);
  }

  public NegativeLabelCounters(final String message, final LabelCounters labelCounters) {
    this.message = message;
    this.labelCounters = labelCounters;
  }

  @Override
  public synchronized boolean addCount(final CharSequence label, final long count) {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized void clearCounts() {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized void clearCounts(final String label) {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized Long getCount(final CharSequence label) {
    final Long count2 = this.labelCounters.getCount(label);
    if (count2 == null) {
      return null;
    } else {
      return -count2;
    }
  }

  @Override
  public synchronized Counter getCounter(final CharSequence label) {
    final Counter counter = this.labelCounters.getCounter(label);
    return counter.newNegative();
  }

  @Override
  public synchronized Set<String> getLabels() {
    return this.labelCounters.getLabels();
  }

  @Override
  public boolean isEmpty() {
    return this.labelCounters.isEmpty();
  }
}
