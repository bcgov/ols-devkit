package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class ColumnIndex implements QueryValue {

  private final int index;

  public ColumnIndex(final int index) {
    this.index = index;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder sql) {
    sql.append(this.index);
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return index;
  }

  @Override
  public void changeRecordDefinition(final RecordDefinition oldRecordDefinition,
    final RecordDefinition newRecordDefinition) {
  }

  @Override
  public ColumnIndex clone() {
    return new ColumnIndex(this.index);
  }

  @Override
  public QueryValue clone(final TableReference oldTable, final TableReference newTable) {
    return clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ColumnIndex) {
      final ColumnIndex value = (ColumnIndex)obj;
      return value.index == this.index;
    } else {
      return false;
    }
  }

  @Override
  public <V> V getValue(final MapEx record) {
    return null;
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    return null;
  }

  @Override
  public String toString() {
    return Integer.toString(this.index);
  }

}
