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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.util.Assert;

/**
 * Writes a {@link Geometry} into Well-Known Binary format.
 * Supports use of an {@link OutStream}, which allows easy use
 * with arbitary byte stream sinks.
 * <p>
 * The WKB format is specified in the
 * OGC <A HREF="http://www.opengis.org/techno/specs.htm"><i>Simple Features for SQL</i></a>
 * specification.
 * This implementation also supports the <b>Extended WKB</b>
 * standard. Extended WKB allows writing 3-dimensional coordinates
 * and including the geometry SRID value.
 * The presence of 3D coordinates is signified
 * by setting the high bit of the <tt>wkbType</tt> word.
 * The presence of an SRID is signified
 * by setting the third bit of the <tt>wkbType</tt> word.
 * EWKB format is upward compatible with the original SFS WKB format.
 * <p>
 * Empty Point cannot be represented in WKB; an
 * {@link IllegalArgumentException} will be thrown if one is
 * written.
 * <p>
 * The WKB specification does not support representing {@link LinearRing}s;
 * they will be written as {@link LineString}s.
 * <p>
 * This class is designed to support reuse of a single instance to read multiple
 * geometries. This class is not thread-safe; each thread should create its own
 * instance.
 *
 * <h3>Syntax</h3>
 * The following syntax specification describes the version of Well-Known Binary
 * supported by JTS.
 * <p>
 * <i>The specification uses a syntax language similar to that used in
 * the C language.  Bitfields are specified from hi-order to lo-order bits.</i>
 * <p>
 * <blockquote><pre>
 *
 * <b>byte</b> = 1 byte
 * <b>uint32</b> = 32 bit unsigned integer (4 bytes)
 * <b>double</b> = double precision number (8 bytes)
 *
 * abstract Point { }
 *
 * Point2D extends Point {
 *  <b>double</b> x;
 *  <b>double</b> y;
 * }
 *
 * Point3D extends Point {
 *  <b>double</b> x;
 *  <b>double</b> y;
 *  <b>double</b> z;
 * }
 *
 * LinearRing {
 *  <b>uint32</b> numPoints;
 *  Point points[numPoints];
 * }
 *
 * enum wkbGeometryType {
 *  wkbPoint = 1,
 *  wkbLineString = 2,
 *  wkbPolygon = 3,
 *  wkbMultiPoint = 4,
 *  wkbMultiLineString = 5,
 *  wkbMultiPolygon = 6,
 *  wkbGeometryCollection = 7
 * }
 *
 * enum byteOrder {
 *  wkbXDR = 0, // Big Endian
 *  wkbNDR = 1  // Little Endian
 * }
 *
 * WKBType {
 *  <b>uint32</b> wkbGeometryType : 8; // values from enum wkbGeometryType
 * }
 *
 * EWKBType {
 *  <b>uint32</b> is3D : 1;  // 0 = 2D, 1 = 3D
 *  <b>uint32</b> noData1 : 1;
 *  <b>uint32</b> hasSRID : 1;   // 0, no, 1 = yes
 *  <b>uint32</b> noData2 : 21;
 *  <b>uint32</b> wkbGeometryType : 8; // values from enum wkbGeometryType
 * }
 *
 * abstract WKBGeometry {
 *  <b>byte</b> byteOrder;  // values from enum byteOrder
 *  EWKBType wkbType
 *  [ <b>uint32</b> srid; ]  // only if hasSRID = yes
 * }
 *
 * WKBPoint extends WKBGeometry {
 *  Point point;
 * }
 *
 * WKBLineString extends WKBGeometry {
 *  <b>uint32</b> numCoords;
 *  Point points[numCoords];
 * }
 *
 * WKBPolygon extends WKBGeometry {
 *  <b>uint32</b> numRings;
 *  LinearRing rings[numRings];
 * }
 *
 * WKBMultiPoint extends WKBGeometry {
 *  <b>uint32</b> numElems;
 *  WKBPoint elems[numElems];
 * }
 *
 * WKBMultiLineString extends WKBGeometry {
 *  <b>uint32</b> numElems;
 *  WKBLineString elems[numElems];
 * }
 *
 * wkbMultiPolygon extends WKBGeometry {
 *  <b>uint32</b> numElems;
 *  WKBPolygon elems[numElems];
 * }
 *
 * WKBGeometryCollection extends WKBGeometry {
 *  <b>uint32</b> numElems;
 *  WKBGeometry elems[numElems];
 * }
 *
 * </pre></blockquote>
 * @see WKBReader
 */
public class WKBWriter {
  /**
   * Converts a byte array to a hexadecimal string.
   *
   * @param bytes
   * @return a string of hexadecimal digits
   *
   * @deprecated
   */
  @Deprecated
  public static String bytesToHex(final byte[] bytes) {
    return toHex(bytes);
  }

