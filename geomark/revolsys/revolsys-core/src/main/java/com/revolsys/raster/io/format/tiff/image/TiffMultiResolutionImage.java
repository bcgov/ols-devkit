package com.revolsys.raster.io.format.tiff.image;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.AbstractGeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageMapTile;
import com.revolsys.raster.TiledGeoreferencedImage;

public class TiffMultiResolutionImage extends AbstractGeoreferencedImage
  implements TiledGeoreferencedImage {

  private List<TiffImage> images = new ArrayList<>();

  private final TiffImage firstImage;

  public TiffMultiResolutionImage(final List<TiffImage> images) {
    this.images = images;
    // Lower resolution first
    images.sort((image1, image2) -> {
      final double resolution1 = image1.getResolutionX();
      final double resolution2 = image2.getResolutionX();
      return -Double.compare(resolution1, resolution2);
    });
    final TiffImage firstImage = images.get(0);
    this.firstImage = firstImage;
    setImageWidth(firstImage.getImageWidth());
    setImageHeight(firstImage.getImageHeight());
    setResolutionX(firstImage.getResolutionX());
    setResolutionY(firstImage.getResolutionY());
    setBoundingBox(firstImage.getBoundingBox());
  }

  @Override
  public BufferedImage getBufferedImage() {
    return this.firstImage.getBufferedImage();
  }

  public TiffImage getImage(final BoundingBox boundingBox, final double resolution) {
    TiffImage previousImage = this.images.get(0);
    for (final TiffImage image : this.images) {
      final double imagelResolution = image.getResolutionX();
      if (resolution > imagelResolution) {
        if (image == previousImage) {
          return image;
        } else {
          final double ratio = imagelResolution / resolution;
          if (ratio < 0.95) {
            return previousImage;
          } else {
            return image;
          }
        }
      }
      previousImage = image;
    }
    return previousImage;
  }

  @Override
  public List<GeoreferencedImageMapTile> getOverlappingMapTiles(final BoundingBox boundingBox,
    final double resolution) {
    final TiffImage image = getImage(boundingBox, resolution);
    return image.getOverlappingMapTiles(boundingBox);
  }

  @Override
  public RenderedImage getRenderedImage() {
    return this.firstImage.getRenderedImage();
  }

  @Override
  public double getResolution(final BoundingBox boundingBox, final double resolution) {
    final TiffImage image = getImage(boundingBox, resolution);
    return image.getResolutionX();
  }
}
