package com.revolsys.record.io;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Supplier;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.io.GeometryReaderFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.map.MapReader;
import com.revolsys.io.map.MapReaderFactory;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;
import com.revolsys.util.SupplierWithProperties;

public interface RecordReaderFactory extends GeometryReaderFactory, MapReaderFactory {
  @SuppressWarnings("unchecked")
  static Supplier<RecordReader> newRecordReaderSupplier(
    final Map<String, ? extends Object> properties) {
    final String fileName = (String)properties.get("fileName");
    final String fileUrl = (String)properties.get("fileUrl");
    String defaultFileExtension;
    Object source;
    if (Property.hasValue(fileName)) {
      source = Paths.get(fileName);
      defaultFileExtension = FileUtil.getFileNameExtension(fileName);

    } else if (Property.hasValue(fileUrl)) {
      final Resource resource = Resource.getResource(fileUrl);
      source = resource;
      defaultFileExtension = resource.getFileNameExtension();
    } else {
      throw new IllegalArgumentException("Config must have fileName or fileUrl:" + properties);
    }
    final String fileExtension = Maps.getString(properties, "fileExtension", defaultFileExtension);
    final Supplier<RecordReader> factory = () -> {
      final RecordReader reader;
      if ("zip".equalsIgnoreCase(fileExtension)) {
        final String baseFileExtension = (String)properties.get("baseFileExtension");
        String baseName = (String)properties.get("baseName");
        if (!Property.hasValue(baseName)) {
          baseName = (String)properties.get("baseFileName");
        }
        if (Property.hasValue(baseName)) {
          reader = RecordReader.newZipRecordReader(source, baseName, baseFileExtension);
        } else {
          reader = RecordReader.newZipRecordReader(source, baseFileExtension);
        }
      } else {
        reader = RecordReader.newRecordReader(source);
      }
      if (reader != null) {
        final Map<String, Object> readerProperties = (Map<String, Object>)properties
          .get("readerProperties");
        reader.setProperties(readerProperties);
      }
      return reader;
    };
    return new SupplierWithProperties<>(factory, properties);
  }

  /**
   * Construct a new directory reader using the ({@link ArrayRecordFactory}).
   *
   * @return The reader.
   */
  default Reader<Record> newDirectoryRecordReader() {
    final RecordDirectoryReader directoryReader = new RecordDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    return directoryReader;
  }

  /**
   * Construct a new reader for the directory using the ({@link ArrayRecordFactory}
   * ).
   *
   * @param directory The directory to read.
   * @return The reader for the file.
   */
  default Reader<Record> newDirectoryRecordReader(final File directory) {
    return newDirectoryRecordReader(directory, ArrayRecord.FACTORY);

  }

  /**
   * Construct a new reader for the directory using the specified data object
   * recordFactory.
   *
   * @param directory directory file to read.
   * @param recordFactory The recordFactory used to create data objects.
   * @return The reader for the file.
   */
  default <R extends Record> Reader<Record> newDirectoryRecordReader(final File directory,
    final RecordFactory<R> recordFactory) {
    final RecordDirectoryReader directoryReader = new RecordDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    directoryReader.setDirectory(directory);
    return directoryReader;
  }

  @Override
  default GeometryReader newGeometryReader(final Resource resource) {
    final RecordReader recordReader = newRecordReader(resource);
    final RecordReaderGeometryReader geometryReader = new RecordReaderGeometryReader(recordReader);
    return geometryReader;
  }

  @Override
  default GeometryReader newGeometryReader(final Resource resource, final MapEx properties) {
    final RecordReader recordReader = newRecordReader(resource, ArrayRecord.FACTORY, properties);
    final RecordReaderGeometryReader geometryReader = new RecordReaderGeometryReader(recordReader);
    return geometryReader;
  }

  @Override
  default MapReader newMapReader(final Resource resource) {
    final RecordReader reader = newRecordReader(resource);
    return new RecordMapReader(reader);
  }

  /**
   * Construct a new reader for the resource using the ({@link ArrayRecordFactory}
   * ).
   *
   * @param file The file to read.
   * @return The reader for the file.
   */
  default RecordReader newRecordReader(final Object object) {
    return newRecordReader(object, ArrayRecord.FACTORY);

  }

  /**
   * Construct a new {@link RecordReader} for the given source. The source can be one of the following
   * classes.
   *
   * <ul>
   *   <li>{@link PathUtil}</li>
   *   <li>{@link File}</li>
   *   <li>{@link Resource}</li>
   * </ul>
   * @param source The source to read the records from.
   * @param recordFactory The factory used to create records.
   * @return The reader.
   * @throws IllegalArgumentException If the source is not a supported class.
   */
  default RecordReader newRecordReader(final Object source,
    final RecordFactory<? extends Record> factory) {
    final Resource resource = Resource.getResource(source);
    return newRecordReader(resource, factory, MapEx.EMPTY);
  }

  RecordReader newRecordReader(Resource resource, RecordFactory<? extends Record> factory,
    MapEx properties);
}
