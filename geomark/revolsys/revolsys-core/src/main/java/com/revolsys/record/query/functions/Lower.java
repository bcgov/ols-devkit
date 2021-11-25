package com.revolsys.record.query.functions;

import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.query.QueryValue;
import com.revolsys.util.Property;

public class Lower extends UnaryFunction {

  public static final String NAME = "LOWER";

  public Lower(final List<QueryValue> parameters) {
    super(NAME, parameters);
  }

  public Lower(final QueryValue parameter) {
    super(NAME, parameter);
  }

  @Override
  public String getStringValue(final MapEx record) {
    return getValue(record);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final MapEx record) {
    final QueryValue parameter = getParameter();
    final String stringValue = parameter.getStringValue(record);
    if (Property.hasValue(stringValue)) {
      return (V)stringValue.toLowerCase();
    } else {
      return (V)stringValue;
    }
  }
}
