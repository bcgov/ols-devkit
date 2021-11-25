package com.revolsys.geometry.model.segment;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.vertex.Vertex;

public class GeometryCollectionSegment extends AbstractLineSegment implements Segment {
  private static final long serialVersionUID = 1L;

  private final Geometry geometryCollection;

  private int partIndex = -1;

  private Segment segment;

  public GeometryCollectionSegment(final Geometry geometryCollection, final int... segmentId) {
    super();
    this.geometryCollection = geometryCollection;
    setSegmentId(segmentId);
  }

  @Override
  public GeometryCollectionSegment clone() {
    return (GeometryCollectionSegment)super.clone();
  }

  @Override
  public int getAxisCount() {
    return this.geometryCollection.getAxisCount();
  }

  @Override
  public double getCoordinate(final int index, final int axisIndex) {
    if (this.segment == null) {
      return Double.NaN;
    } else {
      return this.segment.getCoordinate(index, axisIndex);
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
    return (V)this.geometryCollection;
  }

  public Geometry getGeometryCollection() {
    return getGeometry();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryCollection.getGeometryFactory();
  }

  @Override
  public Vertex getGeometryVertex(final int index) {
    final Geometry geometryCollection = this.geometryCollection;
    if (index == 0) {
      getSegmentId().clone();
      return geometryCollection.getVertex(getSegmentId());
    } else if (index == 1) {
      final int[] segmentId = getSegmentId();
      segmentId[segmentId.length - 1]++;
      return geometryCollection.getVertex(segmentId);
    } else {
      return null;
    }
  }

  @Override
  public int getPartIndex() {
    return this.partIndex;
  }

  @Override
  public int[] getSegmentId() {
    if (this.partIndex < 0) {
      return new int[] {
        -1
      };
    } else if (this.segment == null) {
      return new int[] {
        this.partIndex
      };
    } else {
      final int[] partSegmentId = this.segment.getSegmentId();
      final int[] segmentId = new int[partSegmentId.length + 1];
      segmentId[0] = this.partIndex;
      System.arraycopy(partSegmentId, 0, segmentId, 1, partSegmentId.length);
      return segmentId;
    }
  }

  @Override
  public double getX(final int vertexIndex) {
    if (this.segment == null) {
      return Double.NaN;
    } else {
      return this.segment.getX(vertexIndex);
    }
  }

  @Override
  public double getY(final int vertexIndex) {
    if (this.segment == null) {
      return Double.NaN;
    } else {
      return this.segment.getY(vertexIndex);
    }

  }

  @Override
  public double getZ(final int vertexIndex) {
    if (this.segment == null) {
      return Double.NaN;
    } else {
      return this.segment.getZ(vertexIndex);
    }
  }

  @Override
  public boolean hasNext() {
    if (this.partIndex == -2) {
      return false;
    } else {
      final Geometry geometryCollection = this.geometryCollection;
      int partIndex = this.partIndex;
      Segment segment = this.segment;
      if (segment != null && !segment.hasNext()) {
        partIndex++;
        segment = null;
      }
      while (segment == null && partIndex < geometryCollection.getGeometryCount()) {
        if (partIndex >= 0) {
          final Geometry part = geometryCollection.getGeometry(partIndex);
          if (part != null) {
            final Iterator<Segment> segmentIterator = part.segments().iterator();
            if (segmentIterator.hasNext()) {
              return true;
            } else {
              segment = null;
            }
          }
        }
        if (partIndex > -2) {
          partIndex++;
        }
      }
      if (segment == null) {
        return false;
      } else {
        return segment.hasNext();
      }
    }
  }

  @Override
  public boolean isLineClosed() {
    return this.segment.isLineClosed();
  }

  @Override
  public boolean isLineEnd() {
    if (this.segment == null) {
      return false;
    } else {
      return this.segment.isLineEnd();
    }
  }

  @Override
  public boolean isLineStart() {
    if (this.segment == null) {
      return false;
    } else {
      return this.segment.isLineStart();
    }
  }

  @Override
  public Segment next() {
    if (this.partIndex == -2) {
      throw new NoSuchElementException();
    } else {
      final Geometry geometryCollection = this.geometryCollection;
      if (this.segment != null && !this.segment.hasNext()) {
        this.partIndex++;
        this.segment = null;
      }
      while (this.segment == null && this.partIndex < geometryCollection.getGeometryCount()) {
        if (this.partIndex >= 0) {
          final Geometry part = geometryCollection.getGeometry(this.partIndex);
          if (part != null) {
            final Iterator<Segment> segmentIterator = part.segments().iterator();
            if (segmentIterator.hasNext()) {
              this.segment = segmentIterator.next();
              return this.segment;
            } else {
              this.segment = null;
            }
          }
        }
        if (this.partIndex > -2) {
          this.partIndex++;
        }
      }
      if (this.segment != null && this.segment.hasNext()) {
        return this.segment.next();
      } else {
        throw new NoSuchElementException();
      }
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing vertices not supported");
  }

  @Override
  public void setSegmentId(final int... segmentId) {
    this.segment = null;
    if (segmentId.length > 0) {
      this.partIndex = segmentId[0];
      final Geometry geometryCollection = this.geometryCollection;
      if (this.partIndex >= 0 && this.partIndex < geometryCollection.getGeometryCount()) {
        final Geometry part = geometryCollection.getGeometry(this.partIndex);
        if (part != null) {
          final int[] partSegmentId = new int[segmentId.length - 1];
          System.arraycopy(segmentId, 1, partSegmentId, 0, partSegmentId.length);
          this.segment = part.getSegment(partSegmentId);
        }
      }
    } else {
      this.partIndex = -2;
    }
  }
}
