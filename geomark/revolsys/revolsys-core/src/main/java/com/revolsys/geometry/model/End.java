package com.revolsys.geometry.model;

import java.util.List;

import com.revolsys.collection.list.Lists;
import com.revolsys.util.CaseConverter;

public enum End {
  FROM, TO, NONE;

  public static List<End> FROM_TO = Lists.newArray(FROM, TO);

  public static List<End> FROM_TO_NONE = Lists.newArray(FROM, TO, NONE);

  public static End getFrom(final Direction direction) {
    if (direction == null) {
      return null;
    } else if (direction.isForwards()) {
      return FROM;
    } else {
      return TO;
    }
  }

  public static boolean isFrom(final End end) {
    return end == FROM;
  }

  public static boolean isNone(final End end) {
    return end == NONE;
  }

  public static boolean isTo(final End end) {
    return end == TO;
  }

  public static End opposite(final End end) {
    if (end == FROM) {
      return TO;
    } else if (end == TO) {
      return FROM;
    } else {
      return null;
    }
  }

  private final String title;

  private End() {
    final String name = name();
    this.title = CaseConverter.captialize(name);
  }

  public String getTitle() {
    return this.title;
  }

  public boolean isFrom() {
    return this == FROM;
  }

  public boolean isNone() {
    return this == NONE;
  }

  public boolean isOpposite(final End end) {
    switch (this) {
      case FROM:
        return end == TO;
      case TO:
        return end == FROM;

      default:
        return false;
    }
  }

  public boolean isTo() {
    return this == TO;
  }

  public End opposite() {
    switch (this) {
      case FROM:
        return TO;
      case TO:
        return FROM;

      default:
        return NONE;
    }
  }
}
