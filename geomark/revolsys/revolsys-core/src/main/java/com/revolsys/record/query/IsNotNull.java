package com.revolsys.record.query;

import com.revolsys.collection.map.MapEx;

public class IsNotNull extends RightUnaryCondition {

  public IsNotNull(final QueryValue value) {
    super(value, "IS NOT NULL");
  }

  @Override
  public boolean test(final MapEx record) {
    final QueryValue queryValue = getValue();
    final Object value = queryValue.getValue(record);
    return value != null;
  }
}
