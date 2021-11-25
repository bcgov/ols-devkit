package org.jeometry.coordinatesystem.operation;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.number.Doubles;

public class CoordinatesOperationPoint {
  public double x;

  public double y;

  public double z;

  public double m;

  public CoordinatesOperationPoint() {
    this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
  }

  public CoordinatesOperationPoint(final double x, final double y) {
    this.x = x;
    this.y = y;
    this.z = Double.NaN;
    this.m = Double.NaN;
  }

  public CoordinatesOperationPoint(final double x, final double y, final double z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.m = Double.NaN;
  }

  public CoordinatesOperationPoint(final double x, final double y, final double z, final double m) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.m = m;
  }

  public void apply2d(final BiConsumerDouble action) {
    action.accept(this.x, this.y);
  }

  public void copyCoordinatesTo(final double[] coordinates) {
    final int axisCount = coordinates.length;
    coordinates[0] = this.x;
    coordinates[1] = this.y;
    if (axisCount > 2) {
      coordinates[2] = this.z;
    }
    if (axisCount > 3) {
      coordinates[3] = this.m;
    }
  }

  public void copyCoordinatesTo(final double[] coordinates, final int axisCount) {
    coordinates[0] = this.x;
    coordinates[1] = this.y;
    if (axisCount > 2) {
      coordinates[2] = this.z;
    }
    if (axisCount > 3) {
      coordinates[3] = this.m;
    }
  }

  public void copyCoordinatesTo(final double[] coordinates, final int offset, final int axisCount) {
    coordinates[offset] = this.x;
    coordinates[offset + 1] = this.y;
    if (axisCount > 2) {
      coordinates[offset + 2] = this.z;
    }
    if (axisCount > 3) {
      coordinates[offset + 3] = this.m;
    }
  }

  public double getM() {
    return this.m;
  }

  public double getX() {
    return this.x;
  }

  public double getY() {
    return this.y;
  }

  public double getZ() {
    return this.z;
  }

  public boolean isEmpty() {
    return false;
  }

  public void resetPoint(final double x, final double y, final double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public void setM(final double m) {
    this.m = m;
  }

  public void setPoint(final double x, final double y) {
    this.x = x;
    this.y = y;
    this.z = Double.NaN;
    this.m = Double.NaN;
  }

  public void setPoint(final double x, final double y, final double z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.m = Double.NaN;
  }

  public void setPoint(final double[] coordinates) {
    this.x = coordinates[0];
    this.y = coordinates[1];
    if (coordinates.length > 2) {
      this.z = coordinates[2];
      if (coordinates.length > 3) {
        this.m = coordinates[3];
      }
    }
  }

  public void setPoint(final double[] coordinates, final int offset, final int axisCount) {
    this.x = coordinates[offset];
    this.y = coordinates[offset + 1];
    if (axisCount > 2) {
      this.z = coordinates[offset + 2];
      if (axisCount > 3) {
        this.m = coordinates[offset + 3];
      }
    }
  }

  public void setX(final double x) {
    this.x = x;
  }

  public void setY(final double y) {
    this.y = y;
  }

  public void setZ(final double z) {
    this.z = z;
  }

  @Override
  public String toString() {
    if (Double.isFinite(this.m)) {
      return "POINT ZM(" + Doubles.toString(this.x) + " " + Doubles.toString(this.y) + " " + this.z
        + " " + this.m + ")";
    } else if (Double.isFinite(this.z)) {
      return "POINT Z(" + Doubles.toString(this.x) + " " + Doubles.toString(this.y) + " " + this.z
        + ")";
    } else {
      return "POINT(" + Doubles.toString(this.x) + " " + Doubles.toString(this.y) + ")";
    }
  }
}
