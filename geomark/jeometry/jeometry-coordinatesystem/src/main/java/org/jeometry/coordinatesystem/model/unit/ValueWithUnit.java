package org.jeometry.coordinatesystem.model.unit;

public class ValueWithUnit<U extends UnitOfMeasure> {

  private final double value;

  private final U unitOfMeasure;

  public ValueWithUnit(final U unitOfMeasure, final double value) {
    this.unitOfMeasure = unitOfMeasure;
    this.value = value;
  }

  public U getUnitOfMeasure() {
    return this.unitOfMeasure;
  }

  public double getValue() {
    return this.value;
  }
}
