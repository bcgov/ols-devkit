package org.jeometry.coordinatesystem.model;

public enum CoordinateSystemType {
  NONE(false), //
  COMPOUND(true), //
  ENGINEERING(true), //
  GEOCENTRIC(true), //
  GEOGRAPHIC(true), //
  PROJECTED(true), //
  VERTICAL(false) //
  ;

  private boolean horizontal;

  private String name;

  private CoordinateSystemType(final boolean horizontal) {
    this.horizontal = horizontal;
    final String name = name();
    this.name = name.substring(0, 1) + name.substring(1).toLowerCase();
  }

  public String getName() {
    return this.name;
  }

  public boolean isCompound() {
    return CoordinateSystemType.COMPOUND == this;
  }

  public boolean isEngineering() {
    return CoordinateSystemType.ENGINEERING == this;
  }

  public boolean isGeocentric() {
    return CoordinateSystemType.GEOCENTRIC == this;
  }

  public boolean isGeographic() {
    return CoordinateSystemType.GEOGRAPHIC == this;
  }

  public boolean isHorizontal() {
    return this.horizontal;
  }

  public boolean isProjected() {
    return CoordinateSystemType.PROJECTED == this;
  }

  public boolean isVertical() {
    return CoordinateSystemType.VERTICAL == this;
  }

  @Override
  public String toString() {
    return this.name;
  }

}
