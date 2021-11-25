package com.revolsys.elevation.tin;

import java.util.function.Consumer;

import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.index.rstartree.RStarTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.impl.AbstractTriangle;
import com.revolsys.geometry.model.impl.BaseBoundingBox;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public class IntArrayScaleTriangulatedIrregularNetwork extends BaseObjectWithProperties
  implements TriangulatedIrregularNetwork {

  private class TinTriangle extends AbstractTriangle {
    private static final long serialVersionUID = 1L;

    private final int triangleIndex;

    public TinTriangle(final int triangleIndex) {
      this.triangleIndex = triangleIndex;
    }

    @Override
    public double getCoordinate(final int vertexIndex, final int axisIndex) {
      if (this.triangleIndex >= 0
        && this.triangleIndex < IntArrayScaleTriangulatedIrregularNetwork.this.triangleCount
        && axisIndex >= 0 && axisIndex < 3) {
        final double coordinate = getTriangleVertexCoordinate(this.triangleIndex, vertexIndex,
          axisIndex);
        return coordinate;
      }
      return Double.NaN;
    }

    @Override
    public double[] getCoordinates() {
      final double[] coordinates = new double[12];

      int coordinateIndex = 0;
      for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++) {
        coordinates[coordinateIndex++] = getTriangleVertexX(this.triangleIndex, vertexIndex);
        coordinates[coordinateIndex++] = getTriangleVertexY(this.triangleIndex, vertexIndex);
        coordinates[coordinateIndex++] = getTriangleVertexZ(this.triangleIndex, vertexIndex);
      }
      coordinates[coordinateIndex++] = coordinates[0];
      coordinates[coordinateIndex++] = coordinates[1];
      coordinates[coordinateIndex++] = coordinates[2];
      return coordinates;
    }

    @Override
    public GeometryFactory getGeometryFactory() {
      return IntArrayScaleTriangulatedIrregularNetwork.this.geometryFactory;
    }

    @Override
    public double getX(final int vertexIndex) {
      return getTriangleVertexX(this.triangleIndex, vertexIndex);
    }

    @Override
    public double getY(final int vertexIndex) {
      return getTriangleVertexY(this.triangleIndex, vertexIndex);
    }

    @Override
    public double getZ(final int vertexIndex) {
      return getTriangleVertexZ(this.triangleIndex, vertexIndex);
    }
  }

  private class TinTriangleBoundingBox extends BaseBoundingBox {
    private static final long serialVersionUID = 1L;

    private final int triangleIndex;

    public TinTriangleBoundingBox(final int triangleIndex) {
      this.triangleIndex = triangleIndex;
    }

    @Override
    public GeometryFactory getGeometryFactory() {
      return IntArrayScaleTriangulatedIrregularNetwork.this.geometryFactory;
    }

    @Override
    public double getMax(final int axisIndex) {
      if (axisIndex == 0 || axisIndex == 1) {
        double max = Double.NEGATIVE_INFINITY;
        for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++) {
          final double value = getTriangleVertexCoordinate(this.triangleIndex, vertexIndex,
            axisIndex);
          if (value > max) {
            max = value;
          }
        }
        return max;
      } else {
        return Double.NaN;
      }
    }

    @Override
    public double getMin(final int axisIndex) {
      if (axisIndex == 0 || axisIndex == 1) {
        double min = Double.POSITIVE_INFINITY;
        for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++) {
          final double value = getTriangleVertexCoordinate(this.triangleIndex, vertexIndex,
            axisIndex);
          if (value < min) {
            min = value;
          }
        }
        return min;
      } else {
        return Double.NaN;
      }
    }

    @Override
    public boolean isEmpty() {
      return false;
    }
  }

  private final int[] triangleXCoordinates;

  private final int[] triangleYCoordinates;

  private final int[] triangleZCoordinates;

  private final int triangleCount;

  protected final GeometryFactory geometryFactory;

  private SpatialIndex<Triangle> triangleSpatialIndex;

  private final BoundingBox boundingBox;

  private final double scaleX;

  private final double scaleY;

  private final double scaleZ;

  public IntArrayScaleTriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final int triangleCount, final int[] triangleXCoordinates,
    final int[] triangleYCoordinates, final int[] triangleZCoordinates) {
    this.geometryFactory = geometryFactory;
    this.scaleX = geometryFactory.getScaleX();
    this.scaleY = geometryFactory.getScaleY();
    this.scaleZ = geometryFactory.getScaleZ();
    this.triangleCount = triangleCount;
    this.boundingBox = boundingBox;

    this.triangleXCoordinates = triangleXCoordinates;
    this.triangleYCoordinates = triangleYCoordinates;
    this.triangleZCoordinates = triangleZCoordinates;
  }

  @Override
  public void forEachTriangle(final BoundingBox boundingBox,
    final Consumer<? super Triangle> action) {
    final SpatialIndex<Triangle> index = getTriangleSpatialIndex();
    index.forEach(boundingBox, (triangle) -> {
      action.accept(triangle);
    });
  }

  @Override
  public void forEachTriangle(final Consumer<? super Triangle> action) {
    for (int i = 0; i < this.triangleCount; i++) {
      final Triangle triangle = newTriangle(i);
      action.accept(triangle);
    }
  }

  @Override
  public void forEachTriangle(final double x, final double y,
    final Consumer<? super Triangle> action) {
    final SpatialIndex<Triangle> index = getTriangleSpatialIndex();
    index.forEach(x, y, triangle -> {
      action.accept(triangle);
    });
  }

  @Override
  public void forEachVertex(final Consumer<Point> action) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public Resource getResource() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getTriangleCount() {
    return this.triangleCount;
  }

  public SpatialIndex<Triangle> getTriangleSpatialIndex() {
    if (this.triangleSpatialIndex == null) {
      synchronized (this) {
        if (this.triangleSpatialIndex == null) {
          final SpatialIndex<Triangle> index = new RStarTree<>(this.geometryFactory);
          for (int triangleIndex = 0; triangleIndex < this.triangleCount; triangleIndex++) {
            final Triangle triangle = newTriangle(triangleIndex);
            final BoundingBox boundingBox = triangle.getBoundingBox();
            index.insertItem(boundingBox, triangle);
          }
          this.triangleSpatialIndex = index;
        }
      }
    }
    return this.triangleSpatialIndex;
  }

  public double getTriangleVertexCoordinate(final int triangleIndex, final int vertexIndex,
    final int axisIndex) {
    switch (axisIndex) {
      case 0:
        return getTriangleVertexX(triangleIndex, vertexIndex);
      case 1:
        return getTriangleVertexY(triangleIndex, vertexIndex);
      case 2:
        return getTriangleVertexZ(triangleIndex, vertexIndex);

      default:
        return Double.NaN;
    }
  }

  public double getTriangleVertexX(final int triangleIndex, final int vertexIndex) {
    final int intValue = this.triangleXCoordinates[triangleIndex * 3 + vertexIndex];
    if (intValue == Integer.MIN_VALUE) {
      return Double.NaN;
    } else {
      return intValue / this.scaleX;
    }
  }

  public double getTriangleVertexY(final int triangleIndex, final int vertexIndex) {
    final int intValue = this.triangleYCoordinates[triangleIndex * 3 + vertexIndex];
    if (intValue == Integer.MIN_VALUE) {
      return Double.NaN;
    } else {
      return intValue / this.scaleY;
    }
  }

  public double getTriangleVertexZ(final int triangleIndex, final int vertexIndex) {
    final int intValue = this.triangleZCoordinates[triangleIndex * 3 + vertexIndex];
    if (intValue == Integer.MIN_VALUE) {
      return Double.NaN;
    } else {
      return intValue / this.scaleZ;
    }
  }

  @Override
  public int getVertexCount() {
    return 0;
  }

  public Triangle newTriangle(final int triangleIndex) {
    if (triangleIndex >= 0 && triangleIndex < this.triangleCount) {
      return new TinTriangle(triangleIndex);
    } else {
      return null;
    }
  }

  public BoundingBox newTriangleBoundingBox(final int triangleIndex) {
    return new TinTriangleBoundingBox(triangleIndex);
  }
}
