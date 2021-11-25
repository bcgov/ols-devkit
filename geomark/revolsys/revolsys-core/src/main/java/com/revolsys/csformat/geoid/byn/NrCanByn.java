package com.revolsys.csformat.geoid.byn;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.elevation.gridded.GriddedElevationModelReaderFactory;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.spring.resource.Resource;

public class NrCanByn extends AbstractIoFactory implements GriddedElevationModelReaderFactory {

  public static final String FILE_EXTENSION = "byn";

  public static final String FILE_EXTENSION_ZIP = FILE_EXTENSION + ".zip";

  public static final String FILE_EXTENSION_GZ = FILE_EXTENSION + ".gz";

  public NrCanByn() {
    super("NRCAN GEOID BYN");
    addMediaTypeAndFileExtension("image/x-us-ngs-bin", FILE_EXTENSION);
    addFileExtension(FILE_EXTENSION_ZIP);
    addFileExtension(FILE_EXTENSION_GZ);
  }

  @Override
  public GriddedElevationModelReader newGriddedElevationModelReader(final Resource resource,
    final MapEx properties) {
    return new NrCanBynGeoidModelReader(resource, properties);
  }
}
