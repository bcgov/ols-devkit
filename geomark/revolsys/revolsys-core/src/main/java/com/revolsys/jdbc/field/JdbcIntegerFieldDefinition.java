package com.revolsys.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;

public class JdbcIntegerFieldDefinition extends JdbcFieldDefinition {
  public JdbcIntegerFieldDefinition(final String dbName, final String name, final int sqlType,
    final boolean required, final String description, final Map<String, Object> properties) {
    super(dbName, name, DataTypes.INT, sqlType, 11, 0, required, description, properties);
  }

  @Override
  public JdbcIntegerFieldDefinition clone() {
    return new JdbcIntegerFieldDefinition(getDbName(), getName(), getSqlType(), isRequired(),
      getDescription(), getProperties());
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    final int value = resultSet.getInt(indexes.incrementAndGet());
    if (resultSet.wasNull()) {
      return null;
    } else {
      return Integer.valueOf(value);
    }
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, getSqlType());
    } else {
      int numberValue;
      if (value instanceof Number) {
        final Number number = (Number)value;
        numberValue = number.intValue();
      } else {
        numberValue = Integer.parseInt(value.toString());
      }
      statement.setInt(parameterIndex, numberValue);
    }
    return parameterIndex + 1;
  }
}
