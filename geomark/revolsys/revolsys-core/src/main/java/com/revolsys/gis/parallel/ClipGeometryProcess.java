package com.revolsys.gis.parallel;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.util.LineStringUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.Record;

public class ClipGeometryProcess extends BaseInOutProcess<Record, Record> {

  private Polygon clipPolygon;

  /**
   * @return the clipPolygon
   */
  public Polygon getClipPolygon() {
    return this.clipPolygon;
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    final Geometry geometry = object.getGeometry();
    if (geometry != null) {
      final Geometry intersection = geometry.intersection(this.clipPolygon);
      if (!intersection.isEmpty() && intersection.getClass() == geometry.getClass()) {
        if (intersection instanceof LineString) {
          final LineString original = (LineString)geometry;
          LineString lineString = (LineString)intersection;
          lineString = LineStringUtil.addElevation(original, lineString);
        }

        object.setGeometryValue(intersection);
        out.write(object);
      }
    } else {
      out.write(object);
    }
  }

  /**
   * @param clipPolygon the clipPolygon to set
   */
  public void setClipPolygon(final Polygon clipPolygon) {
    this.clipPolygon = clipPolygon;
  }

}
