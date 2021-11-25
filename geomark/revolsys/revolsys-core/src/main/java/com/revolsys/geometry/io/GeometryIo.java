package com.revolsys.geometry.io;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.Writer;

public class GeometryIo {
  public static void copyGeometry(final Iterable<Geometry> reader, final Object target) {
    if (reader != null) {
      try (
        Writer<Geometry> writer = GeometryWriter.newGeometryWriter(target)) {
        if (writer == null) {
          throw new IllegalArgumentException("Unable to create writer " + target);
        } else {
          copyGeometry(reader, writer);
        }
      }
    }
  }

  public static void copyGeometry(final Iterable<Geometry> reader, final Writer<Geometry> writer) {
    if (reader != null && writer != null) {
      for (final Geometry geometry : reader) {
        writer.write(geometry);
      }
    }
  }

  public static void copyGeometry(final Object source, final Object target) {
    try (
      GeometryReader reader = GeometryReader.newGeometryReader(source)) {
      if (reader == null) {
        throw new IllegalArgumentException("Unable to read " + source);
      } else {
        copyGeometry(reader, target);
      }
    }

  }

  public static void copyGeometry(final Object source, final Writer<Geometry> writer) {
    try (
      GeometryReader reader = GeometryReader.newGeometryReader(source)) {
      if (reader == null) {
        throw new IllegalArgumentException("Unable to read " + source);
      } else {
        copyGeometry(reader, writer);
      }
    }

  }

}
