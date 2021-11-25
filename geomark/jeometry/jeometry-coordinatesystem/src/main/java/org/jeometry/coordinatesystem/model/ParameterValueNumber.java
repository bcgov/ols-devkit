package org.jeometry.coordinatesystem.model;

import java.security.MessageDigest;

import org.jeometry.common.number.Doubles;
import org.jeometry.coordinatesystem.model.unit.UnitOfMeasure;
import org.jeometry.coordinatesystem.util.Md5;

public class ParameterValueNumber extends Number implements ParameterValue {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final double unitValue;

  private final UnitOfMeasure unit;

  private final double value;

  public ParameterValueNumber(final double unitValue) {
    this(null, unitValue);
  }

  public ParameterValueNumber(final UnitOfMeasure unit, final double unitValue) {
    this.unit = unit;
    this.unitValue = unitValue;
    if (unit == null) {
      this.value = unitValue;
    } else {
      this.value = unit.toNormal(unitValue);
    }
  }

  @Override
  public double doubleValue() {
    return this.value;
  }

  @Override
  public float floatValue() {
    return (float)this.value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getOriginalValue() {
    return (V)(Double)this.unitValue;
  }

  public UnitOfMeasure getUnit() {
    return this.unit;
  }

  public double getUnitValue() {
    return this.unitValue;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue() {
    return (V)(Double)this.value;
  }

  @Override
  public int intValue() {
    return (int)this.value;
  }

  @Override
  public boolean isSame(final ParameterValue parameterValue) {
    if (parameterValue instanceof ParameterValueNumber) {
      final ParameterValueNumber numberValue = (ParameterValueNumber)parameterValue;
      return this.value == numberValue.value;
    }
    return false;
  }

  @Override
  public long longValue() {
    return (long)this.value;
  }

  @Override
  public String toString() {
    return Doubles.toString(this.value);
  }

  @Override
  public void updateDigest(final MessageDigest digest) {
    Md5.update(digest, Math.floor(1e6 * this.value));
  }
}
