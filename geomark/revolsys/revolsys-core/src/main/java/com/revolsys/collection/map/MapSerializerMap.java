package com.revolsys.collection.map;

import java.util.Map;

import com.revolsys.io.map.MapSerializer;

public class MapSerializerMap extends DelegatingMap<String, Object> {

  private final MapSerializer serializer;

  public MapSerializerMap(final MapSerializer serializer) {
    this.serializer = serializer;
  }

  @Override
  public Map<String, Object> getMap() {
    return this.serializer.toMap();
  }

  @Override
  public void setMap(final Map<String, Object> map) {
    throw new UnsupportedOperationException("Cannot change the underlying map as it is not used.");
  }
}
