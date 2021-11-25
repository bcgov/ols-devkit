package com.revolsys.csformat.geoid.ngabgh;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.elevation.gridded.GriddedElevationModelReaderFactory;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.spring.resource.Resource;

public class NgaBinary extends AbstractIoFactory implements GriddedElevationModelReaderFactory {

  public static final String FILE_EXTENSION = "dac";

  public NgaBinary() {
    super("NGA Binary Geoid Height");
    addMediaTypeAndFileExtension("image/x-us-nga-dac", FILE_EXTENSION);
  }

  @Override
  public GriddedElevationModelReader newGriddedElevationModelReader(final Resource resource,
    final MapEx properties) {
    return new NgaBinaryGeoidModelReader(resource, properties);
  }
}
