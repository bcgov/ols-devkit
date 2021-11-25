package com.revolsys.geopackage.field;

import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordDefinition;

public class GeoPackageJdbcFieldAdder extends JdbcFieldAdder {

  @Override
  public JdbcFieldDefinition newField(final AbstractJdbcRecordStore recordStore,
    final JdbcRecordDefinition recordDefinition, final String dbName, final String name,
    final String dbDataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    JdbcFieldDefinition field;
    if ("DATE".equals(dbDataType)) {
      field = new GeoPackageDateField(dbName, name, sqlType, required, description, null);
    } else if ("DATETIME".equals(dbDataType)) {
      field = new GeoPackageDateTimeField(dbName, name, sqlType, required, description, null);
    } else {
      field = null;
    }
    return field;
  }
}
