package com.revolsys.record.query.functions;

import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;
import com.revolsys.util.Property;

public class RegexpReplace extends SimpleFunction {

  public static final String NAME = "REGEXP_REPLACE";

  public RegexpReplace(final List<QueryValue> parameters) {
    super(NAME, 3, parameters);
  }

  public RegexpReplace(final QueryValue value, final String pattern, final String replace) {
    super(NAME, value, Value.newValue(pattern), Value.newValue(replace));
  }

  public RegexpReplace(final QueryValue value, final String pattern, final String replace,
    final String flags) {
    super(NAME, value, Value.newValue(pattern), Value.newValue(replace), Value.newValue(flags));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final MapEx record) {

    final String text = getParameterStringValue(0, record);
    final String pattern = getParameterStringValue(1, record);
    String replace = getParameterStringValue(2, record);
    if (replace == null) {
      replace = "";
    }
    if (Property.hasValue(text)) {
      return (V)text.replaceAll(pattern, replace);
    } else {
      return null;
    }
  }
}
