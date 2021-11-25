package com.revolsys.raster.commonsimaging;

import org.apache.commons.imaging.ImageParser;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImageWriter;
import com.revolsys.raster.GeoreferencedImageWriterFactory;
import com.revolsys.spring.resource.Resource;

public class CommonsImagingImageWriterFactory extends AbstractIoFactory
  implements GeoreferencedImageWriterFactory {

  private final ImageParser imageParser;

  public final String worldFileExtension;

  public CommonsImagingImageWriterFactory(final ImageParser imageParser) {
    this(imageParser, imageParser.getName(), null, null);
  }

  public CommonsImagingImageWriterFactory(final ImageParser imageParser, final String name,
    final String mimeType, final String worldFileExtension) {
    super(name);
    this.imageParser = imageParser;
    this.worldFileExtension = worldFileExtension;
    final String defaultExtension = imageParser.getDefaultExtension().substring(1);
    if (mimeType == null) {
      addFileExtension(defaultExtension);
    } else {
      addMediaTypeAndFileExtension(mimeType, defaultExtension);
    }
  }

  @Override
  public GeoreferencedImageWriter newGeoreferencedImageWriter(final Resource resource) {
    return new CommonsImagingGeoreferencedImageWriter(resource, this.imageParser,
      this.worldFileExtension);
  }
}
