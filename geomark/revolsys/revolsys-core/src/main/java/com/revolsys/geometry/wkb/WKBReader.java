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
package com.revolsys.geometry.wkb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;

/**
 * Reads a {@link Geometry}from a byte stream in Well-Known Binary format.
 * Supports use of an {@link InStream}, which allows easy use
 * with arbitrary byte stream sources.
 * <p>
 * This class reads the format describe in {@link WKBWriter}.
 * It also partially handles
 * the <b>Extended WKB</b> format used by PostGIS,
 * by parsing and storing SRID values.
 * The reader repairs structurally-invalid input
 * (specifically, LineStrings and LinearRings which contain
 * too few points have vertices added,
 * and non-closed rings are closed).
 * <p>
 * This class is designed to support reuse of a single instance to read multiple
 * geometries. This class is not thread-safe; each thread should create its own
 * instance.
 *
 * @see WKBWriter for a formal format specification
 */
public class WKBReader {
  private static final String INVALID_GEOM_TYPE_MSG = "Invalid geometry type encountered in ";

  /**
   * Converts a hexadecimal string to a byte array.
   * The hexadecimal digit symbols are case-insensitive.
   *
   * @param hex a string containing hex digits
   * @return an array of bytes with the value of the hex string
   */
  public static byte[] hexToBytes(final String hex) {
    final int byteLen = hex.length() / 2;
    final byte[] bytes = new byte[byteLen];

    for (int i = 0; i < hex.length() / 2; i++) {
      final int i2 = 2 * i;
      if (i2 + 1 > hex.length()) {
        throw new IllegalArgumentException("Hex string has odd length");
      }

      final int nib1 = hexToInt(hex.charAt(i2));
      final int nib0 = hexToInt(hex.charAt(i2 + 1));
      final byte b = (byte)((nib1 << 4) + (byte)nib0);
      bytes[i] = b;
    }
    return bytes;
  }

  private static int hexToInt(final char hex) {
    final int nib = Character.digit(hex, 16);
    if (nib < 0) {
      throw new IllegalArgumentException("Invalid hex digit: '" + hex + "'");
    }
    return nib;
  }

  private final ByteOrderDataInStream dis = new ByteOrderDataInStream();

  private final GeometryFactory geometryFactory;

  public WKBReader() {
    this(GeometryFactory.DEFAULT_3D);
  }

