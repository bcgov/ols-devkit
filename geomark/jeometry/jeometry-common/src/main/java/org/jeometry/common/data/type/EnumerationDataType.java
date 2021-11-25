package org.jeometry.common.data.type;

import java.util.Set;

public class EnumerationDataType extends SimpleDataType {

  private final Set<String> allowedValues;

  public EnumerationDataType(final String name, final Class<?> javaClass,
    final Set<String> allowedValues) {
    super(name, javaClass);
    this.allowedValues = allowedValues;
  }

  public Set<String> getAllowedValues() {
    return this.allowedValues;
  }

}
