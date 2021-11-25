package com.revolsys.raster.commonsimaging;

import org.apache.commons.imaging.ImageParser;
import org.apache.commons.imaging.formats.bmp.BmpImageParser;
import org.apache.commons.imaging.formats.gif.GifImageParser;
import org.apache.commons.imaging.formats.pcx.PcxImageParser;
import org.apache.commons.imaging.formats.png.PngImageParser;

import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;

public class CommonsImagingServiceInitializer {

  private static void addFactories(final String name, final ImageParser imageParser,
    final String mimeType) {
    final String fileExtension = imageParser.getDefaultExtension().substring(1);
    final String worldFileExtension = fileExtension.charAt(0) + ""
      + fileExtension.charAt(fileExtension.length() - 1) + "w";

    final IoFactory readFactory = new CommonsImagingImageReadFactory(imageParser, name, mimeType,
      worldFileExtension);
    IoFactoryRegistry.addFactory(readFactory);

    final IoFactory writeFactory = new CommonsImagingImageWriterFactory(imageParser, name, mimeType,
      worldFileExtension);
    IoFactoryRegistry.addFactory(writeFactory);
  }

  public static void serviceInit() {
    addFactories("Windows bitmap", new BmpImageParser(), "image/bmp");
    addFactories("Graphics Interchange Format", new GifImageParser(), "image/gif");
    addFactories("Personal Computer eXchange", new PcxImageParser(), "image/vnd.zbrush.pcx");
    addFactories("Portable Network Graphics", new PngImageParser(), "image/png");
  }

}
