package com.revolsys.geometry.graph.event;

import java.util.LinkedHashSet;

import com.revolsys.geometry.graph.Edge;

public class EdgeEventListenerList<T> extends LinkedHashSet<EdgeEventListener>
  implements EdgeEventListener<T> {

  /**
   *
   */
  private static final long serialVersionUID = 4332907382649828764L;

  public void edgeEvent(final Edge<T> edge, final String ruleName, final String action,
    final String notes) {
    if (!isEmpty()) {
      final EdgeEvent<T> edgeEvent = new EdgeEvent<>(edge, ruleName, action, notes);
      edgeEvent(edgeEvent);
    }
  }

  @Override
  public void edgeEvent(final EdgeEvent<T> edgeEvent) {
    for (final EdgeEventListener<T> listener : this) {
      listener.edgeEvent(edgeEvent);
    }
  }

}
