package com.revolsys.raster.io.format.tiff.image;

import com.revolsys.raster.io.format.tiff.TiffDirectory;

public class TiffTransparencyMaskImage extends AbstractTiffBinaryImage {

  public TiffTransparencyMaskImage(final TiffDirectory directory) {
    super(directory, false);
  }

}