  /**
   * Converts a byte array to a hexadecimal string.
   *
   * @param bytes a byte array
   * @return a string of hexadecimal digits
   */
  public static String toHex(final byte[] bytes) {
    final StringBuilder buf = new StringBuilder();
    for (final byte b : bytes) {
      buf.append(toHexDigit(b >> 4 & 0x0F));
      buf.append(toHexDigit(b & 0x0F));
    }
    return buf.toString();
  }

  private static char toHexDigit(final int n) {
    if (n < 0 || n > 15) {
      throw new IllegalArgumentException("Nibble value out of range: " + n);
    }
    if (n <= 9) {
      return (char)('0' + n);
    }
    return (char)('A' + (n - 10));
  }

  // holds output data values
  private final byte[] buf = new byte[8];

  private final ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();

  private final OutStream byteArrayOutStream = new OutputStreamOutStream(this.byteArrayOS);

  private final int byteOrder;

  private boolean includeSRID = false;

  private int outputDimension = 2;

  /**
   * Creates a writer that writes {@link Geometry}s with
   * output dimension = 2 and BIG_ENDIAN byte order
   */
  public WKBWriter() {
    this(2, ByteOrderValues.BIG_ENDIAN);
  }

  /**
   * Creates a writer that writes {@link Geometry}s with
   * the given dimension (2 or 3) for output coordinates
   * and {@link ByteOrderValues#BIG_ENDIAN} byte order.
   *
   * @param outputDimension the coordinate dimension to output (2 or 3)
   */
  public WKBWriter(final int outputDimension) {
    this(outputDimension, ByteOrderValues.BIG_ENDIAN);
  }

  /**
   * Creates a writer that writes {@link Geometry}s with
   * the given dimension (2 or 3) for output coordinates
   * and {@link ByteOrderValues#BIG_ENDIAN} byte order. This constructor also
   * takes a flag to control whether srid information will be
   * written.
   *
   * @param outputDimension the coordinate dimension to output (2 or 3)
   * @param includeSRID indicates whether SRID should be written
   */
  public WKBWriter(final int outputDimension, final boolean includeSRID) {
    this(outputDimension, ByteOrderValues.BIG_ENDIAN, includeSRID);
  }

  /**
   * Creates a writer that writes {@link Geometry}s with
   * the given dimension (2 or 3) for output coordinates
   * and byte order
   *
   * @param outputDimension the coordinate dimension to output (2 or 3)
   * @param byteOrder the byte ordering to use
   */
  public WKBWriter(final int outputDimension, final int byteOrder) {
    this(outputDimension, byteOrder, false);
  }

  /**
   * Creates a writer that writes {@link Geometry}s with
   * the given dimension (2 or 3) for output coordinates
   * and byte order. This constructor also takes a flag to
   * control whether srid information will be written.
   *
   * @param outputDimension the coordinate dimension to output (2 or 3)
   * @param byteOrder the byte ordering to use
   * @param includeSRID indicates whether SRID should be written
   */
  public WKBWriter(final int outputDimension, final int byteOrder, final boolean includeSRID) {
    this.outputDimension = outputDimension;
    this.byteOrder = byteOrder;
    this.includeSRID = includeSRID;

    if (outputDimension < 2 || outputDimension > 3) {
      throw new IllegalArgumentException("Output dimension must be 2 or 3");
    }
  }

  /**
   * Writes a {@link Geometry} into a byte array.
   *
   * @param geom the geometry to write
   * @return the byte array containing the WKB
   */
  public byte[] write(final Geometry geom) {
    try {
      this.byteArrayOS.reset();
      write(geom, this.byteArrayOutStream);
    } catch (final IOException ex) {
      throw new RuntimeException("Unexpected IO exception: " + ex.getMessage());
    }
    return this.byteArrayOS.toByteArray();
  }

  /**
   * Writes a {@link Geometry} to an {@link OutStream}.
   *
   * @param geom the geometry to write
   * @param os the out stream to write to
   * @throws IOException if an I/O error occurs
   */
  public void write(final Geometry geom, final OutStream os) throws IOException {
    if (geom instanceof Point) {
      writePoint((Point)geom, os);
    } else if (geom instanceof LineString) {
      writeLineString((LineString)geom, os);
    } else if (geom instanceof Polygon) {
      writePolygon((Polygon)geom, os);
    } else if (geom instanceof Punctual) {
      writeGeometryCollection(WKBConstants.wkbMultiPoint, geom, os);
    } else if (geom instanceof Lineal) {
      writeGeometryCollection(WKBConstants.wkbMultiLineString, geom, os);
    } else if (geom instanceof Polygonal) {
      writeGeometryCollection(WKBConstants.wkbMultiPolygon, geom, os);
    } else if (geom.isGeometryCollection()) {
      writeGeometryCollection(WKBConstants.wkbGeometryCollection, geom, os);
    } else {
      Assert.shouldNeverReachHere("Unknown Geometry type");
    }
  }

