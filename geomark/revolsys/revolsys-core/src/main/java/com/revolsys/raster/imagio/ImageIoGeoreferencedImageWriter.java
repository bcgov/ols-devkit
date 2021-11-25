package com.revolsys.raster.imagio;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageWriter;
import com.revolsys.spring.resource.Resource;

public class ImageIoGeoreferencedImageWriter implements GeoreferencedImageWriter {

  private final Resource resource;

  private final String formatName;

  private final String worldFileExtension;

  public ImageIoGeoreferencedImageWriter(final Resource resource, final String formatName,
    final String worldFileExtension) {
    this.resource = resource;
    this.formatName = formatName;
    this.worldFileExtension = worldFileExtension;
  }

  private ImageWriter getWriter(final RenderedImage renderedImage) {
    final ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(renderedImage);
    final Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, this.formatName);
    if (iter.hasNext()) {
      return iter.next();
    } else {
      return null;
    }
  }

  private ImageOutputStream newImageOutputStream() throws IOException {
    Object target;
    final Resource resource = this.resource;
    if (resource.isFile()) {
      target = resource.getFile();
    } else {
      target = resource.newBufferedOutputStream();
    }
    return ImageIO.createImageOutputStream(target);
  }

  @Override
  public void write(final GeoreferencedImage image) {
    final Resource resource = this.resource;
    RenderedImage renderedImage = image.getRenderedImage();
    image.writePrjFile(resource);
    GeoreferencedImageWriter.writeWorldFile(this.resource, this.worldFileExtension, image);

    ImageWriter writer = getWriter(renderedImage);
    if (writer == null) {
      if (renderedImage.getColorModel().hasAlpha()) {
        final int width = renderedImage.getWidth();
        final int height = renderedImage.getHeight();
        final BufferedImage noAlphaImage = new BufferedImage(width, height,
          BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics = (Graphics2D)noAlphaImage.getGraphics();
        graphics.setColor(WebColors.White);
        graphics.fillRect(0, 0, width, height);
        graphics.drawRenderedImage(renderedImage, null);
        renderedImage = noAlphaImage;
        writer = getWriter(renderedImage);
      }
      if (writer == null) {
        throw new IllegalArgumentException("Cannot find writer for: " + resource);
      }
    }
    write(writer, renderedImage);
  }

  private void write(final ImageWriter writer, final RenderedImage renderedImage) {
    try (
      ImageOutputStream imageOut = newImageOutputStream()) {
      writer.setOutput(imageOut);
      try {
        writer.write(renderedImage);
      } finally {
        writer.dispose();
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to write: " + this.resource, e);
    } finally {
      writer.dispose();
    }
  }
}
