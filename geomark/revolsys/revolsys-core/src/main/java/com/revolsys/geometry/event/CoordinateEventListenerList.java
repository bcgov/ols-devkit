package com.revolsys.geometry.event;

import java.util.LinkedHashSet;

import com.revolsys.geometry.model.Point;

public class CoordinateEventListenerList extends LinkedHashSet<CoordinateEventListener>
  implements CoordinateEventListener {

  /**
   *
   */
  private static final long serialVersionUID = 3504994646284361341L;

  @Override
  public void coordinateEvent(final CoordinateEvent coordinateEvent) {
    for (final CoordinateEventListener listener : this) {
      listener.coordinateEvent(coordinateEvent);
    }
  }

  public void coordinateEvent(final Point coordinate, final String typePath, final String ruleName,
    final String action, final String notes) {
    coordinateEvent(new CoordinateEvent(coordinate, typePath, ruleName, action, notes));
  }
}
