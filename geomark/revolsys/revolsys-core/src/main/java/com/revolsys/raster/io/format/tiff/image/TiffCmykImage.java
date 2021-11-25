package com.revolsys.raster.io.format.tiff.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffBaselineTag;
import com.revolsys.raster.io.format.tiff.compression.TiffDecompressor;

public class TiffCmykImage extends AbstractTiffImage {

  private final int[] bitsPerSample;

  public TiffCmykImage(final TiffDirectory directory) {
    super(directory);
    this.bitsPerSample = directory.getIntArray(TiffBaselineTag.BitsPerSample);
    if (this.bitsPerSample.length < 4) {
      throw new IllegalStateException(
        "BitsPerSample.length < 4: " + Arrays.toString(this.bitsPerSample));
    }

    final int expectedBitsPerSample = this.bitsPerSample[0];
    for (final int bitCount : this.bitsPerSample) {
      if (bitCount != expectedBitsPerSample) {
        throw new IllegalStateException(
          "BitsPerSample must all have the same value " + Arrays.toString(this.bitsPerSample));
      }
    }
    if (expectedBitsPerSample % 2 == 1) {
      throw new IllegalStateException(
        "BitsPerSample=" + Arrays.toString(this.bitsPerSample) + " must be a multiple of 2");
    }
  }

  private int cmykToRgb(final int cyan, final int magenta, final int yellow, final int black) {
    final double blackInvert = 255 - black;
    final int red = (int)((1 - cyan / 255.0) * blackInvert);
    final int green = (int)((1 - magenta / 255.0) * blackInvert);
    final int blue = (int)((1 - yellow / 255.0) * blackInvert);
    final int color = 0xff000000 | red << 16 | green << 8 | blue;
    return color;
  }

  @Override
  protected BufferedImage newBufferedImage(final int imageWidth, final int imageHeight) {
    return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
  }

  @Override
  protected void readImagePart(final DataReader in, final BufferedImage bufferedImage,
    final long[] offsets, final long[] counts, final int partIndex, final int imageX,
    final int imageY, final int dataWidth, final int height, final int width) {
    if (this.planarConfiguration == 2) {
      readImagePartPlanar(in, bufferedImage, partIndex, offsets, counts, imageX, imageY, dataWidth,
        height, width);
    } else {
      super.readImagePart(in, bufferedImage, offsets, counts, partIndex, imageX, imageY, dataWidth,
        height, width);
    }
  }

  @Override
  protected void readImagePartDo(final BufferedImage bufferedImage,
    final TiffDecompressor decompressor, final int imageX, final int imageY, final int dataWidth,
    final int dataHeight, final int cropWidth) {
    final ReadSampleInt sampleReader = newSampleReader(decompressor, this.bitsPerSample[0]);
    try {
      int y = imageY;
      for (int yIndex = 0; yIndex < dataHeight; yIndex++) {
        int x = imageX;
        for (int xIndex = 0; xIndex < dataWidth; xIndex++) {
          final int cyan = sampleReader.getValue();
          final int magenta = sampleReader.getValue();
          final int yellow = sampleReader.getValue();
          final int black = sampleReader.getValue();
          if (xIndex < cropWidth) {
            final int color = cmykToRgb(cyan, magenta, yellow, black);
            bufferedImage.setRGB(x, y, color);
          }
          x++;
        }
        y++;
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private void readImagePartPlanar(final DataReader in, final BufferedImage bufferedImage,
    final int partIndex, final long[] offsets, final long[] counts, final int imageX,
    final int imageY, final int dataWidth, final int dataHeight, final int cropWidth) {
    try (
      final TiffDecompressor decompressorCyan = newPlanarDecompressor(in, offsets, counts,
        partIndex, 0);
      final TiffDecompressor decompressorMagenta = newPlanarDecompressor(in, offsets, counts,
        partIndex, 1);
      final TiffDecompressor decompressorYellow = newPlanarDecompressor(in, offsets, counts,
        partIndex, 2);
      final TiffDecompressor decompressorBlack = newPlanarDecompressor(in, offsets, counts,
        partIndex, 3);) {
      final ReadSampleInt sampleReaderCyan = newSampleReader(decompressorCyan,
        this.bitsPerSample[0]);
      final ReadSampleInt sampleReaderMagenta = newSampleReader(decompressorMagenta,
        this.bitsPerSample[0]);
      final ReadSampleInt sampleReaderYellow = newSampleReader(decompressorYellow,
        this.bitsPerSample[0]);
      final ReadSampleInt sampleReaderBlack = newSampleReader(decompressorBlack,
        this.bitsPerSample[0]);

      int y = imageY;
      for (int yIndex = 0; yIndex < dataHeight; yIndex++) {
        int x = imageX;
        for (int xIndex = 0; xIndex < dataWidth; xIndex++) {
          final int cyan = sampleReaderCyan.getValue();
          final int magenta = sampleReaderMagenta.getValue();
          final int yellow = sampleReaderYellow.getValue();
          final int black = sampleReaderBlack.getValue();
          if (xIndex < cropWidth) {
            final int color = cmykToRgb(cyan, magenta, yellow, black);
            bufferedImage.setRGB(x, y, color);
          }
          x++;
        }
        decompressorCyan.endRow();
        decompressorMagenta.endRow();
        decompressorYellow.endRow();
        decompressorBlack.endRow();
        y++;
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
