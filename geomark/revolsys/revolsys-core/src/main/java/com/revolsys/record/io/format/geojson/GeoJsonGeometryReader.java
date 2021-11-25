package com.revolsys.record.io.format.geojson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.record.io.format.json.JsonParser.EventType;
import com.revolsys.spring.resource.Resource;

public class GeoJsonGeometryReader extends AbstractIterator<Geometry> implements GeometryReader {

  private GeometryFactory geometryFactory;

  private JsonParser in;

  public GeoJsonGeometryReader(final Resource resource, final MapEx properties) {
    this.in = new JsonParser(resource);
    setProperties(properties);
  }

  @Override
  protected void closeDo() {
    FileUtil.closeSilent(this.in);
    this.geometryFactory = null;
    this.in = null;
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    init();
    return this.geometryFactory;
  }

  @Override
  protected Geometry getNext() throws NoSuchElementException {
    do {
      final JsonParser parser = this.in;
      final String fieldName = parser.skipToAttribute();
      if (GeoJson.TYPE.equals(fieldName)) {
        this.in.next();
        final String geometryType = this.in.getCurrentValue();
        if (GeoJson.GEOMETRY_TYPE_NAMES.contains(geometryType)) {
          return readGeometry();
        }
      } else if (GeoJson.CRS.equals(fieldName)) {
        this.in.next();
        this.geometryFactory = readCoordinateSystem();

      }
    } while (this.in.getEvent() != EventType.endDocument);
    throw new NoSuchElementException();
  }

  @Override
  public ClockDirection getPolygonRingDirection() {
    return ClockDirection.COUNTER_CLOCKWISE;
  }

  @Override
  protected void initDo() {
    this.geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (this.geometryFactory == null) {
      this.geometryFactory = GeometryFactory.floating3d(EpsgId.WGS84);
    }
    if (this.in.hasNext()) {
      this.in.next();
    }
  }

  private LineString readCoordinatesList(final boolean ring) {
    final List<Double> coordinates = new ArrayList<>();
    final int axisCount = readCoordinatesList(coordinates);
    return new LineStringDouble(axisCount, coordinates);
  }

