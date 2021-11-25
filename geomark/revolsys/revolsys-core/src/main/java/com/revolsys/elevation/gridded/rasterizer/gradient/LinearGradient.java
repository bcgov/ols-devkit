package com.revolsys.elevation.gridded.rasterizer.gradient;

public interface LinearGradient extends Cloneable {

  static int NULL_COLOR = 0;

  LinearGradient clone();

  int getColorIntForValue(double elevation);

  double getValueMax();

  double getValueMin();

  void updateValues();
}
