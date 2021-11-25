package org.jeometry.common.data.type;

import java.util.function.Function;

public class ObjectDataType extends AbstractDataType {
  private static Function<Object, Object> toObjectFunction;

  public static void setToObjectFunction(final Function<Object, Object> toObjectFunction) {
    ObjectDataType.toObjectFunction = toObjectFunction;
  }

  public ObjectDataType() {
    super("object", Object.class, true);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V toObject(final Object value) {
    if (toObjectFunction == null) {
      return (V)value;
    } else {
      return (V)toObjectFunction.apply(value);
    }

  }
}
