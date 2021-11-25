package com.revolsys.record.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.query.functions.F;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Property;

public class Q {
  public static BiFunction<QueryValue, QueryValue, Condition> ILIKE = ILike::new;

  public static Function<QueryValue, Condition> IS_NOT_NULL = IsNotNull::new;

  public static Function<QueryValue, Condition> IS_NULL = IsNull::new;

  public static BiFunction<QueryValue, QueryValue, Condition> EQUAL = Equal::new;

  public static BiFunction<QueryValue, QueryValue, Condition> NOT_EQUAL = NotEqual::new;

  public static BiFunction<QueryValue, QueryValue, Condition> GREATER_THAN = GreaterThan::new;

  public static BiFunction<QueryValue, QueryValue, Condition> GREATER_THAN_EQUAL = GreaterThanEqual::new;

  public static BiFunction<QueryValue, QueryValue, Condition> LESS_THAN = LessThan::new;

  public static BiFunction<QueryValue, QueryValue, Condition> LESS_THAN_EQUAL = LessThanEqual::new;

  public static BiFunction<QueryValue, QueryValue, Condition> IN = In::new;

  public static Add add(final QueryValue left, final QueryValue right) {
    return new Add(left, right);
  }

  public static And and(final Condition... conditions) {
    final List<Condition> list = Arrays.asList(conditions);
    return and(list);
  }

  public static Condition and(final Condition a, final Condition b) {
    if (a == null) {
      return b;
    } else {
      return a.and(b);
    }
  }

  public static And and(final List<? extends Condition> conditions) {
    return new And(conditions);
  }

  public static QueryValue arithmatic(final FieldDefinition field, final String operator,
    final Object value) {
    final Value queryValue = Value.newValue(field, value);
    return arithmatic((QueryValue)field, operator, queryValue);
  }

  public static QueryValue arithmatic(final QueryValue left, final String operator,
    final QueryValue right) {
    if ("+".equals(operator)) {
      return Q.add(left, right);
    } else if ("-".equals(operator)) {
      return Q.subtract(left, right);
    } else if ("*".equals(operator)) {
      return Q.multiply(left, right);
    } else if ("/".equals(operator)) {
      return Q.divide(left, right);
    } else if ("%".equals(operator) || "mod".equals(operator)) {
      return Q.mod(left, right);
    } else {
      throw new IllegalArgumentException("Operator " + operator + " not supported");
    }
  }

  public static QueryValue arithmatic(final String fieldName, final String operator,
    final Object value) {
    final Column column = new Column(fieldName);
    final Value queryValue = Value.newValue(value);
    return arithmatic(column, operator, queryValue);

  }

  public static Between between(final FieldDefinition field, final Object min, final Object max) {
    final Value minCondition = Value.newValue(field, min);
    final Value maxCondition = Value.newValue(field, max);
    return new Between(field, minCondition, maxCondition);
  }

  public static Condition binary(final FieldDefinition field, final String operator,
    final Object value) {
    final Value queryValue = Value.newValue(field, value);
    return binary((QueryValue)field, operator, queryValue);
  }

  public static BinaryCondition binary(final QueryValue left, final String operator,
    final QueryValue right) {
    if ("=".equals(operator)) {
      return Q.equal(left, right);
    } else if ("<>".equals(operator) || "!=".equals(operator)) {
      return Q.notEqual(left, right);
    } else if ("<".equals(operator)) {
      return Q.lessThan(left, right);
    } else if ("<=".equals(operator)) {
      return Q.lessThanEqual(left, right);
    } else if (">".equals(operator)) {
      return Q.greaterThan(left, right);
    } else if (">=".equals(operator)) {
      return Q.greaterThanEqual(left, right);
    } else {
      throw new IllegalArgumentException("Operator " + operator + " not supported");
    }
  }

  public static Condition binary(final String fieldName, final String operator,
    final Object value) {
    final Column column = new Column(fieldName);
    final Value queryValue = Value.newValue(value);
    return binary(column, operator, queryValue);

  }

  public static QueryValue count(final TableReference table, final String fieldName) {
    return new Count(table.getColumn(fieldName));
  }

  public static Divide divide(final QueryValue left, final QueryValue right) {
    return new Divide(left, right);
  }

  public static Equal equal(final FieldDefinition fieldDefinition, final Object value) {
    final Value valueCondition = Value.newValue(fieldDefinition, value);
    return new Equal(fieldDefinition, valueCondition);
  }

  public static Equal equal(final FieldDefinition field, final QueryValue right) {
    return new Equal(field, right);
  }

  public static Equal equal(final QueryValue left, final Object value) {
    final Value valueCondition = Value.newValue(value);
    return new Equal(left, valueCondition);
  }

  public static Equal equal(final QueryValue left, final QueryValue right) {
    return new Equal(left, right);
  }

  public static Equal equal(final String name, final Object value) {
    final Value valueCondition = Value.newValue(value);
    return equal(name, valueCondition);
  }

