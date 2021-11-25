package com.revolsys.raster.imagio;

import java.awt.image.BufferedImage;

import com.revolsys.raster.AbstractGeoreferencedImage;
import com.revolsys.raster.BufferedImages;
import com.revolsys.spring.resource.Resource;

public class ImageIoGeoreferencedImage extends AbstractGeoreferencedImage {
  protected ImageIoGeoreferencedImage() {
  }

  public ImageIoGeoreferencedImage(final Resource imageResource, final String worldFileExtension) {
    super(worldFileExtension);
    setImageResource(imageResource);

    final BufferedImage bufferedImage = BufferedImages.readImageIo(imageResource);
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
