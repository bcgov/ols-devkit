package com.revolsys.raster.io.format.tiff.image;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.InflaterInputStream;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Doubles;
import org.jeometry.common.number.Integers;
import org.jeometry.common.number.Longs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.AbstractGeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageMapTile;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.TiffImageFactory;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKey;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKeys;
import com.revolsys.raster.io.format.tiff.code.TiffBaselineTag;
import com.revolsys.raster.io.format.tiff.code.TiffCompression;
import com.revolsys.raster.io.format.tiff.code.TiffExtensionTag;
import com.revolsys.raster.io.format.tiff.code.TiffPhotogrametricInterpretation;
import com.revolsys.raster.io.format.tiff.code.TiffPrivateTag;
import com.revolsys.raster.io.format.tiff.compression.TiffCCITTFaxDecoderStream;
import com.revolsys.raster.io.format.tiff.compression.TiffDecompressor;
import com.revolsys.raster.io.format.tiff.compression.TiffDecompressorInputStreamBE;
import com.revolsys.raster.io.format.tiff.compression.TiffDecompressorInputStreamLE;
import com.revolsys.raster.io.format.tiff.compression.TiffLzwInputStream;
import com.revolsys.raster.io.format.tiff.compression.TiffPackbitsInputStream;
import com.revolsys.raster.io.format.tiff.compression.TiffThunderscanInputStream;

public abstract class AbstractTiffImage extends AbstractGeoreferencedImage implements TiffImage {

  private class TiffImageTiledMapTile extends GeoreferencedImageMapTile {
    private final int tileX;

    private final int tileY;

    private TiffImageTiledMapTile(final int tileX, final int tileY, final int tileWidth,
      final int tileHeight) {
      super(newTileBoundingBox(tileX, tileY, tileWidth, tileHeight), tileWidth, tileHeight);
      this.tileX = tileX;
      this.tileY = tileY;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      } else if (obj instanceof TiffImageTiledMapTile) {
        final TiffImageTiledMapTile other = (TiffImageTiledMapTile)obj;
        if (this.tileX != other.tileX) {
          return false;
        } else if (this.tileY != other.tileY) {
          return false;
        } else {
          return true;
        }
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + this.tileX;
      result = prime * result + this.tileY;
      return result;
    }

    @Override
    protected BufferedImage loadBuffferedImage() {
      try (
        DataReader in = AbstractTiffImage.this.directory.newChannelReader()) {
        final int tileIndex = this.tileY * AbstractTiffImage.this.tileCountX + this.tileX;
        final int actualTileWidth = getWidthPixels();
        final int actualTileHeight = getHeightPixels();
        final BufferedImage bufferedImage = newBufferedImage(actualTileWidth, actualTileHeight);

        readImagePart(in, bufferedImage, AbstractTiffImage.this.tileOffsets,
          AbstractTiffImage.this.tileByteCounts, tileIndex, 0, 0,
          AbstractTiffImage.this.tileWidthPixels, actualTileHeight, actualTileWidth);

        return bufferedImage;
      }
    }

  }

  private static Map<GeoTiffKey, Object> getGeoKeys(final TiffDirectory directory) {
    final Map<GeoTiffKey, Object> geoKeys = new LinkedHashMap<>();

    final int[] keys = directory.getIntArray(TiffPrivateTag.GeoKeyDirectoryTag,
      Integers.EMPTY_ARRAY);
    final String asciiParams = directory.getString(TiffPrivateTag.GeoAsciiParamsTag, "");
    final double[] doubleParams = directory.getDoubleArray(TiffPrivateTag.GeoDoubleParamsTag,
      Doubles.EMPTY_ARRAY);

    if (keys.length > 0) {
      for (int i = 4; i < keys.length; i += 4) {
        final GeoTiffKey keyId = GeoTiffKeys.getById(keys[i]);
        final int tiffTag = keys[i + 1];
        final int valueCount = keys[i + 2];
        final int valueOrOffset = keys[i + 3];

        Object value = null;
        switch (tiffTag) {
          case 34736: // DOUBLE
            value = doubleParams[valueOrOffset];
          break;
          case 34737: // ASCII
            value = asciiParams.substring(valueOrOffset, valueOrOffset + valueCount - 1);
          break;

          default:
            value = (short)valueOrOffset;
          break;
        }
        geoKeys.put(keyId, value);
      }

    }
    return geoKeys;
  }

