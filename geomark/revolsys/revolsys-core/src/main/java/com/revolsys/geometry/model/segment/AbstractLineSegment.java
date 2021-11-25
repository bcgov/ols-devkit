package com.revolsys.geometry.model.segment;

import com.revolsys.geometry.model.impl.AbstractLineString;

public abstract class AbstractLineSegment extends AbstractLineString implements LineSegment {
  private static final long serialVersionUID = 1L;

  @Override
  public LineSegment clone() {
    return (LineSegment)super.clone();
  }

  /**
   *  Returns <code>true</code> if <code>other</code> has the same values for
   *  its points.
   *
   *@param  o  a <code>LineSegment</code> with which to do the comparison.
   *@return        <code>true</code> if <code>other</code> is a <code>LineSegment</code>
   *      with the same values for the x and y ordinates.
   */
  @Override
  public boolean equals(final Object o) {
    if (o instanceof LineSegment) {
      final LineSegment segment = (LineSegment)o;
      if (equalsVertex(2, 0, segment, 0)) {
        if (equalsVertex(2, 1, segment, 1)) {
          return true;
        }
      }
    }
    return false;
  }
}
