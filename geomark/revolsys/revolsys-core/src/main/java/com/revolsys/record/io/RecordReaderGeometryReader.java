package com.revolsys.record.io;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.Record;

public class RecordReaderGeometryReader implements Iterator<Geometry>, GeometryReader {
  private RecordReader reader;

  private Iterator<Record> iterator;

  public RecordReaderGeometryReader(final RecordReader reader) {
    this.reader = reader;
  }

  @Override
  public void cancel() {
    this.reader.cancel();
  }

  @Override
  public void close() {
    if (this.reader != null) {
      this.reader.close();
    }
    this.reader = null;
    this.iterator = null;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.reader.getGeometryFactory();
  }

  @Override
  public ClockDirection getPolygonRingDirection() {
    if (this.reader == null) {
      return GeometryReader.super.getPolygonRingDirection();
    } else {
      return this.reader.getPolygonRingDirection();
    }
  }

  @Override
  public final MapEx getProperties() {
    return this.reader.getProperties();
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public boolean isCancelled() {
    return this.reader.isCancelled();
  }

  @Override
  public Iterator<Geometry> iterator() {
    open();
    return this;
  }

  @Override
  public Geometry next() {
    if (this.iterator.hasNext()) {
      final Record record = this.iterator.next();
      return record.getGeometry();
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void open() {
    this.iterator = this.reader.iterator();
  }

  @Override
  public void remove() {
    this.iterator.remove();
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    this.reader.setProperties(properties);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    this.reader.setProperty(name, value);
  }
}
