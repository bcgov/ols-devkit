package com.revolsys.record.io.format.wkt;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.io.FileUtil;
import com.revolsys.util.Property;

public class WktParser {

  public static boolean hasChar(final PushbackReader reader, final char expected)
    throws IOException {
    skipWhitespace(reader);
    final int character = reader.read();
    if (character < 0) {
      return false;
    } else if (character == expected) {
      return true;
    } else {
      reader.unread(character);
      return false;
    }
  }

  public static boolean hasSpace(final PushbackReader reader) throws IOException {
    final int character = reader.read();
    if (character < 0) {
      return false;
    } else if (character == ' ') {
      return true;
    } else {
      reader.unread(character);
      return false;
    }
  }

  public static boolean hasText(final PushbackReader reader, final String expected)
    throws IOException {
    final int length = expected.length();
    final char[] characters = new char[length];
    final int characterCount = reader.read(characters);
    if (characterCount == length) {
      if (expected.equals(new String(characters))) {
        return true;
      }
    } else if (characterCount < 0) {
      return false;
    }
    reader.unread(characters, 0, characterCount);
    return false;
  }

  public static double parseDouble(final PushbackReader reader) throws IOException {
    int digitCount = 0;
    long number = 0;
    boolean negative = false;
    double decimalDivisor = -1;
    for (int character = reader.read(); character != -1; character = reader.read()) {
      if (character == '#') {
        if (number == 1 && decimalDivisor == 1) {
          final int character2 = reader.read();
          if (character2 == 'Q') {
            if (hasText(reader, "NAN")) {
              return Double.NaN;
            }
          } else if (character2 == 'I') {
            if (hasText(reader, "NF")) {
              if (negative) {
                return Double.NEGATIVE_INFINITY;
              } else {
                return Double.POSITIVE_INFINITY;
              }
            } else if (hasText(reader, "ND")) {
              return Double.NaN;
            }
          }
        }
        reader.unread(character);
        throw new IllegalArgumentException(
          "Invalid WKT geometry. Expecting #QNAN oe #INF or #IND not "
            + FileUtil.getString(reader, 50));
      } else if (character == 'N' || character == 'n') {
        if (digitCount == 0) {
          final int character2 = reader.read();
          if (character2 == 'a' || character2 == 'A') {
            final int character3 = reader.read();
            if (character3 == 'N' || character == 'n') {
              return Double.NaN;
            }
            reader.unread(character3);
          }
          reader.unread(character2);

        }
        reader.unread(character);
        throw new IllegalArgumentException(
          "Invalid WKT geometry. Expecting NaN not " + FileUtil.getString(reader, 50));
      } else if (character == 'I') {
        if (hasText(reader, "nfinity")) {
          if (negative) {
            return Double.NEGATIVE_INFINITY;
          } else {
            return Double.POSITIVE_INFINITY;
          }
        }
        reader.unread(character);
        throw new IllegalArgumentException(
          "Invalid WKT geometry. Expecting Infinity not " + FileUtil.getString(reader, 50));
      } else if (character == '.') {
        if (decimalDivisor == -1) {
          decimalDivisor = 1;
        } else {
          break;
        }
      } else if (character == '-') {
        if (digitCount == 0 && !negative) {
          negative = true;
        } else {
          reader.unread(character);
          break;
        }
      } else if (character >= '0' && character <= '9') {
        digitCount++;
        if (digitCount < 19) {
          number = number * 10 + character - '0';
          if (decimalDivisor != -1) {
            decimalDivisor *= 10;
          }
        }
      } else {
        reader.unread(character);
        break;
      }
    }
    if (digitCount == 0) {
      throw new IllegalArgumentException("Invalid WKT geometry. No number found");
    } else {
      double doubleNumber;
      if (decimalDivisor > 1) {
        doubleNumber = number / decimalDivisor;
      } else {
        doubleNumber = number;
      }
      if (negative) {
        return -doubleNumber;
      } else {
        return doubleNumber;
      }
    }
  }

  public static Integer parseInteger(final PushbackReader reader) throws IOException {
    int digitCount = 0;
    int number = 0;
    boolean negative = false;
    for (int character = reader.read(); character != -1; character = reader.read()) {
      if (character == '-') {
        if (digitCount == 0 && !negative) {
          negative = true;
        } else {
          reader.unread(character);
          break;
        }
      } else if (character >= '0' && character <= '9') {
        number = number * 10 + character - '0';
        digitCount++;
      } else {
        reader.unread(character);
        break;
      }
    }
    if (digitCount == 0) {
      return null;
    } else if (negative) {
      return -number;
    } else {
      return number;
    }
  }

