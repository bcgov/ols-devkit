package com.revolsys.raster;

import java.io.PrintWriter;

import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Doubles;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;
import com.revolsys.spring.resource.Resource;

public interface GeoreferencedImageWriter extends Writer<GeoreferencedImage> {
  static boolean isWritable(final Object target) {
    return IoFactory.isAvailable(GeoreferencedImageWriterFactory.class, target);
  }

  static GeoreferencedImageWriter newGeoreferencedImageWriter(final Object target,
    final MapEx properties) {
    final GeoreferencedImageWriterFactory factory = IoFactory
      .factory(GeoreferencedImageWriterFactory.class, target);
    if (factory == null) {
      return null;
    } else {
      final Resource resource = Resource.getResource(target);
      final GeoreferencedImageWriter writer = factory.newGeoreferencedImageWriter(resource);
      writer.setProperties(properties);
      return writer;
    }
  }

  private static void println(final PrintWriter writer, final double number) {
    final String string = Doubles.toString(number);
    writer.println(string);
  }

  public static void writeWorldFile(final Resource resource, final String worldFileExtension,
    final GeoreferencedImage image) {
    final Resource worldFile = resource.newResourceChangeExtension(worldFileExtension);
    try {
      try (
        final PrintWriter writer = new PrintWriter(worldFile.newWriter())) {
        final BoundingBox boundingBox = image.getBoundingBox();
        final double minX = boundingBox.getMinX();
        final double maxY = boundingBox.getMaxY();
        final double resolutionX = image.getResolutionX();
        final double resolutionY = image.getResolutionY();

        println(writer, resolutionX);
        println(writer, 0);
        println(writer, 0);
        println(writer, -resolutionY);
        println(writer, minX);
        println(writer, maxY);
      }
    } catch (final Throwable e) {
      Logs.debug(GeoreferencedImageWriter.class, "Error reading world file " + worldFile, e);
    }
  }
}
