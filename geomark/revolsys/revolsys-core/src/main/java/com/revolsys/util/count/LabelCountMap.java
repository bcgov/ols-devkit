package com.revolsys.util.count;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.util.Counter;
import com.revolsys.util.LongCounter;

public class LabelCountMap extends AbstractLabelCounters {
  private final Map<String, Counter> counterByLabel = new TreeMap<>();

  private final Counter total = new LongCounter("total");

  public LabelCountMap() {
    this(null);
  }

  public LabelCountMap(final String message) {
    this.message = message;
  }

  @Override
  public synchronized boolean addCount(final CharSequence label, final long count) {
    if (label == null) {
      return false;
    } else {
      this.total.add(count);
      final String labelString = label.toString();
      Counter counter = this.counterByLabel.get(labelString);
      if (counter == null) {
        counter = new LongCounter(labelString, count);
        this.counterByLabel.put(labelString, counter);
        return true;
      } else {
        counter.add(count);
        return false;
      }
    }
  }

  @Override
  public synchronized void addCountsText(final StringBuilder sb) {
    int totalCount = 0;
    if (this.message != null) {
      sb.append(this.message);
    }
    sb.append("\n");
    for (final Entry<String, Counter> entry : entrySet()) {
      sb.append(entry.getKey());
      sb.append("\t");
      final Counter counter = entry.getValue();
      final long count = counter.get();
      totalCount += count;
      sb.append(count);
      sb.append("\n");
    }
    sb.append("Total");
    sb.append("\t");
    sb.append(totalCount);
    sb.append("\n");
  }

  @Override
  public synchronized void clearCounts() {
    this.counterByLabel.clear();
  }

  @Override
  public synchronized void clearCounts(final String label) {
    if (label != null) {
      final String labelString = label.toString();
      this.counterByLabel.remove(labelString);
    }
  }

  public Set<Entry<String, Counter>> entrySet() {
    return this.counterByLabel.entrySet();
  }

  @Override
  public synchronized Long getCount(final CharSequence label) {
    if (label != null) {
      final String labelString = label.toString();
      final Counter counter = this.counterByLabel.get(labelString);
      if (counter != null) {
        return counter.get();
      }
    }
    return null;
  }

  @Override
  public synchronized Counter getCounter(final CharSequence label) {
    if (label == null) {
      return null;
    } else {
      final String labelString = label.toString();
      Counter counter = this.counterByLabel.get(labelString);
      if (counter == null) {
        counter = new LongCounter(labelString);
        this.counterByLabel.put(labelString, counter);
      }
      return counter;
    }
  }

  @Override
  public synchronized Set<String> getLabels() {
    return this.counterByLabel.keySet();
  }

  public long getTotal() {
    return this.total.get();
  }

  @Override
  public boolean isEmpty() {
    return this.counterByLabel.isEmpty();
  }

  @Override
  public synchronized void setCounter(final CharSequence label, final Counter counter) {
    final String labelString = label.toString();
    this.counterByLabel.put(labelString, counter);
  }

  public int size() {
    return this.counterByLabel.size();
  }

  public JsonObject toJson() {
    if (isEmpty()) {
      return JsonObject.EMPTY;
    } else {
      final JsonObject json = JsonObject.tree();
      for (final Entry<String, Counter> entry : entrySet()) {
        final String label = entry.getKey();
        final Counter counter = entry.getValue();
        final long count = counter.get();
        json.add(label, count);
      }
      return json;
    }
  }
}
