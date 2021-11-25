package com.revolsys.record.io.format.openstreetmap.model;

import org.jeometry.common.data.identifier.SingleIdentifier;

public class OsmWayIdentifier extends SingleIdentifier {

  public OsmWayIdentifier(final long id) {
    super(id);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof OsmWayIdentifier) {
      return super.equals(other);
    } else {
      return false;
    }
  }
}
