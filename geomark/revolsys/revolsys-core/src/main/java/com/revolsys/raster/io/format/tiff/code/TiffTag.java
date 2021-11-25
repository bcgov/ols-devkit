package com.revolsys.raster.io.format.tiff.code;

public interface TiffTag {

  default boolean equalsId(final int id) {
    return getId() == id;
  }

  String getDescription();

  int getId();

  default boolean isArray() {
    return false;
  }

  String name();
}
