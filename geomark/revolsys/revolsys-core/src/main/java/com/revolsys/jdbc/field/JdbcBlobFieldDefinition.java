package com.revolsys.jdbc.field;

import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.jdbc.ByteArrayBlob;

import com.revolsys.jdbc.LocalBlob;
import com.revolsys.record.RecordState;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class JdbcBlobFieldDefinition extends JdbcFieldDefinition {
  public JdbcBlobFieldDefinition(final String dbName, final String name, final int sqlType,
    final int length, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(dbName, name, DataTypes.BLOB, sqlType, length, 0, required, description, properties);
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    final Blob blob = resultSet.getBlob(indexes.incrementAndGet());
    return toFieldValue(blob);
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
      Blob blob;
      if (value instanceof Blob) {
        blob = (Blob)value;
      } else if (value instanceof byte[]) {
        final byte[] bytes = (byte[])value;
        blob = new ByteArrayBlob(bytes);
      } else if (value instanceof CharSequence) {
        final String string = ((CharSequence)value).toString();
        final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        blob = new ByteArrayBlob(bytes);
      } else {
        try {
          final Resource resource = Resource.getResource(value);
          blob = new LocalBlob(resource);
        } catch (final IllegalArgumentException e) {
          throw new IllegalArgumentException(value.getClass() + " not valid for a blob column");
        }
      }
      statement.setBlob(parameterIndex, blob);
    }
    return parameterIndex + 1;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V toFieldValueException(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Blob) {
      return (V)value;
    } else {
      return (V)value;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V toFieldValueException(final RecordState state, final Object value) {
    return toFieldValueException(value);
  }
}
