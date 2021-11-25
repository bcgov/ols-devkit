/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.record.io.format.wkt;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;

public class EWktWriter {

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
        wkt.append('(');
        first = false;
      } else {
        wkt.append(',');
      }
      append(wkt, axisCount, point);
    }
    if (first) {
      wkt.append(" EMPTY");
    } else {
      wkt.append(')');
    }
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

  public static String toString(final Geometry geometry) {
    final StringWriter out = new StringWriter();
    final int srid = geometry.getHorizontalCoordinateSystemId();
    if (srid > 0) {
      out.write("SRID=");
      out.write(Integer.toString(srid));
      out.write(';');
    }
    write(out, geometry);
    out.flush();
    return out.toString();
  }

  public static String toString(final Geometry geometry, final boolean ewkt) {
    if (ewkt) {
      return toString(geometry);
    } else {
      return WktWriter.toString(geometry);
    }
  }

  public static void write(final Writer out, final Geometry geometry) {
    if (geometry != null) {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        write(out, point);
      } else if (geometry instanceof Punctual) {
        final Punctual punctual = (Punctual)geometry;
        write(out, punctual);
      } else if (geometry instanceof LinearRing) {
        final LinearRing line = (LinearRing)geometry;
        write(out, line);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        write(out, line);
      } else if (geometry instanceof Lineal) {
        final Lineal lineal = (Lineal)geometry;
        write(out, lineal);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        write(out, polygon);
      } else if (geometry instanceof Polygonal) {
        final Polygonal polygonal = (Polygonal)geometry;
        write(out, polygonal);
      } else if (geometry.isGeometryCollection()) {
        writeGeometryCollection(out, geometry);
      } else {
        throw new IllegalArgumentException("Unknown geometry type" + geometry.getClass());
      }
    }
  }

  public static void write(final Writer writer, final Geometry geometry, final boolean ewkt) {
    if (ewkt) {
      try {
        final int srid = geometry.getHorizontalCoordinateSystemId();
        if (srid > 0) {
          writer.write("SRID=");
          writer.write(Integer.toString(srid));
          writer.write(';');
        }
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }

      write(writer, geometry);
    } else {
      WktWriter.write(writer, geometry);
    }
  }

  private static void write(final Writer out, final Geometry geometry, final int axisCount)
    throws IOException {
    if (geometry != null) {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        write(out, point, axisCount);
      } else if (geometry instanceof Punctual) {
        final Punctual punctual = (Punctual)geometry;
        write(out, punctual, axisCount);
      } else if (geometry instanceof LinearRing) {
        final LinearRing line = (LinearRing)geometry;
        write(out, line, axisCount);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        write(out, line, axisCount);
      } else if (geometry instanceof Lineal) {
        final Lineal lineal = (Lineal)geometry;
        write(out, lineal, axisCount);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        write(out, polygon, axisCount);
      } else if (geometry instanceof Polygonal) {
        final Polygonal polygonal = (Polygonal)geometry;
        write(out, polygonal, axisCount);
      } else if (geometry.isGeometryCollection()) {
        writeGeometryCollection(out, geometry, axisCount);
      } else {
        throw new IllegalArgumentException("Unknown geometry type" + geometry.getClass());
      }
    }
  }

  public static void write(final Writer out, final Lineal lineal) {
    final int axisCount = Math.min(lineal.getAxisCount(), 4);
    try {
      write(out, lineal, axisCount);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private static void write(final Writer out, final Lineal lineal, final int axisCount)
    throws IOException {
    writeGeometryType(out, "MULTILINESTRING", axisCount);
    if (lineal.isEmpty()) {
      out.write(" EMPTY");
    } else {
      out.write("(");
      LineString line = (LineString)lineal.getGeometry(0);
      LineString points = line;
      writeCoordinates(out, points, axisCount);
      for (int i = 1; i < lineal.getGeometryCount(); i++) {
        out.write(",");
        line = (LineString)lineal.getGeometry(i);
        points = line;
        writeCoordinates(out, points, axisCount);
      }
      out.write(")");
    }
  }

  public static void write(final Writer out, final LinearRing line) {
    final int axisCount = Math.min(line.getAxisCount(), 4);
    try {
      write(out, line, axisCount);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private static void write(final Writer out, final LinearRing line, final int axisCount)
    throws IOException {
    writeGeometryType(out, "LINEARRING", axisCount);
    if (line.isEmpty()) {
      out.write(" EMPTY");
    } else {
      final LineString coordinates = line;
      writeCoordinates(out, coordinates, axisCount);
    }
  }

  public static void write(final Writer out, final LineString line) {
    final int axisCount = Math.min(line.getAxisCount(), 4);
    try {
      write(out, line, axisCount);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private static void write(final Writer out, final LineString line, final int axisCount)
    throws IOException {
    writeGeometryType(out, "LINESTRING", axisCount);
    if (line.isEmpty()) {
      out.write(" EMPTY");
    } else {
      final LineString coordinates = line;
      writeCoordinates(out, coordinates, axisCount);
    }
  }

  private static void write(final Writer out, final LineString coordinates, final int index,
    final int axisCount) throws IOException {
    writeCoordinate(out, coordinates, index, 0);
    for (int j = 1; j < axisCount; j++) {
      out.write(' ');
      writeCoordinate(out, coordinates, index, j);
    }
  }

  public static void write(final Writer out, final Point point) {
    final int axisCount = Math.min(point.getAxisCount(), 4);
    try {
      write(out, point, axisCount);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private static void write(final Writer out, final Point point, final int axisCount)
    throws IOException {
    writeGeometryType(out, "POINT", axisCount);
    if (point.isEmpty()) {
      out.write(" EMPTY");
    } else {
      out.write("(");
      writeCoordinates(out, point, axisCount);
      out.write(')');
    }
  }

  public static void write(final Writer out, final Polygon polygon) {
    final int axisCount = Math.min(polygon.getAxisCount(), 4);
    try {
      write(out, polygon, axisCount);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private static void write(final Writer out, final Polygon polygon, final int axisCount)
    throws IOException {
    writeGeometryType(out, "POLYGON", axisCount);
    if (polygon.isEmpty()) {
      out.write(" EMPTY");
    } else {
      writePolygonRings(out, polygon, axisCount);
    }
  }

  public static void write(final Writer out, final Polygonal polygonal) {
    final int axisCount = Math.min(polygonal.getAxisCount(), 4);
    try {
      write(out, polygonal, axisCount);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private static void write(final Writer out, final Polygonal polygonal, final int axisCount)
    throws IOException {
    writeGeometryType(out, "MULTIPOLYGON", axisCount);
    if (polygonal.isEmpty()) {
      out.write(" EMPTY");
    } else {
      out.write("(");

      Polygon polygon = (Polygon)polygonal.getGeometry(0);
      writePolygonRings(out, polygon, axisCount);
      for (int i = 1; i < polygonal.getGeometryCount(); i++) {
        out.write(",");
        polygon = (Polygon)polygonal.getGeometry(i);
        writePolygonRings(out, polygon, axisCount);
      }
      out.write(")");
    }
  }

  public static void write(final Writer out, final Punctual punctual) {
    final int axisCount = Math.min(punctual.getAxisCount(), 4);
    try {
      write(out, punctual, axisCount);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private static void write(final Writer out, final Punctual punctual, final int axisCount)
    throws IOException {
    writeGeometryType(out, "MULTIPOINT", axisCount);
    if (punctual.isEmpty()) {
      out.write(" EMPTY");
    } else {
      Point point = punctual.getPoint(0);
      out.write("((");
      writeCoordinates(out, point, axisCount);
      for (int i = 1; i < punctual.getGeometryCount(); i++) {
        out.write("),(");
        point = punctual.getPoint(i);
        writeCoordinates(out, point, axisCount);
      }
      out.write("))");
    }
  }

  private static void writeAxis(final Writer out, final int axisCount) throws IOException {
    if (axisCount > 3) {
      out.write(" ZM");
    } else if (axisCount > 2) {
      out.write(" Z");
    }
  }

  public static void writeCCW(final Writer out, final Geometry geometry) {
    if (geometry != null) {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        write(out, point);
      } else if (geometry instanceof Punctual) {
        final Punctual punctual = (Punctual)geometry;
        write(out, punctual);
      } else if (geometry instanceof LinearRing) {
        final LinearRing line = (LinearRing)geometry;
        write(out, line);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        write(out, line);
      } else if (geometry instanceof Lineal) {
        final Lineal lineal = (Lineal)geometry;
        write(out, lineal);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        writeCCWPolygon(out, polygon);
      } else if (geometry instanceof Polygonal) {
        final Polygonal polygonal = (Polygonal)geometry;
        writeCCWPolygonal(out, polygonal);
      } else if (geometry.isGeometryCollection()) {
        writeCCWGeometryCollection(out, geometry);
      } else {
        throw new IllegalArgumentException("Unknown geometry type" + geometry.getClass());
      }
    }
  }

  private static void writeCCW(final Writer out, final Geometry geometry, final int axisCount)
    throws IOException {
    if (geometry != null) {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        write(out, point, axisCount);
      } else if (geometry instanceof Punctual) {
        final Punctual punctual = (Punctual)geometry;
        write(out, punctual, axisCount);
      } else if (geometry instanceof LinearRing) {
        final LinearRing line = (LinearRing)geometry;
        write(out, line, axisCount);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        write(out, line, axisCount);
      } else if (geometry instanceof Lineal) {
        final Lineal lineal = (Lineal)geometry;
        write(out, lineal, axisCount);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        writeCCWPolygon(out, polygon, axisCount);
      } else if (geometry instanceof Polygonal) {
        final Polygonal polygonal = (Polygonal)geometry;
        writeCCWPolygonal(out, polygonal, axisCount);
      } else if (geometry.isGeometryCollection()) {
        writeCCWGeometryCollection(out, geometry, axisCount);
      } else {
        throw new IllegalArgumentException("Unknown geometry type" + geometry.getClass());
      }
    }
  }

  private static void writeCCWGeometryCollection(final Writer out, final Geometry multiGeometry) {
    final int axisCount = Math.min(multiGeometry.getAxisCount(), 4);
    try {
      writeCCWGeometryCollection(out, multiGeometry, axisCount);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private static void writeCCWGeometryCollection(final Writer out, final Geometry multiGeometry,
    final int axisCount) throws IOException {
    writeGeometryType(out, "GEOMETRYCOLLECTION", axisCount);
    if (multiGeometry.isEmpty()) {
      out.write(" EMPTY");
    } else {
      out.write("(");
      Geometry geometry = multiGeometry.getGeometry(0);
      writeCCW(out, geometry, axisCount);
      for (int i = 1; i < multiGeometry.getGeometryCount(); i++) {
        out.write(',');
        geometry = multiGeometry.getGeometry(i);
        writeCCW(out, geometry, axisCount);
      }
      out.write(')');
    }
  }

  private static void writeCCWPolygon(final Writer out, final Polygon polygon) {
    final int axisCount = Math.min(polygon.getAxisCount(), 4);
    try {
      writeCCWPolygon(out, polygon, axisCount);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private static void writeCCWPolygon(final Writer out, final Polygon polygon, final int axisCount)
    throws IOException {
    writeGeometryType(out, "POLYGON", axisCount);
    if (polygon.isEmpty()) {
      out.write(" EMPTY");
    } else {
      writeCCWPolygonRings(out, polygon, axisCount);
    }
  }

  private static void writeCCWPolygonal(final Writer out, final Polygonal polygonal) {
    final int axisCount = Math.min(polygonal.getAxisCount(), 4);
    try {
      writeCCWPolygonal(out, polygonal, axisCount);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private static void writeCCWPolygonal(final Writer out, final Polygonal polygonal,
    final int axisCount) throws IOException {
    writeGeometryType(out, "MULTIPOLYGON", axisCount);
    if (polygonal.isEmpty()) {
      out.write(" EMPTY");
    } else {
      out.write("(");

      Polygon polygon = (Polygon)polygonal.getGeometry(0);
      writeCCWPolygonRings(out, polygon, axisCount);
      for (int i = 1; i < polygonal.getGeometryCount(); i++) {
        out.write(",");
        polygon = (Polygon)polygonal.getGeometry(i);
        writeCCWPolygonRings(out, polygon, axisCount);
      }
      out.write(")");
    }
  }

  private static void writeCCWPolygonRings(final Writer out, final Polygon polygon,
    final int axisCount) throws IOException {
    out.write('(');
    final LinearRing shell = polygon.getShell().toCounterClockwise();
    writeCoordinates(out, shell, axisCount);
    for (final LinearRing hole : polygon.holes()) {
      out.write(',');
      final LinearRing clockwiseHole = hole.toClockwise();
      writeCoordinates(out, clockwiseHole, axisCount);
    }
    out.write(')');
  }

  private static void writeCoordinate(final Writer out, final LineString coordinates,
    final int index, final int ordinateIndex) throws IOException {
    if (ordinateIndex > coordinates.getAxisCount()) {
      out.write('0');
    } else {
      final double ordinate = coordinates.getCoordinate(index, ordinateIndex);
      if (Double.isNaN(ordinate)) {
        out.write('0');
      } else {
        Doubles.write(out, ordinate);
      }
    }
  }

  private static void writeCoordinates(final Writer out, final LineString coordinates,
    final int axisCount) throws IOException {
    if (coordinates != null) {
      out.write('(');
      write(out, coordinates, 0, axisCount);
      for (int i = 1; i < coordinates.getVertexCount(); i++) {
        out.write(',');
        write(out, coordinates, i, axisCount);
      }
      out.write(')');
    }
  }

  private static void writeCoordinates(final Writer out, final Point point, final int axisCount)
    throws IOException {
    final double x = point.getX();
    Doubles.write(out, x);
    out.write(' ');
    final double y = point.getY();
    Doubles.write(out, y);
    for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
      out.write(' ');
      final double cordinate = point.getCoordinate(axisIndex);
      Doubles.write(out, cordinate);
    }
  }

  public static void writeGeometryCollection(final Writer out, final Geometry multiGeometry) {
    final int axisCount = Math.min(multiGeometry.getAxisCount(), 4);
    try {
      writeGeometryCollection(out, multiGeometry, axisCount);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private static void writeGeometryCollection(final Writer out, final Geometry multiGeometry,
    final int axisCount) throws IOException {
    writeGeometryType(out, "GEOMETRYCOLLECTION", axisCount);
    if (multiGeometry.isEmpty()) {
      out.write(" EMPTY");
    } else {
      out.write("(");
      Geometry geometry = multiGeometry.getGeometry(0);
      write(out, geometry, axisCount);
      for (int i = 1; i < multiGeometry.getGeometryCount(); i++) {
        out.write(',');
        geometry = multiGeometry.getGeometry(i);
        write(out, geometry, axisCount);
      }
      out.write(')');
    }
  }

  private static void writeGeometryType(final Writer out, final String geometryType,
    final int axisCount) throws IOException {
    out.write(geometryType);
    writeAxis(out, axisCount);
  }

  private static void writePolygonRings(final Writer out, final Polygon polygon,
    final int axisCount) throws IOException {
    out.write('(');
    final LinearRing shell = polygon.getShell();
    writeCoordinates(out, shell, axisCount);
    for (final LinearRing hole : polygon.holes()) {
      out.write(',');
      writeCoordinates(out, hole, axisCount);
    }
    out.write(')');
  }
}
