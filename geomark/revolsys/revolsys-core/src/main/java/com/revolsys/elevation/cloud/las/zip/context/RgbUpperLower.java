package com.revolsys.elevation.cloud.las.zip.context;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;

public class RgbUpperLower {

  public int redLower;

  public int redUpper;

  public int greenLower;

  public int greenUpper;

  public int blueLower;

  public int blueUpper;

  public void setValues(final LasPoint point) {
    final int red = point.getRed();
    final int green = point.getGreen();
    final int blue = point.getBlue();
    this.redLower = red & 0xFF;
    this.redUpper = red >>> 8;
    this.greenLower = green & 0xFF;
    this.greenUpper = green >>> 8;
    this.blueLower = blue & 0xFF;
    this.blueUpper = blue >>> 8;
  }

  public void setValues(final RgbUpperLower rgb) {
    this.redLower = rgb.redLower;
    this.redUpper = rgb.redUpper;
    this.greenLower = rgb.greenLower;
    this.greenUpper = rgb.greenUpper;
    this.blueLower = rgb.blueLower;
    this.blueUpper = rgb.blueUpper;
  }
}
