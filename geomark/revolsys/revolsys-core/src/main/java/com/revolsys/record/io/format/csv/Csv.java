package com.revolsys.record.io.format.csv;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.FileUtil;
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
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public class Csv extends AbstractRecordIoFactory implements RecordWriterFactory, MapWriterFactory {
  public static final char FIELD_SEPARATOR = ',';

  public static final String MIME_TYPE = "text/csv";

  public static CsvWriter plainWriter(final File file) {
    if (file == null) {
      throw new NullPointerException("File must not be null");
    } else {
      final java.io.Writer writer = FileUtil.newUtf8Writer(file);
      return plainWriter(writer);
    }
  }

  public static CsvWriter plainWriter(final java.io.Writer writer) {
    return new CsvWriter(writer);
  }

  /**
   * Convert a to a CSV string with a header row and a data row.
   *
   * @param map The to convert to CSV
   * @return The CSV string.
   */
  public static String toCsv(final Map<String, ? extends Object> map) {
    final StringWriter csvString = new StringWriter();
    try (
      final CsvMapWriter csvMapWriter = new CsvMapWriter(csvString)) {
      csvMapWriter.write(map);
      return csvString.toString();
    }
  }

  /*
   * Replaces whitespace with spaces
   */
  public static void writeColumns(final StringWriter out,
    final Collection<? extends Object> columns, final char fieldSeparator,
    final char recordSeparator) {
    boolean first = true;
    for (final Object value : columns) {
      if (first) {
        first = false;
      } else {
        out.write(fieldSeparator);
      }
      if (value != null) {
        String text = DataTypes.toString(value);
        text = text.replaceAll("\\s", " ");

        out.write(text);
      }
    }
    out.write(recordSeparator);
  }

  public Csv() {
    super("Comma-Separated Values");
    addMediaTypeAndFileExtension(MIME_TYPE, "csv");
  }

  @Override
  public MapReader newMapReader(final Resource resource) {
    try {
      final CsvMapIterator iterator = new CsvMapIterator(resource, FIELD_SEPARATOR);
      return new IteratorMapReader(iterator);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public MapWriter newMapWriter(final java.io.Writer out) {
    return new CsvMapWriter(out);
  }

  @Override
  public RecordReader newRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory, final MapEx properties) {
    final CsvRecordReader reader = new CsvRecordReader(resource, recordFactory);
    reader.setProperties(properties);
    return reader;
  }

  @Override
  public RecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition,
    final Resource resource) {
    return new CsvRecordWriter(recordDefinition, resource, Csv.FIELD_SEPARATOR, true, true);
  }

  public CsvRecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition,
    final Writer writer) {
    return new CsvRecordWriter(recordDefinition, writer, Csv.FIELD_SEPARATOR, true, true);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);

    return new CsvRecordWriter(recordDefinition, writer, Csv.FIELD_SEPARATOR, true, true);
  }
}
