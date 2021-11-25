package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;

public class TiffDirectoryEntrySignedByteArray extends AbstractTiffDirectoryEntry<byte[]> {

  @Override
  public byte getByte(final int index) {
    return this.value[index];
  }

  @Override
  public byte[] getByteArray() {
    return getValue();
  }

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
  public String getString() {
    return Arrays.toString(this.value);
  }

  @Override
  public TiffFieldType getType() {
    return TiffFieldType.SBYTE;
  }

  @Override
  protected byte[] loadValueDo(final DataReader in, final int count) {
    final byte[] value = new byte[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getByte();
    }
    return value;
  }

  @Override
  public void writeValueDo(final ChannelWriter out) {
    for (final byte number : this.value) {
      out.putByte(number);
    }
  }
}
