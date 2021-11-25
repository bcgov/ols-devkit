package com.revolsys.geometry.model.prep;

import com.revolsys.geometry.algorithm.locate.IndexedPointInAreaLocator;
import com.revolsys.geometry.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Location;

public class PreparedLinearRing extends PreparedLineString implements LinearRing {

  private static final long serialVersionUID = 1L;

  private PointOnGeometryLocator pointLocator;

  public PreparedLinearRing(final LinearRing ring) {
    super(ring);
  }

  @Override
  public LinearRing clone() {
    return (LinearRing)super.clone();
  }

  public synchronized PointOnGeometryLocator getPointLocator() {
    if (this.pointLocator == null) {
      this.pointLocator = new IndexedPointInAreaLocator(this);
    }

    return this.pointLocator;
  }

  @Override
  public boolean isPointInRing(final double x, final double y) {
    final PointOnGeometryLocator pointLocator = getPointLocator();
    final Location location = pointLocator.locate(x, y);
    return location != Location.EXTERIOR;
  }

  @Override
  public PreparedLinearRing prepare() {
    return this;
  }
}
