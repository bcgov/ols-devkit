package com.revolsys.record.io.format.gpx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class GpxRecordWriter extends AbstractRecordWriter implements GpxAttributes, GpxElements {

  private String commentAttribute = "comment";

  private String descriptionAttribute = "description";

  private File file;

  private String nameAttribute = "name";

  private String symAttribute = "sym";

  private final XmlWriter out;

  public GpxRecordWriter(final RecordDefinitionProxy recordDefinition, final Writer writer) {
    super(recordDefinition);
    this.out = new XmlWriter(new BufferedWriter(writer));
    this.out.setIndent(false);
    this.out.startDocument("UTF-8", "1.0");
    startTag(GPX);
    this.out.attribute(VERSION, "1.1");
    this.out.attribute(CREATOR, "Revolution Systems Inc. - GIS");
  }

  @Override
  public void close() {
    endTag(GPX);
    this.out.endDocument();
    this.out.close();
  }

  private void endTag(final String name) {
    this.out.endTag();
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  public String getCommentAttribute() {
    return this.commentAttribute;
  }

  public String getDescriptionAttribute() {
    return this.descriptionAttribute;
  }

  public String getNameAttribute() {
    return this.nameAttribute;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return GpxIterator.GPX_TYPE;
  }

  public String getSymAttribute() {
    return this.symAttribute;
  }

  public void setCommentAttribute(final String commentAttribute) {
    this.commentAttribute = commentAttribute;
  }

  public void setDescriptionAttribute(final String descriptionAttribute) {
    this.descriptionAttribute = descriptionAttribute;
  }

  public void setNameAttribute(final String nameAttribute) {
    this.nameAttribute = nameAttribute;
  }

  public void setSymAttribute(final String symAttribute) {
    this.symAttribute = symAttribute;
  }

  private void startTag(final String name) {
    this.out.startTag(Gpx.GPX_NS_URI, name);
  }

  @Override
  public String toString() {
    return this.file.getAbsolutePath();
  }

  @Override
  public void write(final Record record) {
    try {
      final Geometry geometry = record.getGeometry();
      if (geometry instanceof Point) {
        writeWaypoint(record);
      } else if (geometry instanceof LineString) {
        writeTrack(record);
      }
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private void writeAttributes(final Record record) {
    final Object time = record.getValue("timestamp");
    if (time != null) {
      if (time instanceof Date) {
        final DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        timestampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.out.element(TIME, timestampFormat.format(time));
      } else {
        this.out.element(TIME, time.toString());
      }
    }
    writeElement(record, NAME, this.nameAttribute);
    writeElement(record, COMMENT, this.commentAttribute);
    writeElement(record, DESCRIPTION, this.descriptionAttribute);
    writeElement(record, SYM, this.symAttribute);
  }

  private void writeElement(final Record record, final String tag, final String fieldName) {
    final String name = record.getValue(fieldName);
    if (name != null && name.length() > 0) {
      this.out.element(tag, name);
    }
  }

  private void writeTrack(final Record record) throws IOException {
    startTag(TRACK);
    LineString line = record.getGeometry();
    line = line.convertGeometry(Gpx.GEOMETRY_FACTORY);
    writeAttributes(record);
    startTag(TRACK_SEGMENT);

    final int vertexCount = line.getVertexCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      final double x = line.getX(vertexIndex);
      final double y = line.getY(vertexIndex);
      startTag(TRACK_POINT);
      this.out.attribute(LON, x);
      this.out.attribute(LAT, y);
      if (line.getAxisCount() > 2) {
        final double elevation = line.getZ(vertexIndex);
        if (!Double.isNaN(elevation)) {
          this.out.element(ELEVATION, String.valueOf(elevation));
        }
      }
      endTag(TRACK_POINT);
    }
    endTag(TRACK_SEGMENT);
    endTag(TRACK);
  }

  private void writeWaypoint(final Record wayPoint) throws IOException {
    startTag(WAYPOINT);
    final Point point = wayPoint.getGeometry();
    final Point geoPoint = point.convertGeometry(Gpx.GEOMETRY_FACTORY);
    this.out.attribute(LON, geoPoint.getX());
    this.out.attribute(LAT, geoPoint.getY());
    if (point.getAxisCount() > 2) {
      final double elevation = geoPoint.getZ();
      if (!Double.isNaN(elevation)) {
        this.out.element(ELEVATION, String.valueOf(elevation));
      }
    }
    writeAttributes(wayPoint);
    endTag(WAYPOINT);
  }
}
