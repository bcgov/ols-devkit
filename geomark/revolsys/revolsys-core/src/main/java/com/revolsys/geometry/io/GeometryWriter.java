package com.revolsys.geometry.io;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;
import com.revolsys.spring.resource.Resource;

public interface GeometryWriter extends Writer<Geometry> {
  static boolean isWritable(final Object source) {
    return IoFactory.isAvailable(GeometryWriterFactory.class, source);
  }

  static <GW extends GeometryWriter> GW newGeometryWriter(final Object target) {
    return newGeometryWriter(target, MapEx.EMPTY);
  }

  static <GW extends GeometryWriter> GW newGeometryWriter(final Object target,
    final GeometryFactory geometryFactory) {
    final MapEx properties = new LinkedHashMapEx("geometryFactory", geometryFactory);
    return newGeometryWriter(target, properties);
  }

  @SuppressWarnings("unchecked")
  static <GW extends GeometryWriter> GW newGeometryWriter(final Object target,
    final MapEx properties) {
    final Resource resource = Resource.getResource(target);
    final GeometryWriterFactory writerFactory = IoFactory.factory(GeometryWriterFactory.class,
      resource);
    return (GW)writerFactory.newGeometryWriter(resource, properties);
  }

  static void writeAll(final Object target, final GeometryFactory geometryFactory,
    final Iterable<? extends Geometry> geometries) {
    try (
      GeometryWriter writer = newGeometryWriter(target, geometryFactory)) {
      if (writer == null) {
        throw new IllegalArgumentException("Cannot create writer for: " + target);
      } else {
        writer.writeAll(geometries);
      }
    }
  }

  static void writeAll(final Object target, final MapEx properties,
    final Iterable<? extends Geometry> geometries) {
    try (
      GeometryWriter writer = newGeometryWriter(target, properties)) {
      if (writer == null) {
        throw new IllegalArgumentException("Cannot create writer for: " + target);
      } else {
        writer.writeAll(geometries);
      }
    }
  }

  void setGeometryFactory(GeometryFactory geometryFactory);

  default void writeAll(final Iterable<? extends Geometry> geometries) {
    if (geometries != null) {
      for (final Geometry geometry : geometries) {
        if (geometry != null) {
          write(geometry);
        }
      }
    }
  }
}
