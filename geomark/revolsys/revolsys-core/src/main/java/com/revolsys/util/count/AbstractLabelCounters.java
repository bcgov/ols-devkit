package com.revolsys.util.count;

import org.jeometry.common.logging.Logs;

import com.revolsys.util.Counter;

public abstract class AbstractLabelCounters implements LabelCounters {

  private boolean logCounts = true;

  protected String message;

  private int providerCount = 0;

  public AbstractLabelCounters() {
    super();
  }

  @Override
  public synchronized void addCountsText(final StringBuilder sb) {
    int totalCount = 0;
    if (this.message != null) {
      sb.append(this.message);
    }
    sb.append("\n");
    for (final String label : getLabels()) {
      sb.append(label);
      sb.append("\t");
      final Counter counter = getCounter(label);
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
  public synchronized void connect() {
    this.providerCount++;
  }

  @Override
  public synchronized void disconnect() {
    this.providerCount--;
    if (this.providerCount <= 0) {
      logCounts();
    }
  }

  @Override
  public String getMessage() {
    return this.message;
  }

  @Override
  public boolean isLogCounts() {
    return this.logCounts;
  }

  @Override
  public synchronized String logCounts() {
    final StringBuilder sb = new StringBuilder();
    addCountsText(sb);
    final String string = sb.toString();
    if (isLogCounts() && !isEmpty()) {
      Logs.info(this, string);
    }
    return string;
  }

  @Override
  public void setLogCounts(final boolean logCounts) {
    this.logCounts = logCounts;
  }

  @Override
  public void setMessage(final String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return this.message;
  }

}
