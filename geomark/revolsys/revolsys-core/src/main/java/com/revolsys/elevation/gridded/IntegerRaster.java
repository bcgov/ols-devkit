package com.revolsys.elevation.gridded;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

public class IntegerRaster extends WritableRaster {

  protected IntegerRaster(final SampleModel sampleModel, final DataBuffer dataBuffer) {
    super(sampleModel, dataBuffer, new Point(0, 0));
  }

}
