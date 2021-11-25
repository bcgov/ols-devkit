package com.revolsys.record.io.format.saif.util;

import java.io.IOException;
import java.util.Map;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.record.io.format.saif.SaifConstants;
import com.revolsys.record.io.format.saif.geometry.AlignedPoint;

public class AlignedPointConverter extends PointConverter {
  public AlignedPointConverter(final GeometryFactory geometryFactory) {
    super(geometryFactory, SaifConstants.ALIGNED_POINT);
  }

  @Override
  public Point newPoint(final GeometryFactory geometryFactory, final double... coordinates) {
    return new AlignedPoint(geometryFactory, coordinates);
  }

  /**
   * north, directionIndicator are handled by the default handling
   */
  @Override
  protected void readAttribute(final OsnIterator iterator, final String fieldName,
    final Map<String, Object> values) {
    if (fieldName.equals("alignment")) {
      values.put("alignment", iterator.nextDoubleValue());
    } else {
      super.readAttribute(iterator, fieldName, values);
    }
  }

  @Override
  protected void writeAttributes(final OsnSerializer serializer, final Geometry geometry)
    throws IOException {
    writeAttribute(serializer, geometry, "alignment");
    writeAttribute(serializer, geometry, "directionIndicator");
    writeAttributeEnum(serializer, geometry, "north");
    super.writeAttributes(serializer, geometry);
  }

}
