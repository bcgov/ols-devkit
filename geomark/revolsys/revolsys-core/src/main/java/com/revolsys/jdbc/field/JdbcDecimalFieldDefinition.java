package com.revolsys.jdbc.field;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;

public class JdbcDecimalFieldDefinition extends JdbcFieldDefinition {
  public JdbcDecimalFieldDefinition(final String dbName, final String name, final int sqlType,
    final int length, final int scale, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(dbName, name, DataTypes.DECIMAL, sqlType, length, scale, required, description,
      properties);
  }

  @Override
  public JdbcDecimalFieldDefinition clone() {
    return new JdbcDecimalFieldDefinition(getDbName(), getName(), getSqlType(), getLength(),
      getScale(), isRequired(), getDescription(), getProperties());
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
        statement.setDouble(parameterIndex, number.doubleValue());
      } else {
        final BigDecimal number = new BigDecimal(value.toString());
        statement.setBigDecimal(parameterIndex, number);
      }
    }
    return parameterIndex + 1;
  }
}
