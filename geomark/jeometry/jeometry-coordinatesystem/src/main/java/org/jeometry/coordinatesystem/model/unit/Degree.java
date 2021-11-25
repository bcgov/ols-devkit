package org.jeometry.coordinatesystem.model.unit;

import java.util.List;

import org.jeometry.coordinatesystem.model.Authority;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

public class Degree extends AngularUnit {
  private static Degree instance;

  public static Degree getInstance() {
    return instance;
  }

  public Degree(final String name, final AngularUnit baseUnit, final double conversionFactor,
    final Authority authority, final boolean deprecated) {
    super(name, baseUnit, conversionFactor, authority, deprecated);
    instance = this;
  }

  @Override
  public void addFromDegreesOperation(final List<CoordinatesOperation> operations) {
  }

  @Override
  public void addToDegreesOperation(final List<CoordinatesOperation> operations) {
  }

  @Override
  public double fromDegrees(final double value) {
    return value;
  }

  @Override
  protected void fromRadians(final CoordinatesOperationPoint point) {
    point.x = Math.toDegrees(point.x);
    point.y = Math.toDegrees(point.y);
  }

  @Override
  public double fromRadians(final double value) {
    return Math.toDegrees(value);
  }

  @Override
  public String getLabel() {
    return "°";
  }

  @Override
  public double toDegrees(final double value) {
    return value;
  }

  @Override
  public double toNormal(final double value) {
    return value;
  }

  @Override
  protected void toRadians(final CoordinatesOperationPoint point) {
    point.x = Math.toRadians(point.x);
    point.y = Math.toRadians(point.y);
  }

  @Override
  public double toRadians(final double value) {
    return Math.toRadians(value);
  }
}
