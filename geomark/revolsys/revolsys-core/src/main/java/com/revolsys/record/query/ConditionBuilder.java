package com.revolsys.record.query;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface ConditionBuilder {

  ConditionBuilder addCondition(Condition condition);

  default ConditionBuilder addCondition(final QueryValue left,
    final BiFunction<QueryValue, QueryValue, Condition> operator, final Object value) {
    Condition condition;
    if (value == null) {
      condition = new IsNull(left);
    } else {
      QueryValue right;
      if (value instanceof QueryValue) {
        right = (QueryValue)value;
      } else {
        right = Value.newValue(value);
      }
      condition = operator.apply(left, right);
    }
    return addCondition(condition);
  }

  default ConditionBuilder addCondition(final QueryValue left,
    final Function<QueryValue, Condition> operator) {
    final Condition condition = operator.apply(left);
    return addCondition(condition);
  }

  default ConditionBuilder addCondition(final TableReference table, final CharSequence fieldName,
    final BiFunction<QueryValue, QueryValue, Condition> operator, final Object value) {
    final ColumnReference field = table.getColumn(fieldName);
    Condition condition;
    if (value == null) {
      condition = new IsNull(field);
    } else {
      QueryValue right;
      if (value instanceof QueryValue) {
        right = (QueryValue)value;
      } else {
        right = new Value(field, value);
      }
      condition = operator.apply(field, right);
    }
    return addCondition(condition);
  }

  default ConditionBuilder addCondition(final TableReference table, final CharSequence fieldName,
    final Function<QueryValue, Condition> operator) {
    final ColumnReference field = table.getColumn(fieldName);
    return addCondition(field, operator);
  }

  default ConditionBuilder equal(final TableReference table, final CharSequence fieldName,
    final Object value) {
    return addCondition(table, fieldName, Q.EQUAL, value);
  }

  default ConditionBuilder equal(final TableReference fromTable, final String fieldName,
    final TableReference toTable) {
    return equal(fromTable, fieldName, toTable, fieldName);
  }

  default ConditionBuilder equal(final TableReference fromTable, final String fromFieldName,
    final TableReference toTable, final String toFieldName) {
    final ColumnReference fromColumn = fromTable.getColumn(fromFieldName);
    final ColumnReference toColumn = toTable.getColumn(toFieldName);
    final Condition condition = new Equal(fromColumn, toColumn);
    return addCondition(condition);
  }

  Condition getCondition();

  default ConditionBuilder iLike(final QueryValue left, final Object value) {
    return addCondition(left, Q.ILIKE, value);
  }

  default ConditionBuilder iLike(final TableReference table, final CharSequence fieldName,
    final Object value) {
    return addCondition(table, fieldName, Q.ILIKE, value);
  }

  default ConditionBuilder isNotNull(final TableReference table, final String fieldName) {
    return addCondition(table, fieldName, Q.IS_NOT_NULL);
  }

  default ConditionBuilder isNull(final TableReference table, final String fieldName) {
    return addCondition(table, fieldName, Q.IS_NULL);
  }

  default ConditionBuilder sql(final String sql, final Object... parameters) {
    final Condition condition = Q.sql(sql, parameters);
    return addCondition(condition);
  }
}