  public static void skipWhitespace(final PushbackReader reader) throws IOException {
    for (int character = reader.read(); character != -1; character = reader.read()) {
      if (!Character.isWhitespace(character)) {
        reader.unread(character);
        return;
      }
    }
  }

  private final GeometryFactory geometryFactory;

  public WktParser() {
    this(GeometryFactory.DEFAULT_3D);
  }

  public WktParser(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  private int getAxisCount(final PushbackReader reader) throws IOException {
    skipWhitespace(reader);
    final int character = reader.read();
    switch (character) {
      case '(':
      case 'E':
        reader.unread(character);
        return 2;
      case 'M':
        return 4;
      case 'X':
        final int yChar = reader.read();
        if (yChar == 'Y') {
          final int zChar = reader.read();
          if (zChar == 'Z') {
            final int zNext = reader.read();
            if (zNext == 'M') {
              return 4;
            } else {
              reader.unread(zNext);
              return 3;
            }
          } else {
            reader.unread(zChar);
          }
          return 2;
        }
        reader.unread(yChar);
        return 2;
      case 'Z':
        final int zNext = reader.read();
        if (zNext == 'M') {
          return 4;
        } else {
          reader.unread(zNext);
          return 3;
        }
      default:
        reader.unread(character);
        throw new IllegalArgumentException(
          "Invalid WKT geometry. Expecting Z, M, ZM, (, or EMPTY not: "
            + FileUtil.getString(reader, 50));
    }
  }

  private boolean isEmpty(final PushbackReader reader) throws IOException {
    skipWhitespace(reader);
    if (hasText(reader, "EMPTY")) {
      skipWhitespace(reader);
      return true;
    } else {
      return false;
    }
  }

  private LineStringEditor parseCoordinatesLineString(final GeometryFactory geometryFactory,
    final PushbackReader reader, int axisCount) throws IOException {
    final LineStringEditor line = geometryFactory.newLineStringEditor();
    skipWhitespace(reader);
    int character = reader.read();
    if (character == '(') {
      int axisIndex = 0;
      int vertexIndex = 0;
      while (true) {
        final double number;
        try {
          number = parseDouble(reader);
        } catch (final IllegalArgumentException e) {
          character = reader.read();
          if (character == ')') {
            return line;
          } else {
            reader.unread(character);
            throw new IllegalArgumentException(
              "Invalid WKT geometry. Expecting end of coordinates ')' not "
                + FileUtil.getString(reader, 50));
          }
        }
        character = reader.read();
        if (character == ',' || character == ')') {
          if (character == ',') {
            skipWhitespace(reader);
          }
          if (axisIndex >= axisCount) {
            axisCount = axisIndex + 1;
            line.setAxisCount(axisCount);
          }
          line.setCoordinate(vertexIndex, axisIndex, number);
          axisIndex = 0;
          vertexIndex++;
          if (character == ')') {
            return line;
          }
        } else if (character == ' ' || Character.isWhitespace(character)) {
          skipWhitespace(reader);
          if (axisIndex == 0) {
            line.appendVertex(number, Double.NaN);
            axisIndex++;
          } else {
            if (axisIndex >= axisCount) {
              axisCount = axisIndex + 1;
              line.setAxisCount(axisCount);
            }
            line.setCoordinate(vertexIndex, axisIndex, number);
            axisIndex++;
          }
        } else {
          throw new IllegalArgumentException(
            "Invalid WKT geometry. Expecting a space between coordinates not: "
              + FileUtil.getString(reader, 50));
        }
      }
    } else {
      reader.unread(character);
      throw new IllegalArgumentException(
        "Invalid WKT geometry. Expecting start of coordinates '(' not: "
          + FileUtil.getString(reader, 50));
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Geometry> T parseGeometry(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final PushbackReader reader) {
    try {
      final int axisCount = geometryFactory.getAxisCount();
      final double[] scales = geometryFactory.newScales(axisCount);
      int character = (char)reader.read();
      while (character != -1 && Character.isWhitespace(character)) {
        character = reader.read();
      }
      if (character == 'S') {
        if (hasText(reader, "RID=")) {
          final Integer srid = parseInteger(reader);
          if (srid == null) {
            throw new IllegalArgumentException(
              "Invalid WKT geometry. Missing srid number after 'SRID=': "
                + FileUtil.getString(reader, 50));
          } else if (srid != this.geometryFactory.getHorizontalCoordinateSystemId()) {
            geometryFactory = GeometryFactory.floating(srid, axisCount);
          }
          if (!hasChar(reader, ';')) {
            throw new IllegalArgumentException("Invalid WKT geometry. Missing ; after 'SRID=" + srid
              + "': " + FileUtil.getString(reader, 50));
          }
        } else {
          throw new IllegalArgumentException(
            "Invalid WKT geometry: S" + FileUtil.getString(reader, 50));
        }
        character = reader.read();
        while (character != -1 && Character.isWhitespace(character)) {
          character = reader.read();
        }
      }
      Geometry geometry = null;
      switch (character) {
        case 'G':
          if (hasText(reader, "EOMETRYCOLLECTION")) {
            geometry = parseGeometryCollection(geometryFactory, useAxisCountFromGeometryFactory,
              reader);
          } else {
            throw new IllegalArgumentException(
              "Invalid WKT geometry type: G" + FileUtil.getString(reader, 50));
          }
        break;
        case 'L':
          if (hasText(reader, "INESTRING")) {
            geometry = parseLineString(geometryFactory, useAxisCountFromGeometryFactory, reader);
          } else if (hasText(reader, "INEARRING")) {
            geometry = parseLinearRing(geometryFactory, useAxisCountFromGeometryFactory, reader);
          } else {
            throw new IllegalArgumentException(
              "Invalid WKT geometry type: L" + FileUtil.getString(reader, 50));
          }
        break;
        case 'M':
          if (hasText(reader, "ULTI")) {
            if (hasText(reader, "POINT")) {
              geometry = parseMultiPoint(geometryFactory, useAxisCountFromGeometryFactory, reader);
            } else if (hasText(reader, "LINESTRING")) {
              geometry = parseMultiLineString(geometryFactory, useAxisCountFromGeometryFactory,
                reader);
            } else if (hasText(reader, "POLYGON")) {
              geometry = parseMultiPolygon(geometryFactory, useAxisCountFromGeometryFactory,
                reader);
            } else {
              throw new IllegalArgumentException(
                "Invalid WKT geometry type: MULTI" + FileUtil.getString(reader, 50));
            }
          } else {
            throw new IllegalArgumentException(
              "Invalid WKT geometry type: M" + FileUtil.getString(reader, 50));
          }
        break;
        case 'P':
          if (hasText(reader, "OINT")) {
            geometry = parsePoint(geometryFactory, useAxisCountFromGeometryFactory, reader);
          } else if (hasText(reader, "OLYGON")) {
            geometry = parsePolygon(geometryFactory, useAxisCountFromGeometryFactory, reader);
          } else {
            throw new IllegalArgumentException(
              "Invalid WKT geometry type: P" + FileUtil.getString(reader, 50));
          }
        break;

        default:
      }
      if (geometry == null) {
        throw new IllegalArgumentException(
          "Invalid WKT geometry type: " + FileUtil.getString(reader, 50));
      }
      if (this.geometryFactory.getHorizontalCoordinateSystemId() == 0) {
        final int srid = geometry.getHorizontalCoordinateSystemId();
        if (useAxisCountFromGeometryFactory) {
          geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);
          if (geometry.getGeometryFactory() == geometryFactory) {
            return (T)geometry;
          } else {
            return (T)geometryFactory.geometry(geometry);
          }
        } else {
          return (T)geometry;
        }
      } else if (geometryFactory == this.geometryFactory) {
        return (T)geometry;
      } else {
        return (T)this.geometryFactory.geometry(geometry);
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Error reading WKT:" + FileUtil.getString(reader, 50), e);
    }
  }

  public <T extends Geometry> T parseGeometry(final String value) {
    return parseGeometry(value, true);
  }

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T parseGeometry(final String value,
    final boolean useAxisCountFromGeometryFactory) {
    if (Property.hasValue(value)) {
      final PushbackReader reader = new PushbackReader(new StringReader(value), 20);
      final GeometryFactory geometryFactory = this.geometryFactory;
      return (T)parseGeometry(geometryFactory, useAxisCountFromGeometryFactory, reader);
    } else {
      return null;
    }
  }

  private Geometry parseGeometryCollection(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final PushbackReader reader) throws IOException {
    final int axisCount = getAxisCount(reader);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getHorizontalCoordinateSystemId();
        final double[] scales = geometryFactory.newScales(axisCount);
        geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);
      }
    }

    if (isEmpty(reader)) {
      return geometryFactory.geometryCollection();
    } else {
      final List<Geometry> geometries = new ArrayList<>();
      skipWhitespace(reader);
      int character = reader.read();
      switch (character) {
        case '(':
          do {
            final Geometry geometry = parseGeometry(geometryFactory,
              useAxisCountFromGeometryFactory, reader);
            if (!geometry.isEmpty()) {
              geometries.add(geometry);
            }
            skipWhitespace(reader);
            character = reader.read();
          } while (character == ',');
          if (character == ')') {
          } else {
            throw new IllegalArgumentException("Expecting ) not" + FileUtil.getString(reader, 50));
          }
        break;
        case ')':
          character = reader.read();
          if (character == ')' || character == ',') {
            skipWhitespace(reader);
          } else {
            throw new IllegalArgumentException(
              "Expecting ' or ) not" + FileUtil.getString(reader, 50));
          }
        break;

        default:
          throw new IllegalArgumentException("Expecting ( not" + FileUtil.getString(reader, 50));
      }
      return geometryFactory.geometry(geometries);
    }
  }

