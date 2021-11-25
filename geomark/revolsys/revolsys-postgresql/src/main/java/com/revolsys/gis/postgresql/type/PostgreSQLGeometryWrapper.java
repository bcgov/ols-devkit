package com.revolsys.gis.postgresql.type;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.function.Consumer3;
import org.jeometry.common.number.Doubles;
import org.postgresql.util.PGobject;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;

public class PostgreSQLGeometryWrapper extends PGobject {
  private static final long serialVersionUID = 0L;

  private static final Map<DataType, Consumer3<PrintWriter, Geometry, Integer>> WRITER_BY_TYPE = Maps
    .<DataType, Consumer3<PrintWriter, Geometry, Integer>> buildHash() //
    .add(GeometryDataTypes.POINT, PostgreSQLGeometryWrapper::writePoint)
    .add(GeometryDataTypes.LINE_STRING, PostgreSQLGeometryWrapper::writeLineString)
    .add(GeometryDataTypes.LINEAR_RING, PostgreSQLGeometryWrapper::writeLinearRing)
    .add(GeometryDataTypes.POLYGON, PostgreSQLGeometryWrapper::writePolygon)
    .add(GeometryDataTypes.MULTI_POINT, PostgreSQLGeometryWrapper::writeMultiPoint)
    .add(GeometryDataTypes.MULTI_LINE_STRING, PostgreSQLGeometryWrapper::writeMultiLineString)
    .add(GeometryDataTypes.MULTI_POLYGON, PostgreSQLGeometryWrapper::writeMultiPolygon)
    .add(GeometryDataTypes.GEOMETRY_COLLECTION, PostgreSQLGeometryWrapper::writeGeometry)
    .add(GeometryDataTypes.GEOMETRY, PostgreSQLGeometryWrapper::writeGeometry)
    .getMap();

  public static void append(final StringBuilder wkt, final int axisCount, final Point point) {
    for (int i = 0; i < axisCount; i++) {
      if (i > 0) {
        wkt.append(" ");
      }
      Doubles.append(wkt, point.getCoordinate(i));
    }
  }

  public static void appendLineString(final StringBuilder wkt, final Point... points) {
    wkt.append("LINESTRING");
    int axisCount = 2;
    for (final Point point : points) {
      axisCount = Math.max(axisCount, point.getAxisCount());
    }
    if (axisCount > 3) {
      wkt.append(" ZM");
    } else if (axisCount > 2) {
      wkt.append(" Z");
    }
    boolean first = true;
    for (final Point point : points) {
      if (first) {
        first = false;
      } else {
        wkt.append(",");
      }
      append(wkt, axisCount, point);
    }
    wkt.append(")");
  }

  public static void appendPoint(final StringBuilder wkt, final Point point) {
    wkt.append("POINT");
    final int axisCount = point.getAxisCount();
    if (axisCount > 3) {
      wkt.append(" ZM");
    } else if (axisCount > 2) {
      wkt.append(" Z");
    }
    append(wkt, axisCount, point);
    wkt.append(")");
  }

  @SuppressWarnings("unchecked")
  private static <G extends Geometry> G getGeometry(final Geometry geometry,
    final Class<G> expectedClass) {
    if (expectedClass.isAssignableFrom(geometry.getClass())) {
      return (G)geometry;
    } else if (geometry.isGeometryCollection()) {
      if (geometry.getGeometryCount() == 1) {
        final Geometry firstGeometry = geometry.getGeometry(0);
        if (expectedClass.isAssignableFrom(firstGeometry.getClass())) {
          return (G)geometry;
        } else {
          throw new RuntimeException(geometry.getGeometryType() + " must contain a single "
            + expectedClass.getSimpleName() + " not a " + firstGeometry.getGeometryType());
        }
      } else {
        throw new RuntimeException(geometry.getGeometryType() + " must only have one "
          + expectedClass.getSimpleName() + " not " + geometry.getGeometryCount());
      }
    } else {
      throw new RuntimeException(
        "Expecting a " + expectedClass.getSimpleName() + " not " + geometry.getGeometryType());
    }
  }