  public WKBReader(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  /**
   * Reads a single {@link Geometry} in WKB format from a byte array.
   *
   * @param bytes the byte array to read from
   * @return the geometry read
   * @throws ParseException if the WKB is ill-formed
   */
  public Geometry read(final byte[] bytes) throws ParseException {
    // possibly reuse the ByteArrayInStream?
    // don't throw IOExceptions, since we are not doing any I/O
    try {
      return read(new ByteArrayInStream(bytes));
    } catch (final IOException ex) {
      throw new RuntimeException("Unexpected IOException caught: " + ex.getMessage());
    }
  }

  /**
   * Reads a {@link Geometry} in binary WKB format from an {@link InStream}.
   *
   * @param is the stream to read from
   * @return the Geometry read
   * @throws IOException if the underlying stream creates an error
   * @throws ParseException if the WKB is ill-formed
   */
  public Geometry read(final InStream is) throws IOException, ParseException {
    this.dis.setInStream(is);
    final Geometry geometry = readGeometry(this.geometryFactory);
    return geometry;
  }

  private double[] readCoordinates(final int axisCount, final int vertexCount) throws IOException {
    final double[] coordinates = new double[vertexCount * axisCount];

    int coordinateIndex = 0;
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final double coordinate = this.dis.readDouble();
        final double coordinatePrecise = this.geometryFactory.makePrecise(axisIndex, coordinate);
        coordinates[coordinateIndex++] = coordinatePrecise;
      }
    }
    return coordinates;
  }

  private Geometry readGeometry(GeometryFactory geometryFactory)
    throws IOException, ParseException {

    // determine byte order
    final byte byteOrderWKB = this.dis.readByte();

    // always set byte order, since it may change from geometry to geometry
    if (byteOrderWKB == WKBConstants.wkbNDR) {
      this.dis.setOrder(ByteOrderValues.LITTLE_ENDIAN);
    } else if (byteOrderWKB == WKBConstants.wkbXDR) {
      this.dis.setOrder(ByteOrderValues.BIG_ENDIAN);
    } else {
      throw new ParseException("Unknown geometry byte order (not NDR or XDR): " + byteOrderWKB);
    }

    final int typeInt = this.dis.readInt();
    final int geometryType = typeInt & 0xff;
    // determine if Z values are present
    final boolean hasZ = (typeInt & 0x80000000) != 0;
    if (hasZ) {
      geometryFactory = geometryFactory.convertAxisCount(3);
    } else {
      geometryFactory = geometryFactory.convertAxisCount(2);
    }

    // determine if SRIDs are present
    final boolean hasSRID = (typeInt & 0x20000000) != 0;
    int coordinateSystemId = 0;
    if (hasSRID) {
      coordinateSystemId = this.dis.readInt();
      if (coordinateSystemId != geometryFactory.getHorizontalCoordinateSystemId()) {
        geometryFactory = geometryFactory.convertSrid(coordinateSystemId);
      }
    }

    Geometry geom = null;
    switch (geometryType) {
      case WKBConstants.wkbPoint:
        geom = readPoint(geometryFactory);
      break;
      case WKBConstants.wkbLineString:
        geom = readLineString(geometryFactory);
      break;
      case WKBConstants.wkbPolygon:
        geom = readPolygon(geometryFactory);
      break;
      case WKBConstants.wkbMultiPoint:
        geom = readMultiPoint(geometryFactory);
      break;
      case WKBConstants.wkbMultiLineString:
        geom = readMultiLineString(geometryFactory);
      break;
      case WKBConstants.wkbMultiPolygon:
        geom = readMultiPolygon(geometryFactory);
      break;
      case WKBConstants.wkbGeometryCollection:
        geom = readGeometryCollection(geometryFactory);
      break;
      default:
        throw new ParseException("Unknown WKB type " + geometryType);
    }
    return geom;
  }

  private Geometry readGeometryCollection(final GeometryFactory geometryFactory)
    throws IOException, ParseException {
    final int geometryCount = this.dis.readInt();
    final List<Geometry> geometries = new ArrayList<>(geometryCount);
    for (int i = 0; i < geometryCount; i++) {
      final Geometry geometry = readGeometry(geometryFactory);
      geometries.add(geometry);
    }
    return geometryFactory.geometry(geometries);
  }

  private LinearRing readLinearRing(final GeometryFactory geometryFactory) throws IOException {
    final int vertexCount = this.dis.readInt();
    final int axisCount = geometryFactory.getAxisCount();
    if (vertexCount == 0) {
      return geometryFactory.linearRing();
    } else {
      final double[] coordinates = readCoordinates(axisCount, vertexCount);
      final int lastIndex = (vertexCount - 1) * axisCount;
      if (coordinates[0] == coordinates[lastIndex]) {
        if (coordinates[1] == coordinates[lastIndex + 1]) {
          return geometryFactory.linearRing(axisCount, coordinates);
        }
      }
      final double[] newCoordinates = new double[coordinates.length + axisCount];
      System.arraycopy(coordinates, 0, newCoordinates, 0, coordinates.length);
      System.arraycopy(coordinates, 0, newCoordinates, lastIndex, axisCount);
      return geometryFactory.linearRing(axisCount, newCoordinates);
    }
  }

  private LineString readLineString(final GeometryFactory geometryFactory) throws IOException {
    final int vertexCount = this.dis.readInt();
    if (vertexCount == 0) {
      return geometryFactory.lineString();
    } else {
      final int axisCount = geometryFactory.getAxisCount();
      final double[] coordinates = readCoordinates(axisCount, vertexCount);
      return geometryFactory.lineString(axisCount, coordinates);
    }
  }

  private Lineal readMultiLineString(final GeometryFactory geometryFactory)
    throws IOException, ParseException {
    final int geometryCount = this.dis.readInt();
    final LineString[] lines = new LineString[geometryCount];
    for (int i = 0; i < geometryCount; i++) {
      final Geometry geometry = readGeometry(geometryFactory);
      if (!(geometry instanceof LineString)) {
        throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiLineString");
      }
      lines[i] = (LineString)geometry;
    }
    return geometryFactory.lineal(lines);
  }

  private Punctual readMultiPoint(final GeometryFactory geometryFactory)
    throws IOException, ParseException {
    final int geometryCount = this.dis.readInt();
    final Point[] points = new Point[geometryCount];
    for (int i = 0; i < geometryCount; i++) {
      final Geometry geometry = readGeometry(geometryFactory);
      if (!(geometry instanceof Point)) {
        throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiPoint");
      }
      points[i] = (Point)geometry;
    }
    return geometryFactory.punctual(points);
  }

  private Polygonal readMultiPolygon(final GeometryFactory geometryFactory)
    throws IOException, ParseException {
    final int geometryCount = this.dis.readInt();
    final Polygon[] polygons = new Polygon[geometryCount];

    for (int i = 0; i < geometryCount; i++) {
      final Geometry g = readGeometry(geometryFactory);
      if (!(g instanceof Polygon)) {
        throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiPolygon");
      }
      polygons[i] = (Polygon)g;
    }
    return geometryFactory.polygonal(polygons);
  }

  private Point readPoint(final GeometryFactory geometryFactory) throws IOException {
    final int axisCount = geometryFactory.getAxisCount();
    final double[] coordinates = readCoordinates(axisCount, 1);
    return geometryFactory.point(coordinates);
  }

  private Polygon readPolygon(final GeometryFactory geometryFactory) throws IOException {
    final int ringCount = this.dis.readInt();
    final List<LinearRing> rings = new ArrayList<>();

    for (int i = 0; i < ringCount; i++) {
      final LinearRing ring = readLinearRing(geometryFactory);
      rings.add(ring);
    }
    return geometryFactory.polygon(rings);
  }

}
