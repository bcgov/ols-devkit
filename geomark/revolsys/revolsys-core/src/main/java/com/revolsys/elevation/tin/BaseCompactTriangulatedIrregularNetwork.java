package com.revolsys.elevation.tin;

import java.util.function.Consumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.impl.AbstractTriangle;
import com.revolsys.geometry.model.impl.BaseBoundingBox;

public abstract class BaseCompactTriangulatedIrregularNetwork
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
        && this.triangleIndex < BaseCompactTriangulatedIrregularNetwork.this.triangleCount
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
      for (int triangleVertexIndex = 0; triangleVertexIndex < 3; triangleVertexIndex++) {
        final int vertexIndex = getTriangleVertexIndex(this.triangleIndex, triangleVertexIndex);
        for (int i = 0; i < 3; i++) {
          coordinates[coordinateIndex++] = getVertexCoordinate(vertexIndex, i);
        }
      }
      coordinates[coordinateIndex++] = coordinates[0];
      coordinates[coordinateIndex++] = coordinates[1];
      coordinates[coordinateIndex++] = coordinates[2];
      return coordinates;
    }

    @Override
    public GeometryFactory getGeometryFactory() {
      return BaseCompactTriangulatedIrregularNetwork.this.geometryFactory;
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
      return BaseCompactTriangulatedIrregularNetwork.this.geometryFactory;
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

  public static double[] increaseSize(final double[] array) {
    final int oldLength = array.length;
    final int newLength = oldLength + (oldLength >>> 1);
    final double[] newArray = new double[newLength];
    System.arraycopy(array, 0, newArray, 0, oldLength);
    return newArray;
  }

  public static double[] increaseSize(final double[] array, final int minSize) {
    final int oldLength = array.length;
    final int newLength = Math.round(minSize / 2) * 2 + 16;
    final double[] newArray = new double[newLength];
    System.arraycopy(array, 0, newArray, 0, oldLength);
    return newArray;
  }

  public static int[] increaseSize(final int[] array, final int minSize) {
    final int oldLength = array.length;
    final int newLength = Math.round(minSize / 2) * 2 + 16;
    final int[] newArray = new int[newLength];
    System.arraycopy(array, 0, newArray, 0, oldLength);
    return newArray;
  }

  private int[] triangleVertex0Indices;

  private int[] triangleVertex1Indices;

  private int[] triangleVertex2Indices;

  protected double[] vertexXCoordinates = new double[1024];

  protected double[] vertexYCoordinates = new double[1024];

  protected double[] vertexZCoordinates = new double[1024];

  protected int vertexCount;

  private int triangleCount;

  protected final GeometryFactory geometryFactory;

  public BaseCompactTriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final int vertexCount, final double[] vertexXCoordinates, final double[] vertexYCoordinates,
    final double[] vertexZCoordinates, final int triangleCount, final int[] triangleVertex0Indices,
    final int[] triangleVertex1Indices, final int[] triangleVertex2Indices) {
    this.geometryFactory = geometryFactory.convertAxisCount(3);
    this.vertexCount = vertexCount;
    this.vertexXCoordinates = vertexXCoordinates;
    this.vertexYCoordinates = vertexYCoordinates;
    this.vertexZCoordinates = vertexZCoordinates;
    this.triangleCount = triangleCount;
    this.triangleVertex0Indices = triangleVertex0Indices;
    this.triangleVertex1Indices = triangleVertex1Indices;
    this.triangleVertex2Indices = triangleVertex2Indices;
  }

  protected int appendTriangleVertexIndices(final int vertexIndex1, final int vertexIndex2,
    final int vertexIndex3) {
    final int triangleCount = this.triangleCount;
    if (this.triangleVertex0Indices.length < triangleCount + 1) {
      final int newTriangleCapacity = triangleCount + (triangleCount >>> 1);
      setTriangleCapacity(newTriangleCapacity);
    }

    this.triangleCount++;
    setTriangleVertexIndices(triangleCount, vertexIndex1, vertexIndex2, vertexIndex3);

    return triangleCount;
  }

  @Override
  public void forEachTriangle(final Consumer<? super Triangle> action) {
    for (int i = 0; i < this.triangleCount; i++) {
      final Triangle triangle = newTriangle(i);
      action.accept(triangle);
    }
  }

  @Override
  public void forEachVertex(final Consumer<Point> action) {
    for (int i = 0; i < getVertexCount(); i++) {
      final Point point = getVertex(i);
      action.accept(point);
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public int getTriangleCount() {
    return this.triangleCount;
  }

  protected int[] getTriangleVertex0Indices() {
    return this.triangleVertex0Indices;
  }

  protected int[] getTriangleVertex1Indices() {
    return this.triangleVertex1Indices;
  }

  protected int[] getTriangleVertex2Indices() {
    return this.triangleVertex2Indices;
  }

  public double getTriangleVertexCoordinate(final int triangleIndex, final int vertexIndex,
    final int axisIndex) {
    final int triangleVertexVertexIndex = getTriangleVertexIndex(triangleIndex, vertexIndex);
    final double coordinate = getVertexCoordinate(triangleVertexVertexIndex, axisIndex);
    return coordinate;
  }

  public int getTriangleVertexIndex(final int triangleIndex, final int triangleVertexIndex) {
    switch (triangleVertexIndex) {
      case 0:
        return this.triangleVertex0Indices[triangleIndex];
      case 1:
        return this.triangleVertex1Indices[triangleIndex];
      case 2:
        return this.triangleVertex2Indices[triangleIndex];

      default:
        throw new ArrayIndexOutOfBoundsException();
    }
  }

  public double getTriangleVertexX(final int triangleIndex, final int vertexIndex) {
    final int triangleVertexVertexIndex = getTriangleVertexIndex(triangleIndex, vertexIndex);
    final double coordinate = getVertexX(triangleVertexVertexIndex);
    return coordinate;
  }

  public double getTriangleVertexY(final int triangleIndex, final int vertexIndex) {
    final int triangleVertexVertexIndex = getTriangleVertexIndex(triangleIndex, vertexIndex);
    final double coordinate = getVertexY(triangleVertexVertexIndex);
    return coordinate;
  }

  public double getTriangleVertexZ(final int triangleIndex, final int vertexIndex) {
    final int triangleVertexVertexIndex = getTriangleVertexIndex(triangleIndex, vertexIndex);
    final double coordinate = getVertexZ(triangleVertexVertexIndex);
    return coordinate;
  }

  public Point getVertex(final int vertexIndex) {
    final double x = getVertexX(vertexIndex);
    final double y = this.vertexYCoordinates[vertexIndex];
    final double z = this.vertexZCoordinates[vertexIndex];
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(x, y, z);
  }

  public double getVertexCoordinate(final int vertexIndex, final int axisIndex) {
    switch (axisIndex) {
      case 0:
        return this.vertexXCoordinates[vertexIndex];
      case 1:
        return this.vertexYCoordinates[vertexIndex];
      case 2:
        return this.vertexZCoordinates[vertexIndex];

      default:
        return Double.NaN;
    }
  }

  @Override
  public int getVertexCount() {
    return this.vertexCount;
  }

  public double getVertexX(final int vertexIndex) {
    return this.vertexXCoordinates[vertexIndex];
  }

  public double getVertexY(final int vertexIndex) {
    return this.vertexYCoordinates[vertexIndex];
  }

  public double getVertexZ(final int vertexIndex) {
    return this.vertexZCoordinates[vertexIndex];
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

  protected void setTriangleCapacity(final int triangleCapacity) {
    if (this.triangleVertex0Indices.length < triangleCapacity) {
      this.triangleVertex0Indices = increaseSize(this.triangleVertex0Indices, triangleCapacity);
      this.triangleVertex1Indices = increaseSize(this.triangleVertex1Indices, triangleCapacity);
      this.triangleVertex2Indices = increaseSize(this.triangleVertex2Indices, triangleCapacity);
    }
  }

  protected void setTriangleCount(final int triangleCount) {
    this.triangleCount = triangleCount;
  }

  protected void setTriangleVertexIndices(final int triangleIndex, final int vertexIndex1,
    final int vertexIndex2, final int vertexIndex3) {
    this.triangleVertex0Indices[triangleIndex] = vertexIndex1;
    this.triangleVertex1Indices[triangleIndex] = vertexIndex2;
    this.triangleVertex2Indices[triangleIndex] = vertexIndex3;
  }
}
