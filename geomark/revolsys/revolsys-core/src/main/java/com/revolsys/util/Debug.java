package com.revolsys.util;

import java.sql.Timestamp;

import org.jeometry.common.date.Dates;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;

public class Debug {
  public static boolean equals(final Geometry geometry, final double x, final double y) {
    final Point firstPoint = geometry.getPoint();
    if (firstPoint.equalsVertex(x, y)) {
      noOp();
      return true;
    } else {
      return false;
    }
  }

  public static boolean equals(final LineString line, final double x1, final double y1,
    final double x2, final double y2) {
    final LineString points = line;
    if (points.getPoint(0).equalsVertex(x1, y1)
      && points.getPoint(points.getVertexCount() - 1).equalsVertex(x2, y2)) {
      noOp();
      return true;
    } else {
      return false;
    }
  }

  public static void equals(final Object object1, final Object object2) {
    if (object1.equals(object2)) {
      noOp();
    }
  }

  public static boolean equals(final Point coordinates1End, final double... coordinates) {
    if (coordinates1End.equalsVertex(coordinates)) {
      noOp();
      return true;
    } else {
      return false;
    }
  }

  public static void equals(final Record object, final double x, final double y) {
    final Geometry geometry = object.getGeometry();
    equals(geometry, x, y);
  }

  public static void equals(final Record object, final Double x, final Double y) {
    final Geometry geometry = object.getGeometry();
    equals(geometry, x, y);
  }

  public static void idNull(final Record object) {
    if (object.getIdentifier() == null) {
      noOp();
    }
  }

  public static void infinite(final double value) {
    if (Double.isInfinite(value)) {
      noOp();
    }
  }

  public static void invalidGeometry(final Geometry geometry) {
    if (!geometry.isValid()) {
      noOp();
    }
  }

  public static void isNull(final Object value) {
    if (value == null) {
      noOp();
    }
  }

  public static void modified(final Record object) {
    if (object.getState() == RecordState.MODIFIED) {
      noOp();
    }
  }

  public static void nan(final double value) {
    if (Double.isNaN(value)) {
      noOp();
    }
  }

  public static void noOp() {
  }

  public static void println(final Object object) {
    System.out.println(object);
  }

  public static void println(final Object... values) {
    System.out.println(Strings.toString("\t", values));
  }

  public static void printTime() {
    println(new Timestamp(System.currentTimeMillis()));
  }

  public static void time(final String message, final Runnable runnable) {
    final long startTime = System.currentTimeMillis();
    try {
      runnable.run();
    } finally {
      Dates.printEllapsedTime(message, startTime);
    }
  }

  public static void typePath(final Edge<?> edge, final String typePath) {
    final String typePath2 = edge.getTypeName();
    equals(typePath2, typePath);
  }

  public static void typePath(final Record object, final String typePath) {
    final String typePath2 = object.getRecordDefinition().getPath();
    equals(typePath2, typePath);
  }

  public static void zeroLegthLine(final LineString line) {
    if (line.getLength() == 0) {
      noOp();
    }
  }
}
