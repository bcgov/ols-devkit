package com.revolsys.record.query;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class SelectAlias implements QueryValue {

  private final String alias;

  private final QueryValue value;

  public SelectAlias(final QueryValue value, final CharSequence alias) {
    this.value = value;
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
    this.value.appendDefaultSelect(query, recordStore, sql);
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
  public int appendParameters(final int index, final PreparedStatement statement) {
    return this.value.appendParameters(index, statement);
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
  public SelectAlias clone(final TableReference oldTable, final TableReference newTable) {
    if (oldTable != newTable) {
      final QueryValue clonedColumn = this.value.clone(oldTable, newTable);
      return new SelectAlias(clonedColumn, this.alias);
    }
    return (SelectAlias)clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof SelectAlias) {
      final SelectAlias alias = (SelectAlias)obj;
      if (this.value.equals(alias.value)) {
        return DataType.equal(alias.alias, this.alias);
      }
    }
    return false;
  }

  @Override
  public int getFieldIndex() {
    return this.value.getFieldIndex();
  }

  @Override
  public String getStringValue(final MapEx record) {
    return this.value.getStringValue(record);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final MapEx record) {
    return this.value.getValue(record);
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    return this.value.getValueFromResultSet(recordDefinition, resultSet, indexes, internStrings);
  }

  @Override
  public String toString() {
    final StringBuilder sql = new StringBuilder();
    this.value.appendSelect(null, null, sql);
    sql.append(" as ");
    appendAlias(sql);
    return sql.toString();
  }

}
