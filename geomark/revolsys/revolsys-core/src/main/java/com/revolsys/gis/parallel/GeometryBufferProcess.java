package com.revolsys.gis.parallel;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.Record;
import com.revolsys.record.Records;

public class GeometryBufferProcess extends BaseInOutProcess<Record, Record> {

  private int buffer;

  public int getBuffer() {
    return this.buffer;
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    final Geometry geometry = object.getGeometry();
    if (geometry == null) {
      out.write(object);
    } else {
      final Geometry bufferedGeometry = geometry.buffer(this.buffer);
      final Record newObject = Records.copy(object, bufferedGeometry);
      out.write(newObject);
    }
  }

  public void setBuffer(final int buffer) {
    this.buffer = buffer;
  }

}
