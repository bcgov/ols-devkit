package com.revolsys.raster.io.format.tiff.code;

public class TiffCustomTag implements TiffTag {

  private final int id;

  public TiffCustomTag(final int id) {
    this.id = id;
  }

  @Override
  public String getDescription() {
    return "Unknown tag";
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public boolean isArray() {
    return true;
  }

  @Override
  public String name() {
    return Integer.toString(this.id);
  }

  @Override
  public String toString() {
    return "Custom: " + this.id;
  }
}
