package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.raster.io.format.tiff.code.TiffFieldType;

public class TiffDirectoryEntryUndefinedArray extends TiffDirectoryEntrySignedByteArray {

  @Override
  public TiffFieldType getType() {
    return TiffFieldType.UNDEFINED;
  }

}
