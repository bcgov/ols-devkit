package com.revolsys.raster.commonsimaging;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.imaging.ImageParser;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.common.bytesource.ByteSource;
import org.apache.commons.imaging.common.bytesource.ByteSourceFile;
import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.BufferedImageReadFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.spring.resource.Resource;

public class CommonsImagingImageReadFactory extends AbstractIoFactory
  implements GeoreferencedImageReadFactory, BufferedImageReadFactory {

  private final ImageParser imageParser;

  public final String worldFileExtension;

  public CommonsImagingImageReadFactory(final ImageParser imageParser) {
    this(imageParser, imageParser.getName(), null, null);
  }

  public CommonsImagingImageReadFactory(final ImageParser imageParser, final String name,
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
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public BufferedImage readBufferedImage(final Resource resource) {
    try {
      ByteSource byteSource;
      if (resource.isFile()) {
        final File file = resource.getFile();
        byteSource = new ByteSourceFile(file);
      } else {
        final InputStream in = resource.getInputStream();
        final String filename = resource.getFilename();
        byteSource = new ByteSourceInputStream(in, filename);
      }
      return this.imageParser.getBufferedImage(byteSource, MapEx.EMPTY);
    } catch (final ImageReadException | IOException e) {
      throw Exceptions.wrap("Unable to open: " + resource, e);
    }
  }

  @Override
  public GeoreferencedImage readGeoreferencedImage(final Resource resource) {
    return new CommonsImagingGeoreferencedImage(this, resource, this.worldFileExtension);
  }

}
