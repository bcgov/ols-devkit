package com.revolsys.util.count;

import java.util.Map;
import java.util.function.Supplier;

import com.revolsys.collection.map.Maps;

public class DoubleStatistics {
  public static <K> void addValue(final Map<K, DoubleStatistics> statisticsByKey, final K key,
    final double value) {
    final DoubleStatistics statistics = Maps.get(statisticsByKey, key, DoubleStatistics.factory());
    statistics.addValue(value);
  }

  public static final Supplier<DoubleStatistics> factory() {
    return () -> new DoubleStatistics();
  }

  private double mean;

  private double min = Double.MAX_VALUE;

  private double max = -Double.MAX_VALUE;

  private double sum = 0;

  private int count = 0;

  public synchronized void addValue(final double value) {
    this.count++;
    this.sum += value;
    if (value < this.min) {
      this.min = value;
    }
    if (value > this.max) {
      this.max = value;
    }
    this.mean = this.sum / this.count;
  }

  public int getCount() {
    return this.count;
  }

  public double getMax() {
    return this.max;
  }

  public double getMean() {
    return this.mean;
  }

  public synchronized double getMin() {
    return this.min;
  }

  public synchronized double getRange() {
    return getMax() - getMin();
  }

  public double getSum() {
    return this.sum;
  }
}
