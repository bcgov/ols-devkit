package com.revolsys.record.io.format.kml;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.util.Property;

public class KmlRecordWriter extends AbstractRecordWriter implements Kml22Constants {
  private static final Map<Class<?>, String> TYPE_MAP = new HashMap<>();

  public static final GeometryFactory GEOMETRY_FACTORY_3D = GeometryFactory
    .floating3d(Kml22Constants.COORDINATE_SYSTEM_ID);

  public static final GeometryFactory GEOMETRY_FACTORY_2D = GeometryFactory
    .floating2d(Kml22Constants.COORDINATE_SYSTEM_ID);

  static {
    TYPE_MAP.put(Double.class, "decimal");
    TYPE_MAP.put(Integer.class, "decimal");
    TYPE_MAP.put(BigDecimal.class, "decimal");
    TYPE_MAP.put(java.sql.Date.class, "dateTime");
    TYPE_MAP.put(java.util.Date.class, "dateTime");
    TYPE_MAP.put(String.class, "string");
    TYPE_MAP.put(Geometry.class, "wktGeometry");

  }

  private String defaultStyleUrl;

  private boolean opened;

  private String styleUrl;

  private final java.io.Writer writer;

  public KmlRecordWriter(final RecordDefinitionProxy recordDefinition, final java.io.Writer out) {
    super(recordDefinition);
    this.writer = out;
  }

