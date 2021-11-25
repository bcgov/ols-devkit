package com.revolsys.record.io;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.format.zip.ZipRecordReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public interface RecordReader extends Reader<Record>, RecordDefinitionProxy {
  static RecordReader empty() {
    return new ListRecordReader(null);
  }

  static RecordReader empty(final RecordDefinition recordDefinition) {
    return new ListRecordReader(recordDefinition);
  }

  static boolean isReadable(final Object source) {
    return IoFactory.isAvailable(RecordReaderFactory.class, source);
  }

  /**
   * Construct a new {@link RecordReader} for the given source. The source can be one of the following
   * classes.
   *
   * <ul>
   *   <li>{@link Path}</li>
   *   <li>{@link File}</li>
   *   <li>{@link Resource}</li>
   * </ul>
   * @param source The source to read the records from.
   * @return The reader.
   * @throws IllegalArgumentException If the source is not a supported class.
   */
  static RecordReader newRecordReader(final Object source) {
    return newRecordReader(source, ArrayRecord.FACTORY);
  }

  static RecordReader newRecordReader(final Object source, final GeometryFactory geometryFactory) {
    final LinkedHashMapEx properties = new LinkedHashMapEx("geometryFactory", geometryFactory);
    return newRecordReader(source, ArrayRecord.FACTORY, properties);
  }

  static RecordReader newRecordReader(final Object source, final MapEx properties) {
    return newRecordReader(source, ArrayRecord.FACTORY, properties);
  }

  /**
   * Construct a new {@link RecordReader} for the given source. The source can be one of the following
   * classes.
   *
   * <ul>
   *   <li>{@link Path}</li>
   *   <li>{@link File}</li>
   *   <li>{@link Resource}</li>
   * </ul>
   * @param source The source to read the records from.
   * @param recordFactory The factory used to create records.
   * @return The reader.
   * @throws IllegalArgumentException If the source is not a supported class.
   */
  static RecordReader newRecordReader(final Object source,
    final RecordFactory<? extends Record> recordFactory) {
    return newRecordReader(source, recordFactory, MapEx.EMPTY);
  }

  static RecordReader newRecordReader(final Object source,
    final RecordFactory<? extends Record> recordFactory, final MapEx properties) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class, source);
    if (readerFactory == null) {
      return null;
    } else {
      final Resource resource = readerFactory.getZipResource(source);
      final RecordReader reader = readerFactory.newRecordReader(resource, recordFactory,
        properties);
      return reader;
    }
  }

  static RecordReader newZipRecordReader(final Object source, final String fileExtension) {
    final Resource resource = Resource.getResource(source);
    return new ZipRecordReader(resource, fileExtension, ArrayRecord.FACTORY);
  }

  static RecordReader newZipRecordReader(final Object source, final String baseName,
    final String fileExtension) {
    final Resource resource = Resource.getResource(source);
    return new ZipRecordReader(resource, baseName, fileExtension, ArrayRecord.FACTORY);
  }

  default ClockDirection getPolygonRingDirection() {
    return ClockDirection.NONE;
  }

  default Map<Identifier, Record> readRecordsById() {
    try (
      BaseCloseable closeable = this) {
      final Map<Identifier, Record> recordsById = new TreeMap<>(Identifier.comparator());
      for (final Record record : this) {
        final Identifier identifier = record.getIdentifier();
        recordsById.put(identifier, record);
      }
      return recordsById;
    }
  }
}
