package com.revolsys.record.io.format.esri.gdb.xml.model;

import com.revolsys.geometry.model.BoundingBox;

public class EnvelopeN extends Envelope {
  private double mMax;

  private double mMin;

  private SpatialReference spatialReference;

  private double xMax;

  private double xMin;

  private double yMax;

  private double yMin;

  private double zMax;

  private double zMin;

  public EnvelopeN() {
  }

  public EnvelopeN(final SpatialReference spatialReference) {
    if (spatialReference.isHasHorizontalCoordinateSystem()) {
      final BoundingBox boundingBox = spatialReference.getAreaBoundingBox();
      this.xMin = boundingBox.getMinX();
      this.yMin = boundingBox.getMinY();
      this.xMax = boundingBox.getMaxX();
      this.yMax = boundingBox.getMaxY();
      this.zMin = -10000;
      this.zMax = 10000;
      this.spatialReference = spatialReference;
    }
  }

  public double getMMax() {
    return this.mMax;
  }

  public double getMMin() {
    return this.mMin;
  }

  public SpatialReference getSpatialReference() {
    return this.spatialReference;
  }

  public double getXMax() {
    return this.xMax;
  }

  public double getXMin() {
    return this.xMin;
  }

  public double getYMax() {
    return this.yMax;
  }

  public double getYMin() {
    return this.yMin;
  }

  public double getZMax() {
    return this.zMax;
  }

  public double getZMin() {
    return this.zMin;
  }

  public void setMMax(final double mMax) {
    this.mMax = mMax;
  }

  public void setMMin(final double mMin) {
    this.mMin = mMin;
  }

  public void setSpatialReference(final SpatialReference spatialReference) {
    this.spatialReference = spatialReference;
  }

  public void setXMax(final double xMax) {
    this.xMax = xMax;
  }

  public void setXMin(final double xMin) {
    this.xMin = xMin;
  }

  public void setYMax(final double yMax) {
    this.yMax = yMax;
  }

  public void setYMin(final double yMin) {
    this.yMin = yMin;
  }

  public void setZMax(final double zMax) {
    this.zMax = zMax;
  }

  public void setZMin(final double zMin) {
    this.zMin = zMin;
  }

}
