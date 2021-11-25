package com.revolsys.collection.map;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.data.type.FunctionDataType;

import com.revolsys.record.io.format.json.Json;

public class TreeMapEx extends TreeMap<String, Object> implements MapEx {
  private static final long serialVersionUID = 1L;

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public static final DataType DATA_TYPE = new FunctionDataType("TreeMap", TreeMapEx.class, true,
    value -> {
      if (value instanceof TreeMapEx) {
        return (TreeMapEx)value;
      } else if (value instanceof Map) {
        return new TreeMapEx((Map)value);
      } else if (value instanceof String) {
        final MapEx map = Json.toObjectMap((String)value);
        if (map == null) {
          return null;
        } else {
          return new TreeMapEx(map);
        }
      } else {
        return value;
      }
    }, (value) -> {
      if (value instanceof Map) {
        return Json.toString((Map)value);
      } else if (value == null) {
        return null;
      } else {
        return value.toString();
      }

    }, FunctionDataType.MAP_EQUALS, FunctionDataType.MAP_EQUALS_EXCLUDES);

  static {
    DataTypes.register(DATA_TYPE);
  }

  public TreeMapEx() {
    super();
  }

  public TreeMapEx(final Comparator<? super String> comparator) {
    super(comparator);
  }

  public TreeMapEx(final Map<? extends String, ? extends Object> m) {
    super(m);
  }

  public TreeMapEx(final SortedMap<String, ? extends Object> m) {
    super(m);
  }

  public TreeMapEx(final String key, final Object value) {
    add(key, value);
  }

  @Override
  public TreeMapEx clone() {
    return new TreeMapEx(this);
  }

  @Override
  public String toString() {
    return super.toString();
  }
}
