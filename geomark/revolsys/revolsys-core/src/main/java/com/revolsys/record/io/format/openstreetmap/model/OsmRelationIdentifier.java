package com.revolsys.record.io.format.openstreetmap.model;

import org.jeometry.common.data.identifier.SingleIdentifier;

public class OsmRelationIdentifier extends SingleIdentifier {

  public OsmRelationIdentifier(final long id) {
    super(id);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof OsmRelationIdentifier) {
      return super.equals(other);
    } else {
      return false;
    }
  }
}
