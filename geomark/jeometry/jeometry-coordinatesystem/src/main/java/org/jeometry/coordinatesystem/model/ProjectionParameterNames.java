package org.jeometry.coordinatesystem.model;

import java.util.Map;
import java.util.TreeMap;

public class ProjectionParameterNames {

  private static final Map<String, String> ALIASES = new TreeMap<>();

  static {
  }

  public static String getParameterName(String name) {
    name = name.toLowerCase().replaceAll(" ", "_");
    String alias = ALIASES.get(name);
    if (alias == null) {
      alias = name.intern();
      ALIASES.put(alias, alias);
      // System.out.println(alias);
      return alias;
    } else {
      return alias;
    }
  }
}
