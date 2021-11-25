package com.revolsys.record.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.record.GeometryRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.schema.RecordDefinition;

public class GeometryRecordReader extends AbstractRecordReader {
  private GeometryReader geometryReader;

  private Iterator<Geometry> geometryIterator;

  private final String baseName;

  public GeometryRecordReader(final String baseName, final GeometryReader geometryReader,
    final RecordFactory<? extends Record> recordFactory) {
    super(recordFactory);
    this.baseName = baseName;
    this.geometryReader = geometryReader;
  }

  @Override
  protected void closeDo() {
    final Iterator<Geometry> geometryIterator = this.geometryIterator;
    this.geometryIterator = null;
    if (geometryIterator instanceof BaseCloseable) {
      final BaseCloseable closeable = (BaseCloseable)geometryIterator;
      closeable.close();
    }
    final GeometryReader geometryReader = this.geometryReader;
    this.geometryReader = null;
    if (geometryReader != null) {
      geometryReader.close();
    }
    super.closeDo();
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    if (this.geometryIterator.hasNext()) {
      final Geometry geometry = this.geometryIterator.next();
      final RecordDefinition recordDefinition = getRecordDefinition();
      return new GeometryRecord(recordDefinition, geometry);
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public ClockDirection getPolygonRingDirection() {
    if (this.geometryReader == null) {
      return ClockDirection.NONE;
    } else {
      return this.geometryReader.getPolygonRingDirection();
    }
  }

  @Override
  protected void initDo() {
    this.geometryIterator = this.geometryReader.iterator();
    this.geometryIterator.hasNext();
    final GeometryFactory geometryFactory = this.geometryReader.getGeometryFactory();
    setGeometryFactory(geometryFactory);

    final RecordDefinition recordDefinition = this.geometryReader
      .newRecordDefinition(this.baseName);
    recordDefinition.setGeometryFactory(geometryFactory);
    setRecordDefinition(recordDefinition);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    super.setProperty(name, value);
    this.geometryReader.setProperty(name, value);
  }

}
