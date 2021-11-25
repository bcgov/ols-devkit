package com.revolsys.raster.io.format.tiff;

import java.util.function.BiFunction;

import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.raster.io.format.tiff.code.TiffBaselineTag;
import com.revolsys.raster.io.format.tiff.code.TiffCompression;
import com.revolsys.raster.io.format.tiff.code.TiffExtensionTag;
import com.revolsys.raster.io.format.tiff.code.TiffPhotogrametricInterpretation;
import com.revolsys.raster.io.format.tiff.code.TiffTag;
import com.revolsys.raster.io.format.tiff.directory.entry.AbstractTiffDirectoryEntry;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedIntArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedLongArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedShortArray;

public class TiffDirectoryBuilder extends TiffDirectory {

  private final TiffFileBuilder fileBuilder;

  private int writtenSize = 0;

  private int imageWidth;

  private int imageHeight;

  private int tileWidth;

  private int tileHeight;

  public TiffDirectoryBuilder(final TiffFileBuilder fileBuilder,
    final TiffPhotogrametricInterpretation photogrametricInterpretation) {
    super(fileBuilder.getResource(), fileBuilder.getDirectoryCount(), fileBuilder.getByteOrder(),
      fileBuilder.isBigTiff(), -1);
    this.fileBuilder = fileBuilder;
    fileBuilder.addDirectory(this);
    setPhotogrametricInterpretation(photogrametricInterpretation);
  }

  protected void addEntry(final AbstractTiffDirectoryEntry<?> entry, final TiffTag tag) {
    entry.setTag(tag);
    super.addEntry(entry);
  }

  public TiffFileBuilder getFileBuilder() {
    return this.fileBuilder;
  }

  public int getSize() {
    final int recordCount = getEntryCount();
    if (isBigTiff()) {
      return 8 + 8 + recordCount * 20;
    } else {
      return 2 + 4 + recordCount * 12;
    }
  }

  boolean isSizeIncreased() {
    return this.writtenSize != getSize();
  }

  private <V> AbstractTiffDirectoryEntry<V> newEntryArray(final TiffTag tag,
    final BiFunction<TiffDirectory, Integer, AbstractTiffDirectoryEntry<V>> constructor,
    final int count) {
    final AbstractTiffDirectoryEntry<V> entry = constructor.apply(this, count);
    addEntry(entry, tag);
    return entry;
  }

  private <V> AbstractTiffDirectoryEntry<V> newEntryValue(final TiffTag tag,
    final BiFunction<TiffDirectory, V, AbstractTiffDirectoryEntry<V>> constructor, final V value) {
    final AbstractTiffDirectoryEntry<V> entry = constructor.apply(this, value);
    addEntry(entry, tag);
    return entry;
  }

  public TiffDirectoryBuilder setBitsPerSample(final int... value) {
    setUnsignedShort(TiffBaselineTag.SamplesPerPixel, value.length);
    setUnsignedShort(TiffBaselineTag.BitsPerSample, value);
    return this;
  }

  public TiffDirectoryBuilder setCompression(final TiffCompression compression) {
    setUnsignedShort(TiffBaselineTag.Compression, compression.getId());
    return this;
  }

  public TiffDirectoryBuilder setImageHeight(final int height) {
    this.imageHeight = height;
    if (height < 1 << 16) {
      setUnsignedShort(TiffBaselineTag.ImageLength, height);
    } else {
      setUnsignedInt(TiffBaselineTag.ImageLength, height);
    }
    return this;
  }

  public TiffDirectoryBuilder setImageWidth(final int width) {
    this.imageWidth = width;
    if (width < 1 << 16) {
      setUnsignedShort(TiffBaselineTag.ImageWidth, width);
    } else {
      setUnsignedInt(TiffBaselineTag.ImageWidth, width);
    }

    return this;
  }

  protected void setPhotogrametricInterpretation(
    final TiffPhotogrametricInterpretation photogrametricInterpretation) {
    setUnsignedShort(TiffBaselineTag.PhotometricInterpretation,
      photogrametricInterpretation.getId());
  }

  public TiffDirectoryBuilder setTileHeight(final int tileHeight) {
    this.tileHeight = tileHeight;
    setUnsignedShort(TiffExtensionTag.TileLength, tileHeight);
    updateTileArrays();
    return this;
  }

  public TiffDirectoryBuilder setTileSize(final int tileSize) {
    setTileWidth(tileSize);
    setTileHeight(tileSize);
    return this;
  }

  public TiffDirectoryBuilder setTileWidth(final int tileWidth) {
    this.tileWidth = tileWidth;
    setUnsignedShort(TiffExtensionTag.TileWidth, tileWidth);
    updateTileArrays();
    return this;
  }

  public TiffDirectoryBuilder setUnsignedInt(final TiffTag tag, final long... value) {
    newEntryValue(tag, TiffDirectoryEntryUnsignedIntArray::new, value);
    return this;
  }

  public TiffDirectoryBuilder setUnsignedLong(final TiffTag tag, final long... value) {
    newEntryValue(tag, TiffDirectoryEntryUnsignedLongArray::new, value);
    return this;
  }

  public TiffDirectoryBuilder setUnsignedShort(final TiffTag tag, final int... value) {
    newEntryValue(tag, TiffDirectoryEntryUnsignedShortArray::new, value);
    return this;
  }

  private void updateTileArrays() {
    if (this.imageWidth == 0 || this.imageHeight == 0) {
      throw new IllegalStateException("Image width and height not set");
    } else if (this.tileWidth > 0 && this.tileHeight > 0) {
      final int tileCountX = (this.imageWidth + this.tileWidth - 1) / this.tileWidth;
      final int tileCountY = (this.imageHeight + this.tileHeight - 1) / this.tileHeight;
      final int tileCount = tileCountX * tileCountY;
      if (isBigTiff()) {
        newEntryArray(TiffExtensionTag.TileOffsets, TiffDirectoryEntryUnsignedLongArray::new,
          tileCount);
      } else {
        newEntryArray(TiffExtensionTag.TileOffsets, TiffDirectoryEntryUnsignedIntArray::new,
          tileCount);
      }
      newEntryArray(TiffExtensionTag.TileByteCounts, TiffDirectoryEntryUnsignedIntArray::new,
        tileCount);
    }
  }

  void writeDirectory(final ChannelWriter out) {
    this.writtenSize = getSize();
    final int recordCount = getEntryCount();
    if (isBigTiff()) {
      out.putLong(recordCount);
    } else {
      out.putUnsignedShort(recordCount);
    }
    for (final TiffDirectoryEntry entry : getEntries()) {
      if (!entry.isInline()) {
        if (entry.getOffset() == -1) {
          final int entrySize = entry.getSizeBytes();
          final long offset = this.fileBuilder.reserveSpace(entrySize);
          ((AbstractTiffDirectoryEntry<?>)entry).setOffset(offset);
        }
      }
      entry.writeEntry(this, out);
    }
  }

  public void writeEntries(final ChannelWriter out) {
    for (final TiffDirectoryEntry entry : getEntries()) {
      if (!entry.isInline()) {
        entry.writeValue(out);
      }
    }
  }

}
