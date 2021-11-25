package com.revolsys.record.io.format.tsv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.map.IteratorMapReader;
import com.revolsys.io.map.MapReader;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.AbstractRecordIoFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.format.csv.CsvMapIterator;
import com.revolsys.record.io.format.csv.CsvMapWriter;
import com.revolsys.record.io.format.csv.CsvRecordReader;
import com.revolsys.record.io.format.csv.CsvRecordWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public class Tsv extends AbstractRecordIoFactory implements RecordWriterFactory, MapWriterFactory {
  public static final String DESCRIPTION = "Tab-Separated Values";

  public static final char FIELD_SEPARATOR = '\t';

  public static final String FILE_EXTENSION = "tsv";

  public static final String MIME_TYPE = "text/tab-separated-values";

  public static final char QUOTE_CHARACTER = '"';

  public static MapReader mapReader(final Object source) {
    final Resource resource = Resource.getResource(source);
    try {
      final CsvMapIterator iterator = new CsvMapIterator(resource, FIELD_SEPARATOR);
      return new IteratorMapReader(iterator);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public static RecordWriter newRecordWriter(final RecordDefinition recordDefinition,
    final Object target, final boolean useQuotes, final boolean ewkt) {
    return new CsvRecordWriter(recordDefinition, target, Tsv.FIELD_SEPARATOR, useQuotes, ewkt);
  }

  public static RecordWriter newRecordWriter(final RecordDefinition recordDefinition,
    final Writer writer, final boolean useQuotes, final boolean ewkt) {
    return new CsvRecordWriter(recordDefinition, writer, Tsv.FIELD_SEPARATOR, useQuotes, ewkt);
  }

  public static TsvWriter plainWriter(final Object source) {
    if (source == null) {
      throw new NullPointerException("source must not be null");
    } else {
      final Resource resource = Resource.getResource(source);
      final Writer writer = resource.newWriter();
      return plainWriter(writer);
    }
  }

  public static TsvWriter plainWriter(final Writer writer) {
    return new TsvWriter(writer);
  }

  public Tsv() {
    super(Tsv.DESCRIPTION);
    addMediaTypeAndFileExtension(Tsv.MIME_TYPE, Tsv.FILE_EXTENSION);
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public MapReader newMapReader(final Resource resource) {
    return mapReader(resource);
  }

  @Override
  public MapWriter newMapWriter(final Writer out) {
    return new CsvMapWriter(out, Tsv.FIELD_SEPARATOR, true);
  }

  @Override
  public RecordReader newRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory, final MapEx properties) {
    final CsvRecordReader reader = new CsvRecordReader(resource, recordFactory,
      Tsv.FIELD_SEPARATOR);
    reader.setProperties(properties);
    return reader;
  }

  @Override
  public CsvRecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition,
    final Resource resource) {
    return new CsvRecordWriter(recordDefinition, resource, Tsv.FIELD_SEPARATOR, true, true);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);

    return new CsvRecordWriter(recordDefinition, writer, Tsv.FIELD_SEPARATOR, true, true);
  }
}
