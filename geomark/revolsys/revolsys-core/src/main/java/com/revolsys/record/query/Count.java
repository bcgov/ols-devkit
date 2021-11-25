package com.revolsys.record.query;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class Count extends AbstractUnaryQueryValue {
  public static Count count(final Query query, final String columnName) {
    final TableReference table = query.getTable();
    return count(table, columnName);
  }

  public static Count count(final TableReference table, final String columnName) {
    final ColumnReference column = table.getColumn(columnName);
    return new Count(column);
  }

  public static Count distinct(final Query query, final String columnName) {
    final TableReference table = query.getTable();
    return distinct(table, columnName);
  }

  public static Count distinct(final TableReference table, final String columnName) {
    return count(table, columnName).setDistinct(true);
  }

  private boolean distinct = false;

  public Count(final CharSequence name) {
    this(new Column(name));
  }

  public Count(final QueryValue queryValue) {
    super(queryValue);
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    buffer.append("count(");
    if (this.distinct) {
      buffer.append("distinct ");
    }
    super.appendDefaultSql(query, recordStore, buffer);
    buffer.append(")");
  }

  @Override
  public Count clone() {
    return (Count)super.clone();
  }

  @Override
  public int getFieldIndex() {
    final QueryValue value = getValue();
    return value.getFieldIndex();
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    final long value = resultSet.getLong(indexes.incrementAndGet());
    if (resultSet.wasNull()) {
      return null;
    } else {
      return Long.valueOf(value);
    }
  }

  public Count setDistinct(final boolean distinct) {
    this.distinct = distinct;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("COUNT(");
    if (this.distinct) {
      buffer.append("distinct ");
    }
    buffer.append(super.toString());
    buffer.append(")");
    return buffer.toString();
  }
}
