package com.revolsys.geometry.model;

import java.util.List;

import com.revolsys.collection.list.Lists;

public enum Side {
  LEFT("Left"), RIGHT("Right"), ON("On");

  public static List<Side> VALUES = Lists.newArray(LEFT, RIGHT);

  public static List<Side> ALL_VALUES = Lists.newArray(LEFT, ON, RIGHT);

  public static Side getSide(final double x1, final double y1, final double x2, final double y2,
    final double x, final double y) {
    final double orientationIndex = (x1 - x) * (y2 - y) - (y1 - y) * (x2 - x);
    if (orientationIndex < 0) {
      return RIGHT;
    } else if (orientationIndex > 0) {
      return LEFT;
    } else {
      return ON;
    }
  }

  public static Side getSide(final int x1, final int y1, final int x2, final int y2, final int x,
    final int y) {
    final long deltaX1 = x1 - x;
    final long deltaX2 = x2 - x;
    final long deltaY1 = y1 - y;
    final long deltaY2 = y2 - y;
    final long deltaX1Y2 = deltaX1 * deltaY2;
    final long deltaX2Y2 = deltaY1 * deltaX2;
    final long orientationIndex = deltaX1Y2 - deltaX2Y2;
    if (orientationIndex < 0) {
      return RIGHT;
    } else if (orientationIndex > 0) {
      return LEFT;
    } else {
      return ON;
    }
  }

  public static boolean isLeft(final Side side) {
    return side == LEFT;
  }

  public static boolean isOn(final Side side) {
    return side == ON;
  }

  public static boolean isRight(final Side side) {
    return side == RIGHT;
  }

  public static Side opposite(final Side side) {
    if (side == LEFT) {
      return RIGHT;
    } else if (side == RIGHT) {
      return LEFT;
    } else {
      return ON;
    }
  }

  private char letter;

  private String name;

  private String upperName;

  private String lowerName;

  private Side(final String name) {
    this.name = name;
    this.lowerName = name.toLowerCase();
    this.upperName = name.toUpperCase();
    this.letter = name.charAt(0);
  }

  public char getLetter() {
    return this.letter;
  }

  public String getLowerName() {
    return this.lowerName;
  }

  public String getName() {
    return this.name;
  }

  public String getUpperName() {
    return this.upperName;
  }

  public boolean isLeft() {
    return this == LEFT;
  }

  public boolean isOn() {
    return this == ON;
  }

  public boolean isRight() {
    return this == RIGHT;
  }

  public Side opposite() {
    if (this == LEFT) {
      return RIGHT;
    } else if (this == RIGHT) {
      return LEFT;
    } else {
      return ON;
    }
  }

  @Override
  public String toString() {
    return this.name;
  }
}
