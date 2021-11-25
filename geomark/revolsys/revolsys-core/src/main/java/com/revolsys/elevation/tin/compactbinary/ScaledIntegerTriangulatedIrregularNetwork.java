package com.revolsys.elevation.tin.compactbinary;

import java.nio.charset.StandardCharsets;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.tin.TriangleConsumer;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkReaderFactory;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriter;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriterFactory;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.spring.resource.Resource;

public class ScaledIntegerTriangulatedIrregularNetwork extends AbstractIoFactory
  implements TriangulatedIrregularNetworkReaderFactory, TriangulatedIrregularNetworkWriterFactory {

  public static final String MEDIA_TYPE = "image/x-revolsys-sitin";

  public static final int HEADER_SIZE = 64;

  public static final String FILE_EXTENSION = "sitin";

  public static final String FILE_EXTENSION_GZ = FILE_EXTENSION + ".gz";

  public static final String FILE_EXTENSION_ZIP = FILE_EXTENSION + ".zip";

  public static final short VERSION = 1;

  public static final String FILE_TYPE = "SITIN ";

  public static final byte[] FILE_TYPE_BYTES = FILE_TYPE.getBytes(StandardCharsets.UTF_8);

  public ScaledIntegerTriangulatedIrregularNetwork() {
    super("Scaled Integer Triangulated Irregular Network");
    addMediaTypeAndFileExtension(MEDIA_TYPE, FILE_EXTENSION);
  }

  @Override
  public void forEachTriangle(final Resource resource, final MapEx properties,
    final TriangleConsumer action) {
    try (
      final ScaledIntegerTriangulatedIrregularNetworkReader reader = new ScaledIntegerTriangulatedIrregularNetworkReader(
        resource, properties)) {
      reader.forEachTriangle(action);
    }
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(final Resource resource,
    final MapEx properties) {
    try (
      ScaledIntegerTriangulatedIrregularNetworkReader reader = new ScaledIntegerTriangulatedIrregularNetworkReader(
        resource, properties)) {
      return reader.newTriangulatedIrregularNetwork();
    }
  }

  @Override
  public TriangulatedIrregularNetworkWriter newTriangulatedIrregularNetworkWriter(
    final Resource resource) {
    return new ScaledIntegerTriangulatedIrregularNetworkWriter(resource);
  }
}
