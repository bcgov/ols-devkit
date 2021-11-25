package com.revolsys.util.count;

import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanNameAware;

import com.revolsys.record.Record;

public class LabelCountIterable implements Iterable<Record>, BeanNameAware {
  private Iterable<Record> iterable;

  private LabelCounters labelCountMap;

  private String statsName;

  public LabelCountIterable() {
  }

  public LabelCountIterable(final Iterable<Record> iterable) {
    setIterable(iterable);
  }

  public Iterable<Record> getIterable() {
    return this.iterable;
  }

  /**
   * @return the stats
   */
  public LabelCounters getStatistics() {
    return this.labelCountMap;
  }

  public String getStatsName() {
    return this.statsName;
  }

  @PostConstruct
  public void init() {
    if (this.labelCountMap == null) {
      setStatistics(new LabelCountMap("Read " + this.statsName + " " + this.iterable.toString()));
    }
  }

  @Override
  public Iterator<Record> iterator() {
    if (this.labelCountMap == null) {
      setStatistics(new LabelCountMap("Read " + this.statsName + " " + this.iterable.toString()));
    }
    return new LabelCountIterator(this.iterable.iterator(), this.labelCountMap);
  }

  @Override
  public void setBeanName(final String beanName) {
    if (this.statsName == null) {
      this.statsName = beanName.replaceAll("Stats", "");
    }
  }

  public void setIterable(final Iterable<Record> iterable) {
    this.iterable = iterable;
  }

  /**
   * @param stats the stats to set
   */
  public void setStatistics(final LabelCounters labelCountMap) {
    this.labelCountMap = labelCountMap;
  }

  public void setStatsName(final String statsName) {
    this.statsName = statsName;
  }

  @Override
  public String toString() {
    return this.iterable.toString();
  }

}
