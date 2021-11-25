package com.revolsys.geometry.model.impl;

import java.util.AbstractList;
import java.util.Arrays;

import org.jeometry.common.function.Function4;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public class SortedPointList extends AbstractList<Point> {
  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

  private final GeometryFactory geometryFactory;

  private final int axisCount;

  private double[] coordinates;

  private int vertexCount;

  private Function4<Double, Double, Double, Double, Integer> comparator = (x1, y1, x2, y2) -> {
    int compare = Double.compare(y1, y2);
    if (compare == 0) {
      compare = Double.compare(x1, x2);
    }
    return compare;
  };

  public SortedPointList(final GeometryFactory geometryFactory, final int axisCount) {
    this(geometryFactory, axisCount, 1);
  }

  public SortedPointList(final GeometryFactory geometryFactory, final int axisCount,
    final Function4<Double, Double, Double, Double, Integer> comparator) {
    this(geometryFactory, axisCount, 1);
    this.comparator = comparator;
  }

  public SortedPointList(final GeometryFactory geometryFactory, final int axisCount,
    final int initialSize) {
    if (axisCount < 2) {
      throw new IllegalArgumentException("axisCount=" + axisCount + " must be >= 2");
    }
    this.geometryFactory = geometryFactory.convertAxisCount(axisCount);
    this.axisCount = axisCount;
    this.coordinates = new double[axisCount * initialSize];
    Arrays.fill(this.coordinates, 0, this.coordinates.length, Double.NaN);
    this.vertexCount = 0;
  }

  public SortedPointList(final GeometryFactory geometryFactory, final int axisCount,
    final int initialSize, final Function4<Double, Double, Double, Double, Integer> comparator) {
    this(geometryFactory, axisCount, initialSize);
    this.comparator = comparator;
  }

  public int addPoint(final double x, final double y) {
    return addPoint(x, y, Double.NaN);
  }

  public int addPoint(final double x, final double y, final double z) {
    if (this.vertexCount == 0) {
      insertPoint(0, x, y, z);
      return 0;
    } else {
      int minVertexIndex = 0;
      int maxVertexIndex = this.vertexCount - 1;
      while (true) {
        final int vertexIndex = minVertexIndex + maxVertexIndex >>> 1;

        final int coordinateIndex = vertexIndex * this.axisCount;
        final double x1 = this.coordinates[coordinateIndex];
        final double y1 = this.coordinates[coordinateIndex + 1];
        int compare = this.comparator.apply(x, y, x1, y1);
        if (compare == 0) {
          if (this.axisCount == 2) {
            return vertexIndex;
          } else {
            final double z1 = this.coordinates[coordinateIndex + 2];
            compare = Double.compare(z, z1);
            if (compare == 0) {
              return vertexIndex;
            } else if (Double.isFinite(z1)) {
              throw new IllegalArgumentException("Duplicate points not supported");
            } else if (Double.isFinite(z)) {
              this.coordinates[coordinateIndex + 2] = z;
              return vertexIndex;
            } else {
              return vertexIndex;
            }
          }
        }
        if (compare == -1) {
          if (vertexIndex == minVertexIndex) {
            insertPoint(minVertexIndex, x, y, z);
            return minVertexIndex;
          } else {
            maxVertexIndex = vertexIndex - 1;
          }
        } else if (compare == 1) {
          if (vertexIndex == maxVertexIndex) {
            final int newVertexIndex = maxVertexIndex + 1;
            insertPoint(newVertexIndex, x, y, z);
            return newVertexIndex;
          } else {
            minVertexIndex = vertexIndex + 1;
          }
        } else {
          return vertexIndex;
        }
      }
    }
  }

  @Override
  public SortedPointList clone() {
    try {
      final SortedPointList clone = (SortedPointList)super.clone();
      clone.coordinates = this.coordinates.clone();
      return clone;
    } catch (final CloneNotSupportedException e) {
      return this;
    }
  }

  private void ensureCapacity(final int vertexCount) {
    if (vertexCount >= this.vertexCount) {
      final int coordinateCount = vertexCount * this.axisCount;
      if (coordinateCount - this.coordinates.length > 0) {
        grow(coordinateCount);
      }
    }
  }

  @Override
  public Point get(final int index) {
    return getPoint(index);
  }

  public int getAxisCount() {
    return this.axisCount;
  }

  public double getCoordinate(final int index, final int axisIndex) {
    final int axisCount = getAxisCount();
    if (index >= 0 && index < this.vertexCount && axisIndex < axisCount) {
      return this.coordinates[index * axisCount + axisIndex];
    } else {
      return Double.NaN;
    }
  }

  public double[] getCoordinates() {
    final double[] coordinates = new double[this.vertexCount * this.axisCount];
    System.arraycopy(this.coordinates, 0, coordinates, 0, coordinates.length);
    return coordinates;
  }

  public double[] getCoordinatesRaw() {
    return this.coordinates;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public Point getPoint(final int index) {
    final double[] coordinates = new double[this.axisCount];
    System.arraycopy(this.coordinates, index * this.axisCount, coordinates, 0, this.axisCount);
    return this.geometryFactory.point(coordinates);
  }

  public int getVertexCount() {
    return this.vertexCount;
  }

  private void grow(final int minLength) {
    final int oldLength = this.coordinates.length;
    int newLength;
    if (oldLength == 0) {
      newLength = 8 * this.axisCount;
    } else if (oldLength < 768) {
      newLength = oldLength + (oldLength >> 1) * this.axisCount;
    } else {
      newLength = oldLength + 768;
    }
    if (newLength - minLength < 0) {
      newLength = minLength;
    }
    if (newLength - MAX_ARRAY_SIZE > 0) {
      if (minLength < 0) {
        throw new OutOfMemoryError();
      } else {
        newLength = minLength > MAX_ARRAY_SIZE ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
      }
    }
    this.coordinates = Arrays.copyOf(this.coordinates, newLength);
    Arrays.fill(this.coordinates, oldLength, this.coordinates.length, Double.NaN);
  }

  @Override
  public int indexOf(final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      return indexOfPoint(point);
    }
    return super.indexOf(other);
  }

  public int indexOfPoint(final double x, final double y) {
    return indexOfPoint(x, y, Double.NaN);
  }

  public int indexOfPoint(final double x, final double y, final double z) {
    if (this.vertexCount == 0) {
      return -1;
    } else {
      int minVertexIndex = 0;
      int maxVertexIndex = this.vertexCount - 1;
      while (minVertexIndex <= maxVertexIndex) {
        final int vertexIndex = minVertexIndex + maxVertexIndex >>> 1;

        final int coordinateIndex = vertexIndex * this.axisCount;
        final double x1 = this.coordinates[coordinateIndex];
        final double y1 = this.coordinates[coordinateIndex + 1];
        int compare = this.comparator.apply(x, y, x1, y1);
        if (compare == 0) {
          if (this.axisCount == 2) {
            return vertexIndex;
          } else {
            final double z1 = this.coordinates[coordinateIndex + 2];
            compare = Double.compare(z, z1);
            if (compare == 0) {
              return vertexIndex;
            } else if (Double.isFinite(z1)) {
              if (!Double.isFinite(z)) {
                return vertexIndex;
              }
            } else if (Double.isFinite(z)) {
              return vertexIndex;
            } else {
              return vertexIndex;
            }
          }
        }
        if (compare == -1) {
          if (vertexIndex == minVertexIndex) {
            return -1;
          } else {
            maxVertexIndex = vertexIndex - 1;
          }
        } else if (compare == 1) {
          if (vertexIndex == maxVertexIndex) {
            return -1;
          } else {
            minVertexIndex = vertexIndex + 1;
          }
        } else {
          return vertexIndex;
        }
      }
    }
    return -1;
  }

  public int indexOfPoint(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    final double z = point.getZ();
    return indexOfPoint(x, y, z);
  }

  private void insertPoint(final int index, final double x, final double y, final double z) {
    final int axisCount = getAxisCount();
    if (index >= this.vertexCount) {
      ensureCapacity(index + 1);
      this.vertexCount = index + 1;
    } else {
      ensureCapacity(this.vertexCount + 1);
      final int offset = index * axisCount;
      final int newOffset = offset + axisCount;
      System.arraycopy(this.coordinates, offset, this.coordinates, newOffset,
        this.coordinates.length - newOffset);
      this.vertexCount++;
    }
    final int offset = index * axisCount;
    this.coordinates[offset] = x;
    this.coordinates[offset + 1] = y;
    if (axisCount > 2) {
      this.coordinates[offset + 2] = z;
    }
  }

  @Override
  public boolean isEmpty() {
    return this.vertexCount == 0;
  }

  @Override
  public int size() {
    return getVertexCount();
  }
}
