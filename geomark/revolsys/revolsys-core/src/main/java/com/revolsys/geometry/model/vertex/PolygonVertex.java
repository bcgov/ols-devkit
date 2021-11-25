package com.revolsys.geometry.model.vertex;

import java.util.NoSuchElementException;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;

public class PolygonVertex extends AbstractVertex {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private int ringIndex;

  private int vertexIndex;

  public PolygonVertex(final Polygon polygon, final int... vertexId) {
    super(polygon);
    setVertexId(vertexId);
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    final Polygon polygon = getPolygon();
    final LinearRing ring = polygon.getRing(this.ringIndex);
    if (ring == null) {
      return java.lang.Double.NaN;
    } else {
      return ring.getCoordinate(this.vertexIndex, axisIndex);
    }
  }

  @Override
  public double getLineCoordinateRelative(final int vertexOffset, final int axisIndex) {
    if (isEmpty()) {
      return java.lang.Double.NaN;
    } else {
      final int vertexIndex = getVertexIndex();
      final LinearRing line = getRing();
      int newIndex = vertexIndex + vertexOffset;
      if (newIndex < 0) {
        final int vertexCount = line.getVertexCount();
        newIndex = vertexCount - 1 + newIndex;
        if (newIndex < 0) {
          return java.lang.Double.NaN;
        }
      }
      return line.getCoordinate(newIndex, axisIndex);
    }
  }

  @Override
  public Vertex getLineNext() {
    final LineString ring = getRing();
    if (ring != null) {
      int newVertexIndex = this.vertexIndex + 1;
      final int vertexCount = ring.getVertexCount();
      if (newVertexIndex == vertexCount - 1) {
        newVertexIndex = 1;
      } else if (newVertexIndex < vertexCount - 1) {
        return new PolygonVertex(getPolygon(), this.ringIndex, newVertexIndex);
      }
    }
    return null;
  }

  @Override
  public Vertex getLinePrevious() {
    final LineString ring = getRing();
    if (ring != null) {
      int newVertexIndex = this.vertexIndex - 1;
      if (newVertexIndex == -1) {
        newVertexIndex = ring.getVertexCount() - 2;
      }
      return new PolygonVertex(getPolygon(), this.ringIndex, newVertexIndex);
    }
    return null;
  }

  public Polygon getPolygon() {
    return (Polygon)getGeometry();
  }

  public LinearRing getRing() {
    final Polygon polygon = getPolygon();
    return polygon.getRing(this.ringIndex);

  }

  @Override
  public int getRingIndex() {
    return this.ringIndex;
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      this.ringIndex, this.vertexIndex
    };
  }

  @Override
  public int getVertexIndex() {
    return this.vertexIndex;
  }

  @Override
  public double getX() {
    final Polygon polygon = getPolygon();
    final LinearRing ring = polygon.getRing(this.ringIndex);
    if (ring == null) {
      return java.lang.Double.NaN;
    } else {
      return ring.getX(this.vertexIndex);
    }
  }

  @Override
  public double getY() {
    final Polygon polygon = getPolygon();
    final LinearRing ring = polygon.getRing(this.ringIndex);
    if (ring == null) {
      return java.lang.Double.NaN;
    } else {
      return ring.getY(this.vertexIndex);
    }
  }

  @Override
  public boolean hasNext() {
    if (getGeometry().isEmpty()) {
      return false;
    } else {
      final Polygon polygon = getPolygon();
      int ringIndex = this.ringIndex;
      int vertexIndex = this.vertexIndex;
      while (ringIndex < polygon.getRingCount()) {
        final LinearRing ring = polygon.getRing(ringIndex);
        if (vertexIndex + 1 < ring.getVertexCount()) {
          return true;
        } else {
          ringIndex++;
          vertexIndex = 0;
        }
      }
      return false;
    }
  }

  @Override
  public boolean isFrom() {
    return getVertexIndex() == 0;
  }

  @Override
  public boolean isTo() {
    final int vertexIndex = getVertexIndex();
    final LineString ring = getRing();
    final int lastVertexIndex = ring.getVertexCount() - 1;
    return vertexIndex == lastVertexIndex;
  }

  @Override
  public Vertex next() {
    final Polygon polygon = getPolygon();
    this.vertexIndex++;
    while (this.ringIndex < polygon.getRingCount()) {
      final LinearRing ring = polygon.getRing(this.ringIndex);
      if (this.vertexIndex < ring.getVertexCount()) {
        return this;
      } else {
        this.ringIndex++;
        this.vertexIndex = 0;
      }
    }
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing vertices not supported");
  }

  @Override
  public double setCoordinate(final int axisIndex, final double coordinate) {
    final Polygon polygon = getPolygon();
    return polygon.setCoordinate(this.ringIndex, this.vertexIndex, axisIndex, coordinate);
  }

  public void setVertexId(final int... vertexId) {
    this.ringIndex = vertexId[0];
    this.vertexIndex = vertexId[1];
  }
}
