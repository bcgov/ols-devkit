package com.revolsys.record.io.format.gml;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.jeometry.common.number.Doubles;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.io.IoConstants;
import com.revolsys.record.io.format.xml.StaxReader;
import com.revolsys.spring.resource.Resource;

public class GmlGeometryReader extends AbstractIterator<Geometry> implements GeometryReader {
  public static final LineString parse(final String value, final String separator,
    final int axisCount) {
    final String[] values = value.split(separator);
    final double[] coordinates = new double[values.length];
    for (int i = 0; i < values.length; i++) {
      final String string = values[i];
      coordinates[i] = Double.parseDouble(string);
    }
    return new LineStringDouble(axisCount, coordinates);
  }

  public static LineString parse(final String value, final String decimal, String coordSeperator,
    String toupleSeperator) {

    toupleSeperator = toupleSeperator.replaceAll("\\\\", "\\\\\\\\");
    toupleSeperator = toupleSeperator.replaceAll("\\.", "\\\\.");
    final Pattern touplePattern = Pattern.compile("\\s*" + toupleSeperator + "\\s*");
    final String[] touples = touplePattern.split(value);

    coordSeperator = coordSeperator.replaceAll("\\\\", "\\\\\\\\");
    coordSeperator = coordSeperator.replaceAll("\\.", "\\\\.");
    final Pattern coordinatePattern = Pattern.compile("\\s*" + coordSeperator + "\\s*");

    int axisCount = 0;
    final List<double[]> listOfCoordinateArrays = new ArrayList<>();
    if (touples.length == 0) {
      return null;
    } else {
      for (final String touple : touples) {
        final String[] values = coordinatePattern.split(touple);
        if (values.length > 0) {
          final double[] coordinates = Doubles.toDoubleArray(values);
          axisCount = Math.max(axisCount, coordinates.length);
          listOfCoordinateArrays.add(coordinates);
        }
      }
    }

    return toCoordinateList(axisCount, listOfCoordinateArrays);
  }

  public static LineString toCoordinateList(final int axisCount,
    final List<double[]> listOfCoordinateArrays) {
    final int vertexCount = listOfCoordinateArrays.size();
    final double[] coordinates = new double[vertexCount * axisCount];
    for (int i = 0; i < vertexCount; i++) {
      final double[] coordinates2 = listOfCoordinateArrays.get(i);
      for (int j = 0; j < axisCount; j++) {
        final double value;
        if (j < coordinates2.length) {
          value = coordinates2[j];
        } else {
          value = Double.NaN;
        }
        coordinates[i * axisCount + j] = value;
      }
    }
    return new LineStringDouble(axisCount, coordinates);
  }

  private GeometryFactory geometryFactory;

  private StaxReader in;

  private Reader reader;

  public GmlGeometryReader(final Resource resource, final MapEx properties) {
    try {
      this.reader = resource.newReader();
      this.in = StaxReader.newXmlReader(this.reader);
    } catch (final Exception e) {
      throw new IllegalArgumentException("Unable to open resource " + resource);
    }
    setProperties(properties);
  }

