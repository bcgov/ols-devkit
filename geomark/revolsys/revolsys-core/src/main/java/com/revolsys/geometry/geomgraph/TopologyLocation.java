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

import com.revolsys.geometry.model.Location;

/**
 * A TopologyLocation is the labelling of a
 * GraphComponent's topological relationship to a single Geometry.
 * <p>
 * If the parent component is an area edge, each side and the edge itself
 * have a topological location.  These locations are named
 * <ul>
 * <li> ON: on the edge
 * <li> LEFT: left-hand side of the edge
 * <li> RIGHT: right-hand side
 * </ul>
 * If the parent component is a line edge or node, there is a single
 * topological relationship attribute, ON.
 * <p>
 * The possible values of a topological location are
 * {Location.NONE, Location.EXTERIOR, Location.BOUNDARY, Location.INTERIOR}
 * <p>
 * The labelling is stored in an array location[j] where
 * where j has the values ON, LEFT, RIGHT
 * @version 1.7
 */
public class TopologyLocation {

  Location location[];

  public TopologyLocation(final Location on) {
    init(1);
    this.location[Position.ON] = on;
  }

  /**
   * Constructs a TopologyLocation specifying how points on, to the left of, and to the
   * right of some GraphComponent relate to some Geometry. Possible values for the
   * parameters are Location.NULL, Location.EXTERIOR, Location.BOUNDARY,
   * and Location.INTERIOR.
   * @see Location
   */
  public TopologyLocation(final Location on, final Location left, final Location right) {
    init(3);
    this.location[Position.ON] = on;
    this.location[Position.LEFT] = left;
    this.location[Position.RIGHT] = right;
  }

  public TopologyLocation(final Location[] location) {
    this.location = location;
  }

  public TopologyLocation(final TopologyLocation gl) {
    init(gl.location.length);
    if (gl != null) {
      for (int i = 0; i < this.location.length; i++) {
        this.location[i] = gl.location[i];
      }
    }
  }

  public boolean allPositionsEqual(final Location loc) {
    for (final Location element : this.location) {
      if (element != loc) {
        return false;
      }
    }
    return true;
  }

  public void flip() {
    if (this.location.length <= 1) {
      return;
    }
    final Location temp = this.location[Position.LEFT];
    this.location[Position.LEFT] = this.location[Position.RIGHT];
    this.location[Position.RIGHT] = temp;
  }

  public Location get(final int posIndex) {
    if (posIndex < this.location.length) {
      return this.location[posIndex];
    }
    return Location.NONE;
  }

  public Location[] getLocations() {
    return this.location;
  }

  private void init(final int size) {
    this.location = new Location[size];
    setAllLocations(Location.NONE);
  }

  /**
   * @return true if any locations are NULL
   */
  public boolean isAnyNull() {
    for (final Location element : this.location) {
      if (element == Location.NONE) {
        return true;
      }
    }
    return false;
  }

  public boolean isArea() {
    return this.location.length > 1;
  }

  public boolean isEqualOnSide(final TopologyLocation le, final int locIndex) {
    return this.location[locIndex] == le.location[locIndex];
  }

  public boolean isLine() {
    return this.location.length == 1;
  }

  /**
   * @return true if all locations are NULL
   */
  public boolean isNull() {
    for (final Location element : this.location) {
      if (element != Location.NONE) {
        return false;
      }
    }
    return true;
  }

  /**
   * merge updates only the NULL attributes of this object
   * with the attributes of another.
   */
  public void merge(final TopologyLocation gl) {
    // if the src is an Area label & and the dest is not, increase the dest to
    // be an Area
    if (gl.location.length > this.location.length) {
      final Location[] newLoc = new Location[3];
      newLoc[Position.ON] = this.location[Position.ON];
      newLoc[Position.LEFT] = Location.NONE;
      newLoc[Position.RIGHT] = Location.NONE;
      this.location = newLoc;
    }
    for (int i = 0; i < this.location.length; i++) {
      if (this.location[i] == Location.NONE && i < gl.location.length) {
        this.location[i] = gl.location[i];
      }
    }
  }

  public void setAllLocations(final Location locValue) {
    for (int i = 0; i < this.location.length; i++) {
      this.location[i] = locValue;
    }
  }

  public void setAllLocationsIfNull(final Location locValue) {
    for (int i = 0; i < this.location.length; i++) {
      if (this.location[i] == Location.NONE) {
        this.location[i] = locValue;
      }
    }
  }

  public void setLocation(final int locIndex, final Location locValue) {
    this.location[locIndex] = locValue;
  }

  public void setLocation(final Location locValue) {
    setLocation(Position.ON, locValue);
  }

  public void setLocations(final Location on, final Location left, final Location right) {
    this.location[Position.ON] = on;
    this.location[Position.LEFT] = left;
    this.location[Position.RIGHT] = right;
  }

  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder();
    if (this.location.length > 1) {
      buf.append(Location.toLocationSymbol(this.location[Position.LEFT]));
    }
    buf.append(Location.toLocationSymbol(this.location[Position.ON]));
    if (this.location.length > 1) {
      buf.append(Location.toLocationSymbol(this.location[Position.RIGHT]));
    }
    return buf.toString();
  }
}
