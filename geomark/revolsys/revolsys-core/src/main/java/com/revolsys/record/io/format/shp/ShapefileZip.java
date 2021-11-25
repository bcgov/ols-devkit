package com.revolsys.record.io.format.shp;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.FileUtil;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.AbstractRecordIoFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.format.zip.ZipRecordReader;
import com.revolsys.record.io.format.zip.ZipRecordWriter;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;

public class ShapefileZip extends AbstractRecordIoFactory implements RecordWriterFactory {

  public ShapefileZip() {
    super("Shapefile (ESRI) inside a ZIP archive");
    addMediaTypeAndFileExtension("application/x-shp+zip", "shpz");
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public RecordReader newRecordReader(final Resource resource,
    final RecordFactory<? extends Record> factory, final MapEx properties) {
    return new ZipRecordReader(resource, ShapefileConstants.FILE_EXTENSION, factory);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    File directory;
    try {
      directory = FileUtil.newTempDirectory(baseName, "zipDir");
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to create temporary directory", e);
    }
    final Resource tempResource = new PathResource(new File(directory, baseName + ".shp"));
    final RecordWriter shapeWriter = new ShapefileRecordWriter(recordDefinition, tempResource);
    return new ZipRecordWriter(directory, shapeWriter, outputStream);
  }
}
