package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.schema.RecordStore;

public abstract class AbstractUnaryQueryValue implements QueryValue {

  private QueryValue value;

  public AbstractUnaryQueryValue() {
  }

  public AbstractUnaryQueryValue(final QueryValue value) {
    this.value = value;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    this.value.appendSql(query, recordStore, buffer);
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (this.value != null) {
      index = this.value.appendParameters(index, statement);
    }
    return index;
  }

  protected void appendValue(final StringBuilder sql, final Query query,
    final RecordStore recordStore) {
    if (this.value == null) {
      sql.append("NULL");
    } else {
      this.value.appendSql(query, recordStore, sql);
    }
    sql.append(" ");
  }

  @Override
  public AbstractUnaryQueryValue clone() {
    try {
      return (AbstractUnaryQueryValue)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public AbstractUnaryQueryValue clone(final TableReference oldTable,
    final TableReference newTable) {
    final AbstractUnaryQueryValue clone = clone();
    clone.value = this.value.clone(oldTable, newTable);
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof AbstractUnaryQueryValue) {
      final AbstractUnaryQueryValue binary = (AbstractUnaryQueryValue)obj;
      if (DataType.equal(binary.getValue(), this.getValue())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Collections.singletonList(this.value);
  }

  @Override
  public String getStringValue(final MapEx record) {
    return this.value.getStringValue(record);
  }

  @SuppressWarnings("unchecked")
  public <V extends QueryValue> V getValue() {
    return (V)this.value;
  }

  @Override
  public <V> V getValue(final MapEx record) {
    return this.value.getValue(record);
  }

  protected void setValue(final QueryValue value) {
    this.value = value;
  }

  @Override
  public String toString() {
    if (this.value == null) {
      return "null";
    } else {
      return this.value.toString();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <QV extends QueryValue> QV updateQueryValues(final TableReference oldTable,
    final TableReference newTable, final Function<QueryValue, QueryValue> valueHandler) {
    final QueryValue value = valueHandler.apply(this.value);
    if (value == this.value) {
      return (QV)this;
    } else {
      final AbstractUnaryQueryValue clone = clone(oldTable, newTable);
      clone.value = value;
      return (QV)clone;
    }
  }
}
