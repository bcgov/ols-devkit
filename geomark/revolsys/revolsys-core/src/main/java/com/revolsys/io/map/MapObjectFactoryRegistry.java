package com.revolsys.io.map;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.revolsys.util.CaseConverter;
import com.revolsys.util.ServiceInitializer;

public class MapObjectFactoryRegistry {
  public static final Map<String, MapObjectFactory> TYPE_NAME_TO_FACTORY = new HashMap<>();

  static {
    ServiceInitializer.initializeServices();
  }

  public static void addFactory(final MapObjectFactory factory) {
    final String typeName = factory.getTypeName();
    TYPE_NAME_TO_FACTORY.put(typeName, factory);
  }

  public static MapObjectFactory getFactory(final String type) {
    return TYPE_NAME_TO_FACTORY.get(type);
  }

  public static void init() {
  }

  public static void newFactory(final String typeName,
    final Function<Map<String, ? extends Object>, Object> function) {
    newFactory(typeName, CaseConverter.toCapitalizedWords(typeName), function);
  }

  public static void newFactory(final String typeName, final String description,
    final Function<Map<String, ? extends Object>, Object> function) {
    final FunctionMapObjectFactory factory = new FunctionMapObjectFactory(typeName, description,
      function);
    TYPE_NAME_TO_FACTORY.put(typeName, factory);
  }
}
