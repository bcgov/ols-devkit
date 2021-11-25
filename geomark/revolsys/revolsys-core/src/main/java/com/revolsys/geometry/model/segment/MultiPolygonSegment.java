package com.revolsys.geometry.model.segment;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.vertex.Vertex;

public class MultiPolygonSegment extends AbstractLineSegment implements Iterator<Segment>, Segment {
  private static final long serialVersionUID = 1L;

  private int partIndex;

  private int ringIndex;

  private int segmentIndex;

  private final Polygonal polygonal;

  public MultiPolygonSegment(final Polygonal polygonal, final int partIndex, final int ringIndex,
    final int segmentIndex) {
    this.polygonal = polygonal;
    this.partIndex = partIndex;
    this.ringIndex = ringIndex;
    this.segmentIndex = segmentIndex;
  }

  @Override
  public MultiPolygonSegment clone() {
    return (MultiPolygonSegment)super.clone();
  }

  @Override
  public int getAxisCount() {
    return this.polygonal.getAxisCount();
  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      final LinearRing ring = getRing();
      if (ring == null) {
        return Double.NaN;
      } else {
        return ring.getCoordinate(this.segmentIndex + vertexIndex, axisIndex);
      }
    }
  }

  @Override
  public double[] getCoordinates() {
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[axisCount * 2];
    for (int vertexIndex = 0; vertexIndex < 2; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        coordinates[vertexIndex * axisCount + axisIndex] = getCoordinate(vertexIndex, axisIndex);
      }
    }
    return coordinates;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry() {
    return (V)this.polygonal;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.polygonal.getGeometryFactory();
  }

  @Override
  public Vertex getGeometryVertex(final int index) {
    final Polygonal polygon = this.polygonal;
    if (index == 0) {
      return polygon.getVertex(this.partIndex, this.ringIndex, this.segmentIndex);
    } else if (index == 1) {
      return polygon.getVertex(this.partIndex, this.ringIndex, this.segmentIndex + 1);
    } else {
      return null;
    }
  }

  @Override
  public int getPartIndex() {
    return this.partIndex;
  }

  public Polygon getPolygon() {
    final Polygonal polygonal = this.polygonal;
    if (polygonal == null) {
      return null;
    } else {
      return polygonal.getPolygon(this.partIndex);
    }
  }

  public Polygonal getPolygonal() {
    return (Polygonal)getGeometry();
  }

  public LinearRing getRing() {
    final Polygon polygon = getPolygon();
    if (polygon == null) {
      return null;
    } else {
      return polygon.getRing(this.ringIndex);
    }
  }

  @Override
  public int getRingIndex() {
    return this.ringIndex;
  }

  @Override
  public int[] getSegmentId() {
    return new int[] {
      this.partIndex, this.ringIndex, this.segmentIndex
    };
  }

  @Override
  public int getSegmentIndex() {
    return this.segmentIndex;
  }

  @Override
  public double getX(final int vertexIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      final LinearRing ring = getRing();
      if (ring == null) {
        return Double.NaN;
      } else {
        return ring.getX(this.segmentIndex + vertexIndex);
      }
    }
  }

  @Override
  public double getY(final int vertexIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      final LinearRing ring = getRing();
      if (ring == null) {
        return Double.NaN;
      } else {
        return ring.getY(this.segmentIndex + vertexIndex);
      }
    }
  }

  @Override
  public double getZ(final int vertexIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      final LinearRing ring = getRing();
      if (ring == null) {
        return Double.NaN;
      } else {
        return ring.getZ(this.segmentIndex + vertexIndex);
      }
    }
  }

  @Override
  public boolean hasNext() {
    final Polygonal polygonal = this.polygonal;
    if (polygonal.isEmpty()) {
      return false;
    } else {
      int partIndex = this.partIndex;
      int ringIndex = this.ringIndex;
      int segmentIndex = this.segmentIndex + 1;
      while (partIndex < polygonal.getGeometryCount()) {
        final Polygon polygon = getPolygon();
        while (ringIndex < polygon.getRingCount()) {
          final LinearRing ring = polygon.getRing(ringIndex);
          if (segmentIndex < ring.getSegmentCount()) {
            return true;
          } else {
            ringIndex++;
            segmentIndex = 0;
          }
        }
        partIndex++;
        ringIndex = 0;
        segmentIndex = 0;
      }
      return false;
    }
  }

  @Override
  public boolean isLineClosed() {
    return true;
  }

  @Override
  public boolean isLineEnd() {
    final LineString line = getRing();
    return this.segmentIndex == line.getSegmentCount();
  }

  @Override
  public boolean isLineStart() {
    return this.segmentIndex == 0;
  }

  @Override
  public Segment next() {
    this.segmentIndex++;
    final Polygonal polygonal = this.polygonal;
    final int geometryCount = polygonal.getGeometryCount();
    while (this.partIndex < geometryCount) {
      final Polygon polygon = getPolygon();
      final int ringCount = polygon.getRingCount();
      while (this.ringIndex < ringCount) {
        final LinearRing ring = polygon.getRing(this.ringIndex);
        if (this.segmentIndex < ring.getSegmentCount()) {
          return this;
        } else {
          this.ringIndex++;
          this.segmentIndex = 0;
        }
      }
      this.partIndex++;
      this.ringIndex = 0;
      this.segmentIndex = 0;
    }
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing segments not supported");
  }

  @Override
  public void setSegmentId(final int... segmentId) {
    this.partIndex = segmentId[0];
    this.ringIndex = segmentId[1];
    this.segmentIndex = segmentId[2];
  }
}