  /**
   * Generates the WKT for a <tt>LINESTRING</tt>
   * specified by two {@link Coordinates}s.
   *
   * @param point1 the first coordinate
   * @param point2 the second coordinate
   *
   * @return the WKT
   */
  public static String lineString(final Point... points) {
    final StringBuilder wkt = new StringBuilder();
    appendLineString(wkt, points);
    return wkt.toString();
  }

  /**
   * Generates the WKT for a <tt>POINT</tt>
   * specified by a {@link Coordinates}.
   *
   * @param p0 the point coordinate
   *
   * @return the WKT
   */
  public static String point(final Point point) {
    final StringBuilder wkt = new StringBuilder();
    appendPoint(wkt, point);
    return wkt.toString();
  }

  private static void writeAxis(final PrintWriter out, final int axisCount) {
    if (axisCount > 3) {
      out.print("M");
    }
  }

  private static void writeCoordinates(final PrintWriter out, final LineString coordinates,
    final int axisCount) {
    out.print('(');
    writeLineStringVertex(out, coordinates, 0, axisCount);
    for (int i = 1; i < coordinates.getVertexCount(); i++) {
      out.print(',');
      writeLineStringVertex(out, coordinates, i, axisCount);
    }
    out.print(')');
  }

  private static void writeCoordinates(final PrintWriter out, final Point point,
    final int axisCount) {
    writeOrdinate(out, point, 0);
    for (int j = 1; j < axisCount; j++) {
      out.print(' ');
      writeOrdinate(out, point, j);
    }
  }

  private static void writeCoordinatesReverse(final PrintWriter out, final LineString coordinates,
    final int axisCount) {
    out.print('(');
    final int lastVertexIndex = coordinates.getVertexCount() - 1;
    writeLineStringVertex(out, coordinates, lastVertexIndex, axisCount);
    for (int i = lastVertexIndex - 1; i >= 0; i--) {
      out.print(',');
      writeLineStringVertex(out, coordinates, i, axisCount);
    }
    out.print(')');
  }

