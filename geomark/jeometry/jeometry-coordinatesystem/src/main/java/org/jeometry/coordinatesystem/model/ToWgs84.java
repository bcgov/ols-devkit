package org.jeometry.coordinatesystem.model;

import java.util.List;

public class ToWgs84 {

  private final double dx;

  private final double dy;

  private final double dz;

  private final double ex;

  private final double ey;

  private final double ez;

  private final double ppm;

  public ToWgs84(final List<Object> values) {
    this.dx = ((Number)values.get(0)).doubleValue();
    this.dy = ((Number)values.get(1)).doubleValue();
    this.dz = ((Number)values.get(2)).doubleValue();
    this.ex = ((Number)values.get(3)).doubleValue();
    this.ey = ((Number)values.get(4)).doubleValue();
    this.ez = ((Number)values.get(5)).doubleValue();
    this.ppm = ((Number)values.get(6)).doubleValue();
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof ToWgs84) {
      final ToWgs84 toWgs84 = (ToWgs84)object;
      if (this.dx != toWgs84.dx) {
        return false;
      } else if (this.dy != toWgs84.dy) {
        return false;
      } else if (this.dz != toWgs84.dz) {
        return false;
      } else if (this.ex != toWgs84.ex) {
        return false;
      } else if (this.ey != toWgs84.ey) {
        return false;
      } else if (this.ez != toWgs84.ez) {
        return false;
      } else if (this.ppm != toWgs84.ppm) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public double getDx() {
    return this.dx;
  }

  public double getDy() {
    return this.dy;
  }

  public double getDz() {
    return this.dz;
  }

  public double getEx() {
    return this.ex;
  }

  public double getEy() {
    return this.ey;
  }

  public double getEz() {
    return this.ez;
  }

  public double getPpm() {
    return this.ppm;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp = Double.doubleToLongBits(this.dx);
    result = prime * result + (int)(temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(this.dy);
    result = prime * result + (int)(temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(this.dz);
    result = prime * result + (int)(temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(this.ex);
    result = prime * result + (int)(temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(this.ey);
    result = prime * result + (int)(temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(this.ez);
    result = prime * result + (int)(temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(this.ppm);
    result = prime * result + (int)(temp ^ temp >>> 32);
    return result;
  }

  @Override
  public String toString() {
    return this.dx + ", " + this.dy + ", " + this.dz + ", " + this.ex + ", " + this.ey + ", "
      + this.ez + ", " + this.ppm;
  }
}
