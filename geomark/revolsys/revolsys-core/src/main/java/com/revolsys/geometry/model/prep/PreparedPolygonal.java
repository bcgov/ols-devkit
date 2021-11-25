package com.revolsys.geometry.model.prep;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Polygonal;

public interface PreparedPolygonal extends Polygonal {

  default boolean containsTopo(final Geometry geometry) {
    return Polygonal.super.contains(geometry);
  }

  default boolean coversTopo(final Geometry geometry) {
    return Polygonal.super.covers(geometry);
  }

}
