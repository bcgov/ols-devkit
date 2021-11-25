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
package com.revolsys.geometry.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.algorithm.BoundaryNodeRule;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;

/**
 * Computes the boundary of a {@link Geometry}.
 * Allows specifying the {@link BoundaryNodeRule} to be used.
 * This operation will always return a {@link Geometry} of the appropriate
 * dimension for the boundary (even if the input geometry is empty).
 * The boundary of zero-dimensional geometries (Points) is
 * always the empty {@link Geometry}.
 *
 * @author Martin Davis
 * @version 1.7
 */

public class BoundaryOp {
  private final BoundaryNodeRule bnRule;

  private Map endpointMap;

  private final Geometry geom;

  private final GeometryFactory geomFact;

  public BoundaryOp(final Geometry geom) {
    this(geom, BoundaryNodeRule.MOD2_BOUNDARY_RULE);
  }

  public BoundaryOp(final Geometry geom, final BoundaryNodeRule bnRule) {
    this.geom = geom;
    this.geomFact = geom.getGeometryFactory();
    this.bnRule = bnRule;
  }

  private void addEndpoint(final Point pt) {
    Counter counter = (Counter)this.endpointMap.get(pt);
    if (counter == null) {
      counter = new Counter();
      this.endpointMap.put(pt, counter);
    }
    counter.count++;
  }

  private Geometry boundaryLineString(final LineString line) {
    if (this.geom.isEmpty()) {
      return this.geomFact.point();
    }

    if (line.isClosed()) {
      // check whether endpoints of valence 2 are on the boundary or not
      final boolean closedEndpointOnBoundary = this.bnRule.isInBoundary(2);
      if (closedEndpointOnBoundary) {
        return line.getFromPoint();
      } else {
        return this.geomFact.punctual((Point[])null);
      }
    }
    return this.geomFact.punctual(new Point[] {
      line.getFromPoint(), line.getToPoint()
    });
  }

  /*
   * // MD - superseded private Point[]
   * computeBoundaryFromGeometryGraph(MultiLineString mLine) { GeometryGraph g =
   * new GeometryGraph(0, mLine, bnRule); Point[] bdyPts =
   * g.getBoundaryPoints(); return bdyPts; }
   */

  private Geometry boundaryMultiLineString(final Lineal mLine) {
    if (this.geom.isEmpty()) {
      return this.geomFact.point();
    }

    final Point[] bdyPts = computeBoundaryCoordinates(mLine);

    // return Point or MultiPoint
    if (bdyPts.length == 1) {
      return this.geomFact.point(bdyPts[0]);
    }
    // this handles 0 points case as well
    return this.geomFact.punctual(bdyPts);
  }

  private Point[] computeBoundaryCoordinates(final Lineal mLine) {
    final List bdyPts = new ArrayList();
    this.endpointMap = new TreeMap();
    for (int i = 0; i < mLine.getGeometryCount(); i++) {
      final LineString line = (LineString)mLine.getGeometry(i);
      if (line.getVertexCount() == 0) {
        continue;
      }
      addEndpoint(line.getPoint(0));
      addEndpoint(line.getPoint(line.getVertexCount() - 1));
    }

    for (final Object element : this.endpointMap.entrySet()) {
      final Map.Entry entry = (Map.Entry)element;
      final Counter counter = (Counter)entry.getValue();
      final int valence = counter.count;
      if (this.bnRule.isInBoundary(valence)) {
        bdyPts.add(entry.getKey());
      }
    }

    return (Point[])bdyPts.toArray(new Point[0]);
  }

  public Geometry getBoundary() {
    if (this.geom instanceof LineString) {
      return boundaryLineString((LineString)this.geom);
    } else if (this.geom instanceof Lineal) {
      return boundaryMultiLineString((Lineal)this.geom);
    }
    return this.geom.getBoundary();
  }
}

/**
 * Stores an integer count, for use as a Map entry.
 *
 * @author Martin Davis
 * @version 1.7
 */
class Counter {
  /**
   * The value of the count
   */
  int count;
}
