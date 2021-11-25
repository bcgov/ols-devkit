package com.revolsys.raster.io.format.tiff;

import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.code.TiffBaselineTag;
import com.revolsys.raster.io.format.tiff.code.TiffCompression;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffPhotogrametricInterpretation;
import com.revolsys.raster.io.format.tiff.code.TiffTag;
import com.revolsys.raster.io.format.tiff.code.TiffTags;
import com.revolsys.raster.io.format.tiff.image.TiffBilevelImage;
import com.revolsys.raster.io.format.tiff.image.TiffCmykImage;
import com.revolsys.raster.io.format.tiff.image.TiffGrayscaleImage;
import com.revolsys.raster.io.format.tiff.image.TiffImage;
import com.revolsys.raster.io.format.tiff.image.TiffJpegImage;
import com.revolsys.raster.io.format.tiff.image.TiffPaletteColorImage;
import com.revolsys.raster.io.format.tiff.image.TiffRgbFullColorImage;
import com.revolsys.raster.io.format.tiff.image.TiffTransparencyMaskImage;
import com.revolsys.raster.io.format.tiff.image.TiffYCbCrImage;
import com.revolsys.spring.resource.Resource;

public class TiffDirectory {

  private final Map<TiffTag, TiffDirectoryEntry> entryByTag = new TreeMap<>(
    (a, b) -> Integer.compare(a.getId(), b.getId()));

  private final int index;

  private final long offset;

  private long nextOffset;

  private WeakReference<TiffImage> imageReference = new WeakReference<>(null);

  private final Resource resource;

  private TiffDirectory nextDirectory;

  private final ByteOrder byteOrder;

  private final boolean bigTiff;

  public TiffDirectory(final Resource resource, final DataReader in, final int index,
    final long offset, final boolean bigTiff) {
    this.resource = resource;
    this.bigTiff = bigTiff;
    this.byteOrder = in.getByteOrder();
    this.index = index;
    this.offset = offset;

    readDirectory(in);
  }

  public TiffDirectory(final Resource resource, final int index, final ByteOrder byteOrder,
    final boolean bigTiff, final long offset) {
    this.resource = resource;
    this.index = index;
    this.byteOrder = byteOrder;
    this.bigTiff = bigTiff;
    this.offset = offset;
  }

  protected void addEntry(final TiffDirectoryEntry entry) {
    final TiffTag tag = entry.getTag();
    this.entryByTag.put(tag, entry);
  }

  public void dump(final PrintStream out) {
    out.print("Directory ");
    out.print(this.index);
    out.print(": offset ");
    out.print(this.offset);
    out.print(" (0x");
    out.print(Long.toHexString(this.offset));
    out.print(") next ");
    out.print(getNextOffset());
    out.print(" (");
    if (this.nextOffset != 0) {
      out.print("0x");
    }
    out.print(Long.toHexString(this.nextOffset));
    out.println(")");

    for (final TiffDirectoryEntry entry : this.entryByTag.values()) {
      entry.dump(out);
    }
  }

  public byte getByte(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getByte();
  }

