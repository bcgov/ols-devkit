package org.jeometry.coordinatesystem.model.unit;

import java.util.List;

import org.jeometry.coordinatesystem.model.Authority;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

public class Radian extends AngularUnit {
  private static Radian instance;

  public static Radian getInstance() {
    return instance;
  }

  public Radian(final String name, final AngularUnit baseUnit, final double conversionFactor,
    final Authority authority, final boolean deprecated) {
    super(name, baseUnit, conversionFactor, authority, deprecated);
    instance = this;
  }

  @Override
  public void addFromRadiansOperation(final List<CoordinatesOperation> operations) {
  }

  @Override
  public void addToRadiansOperation(final List<CoordinatesOperation> operations) {
  }

  @Override
  public void fromDegrees(final CoordinatesOperationPoint point) {
    point.x = Math.toRadians(point.x);
    point.y = Math.toRadians(point.y);
  }

  @Override
  public double fromRadians(final double value) {
    return value;
  }

  @Override
  public void toDegrees(final CoordinatesOperationPoint point) {
    point.x = Math.toDegrees(point.x);
    point.y = Math.toDegrees(point.y);
  }

  @Override
  public double toDegrees(final double value) {
    return Math.toDegrees(value);
  }

  @Override
  public double toNormal(final double value) {
    return Math.toDegrees(value);
  }

  @Override
  public void toRadians(final CoordinatesOperationPoint point) {
    point.x = Math.toRadians(point.x);
    point.y = Math.toRadians(point.y);
  }

  @Override
  public double toRadians(final double value) {
    return Math.toRadians(value);
  }
}
