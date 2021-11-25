package com.revolsys.raster.commonsimaging;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.imaging.ImageParser;
import org.apache.commons.imaging.ImageWriteException;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.AbstractWriter;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageWriter;
import com.revolsys.spring.resource.Resource;

public class CommonsImagingGeoreferencedImageWriter extends AbstractWriter<GeoreferencedImage>
  implements GeoreferencedImageWriter {

  private final Resource resource;

  private final ImageParser imageParser;

  private final String worldFileExtension;

  public CommonsImagingGeoreferencedImageWriter(final Resource resource,
    final ImageParser imageParser, final String worldFileExtension) {
    this.resource = resource;
    this.imageParser = imageParser;
    this.worldFileExtension = worldFileExtension;
  }

  @Override
  public void write(final GeoreferencedImage image) {
    final BufferedImage bufferedImage = image.getBufferedImage();
    if (bufferedImage != null) {
      final MapEx params = getProperties();

      final Resource resource = this.resource;
      try (
        OutputStream out = resource.newBufferedOutputStream()) {
        image.writePrjFile(resource);
        GeoreferencedImageWriter.writeWorldFile(this.resource, this.worldFileExtension, image);
        this.imageParser.writeImage(bufferedImage, out, params);
      } catch (final ImageWriteException | IOException e) {
        throw Exceptions.wrap("Unable to write: " + resource, e);
      }
    }
  }
}
