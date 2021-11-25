package com.revolsys.gis.converter;

import java.util.HashMap;
import java.util.Map;

public class SimpleNameConverter implements NameConverter {

  private final Map<String, String> names = new HashMap<>();

  @Override
  public String convert(final String name) {
    final String newName = this.names.get(name);
    if (newName != null) {
      return newName;
    } else {
      return name;
    }
  }

  public void setName(final String name, final String newName) {
    this.names.put(name, newName);
  }
}
