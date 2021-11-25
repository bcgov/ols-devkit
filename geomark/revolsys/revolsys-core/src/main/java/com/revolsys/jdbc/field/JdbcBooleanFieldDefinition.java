package com.revolsys.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;

public class JdbcBooleanFieldDefinition extends JdbcFieldDefinition {
  public JdbcBooleanFieldDefinition(final String dbName, final String name, final int sqlType,
    final int length, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(dbName, name, DataTypes.BOOLEAN, sqlType, length, 0, required, description, properties);
  }

  @Override
  public JdbcBooleanFieldDefinition clone() {
    return new JdbcBooleanFieldDefinition(getDbName(), getName(), getSqlType(), getLength(),
      isRequired(), getDescription(), getProperties());
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    final boolean value = resultSet.getBoolean(indexes.incrementAndGet());
    if (resultSet.wasNull()) {
      return null;
    } else {
      return value;
    }
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, getSqlType());
    } else {
      boolean booleanValue;
      if (value instanceof Boolean) {
        booleanValue = (Boolean)value;
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        booleanValue = number.intValue() == 1;
      } else {
        final String stringValue = value.toString();
        if (stringValue.equals("1") || Boolean.parseBoolean(stringValue)) {
          booleanValue = true;
        } else {
          booleanValue = false;
        }
      }
      statement.setBoolean(parameterIndex, booleanValue);
    }
    return parameterIndex + 1;
  }
}
