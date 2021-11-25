package com.revolsys.util;

import java.util.Map;
import java.util.function.Supplier;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;

public class SupplierWithProperties<T> implements ObjectWithProperties, Supplier<T>, MapSerializer {
  private final Supplier<T> supplier;

  private final JsonObject properties = new JsonObjectHash();

  public SupplierWithProperties(final Supplier<T> supplier,
    final Map<String, ? extends Object> properties) {
    super();
    this.supplier = supplier;
    addAllToMap(this.properties, properties);
  }

  @Override
  public T get() {
    return this.supplier.get();
  }

  @Override
  public MapEx getProperties() {
    return this.properties;
  }

  @Override
  public JsonObject toMap() {
    return this.properties;
  }

  @Override
  public String toString() {
    return MapObjectFactory.getType(this.properties);
  }
}
