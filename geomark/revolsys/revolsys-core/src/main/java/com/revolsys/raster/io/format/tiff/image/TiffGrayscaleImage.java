package com.revolsys.raster.io.format.tiff.image;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffBaselineTag;
import com.revolsys.raster.io.format.tiff.code.TiffPhotogrametricInterpretation;
import com.revolsys.raster.io.format.tiff.compression.TiffDecompressor;

public class TiffGrayscaleImage extends AbstractTiffImage {

  private static final ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);

  private final int bitsPerSample;

  private final boolean invert;

  private final IntHashMap<Integer> colorMap = new IntHashMap<>();

  private final float[] percents = new float[3];

  private ComponentColorModel colorModel;

  private final long max;

  private final int colorMax;

  public TiffGrayscaleImage(final TiffDirectory directory) {
    super(directory);
    this.bitsPerSample = directory.getInt(TiffBaselineTag.BitsPerSample);
    this.invert = directory
      .getPhotogrametricInterpretation() == TiffPhotogrametricInterpretation.MIN_IS_WHITE;
    if (this.bitsPerSample % 2 == 1) {
      throw new IllegalStateException(
        "BitsPerSample=" + this.bitsPerSample + " must be a multiple of 2");
    } else if (this.bitsPerSample > 32) {
      throw new IllegalStateException("BitsPerSample=" + this.bitsPerSample + " must be <=32");
    }
    final long sampleCount = 1L << this.bitsPerSample;
    this.max = sampleCount - 1;

    if (this.bitsPerSample <= 8) {
      this.colorModel = new ComponentColorModel(colorSpace, new int[] {
        8
      }, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
      this.colorMax = 1 << 8;
    } else {
      this.colorModel = new ComponentColorModel(colorSpace, new int[] {
        16
      }, false, true, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
      this.colorMax = 1 << 16;
    }

  }

  private int getColor(final int index) {
    Integer color = this.colorMap.get(index);
    if (color == null) {
      synchronized (this.colorMap) {
        final float percent = (float)((double)Integer.toUnsignedLong(index) / this.max);
        this.percents[0] = percent;
        this.percents[1] = percent;
        this.percents[2] = percent;
        final float[] f = colorSpace.fromRGB(this.percents);
        if (this.bitsPerSample <= 8) {
          final byte[] dataElements = (byte[])this.colorModel.getDataElements(f, 0, null);
          color = (int)dataElements[0];
        } else {
          final short[] dataElements = (short[])this.colorModel.getDataElements(f, 0, null);
          color = (int)dataElements[0];
        }
        this.colorMap.put(index, color);
      }
    }
    return color;
  }

  @Override
  protected BufferedImage newBufferedImage(final int imageWidth, final int imageHeight) {
    if (this.bitsPerSample <= 8) {
      return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
    } else {
      return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_USHORT_GRAY);
    }
  }

  @Override
  protected void readImagePartDo(final BufferedImage bufferedImage,
    final TiffDecompressor decompressor, final int imageX, final int imageY, final int dataWidth,
    final int dataHeight, final int cropWidth) {
    ReadSampleInt readSampleInt;
    final int bitsPerSample = this.bitsPerSample;
    if (bitsPerSample > 16) {
      readSampleInt = () -> {
        final int grayIndex = decompressor.getBitsAsInt(bitsPerSample);
        return getColor(grayIndex);
      };
    } else if (bitsPerSample == 8) {
      readSampleInt = () -> {
        return getColor(decompressor.getByte());
      };
    } else if (bitsPerSample == 16) {
      readSampleInt = () -> {
        final int grayIndex = decompressor.getShort();
        return getColor(grayIndex);
      };
    } else {
      readSampleInt = () -> {
        final int grayIndex = decompressor.getBitsAsInt(bitsPerSample);
        return getColor(grayIndex);
      };
    }
    if (this.invert) {
      readImagePartDoDataBuffer(bufferedImage, decompressor, imageX, imageY, dataWidth, dataHeight,
        cropWidth, () -> this.colorMax - readSampleInt.getValue());
    } else {
      readImagePartDoDataBuffer(bufferedImage, decompressor, imageX, imageY, dataWidth, dataHeight,
        cropWidth, readSampleInt);
    }
  }
}
