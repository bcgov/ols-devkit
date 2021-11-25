package com.revolsys.record.query;

import java.util.Arrays;

import com.revolsys.collection.map.MapEx;

public class Or extends AbstractMultiCondition {

  public Or(final Condition... conditions) {
    this(Arrays.asList(conditions));
  }

  public Or(final Iterable<? extends Condition> conditions) {
    super("OR", conditions);
  }

  public Or(final QueryValue value1, final QueryValue value2) {
    this(Arrays.asList((Condition)value1, (Condition)value2));
  }

  @Override
  public Or clone() {
    return (Or)super.clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Or) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public Condition or(final Condition condition) {
    if (condition != null && !condition.isEmpty()) {
      if (isEmpty()) {
        return condition;
      } else {
        addCondition(condition);
      }
    }
    return this;
  }

  @Override
  public boolean test(final MapEx record) {
    final QueryValue[] values = this.values;
    if (values.length == 0) {
      return true;
    } else {
      for (final QueryValue value : values) {
        final Condition condition = (Condition)value;
        if (condition.test(record)) {
          return true;
        }
      }
      return false;
    }
  }
}
