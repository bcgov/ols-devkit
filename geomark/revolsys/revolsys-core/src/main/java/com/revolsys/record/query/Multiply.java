package com.revolsys.record.query;

import java.math.BigDecimal;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.collection.map.MapEx;

public class Multiply extends BinaryArithmatic {

  public Multiply(final QueryValue left, final QueryValue right) {
    super(left, "*", right);
  }

  @Override
  public Multiply clone() {
    return (Multiply)super.clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Multiply) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public <V> V getValue(final MapEx record) {
    final Object leftValue = getLeft().getValue(record);
    final Object rightValue = getRight().getValue(record);
    if (leftValue instanceof Number && rightValue instanceof Number) {
      final BigDecimal number1 = DataTypes.DECIMAL.toObject(leftValue);
      final BigDecimal number2 = DataTypes.DECIMAL.toObject(rightValue);
      final BigDecimal result = number1.multiply(number2);
      return DataTypes.toObject(leftValue.getClass(), result);
    }
    return null;
  }
}
