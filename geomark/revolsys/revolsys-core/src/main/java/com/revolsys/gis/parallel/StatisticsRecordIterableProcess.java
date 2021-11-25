package com.revolsys.gis.parallel;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.record.Record;
import com.revolsys.util.count.LabelCounters;

public class StatisticsRecordIterableProcess extends IterableProcess<Record> {

  private LabelCounters labelCountMap;

  public StatisticsRecordIterableProcess() {
  }

  @Override
  protected void destroy() {
    super.destroy();
    if (this.labelCountMap != null) {
      this.labelCountMap.disconnect();
      this.labelCountMap = null;
    }
  }

  public LabelCounters getStatistics() {
    return this.labelCountMap;
  }

  public void setStatistics(final LabelCounters labelCountMap) {
    this.labelCountMap = labelCountMap;
    if (labelCountMap != null) {
      labelCountMap.connect();
    }
  }

  @Override
  protected void write(final Channel<Record> out, final Record record) {
    if (record != null) {
      this.labelCountMap.addCount(record);
      out.write(record);
    }
  }
}
