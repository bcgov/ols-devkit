package com.revolsys.raster;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.IoFactory;
import com.revolsys.spring.resource.Resource;

public class BufferedImages {
  public static BufferedImage readBufferedImage(final Object source) {
    final Resource resource = Resource.getResource(source);
    return readBufferedImage(resource);
  }

  public static BufferedImage readBufferedImage(final Resource resource) {
    final BufferedImageReadFactory factory = IoFactory.factory(BufferedImageReadFactory.class,
      resource);
    if (factory == null) {
      return null;
    } else {
      return factory.readBufferedImage(resource);
    }
  }

  public static BufferedImage readBufferedImage(final String url) {
    final Resource resource = Resource.getResource(url);
    return readBufferedImage(resource);
  }

  public static BufferedImage readImageIo(final InputStream in) {
    try {
      return ImageIO.read(in);
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read image", e);
    }
  }

  public static BufferedImage readImageIo(final Resource resource) {
    final BufferedImage bufferedImage;
    try {
      if (resource.isFile()) {
        bufferedImage = ImageIO.read(resource.getFile());
      } else {
        try (
          InputStream in = resource.getInputStream()) {
          bufferedImage = readImageIo(in);
        }
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to open: " + resource, e);
    }
    return bufferedImage;
  }

  public static BufferedImage readImageIo(final String url) {
    final Resource resource = Resource.getResource(url);
    return readImageIo(resource);
  }

  public static BufferedImage readImageIo(final URL url) {
    try {
      return ImageIO.read(url);
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read image: " + url, e);
    }
  }

}
