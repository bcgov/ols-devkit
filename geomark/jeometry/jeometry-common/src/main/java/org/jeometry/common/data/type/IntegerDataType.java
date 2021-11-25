package org.jeometry.common.data.type;

public class IntegerDataType extends AbstractDataType {

  public IntegerDataType() {
    super("int", Integer.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return (int)value1 == (int)value2;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getMaxValue() {
    final Integer max = Integer.MAX_VALUE;
    return (V)max;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getMinValue() {
    final Integer min = Integer.MIN_VALUE;
    return (V)min;
  }

  @Override
  protected Integer toObjectDo(final Object value) {
    if (value instanceof Integer) {
      return (Integer)value;
    } else if (value instanceof Number) {
      return ((Number)value).intValue();
    } else {
      final String string = DataTypes.toString(value);
      return Integer.valueOf(string);
    }
  }

  @Override
  protected String toStringDo(final Object value) {
    return String.valueOf((int)value);
  }
}
