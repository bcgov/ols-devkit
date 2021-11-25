package com.revolsys.oracle.recordstore.esri;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.FileUtil;

/**
 * NOTE -1 and 0 are before applying the scale factor
 * Point        (x,y [,z] [,m])
 * Line         (x,y (,x,y)+) [,z (,z)+] [,m (,m)+]
 * Multi Point  x,y (,-1,0, x,y)* [,z (,0,z)*] [,m (,0,m)*]
 * Multi Line   (x,y (,x,y)+) (,-1,0, (x,y (,x,y)+))* [(,z (,z)+) (,0, (,z (,z)+))*] [(,m (,m)+) (,0, (,m (,m)+))*]
 *
 */
public class PackedCoordinateUtil {
  public static Geometry getGeometry(final byte[] data, final GeometryFactory geometryFactory,
    final int entity, final int vertexCount, final Double xOffset, final Double yOffset,
    final Double xyScale, final Double zOffset, final Double zScale, final Double mOffset,
    final Double mScale) throws IOException {
    final InputStream in = new ByteArrayInputStream(data);
    return getGeometry(in, geometryFactory, entity, vertexCount, xOffset, yOffset, xyScale, zOffset,
      zScale, mOffset, mScale);
  }

  public static Geometry getGeometry(final InputStream inputStream,
    final GeometryFactory geometryFactory, final int geometryType, final int vertexCount,
    final Double xOffset, final Double yOffset, final Double xyScale, final Double zOffset,
    final Double zScale, final Double mOffset, final Double mScale) throws IOException {
    switch (geometryType) {
      case ArcSdeConstants.ST_GEOMETRY_POINT:
        return getPoint(inputStream, geometryFactory, vertexCount, xOffset, yOffset, xyScale,
          zOffset, zScale, mOffset, mScale);
      case ArcSdeConstants.ST_GEOMETRY_MULTI_POINT:
        return getMultiPoint(inputStream, geometryFactory, vertexCount, xOffset, yOffset, xyScale,
          zOffset, zScale, mOffset, mScale);
      case ArcSdeConstants.ST_GEOMETRY_LINESTRING:
        return getLineString(inputStream, geometryFactory, vertexCount, xOffset, yOffset, xyScale,
          zOffset, zScale, mOffset, mScale);
      case ArcSdeConstants.ST_GEOMETRY_MULTI_LINESTRING:
        return getMultiLineString(inputStream, geometryFactory, vertexCount, xOffset, yOffset,
          xyScale, zOffset, zScale, mOffset, mScale);
      case ArcSdeConstants.ST_GEOMETRY_POLYGON:
        return getPolygon(inputStream, geometryFactory, vertexCount, xOffset, yOffset, xyScale,
          zOffset, zScale, mOffset, mScale);
      case ArcSdeConstants.ST_GEOMETRY_MULTI_POLYGON:
        return getMultiPolygon(inputStream, geometryFactory, vertexCount, xOffset, yOffset, xyScale,
          zOffset, zScale, mOffset, mScale);
      default:
        throw new IllegalArgumentException("Unknown ST_GEOMETRY entity type: " + geometryType);
    }
  }

  @SuppressWarnings("unused")
  private static LineString getLineString(final InputStream inputStream,
    final GeometryFactory geometryFactory, final int vertexCount, final Double xOffset,
    final Double yOffset, final Double xyScale, final Double zOffset, final Double zScale,
    final Double mOffset, final Double mScale) throws IOException {
    try (
      final PackedIntegerInputStream in = new PackedIntegerInputStream(inputStream)) {
      final long packedByteLength = in.readLong5();
      final long dimensionFlag = in.readLong();
      final int annotationDimension = in.read();
      final int shapeFlags = in.read();
      final boolean hasZ = (dimensionFlag & 0x01) == 0x01;
      final boolean hasM = (dimensionFlag & 0x02) == 0x02;

      int axisCount;
      if (hasM) {
        axisCount = 4;
      } else if (hasZ) {
        axisCount = 3;
      } else {
        axisCount = 2;
      }
      final double[] coordinates = new double[vertexCount * axisCount];

      long previousX = Math.round(xOffset * xyScale);
      long previousY = Math.round(yOffset * xyScale);

      int xIndex = 0;
      for (int i = 0; i < vertexCount; i++) {
        final long deltaX = in.readLong();
        final long deltaY = in.readLong();
        previousX = previousX + deltaX;
        previousY = previousY + deltaY;
        final double x = previousX / xyScale;
        final double y = previousY / xyScale;
        coordinates[xIndex] = x;
        coordinates[xIndex + 1] = y;
        xIndex += axisCount;
      }

      if (hasZ) {
        getLineStringZorM(in, coordinates, vertexCount, axisCount, 2, zOffset, zScale);
      }
      if (hasM) {
        getLineStringZorM(in, coordinates, vertexCount, axisCount, 3, mOffset, mScale);
      }

      return geometryFactory.lineString(axisCount, coordinates);
    }
  }