  public static Equal equal(final String left, final QueryValue right) {
    final Column leftCondition = new Column(left);
    return new Equal(leftCondition, right);
  }

  public static Condition equalId(final List<?> fields, final Identifier identifier) {
    final And and = new And();
    List<Object> values;
    if (identifier == null) {
      values = Arrays.asList(new Object[fields.size()]);
    } else {
      values = identifier.getValues();
    }
    if (fields.size() == values.size()) {
      for (int i = 0; i < fields.size(); i++) {
        final Object fieldKey = fields.get(i);
        Object value = values.get(i);

        Condition condition;
        if (value == null) {
          if (fieldKey instanceof FieldDefinition) {
            condition = isNull((FieldDefinition)fieldKey);
          } else {
            condition = isNull(fieldKey.toString());
          }
        } else {
          if (fieldKey instanceof FieldDefinition) {
            final FieldDefinition fieldDefinition = (FieldDefinition)fieldKey;
            value = fieldDefinition.toFieldValue(value);
            condition = equal(fieldDefinition, value);
          } else {
            condition = equal(fieldKey.toString(), value);
          }
        }
        and.and(condition);
      }
    } else {
      throw new IllegalArgumentException(
        "Field count for " + fields + " != count for values " + values);
    }
    return and;
  }

  public static GreaterThan greaterThan(final FieldDefinition fieldDefinition, final Object value) {
    final String name = fieldDefinition.getName();
    final Value valueCondition = Value.newValue(fieldDefinition, value);
    return greaterThan(name, valueCondition);
  }

  public static GreaterThan greaterThan(final QueryValue left, final QueryValue right) {
    return new GreaterThan(left, right);
  }

  public static GreaterThan greaterThan(final String name, final Object value) {
    final Value valueCondition = Value.newValue(value);
    return greaterThan(name, valueCondition);
  }

  public static GreaterThan greaterThan(final String name, final QueryValue right) {
    final Column column = new Column(name);
    return new GreaterThan(column, right);
  }

  public static GreaterThanEqual greaterThanEqual(final FieldDefinition fieldDefinition,
    final Object value) {
    final String name = fieldDefinition.getName();
    final Value valueCondition = Value.newValue(fieldDefinition, value);
    return greaterThanEqual(name, valueCondition);
  }

  public static GreaterThanEqual greaterThanEqual(final QueryValue left, final QueryValue right) {
    return new GreaterThanEqual(left, right);
  }

  public static GreaterThanEqual greaterThanEqual(final String name, final Object value) {
    final Value valueCondition = Value.newValue(value);
    return greaterThanEqual(name, valueCondition);
  }

  public static GreaterThanEqual greaterThanEqual(final String name, final QueryValue right) {
    final Column column = new Column(name);
    return greaterThanEqual(column, right);
  }

  public static ILike iLike(final FieldDefinition fieldDefinition, final Object value) {
    final String name = fieldDefinition.getName();
    final Value valueCondition = Value.newValue(fieldDefinition, value);
    return iLike(name, valueCondition);
  }

  public static ILike iLike(final QueryValue left, final Object value) {
    final Value valueCondition = Value.newValue(value);
    return new ILike(left, valueCondition);
  }

  public static ILike iLike(final String name, final Object value) {
    final Value valueCondition = Value.newValue(value);
    return iLike(name, valueCondition);
  }

  public static ILike iLike(final String left, final QueryValue right) {
    final Column leftCondition = new Column(left);
    return new ILike(leftCondition, right);
  }

  public static Condition iLike(final String left, final String right) {
    return Q.like(F.upper(new Cast(left, "varchar(4000)")), ("%" + right + "%").toUpperCase());
  }

  public static In in(final FieldDefinition fieldDefinition,
    final Collection<? extends Object> values) {
    return new In(fieldDefinition, values);
  }

  public static In in(final FieldDefinition fieldDefinition, final Object... values) {
    final List<Object> list = Arrays.asList(values);
    return new In(fieldDefinition, list);
  }

  public static In in(final String name, final Collection<? extends Object> values) {
    final Column left = new Column(name);
    final CollectionValue collectionValue = new CollectionValue(values);
    return new In(left, collectionValue);
  }

  public static IsNotNull isNotNull(final FieldDefinition fieldDefinition) {
    final String name = fieldDefinition.getName();
    return isNotNull(name);
  }

  public static IsNotNull isNotNull(final String name) {
    final Column condition = new Column(name);
    return new IsNotNull(condition);
  }

  public static IsNull isNull(final QueryValue queryValue) {
    return new IsNull(queryValue);
  }

  public static IsNull isNull(final String name) {
    final Column condition = new Column(name);
    return new IsNull(condition);
  }

  public static LessThan lessThan(final FieldDefinition fieldDefinition, final Object value) {
    final String name = fieldDefinition.getName();
    final Value valueCondition = Value.newValue(fieldDefinition, value);
    return lessThan(name, valueCondition);
  }

