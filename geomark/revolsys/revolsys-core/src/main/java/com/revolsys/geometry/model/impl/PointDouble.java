package com.revolsys.geometry.model.impl;

import java.io.Serializable;
import java.util.function.Consumer;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.geometry.model.GeometryFactory;

public class PointDouble extends AbstractPoint implements Serializable {
  private static final long serialVersionUID = 1L;

  private double[] coordinates;

  public PointDouble(final double... coordinates) {
    final int axisCount = coordinates.length;
    this.coordinates = new double[axisCount];
    for (int i = 0; i < axisCount; i++) {
      double value;
      if (i < coordinates.length) {
        value = coordinates[i];
      } else {
        value = java.lang.Double.NaN;
      }
      this.coordinates[i] = value;
    }
  }

  protected PointDouble(final GeometryFactory geometryFactory, final double... coordinates) {
    if (coordinates != null && coordinates.length > 0) {
      final int axisCount = geometryFactory.getAxisCount();
      this.coordinates = new double[axisCount];
      for (int i = 0; i < axisCount; i++) {
        double value;
        if (i < coordinates.length) {
          value = geometryFactory.makePrecise(i, coordinates[i]);
        } else {
          value = java.lang.Double.NaN;
        }
        this.coordinates[i] = value;
      }
    }
  }

  @Override
  public PointDouble clone() {
    final PointDouble point = (PointDouble)super.clone();
    if (this.coordinates != null) {
      point.coordinates = this.coordinates.clone();
    }
    return point;
  }

  @Override
  public void copyCoordinates(final double[] coordinates) {
    int axisCount = this.coordinates.length;
    if (coordinates.length < axisCount) {
      axisCount = coordinates.length;
    }
    System.arraycopy(this.coordinates, 0, coordinates, 0, axisCount);
  }

  @Override
  public <R> R findVertex(final BiFunctionDouble<R> action) {
    if (isEmpty()) {
      return null;
    } else {
      final double x = this.coordinates[0];
      final double y = this.coordinates[1];
      return action.accept(x, y);
    }
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    final double x = this.coordinates[0];
    final double y = this.coordinates[1];
    action.accept(x, y);
  }

  @Override
  public void forEachVertex(final Consumer3Double action) {
    if (!isEmpty()) {
      final double x = this.coordinates[0];
      final double y = this.coordinates[1];
      double z;
      if (this.coordinates.length < 3) {
        z = java.lang.Double.NaN;
      } else {
        z = this.coordinates[2];
      }

      action.accept(x, y, z);
    }
  }

  @Override
  public void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final CoordinatesOperationPoint point, final Consumer<CoordinatesOperationPoint> action) {
    if (!isEmpty()) {
      point.setPoint(this.coordinates);
      coordinatesOperation.perform(point);
      action.accept(point);
    }
  }

  @Override
  public void forEachVertex(final CoordinatesOperationPoint point,
    final Consumer<CoordinatesOperationPoint> action) {
    if (!isEmpty()) {
      point.setPoint(this.coordinates);
      action.accept(point);
    }
  }

  @Override
  public int getAxisCount() {
    if (this.coordinates == null) {
      return 0;
    } else {
      return (byte)this.coordinates.length;
    }
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (isEmpty()) {
      return java.lang.Double.NaN;
    } else {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        return this.coordinates[axisIndex];
      } else {
        return java.lang.Double.NaN;
      }
    }
  }

  @Override
  public double[] getCoordinates() {
    if (this.coordinates == null) {
      return this.coordinates;
    } else {
      return this.coordinates.clone();
    }
  }

  @Override
  public double getX() {
    return this.coordinates[0];
  }

  @Override
  public double getY() {
    return this.coordinates[1];
  }

  @Override
  public boolean isEmpty() {
    return this.coordinates == null;
  }

}
