package com.revolsys.raster.io.format.tiff.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.MultipleInputStream;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffExtensionTag;
import com.revolsys.raster.io.format.tiff.compression.TiffDecompressor;

public class TiffJpegImage extends AbstractTiffImage {

  protected static final int EOI = 0xD9;

  protected static final int SOI = 0xD8;

  private final byte[] jpegTables;

  private ImageReader JPEGReader;

  private ImageReadParam JPEGParam;

  private Reference<BufferedImage> partImageReference = new WeakReference<>(null);

  private int jpegTablesLength;

  public TiffJpegImage(final TiffDirectory directory) {
    super(directory);
    final Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("jpeg");
    if (iter.hasNext()) {
      this.JPEGReader = iter.next();
      this.JPEGParam = this.JPEGReader.getDefaultReadParam();
    } else {
      throw new IllegalStateException("Cannot find a jpeg reader");
    }
    this.jpegTables = directory.getByteArray(TiffExtensionTag.JPEGTables, null);
    if (this.jpegTables != null) {
      int dataOffset = this.jpegTables.length;
      for (int i = this.jpegTables.length - 2; i > 0; i--) {
        if ((this.jpegTables[i] & 0xff) == 0xff && (this.jpegTables[i + 1] & 0xff) == EOI) {
          dataOffset = i;
          break;
        }
      }
      this.jpegTablesLength = dataOffset;
    }
  }

  private synchronized BufferedImage getPartImage() {
    BufferedImage partImage = this.partImageReference.get();
    if (partImage == null) {
      final int tileWidth = getTileWidth();
      if (tileWidth > 0) {
        final int tileHeight = getTileHeight();
        partImage = newBufferedImage(tileWidth, tileHeight);
      } else {
        final int imageWidth = getImageWidth();
        final int rowsPerStrip = getRowsPerStrip();
        partImage = newBufferedImage(imageWidth, rowsPerStrip);
      }
      this.partImageReference = new WeakReference<>(partImage);
    }
    return partImage;
  }

  @Override
  protected BufferedImage newBufferedImage(final int imageWidth, final int imageHeight) {
    return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
  }

  @Override
  protected void readImagePartDo(final BufferedImage bufferedImage,
    final TiffDecompressor decompressor, final int imageX, final int imageY, final int dataWidth,
    final int dataHeight, final int cropWidth) {
    try {
      final InputStream partIn = decompressor.getInputStream();
      ImageInputStream is;
      if (this.jpegTables == null) {
        is = new MemoryCacheImageInputStream(partIn);
      } else {
        final ByteArrayInputStream tableIn = new ByteArrayInputStream(this.jpegTables, 0,
          this.jpegTablesLength);
        partIn.mark(2);
        if (!(partIn.read() == 0xff && partIn.read() == SOI)) {
          partIn.reset();
        }

        final InputStream mergedIn = new MultipleInputStream(tableIn, partIn);
        is = new MemoryCacheImageInputStream(mergedIn);
      }

      this.JPEGReader.setInput(is, false, true);
      final BufferedImage partImage = getPartImage();
      this.JPEGParam.setDestination(partImage);

      this.JPEGReader.read(0, this.JPEGParam);
      int y = imageY;
      for (int yIndex = 0; yIndex < dataHeight; yIndex++) {
        int x = imageX;
        for (int xIndex = 0; xIndex < dataWidth; xIndex++) {
          if (xIndex < cropWidth) {
            final int color = partImage.getRGB(xIndex, yIndex);
            bufferedImage.setRGB(x, y, color);
            x++;
          }
        }
        y++;
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
