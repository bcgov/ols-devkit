package com.revolsys.geometry.io;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.spring.resource.Resource;

public interface GeometryReader extends Reader<Geometry> {
  static boolean isReadable(final Object source) {
    final GeometryReaderFactory readerFactory = IoFactory.factory(GeometryReaderFactory.class,
      source);
    if (readerFactory == null || !readerFactory.isGeometrySupported()) {
      return false;
    } else {
      return true;
    }
  }

  static GeometryReader newGeometryReader(final Object source) {
    return newGeometryReader(source, MapEx.EMPTY);
  }

  static GeometryReader newGeometryReader(final Object source,
    final GeometryFactory geometryFactory) {
    final MapEx properties = new LinkedHashMapEx("geometryFactory", geometryFactory);
    return newGeometryReader(source, properties);
  }

  static GeometryReader newGeometryReader(final Object source, final MapEx properties) {
    final GeometryReaderFactory readerFactory = IoFactory.factory(GeometryReaderFactory.class,
      source);
    if (readerFactory == null || !readerFactory.isGeometrySupported()) {
      return null;
    } else {
      final Resource resource = readerFactory.getZipResource(source);
      return readerFactory.newGeometryReader(resource, properties);
    }
  }

  GeometryFactory getGeometryFactory();

  default ClockDirection getPolygonRingDirection() {
    return ClockDirection.NONE;
  }

  default RecordDefinition newRecordDefinition(final String name) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final RecordDefinition recordDefinition = new RecordDefinitionBuilder(name) //
      .addField("GEOMETRY", GeometryDataTypes.GEOMETRY) //
      .setGeometryFactory(geometryFactory) //
      .getRecordDefinition();
    return recordDefinition;
  }
}
