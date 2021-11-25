package com.revolsys.geometry.wkb;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.BiFunction;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;

public class WkbByteBufferReader {
  private static final IntHashMap<BiFunction<GeometryFactory, ByteBuffer, Geometry>> READERS = new IntHashMap<>();

  static {
    READERS.put(1, WkbByteBufferReader::readPoint);
    READERS.put(1001, WkbByteBufferReader::readPointZ);
    READERS.put(2001, WkbByteBufferReader::readPointM);
    READERS.put(3001, WkbByteBufferReader::readPointZM);

    READERS.put(2, WkbByteBufferReader::readLineString);
    READERS.put(1002, WkbByteBufferReader::readLineStringZ);
    READERS.put(2002, WkbByteBufferReader::readLineStringM);
    READERS.put(3002, WkbByteBufferReader::readLineStringZM);

    READERS.put(3, WkbByteBufferReader::readPolygon);
    READERS.put(1003, WkbByteBufferReader::readPolygonZ);
    READERS.put(2003, WkbByteBufferReader::readPolygonM);
    READERS.put(3003, WkbByteBufferReader::readPolygonZM);

    READERS.put(4, WkbByteBufferReader::readMultiPoint);
    READERS.put(1004, WkbByteBufferReader::readMultiPoint);
    READERS.put(2004, WkbByteBufferReader::readMultiPoint);
    READERS.put(3004, WkbByteBufferReader::readMultiPoint);

    READERS.put(5, WkbByteBufferReader::readMultiLineString);
    READERS.put(1005, WkbByteBufferReader::readMultiLineString);
    READERS.put(2005, WkbByteBufferReader::readMultiLineString);
    READERS.put(3005, WkbByteBufferReader::readMultiLineString);

    READERS.put(6, WkbByteBufferReader::readMultiPolygon);
    READERS.put(1006, WkbByteBufferReader::readMultiPolygon);
    READERS.put(2006, WkbByteBufferReader::readMultiPolygon);
    READERS.put(3006, WkbByteBufferReader::readMultiPolygon);

    READERS.put(7, WkbByteBufferReader::readGeometryCollection);
    READERS.put(1007, WkbByteBufferReader::readGeometryCollection);
    READERS.put(2007, WkbByteBufferReader::readGeometryCollection);
    READERS.put(3007, WkbByteBufferReader::readGeometryCollection);
  }

  private static double[] readCoordinates(final ByteBuffer data, final int axisCount) {
    final int vertexCount = data.getInt();
    final int coordinateCount = axisCount * vertexCount;
    final double[] coordinates = new double[coordinateCount];

    for (int i = 0; i < coordinateCount; i++) {
      final double coordinate = data.getDouble();
      coordinates[i] = coordinate;
    }
    return coordinates;
  }

  private static double[] readCoordinatesM(final ByteBuffer data) {
    final int vertexCount = data.getInt();
    final int coordinateCount = 4 * vertexCount;
    final double[] coordinates = new double[coordinateCount];

    for (int coordinateIndex = 0; coordinateIndex < coordinateCount;) {
      final double x = data.getDouble();
      coordinates[coordinateIndex++] = x;

      final double y = data.getDouble();
      coordinates[coordinateIndex++] = y;

      final double z = Double.NaN;
      coordinates[coordinateIndex++] = z;

      final double m = data.getDouble();
      coordinates[coordinateIndex++] = m;
    }
    return coordinates;
  }

  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G readGeometry(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    ByteOrder byteOrder;
    if (data.get() == 0) {
      byteOrder = ByteOrder.BIG_ENDIAN;
    } else {
      byteOrder = ByteOrder.LITTLE_ENDIAN;
    }
    data.order(byteOrder);

    final int geometryType = data.getInt();
    final BiFunction<GeometryFactory, ByteBuffer, Geometry> reader = READERS.get(geometryType);
    if (reader == null) {
      throw new IllegalArgumentException("Unsupported WKB geometryType=" + geometryType);
    } else {
      return (G)reader.apply(geometryFactory, data);
    }
  }

