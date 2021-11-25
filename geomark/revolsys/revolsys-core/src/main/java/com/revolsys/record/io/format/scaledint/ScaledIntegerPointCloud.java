package com.revolsys.record.io.format.scaledint;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryWriterFactory;
import com.revolsys.geometry.io.PointReaderFactory;
import com.revolsys.record.io.GeometryRecordReaderFactory;
import com.revolsys.spring.resource.OutputStreamResource;
import com.revolsys.spring.resource.Resource;

public class ScaledIntegerPointCloud extends GeometryRecordReaderFactory
  implements GeometryWriterFactory, PointReaderFactory {

  public static final String FILE_EXTENSION = "sipc";

  public static final String FILE_EXTENSION_GZ = FILE_EXTENSION + ".gz";

  public static final String FILE_EXTENSION_ZIP = FILE_EXTENSION + ".zip";

  public static final String FILE_TYPE_HEADER = "SIPC";

  public static final byte[] FILE_TYPE_HEADER_BYTES = FILE_TYPE_HEADER
    .getBytes(StandardCharsets.UTF_8);

  public static final int HEADER_SIZE = 60;

  public static final String MIME_TYPE = "application/x-revolsys-sipc";

  public static final int RECORD_SIZE = 12;

  public static final short VERSION = 1;

  public ScaledIntegerPointCloud() {
    super("Scaled Integer Point Cloud");
    addMediaTypeAndFileExtension(MIME_TYPE, FILE_EXTENSION);
    addFileExtension(FILE_EXTENSION_ZIP);
    addFileExtension(FILE_EXTENSION_GZ);
  }

  @Override
  public boolean isCustomFieldsSupported() {
    return false;
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public ScaledIntegerPointCloudGeometryReader newGeometryReader(final Resource resource,
    final MapEx properties) {
    return new ScaledIntegerPointCloudGeometryReader(resource, properties);
  }

  @Override
  public ScaledIntegerPointCloudGeometryWriter newGeometryWriter(final Resource resource,
    final MapEx properties) {
    return new ScaledIntegerPointCloudGeometryWriter(resource, properties);
  }

  @Override
  public ScaledIntegerPointCloudGeometryWriter newGeometryWriter(final String baseName,
    final OutputStream out, final Charset charset) {
    final OutputStreamResource resource = new OutputStreamResource(baseName, out);
    return newGeometryWriter(resource, MapEx.EMPTY);
  }

  @Override
  public ScaledIntegerPointCloudPointReader newPointReader(final Resource resource,
    final MapEx properties) {
    return new ScaledIntegerPointCloudPointReader(resource, properties);
  }
}
