package com.revolsys.geometry.model.vertex;

import java.awt.geom.PathIterator;
import java.util.Iterator;

import org.jeometry.common.math.Angle;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryComponent;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.util.Property;

public interface Vertex extends Point, Iterator<Vertex>, Iterable<Vertex>, GeometryComponent {

  @Override
  Vertex clone();

  default int getAwtType() {
    if (isFrom()) {
      return PathIterator.SEG_MOVETO;
    } else {
      return PathIterator.SEG_LINETO;
    }
  }

  @Override
  default int getAxisCount() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (byte)geometryFactory.getAxisCount();
  }

  @Override
  default double getCoordinate(final int axisIndex) {
    return 0;
  }

  <V extends Geometry> V getGeometry();

  default double getLineCoordinateRelative(final int vertexOffset, final int axisIndex) {
    return Double.NaN;
  }

  default Vertex getLineNext() {
    return null;
  }

  default Vertex getLinePrevious() {
    return null;
  }

  default double getOrientaton() {
    if (isEmpty()) {
      return 0;
    } else {
      final double x = getX();
      final double y = getY();
      double angle;
      if (isTo()) {
        final double x1 = getLineCoordinateRelative(-1, 0);
        final double y1 = getLineCoordinateRelative(-1, 1);
        angle = Angle.angleDegrees(x1, y1, x, y);
      } else {
        final double x1 = getLineCoordinateRelative(1, 0);
        final double y1 = getLineCoordinateRelative(1, 1);
        angle = Angle.angleDegrees(x, y, x1, y1);
      }
      if (Double.isNaN(angle)) {
        return 0;
      } else {
        return angle;
      }
    }
  }

  default double getOrientaton(final GeometryFactory geometryFactory) {
    if (isEmpty()) {
      return 0;
    } else if (isSameCoordinateSystem(geometryFactory)) {
      return getOrientaton();
    } else {
      final Point point1 = convertPoint2d(geometryFactory);
      final double x = point1.getX();
      final double y = point1.getY();
      double angle;
      if (isTo()) {
        final Point point2 = getLinePrevious().convertPoint2d(geometryFactory);
        if (Property.hasValue(point2)) {
          final double x1 = point2.getX();
          final double y1 = point2.getY();
          angle = Angle.angleDegrees(x1, y1, x, y);
        } else {
          return 0;
        }
      } else {
        final Point point2 = getLineNext().convertPoint2d(geometryFactory);
        if (Property.hasValue(point2)) {
          final double x1 = point2.getX();
          final double y1 = point2.getY();
          angle = Angle.angleDegrees(x, y, x1, y1);
        } else {
          return 0;
        }
      }
      if (Double.isNaN(angle)) {
        return 0;
      } else {
        return angle;
      }
    }
  }

  default int getPartIndex() {
    return -1;
  }

  default int getRingIndex() {
    return -1;
  }

  int[] getVertexId();

  default int getVertexIndex() {
    final int[] vertexId = getVertexId();
    return vertexId[vertexId.length - 1];
  }

  @Override
  default boolean isEmpty() {
    return false;
  }

  default boolean isFrom() {
    return false;
  }

  default boolean isTo() {
    return false;
  }

  @Override
  default Iterator<Vertex> iterator() {
    return this;
  }
}
