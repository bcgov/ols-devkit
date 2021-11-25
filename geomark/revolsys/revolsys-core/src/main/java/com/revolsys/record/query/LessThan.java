package com.revolsys.record.query;

import org.jeometry.common.compare.CompareUtil;

import com.revolsys.collection.map.MapEx;

public class LessThan extends BinaryCondition {

  public LessThan(final QueryValue left, final QueryValue right) {
    super(left, "<", right);
  }

  @Override
  public LessThan clone() {
    return (LessThan)super.clone();
  }

  @Override
  public boolean test(final MapEx record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return CompareUtil.compare(value1, value2) < 0;
  }

}