  private static void getLineStringZorM(final PackedIntegerInputStream in,
    final double[] coordinates, final int vertexCount, final int axisCount, final int axisIndex,
    final double offset, final double scale) throws IOException {
    long previousValue = Math.round(offset * scale);
    int coordinateIndex = axisIndex;
    for (int i = 0; i < vertexCount; i++) {
      final long deltaValue = in.readLong();
      previousValue = previousValue + deltaValue;
      final double value = previousValue / scale;
      coordinates[coordinateIndex] = value;
      coordinateIndex += axisCount;
    }
  }

  private static Lineal getMultiLineString(final InputStream inputStream,
    final GeometryFactory geometryFactory, final int vertexCount, final Double xOffset,
    final Double yOffset, final Double xyScale, final Double zOffset, final Double zScale,
    final Double mOffset, final Double mScale) {
    final List<LineString> parts = getPointsMultiPart(vertexCount, xOffset, yOffset, xyScale,
      zOffset, zScale, mOffset, mScale, inputStream);
    return geometryFactory.lineal(parts);
  }

  private static Punctual getMultiPoint(final InputStream inputStream,
    final GeometryFactory geometryFactory, final int vertexCount, final Double xOffset,
    final Double yOffset, final Double xyScale, final Double zOffset, final Double zScale,
    final Double mOffset, final Double mScale) {
    final List<LineString> parts = getPointsMultiPart(vertexCount, xOffset, yOffset, xyScale,
      zOffset, zScale, mOffset, mScale, inputStream);
    return geometryFactory.punctual(parts);
  }

  private static Polygonal getMultiPolygon(final InputStream inputStream,
    final GeometryFactory geometryFactory, final int vertexCount, final Double xOffset,
    final Double yOffset, final Double xyScale, final Double zOffset, final Double zScale,
    final Double mOffset, final Double mScale) {
    final List<List<LineString>> pointsList = getMultiPolygonPoints(vertexCount, xOffset, yOffset,
      xyScale, zOffset, zScale, mOffset, mScale, inputStream);
    try {
      return geometryFactory.polygonal(pointsList);
    } catch (final IllegalArgumentException e) {
      Logs.error(PackedCoordinateUtil.class, "Unable to load polygon", e);
      return null;
    }
  }

