package org.jeometry.coordinatesystem.model;

import java.security.MessageDigest;
import java.util.Map;

import org.jeometry.coordinatesystem.model.unit.UnitOfMeasure;
import org.jeometry.coordinatesystem.util.Md5;

public interface ParameterName extends Comparable<ParameterName> {
  @Override
  default int compareTo(final ParameterName parameterName) {
    final String normalizedName1 = getNormalizedName();
    final String normalizedName2 = parameterName.getNormalizedName();
    return normalizedName1.compareTo(normalizedName2);
  }

  default ParameterValue getDefaultValue() {
    return null;
  }

  int getId();

  String getName();

  default String getNormalizedName() {
    final String name = getName();
    return ParameterNames.normalizeName(name);
  }

  UnitOfMeasure getUnitOfMeasure();

  <V> V getValue(Map<ParameterName, Object> parameters);

  default ParameterValueNumber newParameterValue(final double value) {
    final UnitOfMeasure unitOfMeasure = getUnitOfMeasure();
    return new ParameterValueNumber(unitOfMeasure, value);
  }

  default void updateDigest(final MessageDigest digest) {
    final String name = getName();
    Md5.update(digest, name.toLowerCase());
  }
}
