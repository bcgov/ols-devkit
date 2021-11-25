package com.revolsys.jdbc.io;

import org.jeometry.common.io.PathName;

import com.revolsys.record.schema.RecordStoreSchema;

public class JdbcRecordStoreSchema extends RecordStoreSchema {

  private String dbName;

  private String quotedDbName;

  private boolean quoteName;

  public JdbcRecordStoreSchema(final AbstractJdbcRecordStore recordStore) {
    super(recordStore);
  }

  public JdbcRecordStoreSchema(final JdbcRecordStoreSchema schema, final PathName pathName,
    final String dbName) {
    this(schema, pathName, dbName, false);
  }

  public JdbcRecordStoreSchema(final JdbcRecordStoreSchema schema, final PathName pathName,
    final String dbName, final boolean quoteName) {
    super(schema, pathName);
    this.quoteName = quoteName;
    this.dbName = dbName;
    if (quoteName) {
      this.quotedDbName = '"' + dbName + '"';
    } else {
      this.quotedDbName = dbName;
    }
  }

  public String getDbName() {
    return this.dbName;
  }

  public String getQuotedDbName() {
    return this.quotedDbName;
  }

  public boolean isQuoteName() {
    return this.quoteName;
  }
}
