package com.revolsys.record.io.format.shp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.io.PathName;

import com.revolsys.io.AbstractDirectoryReader;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordDirectoryReader;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

/**
 * <p>
 * The ShapefileDirectoryReader is a that can read .shp
 * data files contained in a single directory. The reader will iterate through
 * the .shp files in alpabetical order returning all features.
 * </p>
 * <p>
 * See the {@link AbstractDirectoryReader} class for examples on how to use
 * dataset readers.
 * </p>
 *
 * @author Paul Austin
 * @see AbstractDirectoryReader
 */
public class ShapefileDirectoryReader extends RecordDirectoryReader {
  private final Map<String, String> fileNameTypeMap = new HashMap<>();

  private Map<String, RecordDefinition> typeNameRecordDefinitionMap = new HashMap<>();

  public ShapefileDirectoryReader() {
    setFileExtensions(ShapefileConstants.FILE_EXTENSION);
  }

  /**
   * Construct a new ShapefileDirectoryReader.
   *
   * @param directory The containing the .shp files.
   */
  public ShapefileDirectoryReader(final File directory) {
    this();
    setDirectory(directory);
  }

  public Map<String, String> getFileNameTypeMap() {
    return this.fileNameTypeMap;
  }

  public Map<String, RecordDefinition> getTypeNameRecordDefinitionMap() {
    return this.typeNameRecordDefinitionMap;
  }

  @Override
  protected RecordReader newReader(final Resource resource) {
    try {
      final RecordFactory<ArrayRecord> recordFactory = ArrayRecord.FACTORY;
      final ShapefileRecordReader iterator = new ShapefileRecordReader(resource, recordFactory);
      final String baseName = resource.getBaseName().toUpperCase();
      iterator.setTypeName(PathName.newPathName(this.fileNameTypeMap.get(baseName)));
      iterator.setRecordDefinition(this.typeNameRecordDefinitionMap.get(iterator.getTypeName()));
      return iterator;
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  public void setFileNameTypeMap(final Map<String, String> fileNameTypeMap) {
    this.fileNameTypeMap.clear();
    for (final Entry<String, String> entry : fileNameTypeMap.entrySet()) {
      final String fileName = entry.getKey();
      final String typeName = entry.getValue();
      this.fileNameTypeMap.put(fileName.toUpperCase(), typeName);
    }
    setBaseFileNames(fileNameTypeMap.keySet());
  }

  public void setTypeNameRecordDefinitionMap(
    final Map<String, RecordDefinition> typeNameRecordDefinitionMap) {
    this.typeNameRecordDefinitionMap = typeNameRecordDefinitionMap;
  }

}
