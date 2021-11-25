/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.geomgraph;

import com.revolsys.geometry.model.IntersectionMatrix;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.Assert;

/**
 * A GraphComponent is the parent class for the objects'
 * that form a graph.  Each GraphComponent can carry a
 * Label.
 * @version 1.7
 */
abstract public class GraphComponent {

  private boolean isCovered = false;

  private boolean isCoveredSet = false;

  /**
   * isInResult indicates if this component has already been included in the result
   */
  private boolean isInResult = false;

  private boolean isVisited = false;

  protected Label label;

  public GraphComponent() {
  }

  public GraphComponent(final Label label) {
    this.label = label;
  }

  /**
   * compute the contribution to an IM for this component
   */
  abstract protected void computeIM(IntersectionMatrix im);

  public Label getLabel() {
    return this.label;
  }

  /**
   * @return a coordinate in this component (or null, if there are none)
   */
  abstract public Point getPoint();

  public boolean isCovered() {
    return this.isCovered;
  }

  public boolean isCoveredSet() {
    return this.isCoveredSet;
  }

  public boolean isInResult() {
    return this.isInResult;
  }

  /**
   * An isolated component is one that does not intersect or touch any other
   * component.  This is the case if the label has valid locations for
   * only a single Geometry.
   *
   * @return true if this component is isolated
   */
  abstract public boolean isIsolated();

  public boolean isVisited() {
    return this.isVisited;
  }

  public void setCovered(final boolean isCovered) {
    this.isCovered = isCovered;
    this.isCoveredSet = true;
  }

  public void setInResult(final boolean isInResult) {
    this.isInResult = isInResult;
  }

  public void setLabel(final Label label) {
    this.label = label;
  }

  public void setVisited(final boolean isVisited) {
    this.isVisited = isVisited;
  }

  /**
   * Update the IM with the contribution for this component.
   * A component only contributes if it has a labelling for both parent geometries
   */
  public void updateIM(final IntersectionMatrix im) {
    Assert.isTrue(this.label.getGeometryCount() >= 2, "found partial label");
    computeIM(im);
  }

}
