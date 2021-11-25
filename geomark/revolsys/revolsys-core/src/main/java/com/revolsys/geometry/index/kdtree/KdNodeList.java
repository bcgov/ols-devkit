package com.revolsys.geometry.index.kdtree;

import java.util.ArrayList;
import java.util.List;

public class KdNodeList<V> extends KdNode {
  private static final long serialVersionUID = 1L;

  private List<V> values = new ArrayList<>();

  public KdNodeList(final double x, final double y) {
    super(x, y);
  }

  public void addValue(final V value) {
    this.values.add(value);
  }

  public boolean containsValue(final V value) {
    return this.values.contains(value);
  }

  public List<V> getValues() {
    return this.values;
  }

  public boolean removeValue(final V value) {
    return this.values.remove(value);
  }

  public void setValues(final List<V> values) {
    this.values = values;
  }
}