  private void writeByteOrder(final OutStream os) throws IOException {
    if (this.byteOrder == ByteOrderValues.LITTLE_ENDIAN) {
      this.buf[0] = WKBConstants.wkbNDR;
    } else {
      this.buf[0] = WKBConstants.wkbXDR;
    }
    os.write(this.buf, 1);
  }

  private void writeCoordinate(final LineString seq, final int index, final OutStream os)
    throws IOException {
    ByteOrderValues.putDouble(seq.getX(index), this.buf, this.byteOrder);
    os.write(this.buf, 8);
    ByteOrderValues.putDouble(seq.getY(index), this.buf, this.byteOrder);
    os.write(this.buf, 8);

    // only write 3rd dim if caller has requested it for this writer
    if (this.outputDimension >= 3) {
      // if 3rd dim is requested, only write it if the LineString provides
      // it
      double coordinate = Double.NaN;
      if (seq.getAxisCount() >= 3) {
        coordinate = seq.getCoordinate(index, 2);
      }
      ByteOrderValues.putDouble(coordinate, this.buf, this.byteOrder);
      os.write(this.buf, 8);
    }
  }

  private void writeCoordinates(final Point seq, final boolean writeSize, final OutStream os)
    throws IOException {
    ByteOrderValues.putDouble(seq.getX(), this.buf, this.byteOrder);
    os.write(this.buf, 8);
    ByteOrderValues.putDouble(seq.getY(), this.buf, this.byteOrder);
    os.write(this.buf, 8);

    // only write 3rd dim if caller has requested it for this writer
    if (this.outputDimension >= 3) {
      // if 3rd dim is requested, only write it if the LineString provides
      // it
      double coordinate = Double.NaN;
      if (seq.getAxisCount() >= 3) {
        coordinate = seq.getCoordinate(2);
      }
      ByteOrderValues.putDouble(coordinate, this.buf, this.byteOrder);
      os.write(this.buf, 8);
    }
  }

  private void writeCoordinateSequence(final LineString seq, final boolean writeSize,
    final OutStream os) throws IOException {
    int vertexCount;
    if (seq == null) {
      vertexCount = 0;
    } else {
      vertexCount = seq.getVertexCount();
    }
    if (writeSize) {
      writeInt(vertexCount, os);
    }
    for (int i = 0; i < vertexCount; i++) {
      writeCoordinate(seq, i, os);
    }
  }

  private void writeGeometryCollection(final int geometryType, final Geometry geometry,
    final OutStream os) throws IOException {
    writeByteOrder(os);
    writeGeometryType(geometryType, geometry, os);
    writeInt(geometry.getGeometryCount(), os);
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      write(geometry.getGeometry(i), os);
    }
  }

  private void writeGeometryType(final int geometryType, final Geometry g, final OutStream os)
    throws IOException {
    final int flag3D = this.outputDimension == 3 ? 0x80000000 : 0;
    int typeInt = geometryType | flag3D;
    typeInt |= this.includeSRID ? 0x20000000 : 0;
    writeInt(typeInt, os);
    if (this.includeSRID) {
      writeInt(g.getHorizontalCoordinateSystemId(), os);
    }
  }

  private void writeInt(final int intValue, final OutStream os) throws IOException {
    ByteOrderValues.putInt(intValue, this.buf, this.byteOrder);
    os.write(this.buf, 4);
  }

  private void writeLineString(final LineString line, final OutStream os) throws IOException {
    writeByteOrder(os);
    writeGeometryType(WKBConstants.wkbLineString, line, os);
    writeCoordinateSequence(line, true, os);
  }

  private void writePoint(final Point point, final OutStream os) throws IOException {
    if (point.isEmpty()) {
      writeGeometryCollection(WKBConstants.wkbMultiPoint, point, os);
    } else {
      writeByteOrder(os);
      writeGeometryType(WKBConstants.wkbPoint, point, os);
      writeCoordinates(point, false, os);
    }
  }

  private void writePolygon(final Polygon poly, final OutStream os) throws IOException {
    writeByteOrder(os);
    writeGeometryType(WKBConstants.wkbPolygon, poly, os);
    if (poly.isEmpty()) {
      writeInt(0, os);
    } else {
      writeInt(poly.getRingCount(), os);
      writeCoordinateSequence(poly.getShell(), true, os);
      for (int i = 0; i < poly.getHoleCount(); i++) {
        writeCoordinateSequence(poly.getHole(i), true, os);
      }
    }
  }
}
