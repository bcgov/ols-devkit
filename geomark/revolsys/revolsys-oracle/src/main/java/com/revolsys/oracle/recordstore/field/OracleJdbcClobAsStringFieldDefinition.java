package com.revolsys.oracle.recordstore.field;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jeometry.common.jdbc.StringClob;

import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;

public class OracleJdbcClobAsStringFieldDefinition extends OracleJdbcClobFieldDefinition {
  public OracleJdbcClobAsStringFieldDefinition(final String dbName, final String name,
    final int sqlType, final boolean required, final String description) {
    super(dbName, name, sqlType, required, description);
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    final String string = resultSet.getString(indexes.incrementAndGet());
    return new StringClob(string);
  }

}
