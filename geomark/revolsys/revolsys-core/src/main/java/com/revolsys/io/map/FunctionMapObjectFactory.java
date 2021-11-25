package com.revolsys.io.map;

import java.util.Map;
import java.util.function.Function;

public class FunctionMapObjectFactory extends AbstractMapObjectFactory {
  private final Function<Map<String, ? extends Object>, Object> function;

  public FunctionMapObjectFactory(final String typeName, final String description,
    final Function<Map<String, ? extends Object>, Object> function) {
    super(typeName, description);
    this.function = function;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V mapToObject(final Map<String, ? extends Object> properties) {
    return (V)this.function.apply(properties);
  }
}
