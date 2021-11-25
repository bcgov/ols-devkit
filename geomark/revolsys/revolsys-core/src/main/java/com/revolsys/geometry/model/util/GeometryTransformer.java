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

package com.revolsys.geometry.model.util;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.util.Property;

/**
 * A framework for processes which transform an input {@link Geometry} into
 * an output {@link Geometry}, possibly changing its structure and type(s).
 * This class is a framework for implementing subclasses
 * which perform transformations on
 * various different Geometry subclasses.
 * It provides an easy way of applying specific transformations
 * to given geometry types, while allowing unhandled types to be simply copied.
 * Also, the framework ensures that if subcomponents change type
 * the parent geometries types change appropriately to maintain valid structure.
 * Subclasses will override whichever <code>transformX</code> methods
 * they need to to handle particular Geometry types.
 * <p>
 * A typically usage would be a transformation class that transforms <tt>Polygons</tt> into
 * <tt>Polygons</tt>, <tt>LineStrings</tt> or <tt>Points</tt>, depending on the geometry of the input
 * (For instance, a simplification operation).
 * This class would likely need to override the {@link #transformMultiPolygon(MultiPolygon, Geometry)transformMultiPolygon}
 * method to ensure that if input Polygons change type the result is a <tt>GeometryCollection</tt>,
 * not a <tt>MultiPolygon</tt>.
 * <p>
 * The default behaviour of this class is simply to recursively transform
 * each Geometry component into an identical object by deep copying down
 * to the level of, but not including, coordinates.
 * <p>
 * All <code>transformX</code> methods may return <code>null</code>,
 * to avoid creating empty or invalid geometry objects. This will be handled correctly
 * by the transformer.   <code>transform<i>XXX</i></code> methods should always return valid
 * geometry - if they cannot do this they should return <code>null</code>
 * (for instance, it may not be possible for a transformLineString implementation
 * to return at least two points - in this case, it should return <code>null</code>).
 * The {@link #transform(Geometry)transform} method itself will always
 * return a non-null Geometry object (but this may be empty).
 *
 * @version 1.7
 *
 */
public abstract class GeometryTransformer {

  protected GeometryFactory factory = null;

  /**
   * Possible extensions:
   * getParent() method to return immediate parent e.g. of LinearRings in Polygons
   */

  private Geometry inputGeom;

  /**
   * <code>true</code> if the type of the input should be preserved
   */
  private final boolean preserveType = false;

  public GeometryTransformer() {
  }

  /**
   * Convenience method which provides statndard way of copying {@link LineString}s
   * @param seq the sequence to copy
   * @return a deep copy of the sequence
   */
  protected final LineString copy(final LineString seq) {
    return seq.clone();
  }

  /**
   * Utility function to make input geometry available
   *
   * @return the input geometry
   */
  public Geometry getInputGeometry() {
    return this.inputGeom;
  }

  public boolean isPreserveType() {
    return this.preserveType;
  }

  public final Geometry transform(final Geometry inputGeom) {
    this.inputGeom = inputGeom;
    this.factory = inputGeom.getGeometryFactory();

    if (inputGeom instanceof Point) {
      return transformPoint((Point)inputGeom);
    } else if (inputGeom instanceof Punctual) {
      return transformMultiPoint((Punctual)inputGeom);
    } else if (inputGeom instanceof LinearRing) {
      return transformLinearRing((LinearRing)inputGeom, null);
    } else if (inputGeom instanceof LineString) {
      return transformLineString((LineString)inputGeom);
    } else if (inputGeom instanceof Lineal) {
      return transformMultiLineString((Lineal)inputGeom);
    } else if (inputGeom instanceof Polygon) {
      return transformPolygon((Polygon)inputGeom, null);
    } else if (inputGeom instanceof Polygonal) {
      return transformMultiPolygon((Polygonal)inputGeom, null);
    } else if (inputGeom.isGeometryCollection()) {
      return transformGeometryCollection(inputGeom, null);
    } else {
      throw new IllegalArgumentException(
        "Unknown Geometry subtype: " + inputGeom.getClass().getName());
    }
  }

  /**
   * Transforms a {@link LineString}.
   * This method should always return a valid coordinate list for
   * the desired result type.  (E.g. a coordinate list for a LineString
   * must have 0 or at least 2 points).
   * If this is not possible, return an empty sequence -
   * this will be pruned out.
   *
   * @param coords the coordinates to transform
   * @param parent the parent geometry
   * @return the transformed coordinates
   */
  protected LineString transformCoordinates(final LineString coords, final Geometry parent) {
    return copy(coords);
  }

