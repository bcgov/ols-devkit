package com.revolsys.record.io.format.json;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jeometry.common.data.type.AbstractDataType;
import org.jeometry.common.data.type.DataType;

public class JsonObjectDataType extends AbstractDataType {

  public JsonObjectDataType(final String name, final Class<?> javaClass) {
    super(name, javaClass, true);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equalsNotNull(final Object object1, final Object object2) {
    final Map<Object, Object> map1 = (Map<Object, Object>)object1;
    final Map<Object, Object> map2 = (Map<Object, Object>)object2;
    if (map1.size() == map2.size()) {
      final Set<Object> keys1 = map1.keySet();
      final Set<Object> keys2 = map2.keySet();
      if (keys1.equals(keys2)) {
        for (final Object key : keys1) {
          final Object value1 = map1.get(key);
          final Object value2 = map2.get(key);
          if (!DataType.equal(value1, value2)) {
            return false;
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @SuppressWarnings({
    "unchecked"
  })
  @Override
  protected boolean equalsNotNull(final Object object1, final Object object2,
    final Collection<? extends CharSequence> exclude) {
    final Map<Object, Object> map1 = (Map<Object, Object>)object1;
    final Map<Object, Object> map2 = (Map<Object, Object>)object2;
    final Set<Object> keys = new TreeSet<>();
    keys.addAll(map1.keySet());
    keys.addAll(map2.keySet());
    keys.removeAll(exclude);

    for (final Object key : keys) {
      final Object value1 = map1.get(key);
      final Object value2 = map2.get(key);
      if (!DataType.equal(value1, value2, exclude)) {
        return false;
      }
    }
    return true;
  }

  protected JsonObject toJsonObject(final Map<? extends String, ? extends Object> map) {
    return new JsonObjectHash(map);
  }

  @SuppressWarnings({
    "unchecked"
  })
  @Override
  protected Object toObjectDo(final Object value) {
    if (value instanceof JsonObject) {
      return toJsonObject((JsonObject)value);
    } else if (value instanceof Jsonable) {
      return ((Jsonable)value).asJson();
    } else if (value instanceof Map) {
      final Map<? extends String, ? extends Object> map = (Map<? extends String, ? extends Object>)value;
      return toJsonObject(map);
    } else if (value instanceof String) {
      final JsonObject map = Json.toObjectMap((String)value);
      if (map == null) {
        return null;
      } else {
        return toJsonObject(map);
      }
    } else {
      return toJsonObject(JsonParser.read(value));
    }
  }

  @SuppressWarnings({
    "unchecked"
  })
  @Override
  protected String toStringDo(final Object value) {
    if (value instanceof Jsonable) {
      return ((Jsonable)value).toJsonString();
    } else if (value instanceof Map) {
      final Map<? extends String, ? extends Object> map = (Map<? extends String, ? extends Object>)value;
      return Json.toString(map);
    } else if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }
}
