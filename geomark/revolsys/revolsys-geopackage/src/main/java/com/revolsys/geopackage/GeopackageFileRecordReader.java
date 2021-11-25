package com.revolsys.geopackage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordStoreQueryReader;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class GeopackageFileRecordReader implements RecordReader {

  private final GeoPackageRecordStore recordStore;

  private final RecordStoreQueryReader reader;

  public GeopackageFileRecordReader(final Resource resource,
    final RecordFactory<? extends Record> factory, final MapEx properties) {
    this.recordStore = GeoPackage.openRecordStore(resource);
    this.recordStore.setProperties(properties);
    this.recordStore.initialize();
    final List<Query> queries = new ArrayList<>();
    for (final RecordDefinition recordDefinition : this.recordStore.getRootSchema()
      .getRecordDefinitions()) {
      final Query query = new Query(recordDefinition)//
        .setOrderByFieldNames(recordDefinition.getIdFieldNames());
      queries.add(query);
    }
    this.reader = new RecordStoreQueryReader(this.recordStore);
    this.reader.setProperties(properties);
    this.reader.setQueries(queries);
  }

  @Override
  public void cancel() {
    this.reader.cancel();
  }

  @Override
  public void close() {
    try {
      this.reader.close();
    } finally {
      this.recordStore.close();
    }
  }

  @Override
  public ClockDirection getPolygonRingDirection() {
    return ClockDirection.OGC_SFS_COUNTER_CLOCKWISE;
  }

  @Override
  public MapEx getProperties() {
    return this.reader.getProperties();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.reader.getRecordDefinition();
  }

  @Override
  public boolean isCancelled() {
    return this.reader.isCancelled();
  }

  @Override
  public Iterator<Record> iterator() {
    return this.reader.iterator();
  }

  @Override
  public void open() {
    this.reader.open();
  }

}
