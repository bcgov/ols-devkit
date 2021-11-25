package com.revolsys.raster.io.format.tiff.directory.entry;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.TiffDirectoryEntry;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public abstract class AbstractTiffDirectoryEntry<A> implements TiffDirectoryEntry {

  protected A value;

  private TiffTag tag;

  protected long count;

  protected long offset = -1;

  private TiffDirectory directory;

  public AbstractTiffDirectoryEntry() {
  }

  public AbstractTiffDirectoryEntry(final TiffDirectory directory, final A value,
    final long count) {
    this.directory = directory;
    this.value = value;
    this.count = count;
  }

  @Override
  public long getCount() {
    return this.count;
  }

  @Override
  public TiffDirectory getDirectory() {
    return this.directory;
  }

  @Override
  public long getOffset() {
    return this.offset;
  }

  @Override
  public String getString() {
    final Object value = getValue();
    return DataTypes.toString(value);
  }

  @Override
  public TiffTag getTag() {
    return this.tag;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final <V> V getValue() {
    return (V)this.value;
  }

  @Override
  public boolean isLoaded() {
    return this.value != null;
  }

  protected abstract A loadValueDo(DataReader in, int count);

  @Override
  public void readEntry(final TiffTag tag, final TiffDirectory directory, final DataReader in) {
    this.directory = directory;
    this.tag = tag;
    this.count = directory.readOffsetOrCount(in);
    final int maxInlineSize = directory.getMaxInlineSize();
    final long size = this.count * getValueSizeBytes();
    if (size <= maxInlineSize) {
      this.value = loadValueDo(in, (int)this.count);
      in.skipBytes((int)(maxInlineSize - size));
      this.offset = -1;
    } else {
      this.offset = directory.readOffsetOrCount(in);
    }
  }

  @Override
  public void readValue(final DataReader in) {
    if (!isLoaded()) {
      in.seek(this.offset);
      this.value = loadValueDo(in, (int)this.count);
    }
  }

  public void setOffset(final long offset) {
    this.offset = offset;
  }

  public void setTag(final TiffTag tag) {
    this.tag = tag;
  }

  public void setValue(final A value) {
    this.value = value;
  }

  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    final TiffTag tag = getTag();
    s.append(tag.name());
    s.append(" (");
    s.append(tag.getId());
    s.append(") ");

    final TiffFieldType type = getType();
    s.append(type);
    s.append(" (");
    s.append(type.getId());
    s.append(") ");

    final long count = getCount();
    s.append(count);
    s.append('<');
    if (isLoaded()) {
      if (isArray()) {
        for (int i = 0; i < count; i++) {
          if (i > 0) {
            s.append(' ');
          }
          if (type == TiffFieldType.UNDEFINED) {
            final byte b = getByte(i);
            final String hexString = Integer.toHexString(b & 0xff);
            s.append("0x");
            s.append(hexString);
          } else {
            final Number number = getNumber(i);
            s.append(DataTypes.toString(number));
          }
        }
      } else {
        final String string = getString();
        s.append(string);
      }
    } else {
      s.append("...");
    }
    s.append('>');
    return s.toString();
  }

  @Override
  public void writeEntry(final TiffDirectory directory, final ChannelWriter out) {
    out.putUnsignedShort(this.tag.getId());
    out.putUnsignedShort(getType().getId());
    directory.writeOffsetOrCount(out, this.count);
    final int maxInlineSize = directory.getMaxInlineSize();
    final long size = this.count * getValueSizeBytes();
    if (size <= maxInlineSize) {
      writeValueDo(out);
      for (int i = 0; i < (int)(maxInlineSize - size); i++) {
        out.putByte((byte)0);
      }
    } else {
      directory.writeOffsetOrCount(out, this.offset);
    }
  }

  @Override
  public void writeValue(final ChannelWriter out) {
    out.seek(this.offset);
    writeValueDo(out);
  }

  protected abstract void writeValueDo(ChannelWriter out);
}