  private final TiffCompression compression;

  private final TiffDirectory directory;

  private final TiffPhotogrametricInterpretation photometricInterpretation;

  private final int pixelResolutionUnit;

  private int pixelResolutionX;

  private int pixelResolutionY;

  private int rowsPerStrip;

  private final long[] stripByteCounts;

  private final long[] stripOffsets;

  private final long[] tileByteCounts;

  private final int tileHeightPixels;

  private final long[] tileOffsets;

  private final int tileWidthPixels;

  protected int planarConfiguration;

  private int stripOrTileCount;

  private final int stripCount;

  private final int tileCountX;

  private final int tileCountY;

  public AbstractTiffImage(final TiffDirectory directory) {
    this.directory = directory;
    final int width = directory.getInt(TiffBaselineTag.ImageWidth, -1);
    setImageWidth(width);
    final int height = directory.getInt(TiffBaselineTag.ImageLength, -1);
    setImageHeight(height);
    final int compressionId = directory.getInt(TiffBaselineTag.Compression, 1);
    this.compression = TiffCompression.getById(compressionId);
    if (this.compression == null) {
      throw new IllegalArgumentException("Compression " + compressionId + " unknown");
    }
    this.photometricInterpretation = directory.getPhotogrametricInterpretation();
    this.planarConfiguration = directory.getInt(TiffBaselineTag.PlanarConfiguration, 1);
    this.stripOffsets = directory.getLongArray(TiffBaselineTag.StripOffsets, Longs.EMPTY_ARRAY);
    this.rowsPerStrip = directory.getInt(TiffBaselineTag.RowsPerStrip, -1);
    final int imageWidth = getImageWidth();
    final int imageHeight = getImageHeight();
    if (this.rowsPerStrip == -1 || this.rowsPerStrip > imageHeight) {
      this.rowsPerStrip = imageHeight;
    }
    this.stripByteCounts = directory.getLongArray(TiffBaselineTag.StripByteCounts,
      Longs.EMPTY_ARRAY);
    this.pixelResolutionUnit = directory.getInt(TiffBaselineTag.ResolutionUnit, 1);

    this.tileWidthPixels = directory.getInt(TiffExtensionTag.TileWidth, -1);
    this.tileHeightPixels = directory.getInt(TiffExtensionTag.TileLength, -1);
    this.tileOffsets = directory.getLongArray(TiffExtensionTag.TileOffsets, this.stripOffsets);
    this.tileByteCounts = directory.getLongArray(TiffExtensionTag.TileByteCounts,
      this.stripByteCounts);

    try {
      this.pixelResolutionX = (int)directory.getDouble(TiffBaselineTag.XResolution, 1);
      this.pixelResolutionY = (int)directory.getDouble(TiffBaselineTag.YResolution, 1);
      setDpi(this.pixelResolutionX, this.pixelResolutionY);
    } catch (final Throwable e) {
      Logs.error(this, e);
    }
    final Map<GeoTiffKey, Object> geoKeys = getGeoKeys(directory);
    final GeometryFactory geometryFactory = TiffImageFactory.getGeometryFactory(geoKeys);
    if (geometryFactory != null) {
      setGeometryFactory(geometryFactory);
    }

    final double[] tiePoints = directory.getDoubleArray(TiffPrivateTag.ModelTiepointTag,
      Doubles.EMPTY_ARRAY);
    if (tiePoints.length == 0) {
      final double[] geoTransform = directory.getDoubleArray(TiffPrivateTag.ModelTransformationTag,
        Doubles.EMPTY_ARRAY);
      if (geoTransform.length > 0) {
        final double pixelWidth = geoTransform[0];
        final double yRotation = geoTransform[1];
        final double x1 = geoTransform[3];
        final double xRotation = geoTransform[4];
        final double pixelHeight = geoTransform[5];
        final double y1 = geoTransform[7];
        setResolutionX(pixelWidth);
        setResolutionY(pixelHeight);
        // TODO rotation
        setBoundingBox(x1, y1, pixelWidth, pixelHeight);
      }
    } else {
      final double[] pixelScale = directory.getDoubleArray(TiffPrivateTag.ModelPixelScaleTag);
      if (pixelScale.length > 0) {
        final double rasterXOffset = tiePoints[0];
        final double rasterYOffset = tiePoints[1];
        if (rasterXOffset != 0 && rasterYOffset != 0) {
          // These should be 0, not sure what to do if they are not
          throw new IllegalArgumentException(
            "Exepectig 0 for the raster x,y tie points in a GeoTIFF");
        }

        // Top left corner of image in model coordinates
        final double x1 = tiePoints[3];
        final double y1 = tiePoints[4];

        final double pixelWidth = pixelScale[0];
        final double pixelHeight = pixelScale[1];
        setResolutionX(pixelWidth);
        setResolutionY(pixelHeight);
        setBoundingBox(x1, y1, pixelWidth, -pixelHeight);
      }
    }
    this.tileCountX = (imageWidth + this.tileWidthPixels - 1) / this.tileWidthPixels;
    this.tileCountY = (imageHeight + this.tileHeightPixels - 1) / this.tileHeightPixels;
    this.stripCount = (imageHeight + this.rowsPerStrip - 1) / this.rowsPerStrip;
    if (isTiled()) {
      this.stripOrTileCount = this.tileCountX + this.tileCountY;
    } else {
      this.stripOrTileCount = this.stripCount;
    }
  }

