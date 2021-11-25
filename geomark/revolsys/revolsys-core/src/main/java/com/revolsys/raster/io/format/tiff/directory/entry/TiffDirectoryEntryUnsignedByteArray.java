package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;

public class TiffDirectoryEntryUnsignedByteArray extends AbstractTiffDirectoryEntry<short[]> {

  @Override
  public Number getNumber() {
    if (getCount() == 1) {
      return this.value[0];
    } else {
      throw new IllegalStateException("Cannot get single value from array of size " + getCount());
    }
  }

  @Override
  public Number getNumber(final int index) {
    return this.value[index];
  }

  @Override
  public short getShort(final int index) {
    return this.value[index];
  }

  @Override
  public String getString() {
    return Arrays.toString(this.value);
  }

  @Override
  public TiffFieldType getType() {
    return TiffFieldType.BYTE;
  }

  @Override
  protected short[] loadValueDo(final DataReader in, final int count) {
    final short[] value = new short[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getUnsignedByte();
    }
    return value;
  }

  @Override
  public void writeValueDo(final ChannelWriter out) {
    for (final short element : this.value) {
      final byte number = (byte)element;
      out.putByte(number);
    }
  }
}
