package com.revolsys.raster.io.format.tiff.image;

import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffPhotogrametricInterpretation;

public class TiffBilevelImage extends AbstractTiffBinaryImage {

  public TiffBilevelImage(final TiffDirectory directory) {
    super(directory,
      directory.getPhotogrametricInterpretation() == TiffPhotogrametricInterpretation.MIN_IS_WHITE);
  }

}
