package com.revolsys.record.io.format.openstreetmap.model;

import org.jeometry.common.data.identifier.SingleIdentifier;

public class OsmNodeIdentifier extends SingleIdentifier {

  public OsmNodeIdentifier(final long id) {
    super(id);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof OsmNodeIdentifier) {
      return super.equals(other);
    } else {
      return false;
    }
  }
}
