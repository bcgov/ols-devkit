package com.revolsys.record.io.format.wkt;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.Records;
import com.revolsys.record.io.AbstractRecordReader;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class WktRecordReader extends AbstractRecordReader {

  private BufferedReader in;

  private WktParser wktParser;

  public WktRecordReader(final RecordFactory<? extends Record> recordFactory,
    final Resource resource) {
    super(recordFactory);
    this.in = resource.newBufferedReader();
    final RecordDefinition recordDefinition = Records.newGeometryRecordDefinition();
    setRecordDefinition(recordDefinition);
  }

  @Override
  protected void closeDo() {
    super.closeDo();
    FileUtil.closeSilent(this.in);
    this.in = null;
    this.wktParser = null;
  }

  @Override
  protected Record getNext() {
    try {
      final String wkt = this.in.readLine();
      final Geometry geometry = this.wktParser.parseGeometry(wkt, false);
      if (geometry == null) {
        throw new NoSuchElementException();
      } else {
        final Record record = newRecord();
        record.setGeometryValue(geometry);
        return record;
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error reading geometry ", e);
    }

  }

  @Override
  public ClockDirection getPolygonRingDirection() {
    return ClockDirection.COUNTER_CLOCKWISE;
  }

  @Override
  protected void initDo() {
    GeometryFactory geometryFactory;
    final RecordDefinition recordDefinition = getRecordDefinition();
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    if (geometryField == null) {
      geometryFactory = GeometryFactory.DEFAULT_3D;
    } else {
      geometryFactory = geometryField.getGeometryFactory();
      if (geometryFactory == null || !geometryFactory.isHasHorizontalCoordinateSystem()) {
        geometryFactory = getGeometryFactory();
        if (geometryFactory == null) {
          geometryFactory = GeometryFactory.DEFAULT_3D;
        }
        geometryField.setGeometryFactory(geometryFactory);
        recordDefinition.setGeometryFactory(geometryFactory);
      }
    }
    this.wktParser = new WktParser(geometryFactory);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
