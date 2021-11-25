package com.revolsys.gis.postgresql.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;

public class PostgreSQLOidFieldDefinition extends JdbcFieldDefinition {

  public PostgreSQLOidFieldDefinition() {
    super("ctid", "ctid", DataTypes.LONG, Types.OTHER, 0, 0, true, "Row Identifier",
      Collections.emptyMap());
  }

  @Override
  public void addInsertStatementPlaceHolder(final StringBuilder sql, final boolean generateKeys) {
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    Object value = resultSet.getObject(indexes.incrementAndGet());
    if (value instanceof PostgreSQLTidWrapper) {
      final PostgreSQLTidWrapper wrapper = (PostgreSQLTidWrapper)value;
      value = wrapper.getTid();
    }
    return value;
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    return parameterIndex;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    Object value) throws SQLException {
    value = new PostgreSQLTidWrapper(value);
    statement.setObject(parameterIndex, value);
    return parameterIndex + 1;
  }

  @Override
  public Object validate(final Record record, final Object value) {
    if (record.getState() == RecordState.NEW) {
      return true;
    } else {
      return super.validate(record, value);
    }
  }

}
