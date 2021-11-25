package com.revolsys.geometry.model.vertex;

import java.util.NoSuchElementException;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;

public class MultiLineStringVertex extends AbstractVertex {
  private static final long serialVersionUID = 1L;

  private int partIndex;

  private int vertexIndex;

  public MultiLineStringVertex(final Lineal lineal, final int... vertexId) {
    super(lineal);
    setVertexId(vertexId);
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    final LineString lineString = getLineString();
    if (lineString == null) {
      return java.lang.Double.NaN;
    } else {
      return lineString.getCoordinate(this.vertexIndex, axisIndex);
    }
  }

  public Lineal getLineal() {
    return (Lineal)getGeometry();
  }

  @Override
  public double getLineCoordinateRelative(final int vertexOffset, final int axisIndex) {
    if (isEmpty()) {
      return java.lang.Double.NaN;
    } else {
      final int vertexIndex = getVertexIndex();
      final LineString line = getLineString();
      return line.getCoordinate(vertexIndex + vertexOffset, axisIndex);
    }
  }

  @Override
  public Vertex getLineNext() {
    final LineString line = getLineString();
    if (line != null) {
      final int newVertexIndex = this.vertexIndex + 1;
      if (newVertexIndex < line.getVertexCount()) {
        final Lineal lineal = getLineal();
        return new MultiLineStringVertex(lineal, this.partIndex, newVertexIndex);
      }
    }
    return null;
  }

  @Override
  public Vertex getLinePrevious() {
    final LineString line = getLineString();
    if (line != null) {
      final int newVertexIndex = this.vertexIndex - 1;
      if (newVertexIndex >= 0) {
        return new MultiLineStringVertex(getLineal(), this.partIndex, newVertexIndex);
      }
    }
    return null;
  }

  public LineString getLineString() {
    final Lineal lineal = getLineal();
    return lineal.getLineString(this.partIndex);
  }

  @Override
  public int getPartIndex() {
    return super.getPartIndex();
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      this.partIndex, this.vertexIndex
    };
  }

  @Override
  public int getVertexIndex() {
    return this.vertexIndex;
  }

  @Override
  public double getX() {
    final LineString lineString = getLineString();
    if (lineString == null) {
      return java.lang.Double.NaN;
    } else {
      return lineString.getX(this.vertexIndex);
    }
  }

  @Override
  public double getX(final int axisIndex) {
    final LineString lineString = getLineString();
    if (lineString == null) {
      return java.lang.Double.NaN;
    } else {
      return lineString.getX(this.vertexIndex);
    }
  }

  @Override
  public double getY() {
    final LineString lineString = getLineString();
    if (lineString == null) {
      return java.lang.Double.NaN;
    } else {
      return lineString.getY(this.vertexIndex);
    }
  }

  @Override
  public double getY(final int axisIndex) {
    final LineString lineString = getLineString();
    if (lineString == null) {
      return java.lang.Double.NaN;
    } else {
      return lineString.getY(this.vertexIndex);
    }
  }

  @Override
  public boolean hasNext() {
    if (getGeometry().isEmpty()) {
      return false;
    } else {
      final Lineal lineal = getLineal();
      int partIndex = this.partIndex;
      int vertexIndex = this.vertexIndex + 1;

      while (partIndex < lineal.getGeometryCount()) {
        final LineString lineString = lineal.getLineString(partIndex);

        if (vertexIndex < lineString.getVertexCount()) {
          return true;
        } else {
          partIndex++;
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
    final LineString lineString = getLineString();
    final int lastVertexIndex = lineString.getVertexCount() - 1;
    return vertexIndex == lastVertexIndex;
  }

  @Override
  public Vertex next() {
    final Lineal lineal = getLineal();
    this.vertexIndex++;
    while (this.partIndex < lineal.getGeometryCount()) {
      final LineString lineString = lineal.getLineString(this.partIndex);
      if (this.vertexIndex < lineString.getVertexCount()) {
        return this;
      } else {
        this.partIndex++;
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
    final Lineal line = getLineal();
    return line.setCoordinate(this.partIndex, this.vertexIndex, axisIndex, coordinate);
  }

  public void setVertexId(final int... vertexId) {
    this.partIndex = vertexId[0];
    this.vertexIndex = vertexId[1];
  }
}
