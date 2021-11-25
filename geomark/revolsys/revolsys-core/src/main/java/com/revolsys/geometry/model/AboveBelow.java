package com.revolsys.geometry.model;

import java.util.List;

import com.revolsys.collection.list.Lists;

public enum AboveBelow {
  ABOVE("Above"), BELOW("Below"), ON("On");

  public static List<AboveBelow> VALUES = Lists.newArray(ABOVE, BELOW);

  public static boolean isAbove(final AboveBelow side) {
    return side == ABOVE;
  }

  public static boolean isBelow(final AboveBelow side) {
    return side == BELOW;
  }

  public static boolean isOn(final AboveBelow side) {
    return side == ON;
  }

  public static AboveBelow opposite(final AboveBelow side) {
    if (side == ABOVE) {
      return BELOW;
    } else if (side == BELOW) {
      return ABOVE;
    } else {
      return ON;
    }
  }

  private char letter;

  private String name;

  private String upperName;

  private AboveBelow(final String name) {
    this.name = name;
    this.upperName = name.toUpperCase();
    this.letter = name.charAt(0);
  }

  public char getLetter() {
    return this.letter;
  }

  public String getName() {
    return this.name;
  }

  public String getUpperName() {
    return this.upperName;
  }

  public boolean isAbove() {
    return this == ABOVE;
  }

  public boolean isBelow() {
    return this == BELOW;
  }

  public boolean isOn() {
    return this == ON;
  }

  public AboveBelow opposite() {
    if (this == ABOVE) {
      return BELOW;
    } else if (this == BELOW) {
      return ABOVE;
    } else {
      return ON;
    }
  }

  @Override
  public String toString() {
    return this.name;
  }
}
