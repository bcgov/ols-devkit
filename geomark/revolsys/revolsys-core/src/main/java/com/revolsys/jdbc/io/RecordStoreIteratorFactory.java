package com.revolsys.jdbc.io;

import java.util.Map;

import org.jeometry.common.function.Function3;

import com.revolsys.record.io.RecordIterator;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordStore;

public class RecordStoreIteratorFactory {

  private Function3<RecordStore, Query, Map<String, Object>, RecordIterator> createFunction;

  public RecordStoreIteratorFactory() {
  }

  public RecordStoreIteratorFactory(
    final Function3<RecordStore, Query, Map<String, Object>, RecordIterator> createFunction) {
    this.createFunction = createFunction;
  }

  public RecordIterator newIterator(final RecordStore recordStore, final Query query,
    final Map<String, Object> properties) {
    if (this.createFunction != null) {
      return this.createFunction.apply(recordStore, query, properties);
    } else {
      throw new UnsupportedOperationException("Creating query iterators not supported");
    }
  }

}
