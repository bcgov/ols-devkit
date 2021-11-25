package com.revolsys.elevation.gridded.scaledint.compressed;

import java.nio.charset.StandardCharsets;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModelReaderFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.elevation.gridded.GriddedElevationModelWriterFactory;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.spring.resource.Resource;

public class CompressedScaledIntegerGriddedDigitalElevation extends AbstractIoFactory
  implements GriddedElevationModelReaderFactory, GriddedElevationModelWriterFactory {

  public static final String MEDIA_TYPE = "image/x-revolsys-sigdemz";

  public static final String FILE_EXTENSION = "sigdemz";

  public static final String FILE_FORMAT = "SIGDEMZ";

  public static final byte[] FILE_FORMAT_BYTES = "SIGDEMZ ".getBytes(StandardCharsets.UTF_8);

  public static final int HEADER_SIZE = 132;

  public static final short VERSION = 3;

  public CompressedScaledIntegerGriddedDigitalElevation() {
    super("Compressed Scaled Integer Gridded Elevation Model");
    addMediaTypeAndFileExtension(MEDIA_TYPE, FILE_EXTENSION);
  }

  @Override
  public CompressedScaledIntegerGriddedDigitalElevationModelReader newGriddedElevationModelReader(
    final Resource resource, final MapEx properties) {
    return new CompressedScaledIntegerGriddedDigitalElevationModelReader(resource, properties);
  }

  @Override
  public GriddedElevationModelWriter newGriddedElevationModelWriter(final Resource resource) {
    return new CompressedScaledIntegerGriddedDigitalElevationModelWriter(resource);
  }
}
