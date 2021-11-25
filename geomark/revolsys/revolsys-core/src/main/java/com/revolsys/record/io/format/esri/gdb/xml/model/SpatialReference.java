package com.revolsys.record.io.format.esri.gdb.xml.model;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;

public class SpatialReference implements GeometryFactoryProxy {

  private static final double FLOATING_SCALE = 11258999068426.238;

  public static SpatialReference get(final GeometryFactory geometryFactory, final String wkt) {
    if (geometryFactory != null) {
      if (geometryFactory.isGeographic()) {
        return new EsriGdbGeographicCoordinateSystem(geometryFactory, wkt);
      } else if (geometryFactory.isProjected()) {
        return new EsriGdbProjectedCoordinateSystem(geometryFactory, wkt);
      }
    }
    return null;
  }

  private GeometryFactory geometryFactory;

  private boolean highPrecision;

  private int latestWKID;

  private double leftLongitude;

  private double mOrigin;

  private double mScale;

  private double mTolerance;

  private int wkid;

  private String wkt;

  private double xOrigin;

  private double xYScale;

  private double xYTolerance;

  private double yOrigin;

  private double zOrigin;

  private double zScale;

  private double zTolerance;

  public SpatialReference() {
  }

  protected SpatialReference(final GeometryFactory geometryFactory, final String wkt) {
    this.geometryFactory = geometryFactory;
    if (geometryFactory != null) {
      if (geometryFactory.isHasHorizontalCoordinateSystem()) {
        this.wkt = wkt;
        if (this instanceof EsriGdbGeographicCoordinateSystem) {
          this.xOrigin = -180;
          this.yOrigin = -90;
        } else {
          final BoundingBox areaBoundingBox = geometryFactory.getAreaBoundingBox();
          final double minX = areaBoundingBox.getMinX();
          final double minY = areaBoundingBox.getMinY();
          this.xOrigin = minX;
          this.yOrigin = minY;
        }
        this.xYScale = geometryFactory.getScaleXY();
        if (this.xYScale == 0) {
          if (this instanceof EsriGdbGeographicCoordinateSystem) {
            this.xYScale = 10000000;
          } else {
            this.xYScale = 1000;
          }
        }
        this.zOrigin = -100000;
        this.zScale = geometryFactory.getScaleZ();
        if (this.zScale == 0) {
          this.zScale = 10000000;
        }
        this.mOrigin = -100000;
        this.mScale = 10000000;
        this.xYTolerance = 1.0 / this.xYScale;
        this.zTolerance = 1.0 / this.zScale;
        this.mTolerance = 1.0 / this.mScale;
        this.highPrecision = true;
        this.wkid = geometryFactory.getHorizontalCoordinateSystemId();
      }
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.geometryFactory == null) {
      this.geometryFactory = newGeometryFactory(this.latestWKID);
      if (!this.geometryFactory.isHasHorizontalCoordinateSystem()) {
        this.geometryFactory = newGeometryFactory(this.wkid);
      }
    }
    return this.geometryFactory;
  }

  public int getLatestWKID() {
    return this.latestWKID;
  }

  public double getLeftLongitude() {
    return this.leftLongitude;
  }

  public double getMOrigin() {
    return this.mOrigin;
  }

  public double getMScale() {
    return this.mScale;
  }

  public double getMTolerance() {
    return this.mTolerance;
  }

  public int getWKID() {
    return this.wkid;
  }

  public String getWKT() {
    return this.wkt;
  }

  public double getXOrigin() {
    return this.xOrigin;
  }

  public double getXYScale() {
    return this.xYScale;
  }

  public double getXYTolerance() {
    return this.xYTolerance;
  }

  public double getYOrigin() {
    return this.yOrigin;
  }

  public double getZOrigin() {
    return this.zOrigin;
  }

  public double getZScale() {
    return this.zScale;
  }

  public double getZTolerance() {
    return this.zTolerance;
  }

  public boolean isHighPrecision() {
    return this.highPrecision;
  }

  private GeometryFactory newGeometryFactory(final int coordinateSystemId) {
    GeometryFactory geometryFactory;
    if (this.xYScale == FLOATING_SCALE) {
      geometryFactory = GeometryFactory.fixed3d(coordinateSystemId, 0.0, 0.0, this.zScale);
    } else {
      geometryFactory = GeometryFactory.fixed3d(coordinateSystemId, this.xYScale, this.xYScale,
        this.zScale);
    }
    return geometryFactory;
  }

  public void setHighPrecision(final boolean highPrecision) {
    this.highPrecision = highPrecision;
  }

  public void setLatestWKID(final int latestWKID) {
    this.latestWKID = latestWKID;
  }

  public void setLeftLongitude(final double leftLongitude) {
    this.leftLongitude = leftLongitude;
  }

  public void setMOrigin(final double mOrigin) {
    this.mOrigin = mOrigin;
  }

  public void setMScale(final double mScale) {
    this.mScale = mScale;
  }

  public void setMTolerance(final double mTolerance) {
    this.mTolerance = mTolerance;
  }

  public void setWKID(final int wkid) {
    this.wkid = wkid;
  }

  public void setWKT(final String wkt) {
    this.wkt = wkt;
  }

  public void setXOrigin(final double xOrigin) {
    this.xOrigin = xOrigin;
  }

  public void setXYScale(final double xYScale) {
    this.xYScale = xYScale;
  }

  public void setXYTolerance(final double xYTolerance) {
    this.xYTolerance = xYTolerance;
  }

  public void setYOrigin(final double yOrigin) {
    this.yOrigin = yOrigin;
  }

  public void setZOrigin(final double zOrigin) {
    this.zOrigin = zOrigin;
  }

  public void setZScale(final double zScale) {
    this.zScale = zScale;
  }

  public void setZTolerance(final double zTolerance) {
    this.zTolerance = zTolerance;
  }

  @Override
  public String toString() {
    return this.wkt;
  }
}
