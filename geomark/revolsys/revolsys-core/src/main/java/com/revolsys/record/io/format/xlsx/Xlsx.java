package com.revolsys.record.io.format.xlsx;

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
import com.revolsys.spring.resource.Resource;

public class Xlsx extends AbstractRecordIoFactory implements RecordWriterFactory {
  public static final String DESCRIPTION = "Excel Workbook";

  public static final String FILE_EXTENSION = "xlsx";

  public static final String MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  public static final char QUOTE_CHARACTER = '"';

  public Xlsx() {
    super(Xlsx.DESCRIPTION);
    addMediaTypeAndFileExtension(Xlsx.MEDIA_TYPE, Xlsx.FILE_EXTENSION);
  }

  @Override
  public RecordReader newRecordReader(final Object source,
    final RecordFactory<? extends Record> factory) {
    final Resource resource = Resource.getResource(source);
    return new XlsxRecordReader(resource, factory);
  }

  @Override
  public RecordReader newRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory, final MapEx properties) {
    return new XlsxRecordReader(resource, recordFactory, properties);
  }

  @Override
  public RecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition,
    final Resource resource) {
    return new XlsxRecordWriter(recordDefinition, resource);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    return new XlsxRecordWriter(recordDefinition, outputStream);
  }
}
