package com.revolsys.gis.parallel;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.Record;

public class OutsideBoundaryInOutProcess extends BaseInOutProcess<Record, Record> {

  private OutsideBoundaryObjects outsideBoundaryObjects;

  public OutsideBoundaryObjects getOutsideBoundaryObjects() {
    return this.outsideBoundaryObjects;
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    if (this.outsideBoundaryObjects.boundaryContains(object)) {
      this.outsideBoundaryObjects.removeObject(object);
      out.write(object);
    } else {
      this.outsideBoundaryObjects.addObject(object);
    }
  }

  public void setOutsideBoundaryObjects(final OutsideBoundaryObjects outsideBoundaryObjects) {
    this.outsideBoundaryObjects = outsideBoundaryObjects;
  }

}
