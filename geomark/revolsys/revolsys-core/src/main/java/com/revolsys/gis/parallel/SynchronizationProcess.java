package com.revolsys.gis.parallel;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractInOutProcess;
import com.revolsys.record.Record;

public class SynchronizationProcess extends AbstractInOutProcess<Record, Record> {
  private int count = 0;

  @Override
  public synchronized Channel<Record> getIn() {
    this.count++;
    return super.getIn();
  }

  @Override
  protected void run(final Channel<Record> in, final Channel<Record> out) {
    do {
      for (Record object = in.read(); object != null; object = in.read()) {
        out.write(object);
      }
      this.count--;
    } while (this.count > 0);
  }
}
