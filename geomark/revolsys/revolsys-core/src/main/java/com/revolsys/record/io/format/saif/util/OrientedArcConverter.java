package com.revolsys.record.io.format.saif.util;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.model.impl.LineStringDoubleGf;
import com.revolsys.record.io.format.saif.SaifConstants;
import com.revolsys.record.io.format.saif.geometry.ArcLineString;
import com.revolsys.record.io.format.saif.geometry.OrientedArcLineString;
import com.revolsys.util.Property;

public class OrientedArcConverter extends ArcConverter {
  private static final String GEOMETRY_CLASS = SaifConstants.ORIENTED_ARC;

  private final OsnConverterRegistry converters;

  public OrientedArcConverter(final GeometryFactory geometryFactory,
    final OsnConverterRegistry converters) {
    super(geometryFactory, SaifConstants.ARC);
    this.converters = converters;
  }

  @Override
  public LineString newLineString(final GeometryFactory geometryFactory,
    final LineStringEditor line) {
    final int axisCount = geometryFactory.getAxisCount();
    final int vertexCount = line.getVertexCount();
    final double[] coordinates = LineStringDoubleGf.getNewCoordinates(geometryFactory, line);
    return new OrientedArcLineString(geometryFactory, axisCount, vertexCount, coordinates);
  }

  @Override
  public Object read(final OsnIterator iterator) {
    Geometry geometry = null;
    final Map<String, Object> values = new TreeMap<>();
    values.put("type", GEOMETRY_CLASS);
    String name = iterator.nextFieldName();
    while (name != null) {
      if (name.equals("arc")) {
        final String objectName = iterator.nextObjectName();
        final OsnConverter osnConverter = this.converters.getConverter(objectName);
        if (osnConverter == null) {
          iterator.throwParseError("No Geometry Converter for " + objectName);
        }
        geometry = (Geometry)osnConverter.read(iterator);
      } else {
        readAttribute(iterator, name, values);
      }
      name = iterator.nextFieldName();
    }
    Property.set(geometry, values);
    return geometry;
  }

  @Override
  public void write(final OsnSerializer serializer, final Object object) throws IOException {
    if (object instanceof LineString) {
      final LineString line = (LineString)object;
      serializer.startObject(GEOMETRY_CLASS);
      serializer.fieldName("arc");
      super.write(serializer, object, false);
      serializer.endAttribute();
      if (line instanceof ArcLineString) {
        super.writeAttributes(serializer, (ArcLineString)line);
      }
      if (line instanceof OrientedArcLineString) {
        final OrientedArcLineString orientedLine = (OrientedArcLineString)line;
        final String traversalDirection = orientedLine.getTraversalDirection();
        attributeEnum(serializer, "traversalDirection", traversalDirection);
      }
      serializer.endObject();
    }
  }

}
