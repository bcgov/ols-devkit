package com.revolsys.record.io;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.io.GeometryReaderFactory;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.spring.resource.Resource;

public abstract class GeometryRecordReaderFactory extends AbstractRecordIoFactory
  implements GeometryReaderFactory {

  public GeometryRecordReaderFactory(final String name) {
    super(name);
  }

  @Override
  public RecordReader newRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory, final MapEx properties) {
    final GeometryReader geometryReader = newGeometryReader(resource, properties);
    if (geometryReader == null) {
      return null;
    } else {
      final String baseName = resource.getBaseName();
      return new GeometryRecordReader(baseName, geometryReader, recordFactory);
    }
  }

}
