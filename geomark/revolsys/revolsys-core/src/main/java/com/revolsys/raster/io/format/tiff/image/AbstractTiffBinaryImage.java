package com.revolsys.raster.io.format.tiff.image;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.IOException;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.compression.TiffDecompressor;

public class AbstractTiffBinaryImage extends AbstractTiffImage {

  private final boolean whiteZero;

  public AbstractTiffBinaryImage(final TiffDirectory directory, final boolean whiteZero) {
    super(directory);
    this.whiteZero = whiteZero;
  }

  @Override
  protected BufferedImage newBufferedImage(final int imageWidth, final int imageHeight) {
    return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_BINARY);
  }

  @Override
  protected void readImagePartDo(final BufferedImage bufferedImage,
    final TiffDecompressor decompressor, final int imageX, final int imageY, final int dataWidth,
    final int dataHeight, final int cropWidth) {
    final WritableRaster raster = bufferedImage.getRaster();
    final byte[] data = new byte[1];
    int currentByte = -1;
    int bitMask = 0;
    try {
      int y = imageY;
      for (int yIndex = 0; yIndex < dataHeight; yIndex++) {
        int x = imageX;
        for (int xIndex = 0; xIndex < dataWidth; xIndex++) {
          if (bitMask == 0) {
            currentByte = decompressor.getByte();
            if (currentByte == -1) {
              throw new EOFException();
            }
            bitMask = 0b10000000;
          }
          if (xIndex < cropWidth) {
            final boolean flag = (currentByte & bitMask) != 0;
            if (flag == this.whiteZero) {
              data[0] = 0;
            } else {
              data[0] = 1;
            }
            raster.setDataElements(x, y, data);
            x++;
          }
          bitMask >>= 1;
        }
        bitMask = 0;
        y++;
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
