package org.jeometry.coordinatesystem.model;

import java.math.BigDecimal;
import java.security.MessageDigest;

import org.jeometry.coordinatesystem.util.Md5;

public class ParameterValueBigDecimal extends Number implements ParameterValue {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final BigDecimal unitValue;

  private final double value;

  public ParameterValueBigDecimal(final BigDecimal unitValue) {
    this.unitValue = unitValue;
    this.value = unitValue.doubleValue();
  }

  public ParameterValueBigDecimal(final String text) {
    this(new BigDecimal(text));
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
    return (V)this.unitValue;
  }

  public BigDecimal getUnitValue() {
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
    if (parameterValue instanceof ParameterValueBigDecimal) {
      final ParameterValueBigDecimal numberValue = (ParameterValueBigDecimal)parameterValue;
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
    return this.unitValue.toString();
  }

  @Override
  public void updateDigest(final MessageDigest digest) {
    Md5.update(digest, Math.floor(1e6 * this.value));
  }
}