  private static void writeGeometry(final PrintWriter out, final Geometry geometry,
    final int axisCount) {
    if (geometry != null) {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        writePoint(out, point, axisCount);
      } else if (geometry instanceof Punctual) {
        final Punctual punctual = (Punctual)geometry;
        writeMultiPoint(out, punctual, axisCount);
      } else if (geometry instanceof LinearRing) {
        final LinearRing line = (LinearRing)geometry;
        writeLinearRing(out, line, axisCount);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        writeLineString(out, line, axisCount);
      } else if (geometry instanceof Lineal) {
        final Lineal lineal = (Lineal)geometry;
        writeMultiLineString(out, lineal, axisCount);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        writePolygon(out, polygon, axisCount);
      } else if (geometry instanceof Polygonal) {
        final Polygonal polygonal = (Polygonal)geometry;
        writeMultiPolygon(out, polygonal, axisCount);
      } else if (geometry.isGeometryCollection()) {
        writeGeometryCollection(out, geometry, axisCount);
      } else {
        throw new IllegalArgumentException("Unknown geometry type" + geometry.getClass());
      }
    }
  }

  private static void writeGeometryCollection(final PrintWriter out, final Geometry geometry,
    final int axisCount) {
    writeGeometryType(out, "GEOMETRYCOLLECTION", axisCount);
    if (geometry.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      Geometry part = geometry.getGeometry(0);
      writeGeometry(out, part, axisCount);
      for (int i = 1; i < geometry.getGeometryCount(); i++) {
        out.print(',');
        part = part.getGeometry(i);
        writeGeometry(out, part, axisCount);
      }
      out.print(')');
    }
  }

  private static void writeGeometryType(final PrintWriter out, final String geometryType,
    final int axisCount) {
    out.print(geometryType);
    writeAxis(out, axisCount);
  }

  private static void writeLinearRing(final PrintWriter out, final Geometry geometry,
    final int axisCount) {
    final LinearRing line = getGeometry(geometry, LinearRing.class);
    writeLinearRing(out, line, axisCount);
  }

  private static void writeLinearRing(final PrintWriter out, final LinearRing line,
    final int axisCount) {
    writeGeometryType(out, "LINEARRING", axisCount);
    if (line.isEmpty()) {
      out.print(" EMPTY");
    } else {
      writeCoordinates(out, line, axisCount);
    }
  }

  private static void writeLineString(final PrintWriter out, final Geometry geometry,
    final int axisCount) {
    final LineString line = getGeometry(geometry, LineString.class);
    writeLineString(out, line, axisCount);
  }

  private static void writeLineString(final PrintWriter out, final LineString line,
    final int axisCount) {
    writeGeometryType(out, "LINESTRING", axisCount);
    if (line.isEmpty()) {
      out.print(" EMPTY");
    } else {
      final LineString coordinates = line;
      writeCoordinates(out, coordinates, axisCount);
    }
  }

  private static void writeLineStringVertex(final PrintWriter out, final LineString line,
    final int index, final int axisCount) {
    writeOrdinate(out, line, index, 0);
    for (int j = 1; j < axisCount; j++) {
      out.print(' ');
      writeOrdinate(out, line, index, j);
    }
  }

  private static void writeMultiLineString(final PrintWriter out, final Geometry geometry,
    final int axisCount) {
    writeGeometryType(out, "MULTILINESTRING", axisCount);
    if (geometry.isEmpty()) {
      out.print(" EMPTY");
    } else {
      try {
        out.print("(");
        LineString line = geometry.getGeometry(0);
        writeCoordinates(out, line, axisCount);
        for (int i = 1; i < geometry.getGeometryCount(); i++) {
          out.print(",");
          line = (LineString)geometry.getGeometry(i);
          writeCoordinates(out, line, axisCount);
        }
        out.print(")");
      } catch (final ClassCastException e) {
        throw new IllegalArgumentException(
          "Expecting a MultiPoint not " + geometry.getGeometryType());
      }
    }
  }

  private static void writeMultiPoint(final PrintWriter out, final Geometry geometry,
    final int axisCount) {
    writeGeometryType(out, "MULTIPOINT", axisCount);
    if (geometry.isEmpty()) {
      out.print(" EMPTY");
    } else {
      try {
        Point point = geometry.getGeometry(0);
        out.print("((");
        writeCoordinates(out, point, axisCount);
        for (int i = 1; i < geometry.getGeometryCount(); i++) {
          out.print("),(");
          point = geometry.getGeometry(i);
          writeCoordinates(out, point, axisCount);
        }
        out.print("))");
      } catch (final ClassCastException e) {
        throw new IllegalArgumentException(
          "Expecting a MultiPoint not " + geometry.getGeometryType());
      }
    }
  }

  private static void writeMultiPolygon(final PrintWriter out, final Geometry geometry,
    final int axisCount) {
    writeGeometryType(out, "MULTIPOLYGON", axisCount);
    if (geometry.isEmpty()) {
      out.print(" EMPTY");
    } else {
      try {
        out.print("(");

        Polygon polygon = (Polygon)geometry.getGeometry(0);
        writePolygonCoordinates(out, polygon, axisCount);
        for (int i = 1; i < geometry.getGeometryCount(); i++) {
          out.print(",");
          polygon = (Polygon)geometry.getGeometry(i);
          writePolygonCoordinates(out, polygon, axisCount);
        }
        out.print(")");
      } catch (final ClassCastException e) {
        throw new IllegalArgumentException(
          "Expecting a MultiPolygon not " + geometry.getGeometryType());
      }
    }
  }

  private static void writeOrdinate(final PrintWriter out, final LineString coordinates,
    final int index, final int ordinateIndex) {
    if (ordinateIndex > coordinates.getAxisCount()) {
      out.print(0);
    } else {
      final double ordinate = coordinates.getCoordinate(index, ordinateIndex);
      if (Double.isNaN(ordinate)) {
        out.print(0);
      } else {
        out.print(Doubles.toString(ordinate));
      }
    }
  }

  private static void writeOrdinate(final PrintWriter out, final Point coordinates,
    final int ordinateIndex) {
    if (ordinateIndex > coordinates.getAxisCount()) {
      out.print(0);
    } else {
      final double ordinate = coordinates.getCoordinate(ordinateIndex);
      if (Double.isNaN(ordinate)) {
        out.print(0);
      } else {
        out.print(Doubles.toString(ordinate));
      }
    }
  }

  private static void writePoint(final PrintWriter out, final Geometry geometry,
    final int axisCount) {
    final Point point = getGeometry(geometry, Point.class);
    writePoint(out, point, axisCount);
  }

  private static void writePoint(final PrintWriter out, final Point point, final int axisCount) {
    writeGeometryType(out, "POINT", axisCount);
    if (point.isEmpty()) {
      out.print(" EMPTY");
    } else {
      out.print("(");
      writeCoordinates(out, point, axisCount);
      out.print(')');
    }
  }

  private static void writePolygon(final PrintWriter out, final Geometry geometry,
    final int axisCount) {
    final Polygon polygon = getGeometry(geometry, Polygon.class);
    writePolygon(out, polygon, axisCount);
  }

  private static void writePolygon(final PrintWriter out, final Polygon polygon,
    final int axisCount) {
    writeGeometryType(out, "POLYGON", axisCount);
    if (polygon.isEmpty()) {
      out.print(" EMPTY");
    } else {
      writePolygonCoordinates(out, polygon, axisCount);
    }
  }

  private static void writePolygonCoordinates(final PrintWriter out, final Polygon polygon,
    final int axisCount) {
    out.print('(');
    {
      final LineString shell = polygon.getShell();
      if (shell.isClockwise()) {
        writeCoordinatesReverse(out, shell, axisCount);
      } else {
        writeCoordinates(out, shell, axisCount);
      }
    }
    for (final LineString hole : polygon.holes()) {
      out.print(',');
      if (hole.isClockwise()) {
        writeCoordinates(out, hole, axisCount);
      } else {
        writeCoordinatesReverse(out, hole, axisCount);
      }
    }
    out.print(')');
  }

  private Geometry geometry;

  public PostgreSQLGeometryWrapper() {
    setType("geometry");
  }

  public PostgreSQLGeometryWrapper(final DataType dataType, final GeometryFactory geometryFactory,
    final Geometry geometry) {
    this();
    this.geometry = geometry.convertGeometry(geometryFactory);

    final StringWriter wkt = new StringWriter();
    try (
      final PrintWriter writer = new PrintWriter(wkt)) {
      final int srid = geometry.getHorizontalCoordinateSystemId();
      if (srid > 0) {
        writer.print("SRID=");
        writer.print(srid);
        writer.print(';');
      }
      if (this.geometry != null) {
        final Consumer3<PrintWriter, Geometry, Integer> writeMethod = WRITER_BY_TYPE.get(dataType);
        writeMethod.accept(writer, this.geometry, geometryFactory.getAxisCount());
      }
    }
    this.value = wkt.toString();
  }

  @Override
  public PostgreSQLGeometryWrapper clone() {
    try {
      return (PostgreSQLGeometryWrapper)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  public Geometry getGeometry(final GeometryFactory geometryFactory) {
    if (this.geometry == null) {
      newGeometry(geometryFactory);
    }
    return this.geometry;
  }

  public void newGeometry(GeometryFactory geometryFactory) {
    final String value = getValue().trim();
    int srid = -1;
    String wkt;
    if (value.startsWith("SRID=")) {
      final int index = value.indexOf(';', 5);
      if (index == -1) {
        throw new IllegalArgumentException("Error parsing Geometry - SRID not delimited with ';' ");
      } else {
        srid = Integer.parseInt(value.substring(5, index));
        wkt = value.substring(index + 1).trim();
      }
    } else {
      wkt = value;
    }
    if (srid != -1 && geometryFactory.getHorizontalCoordinateSystemId() != srid) {
      geometryFactory = GeometryFactory.floating(srid, geometryFactory.getAxisCount());
    }
    if (wkt.startsWith("00")) {
      this.geometry = parseWkbBigEndian(geometryFactory, wkt);
    } else if (wkt.startsWith("01")) {
      this.geometry = parseWkbLittleEndian(geometryFactory, wkt);
    } else {
      this.geometry = geometryFactory.geometry(value);
    }
  }

  private Geometry parseCollection(final GeometryFactory geometryFactory, final ValueGetter data) {
    final int count = data.getInt();
    final Geometry[] geoms = new Geometry[count];
    parseGeometryArray(geometryFactory, data, geoms);
    return geometryFactory.geometry(geoms);
  }

  private double[] parseCoordinates(final int axisCount, final ValueGetter data, final boolean hasZ,
    final boolean hasM) {
    final int vertexCount = data.getInt();
    final double[] coordinates = new double[axisCount * vertexCount];
    int coordinateIndex = 0;

    if (hasM) {
      if (hasZ) {
        for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
          final double x = data.getDouble();
          final double y = data.getDouble();
          final double z = data.getDouble();
          final double m = data.getDouble();
          coordinates[coordinateIndex++] = x;
          coordinates[coordinateIndex++] = y;
          coordinates[coordinateIndex++] = z;
          coordinates[coordinateIndex++] = m;
        }
      } else {
        for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
          final double x = data.getDouble();
          final double y = data.getDouble();
          final double m = data.getDouble();
          coordinates[coordinateIndex++] = x;
          coordinates[coordinateIndex++] = y;
          coordinateIndex++; // Skip z
          coordinates[coordinateIndex++] = m;
        }
      }
    } else if (hasZ) {
      for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
        final double x = data.getDouble();
        final double y = data.getDouble();
        final double z = data.getDouble();
        coordinates[coordinateIndex++] = x;
        coordinates[coordinateIndex++] = y;
        coordinates[coordinateIndex++] = z;
      }
    } else {
      for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
        final double x = data.getDouble();
        final double y = data.getDouble();
        coordinates[coordinateIndex++] = x;
        coordinates[coordinateIndex++] = y;
      }
    }
    return coordinates;
  }

  private Geometry parseGeometry(final GeometryFactory geometryFactory, final ValueGetter data) {
    final int typeword = data.getInt();

    final int realtype = typeword & 0x1FFFFFFF;

    final boolean hasZ = (typeword & 0x80000000) != 0;
    final boolean hasM = (typeword & 0x40000000) != 0;
    final boolean hasS = (typeword & 0x20000000) != 0;

    GeometryFactory currentGeometryFactory = geometryFactory;
    if (hasS) {
      final int coordinateSystemId = data.getInt();
      if (coordinateSystemId >= 0
        && currentGeometryFactory.getHorizontalCoordinateSystemId() != coordinateSystemId) {
        currentGeometryFactory = currentGeometryFactory.convertSrid(coordinateSystemId);
      }
    }
    int axisCount;
    if (hasM) {
      axisCount = 4;
    } else if (hasZ) {
      axisCount = 3;
    } else {
      axisCount = 2;
    }
    if (axisCount != currentGeometryFactory.getAxisCount()) {
      currentGeometryFactory = currentGeometryFactory.convertAxisCount(axisCount);
    }
    Geometry geometry;
    switch (realtype) {
      case 1:
        geometry = parsePoint(currentGeometryFactory, data, hasZ, hasM);
      break;
      case 2:
        geometry = parseLineString(currentGeometryFactory, data, hasZ, hasM);
      break;
      case 3:
        geometry = parsePolygon(currentGeometryFactory, data, hasZ, hasM);
      break;
      case 4:
        geometry = parseMultiPoint(currentGeometryFactory, data);
      break;
      case 5:
        geometry = parseMultiLineString(currentGeometryFactory, data);
      break;
      case 6:
        geometry = parseMultiPolygon(currentGeometryFactory, data);
      break;
      case 7:
        geometry = parseCollection(currentGeometryFactory, data);
      break;
      default:
        throw new IllegalArgumentException("Unknown Geometry Type: " + realtype);
    }
    if (geometryFactory.isSameCoordinateSystem(currentGeometryFactory)) {
      return geometry;
    } else {
      return geometry.convertGeometry(geometryFactory);
    }
  }

  private void parseGeometryArray(final GeometryFactory geometryFactory, final ValueGetter data,
    final Geometry[] container) {
    for (int i = 0; i < container.length; ++i) {
      data.getByte(); // read endian
      container[i] = parseGeometry(geometryFactory, data);
    }
  }

  private LinearRing parseLinearRing(final GeometryFactory geometryFactory, final ValueGetter data,
    final boolean hasZ, final boolean hasM) {
    final int axisCount = geometryFactory.getAxisCount();
    final double[] coordinates = parseCoordinates(axisCount, data, hasZ, hasM);
    return geometryFactory.linearRing(axisCount, coordinates);
  }

  private LineString parseLineString(final GeometryFactory geometryFactory, final ValueGetter data,
    final boolean hasZ, final boolean hasM) {
    final int axisCount = geometryFactory.getAxisCount();
    final double[] coordinates = parseCoordinates(axisCount, data, hasZ, hasM);
    return geometryFactory.lineString(axisCount, coordinates);
  }

  private Geometry parseMultiLineString(final GeometryFactory geometryFactory,
    final ValueGetter data) {
    final int count = data.getInt();
    final LineString[] lines = new LineString[count];
    parseGeometryArray(geometryFactory, data, lines);
    if (lines.length == 1) {
      return lines[0];
    } else {
      return geometryFactory.lineal(lines);
    }
  }

  private Geometry parseMultiPoint(final GeometryFactory geometryFactory, final ValueGetter data) {
    final Point[] points = new Point[data.getInt()];
    parseGeometryArray(geometryFactory, data, points);
    if (points.length == 1) {
      return points[0];
    } else {
      return geometryFactory.punctual(points);
    }
  }

  private Geometry parseMultiPolygon(final GeometryFactory geometryFactory,
    final ValueGetter data) {
    final int count = data.getInt();
    final Polygon[] polys = new Polygon[count];
    parseGeometryArray(geometryFactory, data, polys);
    if (polys.length == 1) {
      return polys[0];
    } else {
      return geometryFactory.polygonal(polys);
    }
  }

  private Point parsePoint(final GeometryFactory geometryFactory, final ValueGetter data,
    final boolean hasZ, final boolean hasM) {
    final double x = data.getDouble();
    final double y = data.getDouble();

    if (hasM) {
      if (hasZ) {
        final double z = data.getDouble();
        final double m = data.getDouble();
        return geometryFactory.point(x, y, z, m);
      } else {
        final double m = data.getDouble();
        return geometryFactory.point(x, y, Double.NaN, m);
      }
    } else if (hasZ) {
      final double z = data.getDouble();
      return geometryFactory.point(x, y, z);
    } else {
      return geometryFactory.point(x, y);
    }
  }

  private Polygon parsePolygon(final GeometryFactory geometryFactory, final ValueGetter data,
    final boolean hasZ, final boolean hasM) {
    final int count = data.getInt();
    final LinearRing[] rings = new LinearRing[count];
    for (int i = 0; i < count; ++i) {
      rings[i] = parseLinearRing(geometryFactory, data, hasZ, hasM);
    }
    return geometryFactory.polygon(rings);
  }

  private Geometry parseWkbBigEndian(final GeometryFactory geometryFactory, final String wkb) {
    final ValueGetter valueGetter = new BigEndianValueGetter(wkb);
    return parseGeometry(geometryFactory, valueGetter);
  }

  private Geometry parseWkbLittleEndian(final GeometryFactory geometryFactory, final String wkb) {
    final ValueGetter valueGetter = new LittleEndianValueGetter(wkb);
    return parseGeometry(geometryFactory, valueGetter);
  }
}
