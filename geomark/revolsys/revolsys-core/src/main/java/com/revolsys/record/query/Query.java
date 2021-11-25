package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jeometry.common.io.PathName;
import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.predicate.Predicates;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.ArrayChangeTrackRecord;
import com.revolsys.record.ChangeTrackRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.Records;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.LockMode;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.TableRecordStoreConnection;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.TransactionOptions;
import com.revolsys.transaction.Transactionable;
import com.revolsys.util.Cancellable;
import com.revolsys.util.CancellableProxy;
import com.revolsys.util.Property;
import com.revolsys.util.count.LabelCounters;

public class Query extends BaseObjectWithProperties
  implements Cloneable, CancellableProxy, Transactionable {

  private static void addFilter(final Query query, final RecordDefinition recordDefinition,
    final Map<String, ?> filter, final AbstractMultiCondition multipleCondition) {
    if (filter != null && !filter.isEmpty()) {
      for (final Entry<String, ?> entry : filter.entrySet()) {
        final String name = entry.getKey();
        final FieldDefinition fieldDefinition = recordDefinition.getField(name);
        if (fieldDefinition == null) {
          final Object value = entry.getValue();
          if (value == null) {
            multipleCondition.addCondition(Q.isNull(name));
          } else if (value instanceof Collection) {
            final Collection<?> values = (Collection<?>)value;
            multipleCondition.addCondition(new In(name, values));
          } else {
            multipleCondition.addCondition(Q.equal(name, value));
          }
        } else {
          final Object value = entry.getValue();
          if (value == null) {
            multipleCondition.addCondition(Q.isNull(name));
          } else if (value instanceof Collection) {
            final Collection<?> values = (Collection<?>)value;
            multipleCondition.addCondition(new In(fieldDefinition, values));
          } else {
            multipleCondition.addCondition(Q.equal(fieldDefinition, value));
          }
        }
      }
      query.setWhereCondition(multipleCondition);
    }
  }

  public static Query and(final RecordDefinition recordDefinition, final Map<String, ?> filter) {
    final Query query = new Query(recordDefinition);
    final And and = new And();
    addFilter(query, recordDefinition, filter, and);
    return query;
  }

  public static Query equal(final FieldDefinition field, final Object value) {
    final RecordDefinition recordDefinition = field.getRecordDefinition();
    final Query query = new Query(recordDefinition);
    final Value valueCondition = Value.newValue(field, value);
    final BinaryCondition equal = Q.equal(field, valueCondition);
    query.setWhereCondition(equal);
    return query;
  }

  public static Query equal(final RecordDefinitionProxy recordDefinition, final String name,
    final Object value) {
    final FieldDefinition fieldDefinition = recordDefinition.getFieldDefinition(name);
    if (fieldDefinition == null) {
      return null;
    } else {
      final Query query = Query.newQuery(recordDefinition);
      final Value valueCondition = Value.newValue(fieldDefinition, value);
      final BinaryCondition equal = Q.equal(name, valueCondition);
      query.setWhereCondition(equal);
      return query;
    }
  }

  public static Query intersects(final RecordDefinition recordDefinition,
    final BoundingBox boundingBox) {
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    if (geometryField == null) {
      return null;
    } else {
      final Query query = recordDefinition.newQuery();
      F.envelopeIntersects(query, boundingBox);
      return query;
    }

  }

  public static Query intersects(final RecordStore recordStore, final PathName path,
    final BoundingBox boundingBox) {
    final RecordDefinition recordDefinition = recordStore.getRecordDefinition(path);
    return intersects(recordDefinition, boundingBox);
  }

  public static Query newQuery(final RecordDefinitionProxy recordDefinition) {
    return newQuery(recordDefinition, null);
  }

  public static Query newQuery(final RecordDefinitionProxy recordDefinition,
    final Condition whereCondition) {
    final TableReference table = TableReference.getTableReference(recordDefinition);
    return new Query(table, whereCondition);
  }

  public static Query or(final RecordDefinition recordDefinition, final Map<String, ?> filter) {
    final Query query = new Query(recordDefinition);
    final Or or = new Or();
    addFilter(query, recordDefinition, filter, or);
    return query;
  }

  public static Query orderBy(final PathName pathName, final String... orderBy) {
    final Query query = new Query(pathName);
    query.setOrderByFieldNames(orderBy);
    return query;
  }

  public static Query where(
    final BiFunction<FieldDefinition, Object, BinaryCondition> whereFunction,
    final FieldDefinition field, final Object value) {
    final RecordDefinition recordDefinition = field.getRecordDefinition();
    final Query query = new Query(recordDefinition);
    final Value valueCondition = Value.newValue(field, value);
    final BinaryCondition equal = whereFunction.apply(field, valueCondition);
    query.setWhereCondition(equal);
    return query;
  }

  private List<Join> joins = new ArrayList<>();

  private boolean distinct = false;

  private Cancellable cancellable;

  private RecordFactory<Record> recordFactory;

  private List<QueryValue> selectExpressions = new ArrayList<>();

  private final List<QueryValue> groupBy = new ArrayList<>();

  private From from;

  private int limit = Integer.MAX_VALUE;

  private LockMode lockMode = LockMode.NONE;

  private int offset = 0;

  private Map<QueryValue, Boolean> orderBy = new LinkedHashMap<>();

  private List<Object> parameters = new ArrayList<>();

  private TableReference table;

  private String sql;

  private LabelCounters labelCountMap;

  private Condition whereCondition = Condition.ALL;

  public Query() {
    this("/Record");
  }

  public Query(final PathName typePath) {
    this(typePath, null);
  }

  public Query(final PathName typePath, final Condition whereCondition) {
    this(new TableReferenceImpl(typePath), whereCondition);
  }

  public Query(final String typePath) {
    this(typePath, null);
  }

  public Query(final String typePath, final Condition whereCondition) {
    this(PathName.newPathName(typePath), whereCondition);
  }

  public Query(final TableReference table) {
    this.table = table;
  }

  public Query(final TableReference table, final Condition whereCondition) {
    this.table = table;
    setWhereCondition(whereCondition);
  }

  public Query addGroupBy(final Object groupByItem) {
    if (groupByItem instanceof QueryValue) {
      final QueryValue queryValue = (QueryValue)groupByItem;
      this.groupBy.add(queryValue);
    } else if (groupByItem instanceof CharSequence) {
      final CharSequence fieldName = (CharSequence)groupByItem;
      final ColumnReference column = this.table.getColumn(fieldName);
      this.groupBy.add(column);
    } else if (groupByItem instanceof Integer) {
      final Integer index = (Integer)groupByItem;
      final ColumnIndex columnIndex = new ColumnIndex(index);
      this.groupBy.add(columnIndex);
    } else {
      throw new IllegalArgumentException(groupByItem.toString());
    }
    return this;
  }

  public Query addJoin(final Join join) {
    this.joins.add(join);
    return this;
  }

  public Query addOrderBy(final CharSequence field) {
    return addOrderBy(field, true);
  }

  public Query addOrderBy(final Map<?, Boolean> orderBy) {
    for (final Entry<?, Boolean> entry : orderBy.entrySet()) {
      final Object field = entry.getKey();
      final Boolean ascending = entry.getValue();
      addOrderBy(field, ascending);
    }
    return this;
  }

  public Query addOrderBy(final Object field) {
    return addOrderBy(field, true);
  }

  public Query addOrderBy(final Object field, final boolean ascending) {
    QueryValue queryValue;
    if (field instanceof QueryValue) {
      queryValue = (QueryValue)field;
    } else if (field instanceof CharSequence) {
      final CharSequence name = (CharSequence)field;
      try {
        queryValue = this.table.getColumn(name);
      } catch (final IllegalArgumentException e) {
        queryValue = new Column(name);
      }
    } else if (field instanceof Integer) {
      final Integer index = (Integer)field;
      queryValue = new ColumnIndex(index);
    } else {
      throw new IllegalArgumentException("Not a field name: " + field);
    }

    if (!this.orderBy.containsKey(queryValue)) {
      this.orderBy.put(queryValue, ascending);
    }
    return this;
  }

  public Query addOrderById() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition != null) {
      for (final FieldDefinition idField : recordDefinition.getIdFields()) {
        addOrderBy(idField);
      }
    }
    return this;
  }

  @Deprecated
  public Query addParameter(final Object value) {
    this.parameters.add(value);
    return this;
  }

  public Query and(final ColumnReference left, final Object value) {
    Condition condition;
    if (value == null) {
      condition = new IsNull(left);
    } else {
      QueryValue right;
      if (value instanceof QueryValue) {
        right = (QueryValue)value;
      } else {
        right = new Value(left, value);
      }
      condition = new Equal(left, right);
    }
    return and(condition);
  }

  public Query and(final Condition condition) {
    if (condition != null && !condition.isEmpty()) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      condition.changeRecordDefinition(recordDefinition, recordDefinition);
      this.whereCondition = this.whereCondition.and(condition);
    }
    return this;
  }

  public Query and(final Condition... conditions) {
    if (conditions != null) {
      Condition whereCondition = getWhereCondition();
      for (final Condition condition : conditions) {
        if (Property.hasValue(condition)) {
          whereCondition = whereCondition.and(condition);
        }
      }
      setWhereCondition(whereCondition);
    }
    return this;
  }

  public Query and(final Iterable<? extends Condition> conditions) {
    if (conditions != null) {
      Condition whereCondition = getWhereCondition();
      for (final Condition condition : conditions) {
        if (Property.hasValue(condition)) {
          whereCondition = whereCondition.and(condition);
        }
      }
      setWhereCondition(whereCondition);
    }
    return this;
  }

  public Query and(final String fieldName,
    final BiFunction<QueryValue, QueryValue, Condition> operator, final Object value) {
    final Condition condition = newCondition(fieldName, operator, value);
    return and(condition);
  }

  public Query and(final String fieldName,
    final java.util.function.Function<QueryValue, Condition> operator) {
    final Condition condition = newCondition(fieldName, operator);
    return and(condition);
  }

  public Query and(final String fieldName, final Object value) {
    final ColumnReference left = this.table.getColumn(fieldName);
    Condition condition;
    if (value == null) {
      condition = new IsNull(left);
    } else {
      QueryValue right;
      if (value instanceof QueryValue) {
        right = (QueryValue)value;
      } else {
        right = new Value(left, value);
      }
      condition = new Equal(left, right);
    }
    return and(condition);
  }

  public Query andEqualId(final Object id) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final String idFieldName = recordDefinition.getIdFieldName();
    return and(idFieldName, Q.EQUAL, id);
  }

  /**
   * Create an Or from the conditions and and it to this query;
   * @param conditions
   * @return
   */
  public Query andOr(final Condition... conditions) {
    if (conditions != null && conditions.length > 0) {
      final Or or = new Or(conditions);
      if (!or.isEmpty()) {
        and(or);
      }
    }
    return this;
  }

  public void appendSelect(final StringBuilder sql) {
    final TableReference table = this.table;
    final List<QueryValue> select = getSelect();
    if (select.isEmpty()) {
      table.appendSelectAll(this, sql);
    } else {
      boolean first = true;
      for (final QueryValue selectItem : select) {
        if (first) {
          first = false;
        } else {
          sql.append(", ");
        }
        table.appendSelect(this, sql, selectItem);
      }
    }
  }

  public int appendSelectParameters(int index, final PreparedStatement statement) {
    for (final QueryValue select : this.selectExpressions) {
      index = select.appendParameters(index, statement);
    }
    return index;
  }

  public void clearOrderBy() {
    this.orderBy.clear();
  }

  @Override
  public Query clone() {
    final Query clone = (Query)super.clone();
    clone.table = this.table;
    clone.selectExpressions = new ArrayList<>(clone.selectExpressions);
    clone.joins = new ArrayList<>(clone.joins);
    clone.selectExpressions = new ArrayList<>(clone.selectExpressions);
    clone.parameters = new ArrayList<>(this.parameters);
    clone.orderBy = new HashMap<>(this.orderBy);
    if (this.whereCondition != null) {
      clone.whereCondition = this.whereCondition.clone();
    }
    if (!clone.getSelect().isEmpty() || clone.whereCondition != null) {
      clone.sql = null;
    }
    return clone;
  }

  public Query clone(final TableReference oldTable, final TableReference newTable) {
    final Query clone = (Query)super.clone();
    clone.table = this.table;
    clone.selectExpressions = QueryValue.cloneQueryValues(oldTable, newTable,
      clone.selectExpressions);
    clone.joins = QueryValue.cloneQueryValues(oldTable, newTable, this.joins);
    clone.parameters = new ArrayList<>(this.parameters);
    clone.orderBy = new HashMap<>(this.orderBy);
    if (this.whereCondition != null) {
      clone.whereCondition = this.whereCondition.clone(oldTable, newTable);
    }
    if (!clone.getSelect().isEmpty() || clone.whereCondition != null) {
      clone.sql = null;
    }
    return clone;
  }

  public int deleteRecords(final TableRecordStoreConnection connection, final Query query) {
    return getRecordDefinition().getRecordStore().deleteRecords(query);
  }

  @SuppressWarnings("unchecked")
  public <R extends MapEx> void forEachRecord(final Iterable<R> records,
    final Consumer<? super R> consumer) {
    final Map<QueryValue, Boolean> orderBy = getOrderBy();
    final Predicate<R> filter = (Predicate<R>)getWhereCondition();
    if (orderBy.isEmpty()) {
      if (filter == null) {
        records.forEach(consumer);
      } else {
        records.forEach(record -> {
          if (filter.test(record)) {
            consumer.accept(record);
          }
        });
      }
    } else {
      final Comparator<R> comparator = Records.newComparatorOrderBy(orderBy);
      final List<R> results = Predicates.filter(records, filter);
      results.sort(comparator);
      results.forEach(consumer);
    }
  }

  @Override
  public Cancellable getCancellable() {
    return this.cancellable;
  }

  public From getFrom() {
    return this.from;
  }

  public FieldDefinition getGeometryField() {
    return getRecordDefinition().getGeometryField();
  }

  public List<QueryValue> getGroupBy() {
    return this.groupBy;
  }

  public List<Join> getJoins() {
    return this.joins;
  }

  public int getLimit() {
    return this.limit;
  }

  public LockMode getLockMode() {
    return this.lockMode;
  }

  public int getOffset() {
    return this.offset;
  }

  public Map<QueryValue, Boolean> getOrderBy() {
    return this.orderBy;
  }

  public List<Object> getParameters() {
    return this.parameters;
  }

  public String getQualifiedTableName() {
    if (this.table == null) {
      return null;
    } else {
      return this.table.getQualifiedTableName();
    }
  }

  @SuppressWarnings("unchecked")
  public <R extends Record> R getRecord() {
    return (R)getRecordDefinition().getRecord(this);
  }

  public long getRecordCount() {
    return getRecordDefinition().getRecordStore().getRecordCount(this);
  }

  public RecordDefinition getRecordDefinition() {
    return this.table.getRecordDefinition();
  }

  @SuppressWarnings("unchecked")
  public <V extends Record> RecordFactory<V> getRecordFactory() {
    return (RecordFactory<V>)this.recordFactory;
  }

  public RecordReader getRecordReader() {
    return getRecordDefinition().getRecordStore().getRecords(this);
  }

  public RecordReader getRecordReader(final Transaction transaction) {
    return getRecordDefinition().getRecordStore().getRecords(this);
  }

  public List<Record> getRecords() {
    try (
      RecordReader records = getRecordReader()) {
      return records.toList();
    }
  }

  public List<QueryValue> getSelect() {
    return this.selectExpressions;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public List<QueryValue> getSelectExpressions() {
    if (this.selectExpressions.isEmpty() && this.table != null) {
      final RecordDefinition recordDefinition = this.table.getRecordDefinition();
      if (recordDefinition != null) {
        return (List)recordDefinition.getFieldDefinitions();
      }
    }
    return this.selectExpressions;
  }

  public String getSelectSql() {
    String sql = getSql();
    final Map<QueryValue, Boolean> orderBy = getOrderBy();
    final TableReference table = getTable();
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (sql == null) {
      sql = newSelectSql(orderBy, table);
    } else {
      if (sql.toUpperCase().startsWith("SELECT * FROM ")) {
        final StringBuilder newSql = new StringBuilder("SELECT ");
        if (recordDefinition == null) {
          newSql.append("*");
        } else {
          recordDefinition.appendSelectAll(this, newSql);
        }
        newSql.append(" FROM ");
        newSql.append(sql.substring(14));
        sql = newSql.toString();
      }
      if (!orderBy.isEmpty()) {
        final StringBuilder buffer = new StringBuilder(sql);
        JdbcUtils.addOrderBy(this, buffer, table, orderBy);
        sql = buffer.toString();
      }
    }
    return sql;
  }

  public String getSql() {
    return this.sql;
  }

  public LabelCounters getStatistics() {
    return this.labelCountMap;
  }

  public TableReference getTable() {
    return this.table;
  }

  public PathName getTablePath() {
    return this.table.getTablePath();
  }

  @Override
  public PlatformTransactionManager getTransactionManager() {
    return getRecordDefinition().getRecordStore().getTransactionManager();
  }

  public String getWhere() {
    return this.whereCondition.toFormattedString();
  }

  public Condition getWhereCondition() {
    return this.whereCondition;
  }

  public Query groupBy(final Object... groupBy) {
    this.groupBy.clear();
    if (groupBy != null) {
      for (final Object groupByItem : groupBy) {
        addGroupBy(groupByItem);
      }
    }
    return this;
  }

  public boolean hasOrderBy(final Object fieldName) {
    return this.orderBy.containsKey(fieldName);
  }

  public boolean hasSelect() {
    return !this.selectExpressions.isEmpty();
  }

  public Record insertOrUpdateRecord(final Supplier<Record> newRecordSupplier,
    final Consumer<Record> updateAction) {

    final Record record = getRecord();
    if (record == null) {
      final Record newRecord = newRecordSupplier.get();
      if (newRecord == null) {
        return null;
      } else {
        getRecordDefinition().getRecordStore().insertRecord(newRecord);
        return newRecord;
      }
    } else {
      updateAction.accept(record);
      getRecordDefinition().getRecordStore().updateRecord(record);
      return record;
    }
  }

  public Record insertRecord(final Supplier<Record> newRecordSupplier) {
    final ChangeTrackRecord changeTrackRecord = getRecord();
    if (changeTrackRecord == null) {
      final Record newRecord = newRecordSupplier.get();
      if (newRecord == null) {
        return null;
      } else {
        getRecordDefinition().getRecordStore().insertRecord(newRecord);
        return newRecord;
      }
    } else {
      return changeTrackRecord.newRecord();
    }
  }

  public boolean isCustomResult() {
    if (!getJoins().isEmpty()) {
      return true;
    } else if (!getGroupBy().isEmpty()) {
      return true;
    } else if (this.selectExpressions.isEmpty()) {
      return false;
    } else if (this.selectExpressions.size() == 1
      && this.selectExpressions.get(0) instanceof AllColumns) {
      return false;
    } else {
      return true;
    }
  }

  public boolean isDistinct() {
    return this.distinct;
  }

  public boolean isSelectEmpty() {
    return this.selectExpressions.isEmpty();
  }

  public Join join(final TableReference table) {
    final Join join = JoinType.JOIN.build(table);
    this.joins.add(join);
    return join;

  }

  public Condition newCondition(final CharSequence fieldName,
    final BiFunction<QueryValue, QueryValue, Condition> operator, final Object value) {
    final ColumnReference left = this.table.getColumn(fieldName);
    Condition condition;
    if (value == null) {
      condition = new IsNull(left);
    } else {
      QueryValue right;
      if (value instanceof QueryValue) {
        right = (QueryValue)value;
      } else {
        right = new Value(left, value);
      }
      condition = operator.apply(left, right);
    }
    return condition;
  }

  public Condition newCondition(final CharSequence fieldName,
    final java.util.function.Function<QueryValue, Condition> operator) {
    final ColumnReference column = this.table.getColumn(fieldName);
    final Condition condition = operator.apply(column);
    return condition;
  }

  public Condition newCondition(final QueryValue left,
    final BiFunction<QueryValue, QueryValue, Condition> operator, final Object value) {
    Condition condition;
    if (value == null) {
      condition = new IsNull(left);
    } else {
      QueryValue right;
      if (value instanceof QueryValue) {
        right = (QueryValue)value;
      } else {
        if (left instanceof ColumnReference) {
          right = new Value((ColumnReference)left, value);
        } else {
          right = Value.newValue(value);
        }
      }
      condition = operator.apply(left, right);
    }
    return condition;
  }

  public String newDeleteSql() {
    final StringBuilder sql = new StringBuilder();
    sql.append("DELETE FROM ");
    From from = getFrom();
    if (from == null) {
      from = this.table;
    }
    from.appendFromWithAlias(sql);
    JdbcUtils.appendWhere(sql, this);
    return sql.toString();
  }

  public Query newQuery(final RecordDefinition recordDefinition) {
    final Query query = clone();
    query.setRecordDefinition(recordDefinition);
    return query;
  }

  public <QV extends QueryValue> QV newQueryValue(final CharSequence fieldName,
    final BiFunction<QueryValue, QueryValue, QV> operator, final Object value) {
    final ColumnReference left = this.table.getColumn(fieldName);
    QueryValue right;
    if (value instanceof QueryValue) {
      right = (QueryValue)value;
    } else {
      right = new Value(left, value);
    }
    return operator.apply(left, right);
  }

  public <QV extends QueryValue> QV newQueryValue(final CharSequence fieldName,
    final java.util.function.Function<QueryValue, QV> operator) {
    final ColumnReference column = this.table.getColumn(fieldName);
    return operator.apply(column);
  }

  public String newSelectSql(final Map<QueryValue, Boolean> orderBy, final TableReference table) {

    From from = getFrom();
    if (from == null) {
      from = table;
    }
    final List<Join> joins = getJoins();
    final LockMode lockMode = getLockMode();
    final boolean distinct = isDistinct();
    final List<QueryValue> groupBy = getGroupBy();
    final StringBuilder sql = new StringBuilder();
    sql.append("SELECT ");
    if (distinct) {
      sql.append("DISTINCT ");
    }
    appendSelect(sql);
    sql.append(" FROM ");
    from.appendFromWithAlias(sql);
    for (final Join join : joins) {
      JdbcUtils.appendQueryValue(sql, this, join);
    }
    JdbcUtils.appendWhere(sql, this);

    if (groupBy != null) {
      boolean hasGroupBy = false;
      for (final QueryValue groupByItem : groupBy) {
        if (hasGroupBy) {
          sql.append(", ");
        } else {
          sql.append(" GROUP BY ");
          hasGroupBy = true;
        }
        table.appendQueryValue(this, sql, groupByItem);
      }
    }

    JdbcUtils.addOrderBy(this, sql, table, orderBy);

    lockMode.append(sql);
    return sql.toString();
  }

  public Query or(final CharSequence fieldName,
    final BiFunction<QueryValue, QueryValue, Condition> operator, final Object value) {
    final ColumnReference left = this.table.getColumn(fieldName);
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
    return or(condition);
  }

  public Query or(final CharSequence fieldName,
    final java.util.function.Function<QueryValue, Condition> operator) {
    final Condition condition = newCondition(fieldName, operator);
    return or(condition);
  }

  public Query or(final Condition condition) {
    final Condition whereCondition = getWhereCondition();
    if (whereCondition.isEmpty()) {
      setWhereCondition(condition);
    } else if (whereCondition instanceof Or) {
      final Or or = (Or)whereCondition;
      or.or(condition);
    } else {
      setWhereCondition(new Or(whereCondition, condition));
    }
    return this;
  }

  public Query orderBy(final Object... orderBy) {
    this.orderBy.clear();
    for (final Object orderByItem : orderBy) {
      addOrderBy(orderByItem);
    }
    return this;
  }

  public void removeSelect(final String name) {
    for (final Iterator<QueryValue> iterator = this.selectExpressions.iterator(); iterator
      .hasNext();) {
      final QueryValue queryValue = iterator.next();
      if (queryValue instanceof Column) {
        final Column column = (Column)queryValue;
        if (column.getName().equals(name)) {
          iterator.remove();
        }

      }

    }
  }

  public Query select(final Collection<?> selectExpressions) {
    this.selectExpressions.clear();
    for (final Object selectExpression : selectExpressions) {
      select(selectExpression);
    }
    return this;
  }

  public Query select(final Object select) {
    QueryValue selectExpression;
    if (select instanceof QueryValue) {
      selectExpression = (QueryValue)select;
    } else if (select instanceof CharSequence) {
      final CharSequence name = (CharSequence)select;
      selectExpression = this.table.getColumn(name);
    } else {
      throw new IllegalArgumentException("Not a valid select expression :" + select);
    }
    this.selectExpressions.add(selectExpression);
    return this;
  }

  public Query select(final Object... select) {
    this.selectExpressions.clear();
    for (final Object selectItem : select) {
      select(selectItem);
    }
    return this;
  }

  public Query select(final TableReference table, final String fieldName) {
    final ColumnReference column = table.getColumn(fieldName);
    this.selectExpressions.add(column);
    return this;
  }

  public Query select(final TableReference table, final String... fieldNames) {
    for (final String fieldName : fieldNames) {
      final ColumnReference column = table.getColumn(fieldName);
      this.selectExpressions.add(column);
    }
    return this;
  }

  public Query selectAlias(final ColumnReference column, final String alias) {
    final ColumnAlias columnAlias = new ColumnAlias(column, alias);
    this.selectExpressions.add(columnAlias);
    return this;
  }

  public Query selectAlias(final QueryValue value, final String alias) {
    final SelectAlias columnAlias = new SelectAlias(value, alias);
    this.selectExpressions.add(columnAlias);
    return this;
  }

  public Query selectAlias(final String name, final String alias) {
    final ColumnReference column = this.table.getColumn(name);
    return selectAlias(column, alias);
  }

  public Query selectCsv(final String select) {
    if (Property.hasValue(select)) {
      for (String selectItem : select.split(",")) {
        selectItem = selectItem.trim();
        select(selectItem);
      }
    }
    return this;
  }

  public Query setCancellable(final Cancellable cancellable) {
    this.cancellable = cancellable;
    return this;
  }

  public Query setDistinct(final boolean distinct) {
    this.distinct = distinct;
    return this;
  }

  public Query setFrom(final From from, final String alias) {
    this.from = new FromAlias(from, alias);
    return this;
  }

  public Query setFrom(final String from) {
    this.from = new FromSql(from);
    return this;
  }

  public Query setFrom(final String from, final String alias) {
    final FromSql fromSql = new FromSql(from);
    this.from = new FromAlias(fromSql, alias);
    return this;
  }

  public Query setGroupBy(final List<?> groupBy) {
    this.groupBy.clear();
    if (groupBy != null) {
      for (final Object groupByItem : groupBy) {
        addGroupBy(groupByItem);

      }
    }
    return this;
  }

  public Query setGroupBy(final String... fieldNames) {
    final List<String> groupBy = Arrays.asList(fieldNames);
    return setGroupBy(groupBy);
  }

  public Query setLimit(final int limit) {
    if (limit < 0) {
      this.limit = Integer.MAX_VALUE;
    } else {
      this.limit = limit;
    }
    return this;
  }

  public Query setLockMode(final LockMode lockMode) {
    if (lockMode == null) {
      this.lockMode = LockMode.NONE;
    } else {
      this.lockMode = lockMode;
    }
    return this;
  }

  public Query setOffset(final int offset) {
    if (offset > 0) {
      this.offset = offset;
    }
    return this;
  }

  public Query setOrderBy(final CharSequence field) {
    this.orderBy.clear();
    return addOrderBy(field);
  }

  public Query setOrderBy(final Map<?, Boolean> orderBy) {
    if (orderBy != this.orderBy) {
      this.orderBy.clear();
      if (orderBy != null) {
        for (final Entry<?, Boolean> entry : orderBy.entrySet()) {
          final Object field = entry.getKey();
          final Boolean ascending = entry.getValue();
          addOrderBy(field, ascending);
        }
      }
    }
    return this;
  }

  public Query setOrderByFieldNames(final List<? extends CharSequence> orderBy) {
    this.orderBy.clear();
    for (final CharSequence field : orderBy) {
      addOrderBy(field);
    }
    return this;
  }

  public Query setOrderByFieldNames(final String... orderBy) {
    this.orderBy.clear();
    for (final CharSequence field : orderBy) {
      addOrderBy(field);
    }
    return this;
  }

  public Query setRecordDefinition(final RecordDefinition recordDefinition) {
    this.table = recordDefinition;
    if (this.whereCondition != null) {
      this.whereCondition.changeRecordDefinition(getRecordDefinition(), recordDefinition);
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public Query setRecordFactory(final RecordFactory<?> recordFactory) {
    this.recordFactory = (RecordFactory<Record>)recordFactory;
    return this;
  }

  public Query setSelect(final Collection<?> selectExpressions) {
    this.selectExpressions.clear();
    for (final Object selectExpression : selectExpressions) {
      select(selectExpression);
    }
    return this;
  }

  public Query setSelect(final Object... selectExpressions) {
    this.selectExpressions.clear();
    for (final Object selectExpression : selectExpressions) {
      select(selectExpression);
    }
    return this;
  }

  public Query setSelect(final TableReference table, final String... fieldNames) {
    this.selectExpressions.clear();
    return select(table, fieldNames);
  }

  public Query setSql(final String sql) {
    this.sql = sql;
    return this;
  }

  public Query setStatistics(final LabelCounters labelCountMap) {
    this.labelCountMap = labelCountMap;
    return this;
  }

  public Query setWhere(final String where) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final Condition whereCondition = QueryValue.parseWhere(recordDefinition, where);
    return setWhereCondition(whereCondition);
  }

  public Query setWhereCondition(final Condition whereCondition) {
    if (whereCondition == null) {
      this.whereCondition = Condition.ALL;
    } else {
      this.whereCondition = whereCondition;
      final RecordDefinition recordDefinition = getRecordDefinition();
      if (recordDefinition != null) {
        whereCondition.changeRecordDefinition(recordDefinition, recordDefinition);
      }
    }
    return this;
  }

  public <V extends Record> void sort(final List<V> records) {
    final Map<QueryValue, Boolean> orderBy = getOrderBy();
    if (Property.hasValue(orderBy)) {
      final Comparator<Record> comparator = Records.newComparatorOrderBy(orderBy);
      records.sort(comparator);
    }
  }

  @Override
  public String toString() {
    try {
      final StringBuilder string = new StringBuilder();
      if (this.sql == null) {
        final String sql = getSelectSql();
        string.append(sql);
      } else {
        string.append(this.sql);
      }
      if (!this.parameters.isEmpty()) {
        string.append(" ");
        string.append(this.parameters);
      }
      return string.toString();
    } catch (final Throwable t) {
      t.printStackTrace();
      return "";
    }
  }

  public Record updateRecord(final Consumer<Record> updateAction) {
    final Record record = getRecord();
    if (record == null) {
      return null;
    } else {
      updateAction.accept(record);
      getRecordDefinition().getRecordStore().updateRecord(record);
      return record;
    }
  }

  public int updateRecords(final Consumer<? super ChangeTrackRecord> updateAction) {
    int i = 0;
    final RecordDefinition recordDefinition = getRecordDefinition();
    final RecordStore recordStore = recordDefinition.getRecordStore();
    setRecordFactory(ArrayChangeTrackRecord.FACTORY);
    try (
      Transaction transaction = recordStore.newTransaction(TransactionOptions.REQUIRED);
      RecordReader reader = getRecordReader();
      RecordWriter writer = recordStore.newRecordWriter(recordDefinition)) {
      for (final Record record : reader) {
        final ChangeTrackRecord changeTrackRecord = (ChangeTrackRecord)record;
        updateAction.accept(changeTrackRecord);
        if (changeTrackRecord.isModified()) {
          writer.write(changeTrackRecord);
          i++;
        }
      }
    }
    return i;
  }
}
