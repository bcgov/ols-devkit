package com.revolsys.elevation.cloud.las.pointformat;

public interface LasPointRgb extends LasPoint {
  @Override
  int getBlue();

  @Override
  int getGreen();

  @Override
  int getRed();
}
