package com.revolsys.geometry.model.vertex;

import java.util.NoSuchElementException;

import com.revolsys.geometry.model.Point;

public class PointVertex extends AbstractVertex {
  private static final long serialVersionUID = 1L;

  private int vertexIndex;

  public PointVertex(final Point geometry, final int... vertexId) {
    super(geometry);
    setVertexId(vertexId);
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    final Point point = getPoint();
    return point.getCoordinate(axisIndex);
  }

  @Override
  public double getOrientaton() {
    return 0;
  }

  @Override
  public Point getPoint() {
    return (Point)getGeometry();
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      0
    };
  }

  @Override
  public int getVertexIndex() {
    return 0;
  }

  @Override
  public double getX() {
    final Point point = getPoint();
    return point.getX();
  }

  @Override
  public double getY() {
    final Point point = getPoint();
    return point.getY();
  }

  @Override
  public boolean hasNext() {
    final Point point = getPoint();
    if (point == null || point.isEmpty()) {
      return false;
    } else if (this.vertexIndex == -1) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isFrom() {
    return true;
  }

  @Override
  public Vertex next() {
    if (hasNext()) {
      this.vertexIndex++;
      return this;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing vertices not supported");
  }

  @Override
  public double setCoordinate(final int axisIndex, final double coordinate) {
    final Point point = getPoint();
    return point.setCoordinate(axisIndex, coordinate);
  }

  public void setVertexId(final int... vertexId) {
    if (vertexId.length == 1) {
      this.vertexIndex = vertexId[0];
    } else {
      this.vertexIndex = 1;
    }
  }

}
