package com.revolsys.geopackage;

import com.revolsys.jdbc.io.JdbcRecordWriter;
import com.revolsys.record.io.RecordStoreRecordWriter;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class GeoPackageRecordWriter extends RecordStoreRecordWriter {

  public GeoPackageRecordWriter(final GeoPackageRecordStore recordStore,
    final RecordDefinitionProxy recordDefinition) {
    super(recordStore, recordDefinition);
    getWriter().setProperty("batchSize", 1000);
  }

  @Override
  public void close() {
    final JdbcRecordWriter writer = (JdbcRecordWriter)getWriter();
    writer.commit();
    super.close();
  }

}
