package com.revolsys.util.count;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.util.Counter;
import com.revolsys.util.TotalCounter;

public class TotalLabelCounters extends AbstractLabelCounters {
  private final List<LabelCounters> labelCountersList;

  public TotalLabelCounters(final List<LabelCounters> labelCountersList) {
    this(null, labelCountersList);
  }

  public TotalLabelCounters(final String message) {
    this.message = message;
    this.labelCountersList = new ArrayList<>();
  }

  public TotalLabelCounters(final String message, final List<LabelCounters> labelCountersList) {
    this.message = message;
    this.labelCountersList = labelCountersList;
  }

  @Override
  public synchronized boolean addCount(final CharSequence label, final long count) {
    throw new UnsupportedOperationException();
  }

  public TotalLabelCounters addCounters(final LabelCounters labelCounters) {
    this.labelCountersList.add(labelCounters);
    return this;
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
    if (label != null) {
      long count = 0;
      boolean hasCount = false;
      for (final LabelCounters labelCounters : this.labelCountersList) {
        final Long count2 = labelCounters.getCount(label);
        if (count2 != null) {
          count += count2;
          hasCount = true;
        }
      }
      if (hasCount) {
        return count;
      }
    }
    return null;
  }

  @Override
  public synchronized Counter getCounter(final CharSequence label) {
    final String labelString = label.toString();
    final List<Counter> counters = new ArrayList<>();
    for (final LabelCounters labelCounters : this.labelCountersList) {
      final Counter counter2 = labelCounters.getCounter(labelString);
      counters.add(counter2);
    }
    return new TotalCounter(labelString, counters);
  }

  @Override
  public synchronized Set<String> getLabels() {
    final Set<String> labels = new HashSet<>();
    for (final LabelCounters labelCounters : this.labelCountersList) {
      final Set<String> labels2 = labelCounters.getLabels();
      labels.addAll(labels2);
    }
    return labels;
  }

  @Override
  public boolean isEmpty() {
    for (final LabelCounters labelCounters : this.labelCountersList) {
      if (!labelCounters.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public TotalLabelCounters subtractCounters(final LabelCounters labelCounters) {
    this.labelCountersList.add(new NegativeLabelCounters(labelCounters));
    return this;
  }
}
