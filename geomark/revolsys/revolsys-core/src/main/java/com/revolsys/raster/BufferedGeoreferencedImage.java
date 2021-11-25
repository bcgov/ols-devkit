package com.revolsys.raster;

import java.awt.image.BufferedImage;

import com.revolsys.geometry.model.BoundingBox;

public class BufferedGeoreferencedImage extends AbstractGeoreferencedImage {

  public static BufferedGeoreferencedImage newImage(final BoundingBox boundingBox, final int width,
    final int height) {
    return newImage(boundingBox, width, height, BufferedImage.TYPE_INT_ARGB);
  }

  public static BufferedGeoreferencedImage newImage(final BoundingBox boundingBox, final int width,
    final int height, final int imageType) {
    final BufferedImage image = new BufferedImage(width, height, imageType);
    return new BufferedGeoreferencedImage(boundingBox, image);
  }

  protected BufferedGeoreferencedImage() {

  }

  public BufferedGeoreferencedImage(final BoundingBox boundingBox, final BufferedImage image) {
    this(boundingBox, image.getWidth(), image.getHeight());
    setRenderedImage(image);
    postConstruct();
  }

  public BufferedGeoreferencedImage(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight) {
    setBoundingBox(boundingBox);
    setImageWidth(imageWidth);
    setImageHeight(imageHeight);
    postConstruct();
  }

  @Override
  public String toString() {
    return "BufferedImage " + getImageWidth() + "x" + getImageHeight();
  }

}
