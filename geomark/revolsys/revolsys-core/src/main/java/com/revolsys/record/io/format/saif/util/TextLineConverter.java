package com.revolsys.record.io.format.saif.util;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.record.io.format.saif.SaifConstants;
import com.revolsys.record.io.format.saif.geometry.TextLinePoint;
import com.revolsys.util.Property;

public class TextLineConverter implements OsnConverter {
  private static final String TYPE = "type";

  private final OsnConverterRegistry converters;

  public TextLineConverter(final GeometryFactory geometryFactory,
    final OsnConverterRegistry converters) {
    this.converters = converters;
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<>();
    values.put(TYPE, SaifConstants.TEXT_LINE);
    TextLinePoint geometry = null;

    String fieldName = iterator.nextFieldName();
    while (fieldName != null) {
      if (fieldName.equals("position")) {
        final String objectName = iterator.nextObjectName();
        final OsnConverter osnConverter = this.converters.getConverter(objectName);
        if (osnConverter == null) {
          iterator.throwParseError("No Geometry Converter for " + objectName);
        }
        geometry = new TextLinePoint((Point)osnConverter.read(iterator));
      } else {
        readAttribute(iterator, fieldName, values);
      }
      fieldName = iterator.nextFieldName();
    }
    Property.set(geometry, values);
    return geometry;
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
      serializer.startObject(SaifConstants.TEXT_LINE);
      serializer.fieldName("position");
      final OsnConverter osnConverter = this.converters.getConverter(SaifConstants.POINT);
      osnConverter.write(serializer, point);
      serializer.endAttribute();

      writeAttribute(serializer, point, "characterHeight");
      writeAttribute(serializer, point, "fontName");
      writeAttribute(serializer, point, "orientation");
      writeAttribute(serializer, point, "other");
      writeAttribute(serializer, point, "text");
      serializer.endObject();
    }
  }

}
