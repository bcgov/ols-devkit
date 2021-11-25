package com.revolsys.raster.io.format.tiff.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Integers;

import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.TiffDirectoryBuilder;
import com.revolsys.raster.io.format.tiff.code.TiffBaselineTag;
import com.revolsys.raster.io.format.tiff.code.TiffExtensionTag;
import com.revolsys.raster.io.format.tiff.code.TiffPhotogrametricInterpretation;
import com.revolsys.raster.io.format.tiff.compression.TiffDecompressor;

public class TiffRgbFullColorImage extends AbstractTiffImage {
  public static Consumer<TiffDirectoryBuilder> initBits(final int bitsPerSample) {
    return directory -> {
      directory //
        .setUnsignedShort(TiffBaselineTag.PhotometricInterpretation,
          TiffPhotogrametricInterpretation.RGB.getId()) //
        .setBitsPerSample(bitsPerSample, bitsPerSample, bitsPerSample)//
      ;
    };
  }

  private int alphaByteIndex = -1;

  final int[] bitsPerSamples;

  private final int[] extraSamples;

  private final int[] sampleFormats;

  private final int samplesPerPixel;

  private int sampleFormat;

  private final int bitCountPerSample;

  public TiffRgbFullColorImage(final TiffDirectory directory) {
    super(directory);
    this.samplesPerPixel = directory.getInt(TiffBaselineTag.SamplesPerPixel, 3);
    this.bitsPerSamples = directory.getIntArray(TiffBaselineTag.BitsPerSample);
    if (this.bitsPerSamples.length < 3) {
      throw new IllegalStateException(
        "BitsPerSample.length < 3: " + Arrays.toString(this.bitsPerSamples));
    }
    this.bitCountPerSample = this.bitsPerSamples[0];
    for (final int bitCount : this.bitsPerSamples) {
      if (bitCount != this.bitCountPerSample) {
        throw new IllegalStateException(
          "BitsPerSample must all have the same value " + Arrays.toString(this.bitsPerSamples));
      }
    }
    if (this.bitCountPerSample % 2 == 1) {
      throw new IllegalStateException(
        "BitsPerSample=" + Arrays.toString(this.bitsPerSamples) + " must be a multiple of 2");
    }
    this.sampleFormats = directory.getIntArray(TiffExtensionTag.SampleFormat, Integers.EMPTY_ARRAY);
    if (this.sampleFormats.length > 0) {
      this.sampleFormat = this.sampleFormats[0];
      for (final int sampleFormat : this.sampleFormats) {
        if (sampleFormat != this.sampleFormat) {
          throw new IllegalStateException("SampleFormat=" + Arrays.toString(this.sampleFormats)
            + " must all have the same value ");
        }
      }
    }
    this.extraSamples = directory.getIntArray(TiffBaselineTag.ExtraSamples, Integers.EMPTY_ARRAY);
    for (int extraIndex = 0; extraIndex < this.extraSamples.length; extraIndex++) {
      if (this.extraSamples[extraIndex] == 2) {
        this.alphaByteIndex = 3 + extraIndex;
      }
    }
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
    final ReadSampleInt sampleReader = newSampleReader(decompressor, this.bitCountPerSample);
    try {
      int y = imageY;
      for (int yIndex = 0; yIndex < dataHeight; yIndex++) {
        int x = imageX;
        for (int xIndex = 0; xIndex < dataWidth; xIndex++) {
          int alpha = 0xff000000;
          final int red = sampleReader.getValue();
          final int green = sampleReader.getValue();
          final int blue = sampleReader.getValue();
          for (int extraIndex = 3; extraIndex < this.samplesPerPixel; extraIndex++) {
            final int extra = sampleReader.getValue();
            if (extraIndex == this.alphaByteIndex) {
              alpha = extra << 24;
            }
          }
          if (xIndex < cropWidth) {
            final int color = alpha | red << 16 | green << 8 | blue;
            bufferedImage.setRGB(x, y, color);
          }
          x++;
        }
        decompressor.endRow();
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
      final TiffDecompressor decompressorRed = newPlanarDecompressor(in, offsets, counts, partIndex,
        0);
      final TiffDecompressor decompressorGreen = newPlanarDecompressor(in, offsets, counts,
        partIndex, 1);
      final TiffDecompressor decompressorBlue = newPlanarDecompressor(in, offsets, counts,
        partIndex, 2);
      final TiffDecompressor decompressorAlpha = newPlanarDecompressor(in, offsets, counts,
        partIndex, this.alphaByteIndex);) {
      final ReadSampleInt sampleReaderRed = newSampleReader(decompressorRed,
        this.bitCountPerSample);
      final ReadSampleInt sampleReaderGreen = newSampleReader(decompressorGreen,
        this.bitCountPerSample);
      final ReadSampleInt sampleReaderBlue = newSampleReader(decompressorBlue,
        this.bitCountPerSample);
      final ReadSampleInt sampleReaderAlpha = newSampleReader(decompressorAlpha,
        this.bitCountPerSample, 0xff);

      int y = imageY;
      for (int yIndex = 0; yIndex < dataHeight; yIndex++) {
        int x = imageX;
        for (int xIndex = 0; xIndex < dataWidth; xIndex++) {
          final int red = sampleReaderRed.getValue();
          final int green = sampleReaderGreen.getValue();
          final int blue = sampleReaderBlue.getValue();
          final int alpha = sampleReaderAlpha.getValue();
          if (xIndex < cropWidth) {
            final int color = alpha << 24 | red << 16 | green << 8 | blue;
            bufferedImage.setRGB(x, y, color);
          }
          x++;
        }
        decompressorRed.endRow();
        decompressorGreen.endRow();
        decompressorBlue.endRow();
        if (decompressorAlpha != null) {
          decompressorAlpha.endRow();
        }
        y++;
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

}
