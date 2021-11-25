package org.jeometry.coordinatesystem.model.unit;

import org.jeometry.coordinatesystem.model.Authority;

public class ScaleUnit implements UnitOfMeasure {

  private final Authority authority;

  private final ScaleUnit baseUnit;

  private final double conversionFactor;

  private final boolean deprecated;

  private final String name;

  public ScaleUnit(final String name, final ScaleUnit baseUnit, final double conversionFactor,
    final Authority authority, final boolean deprecated) {
    this.name = name;
    this.baseUnit = baseUnit;
    this.conversionFactor = conversionFactor;
    this.authority = authority;
    this.deprecated = deprecated;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof ScaleUnit) {
      final ScaleUnit unit = (ScaleUnit)object;
      if (this.name == null && unit.name != null || !this.name.equals(unit.name)) {
        return false;
      } else if (Math.abs(this.conversionFactor - unit.conversionFactor) > 1.0e-10) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public Authority getAuthority() {
    return this.authority;
  }

  public ScaleUnit getBaseUnit() {
    return this.baseUnit;
  }

  public double getConversionFactor() {
    return this.conversionFactor;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public UnitOfMeasureType getType() {
    return UnitOfMeasureType.SCALE;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.name.hashCode();
    final long temp = Double.doubleToLongBits(this.conversionFactor);
    result = prime * result + (int)(temp ^ temp >>> 32);
    return result;
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <U extends UnitOfMeasure> ValueWithUnit<U> newValue(final double value) {
    return (ValueWithUnit<U>)new ValueWithUnit<>(this, value);
  }

  @Override
  public double toBase(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value * this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return baseValue;
    } else {
      return this.baseUnit.toBase(baseValue);
    }
  }

  @Override
  public double toNormal(final double value) {
    return value;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
