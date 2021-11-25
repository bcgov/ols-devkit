package org.jeometry.coordinatesystem.model;

import java.util.Map;

import org.jeometry.coordinatesystem.model.unit.UnitOfMeasure;

public class MultiParameterName implements ParameterName {

  private final ParameterName[] parameterNames;

  private Double defaultValue;

  private final String name;

  private final String normalizedName;

  public MultiParameterName(final Double defaultValue, final ParameterName... parameterNames) {
    this(parameterNames);
    this.defaultValue = defaultValue;
  }

  public MultiParameterName(final ParameterName... parameterNames) {
    this.parameterNames = parameterNames;
    this.name = parameterNames[0].getName();
    this.normalizedName = parameterNames[0].getNormalizedName();
    for (final ParameterName parameterName : parameterNames) {
      if (parameterName instanceof SingleParameterName) {
        final SingleParameterName singleName = (SingleParameterName)parameterName;
        singleName.setNormalizedName(this.normalizedName);
      }
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof SingleParameterName) {
      final SingleParameterName singleName = (SingleParameterName)object;
      for (final ParameterName parameterName : this.parameterNames) {
        if (singleName.equals(parameterName)) {
          return true;
        }
      }
    } else if (object instanceof MultiParameterName) {
      final MultiParameterName multiName = (MultiParameterName)object;
      for (final ParameterName parameterName : multiName.parameterNames) {
        if (equals(parameterName)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public ParameterValue getDefaultValue() {
    final Double value = this.defaultValue;
    if (value == null) {
      return null;
    } else {
      return newParameterValue(value);
    }
  }

  @Override
  public int getId() {
    return this.parameterNames[0].getId();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getNormalizedName() {
    return this.normalizedName;
  }

  @Override
  public UnitOfMeasure getUnitOfMeasure() {
    return this.parameterNames[0].getUnitOfMeasure();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Map<ParameterName, Object> parameters) {
    for (final ParameterName parameterName : this.parameterNames) {
      final Object value = parameters.get(parameterName);
      if (value != null) {
        return (V)value;
      }
    }
    return null;
  }

  @Override
  public int hashCode() {
    return this.normalizedName.hashCode();
  }

  @Override
  public String toString() {
    return this.name;
  }
}
