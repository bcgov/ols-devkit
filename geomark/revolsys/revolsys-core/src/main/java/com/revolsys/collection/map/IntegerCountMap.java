package com.revolsys.collection.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class IntegerCountMap<K> {
  private final Map<K, AtomicInteger> counterByLabel = new HashMap<>();

  private synchronized void appendCountsText(final StringBuilder sb) {
    int totalCount = 0;

    sb.append("\n");
    for (final Entry<K, AtomicInteger> entry : this.counterByLabel.entrySet()) {
      sb.append(entry.getKey());
      sb.append("\t");
      final AtomicInteger counter = entry.getValue();
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

  public synchronized void clearCounts() {
    this.counterByLabel.clear();
  }

  public synchronized void clearCounts(final K key) {
    if (key != null) {
      this.counterByLabel.remove(key);
    }
  }

  public synchronized boolean decrementCount(final K key) {
    if (key == null) {
      return false;
    } else {
      final AtomicInteger counter = this.counterByLabel.get(key);
      if (counter == null) {
        return false;
      } else {
        if (counter.decrementAndGet() <= 0) {
          this.counterByLabel.remove(key);
          return false;
        } else {
          return true;
        }
      }
    }
  }

  public synchronized int getCount(final K key) {
    if (key != null) {
      final AtomicInteger counter = this.counterByLabel.get(key);
      if (counter != null) {
        return counter.get();
      }
    }
    return 0;
  }

  public synchronized Set<K> getLabels() {
    return this.counterByLabel.keySet();
  }

  public synchronized boolean incrementCount(final K key) {
    if (key == null) {
      return false;
    } else {
      AtomicInteger counter = this.counterByLabel.get(key);
      if (counter == null) {
        counter = new AtomicInteger(1);
        this.counterByLabel.put(key, counter);
      } else {
        counter.addAndGet(1);
      }
      return true;
    }
  }

  public boolean isEmpty() {
    return this.counterByLabel.isEmpty();
  }

  public int size() {
    return this.counterByLabel.size();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    appendCountsText(sb);
    return sb.toString();
  }

}
