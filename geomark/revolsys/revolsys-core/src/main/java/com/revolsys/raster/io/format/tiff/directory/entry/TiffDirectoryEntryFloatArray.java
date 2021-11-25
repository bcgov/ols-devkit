package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;

public class TiffDirectoryEntryFloatArray extends AbstractTiffDirectoryEntry<float[]> {

  @Override
  public float getFloat(final int index) {
    return this.value[index];
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
    return TiffFieldType.FLOAT;
  }

  @Override
  protected float[] loadValueDo(final DataReader in, final int count) {
    final float[] value = new float[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getFloat();
    }
    return value;
  }

  @Override
  public void writeValueDo(final ChannelWriter out) {
    for (final float number : this.value) {
      out.putFloat(number);
    }
  }
}
