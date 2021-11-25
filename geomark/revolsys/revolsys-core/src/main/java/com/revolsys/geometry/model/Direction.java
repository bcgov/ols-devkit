package com.revolsys.geometry.model;

import java.util.List;

import com.revolsys.collection.list.Lists;

public enum Direction {
  BACKWARDS, FORWARDS, NONE;

  public static List<Direction> VALUES = Lists.newArray(FORWARDS, BACKWARDS);

  public static boolean isBackwards(final Direction direction) {
    return direction == BACKWARDS;
  }

  public static boolean isForwards(final Direction direction) {
    return direction == FORWARDS;
  }

  public static Direction opposite(final Direction direction) {
    if (direction == FORWARDS) {
      return BACKWARDS;
    } else if (direction == BACKWARDS) {
      return FORWARDS;
    } else {
      return null;
    }
  }

  public boolean isBackwards() {
    return this == BACKWARDS;
  }

  public boolean isForwards() {
    return this == FORWARDS;
  }

  public boolean isNone() {
    return this == NONE;
  }

  public boolean isOpposite(final Direction direction) {
    if (direction == null || direction.isNone()) {
      return false;
    } else {
      return isForwards() != direction.isForwards();
    }
  }

  public Direction opposite() {
    if (isForwards()) {
      return BACKWARDS;
    } else if (isBackwards()) {
      return FORWARDS;
    } else {
      return NONE;
    }
  }
}
