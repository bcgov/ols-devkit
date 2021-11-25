package com.revolsys.raster.io.format.tiff.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffBaselineTag;
import com.revolsys.raster.io.format.tiff.code.TiffExtensionTag;
import com.revolsys.raster.io.format.tiff.compression.TiffDecompressor;

public class TiffYCbCrImage extends AbstractTiffImage {

  private final double[] yCbCrCoefficients;

  private final int[] yCbCrSampling;

  public TiffYCbCrImage(final TiffDirectory directory) {
    super(directory);
    final int[] bitsPerSample = directory.getIntArray(TiffBaselineTag.BitsPerSample);
    if (bitsPerSample.length < 3 && bitsPerSample[0] != 8 && bitsPerSample[1] != 8
      && bitsPerSample[2] != 8) {
      throw new IllegalArgumentException("BitsPerSample != {8,8,8}");
    }
    this.yCbCrCoefficients = directory.getDoubleArray(TiffExtensionTag.YCbCrCoefficients,
      new double[] {
        0.299, 0.587, 0.114
      });
    this.yCbCrSampling = directory.getIntArray(TiffExtensionTag.YCbCrSubSampling, new int[] {
      2, 2
    });
  }

  @Override
  protected BufferedImage newBufferedImage(final int imageWidth, final int imageHeight) {
    return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
  }

  @Override
  protected void readImagePartDo(final BufferedImage bufferedImage,
    final TiffDecompressor decompressor, final int imageX, final int imageY, final int dataWidth,
    final int dataHeight, final int cropWidth) {
    try {
      final double lumaRed = this.yCbCrCoefficients[0];
      final double lumaGreen = this.yCbCrCoefficients[1];
      final double lumaBlue = this.yCbCrCoefficients[2];
      final double redFactor = 2 - 2 * lumaRed;
      final double blueFactor = 2 - 2 * lumaBlue;

      final int stepX = this.yCbCrSampling[0];
      final int stepY = this.yCbCrSampling[1];

      final int[] ySamples = new int[stepX * stepY];

      for (int yIndex = 0; yIndex < dataHeight; yIndex += stepY) {
        for (int xIndex = 0; xIndex < dataWidth; xIndex += stepX) {
          for (int i = 0; i < ySamples.length; i++) {
            ySamples[i] = decompressor.getByte();
          }
          final int Cb = decompressor.getByte();
          final int Cr = decompressor.getByte();

          int ySampleIndex = 0;
          for (int yOffset = 0; yOffset < stepY; yOffset++) {
            for (int xOffset = 0; xOffset < stepX; xOffset++) {
              final int Y = ySamples[ySampleIndex++];
              final int x = imageX + xIndex + xOffset;
              final int y = imageY + yIndex + yOffset;
              if (x < cropWidth && yIndex + yOffset < dataHeight) {
                final int alpha = 0xff000000;
                int red = (int)((Cr - 128) * redFactor + Y);
                if (red < 0) {
                  red = 0;
                } else if (red > 255) {
                  red = 255;
                }

                int blue = (int)((Cb - 128) * blueFactor + Y);
                if (blue < 0) {
                  blue = 0;
                } else if (blue > 255) {
                  blue = 255;
                }

                int green = (int)((Y - lumaBlue * blue - lumaRed * red) / lumaGreen);
                if (green < 0) {
                  green = 0;
                } else if (green > 255) {
                  green = 255;
                }

                final int color = alpha | red << 16 | green << 8 | blue;
                bufferedImage.setRGB(x, y, color);
              }
            }
          }
        }
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
