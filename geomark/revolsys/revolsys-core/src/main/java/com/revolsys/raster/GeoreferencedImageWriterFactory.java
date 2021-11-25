package com.revolsys.raster;

import com.revolsys.io.FileIoFactory;
import com.revolsys.spring.resource.Resource;

public interface GeoreferencedImageWriterFactory extends FileIoFactory {

  GeoreferencedImageWriter newGeoreferencedImageWriter(Resource resource);
}
