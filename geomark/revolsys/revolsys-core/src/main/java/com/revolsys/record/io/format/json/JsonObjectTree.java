package com.revolsys.record.io.format.json;

import java.util.Map;

import com.revolsys.collection.map.TreeMapEx;

public class JsonObjectTree extends TreeMapEx implements JsonObject {

  private static final long serialVersionUID = 1L;

  JsonObjectTree() {
    super();
  }

  JsonObjectTree(final Map<? extends String, ? extends Object> m) {
    super(m);
  }

  JsonObjectTree(final String key, final Object value) {
    add(key, value);
  }

  @Override
  public JsonObjectTree clone() {
    return (JsonObjectTree)new JsonObjectTree()//
      .addValuesClone(this);
  }

  @Override
  public String toString() {
    return Json.toString(this, false);
  }
}
