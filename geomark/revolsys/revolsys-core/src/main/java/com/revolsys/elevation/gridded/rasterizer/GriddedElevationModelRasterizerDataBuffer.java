package com.revolsys.elevation.gridded.rasterizer;

import java.awt.image.DataBuffer;

public class GriddedElevationModelRasterizerDataBuffer extends DataBuffer {
  private final GriddedElevationModelRasterizer rasterizer;

  public GriddedElevationModelRasterizerDataBuffer(
    final GriddedElevationModelRasterizer rasterizer) {
    super(TYPE_INT, rasterizer.getWidth() * rasterizer.getHeight());
    this.rasterizer = rasterizer;
  }

  @Override
  public int getElem(final int bank, final int index) {
    if (bank == 0) {
      return this.rasterizer.getValue(index);
    } else {
      return 0;
    }
  }

  @Override
  public void setElem(final int bank, final int i, final int val) {
    throw new UnsupportedOperationException();
  }
}
