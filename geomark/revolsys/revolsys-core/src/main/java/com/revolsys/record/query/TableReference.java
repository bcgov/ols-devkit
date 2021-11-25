package com.revolsys.record.query;

import java.io.IOException;
import java.util.List;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;

public interface TableReference extends From {
  static TableReference getTableReference(final RecordDefinitionProxy recordDefinition) {
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getRecordDefinition();
    }
  }

  default void appendColumnPrefix(final Appendable string) {
    final String alias = getTableAlias();
    if (alias != null) {
      try {
        string.append(alias);
        string.append('.');
      } catch (final IOException e) {
        Exceptions.throwUncheckedException(e);
      }
    }
  }

  @Override
  default void appendFrom(final Appendable sql) {
    final String tableName = getQualifiedTableName();
    try {
      sql.append(tableName);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  default void appendFromWithAlias(final Appendable sql) {
    final String tableAlias = getTableAlias();
    appendFromWithAlias(sql, tableAlias);
  }

  default void appendFromWithAlias(final Appendable sql, final String tableAlias) {
    try {
      appendFrom(sql);
      if (tableAlias != null) {
        sql.append(" ");
        sql.append(tableAlias);
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  void appendQueryValue(final Query query, final StringBuilder sql, final QueryValue queryValue);

  void appendSelect(final Query query, final Appendable string, final QueryValue queryValue);

  void appendSelectAll(Query query, final Appendable string);

  default QueryValue count(final String fieldName) {
    final ColumnReference field = getColumn(fieldName);
    return new Count(field);
  }

  default Condition equal(final CharSequence fieldName, final Object value) {
    final ColumnReference field = getColumn(fieldName);
    QueryValue right;
    if (value == null) {
      return new IsNull(field);
    } else if (value instanceof ColumnReference) {
      right = (ColumnReference)value;
    } else if (value instanceof QueryValue) {
      right = (QueryValue)value;
    } else {
      right = new Value(field, value);
    }
    return new Equal(field, right);
  }

  default Condition equal(final String fieldName, final TableReference toTable) {
    return equal(fieldName, toTable, fieldName);
  }

  default Condition equal(final String fromFieldName, final TableReference toTable,
    final String toFieldName) {
    final ColumnReference toColumn = toTable.getColumn(toFieldName);
    return equal(fromFieldName, toColumn);
  }

  ColumnReference getColumn(final CharSequence name);

  List<FieldDefinition> getFields();

  String getQualifiedTableName();

  RecordDefinition getRecordDefinition();

  String getTableAlias();

  PathName getTablePath();

  boolean hasColumn(CharSequence name);

  default ILike iLike(final String fieldName, final Object value) {
    final ColumnReference field = getColumn(fieldName);
    final Value valueCondition = Value.newValue(value);
    return new ILike(field, valueCondition);
  }

  default Condition in(final String fieldName, final List<?> list) {
    final ColumnReference field = getColumn(fieldName);
    final CollectionValue right = new CollectionValue(field, list);
    return new In(field, right);
  }

  default IsNotNull isNotNull(final CharSequence fieldName) {
    final ColumnReference field = getColumn(fieldName);
    return new IsNotNull(field);
  }

  default IsNull isNull(final CharSequence fieldName) {
    final ColumnReference field = getColumn(fieldName);
    return new IsNull(field);
  }
}
