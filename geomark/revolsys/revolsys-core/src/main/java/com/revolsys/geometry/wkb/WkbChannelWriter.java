package com.revolsys.geometry.wkb;

import java.io.ByteArrayOutputStream;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.common.function.Consumer4Double;

import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.channels.ChannelWriter;

public class WkbChannelWriter extends ChannelWriter {

  private final BiConsumerDouble writeVertex = (x, y) -> {
    putDouble(x);
    putDouble(y);
  };

  private final Consumer3Double writeVertexZ = (x, y, z) -> {
    putDouble(x);
    putDouble(y);
    putDouble(z);
  };

  private final Consumer4Double writeVertexZM = (x, y, z, m) -> {
    putDouble(x);
    putDouble(y);
    putDouble(z);
    putDouble(m);
  };

  public WkbChannelWriter(final ByteArrayOutputStream out) {
    super(out);
  }

  public void writeGeometry(final Geometry geometry) {
    putByte((byte)0);
    if (geometry instanceof Point) {
      writePoint((Point)geometry);
    } else if (geometry instanceof LineString) {
      writeLineString((LineString)geometry);
    } else if (geometry instanceof Polygon) {
      writePolygon((Polygon)geometry);
    } else if (geometry instanceof MultiPoint) {
      writeMultiPoint((MultiPoint)geometry);
    } else if (geometry instanceof MultiLineString) {
      writeMultiLineString((MultiLineString)geometry);
    } else if (geometry instanceof MultiPolygon) {
      writeMultiPolygon((MultiPolygon)geometry);
    } else if (geometry instanceof GeometryCollection) {
      writeGeometryCollection((GeometryCollection)geometry);
    } else {
      throw new IllegalArgumentException("Geometry type not supported: " + geometry);
    }

  }

  public void writeGeometryCollection(final GeometryCollection collection) {
    writeGeometryType(collection, 7);
    final int geometryCount = collection.getGeometryCount();
    putInt(geometryCount);
    for (int geometryIndex = 0; geometryIndex < geometryCount; geometryIndex++) {
      final Geometry geometry = collection.getGeometry(geometryIndex);
      writeGeometry(geometry);
    }
  }

  public int writeGeometryType(final Geometry geometry, int geometryType) {
    final int axisCount = geometry.getAxisCount();
    if (geometry.isEmpty()) {
    } else if (axisCount == 3) {
      geometryType += 1000;
    } else if (axisCount == 4) {
      geometryType += 3000;
    }
    putInt(geometryType);
    return axisCount;
  }

  public void writeLinearRing(final LinearRing ring, final int axisCount,
    final ClockDirection ringDirection) {
    if (ring.getClockDirection() == ringDirection) {
      writeLineCoordinates(ring, axisCount);
    } else {
      writeLineCoordinatesReverse(ring, axisCount);
    }
  }

  public void writeLineCoordinates(final LineString line, final int axisCount) {
    final int vertexCount = line.getVertexCount();
    putInt(vertexCount);
    if (axisCount == 4) {
      line.forEachVertex(this.writeVertexZM);
    } else if (axisCount == 3) {
      line.forEachVertex(this.writeVertexZ);
    } else {
      line.forEachVertex(this.writeVertex);
    }
  }

  public void writeLineCoordinatesReverse(final LineString line, final int axisCount) {
    final int vertexCount = line.getVertexCount();
    putInt(vertexCount);
    if (axisCount == 4) {
      line.forEachVertexReverse(this.writeVertexZM);
    } else if (axisCount == 3) {
      line.forEachVertexReverse(this.writeVertexZ);
    } else {
      line.forEachVertexReverse(this.writeVertex);
    }
  }

  public void writeLineString(final LineString line) {
    final int axisCount = writeGeometryType(line, 2);
    writeLineCoordinates(line, axisCount);
  }

  public void writeMultiLineString(final MultiLineString multiLineString) {
    writeGeometryType(multiLineString, 5);
    final int lineCount = multiLineString.getGeometryCount();
    putInt(lineCount);
    for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
      putByte((byte)0);
      final LineString line = multiLineString.getLineString(lineIndex);
      writeLineString(line);
    }
  }

  public void writeMultiPoint(final MultiPoint multiPoint) {
    writeGeometryType(multiPoint, 4);
    final int pointCount = multiPoint.getGeometryCount();
    putInt(pointCount);
    for (int pointIndex = 0; pointIndex < pointCount; pointIndex++) {
      putByte((byte)0);
      final Point point = multiPoint.getPoint(pointIndex);
      writePoint(point);
    }
  }

  public void writeMultiPolygon(final MultiPolygon multiPolygon) {
    writeGeometryType(multiPolygon, 6);
    final int polygonCount = multiPolygon.getGeometryCount();
    putInt(polygonCount);
    for (int polygonIndex = 0; polygonIndex < polygonCount; polygonIndex++) {
      putByte((byte)0);
      final Polygon polygon = multiPolygon.getPolygon(polygonIndex);
      writePolygon(polygon);
    }
  }

  public void writePoint(final Point point) {
    final int axisCount = writeGeometryType(point, 1);
    if (point.isEmpty()) {
      putLong(0x7ff8000000000000L);
      putLong(0x7ff8000000000000L);
    } else {
      if (axisCount == 4) {
        point.forEachVertex(this.writeVertexZM);
      } else if (axisCount == 3) {
        point.forEachVertex(this.writeVertexZ);
      } else {
        point.forEachVertex(this.writeVertex);
      }
    }
  }

  public void writePolygon(final Polygon polygon) {
    final int axisCount = writeGeometryType(polygon, 3);
    if (polygon.isEmpty()) {
      putInt(0);
    } else {
      final int ringCount = polygon.getRingCount();
      putInt(ringCount);
      ClockDirection ringDirection = ClockDirection.COUNTER_CLOCKWISE;
      for (int ringIndex = 0; ringIndex < ringCount; ringIndex++) {
        final LinearRing ring = polygon.getRing(ringIndex);
        writeLinearRing(ring, axisCount, ringDirection);
        ringDirection = ClockDirection.CLOCKWISE;
      }
    }
  }
}
