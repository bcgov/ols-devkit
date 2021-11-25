package com.revolsys.record.io.format.saif.util;

public class SpatialObjectConverter implements OsnConverter {
  private final OsnConverterRegistry converters;

  public SpatialObjectConverter(final OsnConverterRegistry converters) {
    this.converters = converters;
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final String name = iterator.nextFieldName();
    if (!name.equals("geometry")) {
      iterator.throwParseError("No geometry attribute");
    }
    final String objectName = iterator.nextObjectName();
    final OsnConverter osnConverter = this.converters.getConverter(objectName);
    if (osnConverter == null) {
      iterator.throwParseError("No Geometry Converter for " + objectName);
    }
    final Object geometry = osnConverter.read(iterator);
    iterator.nextEndObject();
    return geometry;
  }

  @Override
  public void write(final OsnSerializer serializer, final Object object) {
  }
}
