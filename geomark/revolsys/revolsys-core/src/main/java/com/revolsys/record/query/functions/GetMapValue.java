package com.revolsys.record.query.functions;

import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.query.QueryValue;
import com.revolsys.util.Property;

public class GetMapValue extends SimpleFunction {

  public GetMapValue(final List<QueryValue> parameters) {
    super("get_map_value", parameters);
  }

  @Override
  public <V> V getValue(final MapEx record) {
    final Map<String, ?> map = getParameterValue(0, record);
    final String key = getParameterStringValue(1, record);
    if (map == null || !Property.hasValue(key)) {
      return null;
    } else {
      final V value = Property.get(map, key);
      return value;
    }
  }

}
