package com.revolsys.record.io.format.shp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.AbstractRecordIoFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.OutputStreamResource;
import com.revolsys.spring.resource.Resource;

public class Shapefile extends AbstractRecordIoFactory implements RecordWriterFactory {
  public Shapefile() {
    super(ShapefileConstants.DESCRIPTION);
    addMediaTypeAndFileExtension(ShapefileConstants.MIME_TYPE, ShapefileConstants.FILE_EXTENSION);
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public boolean isSingleFile() {
    return false;
  }

  @Override
  public RecordReader newRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory, final MapEx properties) {
    try {
      return new ShapefileRecordReader(resource, recordFactory);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public RecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition,
    final Resource resource) {
    return new ShapefileRecordWriter(recordDefinition, resource);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    return newRecordWriter(recordDefinition, new OutputStreamResource(baseName, outputStream));
  }
}
