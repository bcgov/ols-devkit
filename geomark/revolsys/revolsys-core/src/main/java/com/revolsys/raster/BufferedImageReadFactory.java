package com.revolsys.raster;

import java.awt.image.BufferedImage;

import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface BufferedImageReadFactory extends ReadIoFactory {

  @Override
  default boolean isReadFromZipFileSupported() {
    return true;
  }

  BufferedImage readBufferedImage(Resource resource);
}
