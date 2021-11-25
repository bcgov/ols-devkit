package com.revolsys.geometry.model.util;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.Triangles;

public class TriangleImpl {

  public Point p0, p1, p2;

  public TriangleImpl(final Point p0, final Point p1, final Point p2) {
    this.p0 = p0;
    this.p1 = p1;
    this.p2 = p2;
  }

  public double area() {
    return Triangles.area(this.p0, this.p1, this.p2);
  }

  public double area3D() {
    return Triangles.area3D(this.p0, this.p1, this.p2);
  }

  public Point centroid() {
    return Triangles.centroid(this.p0, this.p1, this.p2);
  }

  public Point circumcentre() {
    return Triangles.circumcentre(this.p0, this.p1, this.p2);
  }

  public Point inCentre() {
    return Triangles.inCentre(this.p0, this.p1, this.p2);
  }

  public double interpolateZ(final Point p) {
    if (p == null) {
      throw new IllegalArgumentException("Supplied point is null.");
    }
    return Triangles.interpolateZ(p, this.p0, this.p1, this.p2);
  }

  public boolean isAcute() {
    return Triangles.isAcute(this.p0, this.p1, this.p2);
  }

  public double longestSideLength() {
    return Triangles.longestSideLength(this.p0, this.p1, this.p2);
  }

  public double signedArea() {
    return Triangles.signedArea(this.p0, this.p1, this.p2);
  }

}
