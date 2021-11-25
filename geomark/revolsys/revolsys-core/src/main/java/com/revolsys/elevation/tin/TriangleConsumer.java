package com.revolsys.elevation.tin;

import com.revolsys.geometry.model.Triangle;

public interface TriangleConsumer {
  void accept(double x1, double y1, double z1, double x2, double y2, double z2, double x3,
    double y3, double z3);

  default void acceptTriangle(final Triangle triangle) {
    final double x1 = triangle.getX(0);
    final double y1 = triangle.getY(0);
    final double z1 = triangle.getZ(0);

    final double x2 = triangle.getX(1);
    final double y2 = triangle.getY(1);
    final double z2 = triangle.getZ(1);

    final double x3 = triangle.getX(2);
    final double y3 = triangle.getY(2);
    final double z3 = triangle.getZ(2);

    accept(x1, y1, z1, x2, y2, z2, x3, y3, z3);
  }
}
