package com.revolsys.record.query;

import com.revolsys.collection.map.MapEx;

public class Not extends LeftUnaryCondition {

  public Not(final Condition condition) {
    super("NOT", condition);
  }

  public Not(final QueryValue condition) {
    super("NOT", condition);
  }

  @Override
  public Not clone() {
    return (Not)super.clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Not) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public boolean test(final MapEx record) {
    final Condition condition = getValue();
    if (condition.test(record)) {
      return false;
    } else {
      return true;
    }
  }
}
