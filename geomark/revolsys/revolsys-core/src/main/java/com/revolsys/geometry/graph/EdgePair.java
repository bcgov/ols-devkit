package com.revolsys.geometry.graph;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.record.Record;

public class EdgePair<T> {
  private final Edge<T> edge1;

  private final Edge<T> edge2;

  private final Map<String, Object> properties1 = new HashMap<>();

  private final Map<String, Object> properties2 = new HashMap<>();

  public EdgePair(final Edge<T> edge1, final Edge<T> edge2) {
    this.edge1 = edge1;
    this.edge2 = edge2;
  }

  public Edge<T> getEdge1() {
    return this.edge1;
  }

  public Edge<T> getEdge2() {
    return this.edge2;
  }

  @SuppressWarnings("unchecked")
  public <V extends Record> V getObject1() {
    return (V)this.edge1.getObject();
  }

  @SuppressWarnings("unchecked")
  public <V extends Record> V getObject2() {
    return (V)this.edge2.getObject();
  }

  @SuppressWarnings("unchecked")
  public <V> V getProperty1(final String name) {
    return (V)this.properties1.get(name);
  }

  @SuppressWarnings("unchecked")
  public <V> V getProperty2(final String name) {
    return (V)this.properties2.get(name);
  }

  public void setProperty1(final String name, final Object value) {
    this.properties1.put(name, value);
  }

  public void setProperty2(final String name, final Object value) {
    this.properties2.put(name, value);
  }
}
