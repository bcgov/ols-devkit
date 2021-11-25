package com.revolsys.record.io.format.kml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.record.io.format.xml.StaxReader;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public class KmlGeometryReader extends AbstractIterator<Geometry>
  implements GeometryReader, Kml22Constants {
  private GeometryFactory geometryFactory = GeometryFactory.floating3d(COORDINATE_SYSTEM_ID);

  private StaxReader reader;

  private InputStream in;

  public KmlGeometryReader(final InputStream in) {
    this.in = in;
    this.reader = StaxReader.newXmlReader(in);
  }

  public KmlGeometryReader(final Resource resource, final MapEx properties) {
    this(resource.newBufferedInputStream());
    setProperties(properties);
  }

  @Override
  protected void closeDo() {
    if (this.reader != null) {
      this.reader.close();
    }
    if (this.in != null) {
      try {
        this.in.close();
      } catch (final IOException e) {
      }
    }
    this.geometryFactory = null;
    this.reader = null;
    this.in = null;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  protected Geometry getNext() {
    try {
      final int depth = 0;
      if (this.reader.skipToStartElements(depth, MULTI_GEOMETRY, POINT, LINE_STRING, POLYGON)) {
        final Geometry geometry = parseGeometry();
        if (geometry == null) {
          throw new NoSuchElementException();
        } else {
          return geometry;
        }
      } else {
        throw new NoSuchElementException();
      }
    } catch (final XMLStreamException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  protected void initDo() {
    this.reader.skipToStartElement();
  }

  private LineString parseCoordinates() throws XMLStreamException {
    this.reader.requireLocalName(COORDINATES);
    final String coordinatesListString = this.reader.getElementText();
    if (Property.hasValue(coordinatesListString)) {
      int axisCount = 2;
      final String[] coordinatesListArray = coordinatesListString.trim().split("\\s+");
      final List<Point> points = new ArrayList<>();
      for (final String coordinatesString : coordinatesListArray) {
        final String[] coordinatesArray = coordinatesString.split(",");
        final double[] coordinates = new double[coordinatesArray.length];
        for (int axisIndex = 0; axisIndex < coordinatesArray.length; axisIndex++) {
          final String coordinate = coordinatesArray[axisIndex];
          coordinates[axisIndex] = Double.valueOf(coordinate);
        }
        axisCount = Math.max(axisCount, coordinates.length);
        points.add(new PointDouble(coordinates));
      }
      this.reader.skipToEndElement();
      return new LineStringDouble(axisCount, points);
    } else {
      return null;
    }
  }

  private Geometry parseGeometry() throws XMLStreamException {
    if (this.reader.isStartElementLocalName(MULTI_GEOMETRY)) {
      return parseMultiGeometry();
    } else if (this.reader.isStartElementLocalName(POINT)) {
      return parsePoint();
    } else if (this.reader.isStartElementLocalName(LINE_STRING)) {
      return parseLineString();
    } else if (this.reader.isStartElementLocalName(POLYGON)) {
      return parsePolygon();
    } else {
      return null;
    }
  }

  private LinearRing parseLinearRing() throws XMLStreamException {
    this.reader.requireLocalName(LINEAR_RING);
    LineString points = null;
    final int depth = this.reader.getDepth();
    while (this.reader.skipToStartElements(depth, COORDINATES)) {
      if (points == null && this.reader.isStartElementLocalName(COORDINATES)) {
        points = parseCoordinates();
        this.reader.skipToEndElement();
      }
    }
    if (points == null) {
      return this.geometryFactory.linearRing();
    } else {
      final int axisCount = points.getAxisCount();
      final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
      return geometryFactory.linearRing(points);
    }
  }

  private LineString parseLineString() throws XMLStreamException {
    this.reader.requireLocalName(LINE_STRING);
    LineString points = null;
    final int depth = this.reader.getDepth();
    while (this.reader.skipToStartElements(depth, COORDINATES)) {
      if (points == null && this.reader.isStartElementLocalName(COORDINATES)) {
        points = parseCoordinates();
      }
    }
    if (points == null) {
      return this.geometryFactory.lineString();
    } else {
      final int axisCount = points.getAxisCount();
      final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
      return geometryFactory.lineString(points);
    }
  }

  private Geometry parseMultiGeometry() throws XMLStreamException {
    int axisCount = 2;
    final List<Geometry> geometries = new ArrayList<>();
    while (this.reader.skipToChildStartElements(POINT, LINE_STRING, POLYGON)) {
      final Geometry geometry = parseGeometry();
      if (geometry != null) {
        axisCount = Math.max(axisCount, geometry.getAxisCount());
        geometries.add(geometry);
      }
    }
    final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
    final Geometry geometryCollection = geometryFactory.geometry(geometries);

    return geometryCollection;
  }

  private Point parsePoint() throws XMLStreamException {
    this.reader.requireLocalName(POINT);
    LineString points = null;
    final int depth = this.reader.getDepth();
    while (this.reader.skipToStartElements(depth, COORDINATES)) {
      if (points == null && this.reader.isStartElementLocalName(COORDINATES)) {
        points = parseCoordinates();
      }
    }
    if (points == null) {
      return this.geometryFactory.point();
    } else {
      final int axisCount = points.getAxisCount();
      final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
      return geometryFactory.point(points);
    }
  }

  private Polygon parsePolygon() throws XMLStreamException {
    this.reader.requireLocalName(POLYGON);
    final List<LinearRing> rings = new ArrayList<>();
    int axisCount = 2;
    final int depth = this.reader.getDepth();
    while (this.reader.skipToStartElements(depth, OUTER_BOUNDARY_IS, INNER_BOUNDARY_IS)) {
      final LinearRing ring = parseRing();
      if (ring != null) {
        axisCount = Math.max(axisCount, ring.getAxisCount());
        rings.add(ring);
      }
    }
    final GeometryFactory geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
    final Polygon polygon = geometryFactory.polygon(rings);
    return polygon;
  }

  private LinearRing parseRing() throws XMLStreamException {
    final int depth = this.reader.getDepth();
    while (this.reader.skipToStartElements(depth, LINEAR_RING)) {
      final LinearRing ring = parseLinearRing();
      return ring;
    }
    return null;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return this.reader.toString();
  }

}
