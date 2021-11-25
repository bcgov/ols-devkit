package com.revolsys.geometry.model.vertex;

import java.util.NoSuchElementException;

import com.revolsys.geometry.model.Geometry;

public class GeometryCollectionVertex extends AbstractVertex {
  private static final long serialVersionUID = 1L;

  private int partIndex = -1;

  private Vertex vertex;

  public GeometryCollectionVertex(final Geometry geometry, final int... vertexId) {
    super(geometry);
    setVertexId(vertexId);
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (this.vertex == null) {
      return java.lang.Double.NaN;
    } else {
      return this.vertex.getCoordinate(axisIndex);
    }
  }

  public Geometry getGeometryCollection() {
    return getGeometry();
  }

  @Override
  public Vertex getLineNext() {
    if (this.vertex == null) {
      return null;
    } else {
      return this.vertex.getLineNext();
    }
  }

  @Override
  public Vertex getLinePrevious() {
    if (this.vertex == null) {
      return null;
    } else {
      return this.vertex.getLinePrevious();
    }
  }

  @Override
  public double getOrientaton() {
    if (this.vertex == null) {
      return java.lang.Double.NaN;
    } else {
      return this.vertex.getOrientaton();
    }
  }

  @Override
  public int getPartIndex() {
    return super.getPartIndex();
  }

  @Override
  public int[] getVertexId() {
    if (this.partIndex < 0) {
      return new int[] {
        -1
      };
    } else if (this.vertex == null) {
      return new int[] {
        this.partIndex
      };
    } else {
      final int[] partVertexId = this.vertex.getVertexId();
      final int[] vertexId = new int[partVertexId.length + 1];
      vertexId[0] = this.partIndex;
      System.arraycopy(partVertexId, 0, vertexId, 1, partVertexId.length);
      return vertexId;
    }
  }

  @Override
  public double getX() {
    if (this.vertex == null) {
      return java.lang.Double.NaN;
    } else {
      return this.vertex.getX();
    }
  }

  @Override
  public double getY() {
    if (this.vertex == null) {
      return java.lang.Double.NaN;
    } else {
      return this.vertex.getY();
    }
  }

  @Override
  public boolean hasNext() {
    if (this.partIndex == -2) {
      return false;
    } else {
      final Geometry geometryCollection = getGeometryCollection();
      int partIndex = this.partIndex;
      Vertex vertex = this.vertex;
      if (vertex != null && !vertex.hasNext()) {
        partIndex++;
        vertex = null;
      }
      while (vertex == null && partIndex < geometryCollection.getGeometryCount()) {
        if (partIndex >= 0) {
          final Geometry part = geometryCollection.getGeometry(partIndex);
          if (part != null) {
            vertex = (Vertex)part.vertices().iterator();
            if (vertex.hasNext()) {
              return true;
            } else {
              vertex = null;
            }
          }
        }
        if (partIndex > -2) {
          partIndex++;
        }
      }
      if (vertex == null) {
        return false;
      } else {
        return vertex.hasNext();
      }
    }
  }

  @Override
  public boolean isFrom() {
    if (this.vertex == null) {
      return false;
    } else {
      return this.vertex.isFrom();
    }
  }

  @Override
  public boolean isTo() {
    if (this.vertex == null) {
      return false;
    } else {
      return this.vertex.isTo();
    }
  }

  @Override
  public Vertex next() {
    if (this.partIndex == -2) {
      throw new NoSuchElementException();
    } else {
      final Geometry geometryCollection = getGeometryCollection();
      if (this.vertex != null && !this.vertex.hasNext()) {
        this.partIndex++;
        this.vertex = null;
      }
      while (this.vertex == null && this.partIndex < geometryCollection.getGeometryCount()) {
        if (this.partIndex >= 0) {
          final Geometry part = geometryCollection.getGeometry(this.partIndex);
          if (part != null) {
            this.vertex = (Vertex)part.vertices().iterator();
            if (this.vertex.hasNext()) {
              return this.vertex.next();
            } else {
              this.vertex = null;
            }
          }
        }
        if (this.partIndex > -2) {
          this.partIndex++;
        }
      }
      if (this.vertex != null && this.vertex.hasNext()) {
        return this.vertex.next();
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
  public double setCoordinate(final int axisIndex, final double coordinate) {
    if (this.vertex == null) {
      return java.lang.Double.NaN;
    } else {
      return this.vertex.setCoordinate(axisIndex, coordinate);
    }
  }

  public void setVertexId(final int[] vertexId) {
    this.vertex = null;
    if (vertexId.length > 0) {
      this.partIndex = vertexId[0];
      final Geometry geometryCollection = getGeometryCollection();
      if (this.partIndex >= 0 && this.partIndex < geometryCollection.getGeometryCount()) {
        final Geometry part = geometryCollection.getGeometry(this.partIndex);
        if (part != null) {
          final int[] partVertexId = new int[vertexId.length - 1];
          System.arraycopy(vertexId, 1, partVertexId, 0, partVertexId.length);
          this.vertex = part.getVertex(partVertexId);
        }
      }
    } else {
      this.partIndex = -2;
    }
  }

}
