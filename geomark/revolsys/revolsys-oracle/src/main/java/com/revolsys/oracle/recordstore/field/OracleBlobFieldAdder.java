package com.revolsys.oracle.recordstore.field;

import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordDefinition;

public class OracleBlobFieldAdder extends JdbcFieldAdder {

  public OracleBlobFieldAdder() {
  }

  @Override
  public JdbcFieldDefinition newField(final AbstractJdbcRecordStore recordStore,
    final JdbcRecordDefinition recordDefinition, final String dbName, final String name,
    final String dbDataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    return new OracleJdbcBlobFieldDefinition(dbName, name, sqlType, required, description);
  }

}
