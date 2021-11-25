package com.revolsys.record.io.format.gpx;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.AbstractRecordIoFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public class Gpx extends AbstractRecordIoFactory implements RecordWriterFactory {
  public static final String GPX_NS = "gpx";

  public static final String GPX_NS_URI = "http://www.topografix.com/GPX/1/1";

  public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.floating3d(EpsgId.WGS84);

  public Gpx() {
    super("GPS Exchange Format");
    addMediaTypeAndFileExtension("application/gpx+xml", "gpx");
  }

  @Override
  public boolean isCustomFieldsSupported() {
    return false;
  }

  public RecordReader newRecordReader(final RecordDefinition recordDefinition,
    final Resource resource, final RecordFactory<? extends Record> factory) {
    throw new UnsupportedOperationException();
  }

  /**
   * Construct a new reader for the file using the specified data object factory.
   * @param factory The factory used to create data objects.
   * @param inputStream The file to read.
   *
   * @return The reader for the file.
   */
  @Override
  public RecordReader newRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory, final MapEx properties) {
    try {
      return new GpxIterator(resource, recordFactory, null);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to open resource " + resource, e);
    }
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.newUtf8Writer(outputStream);
    return new GpxRecordWriter(recordDefinition, writer);
  }
}
