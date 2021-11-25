package com.revolsys.raster.io.format.pdf;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.raster.GeoreferencedImageWriter;
import com.revolsys.raster.GeoreferencedImageWriterFactory;
import com.revolsys.spring.resource.Resource;

public class PdfImageFactory extends AbstractIoFactory
  implements GeoreferencedImageReadFactory, GeoreferencedImageWriterFactory {

  public PdfImageFactory() {
    super("PDF");
    addMediaTypeAndFileExtension("application/pdf", "pdf");
  }

  @Override
  public GeoreferencedImageWriter newGeoreferencedImageWriter(final Resource resource) {
    return new PdfImageWriter(resource);
  }

  @Override
  public GeoreferencedImage readGeoreferencedImage(final Resource resource) {
    return new PdfImage(resource);
  }
}
