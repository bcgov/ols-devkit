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
import com.revolsys.record.io.format.saif.geometry.ContourLineString;
import com.revolsys.util.Property;

public class ContourConverter extends ArcConverter {
  private static final String GEOMETRY_CLASS = SaifConstants.CONTOUR;

  private final OsnConverterRegistry converters;

  public ContourConverter(final GeometryFactory geometryFactory,
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
    return new ContourLineString(geometryFactory, axisCount, vertexCount, coordinates);
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<>();
    values.put(SaifConstants.TYPE, GEOMETRY_CLASS);
    Geometry geometry = null;

    String fieldName = iterator.nextFieldName();
    while (fieldName != null) {
      if (fieldName.equals("arc")) {
        final String objectName = iterator.nextObjectName();
        final OsnConverter osnConverter = this.converters.getConverter(objectName);
        if (osnConverter == null) {
          iterator.throwParseError("No Geometry Converter for " + objectName);
        }
        geometry = (Geometry)osnConverter.read(iterator);
      } else if (fieldName.equals("value")) {
        final double value = iterator.nextDoubleValue();
        values.put("value", new Double(value));
      } else {
        readAttribute(iterator, fieldName, values);
      }
      fieldName = iterator.nextFieldName();
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
      super.write(serializer, object);
      serializer.endAttribute();

      if (line instanceof ContourLineString) {
        final ContourLineString contourLine = (ContourLineString)line;
        final String form = contourLine.getForm();
        attributeEnum(serializer, "form", form);
        super.writeAttributes(serializer, (ArcLineString)line);
        final int value = contourLine.getValue();
        attributeEnum(serializer, "form", Integer.toString(value));
      } else if (line instanceof ArcLineString) {
        super.writeAttributes(serializer, (ArcLineString)line);
      }
      serializer.endObject();
    }
  }

}
