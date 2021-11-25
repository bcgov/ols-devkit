package com.revolsys.elevation.tin.quadedge.intscale;

public class PointIntXYZ implements Comparable<PointIntXYZ> {
  private final int x;

  private final int y;

  private final int z;

  public PointIntXYZ(final int x, final int y) {
    this.x = x;
    this.y = y;
    this.z = Integer.MIN_VALUE;
  }

  public PointIntXYZ(final int x, final int y, final int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override
  public int compareTo(final PointIntXYZ point) {
    final int x2 = point.getX();
    final int y2 = point.getY();
    final int x1 = getX();
    final int y1 = getY();
    if (x1 < x2) {
      return -1;
    } else if (x1 > x2) {
      return 1;
    } else {
      if (y1 < y2) {
        return -1;
      } else if (y1 > y2) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public int getZ() {
    return this.z;
  }

  @Override
  public String toString() {
    return "POINT Z(" + this.x + " " + this.y + " " + this.z + ")";
  }
}
