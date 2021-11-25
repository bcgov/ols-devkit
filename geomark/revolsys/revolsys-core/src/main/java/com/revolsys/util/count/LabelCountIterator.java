package com.revolsys.util.count;

import java.util.Iterator;

import com.revolsys.record.Record;

public class LabelCountIterator implements Iterator<Record> {
  private final Iterator<Record> iterator;

  private LabelCounters labelCountMap;

  public LabelCountIterator(final Iterator<Record> iterator, final LabelCounters labelCountMap) {
    this.iterator = iterator;
    setStatistics(labelCountMap);
  }

  /**
   * @return the stats
   */
  public LabelCounters getStatistics() {
    return this.labelCountMap;
  }

  @Override
  public boolean hasNext() {
    final boolean hasNext = this.iterator.hasNext();
    if (!hasNext) {
      this.labelCountMap.disconnect();
    }
    return hasNext;
  }

  @Override
  public Record next() {
    final Record object = this.iterator.next();
    if (object != null) {
      this.labelCountMap.addCount(object);
    }
    return object;
  }

  @Override
  public void remove() {
    this.iterator.remove();
  }

  /**
   * @param stats the stats to set
   */
  public void setStatistics(final LabelCounters labelCountMap) {
    this.labelCountMap = labelCountMap;
    labelCountMap.connect();
  }

}
