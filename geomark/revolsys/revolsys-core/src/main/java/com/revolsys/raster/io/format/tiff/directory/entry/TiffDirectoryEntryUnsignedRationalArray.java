package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;

public class TiffDirectoryEntryUnsignedRationalArray extends AbstractTiffDirectoryEntry<double[]> {

  private long numerator;

  private long denominator;

  @Override
  public double getDouble(final int index) {
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
    return TiffFieldType.RATIONAL;
  }

  @Override
  protected double[] loadValueDo(final DataReader in, final int count) {
    final double[] value = new double[count];
    for (int i = 0; i < count; i++) {
      this.numerator = in.getUnsignedInt();
      this.denominator = in.getUnsignedInt();
      value[i] = this.numerator / this.denominator;
    }
    return value;
  }

  @Override
  protected void writeValueDo(final ChannelWriter out) {
    out.putInt((int)this.numerator);
    out.putInt((int)this.denominator);
  }

}
