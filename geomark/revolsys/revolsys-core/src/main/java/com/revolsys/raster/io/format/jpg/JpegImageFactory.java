package com.revolsys.raster.io.format.jpg;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.raster.GeoreferencedImageWriter;
import com.revolsys.raster.GeoreferencedImageWriterFactory;
import com.revolsys.raster.imagio.ImageIoGeoreferencedImage;
import com.revolsys.raster.imagio.ImageIoGeoreferencedImageWriter;
import com.revolsys.spring.resource.Resource;

public class JpegImageFactory extends AbstractIoFactory
  implements GeoreferencedImageReadFactory, GeoreferencedImageWriterFactory {

  public static final String FILE_EXTENSION_WORLD_FILE = "jgw";

  public JpegImageFactory() {
    super("JPEG");
    addMediaTypeAndFileExtension("image/jpeg", "jpg");
    addMediaTypeAndFileExtension("image/jpeg", "jpeg");
  }

  @Override
  public GeoreferencedImageWriter newGeoreferencedImageWriter(final Resource resource) {
    return new ImageIoGeoreferencedImageWriter(resource, "JPEG",
      JpegImageFactory.FILE_EXTENSION_WORLD_FILE);
  }

  @Override
  public GeoreferencedImage readGeoreferencedImage(final Resource resource) {
    return new ImageIoGeoreferencedImage(resource, FILE_EXTENSION_WORLD_FILE);
  }

}
