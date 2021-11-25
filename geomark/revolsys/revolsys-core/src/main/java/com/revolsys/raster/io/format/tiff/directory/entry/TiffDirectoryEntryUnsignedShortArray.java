package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;

public class TiffDirectoryEntryUnsignedShortArray extends AbstractTiffDirectoryEntry<int[]> {

  public TiffDirectoryEntryUnsignedShortArray() {
    super();
  }

  public TiffDirectoryEntryUnsignedShortArray(final TiffDirectory directory, final int[] value) {
    super(directory, value, value.length);
  }

  @Override
  public int getInt(final int index) {
    return this.value[index];
  }

  @Override
  public int[] getIntArray() {
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
    return TiffFieldType.SHORT;
  }

  @Override
  protected int[] loadValueDo(final DataReader in, final int count) {
    final int[] value = new int[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getUnsignedShort();
    }
    return value;
  }

  @Override
  public void writeValueDo(final ChannelWriter out) {
    for (final int element : this.value) {
      final short number = (short)element;
      out.putShort(number);
    }
  }
}