  public static LessThan lessThan(final QueryValue left, final QueryValue right) {
    return new LessThan(left, right);
  }

  public static LessThan lessThan(final String name, final Object value) {
    final Value valueCondition = Value.newValue(value);
    return lessThan(name, valueCondition);
  }

  public static LessThan lessThan(final String name, final QueryValue right) {
    final Column column = new Column(name);
    return lessThan(column, right);
  }

  public static LessThanEqual lessThanEqual(final FieldDefinition fieldDefinition,
    final Object value) {
    final String name = fieldDefinition.getName();
    final Value valueCondition = Value.newValue(fieldDefinition, value);
    return lessThanEqual(name, valueCondition);
  }

  public static LessThanEqual lessThanEqual(final QueryValue left, final QueryValue right) {
    return new LessThanEqual(left, right);
  }

  public static LessThanEqual lessThanEqual(final String name, final Object value) {
    final Value valueCondition = Value.newValue(value);
    return lessThanEqual(name, valueCondition);
  }

  public static LessThanEqual lessThanEqual(final String name, final QueryValue right) {
    final Column column = new Column(name);
    return new LessThanEqual(column, right);
  }

  public static Like like(final FieldDefinition fieldDefinition, final Object value) {
    final String name = fieldDefinition.getName();
    final Value valueCondition = Value.newValue(fieldDefinition, value);
    return like(name, valueCondition);
  }

  public static Like like(final QueryValue left, final Object value) {
    final Value valueCondition = Value.newValue(value);
    return new Like(left, valueCondition);
  }

  public static Like like(final String name, final Object value) {
    final Value valueCondition = Value.newValue(value);
    return like(name, valueCondition);
  }

  public static Like like(final String left, final QueryValue right) {
    final Column leftCondition = new Column(left);
    return new Like(leftCondition, right);
  }

  public static Condition likeRegEx(final RecordStore recordStore, final String fieldName,
    final Object value) {
    QueryValue left;
    if (recordStore.getClass().getName().contains("Oracle")) {
      left = F.regexpReplace(F.upper(fieldName), "[^A-Z0-9]", "");
    } else {
      left = F.regexpReplace(F.upper(fieldName), "[^A-Z0-9]", "", "g");
    }
    final String right = "%" + DataTypes.toString(value).toUpperCase().replaceAll("[^A-Z0-9]", "")
      + "%";
    return Q.like(left, right);
  }

  private static Mod mod(final QueryValue left, final QueryValue right) {
    return new Mod(left, right);
  }

  private static Multiply multiply(final QueryValue left, final QueryValue right) {
    return new Multiply(left, right);
  }

  public static Not not(final Condition condition) {
    return new Not(condition);
  }

  public static NotEqual notEqual(final FieldDefinition fieldDefinition, final Object value) {
    final String name = fieldDefinition.getName();
    final Value valueCondition = Value.newValue(fieldDefinition, value);
    return notEqual(name, valueCondition);
  }

  public static NotEqual notEqual(final QueryValue left, final QueryValue right) {
    return new NotEqual(left, right);
  }

  public static NotEqual notEqual(final String name, final Object value) {
    return notEqual(name, Value.newValue(value));
  }

  public static NotEqual notEqual(final String name, final QueryValue right) {
    final Column column = new Column(name);
    return new NotEqual(column, right);
  }

  public static Or or(final Condition... conditions) {
    final List<Condition> list = Arrays.asList(conditions);
    return or(list);
  }

  public static Condition or(final Condition a, final Condition b) {
    if (a == null) {
      return b;
    } else {
      return a.or(b);
    }
  }

  public static Or or(final List<? extends Condition> conditions) {
    return new Or(conditions);
  }

  public static void setValue(final int index, final Condition condition, final Object value) {
    setValueInternal(-1, index, condition, value);

  }

  public static int setValueInternal(int i, final int index, final QueryValue condition,
    final Object value) {
    for (final QueryValue subCondition : condition.getQueryValues()) {
      if (subCondition instanceof Value) {
        final Value valueCondition = (Value)subCondition;
        i++;
        if (i == index) {
          valueCondition.setValue(value);
          return i;
        }
        i = setValueInternal(i, index, subCondition, value);
        if (i >= index) {
          return i;
        }
      }
    }
    return i;
  }

  public static SqlCondition sql(final String sql) {
    if (Property.hasValue(sql)) {
      return new SqlCondition(sql);
    } else {
      return null;
    }
  }

  public static SqlCondition sql(final String sql, final Object... parameters) {
    return new SqlCondition(sql, parameters);
  }

  public static QueryValue sqlExpression(final String sql, final DataType dataType) {
    return new SqlExpression(sql, dataType);
  }

  private static Subtract subtract(final QueryValue left, final QueryValue right) {
    return new Subtract(left, right);
  }
}
