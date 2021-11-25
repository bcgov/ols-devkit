package com.revolsys.oracle.recordstore.field;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.RecordState;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;

import oracle.jdbc.OracleClob;

public class OracleJdbcClobFieldDefinition extends JdbcFieldDefinition {
  public OracleJdbcClobFieldDefinition(final String dbName, final String name, final int sqlType,
    final boolean required, final String description) {
    super(dbName, name, DataTypes.CLOB, sqlType, 0, 0, required, description,
      Collections.<String, Object> emptyMap());
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    return resultSet.getClob(indexes.incrementAndGet());
  }

  @Override
  public boolean isSortable() {
    return false;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      if (value instanceof OracleClob) {
        final OracleClob clob = (OracleClob)value;
        statement.setClob(parameterIndex, clob);
      } else {
        Reader in;
        if (value instanceof Resource) {
          final Resource resource = (Resource)value;
          in = resource.newBufferedReader();
        } else if (value instanceof Clob) {
          final Clob clob = (Clob)value;
          in = clob.getCharacterStream();
        } else if (value instanceof String) {
          final String string = (String)value;
          in = new StringReader(string);
        } else if (value instanceof File) {
          final File file = (File)value;
          final PathResource resource = new PathResource(file);
          in = resource.newBufferedReader();
        } else {
          throw new IllegalArgumentException("Not valid for a clob column");
        }
        statement.setCharacterStream(parameterIndex, in);
      }
    }
    return parameterIndex + 1;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V toFieldValueException(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Clob) {
      return (V)value;
    } else {
      return DataTypes.CLOB.toObject(value);
    }
  }

  @Override
  public <V> V toFieldValueException(final RecordState state, final Object value) {
    return toFieldValueException(value);
  }
}