  private Geometry parseLinearRing(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final PushbackReader reader) throws IOException {
    final int axisCount = getAxisCount(reader);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getHorizontalCoordinateSystemId();
        final double[] scales = geometryFactory.newScales(axisCount);
        geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);
      }
    }
    if (isEmpty(reader)) {
      return geometryFactory.linearRing();
    } else {
      final LineString points = parseCoordinatesLineString(geometryFactory, reader, axisCount);
      if (points.getVertexCount() == 1) {
        return geometryFactory.point(points);
      } else {
        return points.newLinearRing();
      }
    }
  }

  private Geometry parseLineString(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final PushbackReader reader) throws IOException {
    final int axisCount = getAxisCount(reader);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getHorizontalCoordinateSystemId();
        final double[] scales = geometryFactory.newScales(axisCount);
        geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);
      }
    }
    if (isEmpty(reader)) {
      return geometryFactory.lineString();
    } else {
      final LineStringEditor points = parseCoordinatesLineString(geometryFactory, reader,
        axisCount);
      if (points.getVertexCount() == 1) {
        return geometryFactory.point(points);
      } else {
        return points.newLineString();
      }
    }
  }

  private Lineal parseMultiLineString(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final PushbackReader reader) throws IOException {
    final int axisCount = getAxisCount(reader);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getHorizontalCoordinateSystemId();
        final double[] scales = geometryFactory.newScales(axisCount);
        geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);
      }
    }
    if (isEmpty(reader)) {
      return geometryFactory.lineString();
    } else {
      final List<LineString> lines = parseParts(geometryFactory, reader, axisCount);
      return geometryFactory.lineal(lines);
    }
  }

  private Punctual parseMultiPoint(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final PushbackReader reader) throws IOException {
    final int axisCount = getAxisCount(reader);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getHorizontalCoordinateSystemId();
        final double[] scales = geometryFactory.newScales(axisCount);
        geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);
      }
    }

    if (isEmpty(reader)) {
      return geometryFactory.point();
    } else {
      final List<Point> pointsList = parsePointParts(geometryFactory, reader, axisCount);
      return geometryFactory.punctual(pointsList);
    }
  }

  private Polygonal parseMultiPolygon(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final PushbackReader reader) throws IOException {
    final int axisCount = getAxisCount(reader);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getHorizontalCoordinateSystemId();
        final double[] scales = geometryFactory.newScales(axisCount);
        geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);
      }
    }

    if (isEmpty(reader)) {
      return geometryFactory.polygon();
    } else {
      final List<List<LineString>> polygons = parsePartsList(geometryFactory, reader, axisCount);
      return geometryFactory.polygonal(polygons);
    }
  }

  private List<LineString> parseParts(final GeometryFactory geometryFactory,
    final PushbackReader reader, final int axisCount) throws IOException {
    skipWhitespace(reader);
    final List<LineString> parts = new ArrayList<>();
    int character = reader.read();
    switch (character) {
      case '(':
        do {
          final LineStringEditor lineBuilder = parseCoordinatesLineString(geometryFactory, reader,
            axisCount);
          parts.add(lineBuilder.newLineString());
          character = reader.read();
        } while (character == ',');
        if (character != ')') {
          throw new IllegalArgumentException("Expecting ) not" + FileUtil.getString(reader, 50));
        }
      break;
      case ')':
        character = reader.read();
        if (character == ')' || character == ',') {
        } else {
          throw new IllegalArgumentException(
            "Expecting ' or ) not" + FileUtil.getString(reader, 50));
        }
      break;

      default:
        throw new IllegalArgumentException("Expecting ( not" + FileUtil.getString(reader, 50));
    }
    return parts;
  }

  private List<List<LineString>> parsePartsList(final GeometryFactory geometryFactory,
    final PushbackReader reader, final int axisCount) throws IOException {
    final List<List<LineString>> partsList = new ArrayList<>();
    skipWhitespace(reader);
    int character = reader.read();
    switch (character) {
      case '(':
        do {
          final List<LineString> parts = parseParts(geometryFactory, reader, axisCount);
          partsList.add(parts);
          skipWhitespace(reader);
          character = reader.read();
        } while (character == ',');
        if (character == ')') {
        } else {
          reader.unread(character);
          throw new IllegalArgumentException("Expecting ) not " + FileUtil.getString(reader, 50));
        }
      break;
      case ')':
        skipWhitespace(reader);
        character = reader.read();
        if (character == ')' || character == ',') {
          skipWhitespace(reader);
        } else {
          reader.unread(character);
          throw new IllegalArgumentException(
            "Expecting ' or ) not " + FileUtil.getString(reader, 50));
        }
      break;

      default:
        reader.unread(character);
        throw new IllegalArgumentException("Expecting ( not " + FileUtil.getString(reader, 50));
    }
    return partsList;
  }

  private Point parsePoint(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final PushbackReader reader) throws IOException {
    final int axisCount = getAxisCount(reader);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getHorizontalCoordinateSystemId();
        final double[] scales = geometryFactory.newScales(axisCount);
        geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);
      }
    }
    if (isEmpty(reader)) {
      return geometryFactory.point();
    } else {
      final LineString points = parseCoordinatesLineString(geometryFactory, reader, axisCount);
      if (points.getVertexCount() > 1) {
        throw new IllegalArgumentException("Points may only have 1 vertex");
      }
      return geometryFactory.point(points);
    }
  }

  private List<Point> parsePointParts(final GeometryFactory geometryFactory,
    final PushbackReader reader, final int axisCount) throws IOException {
    skipWhitespace(reader);
    final List<Point> parts = new ArrayList<>();
    int character = reader.read();
    switch (character) {
      case '(':
        do {
          final LineStringEditor lineBuilder = parseCoordinatesLineString(geometryFactory, reader,
            axisCount);
          parts.add(geometryFactory.point(lineBuilder));
          skipWhitespace(reader);
          character = reader.read();
        } while (character == ',');
        if (character != ')') {
          throw new IllegalArgumentException("Expecting ) not" + FileUtil.getString(reader, 50));
        }
      break;
      case ')':
        character = reader.read();
        if (character == ')' || character == ',') {
        } else {
          throw new IllegalArgumentException(
            "Expecting ' or ) not" + FileUtil.getString(reader, 50));
        }
      break;

      default:
        throw new IllegalArgumentException("Expecting ( not" + FileUtil.getString(reader, 50));
    }
    return parts;
  }

  private Polygon parsePolygon(GeometryFactory geometryFactory,
    final boolean useAxisCountFromGeometryFactory, final PushbackReader reader) throws IOException {
    final int axisCount = getAxisCount(reader);
    if (!useAxisCountFromGeometryFactory) {
      if (axisCount != geometryFactory.getAxisCount()) {
        final int srid = geometryFactory.getHorizontalCoordinateSystemId();
        final double[] scales = geometryFactory.newScales(axisCount);
        geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);
      }
    }

    final List<LineString> parts;
    if (isEmpty(reader)) {
      parts = new ArrayList<>();
    } else {
      parts = parseParts(geometryFactory, reader, axisCount);
    }
    return geometryFactory.polygon(parts);
  }

}
