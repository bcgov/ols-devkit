package com.revolsys.gis.parallel;

import com.revolsys.parallel.process.FilterProcess;
import com.revolsys.record.Record;
import com.revolsys.util.count.LabelCounters;

public class StatisticsFilterProcess extends FilterProcess<Record> {

  private LabelCounters acceptStatistics;

  private LabelCounters rejectStatistics;

  @Override
  protected void destroy() {
    if (this.acceptStatistics != null) {
      this.acceptStatistics.disconnect();
    }
    if (this.rejectStatistics != null) {
      this.rejectStatistics.disconnect();
    }
  }

  public LabelCounters getAcceptStatistics() {
    return this.acceptStatistics;
  }

  public LabelCounters getRejectStatistics() {
    return this.rejectStatistics;
  }

  @Override
  protected void initializeDo() {
    if (this.acceptStatistics != null) {
      this.acceptStatistics.connect();
    }
    if (this.rejectStatistics != null) {
      this.rejectStatistics.connect();
    }
  }

  @Override
  protected void postAccept(final Record object) {
    if (this.acceptStatistics != null) {
      this.acceptStatistics.addCount(object);
    }
  }

  @Override
  protected void postReject(final Record object) {
    if (this.rejectStatistics != null) {
      this.rejectStatistics.addCount(object);
    }
  }

  public void setAcceptStatistics(final LabelCounters acceptStatistics) {
    this.acceptStatistics = acceptStatistics;
  }

  public void setRejectStatistics(final LabelCounters rejectStatistics) {
    this.rejectStatistics = rejectStatistics;
  }

}
