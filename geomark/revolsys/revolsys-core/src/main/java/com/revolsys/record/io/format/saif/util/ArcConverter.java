package com.revolsys.record.io.format.saif.util;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.model.impl.LineStringDoubleGf;
import com.revolsys.record.io.format.saif.SaifConstants;
import com.revolsys.record.io.format.saif.geometry.ArcLineString;
import com.revolsys.util.Property;

public class ArcConverter implements OsnConverter {
  private final GeometryFactory geometryFactory;

  private final GeometryFactory geometryFactory3d;

  private String geometryType = SaifConstants.ARC;

  public ArcConverter(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.geometryFactory3d = geometryFactory.convertAxisCount(3);
  }

  public ArcConverter(final GeometryFactory geometryFactory, final String geometryType) {
    this(geometryFactory);
    this.geometryType = geometryType;
  }

  public void attributeEnum(final OsnSerializer serializer, final String name, final String value)
    throws IOException {
    if (value != null) {
      serializer.endLine();
      serializer.attributeEnum(name, value, false);
    }
  }

  public LineString newLineString(final GeometryFactory geometryFactory,
    final LineStringEditor line) {
    final int axisCount = geometryFactory.getAxisCount();
    final int vertexCount = line.getVertexCount();
    final double[] coordinates = LineStringDoubleGf.getNewCoordinates(geometryFactory, line);
    return new ArcLineString(geometryFactory, axisCount, vertexCount, coordinates);
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<>();
    values.put(SaifConstants.TYPE, this.geometryType);

    String field = iterator.nextFieldName();
    LineString geometry = null;
    while (field != null) {
      if (field.equals("LineString")) {
        int axisCount = 2;
        final LineStringEditor line = new LineStringEditor(this.geometryFactory);
        while (iterator.next() != OsnIterator.END_LIST) {
          final String pointName = iterator.nextObjectName();
          if (!pointName.equals("/Point")) {
            iterator.throwParseError("Expecting Point object");
          }
          final String coordsName = iterator.nextFieldName();
          if (!coordsName.equals("coords")) {
            iterator.throwParseError("Expecting coords attribute");
          }
          final String coordTypeName = iterator.nextObjectName();
          if (coordTypeName.equals("/Coord3D")) {
            final double x = iterator.nextDoubleAttribute("c1");
            final double y = iterator.nextDoubleAttribute("c2");
            final double z = iterator.nextDoubleAttribute("c3");
            axisCount = 3;
            line.appendVertex(x, y, z);
          } else if (coordTypeName.equals("/Coord2D")) {
            final double x = iterator.nextDoubleAttribute("c1");
            final double y = iterator.nextDoubleAttribute("c2");
            line.appendVertex(x, y);
          } else {
            iterator.throwParseError("Expecting Coord2D or Coord3D");
          }
          iterator.nextEndObject();
          iterator.nextEndObject();
        }
        final int axisCount1 = axisCount;
        final GeometryFactory geometryFactory1 = this.geometryFactory.convertAxisCount(axisCount1);
        geometry = newLineString(geometryFactory1, line);
      } else {
        readAttribute(iterator, field, values);
      }
      field = iterator.nextFieldName();
    }
    Property.set(geometry, values);

    return geometry;
  }

  protected void readAttribute(final OsnIterator iterator, final String fieldName,
    final Map<String, Object> values) {
    final Object value = iterator.nextValue();
    values.put(fieldName, value);
  }

  @Override
  public void write(final OsnSerializer serializer, final Object object) throws IOException {
    final boolean writeAttributes = true;
    write(serializer, object, writeAttributes);
  }

  protected void write(final OsnSerializer serializer, final Object object,
    final boolean writeAttributes) throws IOException {
    if (object instanceof LineString) {
      final LineString line = (LineString)object;
      serializer.startObject(this.geometryType);

      serializer.fieldName("LineString");
      serializer.startCollection("List");
      final LineString points = line;
      final int axisCount = points.getAxisCount();
      for (int i = 0; i < points.getVertexCount(); i++) {
        serializer.startObject(SaifConstants.POINT);
        serializer.fieldName("coords");
        final double x = points.getX(i);
        final double y = points.getY(i);
        final double z = points.getZ(i);
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
        serializer.endObject();
      }
      serializer.endCollection();
      serializer.endAttribute();
      if (writeAttributes && line instanceof ArcLineString) {
        writeAttributes(serializer, (ArcLineString)line);
      }
      serializer.endObject();
    }
  }

  protected void writeAttribute(final OsnSerializer serializer, final Map<String, Object> values,
    final String name) throws IOException {
    final Object value = values.get(name);
    if (value != null) {
      serializer.endLine();
      serializer.attribute(name, value, false);
    }
  }

  @Override
  public void writeAttribute(final OsnSerializer serializer, final Object object, final String name)
    throws IOException {
  }

  protected void writeAttributes(final OsnSerializer serializer, final ArcLineString line)
    throws IOException {
    final String qualifier = line.getQualifier();
    attributeEnum(serializer, "qualifier", qualifier);
  }
}
