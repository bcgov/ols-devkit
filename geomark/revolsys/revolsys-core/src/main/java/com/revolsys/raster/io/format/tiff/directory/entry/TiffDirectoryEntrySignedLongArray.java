package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;

public class TiffDirectoryEntrySignedLongArray extends AbstractTiffDirectoryEntry<long[]> {

  @Override
  public long getLong(final int index) {
    return this.value[index];
  }

  @Override
  public long[] getLongArray() {
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
    return TiffFieldType.SLONG8;
  }

  @Override
  protected long[] loadValueDo(final DataReader in, final int count) {
    final long[] value = new long[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getLong();
    }
    return value;
  }

  @Override
  public void writeValueDo(final ChannelWriter out) {
    for (final long number : this.value) {
      out.putLong(number);
    }
  }
}
