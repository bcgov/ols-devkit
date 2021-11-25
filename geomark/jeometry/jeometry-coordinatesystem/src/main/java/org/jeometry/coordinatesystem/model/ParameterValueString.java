package org.jeometry.coordinatesystem.model;

import java.security.MessageDigest;

import org.jeometry.coordinatesystem.util.Md5;

public class ParameterValueString implements ParameterValue {
  private final String value;

  public ParameterValueString(final String value) {
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getOriginalValue() {
    return (V)this.value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue() {
    return (V)this.value;
  }

  @Override
  public boolean isSame(final ParameterValue parameterValue) {
    if (parameterValue instanceof ParameterValueString) {
      final ParameterValueString stringValue = (ParameterValueString)parameterValue;
      return this.value.equalsIgnoreCase(stringValue.value);
    }
    return false;
  }

  @Override
  public String toString() {
    return this.value;
  }

  @Override
  public void updateDigest(final MessageDigest digest) {
    Md5.update(digest, this.value);
  }
}
