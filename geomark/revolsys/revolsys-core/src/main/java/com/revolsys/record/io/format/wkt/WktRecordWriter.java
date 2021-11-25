package com.revolsys.record.io.format.wkt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class WktRecordWriter extends AbstractRecordWriter {

  private boolean open;

  private final Writer out;

  private GeometryFactory geometryFactory;

  public WktRecordWriter(final RecordDefinitionProxy recordDefinition, final Writer out) {
    super(recordDefinition);
    this.out = new BufferedWriter(out);
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    if (geometryField != null) {
      this.geometryFactory = geometryField.getGeometryFactory();
    }
  }

  @Override
  public void close() {
    FileUtil.closeSilent(this.out);
  }

  @Override
  public void flush() {
    try {
      this.out.flush();
    } catch (final IOException e) {
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public ClockDirection getPolygonRingDirection() {
    return ClockDirection.COUNTER_CLOCKWISE;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public String toString() {
    return getPathName().toString();
  }

  @Override
  public void write(final Record record) {
    try {
      if (!this.open) {
        this.open = true;
      }
      Geometry geometry = record.getGeometry();
      geometry = this.geometryFactory.convertGeometry(geometry);
      final int srid = geometry.getHorizontalCoordinateSystemId();
      if (srid > 0) {
        this.out.write("SRID=");
        this.out.write(Integer.toString(srid));
        this.out.write(';');
      }
      EWktWriter.writeCCW(this.out, geometry);
      this.out.write('\n');
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

}
