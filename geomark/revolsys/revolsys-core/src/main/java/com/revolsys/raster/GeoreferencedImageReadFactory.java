package com.revolsys.raster;

import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface GeoreferencedImageReadFactory extends ReadIoFactory {
  @Override
  default boolean isReadFromZipFileSupported() {
    return true;
  }

  GeoreferencedImage readGeoreferencedImage(Resource resource);
}
