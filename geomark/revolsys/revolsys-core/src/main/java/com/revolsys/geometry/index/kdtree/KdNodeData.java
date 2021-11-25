package com.revolsys.geometry.index.kdtree;

public class KdNodeData extends KdNode {
  private static final long serialVersionUID = 1L;

  private Object data;

  public KdNodeData(final double x, final double y) {
    super(x, y);
  }

  public Object getData() {
    return this.data;
  }

  public void setData(final Object data) {
    this.data = data;
  }
}
