package com.revolsys.record.io.format.geojson;

import java.io.BufferedWriter;
import java.io.Writer;

import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.json.JsonWriter;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class GeoJsonRecordWriter extends AbstractRecordWriter {

  private static final GeometryFactory WGS84 = GeometryFactory.wgs84();

  boolean initialized = false;

  /** The writer */
  private JsonWriter out;

  private boolean singleObject;

  private int srid = -1;

  private boolean allowCustomCoordinateSystem = false;

  private GeometryFactory geometryFactory;

  public GeoJsonRecordWriter(final Writer out) {
    this(out, null);
  }

  public GeoJsonRecordWriter(final Writer out, final RecordDefinitionProxy recordDefinition) {
    super(recordDefinition);
    if (recordDefinition != null) {
      this.geometryFactory = recordDefinition.getGeometryFactory();
    }
    this.out = new JsonWriter(new BufferedWriter(out));
    this.out.setIndent(true);
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    if (this.out != null) {
      try {
        if (!this.initialized) {
          writeHeader();
        }
        writeFooter();
      } finally {
        this.out.close();
        this.out = null;
      }
    }
  }

  private void coordinatePoint(final Point coordinates) {
    this.out.print('[');
    for (int axisIndex = 0; axisIndex < coordinates.getAxisCount(); axisIndex++) {
      if (axisIndex > 0) {
        this.out.print(',');
      }
      final double value = coordinates.getCoordinate(axisIndex);
      this.out.value(value);
    }
    this.out.print(']');
  }

  private void coordinatesLineString(final LineString line) {
    this.out.startList(false);
    this.out.indent();
    for (int i = 0; i < line.getVertexCount(); i++) {
      if (i > 0) {
        this.out.endAttribute();
        this.out.indent();
      }
      final double x = line.getX(i);
      final double y = line.getY(i);

      this.out.print('[');
      this.out.value(x);
      this.out.print(',');
      this.out.value(y);

      for (int axisIndex = 2; axisIndex < line.getAxisCount(); axisIndex++) {
        this.out.print(',');
        final double value = line.getCoordinate(i, axisIndex);
        this.out.value(value);
      }
      this.out.print(']');
    }
    this.out.endList();
  }

  private void coordinatesPoint(final Point point) {
    coordinatePoint(point);
  }

  private void coordinatesPolygon(final Polygon polygon) {
    this.out.startList(false);
    this.out.indent();

    final LineString shell = polygon.getShell();
    coordinatesLineString(shell.toCounterClockwise());
    for (final LinearRing hole : polygon.holes()) {
      this.out.endAttribute();
      this.out.indent();
      coordinatesLineString(hole.toClockwise());
    }

    this.out.endList();
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  private void geometry(final Geometry geometry) {
    if (geometry == null) {
      this.out.value(null);
    } else {
      this.out.startObject();
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        point(point);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        line(line);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        polygon(polygon);
      } else if (geometry instanceof Punctual) {
        final Punctual punctual = (Punctual)geometry;
        multiPoint(punctual);
      } else if (geometry instanceof Lineal) {
        final Lineal lineal = (Lineal)geometry;
        multiLineString(lineal);
      } else if (geometry instanceof Polygonal) {
        final Polygonal polygonal = (Polygonal)geometry;
        multiPolygon(polygonal);
      } else if (geometry.isGeometryCollection()) {
        geometryCollection(geometry);
      }
      this.out.endObject();
    }
  }

  private void geometryCollection(final Geometry geometryCollection) {
    type(GeoJson.GEOMETRY_COLLECTION);

    this.out.endAttribute();
    this.out.label(GeoJson.GEOMETRIES);
    this.out.startList();
    final int numGeometries = geometryCollection.getGeometryCount();
    if (numGeometries > 0) {
      geometry(geometryCollection.getGeometry(0));
      for (int i = 1; i < numGeometries; i++) {
        final Geometry geometry = geometryCollection.getGeometry(i);
        this.out.endAttribute();
        geometry(geometry);
      }
    }
    this.out.endList();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public ClockDirection getPolygonRingDirection() {
    return ClockDirection.COUNTER_CLOCKWISE;
  }

  public boolean isAllowCustomCoordinateSystem() {
    return this.allowCustomCoordinateSystem;
  }

  private void line(final LineString line) {
    type(GeoJson.LINE_STRING);
    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    if (line.isEmpty()) {
      this.out.startList();
      this.out.endList();
    } else {
      coordinatesLineString(line);
    }
  }

  private void multiLineString(final Lineal lineal) {
    type(GeoJson.MULTI_LINE_STRING);

    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    this.out.startList();
    this.out.indent();
    final int numGeometries = lineal.getGeometryCount();
    if (numGeometries > 0) {
      coordinatesLineString((LineString)lineal.getGeometry(0));
      for (int i = 1; i < numGeometries; i++) {
        final LineString lineString = (LineString)lineal.getGeometry(i);
        this.out.endAttribute();
        this.out.indent();
        coordinatesLineString(lineString);
      }
    }
    this.out.endList();
  }

  private void multiPoint(final Punctual punctual) {
    type(GeoJson.MULTI_POINT);

    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    this.out.startList();
    this.out.indent();
    final int numGeometries = punctual.getGeometryCount();
    if (numGeometries > 0) {
      coordinatesPoint(punctual.getPoint(0));
      for (int i = 1; i < numGeometries; i++) {
        final Point point = punctual.getPoint(i);
        this.out.endAttribute();
        this.out.indent();
        coordinatesPoint(point);
      }
    }
    this.out.endList();
  }

  private void multiPolygon(final Polygonal polygonal) {
    type(GeoJson.MULTI_POLYGON);

    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    this.out.startList();
    this.out.indent();
    final int numGeometries = polygonal.getGeometryCount();
    if (numGeometries > 0) {
      coordinatesPolygon((Polygon)polygonal.getGeometry(0));
      for (int i = 1; i < numGeometries; i++) {
        final Polygon polygon = (Polygon)polygonal.getGeometry(i);
        this.out.endAttribute();
        this.out.indent();
        coordinatesPolygon(polygon);
      }
    }
    this.out.endList();
  }

  private void point(final Point point) {
    type(GeoJson.POINT);
    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    if (point.isEmpty()) {
      this.out.startList();
      this.out.endList();
    } else {
      coordinatesPoint(point);
    }
  }

  private void polygon(final Polygon polygon) {
    type(GeoJson.POLYGON);

    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    if (polygon.isEmpty()) {
      this.out.startList();
      this.out.endList();
    } else {
      coordinatesPolygon(polygon);
    }
  }

  public void setAllowCustomCoordinateSystem(final boolean allowCustomCoordinateSystem) {
    this.allowCustomCoordinateSystem = allowCustomCoordinateSystem;
    if (!allowCustomCoordinateSystem) {
      this.srid = EpsgId.WGS84;
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  private void type(final String type) {
    this.out.labelValue(GeoJson.TYPE, type);
  }

  @Override
  public void write(final Record record) {
    if (this.initialized) {
      this.out.endAttribute();
      if (!isIndent()) {
        this.out.newLineForce();
      }
    } else {
      writeHeader();
      this.initialized = true;
    }
    this.out.startObject();

    type(GeoJson.FEATURE);
    this.out.endAttribute();

    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final int geometryIndex = recordDefinition.getGeometryFieldIndex();

    writeGeometry(record);

    this.out.label(GeoJson.PROPERTIES);
    this.out.startObject();
    boolean hasValue = false;
    for (final FieldDefinition field : recordDefinition.getFields()) {
      final int fieldIndex = field.getIndex();
      if (fieldIndex != geometryIndex) {
        final Object value;
        if (isWriteCodeValues()) {
          value = record.getCodeValue(fieldIndex);
        } else {
          value = record.getValue(fieldIndex);
        }
        if (isValueWritable(value)) {
          if (hasValue) {
            this.out.endAttribute();
          } else {
            hasValue = true;
          }
          final String name = field.getName();
          this.out.label(name);
          if (value instanceof Geometry) {
            Geometry geometry = (Geometry)value;
            if (!this.allowCustomCoordinateSystem) {
              final GeometryFactory wgs84 = this.geometryFactory.convertSrid(EpsgId.WGS84);
              geometry = geometry.convertGeometry(wgs84);
            }
            geometry(geometry);
          } else {
            this.out.value(value);
          }
        }
      }
    }
    this.out.endObject();
    this.out.endObject();
  }

  private void writeFooter() {
    if (!this.singleObject) {
      if (!isIndent()) {
        this.out.newLineForce();
      }
      this.out.endList();
      this.out.endObject();
      this.out.newLineForce();
    }
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      this.out.print(");");
    }
  }

  private void writeGeometry(final Record record) {
    Geometry mainGeometry = record.getGeometry();

    if (mainGeometry != null) {
      final GeometryFactory geometryFactory = this.geometryFactory;
      if (isAllowCustomCoordinateSystem()) {
        if (geometryFactory != null) {
          mainGeometry = mainGeometry.convertGeometry(geometryFactory);
        }
        writeSrid(mainGeometry);
      } else {
        int axisCount = mainGeometry.getAxisCount();
        if (geometryFactory != null) {
          axisCount = geometryFactory.getAxisCount();
        }
        mainGeometry = WGS84.convertGeometry(mainGeometry, axisCount);
      }
    }
    this.out.label(GeoJson.GEOMETRY);
    geometry(mainGeometry);
    this.out.endAttribute();
  }

  private void writeHeader() {
    final JsonWriter out = this.out;
    out.setIndent(isIndent());
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      out.print(callback);
      out.print('(');
    }
    this.singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (!this.singleObject) {
      out.startObject();
      type(GeoJson.FEATURE_COLLECTION);
      out.endAttribute();

      this.srid = writeSrid(this.geometryFactory);

      out.label(GeoJson.FEATURES);
      out.startList();
      out.newLineForce();
    }
  }

  private int writeSrid(final GeometryFactoryProxy geometryFactory) {
    if (isAllowCustomCoordinateSystem() && geometryFactory != null) {
      final int srid = geometryFactory.getHorizontalCoordinateSystemId();
      if (srid > 0 && srid != EpsgId.WGS84 && srid != this.srid) {
        final String urn = GeoJson.URN_OGC_DEF_CRS_EPSG + srid;
        this.out.label(GeoJson.CRS);
        this.out.startObject();
        type(GeoJson.NAME);
        this.out.endAttribute();

        this.out.label(GeoJson.PROPERTIES);
        this.out.startObject();

        this.out.labelValue(GeoJson.NAME, urn);

        this.out.endObject();
        this.out.endObject();
        this.out.endAttribute();
      }
      return this.srid;
    } else {
      return EpsgId.WGS84;
    }
  }
}
