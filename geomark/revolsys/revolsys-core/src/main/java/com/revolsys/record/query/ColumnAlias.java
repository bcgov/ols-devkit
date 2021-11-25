package com.revolsys.record.query;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class ColumnAlias implements QueryValue, ColumnReference {

  private final String alias;

  private final ColumnReference column;

  public ColumnAlias(final ColumnReference column, final CharSequence alias) {
    this.column = column;
    this.alias = alias.toString();
  }

  protected void appendAlias(final Appendable sql) {
    try {
      sql.append('"');
      sql.append(this.alias);
      sql.append('"');
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void appendDefaultSelect(final Query query, final RecordStore recordStore,
    final Appendable sql) {
    this.column.appendDefaultSelect(query, recordStore, sql);
    try {
      sql.append(" as ");
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
    appendAlias(sql);
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder sql) {
    sql.append(this.alias);
  }

  @Override
  public void appendName(final Appendable string) {
    this.column.appendName(string);
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return index;
  }

  @Override
  public ColumnReference clone() {
    try {
      return (ColumnReference)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public ColumnReference clone(final TableReference oldTable, final TableReference newTable) {
    if (oldTable != newTable) {
      final ColumnReference clonedColumn = this.column.clone(oldTable, newTable);
      return new ColumnAlias(clonedColumn, this.alias);
    }
    return clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ColumnAlias) {
      final ColumnAlias alias = (ColumnAlias)obj;
      if (this.column.equals(alias.column)) {
        return DataType.equal(alias.getName(), this.getName());
      }
    }
    return false;
  }

  @Override
  public String getAliasName() {
    return this.alias;
  }

  @Override
  public FieldDefinition getFieldDefinition() {
    return this.column.getFieldDefinition();
  }

  @Override
  public int getFieldIndex() {
    return this.column.getFieldIndex();
  }

  @Override
  public String getName() {
    return this.alias;
  }

  @Override
  public String getStringValue(final MapEx record) {
    final Object value = getValue(record);
    return this.column.toString(value);
  }

  @Override
  public TableReference getTable() {
    return this.column.getTable();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final MapEx record) {
    if (record == null) {
      return null;
    } else {
      final String name = getName();
      return (V)record.getValue(name);
    }
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    return this.column.getValueFromResultSet(recordDefinition, resultSet, indexes, internStrings);
  }

  @Override
  public <V> V toColumnTypeException(final Object value) {
    if (value == null) {
      return null;
    } else {
      return this.column.toColumnTypeException(value);
    }
  }

  @Override
  public <V> V toFieldValueException(final Object value) {
    if (value == null) {
      return null;
    } else {
      return this.column.toFieldValueException(value);
    }
  }

  @Override
  public <V> V toFieldValueException(final RecordState state, final Object value) {
    if (value == null) {
      return null;
    } else {
      return this.column.toFieldValueException(state, value);
    }
  }

  @Override
  public String toString() {
    final StringBuilder sql = new StringBuilder();
    this.column.appendName(sql);
    sql.append(" as ");
    appendAlias(sql);
    return sql.toString();
  }

  @Override
  public String toString(final Object value) {
    return this.column.toString(value);
  }
}
