package com.revolsys.geometry.io;

import java.util.function.Consumer;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.spring.resource.Resource;

public interface PointReader extends Reader<Point>, GeometryFactoryProxy {

  static void forEachPoint(final Object source, final Consumer<? super Point> action) {
    try (
      final PointReader reader = PointReader.newPointReader(source)) {
      if (reader != null) {
        reader.forEach(action);
      }
    }
  }

  static boolean isReadable(final Object source) {
    final PointReaderFactory readerFactory = IoFactory.factory(PointReaderFactory.class, source);
    if (readerFactory == null) {
      return false;
    } else {
      return true;
    }
  }

  static PointReader newPointReader(final Object source) {
    return newPointReader(source, MapEx.EMPTY);
  }

  static PointReader newPointReader(final Object source, final GeometryFactory geometryFactory) {
    final MapEx properties = new LinkedHashMapEx("geometryFactory", geometryFactory);
    return newPointReader(source, properties);
  }

  static PointReader newPointReader(final Object source, final MapEx properties) {
    final PointReaderFactory readerFactory = IoFactory.factory(PointReaderFactory.class, source);
    if (readerFactory == null || !readerFactory.isGeometrySupported()) {
      return null;
    } else {
      final Resource resource = readerFactory.getZipResource(source);
      return readerFactory.newPointReader(resource, properties);
    }
  }

  default RecordDefinition newRecordDefinition(final String name) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final RecordDefinition recordDefinition = new RecordDefinitionBuilder(name) //
      .addField("POINT", GeometryDataTypes.POINT) //
      .setGeometryFactory(geometryFactory) //
      .getRecordDefinition();
    return recordDefinition;
  }
}