  protected Geometry transformGeometryCollection(final Geometry geom, final Geometry parent) {
    final List<Geometry> newGeometries = new ArrayList<>();
    for (final Geometry geometry : geom.geometries()) {
      final Geometry transformGeom = transform(geometry);
      if (transformGeom == null || transformGeom.isEmpty()) {
      } else {
        newGeometries.add(transformGeom);
      }
    }
    return this.factory.geometry(newGeometries);
  }

  /**
   * Transforms a LinearRing.
   * The transformation of a LinearRing may result in a coordinate sequence
   * which does not form a structurally valid ring (i.e. a degnerate ring of 3 or fewer points).
   * In this case a LineString is returned.
   * Subclasses may wish to override this method and check for this situation
   * (e.g. a subclass may choose to eliminate degenerate linear rings)
   *
   * @param ring the ring to simplify
   * @param parent the parent geometry
   * @return a LinearRing if the transformation resulted in a structurally valid ring
   * @return a LineString if the transformation caused the LinearRing to collapse to 3 or fewer points
   */
  protected Geometry transformLinearRing(final LinearRing ring, final Geometry parent) {
    if (ring == null) {
      return this.factory.linearRing();
    } else {
      final LineString points = transformCoordinates(ring, ring);
      if (points == null) {
        return this.factory.linearRing();
      } else {
        final int seqSize = points.getVertexCount();
        // ensure a valid LinearRing
        if (seqSize > 0 && seqSize < 4 && !this.preserveType) {
          return this.factory.lineString(points);
        } else {
          return this.factory.linearRing(points);
        }
      }
    }
  }

  /**
   * Transforms a {@link LineString} geometry.
   *
   * @param line
   * @return
   */
  protected LineString transformLineString(final LineString line) {
    // should check for 1-point sequences and downgrade them to points
    return this.factory.lineString(transformCoordinates(line, line));
  }

  protected Lineal transformMultiLineString(final Lineal lineal) {
    boolean updated = false;
    final List<LineString> newLines = new ArrayList<>();
    for (final LineString line : lineal.lineStrings()) {
      final LineString newLine = transformLineString(line);
      if (newLine == null || newLine.isEmpty()) {
        updated = true;
      } else {
        newLines.add(newLine);
        if (newLine != line) {
          updated = true;
        }
      }
    }
    if (updated) {
      return this.factory.lineal(newLines);
    } else {
      return lineal;
    }
  }

  protected Punctual transformMultiPoint(final Punctual punctual) {
    boolean updated = false;
    final List<Point> newPoints = new ArrayList<>();
    for (final Point point : punctual.points()) {
      final Point newPoint = transformPoint(point);
      if (newPoint == null || newPoint.isEmpty()) {
        updated = true;
      } else {
        newPoints.add(newPoint);
        if (newPoint != point) {
          updated = true;
        }
      }
    }
    if (updated) {
      return this.factory.punctual(newPoints);
    } else {
      return punctual;
    }
  }

  protected Geometry transformMultiPolygon(final Polygonal polygonal, final Geometry parent) {
    final List<Geometry> transGeomList = new ArrayList<>();
    for (final Polygon polygon : polygonal.polygons()) {
      final Geometry transformGeom = transformPolygon(polygon, polygonal);
      if (transformGeom == null) {
        continue;
      }
      if (transformGeom.isEmpty()) {
        continue;
      }
      transGeomList.add(transformGeom);
    }
    return this.factory.buildGeometry(transGeomList);
  }

  protected Point transformPoint(final Point point) {
    return point;
  }

  protected Geometry transformPolygon(final Polygon polygon, final Geometry parent) {
    boolean isAllValidLinearRings = true;
    final Geometry newShell = transformLinearRing(polygon.getShell(), polygon);

    if (newShell == null || !(newShell instanceof LinearRing) || newShell.isEmpty()) {
      isAllValidLinearRings = false;
    }

    final List<Geometry> components = new ArrayList<>();
    components.add(newShell);
    for (final LinearRing hole : polygon.holes()) {
      final Geometry newHole = transformLinearRing(hole, polygon);
      if (Property.hasValue(newHole)) {
        if (!(newHole instanceof LinearRing)) {
          isAllValidLinearRings = false;
        }
        components.add(newHole);
      }
    }

    if (isAllValidLinearRings) {
      return this.factory.polygon(components);
    } else {
      return this.factory.buildGeometry(components);
    }
  }

}
