package com.revolsys.raster.io.format.tiff.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffBaselineTag;
import com.revolsys.raster.io.format.tiff.compression.TiffDecompressor;

public class TiffPaletteColorImage extends AbstractTiffImage {
  final int bitsPerSample;

  private final int[] colorMap;

  private IndexColorModel colorModel;

  public TiffPaletteColorImage(final TiffDirectory directory) {
    super(directory);
    this.bitsPerSample = directory.getInt(TiffBaselineTag.BitsPerSample);
    this.colorMap = directory.getIntArray(TiffBaselineTag.ColorMap);
    if (this.bitsPerSample % 2 == 1) {
      throw new IllegalStateException(
        "BitsPerSample=" + this.bitsPerSample + " must be a multple of 2");
    } else if (this.bitsPerSample > 16) {
      throw new IllegalStateException("BitsPerSample=" + this.bitsPerSample + " must be <= 16");
    }

    final int indexSize = 1 << this.bitsPerSample;
    final int[] colorTable = new int[indexSize];
    int index = 0;
    for (int i = 0; i < indexSize; i++) {
      final int red = this.colorMap[i] / 256;
      final int green = this.colorMap[i + indexSize] / 256;
      final int blue = this.colorMap[i + 2 * indexSize] / 256;
      colorTable[index++] = red << 16 | green << 8 | blue;
    }

    if (this.bitsPerSample <= 8) {
      this.colorModel = new IndexColorModel(8, indexSize, colorTable, 0, false, -1,
        DataBuffer.TYPE_BYTE);
    } else {
      this.colorModel = new IndexColorModel(this.bitsPerSample, indexSize, colorTable, 0, false, -1,
        DataBuffer.TYPE_USHORT);

    }
  }

  @Override
  protected BufferedImage newBufferedImage(final int imageWidth, final int imageHeight) {
    if (this.bitsPerSample <= 8) {
      return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_INDEXED,
        this.colorModel);
    } else {
      final WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_USHORT,
        imageWidth, imageHeight, 1, null);
      return new BufferedImage(this.colorModel, raster, false, new Hashtable<>());
    }
  }

  @Override
  protected void readImagePartDo(final BufferedImage bufferedImage,
    final TiffDecompressor decompressor, final int imageX, final int imageY, final int dataWidth,
    final int dataHeight, final int cropWidth) {
    ReadSampleInt readSampleInt;
    final int bitsPerSample = this.bitsPerSample;
    if (bitsPerSample == 8) {
      readSampleInt = () -> {
        return decompressor.getByte();
      };
    } else if (bitsPerSample == 16) {
      readSampleInt = () -> {
        return decompressor.getShort();
      };
    } else {
      readSampleInt = () -> {
        return decompressor.getBitsAsInt(bitsPerSample);
      };
    }
    readImagePartDoDataBuffer(bufferedImage, decompressor, imageX, imageY, dataWidth, dataHeight,
      cropWidth, readSampleInt);
  }
}
