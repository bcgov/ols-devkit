package com.revolsys.gis.converter;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.util.CaseConverter;

public class UpperCamelCaseNameConverter implements NameConverter {

  private final Map<String, String> names = new HashMap<>();

  @Override
  public String convert(final String name) {
    String newName = this.names.get(name);
    if (newName == null) {
      newName = CaseConverter.toUpperCamelCase(name);
    }
    return newName;
  }

  public void setName(final String name, final String newName) {
    this.names.put(name, newName);
  }
}