  @Override
  protected void closeDo() {
    if (this.in != null) {
      this.in.close();
    }
    if (this.reader != null) {
      try {
        this.reader.close();
      } catch (final IOException e) {
      }
    }
    this.geometryFactory = null;
    this.in = null;
    this.reader = null;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  private GeometryFactory getGeometryFactory(final GeometryFactory geometryFactory) {
    final String srsName = this.in.getAttributeValue(Gml.SRS_NAME.getNamespaceURI(),
      Gml.SRS_NAME.getLocalPart());
    if (srsName == null) {
      return geometryFactory;
    } else {
      if (srsName.toLowerCase().startsWith("epsg:6.6:")) {
        final int srid = Integer.parseInt(srsName.substring("urn:ogc:def:crs:EPSG:6.6:".length()));
        final GeometryFactory factory = GeometryFactory.floating3d(srid);
        return factory;
      } else if (srsName.toLowerCase().startsWith("epsg:")) {
        final int srid = Integer.parseInt(srsName.substring("EPSG:".length()));
        final GeometryFactory factory = GeometryFactory.floating3d(srid);
        return factory;
      } else {
        return geometryFactory;
      }
    }
  }

  @Override
  protected Geometry getNext() {
    try {
      final int depth = 0;
      while (this.in.skipToStartElements(depth, Gml.ENVELOPE_AND_GEOMETRY_TYPE_NAMES)) {
        final QName name = this.in.getName();
        if (name.equals(Gml.ENVELOPE)) {
          this.geometryFactory = getGeometryFactory(this.geometryFactory);
        } else {
          return readGeometry(this.geometryFactory);
        }
      }
      throw new NoSuchElementException();
    } catch (final XMLStreamException e) {
      throw new RuntimeException("Error reading next geometry", e);
    }

  }

  @Override
  public ClockDirection getPolygonRingDirection() {
    return ClockDirection.COUNTER_CLOCKWISE;
  }

  @Override
  protected void initDo() {
    this.geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (this.geometryFactory == null) {
      this.geometryFactory = GeometryFactory.DEFAULT_3D;
    }
  }

  private LineString readCoordinates() throws XMLStreamException {
    String decimal = this.in.getAttributeValue(null, "decimal");
    if (decimal == null) {
      decimal = ".";
    }
    String coordSeperator = this.in.getAttributeValue(null, "coordSeperator");
    if (coordSeperator == null) {
      coordSeperator = ",";
    }
    String toupleSeperator = this.in.getAttributeValue(null, "toupleSeperator");
    if (toupleSeperator == null) {
      toupleSeperator = " ";
    }
    final String value = this.in.getElementText();

    final LineString points = GmlGeometryReader.parse(value, decimal, coordSeperator,
      toupleSeperator);
    this.in.skipToEndElement();
    return points;
  }

  private Geometry readGeometry(final GeometryFactory geometryFactory) throws XMLStreamException {
    final QName typeName = this.in.getName();
    if (typeName.equals(Gml.POINT)) {
      return readPoint(geometryFactory);
    } else if (typeName.equals(Gml.LINE_STRING)) {
      return readLineString(geometryFactory);
    } else if (typeName.equals(Gml.POLYGON)) {
      return readPolygon(geometryFactory);
    } else if (typeName.equals(Gml.MULTI_POINT)) {
      return readMultiPoint(geometryFactory);
    } else if (typeName.equals(Gml.MULTI_LINE_STRING)) {
      return readMultiLineString(geometryFactory);
    } else if (typeName.equals(Gml.MULTI_POLYGON)) {
      return readMultiPolygon(geometryFactory);
    } else if (typeName.equals(Gml.MULTI_GEOMETRY)) {
      return readMultiGeometry(geometryFactory);
    } else {
      throw new IllegalStateException("Unexpected geometry type " + typeName);
    }
  }

  private LinearRing readLinearRing(final GeometryFactory geometryFactory)
    throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    LineString points = null;
    final int depth = this.in.getDepth();
    while (this.in.skipToStartElements(depth, Gml.POS_LIST, Gml.COORDINATES)) {
      final QName elementName = this.in.getName();
      if (elementName.equals(Gml.POS_LIST)) {
        points = readPosList();
      } else if (elementName.equals(Gml.COORDINATES)) {
        points = readCoordinates();
      }
    }
    if (points == null) {
      return factory.linearRing();
    } else {
      final int axisCount = points.getAxisCount();
      return factory.convertAxisCount(axisCount).linearRing(points);
    }
  }

