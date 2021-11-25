package com.revolsys.jdbc.field;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordDefinition;

public class JdbcStringFieldAdder extends JdbcFieldAdder {

  public JdbcStringFieldAdder() {
    super(DataTypes.STRING);
  }

  @Override
  public JdbcFieldDefinition newField(final AbstractJdbcRecordStore recordStore,
    final JdbcRecordDefinition recordDefinition, final String dbName, final String name,
    final String dbDataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    return new JdbcStringFieldDefinition(dbName, name, sqlType, length, required, description,
      null);
  }
}
