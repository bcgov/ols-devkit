package com.revolsys.gis.parallel;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.Record;
import com.revolsys.util.count.LabelCountMap;
import com.revolsys.util.count.LabelCounters;

public class StatisticsProcess extends BaseInOutProcess<Record, Record> {

  private LabelCounters labelCountMap;

  @Override
  protected void postRun(final Channel<Record> in, final Channel<Record> out) {
    if (this.labelCountMap != null) {
      this.labelCountMap.disconnect();
    }
  }

  @Override
  protected void preRun(final Channel<Record> in, final Channel<Record> out) {
    this.labelCountMap = new LabelCountMap(getBeanName());
    this.labelCountMap.connect();
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    this.labelCountMap.addCount(object);
    out.write(object);
  }

}
