package com.revolsys.collection.map;

import java.util.Properties;

public class PropertiesEx extends Properties {
  private static final long serialVersionUID = 1L;

  public PropertiesEx add(final String key, final String value) {
    setProperty(key, value);
    return this;
  }
}
