package com.revolsys.record.io.format.kml;

import java.io.IOException;
import java.io.Writer;

import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;

public class KmlWriterUtil {

  public static void writeCoordinates(final Writer out, final LineString line) throws IOException {
    out.write("<coordinates>");
    final int vertexCount = line.getVertexCount();
    final int axisCount = line.getAxisCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      if (vertexIndex > 0) {
        out.write(' ');
      }
      out.write(Doubles.toString(line.getX(vertexIndex)));
      out.write(',');
      out.write(Doubles.toString(line.getY(vertexIndex)));
      for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
        out.write(',');
        out.write(String.valueOf(line.getCoordinate(vertexIndex, axisIndex)));
      }
    }
    out.write("</coordinates>\n");
  }

  public static void writeCoordinates(final Writer out, final Point point) throws IOException {
    out.write("<coordinates>");
    if (point != null && !point.isEmpty()) {
      out.write(Doubles.toString(point.getX()));
      out.write(',');
      out.write(Doubles.toString(point.getY()));
      for (int axisIndex = 2; axisIndex < point.getAxisCount(); axisIndex++) {
        final double value = point.getCoordinate(axisIndex);
        out.write(',');
        out.write(Doubles.toString(value));
      }
    }
    out.write("</coordinates>\n");
  }

  // Assumes geometry is wgs84
  public static void writeGeometry(final Writer out, final Geometry geometry, final int axisCount)
    throws IOException {
    if (geometry != null) {
      final int numGeometries = geometry.getGeometryCount();
      if (numGeometries > 1) {
        out.write("<MultiGeometry>\n");
        for (int i = 0; i < numGeometries; i++) {
          writeGeometry(out, geometry.getGeometry(i), axisCount);
        }
        out.write("</MultiGeometry>\n");
      } else {
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          writePoint(out, point);
        } else if (geometry instanceof LinearRing) {
          final LinearRing line = (LinearRing)geometry;
          writeLinearRing(out, line);
        } else if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          writeLineString(out, line);
        } else if (geometry instanceof Polygon) {
          final Polygon polygon = (Polygon)geometry;
          writePolygon(out, polygon);
        } else if (geometry.isGeometryCollection()) {
          writeMultiGeometry(out, geometry, axisCount);
        }
      }
    }
  }

  public static void writeLinearRing(final Writer out, final LineString ring) throws IOException {
    out.write("<LinearRing>\n");
    writeCoordinates(out, ring);
    out.write("</LinearRing>\n");
  }

  public static void writeLineString(final Writer out, final LineString line) throws IOException {
    out.write("<LineString>\n");
    writeCoordinates(out, line);
    out.write("</LineString>\n");
  }

  public static void writeMultiGeometry(final Writer out, final Geometry collection,
    final int axisCount) throws IOException {
    out.write("<MultiGeometry>\n");
    for (int i = 0; i < collection.getGeometryCount(); i++) {
      final Geometry geometry = collection.getGeometry(i);
      writeGeometry(out, geometry, axisCount);
    }
    out.write("</MultiGeometry>\n");

  }

  public static void writePoint(final Writer out, final Point point) throws IOException {
    out.write("<Point>\n");
    writeCoordinates(out, point);
    out.write("</Point>\n");
  }

  public static void writePolygon(final Writer out, final Polygon polygon) throws IOException {
    out.write("<Polygon>\n");
    if (!polygon.isEmpty()) {
      out.write("<outerBoundaryIs>\n");
      writeLinearRing(out, polygon.getShell());
      out.write("</outerBoundaryIs>\n");
      for (final LineString hole : polygon.holes()) {
        out.write("<innerBoundaryIs>\n");
        writeLinearRing(out, hole);
        out.write("</innerBoundaryIs>\n");
      }
    }
    out.write("</Polygon>\n");
  }
}
