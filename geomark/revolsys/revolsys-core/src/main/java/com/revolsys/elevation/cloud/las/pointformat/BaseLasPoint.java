package com.revolsys.elevation.cloud.las.pointformat;

import java.io.Serializable;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.AbstractPoint;
import com.revolsys.geometry.util.Points;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.util.Property;

public abstract class BaseLasPoint extends AbstractPoint implements LasPoint, Serializable {
  private static final long serialVersionUID = 1L;

  protected int intensity;

  private final LasPointCloud pointCloud;

  private int x = Integer.MIN_VALUE;

  private int y = Integer.MIN_VALUE;

  private int z = Integer.MIN_VALUE;

  protected int pointSourceID = 1;

  public BaseLasPoint(final LasPointCloud pointCloud) {
    this.pointCloud = pointCloud;
  }

  /**
   * Creates and returns a full copy of this {@link Point} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public BaseLasPoint clone() {
    return (BaseLasPoint)super.clone();
  }

  @Override
  public void copyCoordinates(final double[] coordinates) {
    coordinates[X] = getX();
    coordinates[Y] = getY();
    if (coordinates.length > 2) {
      coordinates[Z] = getZ();
    }
  }

  @Override
  public double distancePoint(Point point) {
    if (isEmpty()) {
      return java.lang.Double.POSITIVE_INFINITY;
    } else if (Property.isEmpty(point)) {
      return java.lang.Double.POSITIVE_INFINITY;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.convertPoint2d(geometryFactory);
      final double x = point.getX();
      final double y = point.getY();
      final double x1 = getX();
      final double y1 = this.y;
      return Points.distance(x1, y1, x, y);
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      return equals(point);
    } else {
      return false;
    }
  }

  @Override
  public <R> R findVertex(final BiFunctionDouble<R> action) {
    return action.accept(this.x, this.y);
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    action.accept(this.x, this.y);
  }

  @Override
  public void forEachVertex(final Consumer3Double action) {
    action.accept(this.x, this.y, this.z);
  }

  @Override
  public int getAxisCount() {
    return 3;
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (isEmpty()) {
      return java.lang.Double.NaN;
    } else {
      if (axisIndex == X) {
        return this.pointCloud.toDoubleX(this.x);
      } else if (axisIndex == Y) {
        return this.pointCloud.toDoubleY(this.y);
      } else if (axisIndex == Z) {
        return this.pointCloud.toDoubleZ(this.z);
      } else {
        return java.lang.Double.NaN;
      }
    }
  }

  @Override
  public double[] getCoordinates() {
    return new double[] {
      getX(), getY(), getZ()
    };
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.pointCloud.getGeometryFactory();
  }

  @Override
  public int getIntensity() {
    return this.intensity;
  }

  public LasPointCloud getPointCloud() {
    return this.pointCloud;
  }

  @Override
  public int getPointSourceID() {
    return this.pointSourceID;
  }

  @Override
  public double getX() {
    return this.pointCloud.toDoubleX(this.x);
  }

  @Override
  public int getXInt() {
    return this.x;
  }

  @Override
  public double getY() {
    return this.pointCloud.toDoubleY(this.y);
  }

  @Override
  public int getYInt() {
    return this.y;
  }

  @Override
  public double getZ() {
    return this.pointCloud.toDoubleZ(this.z);
  }

  @Override
  public int getZInt() {
    return this.z;
  }

  @Override
  public int hashCode() {
    long bits = java.lang.Double.doubleToLongBits(getX());
    bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
    return (int)bits ^ (int)(bits >> 32);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public BaseLasPoint setIntensity(final int intensity) {
    if (intensity >= 0 && intensity <= 65535) {
      this.intensity = intensity;
    } else {
      throw new IllegalArgumentException("intensity must be in range 0..65535: " + intensity);
    }
    return this;
  }

  @Override
  public BaseLasPoint setPointSourceID(final int pointSourceID) {
    if (pointSourceID >= 1 && pointSourceID <= 65535) {
      this.pointSourceID = pointSourceID;
    } else {
      throw new IllegalArgumentException(
        "pointSourceID must be in range 1..65535: " + pointSourceID);
    }

    return this;
  }

  @Override
  public BaseLasPoint setXInt(final int x) {
    this.x = x;
    return this;
  }

  @Override
  public void setXYZ(final int x, final int y, final int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override
  public BaseLasPoint setYInt(final int y) {
    this.y = y;
    return this;
  }

  @Override
  public BaseLasPoint setZInt(final int z) {
    this.z = z;
    return this;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    addToMap(map, "x", getX());
    addToMap(map, "y", getY());
    addToMap(map, "z", getZ());
    return map;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
