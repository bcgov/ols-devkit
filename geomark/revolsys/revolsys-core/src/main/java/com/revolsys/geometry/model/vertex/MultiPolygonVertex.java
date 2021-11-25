package com.revolsys.geometry.model.vertex;

import java.util.NoSuchElementException;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;

public class MultiPolygonVertex extends AbstractVertex {
  private static final long serialVersionUID = 1L;

  private int partIndex;

  private int ringIndex;

  private int vertexIndex;

  public MultiPolygonVertex(final Polygonal polygonal, final int... vertexId) {
    super(polygonal);
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
      final LinearRing getLine = getRing();
      return getLine.getCoordinate(vertexIndex + vertexOffset, axisIndex);
    }
  }

  @Override
  public Vertex getLineNext() {
    final LineString ring = getRing();
    if (ring != null) {
      int newVertexIndex = this.vertexIndex + 1;
      if (newVertexIndex >= ring.getVertexCount() - 1) {
        newVertexIndex -= ring.getVertexCount();
      }
      if (newVertexIndex < ring.getVertexCount() - 1) {
        return new MultiPolygonVertex(getPolygonal(), this.partIndex, this.ringIndex,
          newVertexIndex);
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
      if (newVertexIndex >= 0) {
        return new MultiPolygonVertex(getPolygonal(), this.partIndex, this.ringIndex,
          newVertexIndex);
      }
    }
    return null;
  }

  @Override
  public int getPartIndex() {
    return super.getPartIndex();
  }

  public Polygon getPolygon() {
    final Polygonal polygonal = getPolygonal();
    return polygonal.getPolygon(this.partIndex);
  }

  public Polygonal getPolygonal() {
    return (Polygonal)getGeometry();
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
      this.partIndex, this.ringIndex, this.vertexIndex
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
      final Polygonal polygonal = getPolygonal();
      int partIndex = this.partIndex;
      int ringIndex = this.ringIndex;
      int vertexIndex = this.vertexIndex + 1;

      while (partIndex < polygonal.getGeometryCount()) {
        final Polygon polygon = polygonal.getPolygon(partIndex);

        while (ringIndex < polygon.getRingCount()) {
          final LinearRing ring = polygon.getRing(ringIndex);
          if (vertexIndex < ring.getVertexCount()) {
            return true;
          } else {
            ringIndex++;
            vertexIndex = 0;
          }
        }
        partIndex++;
        ringIndex = 0;
        vertexIndex = 0;
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
    final Polygonal polygonal = getPolygonal();
    this.vertexIndex++;
    while (this.partIndex < polygonal.getGeometryCount()) {
      final Polygon polygon = polygonal.getPolygon(this.partIndex);
      while (this.ringIndex < polygon.getRingCount()) {
        final LinearRing ring = polygon.getRing(this.ringIndex);
        if (this.vertexIndex < ring.getVertexCount()) {
          return this;
        } else {
          this.ringIndex++;
          this.vertexIndex = 0;
        }
      }
      this.partIndex++;
      this.ringIndex = 0;
      this.vertexIndex = 0;
    }
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing vertices not supported");
  }

  @Override
  public double setCoordinate(final int axisIndex, final double coordinate) {
    final Polygonal Polygonal = getPolygonal();
    return Polygonal.setCoordinate(this.partIndex, this.ringIndex, this.vertexIndex, axisIndex,
      coordinate);
  }

  public void setVertexId(final int... vertexId) {
    this.partIndex = vertexId[0];
    this.ringIndex = vertexId[1];
    this.vertexIndex = vertexId[2];
  }

}