  private LineString readLineString(final GeometryFactory geometryFactory)
    throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    LineString points = null;
    final int depth = this.in.getDepth();
    while (this.in.skipToStartElements(depth, Gml.POS_LIST, Gml.COORDINATES)) {
      if (this.in.getName().equals(Gml.POS)) {
        points = readPosList();
      } else if (this.in.getName().equals(Gml.COORDINATES)) {
        points = readCoordinates();
      }
    }
    if (points == null) {
      return factory.lineString();
    } else {
      final int axisCount = points.getAxisCount();
      return factory.convertAxisCount(axisCount).lineString(points);
    }
  }

  private Geometry readMultiGeometry(final GeometryFactory geometryFactory)
    throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<Geometry> geometries = new ArrayList<>();
    this.in.skipSubTree();
    return factory.geometry(geometries);
  }

  private Lineal readMultiLineString(final GeometryFactory geometryFactory)
    throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    int axisCount = 2;
    final List<LineString> lines = new ArrayList<>();
    final int depth = this.in.getDepth();
    while (this.in.skipToStartElements(depth, Gml.LINE_STRING)) {
      final LineString line = readLineString(factory);
      if (line != null) {
        axisCount = Math.max(axisCount, line.getAxisCount());
        lines.add(line);
      }
    }
    return factory.convertAxisCount(axisCount).lineal(lines);
  }

  private Punctual readMultiPoint(final GeometryFactory geometryFactory) throws XMLStreamException {
    int axisCount = 2;
    final List<Point> points = new ArrayList<>();
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    final int depth = this.in.getDepth();
    while (this.in.skipToStartElements(depth, Gml.POINT)) {
      final Point point = readPoint(factory);
      if (point != null) {
        axisCount = Math.max(axisCount, point.getAxisCount());
        points.add(point);
      }
    }
    return factory.convertAxisCount(axisCount).punctual(points);
  }

  private Polygonal readMultiPolygon(final GeometryFactory geometryFactory)
    throws XMLStreamException {
    int axisCount = 2;
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<Polygon> polygons = new ArrayList<>();
    final int depth = this.in.getDepth();
    while (this.in.skipToStartElements(depth, Gml.POLYGON)) {
      final Polygon polygon = readPolygon(factory);
      if (polygon != null) {
        axisCount = Math.max(axisCount, polygon.getAxisCount());
        polygons.add(polygon);
      }
    }
    return factory.convertAxisCount(axisCount).polygonal(polygons);
  }

  private Point readPoint(final GeometryFactory geometryFactory) throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    LineString points = null;
    final int depth = this.in.getDepth();
    while (this.in.skipToStartElements(depth, Gml.POS, Gml.COORDINATES)) {
      if (this.in.getName().equals(Gml.POS)) {
        points = readPosList();
      } else if (this.in.getName().equals(Gml.COORDINATES)) {
        points = readCoordinates();
      }
    }
    if (points == null) {
      return factory.point();
    } else {
      final int axisCount = points.getAxisCount();
      return factory.convertAxisCount(axisCount).point(points);
    }
  }

  private Polygon readPolygon(final GeometryFactory geometryFactory) throws XMLStreamException {
    int axisCount = 2;
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<LinearRing> rings = new ArrayList<>();
    final int depth = this.in.getDepth();
    while (this.in.skipToStartElements(depth, Gml.OUTER_BOUNDARY_IS, Gml.INNER_BOUNDARY_IS)) {
      final LinearRing ring = readLinearRing(factory);
      if (ring != null) {
        axisCount = Math.max(axisCount, ring.getAxisCount());
        rings.add(ring);
      }
    }
    final Polygon polygon = factory.convertAxisCount(axisCount).polygon(rings);
    return polygon;
  }

  private LineString readPosList() throws XMLStreamException {
    final String dimension = this.in.getAttributeValue(null, "dimension");
    if (dimension == null) {
      this.in.skipSubTree();
      return null;
    } else {
      final int axisCount = Integer.parseInt(dimension);
      final String value = this.in.getElementText();
      final LineString points = GmlGeometryReader.parse(value, "\\s+", axisCount);
      this.in.skipToEndElement();
      return points;
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }
}
