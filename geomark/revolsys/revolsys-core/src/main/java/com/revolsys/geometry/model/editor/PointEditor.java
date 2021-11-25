package com.revolsys.geometry.model.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import org.jeometry.common.number.Doubles;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public class PointEditor extends AbstractGeometryEditor<PointEditor>
  implements Point, PunctualEditor {
  private static final long serialVersionUID = 1L;

  private Point point;

  private double[] coordinates;

  public PointEditor(final AbstractGeometryCollectionEditor<?, ?, ?> parentEditor,
    final Point point) {
    super(parentEditor, point);
    if (point != null && !point.isEmpty()) {
      this.point = point;
    }
  }

  public PointEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public PointEditor(final Point point) {
    this(null, point);
  }

  @Override
  public GeometryEditor<?> appendVertex(final int[] geometryId, final Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (point == null || point.isEmpty()) {
      return this;
    } else if (isEmpty()) {
      setVertex(point);
      return this;
    } else {
      final PointEditor editorThis = newGeometryEditor();
      final Point newPoint = point.convertGeometry(geometryFactory);
      final PointEditor editorOther = newPoint.newGeometryEditor();
      final GeometryCollectionImplEditor parentEditor = (GeometryCollectionImplEditor)getParentEditor();
      return new MultiPointEditor(parentEditor, geometryFactory, editorThis, editorOther);
    }
  }

  @Override
  public Point clone() {
    return (Point)super.clone();
  }

  @Override
  public PointEditor deleteVertex(final int[] vertexId) {
    if (vertexId == null || vertexId.length == 0) {
      this.coordinates = null;
      this.point = null;
    }
    return this;
  }

  @Override
  public Iterable<PointEditor> editors() {
    return Collections.singletonList(this);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      return equals(point);
    } else if (other instanceof BoundingBox) {
      final BoundingBox boundingBox = (BoundingBox)other;
      return bboxEquals(boundingBox);
    } else {
      return false;
    }
  }

  @Override
  public boolean equalsVertex(final int axisCount, final int vertexIndex, final Point point) {
    if (vertexIndex == 0) {
      return equals(axisCount, point);
    } else {
      return false;
    }
  }

  @Override
  public boolean equalsVertex(final int axisCount, final int[] geometryId, final int vertexIndex,
    final Point point) {
    if (geometryId == null || geometryId.length == 0) {
      return equalsVertex(axisCount, vertexIndex, point);
    } else {
      return false;
    }
  }

  @Override
  public void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final CoordinatesOperationPoint point, final Consumer<CoordinatesOperationPoint> action) {
    if (!isEmpty()) {
      if (this.coordinates == null) {
        this.point.forEachVertex(coordinatesOperation, point, action);
      } else {
        point.setPoint(this.coordinates);
        coordinatesOperation.perform(point);
        action.accept(point);
      }
    }
  }

  @Override
  public void forEachVertex(final CoordinatesOperationPoint point,
    final Consumer<CoordinatesOperationPoint> action) {
    if (!isEmpty()) {
      if (this.coordinates == null) {
        this.point.forEachVertex(point, action);
      } else {
        point.setPoint(this.coordinates);
        action.accept(point);
      }
    }
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (this.coordinates == null) {
      if (this.point == null) {
        return Double.NaN;
      } else {
        return this.point.getCoordinate(axisIndex);
      }
    } else {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        return this.coordinates[axisIndex];
      } else {
        return java.lang.Double.NaN;
      }
    }
  }

  @Override
  public Geometry getCurrentGeometry() {
    return this;
  }

  @Override
  public int getVertexCount(final int[] geometryId, final int idLength) {
    if (geometryId == null || idLength == 0) {
      return 0;
    } else if (isEmpty()) {
      return 0;
    } else {
      return 1;
    }
  }

  @Override
  public double getX() {
    if (this.coordinates == null) {
      if (this.point == null) {
        return Double.NaN;
      } else {
        return this.point.getX();
      }
    } else {
      return this.coordinates[X];
    }
  }

  @Override
  public double getY() {
    if (this.coordinates == null) {
      if (this.point == null) {
        return Double.NaN;
      } else {
        return this.point.getY();
      }
    } else {
      return this.coordinates[Y];
    }
  }

  @Override
  public int hashCode() {
    return Point.hashCode(this);
  }

  @Override
  public GeometryEditor<?> insertVertex(final int[] vertexId, final Point point) {
    if (vertexId.length == 1) {
      final int vertexIndex = vertexId[0];
      if (vertexIndex < 0 || vertexIndex > 1) {
        throw new IllegalArgumentException(
          "Vertex index must be 0 or 1 for point: " + Arrays.toString(vertexId));
      } else {
        final GeometryFactory geometryFactory = getGeometryFactory();
        if (point == null || point.isEmpty()) {
          return this;
        } else if (isEmpty()) {
          return setVertex(point);
        } else if (point.isEmpty()) {
          return this;
        } else {
          final PointEditor editorThis = newGeometryEditor();
          final Point newPoint = point.convertGeometry(geometryFactory);
          final PointEditor editorOther = newPoint.newGeometryEditor();
          final GeometryCollectionImplEditor parentEditor = (GeometryCollectionImplEditor)getParentEditor();
          if (vertexIndex == 0) {
            return new MultiPointEditor(parentEditor, geometryFactory, editorOther, editorThis);
          } else {
            return new MultiPointEditor(parentEditor, geometryFactory, editorThis, editorOther);
          }
        }
      }
    } else {
      throw new IllegalArgumentException("Vertex id's for " + getGeometryType()
        + " must have length 1. " + Arrays.toString(vertexId));
    }

  }

  @Override
  public boolean isEmpty() {
    return this.point == null && this.coordinates == null;
  }

  @Override
  public Point newGeometry() {
    if (this.coordinates == null) {
      if (this.point == null) {
        return getGeometryFactory().point();
      }
      return this.point;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.point(this.coordinates);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <GA extends Geometry> GA newGeometryAny() {
    return (GA)newGeometry();
  }

  @Override
  public Point newPoint(final GeometryFactory geometryFactory, final double... coordinates) {
    return geometryFactory.point(coordinates);
  }

  @Override
  public void removeGeometry(final int partIndex) {
    this.point = null;
    this.coordinates = null;
    setModified(true);
  }

  @Override
  public void revertChanges() {
    this.coordinates = null;
    setModified(false);
  }

  @Override
  public GeometryEditor<?> setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      this.coordinates = getCoordinates(axisCount);
      super.setAxisCount(axisCount);
    }
    return this;
  }

  @Override
  public double setCoordinate(final int axisIndex, final double coordinate) {
    final int axisCount = getAxisCount();
    if (axisIndex >= 0 && axisIndex < axisCount) {
      final double oldValue;
      if (this.coordinates != null) {
        oldValue = this.coordinates[axisIndex];
      } else if (this.point == null) {
        oldValue = Double.NaN;
      } else {
        oldValue = this.point.getCoordinate(axisIndex);
      }
      if (!Doubles.equal(coordinate, oldValue)) {
        if (this.coordinates == null) {
          if (this.point == null) {
            this.coordinates = new double[axisCount];
          } else {
            this.coordinates = this.point.getCoordinates(axisCount);
          }
        }
        this.coordinates[axisIndex] = coordinate;
        setModified(true);
      }
      return oldValue;
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double setCoordinate(final int partIndex, final int axisIndex, final double coordinate) {
    if (partIndex == 0) {
      return setCoordinate(axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public PointEditor setCoordinate(final int[] vertexId, final int axisIndex,
    final double coordinate) {
    if (vertexId.length <= 1) {
      setCoordinate(axisIndex, coordinate);
    }
    return this;
  }

  @Override
  public PointEditor setVertex(final int[] vertexId, final Point newPoint) {
    final int axisCount = getAxisCount();
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double coordinate = newPoint.getCoordinate(axisIndex);
      setCoordinate(vertexId, axisIndex, coordinate);
    }
    return this;
  }

  public PointEditor setVertex(final Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point newPoint = point.convertGeometry(geometryFactory);
    final int axisCount = getAxisCount();
    if (this.coordinates == null) {
      this.coordinates = newPoint.getCoordinates(axisCount);
    } else {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final double coordinate = newPoint.getCoordinate(axisIndex);
        this.coordinates[axisIndex] = coordinate;
      }
    }
    return this;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
