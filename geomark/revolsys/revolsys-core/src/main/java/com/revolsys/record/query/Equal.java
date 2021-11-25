package com.revolsys.record.query;

import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.map.MapEx;

public class Equal extends BinaryCondition {

  public Equal(final QueryValue left, final QueryValue right) {
    super(left, "=", right);
  }

  @Override
  public Equal clone() {
    return (Equal)super.clone();
  }

  @Override
  public boolean test(final MapEx record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return DataType.equal(value1, value2);
  }

}