  public byte getByte(final TiffTag tag, final byte defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getByte();
    }
  }

  public byte[] getByteArray(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getByteArray();
  }

  public byte[] getByteArray(final TiffTag tag, final byte[] defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getByteArray();
    }
  }

  public ByteOrder getByteOrder() {
    return this.byteOrder;
  }

  private TiffCompression getCompression() {
    final int id = getInt(TiffBaselineTag.Compression, 1);
    return TiffCompression.getById(id);
  }

  public double getDouble(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getDouble();
  }

  public double getDouble(final TiffTag tag, final double defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getDouble();
    }
  }

  public double[] getDoubleArray(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getDoubleArray();
  }

  public double[] getDoubleArray(final TiffTag tag, final double[] defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getDoubleArray();
    }
  }

  public Collection<TiffDirectoryEntry> getEntries() {
    return Collections.unmodifiableCollection(this.entryByTag.values());
  }

  public TiffDirectoryEntry getEntry(final TiffTag tag) {
    return this.entryByTag.get(tag);
  }

  public int getEntryCount() {
    return this.entryByTag.size();
  }

  public TiffDirectoryEntry getEntryRequired(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      throw new IllegalArgumentException(tag + " not found in file: " + this.resource);
    }
    return entry;
  }

  public float getFloat(final TiffTag tag, final float defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getFloat();
    }
  }

  public TiffImage getImage() {
    TiffImage image = this.imageReference.get();
    if (image == null) {
      image = newImage();
      this.imageReference = new WeakReference<>(image);
    }
    return image;
  }

  public int getIndex() {
    return this.index;
  }

  public int getInt(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getInt();
  }

  public int getInt(final TiffTag tag, final int defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getInt();
    }
  }

  public int[] getIntArray(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getIntArray();
  }

  public int[] getIntArray(final TiffTag tag, final int[] defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getIntArray();
    }
  }

  public long getLong(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getLong();
  }

  public long getLong(final TiffTag tag, final long defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getLong();
    }
  }

  public long[] getLongArray(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getLongArray();
  }

  public long[] getLongArray(final TiffTag tag, final long[] defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getLongArray();
    }
  }

  public int getMaxInlineCount(final int dataSize) {
    if (this.bigTiff) {
      return 8 / dataSize;
    } else {
      return 4 / dataSize;
    }
  }

  public int getMaxInlineSize() {
    if (this.bigTiff) {
      return 8;
    } else {
      return 4;
    }
  }

  public long getNextOffset() {
    if (this.nextDirectory == null) {
      return this.nextOffset;
    } else {
      return this.nextDirectory.getOffset();
    }
  }

  public long getOffset() {
    return this.offset;
  }

  public TiffPhotogrametricInterpretation getPhotogrametricInterpretation() {
    final int id = getInt(TiffBaselineTag.PhotometricInterpretation);
    return TiffPhotogrametricInterpretation.getById(id);
  }

  public Resource getResource() {
    return this.resource;
  }

  public short getShort(final TiffTag tag, final short defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getShort();
    }
  }

  public String getString(final TiffTag tag, final String defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getString();
    }
  }

  public boolean isBigTiff() {
    return this.bigTiff;
  }

  public DataReader newChannelReader() {
    return this.resource.newChannelReader();
  }

  public TiffImage newImage() {
    final TiffPhotogrametricInterpretation photometricInterpretation = getPhotogrametricInterpretation();
    final TiffCompression compression = getCompression();
    if (compression == TiffCompression.JPEG) {
      return new TiffJpegImage(this);
    } else {
      switch (photometricInterpretation) {
        case MIN_IS_WHITE: // 0
        case MIN_IS_BLACK: // 1
          final int bitsPerSample = getInt(TiffBaselineTag.BitsPerSample);
          if (bitsPerSample == 1) {
            return new TiffBilevelImage(this);
          } else {
            return new TiffGrayscaleImage(this);
          }
        case RGB: // 2
          return new TiffRgbFullColorImage(this);
        case PALETTE: // 3
          return new TiffPaletteColorImage(this);
        case MASK: // 4
          return new TiffTransparencyMaskImage(this);
        case CMYK: // 5
          return new TiffCmykImage(this);
        case YCBCR: // 6
          return new TiffYCbCrImage(this);
        default:
          throw new IllegalArgumentException(
            "PhotometricInterpretation=" + photometricInterpretation + " not yet supported");
      }
    }
  }

  public void readDirectory(final DataReader in) {
    in.seek(this.offset);
    long recordCount;
    int skipSize;
    if (isBigTiff()) {
      recordCount = in.getLong();
      skipSize = 16;
    } else {
      recordCount = in.getUnsignedShort();
      skipSize = 8;
    }
    for (int i = 0; i < recordCount; i++) {
      final TiffTag tag = TiffTags.readTag(in);
      final TiffFieldType fieldType = TiffFieldType.readValue(in);
      if (fieldType == null) {
        in.skipBytes(skipSize);
      } else {
        final TiffDirectoryEntry entry = fieldType.newDirectoryEntry(tag, this, in);
        addEntry(entry);
      }
    }
    this.nextOffset = readOffsetOrCount(in);
    for (final TiffDirectoryEntry entry : this.entryByTag.values()) {
      entry.readValue(in);
    }
  }

  public long readOffsetOrCount(final DataReader in) {
    if (this.bigTiff) {
      return in.getUnsignedLong();
    } else {
      return in.getUnsignedInt();
    }
  }

  void setNextDirectory(final TiffDirectory nextDirectory) {
    this.nextDirectory = nextDirectory;
  }

  protected RuntimeException throwRequired(final TiffTag tag) {
    return new IllegalArgumentException(tag + " not found in file");
  }

  @Override
  public String toString() {
    return "TiffDirectory " + this.index + ": " + this.resource;
  }

  public void writeOffsetOrCount(final ChannelWriter out, final long value) {
    if (isBigTiff()) {
      out.putUnsignedLong(value);
    } else {
      out.putUnsignedInt(value);
    }
  }

}
