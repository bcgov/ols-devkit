package com.revolsys.geometry.graph.event;

import java.util.LinkedHashSet;

import com.revolsys.geometry.graph.Node;

public class NodeEventListenerList<T> extends LinkedHashSet<NodeEventListener>
  implements NodeEventListener<T> {

  /**
   *
   */
  private static final long serialVersionUID = 491848000001273343L;

  public void nodeEvent(final Node<T> node, final String typePath, final String ruleName,
    final String action, final String notes) {
    if (!isEmpty()) {
      nodeEvent(new NodeEvent<>(node, typePath, ruleName, action, notes));
    }
  }

  @Override
  public void nodeEvent(final NodeEvent<T> nodeEvent) {
    for (final NodeEventListener<T> listener : this) {
      listener.nodeEvent(nodeEvent);
    }
  }
}
