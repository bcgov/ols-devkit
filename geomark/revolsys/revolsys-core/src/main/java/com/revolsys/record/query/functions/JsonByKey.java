package com.revolsys.record.query.functions;

import java.sql.PreparedStatement;
import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.RecordStore;

public class JsonByKey extends SimpleFunction {

  public static final String NAME = "JSON_BY_KEY";

  private String path;

  public JsonByKey(final QueryValue json, final QueryValue path) {
    super(NAME, 2, json, path);
    if (Value.isString(path)) {
      this.path = (String)((Value)path).getValue();
    } else {
      throw new IllegalArgumentException("JSON_BY_KEY path parameter is not a string: " + path);
    }
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    final QueryValue jsonParameter = getParameter(0);
    jsonParameter.appendSql(query, recordStore, buffer);

    buffer.append(" -> '");
    buffer.append(this.path);
    buffer.append("'");
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    final QueryValue jsonParameter = getParameter(0);
    index = jsonParameter.appendParameters(index, statement);
    return index;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final MapEx record) {

    final JsonObject value = getParameterValue(0, record, Json.JSON_OBJECT);
    final String path = getParameterStringValue(1, record);
    if (value != null) {
      final String[] names = path.split("\\.");
      final Object result = value.getByPath(names);
      if (result != null) {
        return (V)result.toString();
      }
    }
    return null;

  }

  @Override
  public String toString() {
    final List<QueryValue> parameters = getParameters();
    return parameters.get(0) + " -> '" + this.path + "'";
  }
}
