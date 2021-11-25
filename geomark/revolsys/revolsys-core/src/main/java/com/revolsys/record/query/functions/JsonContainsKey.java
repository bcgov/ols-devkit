package com.revolsys.record.query.functions;

import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.BinaryCondition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;

public class JsonContainsKey extends BinaryCondition {

  public static Query and(final Query query, final CharSequence fieldName, final String key,
    final Object value) {
    final Value queryValue = Value.newValue(value);
    final QueryValue json = query.newQueryValue(fieldName, JsonByKey::new, key);
    final JsonContainsKey condition = new JsonContainsKey(json, queryValue);
    query.and(condition);
    return query;
  }

  public JsonContainsKey(final QueryValue left, final QueryValue right) {
    super(left, "??", right);
    if (!Value.isString(right)) {
      throw new IllegalArgumentException(
        "JsonContainsKey path parameter is not a string: " + right);
    }
  }

  @Override
  public JsonContainsKey clone() {
    return (JsonContainsKey)super.clone();
  }

  @Override
  public boolean test(final MapEx record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final String value2 = right.getValue(record);

    if (value1 instanceof JsonObject) {
      final JsonObject jsonObject = (JsonObject)value1;
      return jsonObject.containsKey(value2);
    }
    if (value1 instanceof List) {
      final List<?> jsonObject = (List<?>)value1;
      return jsonObject.contains(value2);
    } else {
      return false;
    }
  }

}