  @SuppressWarnings("unused")
  private static List<List<LineString>> getMultiPolygonPoints(final int vertexCount,
    final Double xOffset, final Double yOffset, final Double xyScale, final Double zOffset,
    final Double zScale, final Double mOffset, final Double mScale, final InputStream inputStream) {
    try (
      final PackedIntegerInputStream in = new PackedIntegerInputStream(inputStream)) {
      final List<List<double[]>> parts = new ArrayList<>();

      final long packedByteLength = in.readLong5();
      final long dimensionFlag = in.readLong();
      final int annotationDimension = in.read();
      final int shapeFlags = in.read();
      final boolean hasZ = (dimensionFlag & 0x01) == 0x01;
      final boolean hasM = (dimensionFlag & 0x02) == 0x02;

      int axisCount;
      if (hasM) {
        axisCount = 4;
      } else if (hasZ) {
        axisCount = 3;
      } else {
        axisCount = 2;
      }

      List<double[]> pointsList = new ArrayList<>();
      final double[] coordinates = new double[vertexCount * axisCount];

      long previousX = Math.round(xOffset * xyScale);
      long previousY = Math.round(yOffset * xyScale);

      int j = 0;
      for (int i = 0; i < vertexCount; i++) {
        final long deltaX = in.readLong();
        final long deltaY = in.readLong();
        previousX = previousX + deltaX;
        previousY = previousY + deltaY;
        final double x = previousX / xyScale;
        final double y = previousY / xyScale;
        if (previousX == -1 && previousY == 0 || x == -1 && y == 0) {
          if (!pointsList.isEmpty()) {
            parts.add(pointsList);
          }
          pointsList = new ArrayList<>();
        } else {
          coordinates[j * axisCount] = x;
          coordinates[j * axisCount + 1] = y;
          if (j > 0 && i < vertexCount - 1) {
            if (coordinates[0] == x && coordinates[1] == y) {
              if (j > 2) {
                final double[] subCoordinates = new double[j * axisCount + axisCount];
                System.arraycopy(coordinates, 0, subCoordinates, 0, subCoordinates.length);
                pointsList.add(subCoordinates);
              }
              j = 0;
            } else {
              j++;
            }
          } else {
            j++;
          }
        }
      }
      if (j > 2) {
        if (coordinates.length == axisCount * j) {
          pointsList.add(coordinates);
        } else {
          final double[] subCoordinates = new double[j * axisCount];
          System.arraycopy(coordinates, 0, subCoordinates, 0, subCoordinates.length);
          pointsList.add(subCoordinates);
        }
      }
      if (!pointsList.isEmpty()) {
        parts.add(pointsList);
      }
      if (hasZ) {
        getMultiPolygonPointsZorM(in, parts, axisCount, 2, zOffset, zScale);
      }
      if (hasM) {
        getMultiPolygonPointsZorM(in, parts, axisCount, 3, mOffset, mScale);
      }

      final List<List<LineString>> lists = new ArrayList<>();
      for (final List<double[]> part : parts) {
        final List<LineString> list = new ArrayList<>();
        lists.add(list);
        for (final double[] partCoordinates : part) {
          list.add(new LineStringDouble(axisCount, partCoordinates));
        }
      }
      return lists;
    } catch (final IOException e) {
      throw new RuntimeException("Error reading coordinates", e);
    } finally {
      FileUtil.closeSilent(inputStream);
    }
  }

  private static void getMultiPolygonPointsZorM(final PackedIntegerInputStream in,
    final List<List<double[]>> parts, final int axisCount, final int axisIndex, final double offset,
    final double scale) throws IOException {
    long previousValue = Math.round(offset * scale);
    boolean first = true;
    for (final List<double[]> part : parts) {
      if (first) {
        first = false;
      } else {
        in.readLong();
        previousValue = 0;
      }
      for (final double[] coordinates : part) {
        final int vertexCount = coordinates.length / axisCount;
        int coordinateIndex = axisIndex;
        for (int i = 0; i < vertexCount; i++) {
          final long deltaValue = in.readLong();
          previousValue = previousValue + deltaValue;
          final double value = previousValue / scale;
          coordinates[coordinateIndex] = value;
          coordinateIndex += axisCount;
        }
      }
    }
  }

  public static int getNumPoints(final List<List<Geometry>> parts) {
    int vertexCount = 0;
    if (!parts.isEmpty()) {
      for (final List<Geometry> part : parts) {
        for (final Geometry points : part) {
          vertexCount += points.getVertexCount();
        }
      }
      vertexCount += parts.size() - 1;
    }
    return vertexCount;
  }

  public static byte[] getPackedBytes(final Double xOffset, final Double yOffset,
    final Double xyScale, final boolean hasZ, final Double zOffset, final Double zScale,
    final boolean hasM, final Double mScale, final Double mOffset,
    final List<List<Geometry>> parts) {

    final int packedByteLength = 0;
    byte dimensionFlag = 0;
    final byte annotationDimension = 0;
    final byte shapeFlags = 0;

    if (hasZ) {
      dimensionFlag |= 1;
    }
    if (hasM) {
      dimensionFlag |= 2;
    }

    final PackedIntegerOutputStream out = new PackedIntegerOutputStream();
    out.writeLong5(packedByteLength);
    out.writeLong(dimensionFlag);
    out.writeLong(annotationDimension);
    out.writeLong(shapeFlags);

    // Write x,y for all parts
    long previousX = Math.round(xOffset * xyScale);
    long previousY = Math.round(yOffset * xyScale);
    boolean first = true;
    for (final List<Geometry> part : parts) {
      if (first) {
        first = false;
      } else {
        out.writeLong(-1 - previousX);
        out.writeLong(-previousY);
        previousX = -1;
        previousY = 0;
      }
      for (final Geometry component : part) {
        for (final Vertex vertex : component.vertices()) {
          previousX = writeCoordinate(out, vertex, previousX, xyScale, 0);
          previousY = writeCoordinate(out, vertex, previousY, xyScale, 1);
        }
      }
    }

    // Write z for all parts
    if (hasZ) {
      writeMultiCoordinates(out, parts, 2, zOffset, zScale);
    }

    // Write m for all parts
    if (hasM) {
      writeMultiCoordinates(out, parts, 3, mOffset, mScale);
    }
    return out.toByteArray();
  }

