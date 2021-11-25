package com.revolsys.gis.postgresql.type;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.CollectionDataType;
import org.jeometry.common.data.type.DataType;
import org.postgresql.jdbc.PgConnection;

import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;

public class PostgreSQLArrayFieldDefinition extends JdbcFieldDefinition {

  private final DataType elementDataType;

  private final JdbcFieldDefinition elementField;

  private final String elementDbDataType;

  public PostgreSQLArrayFieldDefinition(final String dbName, final String name,
    final CollectionDataType dataType, final String elementDbDataType, final int sqlType,
    final int length, final int scale, final boolean required, final String description,
    final JdbcFieldDefinition elementField, final Map<String, Object> properties) {
    super(dbName, name, dataType, sqlType, length, scale, required, description, properties);
    this.elementDbDataType = elementDbDataType;
    this.elementDataType = dataType.getContentType();
    this.elementField = elementField;
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    final Object value = resultSet.getObject(indexes.incrementAndGet());
    if (value instanceof Array) {
      final Array array = (Array)value;
      final List<Object> values = new ArrayList<>();
      final ResultSet arrayResultSet = array.getResultSet();
      final ColumnIndexes columnIndex = new ColumnIndexes();
      while (arrayResultSet.next()) {
        columnIndex.columnIndex = 1;
        final Object elementValue = this.elementField.getValueFromResultSet(recordDefinition,
          arrayResultSet, columnIndex, internStrings);
        values.add(elementValue);
      }
      return values;
    }
    return value;
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    Array array;
    if (value == null) {
      statement.setNull(parameterIndex, getSqlType());
      return parameterIndex + 1;
    } else if (value instanceof Array) {
      array = (Array)value;
    } else if (value instanceof Collection) {
      final Collection<?> elements = (Collection<?>)value;
      final int size = elements.size();
      final Object[] values = new Object[size];
      int i = 0;
      for (final Object element : elements) {
        values[i++] = this.elementDataType.toObject(element);
      }
      final PgConnection connection = statement.getConnection().unwrap(PgConnection.class);
      array = connection.createArrayOf(this.elementDbDataType, values);
    } else {
      final Object[] values = new Object[] {
        this.elementDataType.toObject(value)
      };
      final PgConnection connection = statement.getConnection().unwrap(PgConnection.class);
      array = connection.createArrayOf(this.elementDbDataType, values);
    }

    statement.setArray(parameterIndex, array);
    return parameterIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    return setInsertPreparedStatementValue(statement, parameterIndex, value);
  }

}
