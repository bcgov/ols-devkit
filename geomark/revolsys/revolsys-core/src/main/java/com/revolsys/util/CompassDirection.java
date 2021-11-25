package com.revolsys.util;

import java.util.HashMap;
import java.util.Map;

public enum CompassDirection {
  E("East"), //
  N("North"), //
  NE("North East"), //
  NW("North West"), //
  S("South"), //
  SE("South East"), //
  SW("South West"), //
  W("West");

  private static final Map<String, CompassDirection> ALIAS_MAP = new HashMap<>();

  static {
    for (final CompassDirection direction : values()) {
      final String name = direction.name();
      final String description = direction.getDescription();

      addNameAlias(name, direction);
      addNameAlias(name.toLowerCase(), direction);

      addDescriptionAlias(description, direction);
      addDescriptionAlias(description.toLowerCase(), direction);
      addDescriptionAlias(description.toLowerCase(), direction);
    }
  }

  private static void addDescriptionAlias(final String description,
    final CompassDirection direction) {
    ALIAS_MAP.put(description, direction);
    ALIAS_MAP.put(description.replace(" ", ""), direction);
  }

  private static void addNameAlias(final String name, final CompassDirection direction) {
    ALIAS_MAP.put(name, direction);
    if (name.length() == 2) {
      ALIAS_MAP.put(name.substring(0, 0) + " " + name.substring(1), direction);
    }
  }

  public static CompassDirection getDirection(final String name) {
    final CompassDirection direction = ALIAS_MAP.get(name);
    if (direction == null && name != null) {
      return ALIAS_MAP.get(name.toUpperCase());
    }
    return direction;
  }

  private String description;

  private CompassDirection(final String description) {
    this.description = description;
  }

  public String getDescription() {
    return this.description;
  }

  public String getName() {
    return name();

  }
}