  @SuppressWarnings("unused")
  private static Point getPoint(final InputStream inputStream,
    final GeometryFactory geometryFactory, final int vertexCount, final Double xOffset,
    final Double yOffset, final Double xyScale, final Double zOffset, final Double zScale,
    final Double mOffset, final Double mScale) {
    try (
      final PackedIntegerInputStream in = new PackedIntegerInputStream(inputStream)) {
      final long packedByteLength = in.readLong5();
      final long dimensionFlag = in.readLong();
      final int annotationDimension = in.read();
      final int shapeFlags = in.read();
      final boolean hasZ = (dimensionFlag & 0x01) == 0x01;
      final boolean hasM = (dimensionFlag & 0x02) == 0x02;

      final double x = getPointAxisValue(in, xOffset, xyScale);
      final double y = getPointAxisValue(in, yOffset, xyScale);

      if (hasM) {
        double z;
        if (hasZ) {
          z = getPointAxisValue(in, zOffset, zScale);
        } else {
          z = Double.NaN;
        }
        final double m = getPointAxisValue(in, mOffset, mScale);
        return geometryFactory.point(x, y, z, m);
      } else if (hasZ) {
        final double z = getPointAxisValue(in, zOffset, zScale);
        return geometryFactory.point(x, y, z);
      } else {
        return geometryFactory.point(x, y);
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error reading coordinates", e);
    }
  }

  private static double getPointAxisValue(final PackedIntegerInputStream in, final double offset,
    final double scale) throws IOException {
    final long deltaValue = in.readLong();
    final long offsetLong = Math.round(offset * scale);
    final double value = (offsetLong + deltaValue) / scale;
    return value;
  }

  @SuppressWarnings("unused")
  private static List<LineString> getPointsMultiPart(final int vertexCount, final Double xOffset,
    final Double yOffset, final Double xyScale, final Double zOffset, final Double zScale,
    final Double mOffset, final Double mScale, final InputStream inputStream) {
    try (
      final PackedIntegerInputStream in = new PackedIntegerInputStream(inputStream)) {
      final List<double[]> pointsList = new ArrayList<>();

      final long packedByteLength = in.readLong5();
      final long dimensionFlag = in.readLong();
      final int annotationDimension = in.read();
      final int shapeFlags = in.read();
      final boolean hasZ = (dimensionFlag & 0x01) == 0x01;
      final boolean hasM = (dimensionFlag & 0x02) == 0x02;

      int axisCount;
      if (hasM) {
        axisCount = 4;
      } else if (hasZ) {
        axisCount = 3;
      } else {
        axisCount = 2;
      }
      final double[] coordinates = new double[vertexCount * axisCount];

      long previousX = Math.round(xOffset * xyScale);
      long previousY = Math.round(yOffset * xyScale);

      int j = 0;
      for (int i = 0; i < vertexCount; i++) {
        final long deltaX = in.readLong();
        final long deltaY = in.readLong();
        previousX = previousX + deltaX;
        previousY = previousY + deltaY;
        final double x = previousX / xyScale;
        final double y = previousY / xyScale;
        if (previousX == -1 && previousY == 0) {
          final double[] subCoordinates = new double[j * axisCount];
          System.arraycopy(coordinates, 0, subCoordinates, 0, subCoordinates.length);
          pointsList.add(subCoordinates);
          j = 0;
        } else {
          final int xIndex = j * axisCount;
          coordinates[xIndex] = x;
          coordinates[xIndex + 1] = y;
          j++;
        }
      }
      if (coordinates.length == axisCount * j) {
        pointsList.add(coordinates);
      } else {
        final double[] subCoordinates = new double[j * axisCount];
        System.arraycopy(coordinates, 0, subCoordinates, 0, subCoordinates.length);
        pointsList.add(subCoordinates);
      }

      if (hasZ) {
        getPointsMultiPartZorM(in, pointsList, axisCount, 2, zOffset, zScale);
      }
      if (hasM) {
        getPointsMultiPartZorM(in, pointsList, axisCount, 3, mOffset, mScale);
      }

      final List<LineString> lists = new ArrayList<>();
      for (final double[] partCoordinates : pointsList) {
        lists.add(new LineStringDouble(axisCount, partCoordinates));
      }
      return lists;
    } catch (final IOException e) {
      throw new RuntimeException("Error reading coordinates", e);
    } finally {
      FileUtil.closeSilent(inputStream);
    }
  }

  private static void getPointsMultiPartZorM(final PackedIntegerInputStream in,
    final List<double[]> pointsList, final int axisCount, final int axisIndex, final double offset,
    final double scale) throws IOException {
    long previousValue = Math.round(offset * scale);
    int j = 0;
    for (final double[] coordinates : pointsList) {
      if (j > 0) {
        in.readLong();
        previousValue = 0;
      }
      final int vertexCount = coordinates.length / axisCount;
      int coordinateIndex = axisIndex;
      for (int i = 0; i < vertexCount; i++) {
        final long deltaValue = in.readLong();
        previousValue = previousValue + deltaValue;
        final double value = previousValue / scale;
        coordinates[coordinateIndex] = value;
        coordinateIndex += axisCount;
      }
      j++;
    }
  }

  private static Polygon getPolygon(final InputStream inputStream,
    final GeometryFactory geometryFactory, final int vertexCount, final Double xOffset,
    final Double yOffset, final Double xyScale, final Double zOffset, final Double zScale,
    final Double mOffset, final Double mScale) {
    final List<LinearRing> pointsList = getPolygonRings(geometryFactory, vertexCount, xOffset,
      yOffset, xyScale, zOffset, zScale, mOffset, mScale, inputStream);
    try {
      return geometryFactory.polygon(pointsList);
    } catch (final IllegalArgumentException e) {
      e.printStackTrace();
      Logs.error(PackedCoordinateUtil.class, "Unable to load polygon", e);
      return null;
    }
  }

  private static void getPolygonPointsZorM(final PackedIntegerInputStream in,
    final List<double[]> pointsList, final int axisCount, final int axisIndex, final double offset,
    final double scale) throws IOException {
    long previousValue = Math.round(offset * scale);

    for (final double[] coordinates : pointsList) {
      final int vertexCount = coordinates.length / axisCount;
      int coordinateIndex = axisIndex;
      for (int i = 0; i < vertexCount; i++) {
        final long deltaValue = in.readLong();
        previousValue = previousValue + deltaValue;
        final double value = previousValue / scale;
        coordinates[coordinateIndex] = value;
        coordinateIndex += axisCount;
      }
    }
  }

  @SuppressWarnings("unused")
  private static List<LinearRing> getPolygonRings(final GeometryFactory geometryFactory,
    final int vertexCount, final Double xOffset, final Double yOffset, final Double xyScale,
    final Double zOffset, final Double zScale, final Double mOffset, final Double mScale,
    final InputStream inputStream) {

    try (
      final PackedIntegerInputStream in = new PackedIntegerInputStream(inputStream)) {
      final List<double[]> pointsList = new ArrayList<>();

      final long packedByteLength = in.readLong5();
      final long dimensionFlag = in.readLong();
      final int annotationDimension = in.read();
      final int shapeFlags = in.read();
      final boolean hasZ = (dimensionFlag & 0x01) == 0x01;
      final boolean hasM = (dimensionFlag & 0x02) == 0x02;

      int axisCount;
      if (hasM) {
        axisCount = 4;
      } else if (hasZ) {
        axisCount = 3;
      } else {
        axisCount = 2;
      }
      final double[] coordinates = new double[vertexCount * axisCount];

      long previousX = Math.round(xOffset * xyScale);
      long previousY = Math.round(yOffset * xyScale);

      int j = 0;
      for (int i = 0; i < vertexCount; i++) {
        final long deltaX = in.readLong();
        final long deltaY = in.readLong();
        previousX = previousX + deltaX;
        previousY = previousY + deltaY;
        final double x = previousX / xyScale;
        final double y = previousY / xyScale;
        final int xIndex = j * axisCount;
        coordinates[xIndex] = x;
        coordinates[xIndex + 1] = y;
        if (j > 0 && i < vertexCount - 1) {
          if (coordinates[0] == x && coordinates[1] == y) {
            if (j > 2) {
              final double[] subCoordinates = new double[xIndex + axisCount];
              System.arraycopy(coordinates, 0, subCoordinates, 0, subCoordinates.length);
              pointsList.add(subCoordinates);
            }
            j = 0;
          } else {
            j++;
          }
        } else {
          j++;
        }
      }
      if (j > 2) {
        final int numCoordinates = j * axisCount;
        if (numCoordinates == coordinates.length) {
          pointsList.add(coordinates);
        } else {
          final double[] subCoordinates = new double[numCoordinates];
          System.arraycopy(coordinates, 0, subCoordinates, 0, subCoordinates.length);
          pointsList.add(subCoordinates);
        }
      }
      if (hasZ) {
        getPolygonPointsZorM(in, pointsList, axisCount, 2, zOffset, zScale);
      }
      if (hasM) {
        getPolygonPointsZorM(in, pointsList, axisCount, 3, mOffset, mScale);
      }
      final List<LinearRing> rings = new ArrayList<>();
      for (final double[] partCoordinates : pointsList) {
        rings.add(geometryFactory.linearRing(axisCount, partCoordinates));
      }
      return rings;
    } catch (final IOException e) {
      throw new RuntimeException("Error reading coordinates", e);
    } finally {
      FileUtil.closeSilent(inputStream);
    }
  }

  private static long writeCoordinate(final PackedIntegerOutputStream out, final long previousValue,
    final double scale, final double value) {
    long longValue;
    if (Double.isNaN(value)) {
      longValue = 0;
    } else {
      longValue = Math.round(value * scale);
    }
    out.writeLong(longValue - previousValue);
    return longValue;
  }

  /**
   * Write the value of an ordinate from the coordinates which has the specified
   * coordinateIndex and ordinateIndex. The value written is the difference
   * between the current value and the previous value which are both multiplied
   * by the scale and rounded to longs before conversion.
   *
   * @param out The stream to write the bytes to.
   * @param coordinates The coordinates.
   * @param previousValue The value of the previous coordinate, returned from
   *          this method.
   * @param scale The scale which defines the precision of the values.
   * @param vertexIndex The coordinate index.
   * @param axisIndex The ordinate index.
   * @return The current ordinate value * scale rounded to a long value.
   */
  private static long writeCoordinate(final PackedIntegerOutputStream out, final Vertex vertex,
    final long previousValue, final double scale, final int axisIndex) {
    final double coordinate = vertex.getCoordinate(axisIndex);
    return writeCoordinate(out, previousValue, scale, coordinate);
  }

  private static long writeCoordinates(final PackedIntegerOutputStream out, final Geometry geometry,
    long previousValue, final double scale, final int axisIndex) {
    for (final Vertex vertex : geometry.vertices()) {
      previousValue = writeCoordinate(out, vertex, previousValue, scale, axisIndex);
    }
    return previousValue;
  }

  private static void writeMultiCoordinates(final PackedIntegerOutputStream out,
    final List<List<Geometry>> partsList, final int axisIndex, final double offset,
    final double scale) {
    long previous = Math.round(offset * scale);
    boolean firstPart = true;
    for (final List<Geometry> part : partsList) {
      if (firstPart) {
        firstPart = false;
      } else {
        out.writeLong(-previous);
        previous = 0;
      }
      for (final Geometry component : part) {
        if (component.getAxisCount() > axisIndex) {
          previous = writeCoordinates(out, component, previous, scale, axisIndex);
        } else {
          previous = writeZeroCoordinates(out, component.getVertexCount(), scale, previous);
        }
      }
    }
  }

  private static long writeZeroCoordinates(final PackedIntegerOutputStream out,
    final int vertexCount, final double scale, long previousValue) {
    for (int i = 0; i < vertexCount; i++) {
      previousValue = writeCoordinate(out, previousValue, scale, 0);
    }
    return previousValue;
  }
}
