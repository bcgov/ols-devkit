package com.revolsys.raster;

import java.awt.image.BufferedImage;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.util.AbstractMapTile;

public abstract class GeoreferencedImageMapTile extends AbstractMapTile<GeoreferencedImage> {

  public GeoreferencedImageMapTile(final BoundingBox boundingBox, final int width,
    final int height) {
    super(boundingBox, width, height);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof GeoreferencedImageMapTile) {
      return super.equals(obj);
    }
    return false;
  }

  protected abstract BufferedImage loadBuffferedImage();

  @Override
  protected GeoreferencedImage loadDataDo() {
    final BufferedImage bufferedImage = loadBuffferedImage();
    if (bufferedImage == null) {
      return null;
    } else {
      final BoundingBox boundingBox = getBoundingBox();
      final BufferedGeoreferencedImage image = new BufferedGeoreferencedImage(boundingBox,
        bufferedImage);
      image.addTiePointsForBoundingBox();
      return image;
    }
  }

}