  private InputStream getInputStream(final DataReader in, final long[] offsets,
    final long[] counts, final int index) {
    final long offset = offsets[index];
    final int byteCount = (int)counts[index];
    return in.getInputStream(offset, byteCount);
  }

  @Override
  public List<GeoreferencedImageMapTile> getOverlappingMapTiles(BoundingBox boundingBox) {
    final BoundingBox imageBoundingBox = getBoundingBox();
    boundingBox = boundingBox.bboxToCs(this);
    if (bboxIntersects(imageBoundingBox)) {
      final List<GeoreferencedImageMapTile> tiles = new ArrayList<>();
      if (isTiled()) {
        final int lastTileWidthPixels = getImageWidth() % this.tileWidthPixels;
        final int lastTileHeightPixels = getImageHeight() % this.tileHeightPixels;
        final double tileWidth = getResolutionX() * this.tileWidthPixels;
        final double tileHeight = getResolutionY() * this.tileHeightPixels;
        final double imageMinX = imageBoundingBox.getMinX();
        final double imageMaxY = imageBoundingBox.getMaxY();

        final double minX = boundingBox.getMinX();
        final double minY = boundingBox.getMinY();
        final double maxX = boundingBox.getMaxX();
        final double maxY = boundingBox.getMaxY();

        // Tiles start at the North-West corner of the map
        int minTileX = (int)Math.floor((minX - imageMinX) / tileWidth);
        if (minTileX < 0) {
          minTileX = 0;
        }
        int minTileY = (int)Math.floor((imageMaxY - maxY) / tileHeight);
        if (minTileY < 0) {
          minTileY = 0;
        }
        int maxTileX = (int)Math.floor((maxX - imageMinX) / tileWidth);
        if (maxTileX >= this.tileCountX) {
          maxTileX = this.tileCountX - 1;
        }
        int maxTileY = (int)Math.floor((imageMaxY - minY) / tileHeight);
        if (maxTileY >= this.tileCountY) {
          maxTileY = this.tileCountY - 1;
        }

        int tileHeightPixels = this.tileHeightPixels;
        for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
          if (tileY == this.tileCountY - 1) {
            tileHeightPixels = lastTileHeightPixels;
          }
          int tileWidthPixels = this.tileWidthPixels;
          for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
            if (tileX == this.tileCountX - 1) {
              tileWidthPixels = lastTileWidthPixels;
            }
            final GeoreferencedImageMapTile tile = new TiffImageTiledMapTile(tileX, tileY,
              tileWidthPixels, tileHeightPixels);
            tiles.add(tile);
          }
        }
      }
      return tiles;
    } else {
      return Collections.emptyList();
    }
  }

  public TiffPhotogrametricInterpretation getPhotometricInterpretation() {
    return this.photometricInterpretation;
  }

  @Override
  public synchronized RenderedImage getRenderedImage() {
    RenderedImage image = super.getRenderedImage();
    if (image == null) {
      BufferedImage bufferedImage;
      try (
        DataReader in = this.directory.newChannelReader()) {
        if (isTiled()) {
          bufferedImage = newBufferedImageTiles(in);
        } else if (this.stripOffsets.length > 0) {
          bufferedImage = newBufferedImageStrips(in);
        } else {
          throw new IllegalArgumentException("Data must be in strips or tiles: " + this.directory);
        }
      }
      image = bufferedImage;
      setRenderedImage(image);
    }
    return image;
  }

  public int getRowsPerStrip() {
    return this.rowsPerStrip;
  }

  protected int getStripOrTileCount() {
    return this.stripOrTileCount;
  }

  @Override
  public TiffDirectory getTiffDirectory() {
    return this.directory;
  }

  public int getTileHeight() {
    return this.tileHeightPixels;
  }

  public int getTileWidth() {
    return this.tileWidthPixels;
  }

  public boolean isTiled() {
    return this.tileWidthPixels > 0 && this.tileHeightPixels > 0;
  }

  private BufferedImage newBufferedImage() {
    final int imageWidth = getImageWidth();
    final int imageHeight = getImageHeight();
    return newBufferedImage(imageWidth, imageHeight);
  }

  protected abstract BufferedImage newBufferedImage(int imageWidth, int imageHeight);

  private BufferedImage newBufferedImageStrips(final DataReader in) {
    final BufferedImage bufferedImage = newBufferedImage();
    final int imageWidth = getImageWidth();
    final int imageHeight = getImageHeight();
    for (int stripIndex = 0; stripIndex < this.stripCount; stripIndex++) {
      final int imageY = stripIndex * this.rowsPerStrip;
      final int index = stripIndex;
      int stripHeight = this.rowsPerStrip;
      if (stripIndex == this.stripCount - 1) {
        stripHeight = imageHeight - imageY;
      }
      readImagePart(in, bufferedImage, this.stripOffsets, this.stripByteCounts, index, 0, imageY,
        imageWidth, stripHeight, imageWidth);
    }
    return bufferedImage;
  }

  private BufferedImage newBufferedImageTiles(final DataReader in) {
    final BufferedImage bufferedImage = newBufferedImage();
    final int imageWidth = getImageWidth();
    final int imageHeight = getImageHeight();

    int lastTileWidth = imageWidth % this.tileWidthPixels;
    if (lastTileWidth == 0) {
      lastTileWidth = this.tileWidthPixels;
    }
    int lastTileHeight = imageHeight % this.tileHeightPixels;
    if (lastTileHeight == 0) {
      lastTileHeight = this.tileHeightPixels;
    }
    int tileIndex = 0;
    for (int tileY = 0; tileY < this.tileCountY; tileY++) {
      final int imageY = tileY * this.tileHeightPixels;
      int actualTileHeight = this.tileHeightPixels;
      if (tileY == this.tileCountY - 1) {
        actualTileHeight = lastTileHeight;
      }
      for (int tileX = 0; tileX < this.tileCountX; tileX++) {
        final int imageX = tileX * this.tileWidthPixels;
        int actualTileWidth = this.tileWidthPixels;
        if (tileX == this.tileCountX - 1) {
          actualTileWidth = lastTileWidth;
        }

        readImagePart(in, bufferedImage, this.tileOffsets, this.tileByteCounts, tileIndex, imageX,
          imageY, this.tileWidthPixels, actualTileHeight, actualTileWidth);

        tileIndex++;
      }
    }
    return bufferedImage;
  }

  protected TiffDecompressor newPlanarDecompressor(final DataReader in, final long[] offsets,
    final long[] counts, final int partIndex, final int sampleIndex) {
    if (sampleIndex < 0) {
      return null;
    } else {
      final int stripOrTileCount = getStripOrTileCount();
      final TiffDecompressor decompressor = newTiffDecompressor(in, offsets, counts,
        stripOrTileCount * sampleIndex + partIndex);
      return decompressor;
    }
  }

  protected ReadSampleInt newSampleReader(final TiffDecompressor decompressor,
    final int bitsPerSample) {
    if (bitsPerSample == 8) {
      return () -> {
        return decompressor.getByte();
      };
    } else if (bitsPerSample == 24) {
      return () -> {
        return decompressor.getBytesAsInt(3) >> 16;
      };
    } else if (bitsPerSample == 32) {
      return () -> {
        return (int)(decompressor.getUnsignedInt() >> 24);
      };
    } else if (bitsPerSample % 8 == 0) {
      final int bytesPerSample = bitsPerSample / 8;
      final int divisor = 1 << bitsPerSample;
      return () -> {
        return decompressor.getBytesAsInt(bytesPerSample) * 256 / divisor;
      };
    } else {
      final int divisor = 1 << bitsPerSample;
      return () -> {
        return decompressor.getBitsAsInt(bitsPerSample) * 256 / divisor;
      };
    }
  }

  protected ReadSampleInt newSampleReader(final TiffDecompressor decompressor,
    final int bitsPerSample, final int defaultValue) {
    if (decompressor == null) {
      return () -> defaultValue;
    } else {
      return newSampleReader(decompressor, bitsPerSample);
    }
  }

  protected TiffDecompressor newTiffDecompressor(final DataReader in, final long[] offsets,
    final long[] counts, final int partIndex) {
    final InputStream inputStream = getInputStream(in, offsets, counts, partIndex);
    return newTiffDecompressor(inputStream);
  }

  protected TiffDecompressor newTiffDecompressor(final InputStream in) {
    InputStream decompressedIn;
    switch (this.compression) {
      case NONE: // 1
      case JPEG: // 7
        decompressedIn = in;
      break;
      case CIIT_GROUP_3_FAX: // 3
        decompressedIn = new TiffCCITTFaxDecoderStream(in, this.directory, this.compression);
      break;
      case CIIT_GROUP_4_FAX: // 4
        decompressedIn = new TiffCCITTFaxDecoderStream(in, this.directory, this.compression);
      break;
      case LZW: // 5
        decompressedIn = new TiffLzwInputStream(in);
      break;
      case ADOBE_DEFLATE: // 8
      case DEFLATE: // 32946
        decompressedIn = new InflaterInputStream(in);
      break;
      case PACKBITS: // 32773
        decompressedIn = new TiffPackbitsInputStream(in);
      break;
      case THUNDERSCAN:
        if (this instanceof TiffGrayscaleImage) {
          if (this.directory.getInt(TiffBaselineTag.BitsPerSample) != 4) {
            throw new IllegalStateException("Thunderscan only supports 4-bit Greyscale images");
          } else {
            decompressedIn = new TiffThunderscanInputStream(in);
          }
        } else {
          throw new IllegalStateException("Thunderscan only supports Greyscale images");
        }
      break;
      default:
        throw new IllegalArgumentException(
          "Compression " + this.compression + " not yet supported");
    }
    if (this.directory.getByteOrder() == ByteOrder.BIG_ENDIAN) {
      return new TiffDecompressorInputStreamBE(decompressedIn);
    } else {
      return new TiffDecompressorInputStreamLE(decompressedIn);
    }
  }

  public BoundingBox newTileBoundingBox(final int tileX, final int tileY, final int tileWidthPixels,
    final int tileHeightPixels) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double resolutionX = getResolutionX();
    final double resolutionY = getResolutionY();
    final BoundingBox imageBoundingBox = getBoundingBox();
    final double minX = imageBoundingBox.getMinX() + tileX * this.tileWidthPixels * resolutionX;
    final double maxY = imageBoundingBox.getMaxY() - tileY * this.tileHeightPixels * resolutionY;
    final double maxX = minX + tileWidthPixels * resolutionX + resolutionX;
    final double minY = maxY - tileHeightPixels * resolutionY - resolutionX;
    return geometryFactory.newBoundingBox(minX, maxY, maxX, minY);
  }

  protected void readImagePart(final DataReader in, final BufferedImage bufferedImage,
    final long[] offsets, final long[] counts, final int partIndex, final int imageX,
    final int imageY, final int dataWidth, final int dataHeight, final int cropWidth) {
    try (
      final TiffDecompressor stripDecompressor = newTiffDecompressor(in, offsets, counts,
        partIndex)) {
      readImagePartDo(bufferedImage, stripDecompressor, imageX, imageY, dataWidth, dataHeight,
        cropWidth);
    }
  }

  protected void readImagePartDo(final BufferedImage bufferedImage,
    final TiffDecompressor decompressor, final int imageX, final int imageY, final int dataWidth,
    final int dataHeight, final int cropWidth) {
  }

  protected void readImagePartDoDataBuffer(final BufferedImage bufferedImage,
    final TiffDecompressor decompressor, final int imageX, final int imageY, final int dataWidth,
    final int dataHeight, final int cropWidth, final ReadSampleInt readSampleInt) {
    final int imageWidth = bufferedImage.getWidth();
    final WritableRaster raster = bufferedImage.getRaster();
    final DataBuffer dataBuffer = raster.getDataBuffer();
    try {
      int y = imageY;
      for (int yIndex = 0; yIndex < dataHeight; yIndex++) {
        int x = imageX;
        for (int xIndex = 0; xIndex < dataWidth; xIndex++) {
          final int colorIndex = readSampleInt.getValue();
          if (xIndex < cropWidth) {
            final int pixelIndex = y * imageWidth + x;
            dataBuffer.setElem(pixelIndex, colorIndex);
            x++;
          }
        }
        decompressor.endRow();
        y++;
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  protected void readImagePartPixelValueFloat(final BufferedImage bufferedImage,
    final TiffDecompressor decompressor, final int imageX, final int imageY, final int dataWidth,
    final int dataHeight, final int cropWidth, final ReadPixelValueFloat readPixelValue) {
    try {
      final ColorModel colorModel = bufferedImage.getColorModel();
      final ColorSpace colorSpace = colorModel.getColorSpace();
      final WritableRaster raster = bufferedImage.getRaster();
      final float[] pixelData = new float[3];
      int y = imageY;
      for (int yIndex = 0; yIndex < dataHeight; yIndex++) {
        int x = imageX;
        for (int xIndex = 0; xIndex < dataWidth; xIndex++) {
          readPixelValue.getPixelValue(pixelData);
          if (xIndex < cropWidth) {
            final float[] f = colorSpace.fromRGB(pixelData);
            final Object dataElements = colorModel.getDataElements(f, 0, null);
            raster.setDataElements(x, y, dataElements);
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

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    super.setBoundingBox(boundingBox);
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    double gridTileWidth;
    double gridTileHeight;
    if (isTiled()) {
      gridTileWidth = this.tileWidthPixels * getResolutionX();
      gridTileHeight = this.tileHeightPixels * getResolutionY();
    } else {
      gridTileWidth = boundingBox.getWidth();
      gridTileHeight = this.rowsPerStrip * getResolutionY();
    }
  }

  @Override
  public String toString() {
    return this.photometricInterpretation + " " + this.directory.getIndex() + ": "
      + this.directory.getResource();
  }

}