  private int readCoordinatesList(final List<Double> coordinates) {
    int axisCount = 0;
    if (this.in.getEvent() == EventType.startArray
      || this.in.hasNext() && this.in.next() == EventType.startArray) {
      EventType event = this.in.next();
      if (event != EventType.endArray) {
        do {
          axisCount = Math.max(axisCount, readCoordinatesListCoordinates(coordinates));
          event = this.in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this.in.getEvent());
    }
    return axisCount;
  }

  /**
   * Read one points coordinates and add them to the list of coordinate values.
   *
   * @param values The list to add the points coordinates to.
   * @return The dimension of the coordinate read.
   */
  private int readCoordinatesListCoordinates(final List<Double> values) {
    int numAxis = 0;
    if (this.in.getEvent() == EventType.startArray
      || this.in.hasNext() && this.in.next() == EventType.startArray) {
      EventType event = this.in.getEvent();
      do {
        final JsonParser parser = this.in;
        final Object value = parser.getValue();
        if (value instanceof EventType) {
          event = (EventType)value;
        } else if (value instanceof Number) {
          values.add(((Number)value).doubleValue());
          numAxis++;
          event = this.in.next();
        } else {
          throw new IllegalArgumentException("Expecting number, not: " + value);
        }
      } while (event == EventType.comma);
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }

      return numAxis;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this.in.getEvent());
    }
  }

  private List<LineString> readCoordinatesListList(final boolean ring) {
    if (this.in.getEvent() == EventType.startArray
      || this.in.hasNext() && this.in.next() == EventType.startArray) {
      EventType event = this.in.next();
      final List<LineString> coordinatesLists = new ArrayList<>();
      if (event != EventType.endArray) {
        do {
          coordinatesLists.add(readCoordinatesList(ring));
          event = this.in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return coordinatesLists;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this.in.getEvent());
    }
  }

  private List<List<LineString>> readCoordinatesListListList() {
    if (this.in.getEvent() == EventType.startArray
      || this.in.hasNext() && this.in.next() == EventType.startArray) {
      EventType event = this.in.next();
      final List<List<LineString>> coordinatesLists = new ArrayList<>();
      if (event != EventType.endArray) {
        do {
          coordinatesLists.add(readCoordinatesListList(true));
          event = this.in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return coordinatesLists;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this.in.getEvent());
    }
  }

  private GeometryFactory readCoordinateSystem() {
    GeometryFactory factory = this.geometryFactory;
    do {
      final JsonParser parser = this.in;
      final String fieldName = parser.skipToNextAttribute();
      if (GeoJson.PROPERTIES.equals(fieldName)) {
        final JsonParser parser1 = this.in;
        final Map<String, Object> properties = parser1.getMap();
        final String name = (String)properties.get("name");
        if (name != null) {
          if (name.startsWith(GeoJson.URN_OGC_DEF_CRS_EPSG)) {
            final int srid = Integer
              .parseInt(name.substring(GeoJson.URN_OGC_DEF_CRS_EPSG.length()));
            factory = GeometryFactory.floating3d(srid);
          } else if (name.startsWith(GeoJson.EPSG)) {
            final int srid = Integer.parseInt(name.substring(GeoJson.EPSG.length()));
            factory = GeometryFactory.floating3d(srid);
          }
        }
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);
    return factory;
  }

  private Geometry readGeometry() {
    final String geometryType = this.in.getCurrentValue();
    if (geometryType.equals(GeoJson.POINT)) {
      return readPoint();
    } else if (geometryType.equals(GeoJson.LINE_STRING)) {
      return readLineString();
    } else if (geometryType.equals(GeoJson.POLYGON)) {
      return readPolygon();
    } else if (geometryType.equals(GeoJson.MULTI_POINT)) {
      return readMultiPoint();
    } else if (geometryType.equals(GeoJson.MULTI_LINE_STRING)) {
      return readMultiLineString();
    } else if (geometryType.equals(GeoJson.MULTI_POLYGON)) {
      return readMultiPolygon();
    } else if (geometryType.equals(GeoJson.GEOMETRY_COLLECTION)) {
      return readGeometryCollection();
    } else {
      return null;
    }
  }

  private Geometry readGeometryCollection() {
    List<Geometry> geometries = new ArrayList<>();
    GeometryFactory factory = this.geometryFactory;
    do {
      final JsonParser parser = this.in;
      final String fieldName = parser.skipToNextAttribute();
      if (GeoJson.GEOMETRIES.equals(fieldName)) {
        geometries = readGeometryList();
      } else if (GeoJson.CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);

    return factory.geometry(geometries);
  }

  private List<Geometry> readGeometryList() {
    if (this.in.getEvent() == EventType.startArray
      || this.in.hasNext() && this.in.next() == EventType.startArray) {
      EventType event = this.in.next();
      final List<Geometry> geometries = new ArrayList<>();
      if (event != EventType.endArray) {
        do {
          final Geometry geometry = getNext();
          geometries.add(geometry);
          event = this.in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return geometries;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this.in.getEvent());
    }
  }

  private LineString readLineString() {
    LineString points = null;
    GeometryFactory factory = this.geometryFactory;
    do {
      final JsonParser parser = this.in;
      final String fieldName = parser.skipToNextAttribute();
      if (GeoJson.COORDINATES.equals(fieldName)) {
        points = readCoordinatesList(false);
      } else if (GeoJson.CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);

    if (points == null) {
      return factory.lineString();
    } else {
      final int axisCount = points.getAxisCount();
      final GeometryFactory geometryFactory = factory.convertAxisCount(axisCount);
      return geometryFactory.lineString(points);
    }
  }

  private Geometry readMultiLineString() {
    List<LineString> lineStrings = null;
    GeometryFactory factory = this.geometryFactory;
    do {
      final JsonParser parser = this.in;
      final String fieldName = parser.skipToNextAttribute();
      if (GeoJson.COORDINATES.equals(fieldName)) {
        lineStrings = readCoordinatesListList(false);
      } else if (GeoJson.CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);
    int axisCount = 2;
    for (final LineString points : lineStrings) {
      axisCount = Math.max(axisCount, points.getAxisCount());
    }
    factory = factory.convertAxisCount(axisCount);
    return factory.lineal(lineStrings);
  }

  private Geometry readMultiPoint() {
    List<LineString> pointsList = null;
    GeometryFactory factory = this.geometryFactory;
    do {
      final JsonParser parser = this.in;
      final String fieldName = parser.skipToNextAttribute();
      if (GeoJson.COORDINATES.equals(fieldName)) {
        pointsList = readPointCoordinatesListList();
      } else if (GeoJson.CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);
    int axisCount = 2;
    for (final LineString points : pointsList) {
      axisCount = Math.max(axisCount, points.getAxisCount());
    }
    factory = factory.convertAxisCount(axisCount);
    return factory.punctual(pointsList);
  }

  private Geometry readMultiPolygon() {
    final List<Polygon> polygons = new ArrayList<>();
    List<List<LineString>> polygonRings = null;
    GeometryFactory factory = this.geometryFactory;
    do {
      final JsonParser parser = this.in;
      final String fieldName = parser.skipToNextAttribute();
      if (GeoJson.COORDINATES.equals(fieldName)) {
        polygonRings = readCoordinatesListListList();
      } else if (GeoJson.CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);
    int axisCount = 2;
    if (polygonRings != null) {
      for (final List<LineString> rings : polygonRings) {
        for (final LineString points : rings) {
          axisCount = Math.max(axisCount, points.getAxisCount());
        }
        factory = factory.convertAxisCount(axisCount);

        final Polygon polygon = factory.polygon(rings);
        polygons.add(polygon);
      }
    }
    return factory.polygonal(polygons);
  }

  private Point readPoint() {
    LineString coordinates = null;
    GeometryFactory factory = this.geometryFactory;
    do {
      final JsonParser parser = this.in;
      final String fieldName = parser.skipToNextAttribute();
      if (GeoJson.COORDINATES.equals(fieldName)) {
        coordinates = readPointCoordinatesList();
      } else if (GeoJson.CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);
    if (coordinates == null) {
      return factory.point();
    } else {
      final int axisCount = coordinates.getAxisCount();
      final GeometryFactory geometryFactory = factory.convertAxisCount(axisCount);
      return geometryFactory.point(coordinates);
    }
  }

  private LineString readPointCoordinatesList() {
    final JsonParser parser = this.in;
    final double[] values = parser.getDoubleArray();
    if (values == null) {
      return null;
    } else {
      return new LineStringDouble(values.length, values);
    }
  }

  private List<LineString> readPointCoordinatesListList() {
    if (this.in.getEvent() == EventType.startArray
      || this.in.hasNext() && this.in.next() == EventType.startArray) {
      EventType event = this.in.next();
      final List<LineString> coordinatesLists = new ArrayList<>();
      if (event != EventType.endArray) {
        do {
          coordinatesLists.add(readPointCoordinatesList());
          event = this.in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return coordinatesLists;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this.in.getEvent());
    }
  }

  private Polygon readPolygon() {
    List<LineString> rings = null;
    GeometryFactory factory = this.geometryFactory;
    do {
      final JsonParser parser = this.in;
      final String fieldName = parser.skipToNextAttribute();
      if (GeoJson.COORDINATES.equals(fieldName)) {
        rings = readCoordinatesListList(true);
      } else if (GeoJson.CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);
    int axisCount = 2;
    for (final LineString points : rings) {
      axisCount = Math.max(axisCount, points.getAxisCount());
    }
    factory = factory.convertAxisCount(axisCount);
    return factory.polygon(rings);
  }
}
