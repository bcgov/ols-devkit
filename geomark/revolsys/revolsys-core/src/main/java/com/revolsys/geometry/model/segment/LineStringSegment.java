package com.revolsys.geometry.model.segment;

import java.util.NoSuchElementException;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.vertex.Vertex;

public class LineStringSegment extends AbstractLineSegment implements Segment {
  private static final long serialVersionUID = 1L;

  private int segmentIndex;

  protected final LineString lineString;

  public LineStringSegment(final LineString line, final int segmentIndex) {
    this.lineString = line;
    this.segmentIndex = segmentIndex;
  }

  @Override
  public LineStringSegment clone() {
    return (LineStringSegment)super.clone();
  }

  @Override
  public int getAxisCount() {
    return this.lineString.getAxisCount();
  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      return this.lineString.getCoordinate(this.segmentIndex + vertexIndex, axisIndex);
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
    return (V)this.lineString;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.lineString.getGeometryFactory();
  }

  @Override
  public Vertex getGeometryVertex(final int index) {
    if (index == 0) {
      return this.lineString.getVertex(this.segmentIndex);
    } else if (index == 1) {
      return this.lineString.getVertex(this.segmentIndex + 1);
    } else {
      return null;
    }
  }

  public LineString getLineString() {
    return this.lineString;
  }

  @Override
  public int[] getSegmentId() {
    return new int[] {
      this.segmentIndex
    };
  }

  @Override
  public int getSegmentIndex() {
    return this.segmentIndex;
  }

  @Override
  public double getX(final int vertexIndex, final int axisIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      return this.lineString.getX(this.segmentIndex + vertexIndex, axisIndex);
    }
  }

  @Override
  public double getY(final int vertexIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      return this.lineString.getY(this.segmentIndex + vertexIndex);
    }
  }

  @Override
  public double getZ(final int vertexIndex) {
    if (vertexIndex < 0 || vertexIndex > 1) {
      return Double.NaN;
    } else {
      return this.lineString.getZ(this.segmentIndex + vertexIndex);
    }
  }

  @Override
  public boolean hasNext() {
    final int segmentIndex = this.segmentIndex;
    if (segmentIndex + 1 < this.lineString.getSegmentCount()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isLineClosed() {
    return this.lineString.isClosed();
  }

  @Override
  public boolean isLineEnd() {
    return this.segmentIndex == this.lineString.getSegmentCount() - 1;
  }

  @Override
  public boolean isLineStart() {
    return this.segmentIndex == 0;
  }

  @Override
  public Segment next() {
    this.segmentIndex++;
    if (this.segmentIndex < this.lineString.getSegmentCount()) {
      return this;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing segments not supported");
  }

  @Override
  public void setSegmentId(final int... segmentId) {
    this.segmentIndex = segmentId[0];
  }

  public void setSegmentIndex(final int segmentIndex) {
    this.segmentIndex = segmentIndex;
  }

}
