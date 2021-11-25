package com.revolsys.raster.commonsimaging;

import java.awt.image.BufferedImage;

import com.revolsys.raster.AbstractGeoreferencedImage;
import com.revolsys.spring.resource.Resource;

public class CommonsImagingGeoreferencedImage extends AbstractGeoreferencedImage {

  public CommonsImagingGeoreferencedImage(final CommonsImagingImageReadFactory factory,
    final Resource resource, final String worldFileExtension) {
    super(worldFileExtension);
    setImageResource(resource);

    final BufferedImage bufferedImage = factory.readBufferedImage(resource);
    setRenderedImage(bufferedImage);

    loadImageMetaData();
    postConstruct();
  }

  @Override
  public void cancelChanges() {
    if (getImageResource() != null) {
      loadImageMetaData();
      setHasChanges(false);
    }
  }
}