  @Override
  public void close() {
    open();
    try {
      if (!Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY))) {
        this.writer.write("</Document>\n");
      }
      this.writer.write("</kml>\n");
      this.writer.close();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void flush() {
    try {
      this.writer.flush();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return GEOMETRY_FACTORY_3D;
  }

  public boolean isKmlWriteNulls() {
    return super.isWriteNulls();
  }

  @Override
  public void open() {
    if (!this.opened) {
      try {
        writeHeader();
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  public void setKmlWriteNulls(final boolean writeNulls) {
    super.setWriteNulls(writeNulls);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    super.setProperty(name, value);
    if (Kml22Constants.STYLE_URL_PROPERTY.equals(name)) {
      String styleUrl;
      if (value == null) {
        styleUrl = null;
      } else {
        styleUrl = value.toString();
      }
      if (Property.hasValue(styleUrl)) {
        if (Property.hasValue(this.defaultStyleUrl)) {
          this.styleUrl = styleUrl;
        } else {
          this.defaultStyleUrl = styleUrl;
        }
      } else {
        this.styleUrl = this.defaultStyleUrl;
      }
    }
  }

  @Override
  public String toString() {
    return "KML Writer";
  }

  @Override
  public void write(final Record record) {
    try {
      open();
      this.writer.write("<Placemark>\n");
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final int geometryIndex = recordDefinition.getGeometryFieldIndex();
      final int idIndex = recordDefinition.getIdFieldIndex();

      final String nameAttribute = getProperty(PLACEMARK_NAME_ATTRIBUTE_PROPERTY);
      String name = null;
      if (nameAttribute != null) {
        name = record.getValue(nameAttribute);
      }
      if (name == null && idIndex != -1) {
        final Object id = record.getValue(idIndex);
        final String typeName = recordDefinition.getName();
        name = typeName + " " + id;
      }
      if (name != null) {
        this.writer.write("<name>");
        XmlWriter.writeElementContent(this.writer, name);
        this.writer.write("</name>\n");
      }
      final String snippet = getProperty(SNIPPET_PROPERTY);
      if (snippet != null) {
        this.writer.write("<Snippet>");
        XmlWriter.writeElementContent(this.writer, snippet);
        this.writer.write("</Snippet>\n");
      }
      String description = getProperty(PLACEMARK_DESCRIPTION_PROPERTY);
      if (description == null) {
        description = getProperty(IoConstants.DESCRIPTION_PROPERTY);
      }
      if (Property.hasValue(description)) {
        this.writer.write("<description>");
        this.writer.write("<![CDATA[");
        this.writer.write(description);
        this.writer.write("]]>");
        this.writer.write("<description>");
      }
      Geometry geometry = null;
      GeometryFactory kmlGeometryFactory = GEOMETRY_FACTORY_2D;
      final List<Integer> geometryFieldIndexes = recordDefinition.getGeometryFieldIndexes();
      if (!geometryFieldIndexes.isEmpty()) {
        if (geometryFieldIndexes.size() == 1) {
          geometry = record.getValue(geometryFieldIndexes.get(0));
          final int axisCount = geometry.getAxisCount();
          if (axisCount > 2) {
            kmlGeometryFactory = GEOMETRY_FACTORY_2D.convertAxisCount(axisCount);
          }
          geometry = geometry.convertGeometry(kmlGeometryFactory);
        } else {
          final List<Geometry> geometries = new ArrayList<>();
          for (final Integer geometryFieldIndex : geometryFieldIndexes) {
            Geometry part = record.getValue(geometryFieldIndex);
            if (part != null) {
              final int axisCount = part.getAxisCount();
              if (axisCount > 2) {
                kmlGeometryFactory = GEOMETRY_FACTORY_2D.convertAxisCount(axisCount);
              }
              part = part.convertGeometry(kmlGeometryFactory);
              if (!part.isEmpty()) {
                geometries.add(part);
              }
            }
          }
          if (!geometries.isEmpty()) {
            geometry = kmlGeometryFactory.geometry(geometries);
          }
        }

      }
      writeLookAt(geometry);
      if (Property.hasValue(this.styleUrl)) {
        this.writer.write("<styleUrl>");
        XmlWriter.writeElementContent(this.writer, this.styleUrl);
        this.writer.write("</styleUrl>\n");
      } else if (Property.hasValue(this.defaultStyleUrl)) {
        this.writer.write("<styleUrl>");
        XmlWriter.writeElementContent(this.writer, this.defaultStyleUrl);
        this.writer.write("</styleUrl>\n");
      }
      boolean hasValues = false;
      for (final FieldDefinition field : recordDefinition.getFields()) {
        final int fieldIndex = field.getIndex();
        if (fieldIndex != geometryIndex) {
          final String fieldName = field.getName();
          final Object value;
          if (isWriteCodeValues()) {
            value = record.getCodeValue(fieldIndex);
          } else {
            value = record.getValue(fieldIndex);
          }
          if (isValueWritable(value)) {
            if (!hasValues) {
              hasValues = true;
              this.writer.write("<ExtendedData>\n");
            }
            this.writer.write("<Data name=\"");
            XmlWriter.writeAttributeContent(this.writer, fieldName);
            this.writer.write("\">\n");
            this.writer.write("<value>");
            if (Property.hasValue(value)) {
              XmlWriter.writeElementContent(this.writer, value.toString());
            }
            this.writer.write("</value>\n");
            this.writer.write("</Data>\n");
          }
        }
      }
      if (hasValues) {
        this.writer.write("</ExtendedData>\n");
      }

      if (geometry != null) {
        GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
        if (geometryFactory == null) {
          geometryFactory = geometry.getGeometryFactory();
        }
        final int axisCount = geometryFactory.getAxisCount();
        KmlWriterUtil.writeGeometry(this.writer, geometry, axisCount);
      }
      this.writer.write("</Placemark>\n");
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private void writeHeader() throws IOException {
    this.opened = true;

    this.writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

    this.writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
    if (!Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY))) {
      this.writer.write("<Document>\n");
      final String name = getProperty(DOCUMENT_NAME_PROPERTY);
      if (Property.hasValue(name)) {
        this.writer.write("<name>");
        XmlWriter.writeElementContent(this.writer, name);
        this.writer.write("</name>\n");
      }
      final String snippet = getProperty(SNIPPET_PROPERTY);
      if (Property.hasValue(snippet)) {
        this.writer.write("<Snippet>");
        XmlWriter.writeElementContent(this.writer, snippet);
        this.writer.write("</Snippet>\n");
      }
      final String description = getProperty(DOCUMENT_DESCRIPTION_PROPERTY);
      if (Property.hasValue(description)) {
        this.writer.write("<description>");
        XmlWriter.writeElementContent(this.writer, description);
        this.writer.write("</description>\n");
      }
      this.writer.write("<open>1</open>\n");
      final Point point = getProperty(LOOK_AT_POINT_PROPERTY);
      if (point != null) {
        Number range = getProperty(LOOK_AT_RANGE_PROPERTY);
        if (range == null) {
          range = 1000;
        }
        final double[] coordinates = point.convertCoordinates(GEOMETRY_FACTORY_2D);
        final double x = coordinates[0];
        final double y = coordinates[1];
        writeLookAt(x, y, range.longValue());
      }
      final String style = getProperty(STYLE_PROPERTY);
      if (Property.hasValue(style)) {
        this.writer.write(style);
      }

    }
  }

  public void writeLookAt(final double x, final double y, long range) {
    try {
      final Number minRange = getProperty(Kml22Constants.LOOK_AT_MIN_RANGE_PROPERTY);
      if (minRange != null) {
        if (range < minRange.doubleValue()) {
          range = minRange.longValue();
        }
      }
      final Number maxRange = getProperty(Kml22Constants.LOOK_AT_MAX_RANGE_PROPERTY);
      if (maxRange != null) {
        if (range > maxRange.doubleValue()) {
          range = maxRange.longValue();
        }
      }

      this.writer.write("<LookAt>\n");
      this.writer.write("<longitude>");
      this.writer.write(Doubles.toString(x));
      this.writer.write("</longitude>\n");
      this.writer.write("<latitude>");
      this.writer.write(Doubles.toString(y));
      this.writer.write("</latitude>\n");
      this.writer.write("<altitude>0</altitude>\n");
      this.writer.write("<heading>0</heading>\n");
      this.writer.write("<tilt>0</tilt>\n");
      this.writer.write("<range>");
      this.writer.write(Long.toString(range));
      this.writer.write("</range>\n");
      this.writer.write("</LookAt>\n");
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private void writeLookAt(final Geometry geometry) {
    if (geometry != null) {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      final double centreX = boundingBox.getCentreX();
      final double centreY = boundingBox.getCentreY();

      final Number configRange = getProperty(LOOK_AT_RANGE_PROPERTY);
      final long range;
      if (configRange == null) {
        range = Kml.getLookAtRange(boundingBox);
      } else {
        range = configRange.longValue();
      }
      writeLookAt(centreX, centreY, range);
    }
  }
}
