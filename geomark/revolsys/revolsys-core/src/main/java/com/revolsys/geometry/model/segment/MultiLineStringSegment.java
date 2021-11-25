package com.revolsys.geometry.model.segment;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.vertex.Vertex;

public class MultiLineStringSegment extends AbstractLineSegment
  implements Iterator<Segment>, Segment {
  private static final long serialVersionUID = 1L;

  private final Lineal lineal;

  private int partIndex;

  private int segmentIndex;

  public MultiLineStringSegment(final Lineal lineal, final int partIndex, final int segmentIndex) {
    super();
    this.lineal = lineal;
    this.partIndex = partIndex;
    this.segmentIndex = segmentIndex;
  }

  @Override
  public MultiLineStringSegment clone() {
    return (MultiLineStringSegment)super.clone();
  }

  @Override
  public int getAxisCount() {
    return this.lineal.getAxisCount();
  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      final LineString part = getPart();
      if (part == null) {
        return Double.NaN;
      } else {
        return part.getCoordinate(this.segmentIndex + vertexIndex, axisIndex);
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
    return (V)this.lineal;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.lineal.getGeometryFactory();
  }

  @Override
  public Vertex getGeometryVertex(final int index) {
    final Lineal line = this.lineal;
    if (index == 0) {
      return line.getVertex(this.partIndex, this.segmentIndex);
    } else if (index == 1) {
      return line.getVertex(this.partIndex, this.segmentIndex + 1);
    } else {
      return null;
    }
  }

  public Lineal getLineal() {
    return this.lineal;
  }

  public LineString getPart() {
    final Lineal multiLine = this.lineal;
    if (multiLine == null) {
      return null;
    } else {
      return multiLine.getGeometry(this.partIndex);
    }
  }

  @Override
  public int getPartIndex() {
    return this.partIndex;
  }

  @Override
  public int[] getSegmentId() {
    return new int[] {
      this.partIndex, this.segmentIndex
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
      final LineString part = getPart();
      if (part == null) {
        return Double.NaN;
      } else {
        return part.getX(this.segmentIndex + vertexIndex);
      }
    }
  }

  @Override
  public double getY(final int vertexIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      final LineString part = getPart();
      if (part == null) {
        return Double.NaN;
      } else {
        return part.getY(this.segmentIndex + vertexIndex);
      }
    }
  }

  @Override
  public double getZ(final int vertexIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      final LineString part = getPart();
      if (part == null) {
        return Double.NaN;
      } else {
        return part.getZ(this.segmentIndex + vertexIndex);
      }
    }
  }

  @Override
  public boolean hasNext() {
    if (getGeometry().isEmpty()) {
      return false;
    } else {
      final Lineal line = this.lineal;
      int partIndex = this.partIndex;
      int segmentIndex = this.segmentIndex + 1;
      final int geometryCount = line.getGeometryCount();
      while (partIndex < geometryCount) {
        final LineString part = line.getGeometry(partIndex);
        final int segmentCount = part.getSegmentCount();
        if (segmentIndex < segmentCount) {
          return true;
        } else {
          partIndex++;
          segmentIndex = 0;
        }
      }
      return false;
    }
  }

  @Override
  public boolean isLineClosed() {
    return getPart().isClosed();
  }

  @Override
  public boolean isLineEnd() {
    final LineString line = getPart();
    return this.segmentIndex == line.getSegmentCount() - 1;
  }

  @Override
  public boolean isLineStart() {
    return this.segmentIndex == 0;
  }

  @Override
  public Segment next() {
    final Lineal lineal = this.lineal;
    this.segmentIndex++;
    while (this.partIndex < lineal.getGeometryCount()) {
      final LineString part = getPart();
      if (this.segmentIndex < part.getSegmentCount()) {
        return this;
      } else {
        this.partIndex++;
        this.segmentIndex = 0;
      }
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
    this.segmentIndex = segmentId[1];
  }
}
