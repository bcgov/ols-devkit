package com.revolsys.record.query;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;

import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;

public class TableReferenceImpl implements TableReference {

  private final RecordDefinition recordDefinition;

  private final PathName tablePath;

  private final String tableAlias;

  private final String qualifiedTableName;

  public TableReferenceImpl(final PathName tablePath) {
    this(tablePath, null);
  }

  public TableReferenceImpl(final PathName tablePath, final String alias) {
    this.recordDefinition = null;
    this.tablePath = tablePath;
    this.tableAlias = alias;
    this.qualifiedTableName = JdbcUtils.getQualifiedTableName(tablePath);
  }

  public TableReferenceImpl(final RecordDefinitionProxy recordDefinition) {
    this(recordDefinition, null);
  }

  public TableReferenceImpl(final RecordDefinitionProxy recordDefinition, final String alias) {
    if (recordDefinition == null) {
      this.recordDefinition = null;
    } else {
      this.recordDefinition = recordDefinition.getRecordDefinition();
    }
    this.tablePath = this.recordDefinition.getPathName();
    this.tableAlias = alias;
    this.qualifiedTableName = this.recordDefinition.getQualifiedTableName();

  }

  @Override
  public void appendColumnPrefix(final Appendable string) {
    if (this.tableAlias != null) {
      try {
        string.append(this.tableAlias);
        string.append('.');
      } catch (final IOException e) {
        Exceptions.throwUncheckedException(e);
      }
    }
  }

  @Override
  public void appendQueryValue(final Query query, final StringBuilder sql,
    final QueryValue queryValue) {
    final RecordDefinition recordDefinition = this.recordDefinition;
    if (recordDefinition == null) {
      queryValue.appendSql(query, null, sql);
    } else {
      final RecordStore recordStore = recordDefinition.getRecordStore();
      queryValue.appendSql(query, recordStore, sql);
    }
  }

  @Override
  public void appendSelect(final Query query, final Appendable sql, final QueryValue queryValue) {
    final RecordDefinition recordDefinition = this.recordDefinition;
    if (recordDefinition == null) {
      queryValue.appendSelect(query, null, sql);
    } else {
      recordDefinition.appendSelect(query, sql, queryValue);
    }
  }

  @Override
  public void appendSelectAll(final Query query, final Appendable sql) {
    try {
      if (this.recordDefinition == null) {
        appendColumnPrefix(sql);
        sql.append('*');
      } else {
        this.recordDefinition.appendSelectAll(query, sql);
      }
    } catch (final IOException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public ColumnReference getColumn(final CharSequence name) {
    if (this.recordDefinition == null) {
      return new Column(this, name);
    } else {
      return this.recordDefinition.getColumn(name);
    }
  }

  @Override
  public List<FieldDefinition> getFields() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return Collections.emptyList();
    } else {
      return recordDefinition.getFields();
    }
  }

  @Override
  public String getQualifiedTableName() {
    return this.qualifiedTableName;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  public String getTableAlias() {
    return this.tableAlias;
  }

  @Override
  public PathName getTablePath() {
    return this.tablePath;
  }

  @Override
  public boolean hasColumn(final CharSequence name) {
    if (this.recordDefinition == null) {
      return false;
    } else {
      return this.recordDefinition.hasColumn(name);
    }
  }

  public boolean hasField(final CharSequence name) {
    if (this.recordDefinition == null) {
      return false;
    } else {
      return this.recordDefinition.hasField(name);
    }
  }

  @Override
  public String toString() {
    if (this.tableAlias == null) {
      return this.qualifiedTableName.toString();
    } else {
      return this.qualifiedTableName + " \"" + this.tableAlias + '"';
    }
  }
}
