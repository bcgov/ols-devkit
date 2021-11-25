package com.revolsys.jdbc.field;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;

public class JdbcBigIntegerFieldDefinition extends JdbcFieldDefinition {
  public JdbcBigIntegerFieldDefinition(final String dbName, final String name, final int sqlType,
    final int length, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(dbName, name, DataTypes.BIG_INTEGER, sqlType, length, 0, required, description,
      properties);
  }

  @Override
  public JdbcBigIntegerFieldDefinition clone() {
    return new JdbcBigIntegerFieldDefinition(getDbName(), getName(), getSqlType(), getLength(),
      isRequired(), getDescription(), getProperties());
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    Object value;
    final int length = getLength();
    if (length <= 2) {
      value = resultSet.getByte(indexes.incrementAndGet());
    } else if (length <= 4) {
      value = resultSet.getShort(indexes.incrementAndGet());
    } else if (length <= 9) {
      value = resultSet.getInt(indexes.incrementAndGet());
    } else if (length <= 18) {
      value = resultSet.getLong(indexes.incrementAndGet());
    } else {
      final BigDecimal number = resultSet.getBigDecimal(indexes.incrementAndGet());
      if (number == null) {
        value = null;
      } else {
        value = number.toBigInteger();
      }
    }
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
      if (value instanceof BigDecimal) {
        final BigDecimal number = (BigDecimal)value;
        statement.setBigDecimal(parameterIndex, number);
      } else if (value instanceof BigInteger) {
        final BigInteger number = (BigInteger)value;
        statement.setBigDecimal(parameterIndex, new BigDecimal(number));
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        statement.setLong(parameterIndex, number.longValue());
      } else {
        final BigDecimal number = new BigDecimal(value.toString());
        statement.setBigDecimal(parameterIndex, number);
      }
    }
    return parameterIndex + 1;
  }
}
