package com.revolsys.record.io.format.saif.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.record.io.format.saif.SaifConstants;
import com.revolsys.record.io.format.saif.geometry.TextLinePoint;
import com.revolsys.record.io.format.saif.geometry.TextOnCurve;

public class TextOnCurveConverter implements OsnConverter {
  private final OsnConverterRegistry converters;

  private final GeometryFactory geometryFactory;

  public TextOnCurveConverter(final GeometryFactory geometryFactory,
    final OsnConverterRegistry converters) {
    this.geometryFactory = geometryFactory;
    this.converters = converters;
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final List<Point> points = new ArrayList<>();

    String fieldName = iterator.nextFieldName();
    while (fieldName != null) {
      if (fieldName.equals("characters")) {
        while (iterator.next() != OsnIterator.END_LIST) {
          final String objectName = iterator.nextObjectName();
          final OsnConverter osnConverter = this.converters.getConverter(objectName);
          if (osnConverter == null) {
            iterator.throwParseError("No Geometry Converter for " + objectName);
          }
          points.add((TextLinePoint)osnConverter.read(iterator));
        }
      }
      fieldName = iterator.nextFieldName();
    }
    final Geometry geometry = new TextOnCurve(this.geometryFactory, points);
    return geometry;
  }

  protected void readAttribute(final OsnIterator iterator, final String fieldName,
    final Map<String, Object> values) {
    iterator.next();
    values.put(fieldName, iterator.getValue());
  }

  @Override
  public void write(final OsnSerializer serializer, final Object object) throws IOException {
    if (object instanceof Punctual) {
      final Punctual punctual = (Punctual)object;
      serializer.startObject(SaifConstants.TEXT_ON_CURVE);
      serializer.fieldName("characters");
      serializer.startCollection("List");
      final OsnConverter osnConverter = this.converters.getConverter(SaifConstants.TEXT_LINE);
      for (final Point point : punctual.points()) {
        osnConverter.write(serializer, point);
      }
      serializer.endCollection();
      serializer.endAttribute();

      serializer.endObject();
    }
  }

  @Override
  public void writeAttribute(final OsnSerializer serializer, final Object object, final String name)
    throws IOException {
  }

}