  private static Geometry readGeometryCollection(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    final int geometryCount = data.getInt();
    final Geometry[] geometries = new Geometry[geometryCount];
    for (int i = 0; i < geometryCount; i++) {
      geometries[i] = readGeometry(geometryFactory, data);
    }
    return geometryFactory.geometry(geometries);
  }

  private static LineString readLineString(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    final int axisCount = 2;
    final double[] coordinates = readCoordinates(data, axisCount);
    return geometryFactory.lineString(axisCount, coordinates);
  }

  private static LineString readLineStringM(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    final int axisCount = 4;
    final double[] coordinates = readCoordinatesM(data);
    return geometryFactory.lineString(axisCount, coordinates);
  }

  private static LineString readLineStringZ(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    final int axisCount = 3;
    final double[] coordinates = readCoordinates(data, axisCount);
    return geometryFactory.lineString(axisCount, coordinates);
  }

  private static LineString readLineStringZM(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    final int axisCount = 4;
    final double[] coordinates = readCoordinates(data, axisCount);
    return geometryFactory.lineString(axisCount, coordinates);
  }

  private static Lineal readMultiLineString(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    final int lineCount = data.getInt();
    final LineString[] lines = new LineString[lineCount];
    for (int i = 0; i < lineCount; i++) {
      lines[i] = readGeometry(geometryFactory, data);
    }
    return geometryFactory.lineal(lines);
  }

  private static Punctual readMultiPoint(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    final int pointCount = data.getInt();
    final Point[] points = new Point[pointCount];
    for (int i = 0; i < pointCount; i++) {
      points[i] = readGeometry(geometryFactory, data);
    }
    return geometryFactory.punctual(points);
  }

  private static Polygonal readMultiPolygon(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    final int polygonCount = data.getInt();
    final Polygon[] polygons = new Polygon[polygonCount];
    for (int i = 0; i < polygonCount; i++) {
      polygons[i] = readGeometry(geometryFactory, data);
    }
    return geometryFactory.polygonal(polygons);
  }

  private static Point readPoint(final GeometryFactory geometryFactory, final ByteBuffer data) {
    final double x = data.getDouble();
    final double y = data.getDouble();
    return geometryFactory.point(x, y);
  }

  private static Point readPointM(final GeometryFactory geometryFactory, final ByteBuffer data) {
    final double x = data.getDouble();
    final double y = data.getDouble();
    final double z = Double.NaN;
    final double m = data.getDouble();
    return geometryFactory.point(x, y, z, m);
  }

  private static Point readPointZ(final GeometryFactory geometryFactory, final ByteBuffer data) {
    final double x = data.getDouble();
    final double y = data.getDouble();
    final double z = data.getDouble();
    return geometryFactory.point(x, y, z);
  }

  private static Point readPointZM(final GeometryFactory geometryFactory, final ByteBuffer data) {
    final double x = data.getDouble();
    final double y = data.getDouble();
    final double z = data.getDouble();
    final double m = data.getDouble();
    return geometryFactory.point(x, y, z, m);
  }

  private static Polygon readPolygon(final GeometryFactory geometryFactory, final ByteBuffer data) {
    return readPolygon(geometryFactory, data, 2);
  }

  private static Polygon readPolygon(final GeometryFactory geometryFactory, final ByteBuffer data,
    final int axisCount) {
    final int ringCount = data.getInt();
    final LinearRing[] rings = new LinearRing[ringCount];
    for (int i = 0; i < ringCount; i++) {
      final double[] coordinates = readCoordinates(data, axisCount);
      final LinearRing ring = geometryFactory.linearRing(axisCount, coordinates);
      rings[i] = ring;
    }
    return geometryFactory.polygon(rings);
  }

  private static Polygon readPolygonM(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    final int ringCount = data.getInt();
    final LinearRing[] rings = new LinearRing[ringCount];
    for (int i = 0; i < ringCount; i++) {
      final double[] coordinates = readCoordinatesM(data);
      final LinearRing ring = geometryFactory.linearRing(4, coordinates);
      rings[i] = ring;
    }
    return geometryFactory.polygon(rings);
  }

  private static Polygon readPolygonZ(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    return readPolygon(geometryFactory, data, 3);
  }

  private static Polygon readPolygonZM(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    return readPolygon(geometryFactory, data, 4);
  }
}
