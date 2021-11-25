package com.revolsys.record.io.format.kml;

import java.io.OutputStream;
import java.nio.charset.Charset;

import org.jeometry.coordinatesystem.model.CoordinateSystem;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.record.io.GeometryRecordReaderFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public class Kmz extends GeometryRecordReaderFactory
  implements RecordWriterFactory, MapWriterFactory {

  public Kmz() {
    super(Kml22Constants.KMZ_FORMAT_DESCRIPTION);
    addMediaTypeAndFileExtension(Kml22Constants.KMZ_MEDIA_TYPE, Kml22Constants.KMZ_FILE_EXTENSION);
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public boolean isCoordinateSystemSupported(final CoordinateSystem coordinateSystem) {
    return Kml.COORDINATE_SYSTEM.equals(coordinateSystem);
  }

  @Override
  public GeometryReader newGeometryReader(final Resource resource, final MapEx properties) {
    return new KmzGeometryIterator(resource, properties);
  }

  @Override
  public MapWriter newMapWriter(final java.io.Writer out) {
    throw new IllegalArgumentException("Cannot use a writer");
  }

  @Override
  public MapWriter newMapWriter(final OutputStream out) {
    return new KmzMapWriter(out);
  }

  @Override
  public MapWriter newMapWriter(final OutputStream out, final Charset charset) {
    return newMapWriter(out);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    return new KmzRecordWriter(recordDefinition, outputStream, charset);
  }
}
