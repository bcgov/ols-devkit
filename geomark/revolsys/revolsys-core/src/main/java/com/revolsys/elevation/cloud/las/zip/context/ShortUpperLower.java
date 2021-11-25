package com.revolsys.elevation.cloud.las.zip.context;

public class ShortUpperLower {

  public int lower;

  public int upper;

  public void setValues(final int value) {
    this.lower = value & 0xFF;
    this.upper = value >>> 8;
  }

  public void setValues(final ShortUpperLower upperLower) {
    this.lower = upperLower.lower;
    this.upper = upperLower.upper;
  }
}
