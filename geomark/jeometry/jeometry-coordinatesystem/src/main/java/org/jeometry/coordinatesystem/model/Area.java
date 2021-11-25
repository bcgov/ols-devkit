package org.jeometry.coordinatesystem.model;

public class Area {

  private final Authority authority;

  private final boolean deprecated;

  private final double maxX;

  private final double maxY;

  private final double minX;

  private final double minY;

  private final String name;

  public Area(final String name, final double minX, final double minY, final double maxX,
    final double maxY, final Authority authority, final boolean deprecated) {
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
    this.name = name;
    this.authority = authority;
    this.deprecated = deprecated;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Area) {
      final Area area = (Area)obj;
      if (super.equals(area)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public Authority getAuthority() {
    return this.authority;
  }

  public double getMaxX() {
    return this.maxX;
  }

  public double getMaxY() {
    return this.maxY;
  }

  public double getMinX() {
    return this.minX;
  }

  public double getMinY() {
    return this.minY;
  }

  public String getName() {
    return this.name;
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
