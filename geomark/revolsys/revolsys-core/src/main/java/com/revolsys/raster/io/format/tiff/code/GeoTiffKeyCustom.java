package com.revolsys.raster.io.format.tiff.code;

public class GeoTiffKeyCustom implements GeoTiffKey {

  private final int id;

  public GeoTiffKeyCustom(final int id) {
    this.id = id;
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public String toString() {
    return "Custom<" + this.id + ">";
  }

}
