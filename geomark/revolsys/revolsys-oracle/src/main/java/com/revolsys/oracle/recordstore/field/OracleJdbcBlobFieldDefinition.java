package com.revolsys.oracle.recordstore.field;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class OracleJdbcBlobFieldDefinition extends JdbcFieldDefinition {
  public OracleJdbcBlobFieldDefinition(final String dbName, final String name, final int sqlType,
    final boolean required, final String description) {
    super(dbName, name, DataTypes.BLOB, sqlType, 0, 0, required, description,
      Collections.<String, Object> emptyMap());
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    return resultSet.getBlob(indexes.incrementAndGet());
  }

  @Override
  public boolean isSortable() {
    return false;
  }

  private InputStream openInputStream(final Object value) {
    if (value instanceof byte[]) {
      final byte[] bytes = (byte[])value;
      return new ByteArrayInputStream(bytes);
    } else if (value instanceof CharSequence) {
      final String string = ((CharSequence)value).toString();
      final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
      return new ByteArrayInputStream(bytes);
    } else {
      try {
        final Resource resource = Resource.getResource(value);
        return resource.newBufferedInputStream();
      } catch (final IllegalArgumentException e) {
        throw new IllegalArgumentException(value.getClass() + " not valid for a blob column");
      }
    }
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      if (value instanceof Blob) {
        final Blob blob = (Blob)value;
        statement.setBlob(parameterIndex, blob);
      } else {
        try (
          InputStream in = openInputStream(value)) {
          statement.setBinaryStream(parameterIndex, in);
        } catch (final IOException e) {
          throw Exceptions.wrap(e);
        }
      }
    }
    return parameterIndex + 1;
  }
}
