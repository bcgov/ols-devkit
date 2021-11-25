package com.revolsys.record.query;

import java.sql.PreparedStatement;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class AcceptAllCondition implements Condition {
  @Override
  public Condition and(final Condition condition) {
    if (condition == null) {
      return this;
    } else {
      return condition;
    }
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder sql) {
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return 0;
  }

  @Override
  public void changeRecordDefinition(final RecordDefinition oldRecordDefinition,
    final RecordDefinition newRecordDefinition) {

  }

  @Override
  public AcceptAllCondition clone() {
    return this;
  }

  @Override
  public Condition clone(final TableReference oldTable, final TableReference newTable) {
    return this;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public Condition or(final Condition condition) {
    if (condition == null) {
      return this;
    } else {
      return condition;
    }
  }

  @Override
  public boolean test(final MapEx record) {
    return true;
  }

  @Override
  public String toFormattedString() {
    return null;
  }

  @Override
  public String toString() {
    return null;
  }
}
