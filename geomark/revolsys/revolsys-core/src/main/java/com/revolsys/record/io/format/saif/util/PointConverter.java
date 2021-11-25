package com.revolsys.record.io.format.saif.util;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.record.io.format.saif.SaifConstants;
import com.revolsys.util.Property;

public class PointConverter implements OsnConverter {
  private String geometryClass = SaifConstants.POINT;

  private final GeometryFactory geometryFactory;

  public PointConverter(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public PointConverter(final GeometryFactory geometryFactory, final String geometryClass) {
    this.geometryFactory = geometryFactory;
    this.geometryClass = geometryClass;
  }

  public Point newPoint(final GeometryFactory geometryFactory, final double... coordinates) {
    return geometryFactory.point(coordinates);
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<>();
    values.put("type", this.geometryClass);
    Point point = null;

    String fieldName = iterator.nextFieldName();
    while (fieldName != null) {
      if (fieldName.equals("coords")) {
        final String coordTypeName = iterator.nextObjectName();
        if (coordTypeName.equals("/Coord3D")) {
          final double x = iterator.nextDoubleAttribute("c1");
          final double y = iterator.nextDoubleAttribute("c2");
          double z = iterator.nextDoubleAttribute("c3");
          if (z == 2147483648.0) {
            z = 0;
          }
          final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(3);
          point = newPoint(geometryFactory, x, y, z);
        } else if (coordTypeName.equals("/Coord2D")) {
          final double x = iterator.nextDoubleAttribute("c1");
          final double y = iterator.nextDoubleAttribute("c2");
          final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(3);
          point = newPoint(geometryFactory, x, y);
        } else {
          iterator.throwParseError("Expecting Coord2D or Coord3D");
        }
        iterator.nextEndObject();
      } else {
        readAttribute(iterator, fieldName, values);
      }
      fieldName = iterator.nextFieldName();
    }
    Property.set(point, values);
    return point;
  }

  protected void readAttribute(final OsnIterator iterator, final String fieldName,
    final Map<String, Object> values) {
    iterator.next();
    values.put(fieldName, iterator.getValue());
  }

  @Override
  public void write(final OsnSerializer serializer, final Object object) throws IOException {
    if (object instanceof Point) {
      final Point point = (Point)object;
      final int axisCount = point.getAxisCount();
      final double x = point.getX();
      final double y = point.getY();
      final double z = point.getZ();
      serializer.startObject(this.geometryClass);
      serializer.fieldName("coords");
      if (axisCount == 2) {
        serializer.startObject("/Coord2D");
        serializer.attribute("c1", x, true);
        serializer.attribute("c2", y, false);
      } else {
        serializer.startObject("/Coord3D");
        serializer.attribute("c1", x, true);
        serializer.attribute("c2", y, true);
        if (Double.isNaN(z)) {
          serializer.attribute("c3", 0, false);
        } else {
          serializer.attribute("c3", z, false);
        }
      }
      serializer.endObject();
      serializer.endAttribute();

      writeAttributes(serializer, point);
      serializer.endObject();
    }
  }

  protected void writeAttributes(final OsnSerializer serializer, final Geometry geometry)
    throws IOException {
    writeAttributeEnum(serializer, geometry, "qualifier");
  }

}
