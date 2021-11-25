package com.revolsys.record.query.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.functions.JsonValue;
import com.revolsys.record.query.functions.Lower;
import com.revolsys.record.query.functions.RegexpReplace;
import com.revolsys.record.query.functions.SimpleFunction;
import com.revolsys.record.query.functions.Upper;
import com.revolsys.record.schema.RecordDefinition;

public abstract class AbstractSqlParser implements SqlParser {

  private static Map<String, Function<List<QueryValue>, QueryValue>> FUNCTION_CONSTRUCTOR_BY_NAME = new HashMap<>();

  static {
    addFunction(Upper.NAME, Upper::new);
    addFunction(Lower.NAME, Lower::new);
    addFunction(RegexpReplace.NAME, RegexpReplace::new);
    addFunction(JsonValue.NAME, JsonValue::new);
  }

  public static void addFunction(final String name,
    final Function<List<QueryValue>, QueryValue> functionConverter) {
    final String upperName = name.toUpperCase();
    if (FUNCTION_CONSTRUCTOR_BY_NAME.containsKey(upperName)) {
      throw new IllegalArgumentException("Duplicate function: " + name);
    } else {
      FUNCTION_CONSTRUCTOR_BY_NAME.put(upperName, functionConverter);
      FUNCTION_CONSTRUCTOR_BY_NAME.put(name, functionConverter);
    }
  }

  public static Set<String> getFunctionNames() {
    final Set<String> names = new TreeSet<>();
    for (String name : FUNCTION_CONSTRUCTOR_BY_NAME.keySet()) {
      name = name.toUpperCase();
      names.add(name);
    }
    return names;
  }

  protected final RecordDefinition recordDefinition;

  protected final String sqlPrefix;

  public AbstractSqlParser(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
    String tableName;
    if (this.recordDefinition == null) {
      tableName = "Unknown";
    } else {
      tableName = this.recordDefinition.getPath().substring(1).replace('/', '.');
    }
    this.sqlPrefix = "SELECT * FROM " + tableName + " WHERE";

  }

  protected QueryValue newFunction(final String name, final List<QueryValue> parameters) {
    Function<List<QueryValue>, QueryValue> constructor = FUNCTION_CONSTRUCTOR_BY_NAME.get(name);
    if (constructor == null) {
      final String upperName = name.toUpperCase();
      constructor = FUNCTION_CONSTRUCTOR_BY_NAME.get(upperName);
      if (constructor == null) {
        return new SimpleFunction(name, parameters);
      }

    }
    return constructor.apply(parameters);
  }
}
