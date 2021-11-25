package com.revolsys.geometry.model.vertex;

import java.util.NoSuchElementException;

import com.revolsys.geometry.model.Punctual;

public class MultiPointVertex extends AbstractVertex {
  private static final long serialVersionUID = 1L;

  private int partIndex;

  public MultiPointVertex(final Punctual geometry, final int... vertexId) {
    super(geometry);
    setVertexId(vertexId);
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    final Punctual punctual = getPunctual();
    return punctual.getCoordinate(this.partIndex, axisIndex);
  }

  @Override
  public double getOrientaton() {
    return 0;
  }

  @Override
  public int getPartIndex() {
    return this.partIndex;
  }

  public Punctual getPunctual() {
    return (Punctual)getGeometry();
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      this.partIndex
    };
  }

  @Override
  public int getVertexIndex() {
    return 0;
  }

  @Override
  public double getX() {
    final Punctual punctual = getPunctual();
    return punctual.getX(this.partIndex);
  }

  @Override
  public double getY() {
    final Punctual punctual = getPunctual();
    return punctual.getY(this.partIndex);
  }

  @Override
  public boolean hasNext() {
    final Punctual punctual = getPunctual();
    if (punctual.isEmpty()) {
      return false;
    } else {
      if (this.partIndex + 1 < punctual.getGeometryCount()) {
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public boolean isFrom() {
    return true;
  }

  @Override
  public Vertex next() {
    final Punctual punctual = getPunctual();
    this.partIndex++;
    if (this.partIndex < punctual.getGeometryCount()) {
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
    final Punctual punctual = getPunctual();
    return punctual.setCoordinate(this.partIndex, axisIndex, coordinate);
  }

  public void setPartIndex(final int partIndex) {
    this.partIndex = partIndex;
  }

  public void setVertexId(final int... vertexId) {
    this.partIndex = vertexId[0];
  }
}
