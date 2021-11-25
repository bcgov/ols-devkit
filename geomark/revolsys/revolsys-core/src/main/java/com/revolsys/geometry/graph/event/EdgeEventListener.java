package com.revolsys.geometry.graph.event;

import java.util.EventListener;

public interface EdgeEventListener<T> extends EventListener {
  public void edgeEvent(EdgeEvent<T> edgeEvent);
}
