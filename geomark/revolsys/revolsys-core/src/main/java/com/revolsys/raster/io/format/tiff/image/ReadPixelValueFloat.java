package com.revolsys.raster.io.format.tiff.image;

import java.io.IOException;

@FunctionalInterface
public interface ReadPixelValueFloat {
  void getPixelValue(float[] pixel) throws IOException;
}
