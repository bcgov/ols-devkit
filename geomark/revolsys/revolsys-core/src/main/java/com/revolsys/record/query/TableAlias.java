package com.revolsys.record.query;

import java.util.List;

import org.jeometry.common.io.PathName;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class TableAlias implements TableReference {

  private final String alias;

  private final TableReference table;

  public TableAlias(final TableReference table, final String alias) {
    super();
    this.table = table;
    this.alias = alias;
  }

  @Override
  public void appendQueryValue(final Query query, final StringBuilder sql,
    final QueryValue queryValue) {
    this.table.appendQueryValue(query, sql, queryValue);
  }

  @Override
  public void appendSelect(final Query query, final Appendable string,
    final QueryValue queryValue) {
    // TODO Auto-generated method stub

  }

  @Override
  public void appendSelectAll(final Query query, final Appendable string) {
    // TODO Auto-generated method stub

  }

  @Override
  public ColumnReference getColumn(final CharSequence name) {
    final ColumnReference column = this.table.getColumn(name);
    return column;
  }

  @Override
  public List<FieldDefinition> getFields() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getQualifiedTableName() {
    return this.table.getQualifiedTableName();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.table.getRecordDefinition();
  }

  @Override
  public String getTableAlias() {
    return this.alias;
  }

  @Override
  public PathName getTablePath() {
    return this.table.getTablePath();
  }

  @Override
  public boolean hasColumn(final CharSequence name) {
    return this.table.hasColumn(name);
  }

}
