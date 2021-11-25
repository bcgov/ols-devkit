package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;

public class LinearRingEditor extends LineStringEditor implements LinearRing {
  private static final long serialVersionUID = 1L;

  public static LinearRingEditor getEditor(final LinearRing ring) {
    if (ring instanceof LinearRingEditor) {
      return (LinearRingEditor)ring;
    } else {
      return new LinearRingEditor(ring);
    }
  }

  public LinearRingEditor(final AbstractGeometryEditor<?> parentEditor) {
    this(parentEditor, parentEditor.getGeometryFactory().linearRing());
  }

  public LinearRingEditor(final AbstractGeometryEditor<?> parentEditor, final LinearRing ring) {
    super(parentEditor, ring);
  }

  public LinearRingEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public LinearRingEditor(final LinearRing ring) {
    super(ring);
  }

  @Override
  public LinearRingEditor clone() {
    return (LinearRingEditor)super.clone();
  }

  @Override
  public void deleteVertex(final int vertexIndex) {
    final int lastVertexIndex = getLastVertexIndex();
    if (isClosed() && (vertexIndex == 0 || vertexIndex == lastVertexIndex)) {
      getCoordinatesModified();
      if (vertexIndex == 0) {
        System.arraycopy(this.coordinates, this.axisCount, this.coordinates,
          lastVertexIndex * this.axisCount, this.axisCount);
      } else if (vertexIndex == lastVertexIndex) {
        System.arraycopy(this.coordinates, (lastVertexIndex - 1) * this.axisCount, this.coordinates,
          0, this.axisCount);
      }
      super.deleteVertex(vertexIndex);

    } else {
      super.deleteVertex(vertexIndex);
    }
  }

  @Override
  public Geometry getCurrentGeometry() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int vertexCount = getVertexCount();
    if (vertexCount == 0) {
      return newLineStringEmpty();
    } else if (vertexCount == 1) {
      return newPoint();
    } else if (vertexCount == 2 || !isClosed()) {
      return newLineString(geometryFactory, this.axisCount, vertexCount, this.coordinates);
    } else {
      return this;
    }
  }

  @Override
  public LinearRing getOriginalGeometry() {
    return (LinearRing)super.getOriginalGeometry();
  }

  @Override
  public LinearRing newGeometry() {
    return (LinearRing)super.newGeometry();
  }

  @Override
  public LinearRing newGeometry(final GeometryFactory geometryFactory) {
    return LinearRing.super.newGeometry(geometryFactory);
  }

  @Override
  public LineString newLineString(final GeometryFactory geometryFactory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    final GeometryFactory geometryFactoryAxisCount = geometryFactory.convertAxisCount(axisCount);
    if (isClosed()) {
      return geometryFactoryAxisCount.linearRing(axisCount, vertexCount, coordinates);
    } else {
      return geometryFactoryAxisCount.lineString(axisCount, vertexCount, coordinates);
    }
  }

  @Override
  public LinearRingEditor removeDuplicatePoints() {
    return (LinearRingEditor)super.removeDuplicatePoints();
  }

  @Override
  public double setCoordinate(final int vertexIndex, final int axisIndex, final double coordinate) {
    final int lastVertexIndex = getLastVertexIndex();
    if (vertexIndex == 0 || vertexIndex == lastVertexIndex) {
      // Ensure valid loop
      super.setCoordinate(0, axisIndex, coordinate);
      return super.setCoordinate(lastVertexIndex, axisIndex, coordinate);
    } else {
      return super.setCoordinate(vertexIndex, axisIndex, coordinate);
    }
  }

  @Override
  public double setM(final int vertexIndex, final double m) {
    final int lastVertexIndex = getLastVertexIndex();
    if (vertexIndex == 0 || vertexIndex == lastVertexIndex) {
      // Ensure valid loop
      super.setM(0, m);
      return super.setM(lastVertexIndex, m);
    } else {
      return super.setM(vertexIndex, m);
    }
  }

  @Override
  public double setX(final int vertexIndex, final double x) {
    final int lastVertexIndex = getLastVertexIndex();
    if (vertexIndex == 0 || vertexIndex == lastVertexIndex) {
      // Ensure valid loop
      super.setX(0, x);
      return super.setX(lastVertexIndex, x);
    } else {
      return super.setX(vertexIndex, x);
    }
  }

  @Override
  public double setY(final int vertexIndex, final double y) {
    final int lastVertexIndex = getLastVertexIndex();
    if (vertexIndex == 0 || vertexIndex == lastVertexIndex) {
      // Ensure valid loop
      super.setY(0, y);
      return super.setY(lastVertexIndex, y);
    } else {
      return super.setY(vertexIndex, y);
    }
  }

  @Override
  public double setZ(final int vertexIndex, final double z) {
    final int lastVertexIndex = getLastVertexIndex();
    if (vertexIndex == 0 || vertexIndex == lastVertexIndex) {
      // Ensure valid loop
      super.setZ(0, z);
      return super.setZ(lastVertexIndex, z);
    } else {
      return super.setZ(vertexIndex, z);
    }
  }

  @Override
  public void simplifyStraightLines() {

    super.simplifyStraightLines();
    if (this.vertexCount > 2) {
      final int previousCoordinateIndex = (this.vertexCount - 2) * this.axisCount;
      final double x1 = this.coordinates[previousCoordinateIndex];
      final double y1 = this.coordinates[previousCoordinateIndex + 1];
      final double x = this.coordinates[0];
      final double y = this.coordinates[1];
      final int nextCoordinateIndex = this.axisCount;
      final double x2 = this.coordinates[nextCoordinateIndex];
      final double y2 = this.coordinates[nextCoordinateIndex + 1];

      boolean remove = false;
      if (x1 == x) {
        if (y1 == y || x == x2) {
          remove = true;
        }
      } else if (y1 == y && y == y2) {
        remove = true;
      }
      if (remove) {
        deleteVertex(0);
      }
    }
  }

  @Override
  public String toString() {
    return toEwkt();
  }

}
