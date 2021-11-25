package com.revolsys.record.io;

import java.util.Iterator;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.map.MapReader;

public class RecordMapReader implements MapReader {
  private final RecordReader reader;

  public RecordMapReader(final RecordReader reader) {
    this.reader = reader;
  }

  @Override
  public void cancel() {
    this.reader.cancel();
  }

  @Override
  public void close() {
    this.reader.close();
  }

  @Override
  public MapEx getProperties() {
    return this.reader.getProperties();
  }

  @Override
  public boolean isCancelled() {
    return this.reader.isCancelled();
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public Iterator<MapEx> iterator() {
    return (Iterator)this.reader.iterator();
  }

  @Override
  public void open() {
    this.reader.open();
  }
}
