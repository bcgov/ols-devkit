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
package com.revolsys.geometry.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.measure.Unit;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jeometry.common.data.type.DataTypeProxy;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.common.function.Consumer4Double;
import org.jeometry.common.function.Function4Double;
import org.jeometry.common.number.Doubles;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.geometry.algorithm.Centroid;
import com.revolsys.geometry.algorithm.ConvexHull;
import com.revolsys.geometry.algorithm.InteriorPointArea;
import com.revolsys.geometry.algorithm.InteriorPointLine;
import com.revolsys.geometry.algorithm.PointLocator;
import com.revolsys.geometry.graph.linemerge.LineMerger;
import com.revolsys.geometry.model.editor.AbstractGeometryEditor;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.buffer.Buffer;
import com.revolsys.geometry.operation.buffer.BufferParameters;
import com.revolsys.geometry.operation.distance.DistanceOp;
import com.revolsys.geometry.operation.overlay.OverlayOp;
import com.revolsys.geometry.operation.overlay.snap.SnapIfNeededOverlayOp;
import com.revolsys.geometry.operation.predicate.RectangleContains;
import com.revolsys.geometry.operation.predicate.RectangleIntersects;
import com.revolsys.geometry.operation.relate.RelateOp;
import com.revolsys.geometry.operation.union.UnaryUnionOp;
import com.revolsys.geometry.operation.valid.GeometryValidationError;
import com.revolsys.geometry.operation.valid.IsValidOp;
import com.revolsys.record.io.format.wkt.EWktWriter;
import com.revolsys.util.Emptyable;
import com.revolsys.util.Pair;
import com.revolsys.util.Property;

/**
 * A representation of a planar, linear vector geometry.
 * <P>
 *
 *  <H3>Binary Predicates</H3>
 * Because it is not clear at this time
 * what semantics for spatial
 * analysis methods involving <code>GeometryCollection</code>s would be useful,
 * <code>GeometryCollection</code>s are not supported as arguments to binary
 * predicates or the <code>relate</code>
 * method.
 *
 * <H3>Overlay Methods</H3>
 *
 * The overlay methods
 * return the most specific class possible to represent the result. If the
 * result is homogeneous, a <code>Point</code>, <code>LineString</code>, or
 * <code>Polygon</code> will be returned if the result contains a single
 * element; otherwise, a <code>MultiPoint</code>, <code>MultiLineString</code>,
 * or <code>MultiPolygon</code> will be returned. If the result is
 * heterogeneous a <code>GeometryCollection</code> will be returned. <P>
 *
 * Because it is not clear at this time what semantics for set-theoretic
 * methods involving <code>GeometryCollection</code>s would be useful,
 * <code>GeometryCollections</code>
 * are not supported as arguments to the set-theoretic methods.
 *
 *  <H4>Representation of Computed Geometries </H4>
 *
 *  The SFS states that the result
 *  of a set-theoretic method is the "point-set" result of the usual
 *  set-theoretic definition of the operation (SFS 3.2.21.1). However, there are
 *  sometimes many ways of representing a point set as a <code>Geometry</code>.
 *  <P>
 *
 *  The SFS does not specify an unambiguous representation of a given point set
 *  returned from a spatial analysis method. One goal of JTS is to make this
 *  specification precise and unambiguous. JTS uses a canonical form for
 *  <code>Geometry</code>s returned from overlay methods. The canonical
 *  form is a <code>Geometry</code> which is simple and noded:
 *  <UL>
 *    <LI> Simple means that the Geometry returned will be simple according to
 *    the JTS definition of <code>isSimple</code>.
 *    <LI> Noded applies only to overlays involving <code>LineString</code>s. It
 *    means that all intersection points on <code>LineString</code>s will be
 *    present as endpoints of <code>LineString</code>s in the result.
 *  </UL>
 *  This definition implies that non-simple geometries which are arguments to
 *  spatial analysis methods must be subjected to a line-dissolve process to
 *  ensure that the results are simple.
 *
 *  <H4> Constructed Point And The Precision Model </H4>
 *
 *  The results computed by the set-theoretic methods may
 *  contain constructed points which are not present in the input <code>Geometry</code>
 *  s. These new points arise from intersections between line segments in the
 *  edges of the input <code>Geometry</code>s. In the general case it is not
 *  possible to represent constructed points exactly. This is due to the geometryFactory
 *  that the coordinates of an intersection point may contain twice as many bits
 *  of precision as the coordinates of the input line segments. In order to
 *  represent these constructed points explicitly, JTS must truncate them to fit
 *  the <code>PrecisionModel</code>. <P>
 *
 *  Unfortunately, truncating coordinates moves them slightly. Line segments
 *  which would not be coincident in the exact result may become coincident in
 *  the truncated representation. This in turn leads to "topology collapses" --
 *  situations where a computed element has a lower dimension than it would in
 *  the exact result. <P>
 *
 *  When JTS detects topology collapses during the computation of spatial
 *  analysis methods, it will throw an exception. If possible the exception will
 *  report the location of the collapse. <P>
 *
 * <h3>Geometry Equality</h3>
 *
 * There are two ways of comparing geometries for equality:
 * <b>structural equality</b> and <b>topological equality</b>.
 *
 * <h4>Structural Equality</h4>
 *
 * Structural Equality is provided by the
 * {@link #equals(2,Geometry)} method.
 * This implements a comparison based on exact, structural pointwise
 * equality.
 * The {@link #equals(Object)} is a synonym for this method,
 * to provide structural equality semantics for
 * use in Java collections.
 * It is important to note that structural pointwise equality
 * is easily affected by things like
 * ring order and component order.  In many situations
 * it will be desirable to normalize geometries before
 * comparing them (using the {@link #norm()}
 * or {@link #normalize()} methods).
 * {@link #equalsNorm(Geometry)} is provided
 * as a convenience method to compute equality over
 * normalized geometries, but it is expensive to use.
 * Finally, {@link #equalsExact(Geometry, double)}
 * allows using a tolerance value for point comparison.
 *
 *
 * <h4>Topological Equality</h4>
 *
 * Topological Equality is provided by the
 * {@link #equalsTopo(Geometry)} method.
 * It implements the SFS definition of point-set equality
 * defined in terms of the DE-9IM matrix.
 * To support the SFS naming convention, the method
 * {@link #equals(Geometry)} is also provided as a synonym.
 * However, due to the potential for confusion with {@link #equals(Object)}
 * its use is discouraged.
 * <p>
 * Since {@link #equals(Object)} and {@link #hashCode()} are overridden,
 * Geometries can be used effectively in Java collections.
 *
 *@version 1.7
 */
public interface Geometry extends BoundingBoxProxy, Cloneable, Comparable<Object>, Emptyable,
  GeometryFactoryProxy, Serializable, DataTypeProxy {
  int M = 3;

  List<String> SORTED_GEOMETRY_TYPES = Arrays.asList("Point", "MultiPoint", "LineString",
    "LinearRing", "MultiLineString", "Polygon", "MultiPolygon", "GeometryCollection");

  /**
   * Standard ordinate index values
   */
  int X = 0;

  int Y = 1;

  int Z = 2;

  /**
  *  Throws an exception if the <code>geometry</code>'s is a {@link #isHeterogeneousGeometryCollection()}.
  *
  *
  *@param  geometry   the <code>Geometry</code> to check
  *@throws  IllegalArgumentException  if <code>geometry</code>'s is a {@link #isHeterogeneousGeometryCollection()}
  */
  static void checkNotGeometryCollection(final Geometry geometry) {
    if (geometry.isHeterogeneousGeometryCollection()) {
      throw new IllegalArgumentException(
        "This method does not support GeometryCollection arguments");
    }
  }

  /**
  *  Returns the first non-zero result of <code>compareTo</code> encountered as
  *  the two <code>Collection</code>s are iterated over. If, by the time one of
  *  the iterations is complete, no non-zero result has been encountered,
  *  returns 0 if the other iteration is also complete. If <code>b</code>
  *  completes before <code>a</code>, a positive number is returned; if a
  *  before b, a negative number.
  *
  *@param  a  a <code>Collection</code> of <code>Comparable</code>s
  *@param  b  a <code>Collection</code> of <code>Comparable</code>s
  *@return    the first non-zero <code>compareTo</code> result, if any;
  *      otherwise, zero
  */
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  static int compare(final Collection a, final Collection b) {
    final Iterator i = a.iterator();
    final Iterator j = b.iterator();
    while (i.hasNext() && j.hasNext()) {
      final Comparable aElement = (Comparable)i.next();
      final Comparable bElement = (Comparable)j.next();
      final int comparison = aElement.compareTo(bElement);
      if (comparison != 0) {
        return comparison;
      }
    }
    if (i.hasNext()) {
      return 1;
    }
    if (j.hasNext()) {
      return -1;
    }
    return 0;
  }

  static boolean equalsExact(final Object geometry1, final Object geometry2) {
    return ((Geometry)geometry1).equalsExact((Geometry)geometry2);
  }

  static int getGeometryCount(final Iterable<Geometry> geometries) {
    int totalGeometryCount = 0;
    for (final Geometry geometry : geometries) {
      final int geometryCount = geometry.getGeometryCount();
      totalGeometryCount += geometryCount;
    }
    return totalGeometryCount;
  }

  static int getVertexIndex(final int[] index) {
    final int length = index.length;
    final int lastIndex = length - 1;
    return index[lastIndex];
  }

  /**
   * Returns true if the array contains any non-empty <code>Geometry</code>s.
   *
   *@param  geometries  an array of <code>Geometry</code>s; no elements may be
   *      <code>null</code>
   *@return             <code>true</code> if any of the <code>Geometry</code>s
   *      <code>isEmpty</code> methods return <code>false</code>
   */
  static boolean hasNonEmptyElements(final Geometry... geometries) {
    for (final Geometry geometry : geometries) {
      if (!geometry.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  /**
   *  Returns true if the array contains any <code>null</code> elements.
   *
   *@param  array  an array to validate
   *@return        <code>true</code> if any of <code>array</code>s elements are
   *      <code>null</code>
   */
  public static boolean hasNullElements(final Object[] array) {
    for (final Object element : array) {
      if (element == null) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests whether any representative of the target geometry
   * intersects the test geometry.
   * This is useful in A/A, A/L, A/P, L/P, and P/P cases.
   * @param geometry TODO
   * @param geom the test geometry
   * @param repPts the representative points of the target geometry
   *
   * @return true if any component intersects the areal test geometry
   */
  static boolean isAnyTargetComponentInTest(final Geometry geometry, final Geometry testGeom) {
    final PointLocator locator = new PointLocator();
    for (final Vertex vertex : geometry.vertices()) {
      final boolean intersects = locator.intersects(vertex, testGeom);
      if (intersects) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  static <G extends Geometry> G newGeometry(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Geometry) {
      return (G)value;
    } else {
      final String string = DataTypes.toString(value);
      return GeometryFactory.DEFAULT_3D.geometry(string, false);
    }
  }

  static int[] newVertexId(final int[] partId, final int vertexIndex) {
    final int[] vertexId = new int[partId.length + 1];
    System.arraycopy(partId, 0, vertexId, 0, partId.length);
    vertexId[partId.length] = vertexIndex;
    return vertexId;
  }

  static int[] setVertexIndex(final int[] vertexId, final int vertexIndex) {
    final int length = vertexId.length;
    final int lastIndex = length - 1;
    final int[] newVertextId = new int[length];
    System.arraycopy(vertexId, 0, newVertextId, 0, lastIndex);
    newVertextId[lastIndex] = vertexIndex;
    return newVertextId;
  }

  default boolean addIsSimpleErrors(final List<GeometryValidationError> errors,
    final boolean shortCircuit) {
    return true;
  }

  default void applyCoordinatesOperation(final CoordinatesOperation operation,
    final BiConsumerDouble action) {
    final CoordinatesOperationPoint point = new CoordinatesOperationPoint();
    forEachVertex((x, y) -> {
      point.setPoint(x, y);
      operation.perform(point);
      point.apply2d(action);
    });
  }

  @SuppressWarnings("unchecked")
  default <GIN extends Geometry, GRET extends Geometry> GRET applyGeometry(
    final Function<? super GIN, ? super Geometry> function) {
    if (!isEmpty()) {
      return (GRET)function.apply((GIN)this);
    }
    return (GRET)this;
  }

  /**
   * If this geometry factory's coordinate system requires conversion ({@link GeometryFactory#isProjectionRequired(GeometryFactory)})
   * then return a new 2d geometry converted to the target geometry factory.
   *
   * @param geometryFactory The target geometry factory
   * @return This geometry or converted geometry.
   * @see GeometryFactory#isProjectionRequired(GeometryFactory)
   */
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V as2d(final GeometryFactory geometryFactory) {
    if (isProjectionRequired(geometryFactory)) {
      return (V)newGeometry(geometryFactory);
    } else {
      return (V)this;
    }
  }

  /**
   * If this geometry factory's coordinate system requires conversion ({@link GeometryFactory#isProjectionRequired(GeometryFactory)})
   * then return a new 2d geometry converted to the target geometry factory.
   *
   * @param geometryFactory The target geometry factory
   * @return This geometry or converted geometry.
   * @see GeometryFactory#isProjectionRequired(GeometryFactory)
   */
  default <V extends Geometry> V as2d(final GeometryFactoryProxy geometryFactoryProxy) {
    final GeometryFactory geometryFactory = geometryFactoryProxy.getGeometryFactory();
    return as2d(geometryFactory);
  }

  /**
   * Computes a buffer area around this geometry having the given width. The
   * buffer of a Geometry is the Minkowski sum or difference of the geometry
   * with a disc of radius <code>abs(distance)</code>.
   * <p>
   * Mathematically-exact buffer area boundaries can contain circular arcs.
   * To represent these arcs using linear geometry they must be approximated with line segments.
   * The buffer geometry is constructed using 8 segments per quadrant to approximate
   * the circular arcs.
   * The end cap style is <code>CAP_ROUND</code>.
   * <p>
   * The buffer operation always returns a polygonal result. The negative or
   * zero-distance buffer of lines and points is always an empty {@link Polygon}.
   * This is also the result for the buffers of degenerate (zero-area) polygons.
   *
   * @param distance
   *          the width of the buffer (may be positive, negative or 0)
   * @return a polygonal geometry representing the buffer region (which may be
   *         empty)
   *
   * @throws TopologyException
   *           if a robustness error occurs
   *
   * @see #buffer(double, int)
   * @see #buffer(double, int, int)
   */

  default Polygonal buffer(final double distance) {
    return buffer(distance, BufferParameters.DEFAULT_QUADRANT_SEGMENTS, LineCap.ROUND,
      LineJoin.ROUND, BufferParameters.DEFAULT_MITRE_LIMIT);
  }

  default <G extends Polygonal> G buffer(final double distance, final BufferParameters parameters) {
    return Buffer.buffer(this, distance, parameters);
  }

  /**
   * Computes a buffer area around this geometry having the given width and with
   * a specified accuracy of approximation for circular arcs.
   * <p>
   * Mathematically-exact buffer area boundaries can contain circular arcs.
   * To represent these arcs
   * using linear geometry they must be approximated with line segments. The
   * <code>quadrantSegments</code> argument allows controlling the accuracy of
   * the approximation by specifying the number of line segments used to
   * represent a quadrant of a circle
   * <p>
   * The buffer operation always returns a polygonal result. The negative or
   * zero-distance buffer of lines and points is always an empty {@link Polygon}.
   * This is also the result for the buffers of degenerate (zero-area) polygons.
   *
   * @param distance
   *          the width of the buffer (may be positive, negative or 0)
   * @param quadrantSegments
   *          the number of line segments used to represent a quadrant of a
   *          circle
   * @return a polygonal geometry representing the buffer region (which may be
   *         empty)
   *
   * @throws TopologyException
   *           if a robustness error occurs
   *
   * @see #buffer(double)
   * @see #buffer(double, int, int)
   */

  default Polygonal buffer(final double distance, final int quadrantSegments) {
    return buffer(distance, quadrantSegments, LineCap.ROUND, LineJoin.ROUND,
      BufferParameters.DEFAULT_MITRE_LIMIT);
  }

  /**
   * Computes a buffer area around this geometry having the given
   * width and with a specified accuracy of approximation for circular arcs,
   * and using a specified end cap style.
   * <p>
   * Mathematically-exact buffer area boundaries can contain circular arcs.
   * To represent these arcs using linear geometry they must be approximated with line segments.
   * The <code>quadrantSegments</code> argument allows controlling the
   * accuracy of the approximation
   * by specifying the number of line segments used to represent a quadrant of a circle
   * <p>
   * The end cap style specifies the buffer geometry that will be
   * created at the ends of linestrings.  The styles provided are:
   * <ul>
   * <li><code>{@link LineCap#ROUND}</code> - (default) a semi-circle
   * <li><code>{@link LineCap#BUTT}</code> - a straight line perpendicular to the end segment
   * <li><code>{@link LineCap#SQUARE}</code> - a half-square
   * </ul>
   * <p>
   * The buffer operation always returns a polygonal result. The negative or
   * zero-distance buffer of lines and points is always an empty {@link Polygon}.
   * This is also the result for the buffers of degenerate (zero-area) polygons.
   *
   *@param  distance  the width of the buffer (may be positive, negative or 0)
   *@param quadrantSegments the number of line segments used to represent a quadrant of a circle
   *@param endCapStyle the end cap style to use
   *@return a polygonal geometry representing the buffer region (which may be empty)
   *
   * @throws TopologyException if a robustness error occurs
   *
   * @see #buffer(double)
   * @see #buffer(double, int)
   */

  default Polygonal buffer(final double distance, final int quadrantSegments,
    final LineCap endCapStyle) {
    return buffer(distance, quadrantSegments, endCapStyle, LineJoin.ROUND,
      BufferParameters.DEFAULT_MITRE_LIMIT);
  }

  default <G extends Polygonal> G buffer(final double distance, final int quadrantSegments,
    final LineCap endCapStyle, final LineJoin joinStyle, final double mitreLimit) {
    final BufferParameters parameters = new BufferParameters(quadrantSegments, endCapStyle,
      joinStyle, mitreLimit);
    return buffer(distance, parameters);
  }

  Geometry clone();

  default int compareTo(final Geometry geometry) {
    if (getClassSortIndex() != geometry.getClassSortIndex()) {
      return getClassSortIndex() - geometry.getClassSortIndex();
    } else if (isEmpty() && geometry.isEmpty()) {
      return 0;
    } else if (isEmpty()) {
      return -1;
    } else if (geometry.isEmpty()) {
      return 1;
    } else {
      return compareToSameClass(geometry);
    }
  }

  /**
   *  Returns whether this <code>Geometry</code> is greater than, equal to,
   *  or less than another <code>Geometry</code>. <P>
   *
   *  If their classes are different, they are compared using the following
   *  ordering:
   *  <UL>
   *    <LI> Point (lowest)
   *    <LI> MultiPoint
   *    <LI> LineString
   *    <LI> LinearRing
   *    <LI> MultiLineString
   *    <LI> Polygon
   *    <LI> MultiPolygon
   *    <LI> GeometryCollection (highest)
   *  </UL>
   *  If the two <code>Geometry</code>s have the same class, their first
   *  elements are compared. If those are the same, the second elements are
   *  compared, etc.
   *
   *@param  other  a <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return    a positive number, 0, or a negative number, depending on whether
   *      this object is greater than, equal to, or less than <code>o</code>, as
   *      defined in "Normal Form For Geometry" in the JTS Technical
   *      Specifications
   */

  @Override
  default int compareTo(final Object other) {
    if (other instanceof Geometry) {
      final Geometry geometry = (Geometry)other;
      return compareTo(geometry);
    } else {
      return -1;
    }
  }

  /**
   *  Returns whether this <code>Geometry</code> is greater than, equal to,
   *  or less than another <code>Geometry</code> having the same class.
   *
   *@param  o  a <code>Geometry</code> having the same class as this <code>Geometry</code>
   *@return    a positive number, 0, or a negative number, depending on whether
   *      this object is greater than, equal to, or less than <code>o</code>, as
   *      defined in "Normal Form For Geometry" in the JTS Technical
   *      Specifications
   */
  int compareToSameClass(Geometry o);

  /**
   * Check that geom is not contained entirely in the rectangle boundary.
   * According to the somewhat odd spec of the SFS, if this
   * is the case the geometry is NOT contained.
   */
  default boolean containedBy(final BoundingBox boundingBox) {
    if (boundingBox.bboxCovers(this)) {
      if (isContainedInBoundary(boundingBox)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  default boolean contains(final double x, final double y) {
    return false;
  }

  /**
   * Tests whether this geometry contains the
   * argument geometry.
   * <p>
   * The <code>contains</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of the other geometry is a point of this geometry,
   * and the interiors of the two geometries have at least one point in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * the pattern
   * <code>[T*****FF*]</code>
   * <li><code>g.within(this) = true</code>
   * <br>(<code>contains</code> is the converse of {@link #within} )
   * </ul>
   * An implication of the definition is that "Geometries do not
   * contain their boundary".  In other words, if a geometry A is a subset of
   * the points in the boundary of a geometry B, <code>B.contains(A) = false</code>.
   * (As a concrete example, take A to be a LineString which lies in the boundary of a Polygon B.)
   * For a predicate with similar behaviour but avoiding
   * this subtle limitation, see {@link #covers}.
   *
   *@param  geometry  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if this <code>Geometry</code> contains <code>g</code>
   *
   * @see Geometry#within
   * @see Geometry#covers
   */

  default boolean contains(final Geometry geometry) {
    if (bboxCovers(geometry)) {
      // optimization for rectangle arguments
      if (isRectangle()) {
        return RectangleContains.contains((Polygon)this, geometry);
      }
      // general case
      return relate(geometry).isContains();
    } else {
      return false;
    }
  }

  default boolean containsProperly(final Geometry geometry) {
    if (bboxCovers(geometry)) {
      return relate(geometry, "T**FF*FF*");
    } else {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  default <G extends Geometry> G convertAxisCount(final int axisCount) {
    final int axisCountThis = getAxisCount();
    if (axisCountThis > axisCount) {
      GeometryFactory geometryFactory = getGeometryFactory();
      geometryFactory = geometryFactory.convertAxisCount(axisCount);
      return (G)geometryFactory.geometry(this);
    } else {
      return (G)this;
    }
  }

  @SuppressWarnings("unchecked")
  default <V extends Geometry> V convertGeometry(final GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (geometryFactory == null || sourceGeometryFactory == geometryFactory) {
      return (V)this;
    } else {
      return (V)newGeometry(geometryFactory);
    }
  }

  @SuppressWarnings("unchecked")
  default <V extends Geometry> V convertGeometry(GeometryFactory targetGeometryFactory,
    final int targetAxisCount) {
    if (targetGeometryFactory != null) {
      targetGeometryFactory = targetGeometryFactory.convertAxisCount(targetAxisCount);
    }
    boolean copy = false;
    if (targetGeometryFactory != null) {
      final GeometryFactory sourceGeometryFactory = getGeometryFactory();
      if (sourceGeometryFactory != targetGeometryFactory) {
        if (sourceGeometryFactory.hasSameCoordinateSystem(targetGeometryFactory)) {
          if (!targetGeometryFactory.isFloating()) {
            final int sourceAxisCount = sourceGeometryFactory.getAxisCount();
            final int minAxisCount = Math.min(sourceAxisCount, targetAxisCount);
            for (int axisIndex = 0; axisIndex < minAxisCount; axisIndex++) {
              final double sourceScale = sourceGeometryFactory.getScale(axisIndex);
              final double targetScale = targetGeometryFactory.getScale(axisIndex);
              if (!Doubles.equal(sourceScale, targetScale)) {
                copy = true;
              }
            }
          }
        } else {
          copy = true;
        }
      }
    }
    if (copy) {
      return (V)newGeometry(targetGeometryFactory);
    } else {
      return (V)this;
    }
  }

  default <V extends Geometry> V convertScales(final double... scales) {
    GeometryFactory geometryFactory = getGeometryFactory();
    geometryFactory = geometryFactory.convertScales(scales);
    return convertGeometry(geometryFactory);
  }

  /**
   *  Computes the smallest convex <code>Polygon</code> that contains all the
   *  points in the <code>Geometry</code>. This obviously applies only to <code>Geometry</code>
   *  s which contain 3 or more points; the results for degenerate cases are
   *  specified as follows:
   *  <TABLE>
   *    <TR>
   *      <TH>    Number of <code>Point</code>s in argument <code>Geometry</code>   </TH>
   *      <TH>    <code>Geometry</code> class of result     </TH>
   *    </TR>
   *    <TR>
   *      <TD>        0      </TD>
   *      <TD>        empty <code>GeometryCollection</code>      </TD>
   *    </TR>
   *    <TR>  <TD>      1     </TD>
   *      <TD>     <code>Point</code>     </TD>
   *    </TR>
   *    <TR>
   *      <TD>      2     </TD>
   *      <TD>     <code>LineString</code>     </TD>
   *    </TR>
   *    <TR>
   *      <TD>       3 or more     </TD>
   *      <TD>      <code>Polygon</code>     </TD>
   *    </TR>
   *  </TABLE>
   *
   *@return    the minimum-area convex polygon containing this <code>Geometry</code>'
   *      s points
   */
  default Geometry convexHull() {
    return ConvexHull.convexHull(this);
  }

  /**
   * Tests whether this geometry is covered by the
   * argument geometry.
   * <p>
   * The <code>coveredBy</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of this geometry is a point of the other geometry.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the following patterns:
   *  <ul>
   *   <li><code>[T*F**F***]</code>
   *   <li><code>[*TF**F***]</code>
   *   <li><code>[**FT*F***]</code>
   *   <li><code>[**F*TF***]</code>
   *  </ul>
   * <li><code>g.covers(this) = true</code>
   * <br>(<code>coveredBy</code> is the converse of {@link #covers})
   * </ul>
   * If either geometry is empty, the value of this predicate is <code>false</code>.
   * <p>
   * This predicate is similar to {@link #within},
   * but is more inclusive (i.e. returns <code>true</code> for more cases).
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if this <code>Geometry</code> is covered by <code>g</code>
   *
   * @see Geometry#within
   * @see Geometry#covers
   */

  default boolean coveredBy(final Geometry g) {
    return g.covers(this);
  }

  /**
   * Tests whether this geometry covers the
   * argument geometry.
   * <p>
   * The <code>covers</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>Every point of the other geometry is a point of this geometry.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the following patterns:
   *  <ul>
   *   <li><code>[T*****FF*]</code>
   *   <li><code>[*T****FF*]</code>
   *   <li><code>[***T**FF*]</code>
   *   <li><code>[****T*FF*]</code>
   *  </ul>
   * <li><code>g.coveredBy(this) = true</code>
   * <br>(<code>covers</code> is the converse of {@link #coveredBy})
   * </ul>
   * If either geometry is empty, the value of this predicate is <code>false</code>.
   * <p>
   * This predicate is similar to {@link #contains},
   * but is more inclusive (i.e. returns <code>true</code> for more cases).
   * In particular, unlike <code>contains</code> it does not distinguish between
   * points in the boundary and in the interior of geometries.
   * For most situations, <code>covers</code> should be used in preference to <code>contains</code>.
   * As an added benefit, <code>covers</code> is more amenable to optimization,
   * and hence should be more performant.
   *
   *@param  geometry  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if this <code>Geometry</code> covers <code>g</code>
   *
   * @see Geometry#contains
   * @see Geometry#coveredBy
   */

  default boolean covers(final Geometry geometry) {
    // short-circuit test
    if (!bboxCovers(geometry)) {
      return false;
    }
    // optimization for rectangle arguments
    if (isRectangle()) {
      // since we have already tested that the test boundingBox is covered
      return true;
    }
    return relate(geometry).isCovers();
  }

  default boolean covers(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return contains(x, y);
  }

  /**
   * Tests whether this geometry crosses the
   * argument geometry.
   * <p>
   * The <code>crosses</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have some but not all interior points in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * one of the following patterns:
   *   <ul>
   *    <li><code>[T*T******]</code> (for P/L, P/A, and L/A situations)
   *    <li><code>[T*****T**]</code> (for L/P, A/P, and A/L situations)
   *    <li><code>[0********]</code> (for L/L situations)
   *   </ul>
   * </ul>
   * For any other combination of dimensions this predicate returns <code>false</code>.
   * <p>
   * The SFS defined this predicate only for P/L, P/A, L/L, and L/A situations.
   * In order to make the relation symmetric,
   * JTS extends the definition to apply to L/P, A/P and A/L situations as well.
   *
   *@param  geometry  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s cross.
   */

  default boolean crosses(final Geometry geometry) {
    if (bboxIntersects(geometry)) {
      final IntersectionMatrix matrix = relate(geometry);
      final Dimension dimension1 = getDimension();
      final Dimension dimension2 = geometry.getDimension();
      return matrix.isCrosses(dimension1, dimension2);
    } else {
      return false;
    }
  }

  /**
   * Computes a <code>Geometry</code> representing the closure of the point-set
   * of the points contained in this <code>Geometry</code> that are not contained in
   * the <code>other</code> Geometry.
   * <p>
   * If the result is empty, it is an atomic geometry
   * with the dimension of the left-hand input.
   * <p>{@link #isHeterogeneousGeometryCollection()} arguments are not supported.
   *
   *@param  other  the <code>Geometry</code> with which to compute the
   *      difference
   *@return a Geometry representing the point-set difference of this <code>Geometry</code> with
   *      <code>other</code>
   * @throws TopologyException if a robustness error occurs
   * @throws IllegalArgumentException if either input is a non-empty GeometryCollection
   */

  default Geometry difference(final Geometry other) {
    // special case: if A.isEmpty ==> empty; if B.isEmpty ==> A
    if (this.isEmpty()) {
      return OverlayOp.newEmptyResult(OverlayOp.DIFFERENCE, this, other, getGeometryFactory());
    } else if (other.isEmpty()) {
      return clone();
    } else {
      checkNotGeometryCollection(this);
      checkNotGeometryCollection(other);
      return SnapIfNeededOverlayOp.overlayOp(this, other, OverlayOp.DIFFERENCE);
    }
  }

  /**
   * Tests whether this geometry is disjoint from the argument geometry.
   * <p>
   * The <code>disjoint</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The two geometries have no point in common
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * <code>[FF*FF****]</code>
   * <li><code>! g.intersects(this) = true</code>
   * <br>(<code>disjoint</code> is the inverse of <code>intersects</code>)
   * </ul>
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s are
   *      disjoint
   *
   * @see Geometry#intersects
   */

  default boolean disjoint(final Geometry g) {
    return !intersects(g);
  }

  /**
   *  Returns the minimum distance between this <code>Geometry</code>
   *  and another <code>Geometry</code>.
   *
   * @param  geometry the <code>Geometry</code> from which to compute the distance
   * @return the distance between the geometries or 0 if either input geometry is empty
   * @throws IllegalArgumentException if g is null
   */

  default double distanceGeometry(final Geometry geometry) {
    return distanceGeometry(geometry, 0.0);
  }

  /**
  *  Returns the minimum distance between this <code>Geometry</code>
  *  and another <code>Geometry</code>.
  *
  * @param  geometry the <code>Geometry</code> from which to compute the distance
  * @return the distance between the geometries or 0 if either input geometry is empty
  * @throws IllegalArgumentException if g is null
  */

  default double distanceGeometry(Geometry geometry, final double terminateDistance) {
    if (isEmpty()) {
      return Double.POSITIVE_INFINITY;
    } else if (Property.isEmpty(geometry)) {
      return Double.POSITIVE_INFINITY;
    } else if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return distancePoint(point, terminateDistance);
    } else {
      geometry = geometry.as2d(this);
      final DistanceOp distOp = new DistanceOp(this, geometry, terminateDistance);
      final double distance = distOp.distance();
      return distance;
    }
  }

  default double distanceLine(final LineString line) {
    return distanceGeometry(line, 0.0);
  }

  /**
   *  Returns the minimum distance between this <code>Geometry</code>
   *  and another {@link Point}.
   *
   * @param  x the x coordinate from which to compute the distance
   * @param  y the y coordinate from which to compute the distance
   * @return the distance between the geometries or 0 if either input geometry is empty
   * @throws IllegalArgumentException if g is null
   */
  default double distancePoint(final double x, final double y) {
    return distancePoint(x, y, 0.0);
  }

  /**
   *  Returns the minimum distance between this <code>Geometry</code>
   *  and another {@link Point}.
   *
   * @param  x the x coordinate from which to compute the distance
   * @param  y the y coordinate from which to compute the distance
   * @return the distance between the geometries or 0 if either input geometry is empty
   * @throws IllegalArgumentException if g is null
   */

  double distancePoint(double x, double y, final double terminateDistance);

  /**
   *  Returns the minimum distance between this <code>Geometry</code>
   *  and another {@link Point}.
   *
   * @param  point the point from which to compute the distance
   * @return the distance between the geometries or 0 if either input geometry is empty
   * @throws IllegalArgumentException if g is null
   */
  default double distancePoint(final Point point) {
    return distancePoint(point, 0.0);
  }

  /**
   *  Returns the minimum distance between this <code>Geometry</code>
   *  and another {@link Point}.
   *
   * @param  point the point from which to compute the distance
   * @return the distance between the geometries or 0 if either input geometry is empty
   * @throws IllegalArgumentException if g is null
   */

  default double distancePoint(Point point, final double terminateDistance) {
    if (isEmpty()) {
      return Double.POSITIVE_INFINITY;
    } else if (Property.isEmpty(point)) {
      return Double.POSITIVE_INFINITY;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.convertPoint2d(geometryFactory);
      final double x = point.getX();
      final double y = point.getY();
      return distancePoint(x, y, terminateDistance);
    }
  }

  @SuppressWarnings("unchecked")
  default <G extends Geometry, GEIN extends GeometryEditor<?>, GEOUT extends GeometryEditor<?>> G edit(
    final Function<GEIN, GEOUT> edit) {
    final GEIN editor = (GEIN)newGeometryEditor();
    final GEOUT newEditor = edit.apply(editor);
    if (newEditor.isModified()) {
      return (G)newEditor.newGeometry();
    } else {
      return (G)this;
    }
  }

  default boolean equal(final Point a, final Point b, final double tolerance) {
    if (tolerance == 0) {
      return a.equals(b);
    } else {
      return a.distancePoint(b) <= tolerance;
    }
  }

  /**
   * Tests whether this geometry is
   * topologically equal to the argument geometry.
   * <p>
   * This method is included for backward compatibility reasons.
   * It has been superseded by the {@link #equalsTopo(Geometry)} method,
   * which has been named to clearly denote its functionality.
   * <p>
   * This method should NOT be confused with the method
   * {@link #equals(Object)}, which implements
   * an exact equality comparison.
   *
   *@param  geometry  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return true if the two <code>Geometry</code>s are topologically equal
   *
   *@see #equalsTopo(Geometry)
   */

  default boolean equals(final Geometry geometry) {
    if (geometry == null || geometry.isEmpty()) {
      return false;
    } else {
      return equalsTopo(geometry);
    }
  }

  boolean equals(int axisCount, Geometry geometry);

  default boolean equalsExact(final Geometry geometry) {
    if (geometry == null) {
      return false;
    } else {
      final int axisCount = getAxisCount();
      final int axisCount2 = geometry.getAxisCount();
      if (axisCount == axisCount2) {
        final int srid = getHorizontalCoordinateSystemId();
        final int otherSrid = geometry.getHorizontalCoordinateSystemId();
        if (srid == 0 || otherSrid == 0 || srid == otherSrid) {
          return equals(axisCount, geometry);
        }
      }
    }
    return false;
  }

  /**
  * Returns true if the two <code>Geometry</code>s are exactly equal,
  * up to a specified distance tolerance.
  * Two Geometries are exactly equal within a distance tolerance
  * if and only if:
  * <ul>
  * <li>they have the same structure
  * <li>they have the same values for their vertices,
  * within the given tolerance distance, in exactly the same order.
  * </ul>
  * This method does <i>not</i>
  * test the values of the <code>GeometryFactory</code>, the <code>SRID</code>.
  * <p>
  * To properly test equality between different geometries,
  * it is usually necessary to {@link #normalize()} them first.
  *
  * @param other the <code>Geometry</code> with which to compare this <code>Geometry</code>
  * @param tolerance distance at or below which two <code>Coordinate</code>s
  *   are considered equal
  * @return <code>true</code> if this and the other <code>Geometry</code>
  *   have identical structure and point values, up to the distance tolerance.
  *
  * @see #equals(2,Geometry)
  * @see #normalize()
  */
  boolean equalsExact(Geometry other, double tolerance);

  default boolean equalsExactNormalize(final Geometry geometry) {
    if (geometry == null) {
      return false;
    } else {
      final Geometry geometry1 = normalize();
      final Geometry geometry2 = geometry.normalize();
      return geometry1.equalsExact(geometry2);
    }
  }

  /**
   * Tests whether two geometries are exactly equal
   * in their normalized forms.
   * This is a convenience method which creates normalized
   * versions of both geometries before computing
   * {@link #equals(2,Geometry)}.
   * <p>
   * This method is relatively expensive to compute.
   * For maximum performance, the client
   * should instead perform normalization on the individual geometries
   * at an appropriate point during processing.
   *
   * @param g a Geometry
   * @return true if the input geometries are exactly equal in their normalized form
   */

  default boolean equalsNorm(final Geometry g) {
    if (g == null) {
      return false;
    }
    return normalize().equals(2, g.normalize());
  }

  /**
   * Tests whether this geometry is topologically equal to the argument geometry
   * as defined by the SFS <code>equals</code> predicate.
   * <p>
   * The SFS <code>equals</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The two geometries have at least one point in common,
   * and no point of either geometry lies in the exterior of the other geometry.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * the pattern <code>T*F**FFF*</code>
   * <pre>
   * T*F
   * **F
   * FF*
   * </pre>
   * </ul>
   * <b>Note</b> that this method computes <b>topologically equality</b>.
   * For structural equality, see {@link #equals(2,Geometry)}.
   *
   *@param geometry the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return <code>true</code> if the two <code>Geometry</code>s are topologically equal
   *
   *@see #equals(2,Geometry)
   */

  default boolean equalsTopo(final Geometry geometry) {
    // short-circuit test
    if (bboxEquals(geometry)) {
      final IntersectionMatrix relate = relate(geometry);
      final Dimension dimension1 = getDimension();
      final Dimension dimension2 = geometry.getDimension();
      return relate.isEquals(dimension1, dimension2);
    } else {
      return false;
    }
  }

  default Pair<GeometryComponent, Double> findClosestGeometryComponent(final double x,
    final double y) {
    if (isEmpty()) {
      return new Pair<>();
    } else {
      GeometryComponent closestComponent = null;
      double closestDistance = Double.POSITIVE_INFINITY;
      for (final Segment segment : segments()) {
        if (segment.isLineStart()) {
          final Vertex from = segment.getGeometryVertex(0);
          if (from.equalsVertex(x, y)) {
            return new Pair<>(from, 0.0);
          } else {
            final double fromDistance = from.distancePoint(x, y);
            if (fromDistance < closestDistance || //
              fromDistance == closestDistance && !(closestComponent instanceof Vertex)) {
              closestDistance = fromDistance;
              closestComponent = from.clone();
            }
          }
        }
        {
          final Vertex to = segment.getGeometryVertex(1);
          if (to.equalsVertex(x, y)) {
            return new Pair<>(to, 0.0);
          } else {
            final double toDistance = to.distancePoint(x, y);
            if (toDistance < closestDistance || //
              toDistance == closestDistance && !(closestComponent instanceof Vertex)) {
              closestDistance = toDistance;
              closestComponent = to.clone();
            }
          }
        }
        {
          final double segmentDistance = segment.distancePoint(x, y);
          if (segmentDistance == 0) {
            return new Pair<>(segment, 0.0);
          } else if (segmentDistance < closestDistance || //
            segmentDistance == closestDistance && !(closestComponent instanceof Vertex)) {
            closestDistance = segmentDistance;
            closestComponent = segment.clone();
          }
        }
      }
      if (Double.isFinite(closestDistance)) {
        return new Pair<>(closestComponent, closestDistance);
      } else {
        return new Pair<>();
      }
    }
  }

  default Pair<GeometryComponent, Double> findClosestGeometryComponent(final double x,
    final double y, final double maxDistance) {
    if (isEmpty()) {
      return new Pair<>();
    } else {
      GeometryComponent closestComponent = null;
      double closestDistance = Double.POSITIVE_INFINITY;
      for (final Segment segment : segments()) {
        boolean matched = false;
        if (segment.isLineStart()) {
          final Vertex from = segment.getGeometryVertex(0);
          if (from.equalsVertex(x, y)) {
            return new Pair<>(from, 0.0);
          } else {
            final double fromDistance = from.distancePoint(x, y);
            if (fromDistance <= maxDistance) {
              if (fromDistance < closestDistance || //
                fromDistance == closestDistance && !(closestComponent instanceof Vertex)) {
                closestDistance = fromDistance;
                closestComponent = from.clone();
                matched = true;
              }
            }
          }
        }
        {
          final Vertex to = segment.getGeometryVertex(1);
          if (to.equalsVertex(x, y)) {
            return new Pair<>(to, 0.0);
          } else {
            final double toDistance = to.distancePoint(x, y);
            if (toDistance <= maxDistance) {
              if (toDistance < closestDistance || //
                toDistance == closestDistance && !(closestComponent instanceof Vertex)) {
                closestDistance = toDistance;
                closestComponent = to.clone();
                matched = true;
              }
            }
          }
        }
        if (!matched) {
          final double segmentDistance = segment.distancePoint(x, y);
          if (segmentDistance == 0) {
            return new Pair<>(segment, 0.0);
          } else if (segmentDistance <= maxDistance) {
            if (segmentDistance < closestDistance || //
              segmentDistance == closestDistance && !(closestComponent instanceof Vertex)) {
              closestDistance = segmentDistance;
              closestComponent = segment.clone();
            }
          }
        }
      }
      if (Double.isFinite(closestDistance)) {
        return new Pair<>(closestComponent, closestDistance);
      } else {
        return new Pair<>();
      }
    }
  }

  default Pair<GeometryComponent, Double> findClosestGeometryComponent(final Point point) {
    return findClosestGeometryComponent(point, Double.POSITIVE_INFINITY);
  }

  default Pair<GeometryComponent, Double> findClosestGeometryComponent(Point point,
    final double maxDistance) {
    if (point.isEmpty()) {
      return new Pair<>();
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory().toFloating2d();
      point = point.convertGeometry(geometryFactory);
      final double x = point.getX();
      final double y = point.getY();
      return findClosestGeometryComponent(x, y, maxDistance);
    }
  }

  /**
   * First the vertex or segment that is within the specified maxDistance. If a vertex is within the
   * distance, then it is returned, even if a segment is closer.
   * @param x
   * @param y
   * @param maxDistance
   * @return
   */
  default Pair<GeometryComponent, Double> findGeometryComponentWithinDistance(final double x,
    final double y, final double maxDistance) {
    if (isEmpty()) {
      return new Pair<>();
    } else {
      GeometryComponent closestComponent = null;
      double closestDistance = Double.POSITIVE_INFINITY;
      for (final Segment segment : segments()) {
        boolean matched = false;
        if (segment.isLineStart()) {
          final Vertex from = segment.getGeometryVertex(0);
          final double fromDistance = from.distancePoint(x, y);
          if (fromDistance == 0) {
            return new Pair<>(from, 0.0);
          } else if (fromDistance < maxDistance) {
            if (fromDistance < closestDistance || //
              fromDistance == closestDistance && !(closestComponent instanceof Vertex)) {
              closestDistance = fromDistance;
              closestComponent = from;
              matched = true;
            }
          }
        }

        {
          final Vertex to = segment.getGeometryVertex(1);
          final double toDistance = to.distancePoint(x, y);
          if (toDistance == 0) {
            return new Pair<>(to, 0.0);
          } else if (toDistance < maxDistance) {
            if (toDistance < closestDistance || //
              toDistance == closestDistance && !(closestComponent instanceof Vertex)) {
              closestDistance = toDistance;
              closestComponent = to.clone();
              matched = true;
            }
          }
        }
        if (!matched && !(closestComponent instanceof Vertex)) {
          final double segmentDistance = segment.distancePoint(x, y);
          if (segmentDistance == 0) {
            return new Pair<>(segment, 0.0);
          } else {
            if (segmentDistance < maxDistance) {
              if (!(closestComponent instanceof Vertex) && segmentDistance < closestDistance) {
                closestDistance = segmentDistance;
                closestComponent = segment.clone();
              }
            }
          }
        }
      }
      if (Double.isFinite(closestDistance)) {
        return new Pair<>(closestComponent, closestDistance);
      } else {
        return new Pair<>();
      }
    }
  }

  default <R> R findSegment(final Function4Double<R> action) {
    return null;
  }

  /**
   * Iterate through all the vertices of the geometry and call the supplied action.
   * If the action returns a value other than null the iteration will stop and that value returned.
  *
    * @param action
   * @return The first non-null result
   */
  default <R> R findVertex(final BiFunctionDouble<R> action) {
    return null;
  }

  default void forEachGeometry(final Consumer<Geometry> action) {
    action.accept(this);
  }

  @SuppressWarnings("unchecked")
  default <G extends Geometry> void forEachGeometryComponent(final Class<G> geometryClass,
    final Consumer<G> action) {
    if (geometryClass.isAssignableFrom(getClass())) {
      action.accept((G)this);
    }
  }

  default void forEachPolygon(final Consumer<Polygon> action) {
  }

  default void forEachSegment(final Consumer4Double action) {
  }

  default void forEachVertex(final BiConsumerDouble action) {
  }

  default void forEachVertex(final Consumer<CoordinatesOperationPoint> action) {
    if (!isEmpty()) {
      final CoordinatesOperationPoint point = new CoordinatesOperationPoint();
      forEachVertex(point, action);
    }
  }

  default void forEachVertex(final Consumer3Double action) {
  }

  default void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final CoordinatesOperationPoint point, final Consumer<CoordinatesOperationPoint> action) {
  }

  default void forEachVertex(final CoordinatesOperationPoint coordinates,
    final Consumer<CoordinatesOperationPoint> action) {
  }

  default void forEachVertex(final GeometryFactory geometryFactory,
    final Consumer<CoordinatesOperationPoint> action) {
    if (!isEmpty()) {
      int axisCount = getAxisCount();
      final int axisCount2 = geometryFactory.getAxisCount();
      if (axisCount2 < axisCount) {
        axisCount = axisCount2;
      }
      final CoordinatesOperationPoint point = new CoordinatesOperationPoint();
      if (isProjectionRequired(geometryFactory)) {
        final CoordinatesOperation coordinatesOperation = getCoordinatesOperation(geometryFactory);
        forEachVertex(coordinatesOperation, point, action);
      } else {
        forEachVertex(point, action);
      }
    }
  }

  default void forEachVertex(final GeometryFactory geometryFactory, final int axisCount,
    final Consumer<CoordinatesOperationPoint> action) {
    if (!isEmpty()) {
      final CoordinatesOperationPoint point = new CoordinatesOperationPoint();
      if (isProjectionRequired(geometryFactory)) {
        final CoordinatesOperation coordinatesOperation = getCoordinatesOperation(geometryFactory);

        forEachVertex(coordinatesOperation, point, action);
      } else {
        forEachVertex(point, action);
      }
    }
  }

  default void forEachVertex(final int axisCount,
    final Consumer<CoordinatesOperationPoint> action) {
    if (!isEmpty()) {
      final CoordinatesOperationPoint point = new CoordinatesOperationPoint();
      forEachVertex(point, action);
    }
  }

  default Iterable<Geometry> geometries() {
    return getGeometries();
  }

  /**
   *  Returns the area of this <code>Geometry</code>.
   *  Areal Geometries have a non-zero area.
   *  They override this function to compute the area.
   *  Others return 0.0
   *
   *@return the area of the Geometry
   */

  default double getArea() {
    return 0.0;
  }

  default double getArea(final Unit<Area> unit) {
    return 0.0;
  }

  /**
   * Returns the boundary, or an empty geometry of appropriate dimension
   * if this <code>Geometry</code>  is empty.
   * (In the case of zero-dimensional geometries, '
   * an empty GeometryCollection is returned.)
   * For a discussion of this function, see the OpenGIS Simple
   * Features Specification. As stated in SFS Section 2.1.13.1, "the boundary
   * of a Geometry is a set of Geometries of the next lower dimension."
   *
   *@return    the closure of the combinatorial boundary of this <code>Geometry</code>
   */

  Geometry getBoundary();

  /**
   *  Returns the dimension of this <code>Geometry</code>s inherent boundary.
   *
   *@return    the dimension of the boundary of the class implementing this
   *      interface, whether or not this object is the empty geometry. Returns
   *      <code>Dimension.FALSE</code> if the boundary is the empty geometry.
   */

  Dimension getBoundaryDimension();

  /**
   * Gets an {@link BoundingBox} containing
   * the minimum and maximum x and y values in this <code>Geometry</code>.
   * If the geometry is empty, an empty <code>BoundingBox</code>
   * is returned.
   * <p>
   *
   *@return the boundingBox of this <code>Geometry</code>.
   *@return an empty BoundingBox if this Geometry is empty
   */
  @Override
  default BoundingBox getBoundingBox() {
    return newBoundingBox();
  }

  /**
   * Computes the centroid of this <code>Geometry</code>.
   * The centroid
   * is equal to the centroid of the set of component Geometries of highest
   * dimension (since the lower-dimension geometries contribute zero
   * "weight" to the centroid).
   * <p>
   * The centroid of an empty geometry is <code>POINT EMPTY</code>.
   *
   * @return a {@link Point} which is the centroid of this Geometry
   */

  default Point getCentroid() {
    if (isEmpty()) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.point();
    } else {
      return Centroid.getCentroid(this);
    }
  }

  default int getClassSortIndex() {
    final String geometryType = getGeometryType();
    final int index = SORTED_GEOMETRY_TYPES.indexOf(geometryType);
    return index;
  }

  @Override
  default GeometryDataType<?, ?> getDataType() {
    return GeometryDataTypes.GEOMETRY;
  }

  /**
   * Returns the dimension of this geometry.
   * The dimension of a geometry is is the topological
   * dimension of its embedding in the 2-D Euclidean plane.
   * In the JTS spatial model, dimension values are in the set {0,1,2}.
   * <p>
   * Note that this is a different concept to the dimension of
   * the vertex {@link Coordinates}s.
   * The geometry dimension can never be greater than the coordinate dimension.
   * For example, a 0-dimensional geometry (e.g. a Point)
   * may have a coordinate dimension of 3 (X,Y,Z).
   *
   *@return the topological dimension of this geometry.
   */

  Dimension getDimension();

  /**
   *  Gets a Geometry representing the boundingBox (bounding box) of
   *  this <code>Geometry</code>.
   *  <p>
   *  If this <code>Geometry</code> is:
   *  <ul>
   *  <li>empty, returns an empty <code>Point</code>.
   *  <li>a point, returns a <code>Point</code>.
   *  <li>a line parallel to an axis, a two-vertex <code>LineString</code>
   *  <li>otherwise, returns a
   *  <code>Polygon</code> whose vertices are (minx miny, maxx miny,
   *  maxx maxy, minx maxy, minx miny).
   *  </ul>
   *
   *@return a Geometry representing the boundingBox of this Geometry
   *
   * @see GeometryFactory#toLineString(BoundingBox)
   */

  default Geometry getEnvelope() {
    return getBoundingBox().toGeometry();
  }

  @SuppressWarnings("unchecked")
  default <V extends Geometry> List<V> getGeometries() {
    return (List<V>)Arrays.asList(this);
  }

  @SuppressWarnings("unchecked")

  default <V extends Geometry> List<V> getGeometries(final Class<V> geometryClass) {
    final List<V> geometries = new ArrayList<>();
    if (geometryClass.isAssignableFrom(getClass())) {
      geometries.add((V)this);
    }
    return geometries;
  }

  default <V extends Geometry> V getGeometry(final Class<? extends Geometry> geometryClass) {
    final List<? extends Geometry> geometries = getGeometries(geometryClass);
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.geometry(geometries);
  }

  /**
   * Returns an element {@link Geometry} from a {@link GeometryCollection}
   * (or <code>this</code>, if the geometry is not a collection).
   *
   * @param partIndex the index of the geometry element
   * @return the n'th geometry contained in this geometry
   */

  @SuppressWarnings("unchecked")
  default <V extends Geometry> V getGeometry(final int partIndex) {
    return (V)this;
  }

  @SuppressWarnings("unchecked")
  default <V extends Geometry> List<V> getGeometryComponents(final Class<V> geometryClass) {
    if (geometryClass.isAssignableFrom(getClass())) {
      return Collections.singletonList((V)this);
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * Returns the number of {@link Geometry}s in a {@link GeometryCollection}
   * (or 1, if the geometry is not a collection).
   *
   * @return the number of geometries contained in this geometry
   */
  default int getGeometryCount() {
    if (isEmpty()) {
      return 0;
    } else {
      return 1;
    }
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    final int axisCount = getAxisCount();
    if (axisCount == 2) {
      return GeometryFactory.DEFAULT_2D;
    } else if (axisCount == 3) {
      return GeometryFactory.DEFAULT_3D;
    } else {
      return GeometryFactory.floating(0, axisCount);
    }
  }

  /**
   * Returns the name of this Geometry's actual class.
   *
   *@return the name of this <code>Geometry</code>s actual class
   */

  default String getGeometryType() {
    return getDataType().toString();
  }

  /**
   * Computes an interior point of this <code>Geometry</code>.
   * An interior point is guaranteed to lie in the interior of the Geometry,
   * if it possible to calculate such a point exactly. Otherwise,
   * the point may lie on the boundary of the geometry.
   * <p>
   * The interior point of an empty geometry is <code>POINT EMPTY</code>.
   *
   * @return a {@link Point} which is in the interior of this Geometry
   */

  default Point getInteriorPoint() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return geometryFactory.point();
    } else {
      Point interiorPt = null;
      final Dimension dim = getDimension();
      if (dim.isPoint()) {
        final Point centroid = getCentroid();
        final double centroidX = centroid.getX();
        final double centroidY = centroid.getY();
        double minDistance = Double.MAX_VALUE;
        Point interiorPoint = null;
        for (final Point point : getGeometries(Point.class)) {
          final double distance = point.distancePoint(centroidX, centroidY);
          if (distance < minDistance) {
            interiorPoint = point;
            minDistance = distance;
          }
        }
        return interiorPoint;
      } else if (dim.isLine()) {
        final InteriorPointLine intPt = new InteriorPointLine(this);
        interiorPt = intPt.getInteriorPoint();
      } else {
        final InteriorPointArea intPt = new InteriorPointArea(this);
        interiorPt = intPt.getInteriorPoint();
      }
      return geometryFactory.point(interiorPt.getX(), interiorPt.getY());
    }
  }

  default List<GeometryValidationError> getIsSimpleErrors() {
    return getIsSimpleErrors(false);
  }

  default List<GeometryValidationError> getIsSimpleErrors(final boolean shortCircuit) {
    final List<GeometryValidationError> errors = new ArrayList<>();
    addIsSimpleErrors(errors, shortCircuit);
    return errors;
  }

  /**
  *  Returns the length of this <code>Geometry</code>.
  *  Linear geometries return their length.
  *  Areal geometries return their perimeter.
  *  Others return 0.0
  *
  * @return the length of the Geometry
  */
  default double getLength() {
    return 0.0;
  }

  default double getLength(final Unit<Length> unit) {
    return 0.0;
  }

  @Override
  default GeometryFactory getNonZeroGeometryFactory(GeometryFactory geometryFactory) {
    final GeometryFactory geometryFactoryThis = getGeometryFactory();
    if (geometryFactory == null) {
      return geometryFactoryThis;
    } else {
      final int srid = geometryFactory.getHorizontalCoordinateSystemId();
      if (srid == 0) {
        final int geometrySrid = geometryFactoryThis.getHorizontalCoordinateSystemId();
        if (geometrySrid != 0) {
          geometryFactory = geometryFactory.convertSrid(geometrySrid);
        }
      }
      return geometryFactory;
    }
  }

  /**
   *  Returns a vertex of this <code>Geometry</code>
   *  (usually, but not necessarily, the first one).
   *  The returned coordinate should not be assumed
   *  to be an actual Point object used in
   *  the internal representation.
   *
   *@return    a {@link Coordinates} which is a vertex of this <code>Geometry</code>.
   *@return null if this Geometry is empty
   */
  Point getPoint();

  Point getPointWithin();

  /**
   * <p>Get the {@link Segment} at the specified vertexId (see {@link Segment#getSegmentId()}).</p>
   *
   *
   * @param vertexId The id of the vertex.
   * @return The vertex or null if it does not exist.
   */
  Segment getSegment(final int... segmentId);

  default int getSegmentCount() {
    return 0;
  }

  /**
   * <p>Get the {@link Vertex} at the specified vertexId starting at the end of the geometry (see {@link Vertex#getVertexId()}).</p>
   *
   *
   * @param vertexId The id of the vertex.
   * @return The vertex or null if it does not exist.
   */
  Vertex getToVertex(final int... vertexId);

  /**
   * <p>Get the {@link Vertex} at the specified vertexId (see {@link Vertex#getVertexId()}).</p>
   *
   *
   * @param vertexId The id of the vertex.
   * @return The vertex or null if it does not exist.
   */
  Vertex getVertex(final int... vertexId);

  /**
   *  Returns the count of this <code>Geometry</code>s vertices. The <code>Geometry</code>
   *  s contained by composite <code>Geometry</code>s must be
   *  Geometry's; that is, they must implement <code>getNumPoints</code>
   *
   *@return    the number of vertices in this <code>Geometry</code>
   */
  int getVertexCount();

  default boolean hasGeometryType(final GeometryDataType<?, ?> dataType) {
    return false;
  }

  boolean hasInvalidXyCoordinates();

  /**
   * Computes a <code>Geometry</code> representing the point-set which is
   * common to both this <code>Geometry</code> and the <code>other</code> Geometry.
   * <p>
   * The intersection of two geometries of different dimension produces a result
   * geometry of dimension less than or equal to the minimum dimension of the input
   * geometries.
   * The result geometry may be a {@link #isHeterogeneousGeometryCollection()}.
   * If the result is empty, it is an atomic geometry
   * with the dimension of the lowest input dimension.
   * <p>
   * Intersection of {@link #isHomogeneousGeometryCollection()}s is supported
   * only.
   * <p>
   * {@link #isHeterogeneousGeometryCollection()} arguments are not supported.
   *
   * @param  geometry the <code>Geometry</code> with which to compute the intersection
   * @return a Geometry representing the point-set common to the two <code>Geometry</code>s
   * @throws TopologyException if a robustness error occurs
   * @throws IllegalArgumentException if the argument is a non-empty heterogeneous <code>GeometryCollection</code>
   */

  default Geometry intersection(final Geometry geometry) {
    /**
     * TODO: MD - add optimization for P-A case using Point-In-Polygon
     */
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (this.isEmpty() || geometry.isEmpty()) {
      // special case: if one input is empty ==> empty
      return OverlayOp.newEmptyResult(OverlayOp.INTERSECTION, this, geometry, geometryFactory);
    } else if (isHeterogeneousGeometryCollection()) {
      final List<Geometry> geometries = new ArrayList<>();
      for (final Geometry part : geometries()) {
        final Geometry partIntersection = part.intersection(geometry);
        if (!partIntersection.isEmpty()) {
          if (partIntersection.isGeometryCollection()) {
            geometries.addAll(partIntersection.getGeometries());
          } else {
            geometries.add(partIntersection);
          }
        }
      }
      return geometryFactory.geometry(geometries);
    } else {
      checkNotGeometryCollection(geometry);
      return SnapIfNeededOverlayOp.overlayOp(this, geometry, OverlayOp.INTERSECTION);
    }
  }

  Geometry intersectionBbox(BoundingBox boundingBox);

  default boolean intersects(final double x, final double y) {
    return locate(x, y) != Location.EXTERIOR;
  }

  /**
   * Tests whether this geometry intersects the argument geometry.
   * <p>
   * The <code>intersects</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The two geometries have at least one point in common
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the patterns
   *  <ul>
   *   <li><code>[T********]</code>
   *   <li><code>[*T*******]</code>
   *   <li><code>[***T*****]</code>
   *   <li><code>[****T****]</code>
   *  </ul>
   * <li><code>! g.disjoint(this) = true</code>
   * <br>(<code>intersects</code> is the inverse of <code>disjoint</code>)
   * </ul>
   *
   *@param  geometry  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s intersect
   *
   * @see Geometry#disjoint
   */

  default boolean intersects(final Geometry geometry) {

    // short-circuit boundingBox test
    if (!bboxIntersects(geometry)) {
      return false;
    }

    /**
     * TODO: (MD) Add optimizations:
     *
     * - for P-A case:
     * If P is in env(A), test for point-in-poly
     *
     * - for A-A case:
     * If env(A1).overlaps(env(A2))
     * test for overlaps via point-in-poly first (both ways)
     * Possibly optimize selection of point to test by finding point of A1
     * closest to centre of env(A2).
     * (Is there a test where we shouldn't bother - e.g. if env A
     * is much smaller than env B, maybe there's no point in testing
     * pt(B) in env(A)?
     */

    // optimization for rectangle arguments
    if (isRectangle()) {
      return RectangleIntersects.rectangleIntersects((Polygon)this, geometry);
    }
    if (geometry.isRectangle()) {
      return RectangleIntersects.rectangleIntersects((Polygon)geometry, this);
    }
    // general case
    return relate(geometry).isIntersects();
  }

  default boolean intersects(Point point) {
    point = point.as2d(this);
    final double x = point.getX();
    final double y = point.getY();
    return intersects(x, y);
  }

  boolean intersectsBbox(BoundingBox boundingBox);

  boolean isContainedInBoundary(final BoundingBox boundingBox);

  /**
   *  Returns whether the two <code>Geometry</code>s are equal, from the point
   *  of view of the <code>equalsExact</code> method. Called by <code>equalsExact</code>
   *  . In general, two <code>Geometry</code> classes are considered to be
   *  "equivalent" only if they are the same class. An exception is <code>LineString</code>
   *  , which is considered to be equivalent to its subclasses.
   *
   *@param  other  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *      for equality
   *@return        <code>true</code> if the classes of the two <code>Geometry</code>
   *      s are considered to be equal by the <code>equalsExact</code> method.
   */
  default boolean isEquivalentClass(final Geometry other) {
    return this.getClass().getName().equals(other.getClass().getName());
  }

  default boolean isGeometryCollection() {
    return false;
  }

  /**
   * Does it contain different types of geometry
   *
   * @return
   */
  default boolean isHeterogeneousGeometryCollection() {
    if (isGeometryCollection()) {
      return !isHomogeneousGeometryCollection();
    } else {
      return false;
    }
  }

  /**
   * Are all the elements of the collection the same type of geometry
   *
   * @return
   */
  default boolean isHomogeneousGeometryCollection() {
    return false;
  }

  /**
   * Tests whether the distance from this <code>Geometry</code>
   * to another is less than or equal to a specified value.
   *
   * @param geometry the Geometry to check the distance to
   * @param distance the distance value to compare
   * @return <code>true</code> if the geometries are less than <code>distance</code> apart.
   */
  default boolean isLessThanDistance(Geometry geometry, final double distance) {
    geometry = geometry.as2d(this);
    final double bboxDistance = bboxDistance(geometry);
    if (bboxDistance > distance) {
      return false;
    } else {
      final double geometryDistance = this.distanceGeometry(geometry);
      return geometryDistance < distance;

    }
  }

  default boolean isRectangle() {
    // Polygon overrides to check for actual rectangle
    return false;
  }

  /**
   * Tests whether this {@link Geometry} is simple.
   * The SFS definition of simplicity
   * follows the general rule that a Geometry is simple if it has no points of
   * self-tangency, self-intersection or other anomalous points.
   * <p>
   * Simplicity is defined for each {@link Geometry} subclass as follows:
   * <ul>
   * <li>Valid polygonal geometries are simple, since their rings
   * must not self-intersect.  <code>isSimple</code>
   * tests for this condition and reports <code>false</code> if it is not met.
   * (This is a looser test than checking for validity).
   * <li>Linear rings have the same semantics.
   * <li>Linear geometries are simple iff they do not self-intersect at points
   * other than boundary points.
   * <li>Zero-dimensional geometries (points) are simple iff they have no
   * repeated points.
   * <li>Empty <code>Geometry</code>s are always simple.
   * <ul>
   *
   * @return <code>true</code> if this <code>Geometry</code> is simple
   * @see #isValid
   */
  default boolean isSimple() {
    final List<GeometryValidationError> errors = new ArrayList<>();
    return addIsSimpleErrors(errors, true);
  }

  /**
   * Tests whether this <code>Geometry</code>
   * is topologically valid, according to the OGC SFS specification.
   * <p>
   * For validity rules see the Javadoc for the specific Geometry subclass.
   *
   *@return <code>true</code> if this <code>Geometry</code> is valid
   *
   * @see IsValidOp
   */

  default boolean isValid() {
    return IsValidOp.isValid(this);
  }

  /**
   * Tests whether the distance from this <code>Geometry</code>
   * to another is less than or equal to a specified value.
   *
   * @param geometry the Geometry to check the distance to
   * @param distance the distance value to compare
   * @return <code>true</code> if the geometries are less than <code>distance</code> apart.
   */
  default boolean isWithinDistance(Geometry geometry, final double distance) {
    geometry = geometry.as2d(this);
    final double bboxDistance = bboxDistance(geometry);
    if (bboxDistance > distance) {
      return false;
    } else {
      final double geometryDistance = this.distanceGeometry(geometry);
      return geometryDistance <= distance;

    }
  }

  Location locate(double x, double y);

  Location locate(Point point);

  /**
   *  Returns the minimum and maximum x and y values in this <code>Geometry</code>
   *  , or a null <code>BoundingBox</code> if this <code>Geometry</code> is empty.
   *  Unlike <code>getEnvelopeInternal</code>, this method calculates the <code>BoundingBox</code>
   *  each time it is called; <code>getEnvelopeInternal</code> caches the result
   *  of this method.
   *
   *@return    this <code>Geometry</code>s bounding box; if the <code>Geometry</code>
   *      is empty, <code>BoundingBox#isNull</code> will return <code>true</code>
   */
  default BoundingBox newBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return geometryFactory.bboxEmpty();
    } else {
      final BoundingBoxEditor boundingBox = new BoundingBoxEditor(geometryFactory);
      forEachVertex(boundingBox);
      return boundingBox.newBoundingBox();
    }
  }

  /**
   * Construct a new copy of the geometry to the required geometry factory. Projecting to the required
   * coordinate system and applying the precision model.
   *
   * @param geometryFactory The geometry factory to convert the geometry to.
   * @return The converted geometry
   */
  Geometry newGeometry(GeometryFactory geometryFactory);

  @SuppressWarnings("unchecked")
  default <G extends Geometry> G newGeometry(GeometryFactory targetGeometryFactory,
    final int axisCount) {
    if (targetGeometryFactory == null) {
      return newGeometry(axisCount);
    } else {
      targetGeometryFactory = targetGeometryFactory.convertAxisCount(2);
      return (G)newGeometry(targetGeometryFactory);
    }
  }

  @SuppressWarnings("unchecked")
  default <G extends Geometry> G newGeometry(final int axisCount) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final GeometryFactory targetGeometryFactory = geometryFactory.convertAxisCount(2);

    return (G)newGeometry(targetGeometryFactory);
  }

  default GeometryEditor<?> newGeometryEditor() {
    return newGeometryEditor(null);
  }

  default GeometryEditor<?> newGeometryEditor(final AbstractGeometryEditor<?> parentEditor) {
    throw new UnsupportedOperationException();
  }

  default GeometryEditor<?> newGeometryEditor(final int axisCount) {
    final GeometryEditor<?> geometryEditor = newGeometryEditor();
    geometryEditor.setAxisCount(axisCount);
    return geometryEditor;
  }

  /**
   * Return a new geometry with the same coordinates but using the geometry factory. No projection will be performed.
   *
   * @param factory
   * @return
   */
  <G> G newUsingGeometryFactory(GeometryFactory factory);

  @SuppressWarnings("unchecked")
  default <G extends Geometry> G newValidGeometry() {
    return (G)this;
  }

  /**
   *  Converts this <code>Geometry</code> to <b>normal form</b> (or <b>
   *  canonical form</b> ). Normal form is a unique representation for <code>Geometry</code>
   *  s. It can be used to test whether two <code>Geometry</code>s are equal
   *  in a way that is independent of the ordering of the coordinates within
   *  them. Normal form equality is a stronger condition than topological
   *  equality, but weaker than pointwise equality. The definitions for normal
   *  form use the standard lexicographical ordering for coordinates. "Sorted in
   *  order of coordinates" means the obvious extension of this ordering to
   *  sequences of coordinates.
   *
   * @return a normalized copy of this geometry.
   */
  Geometry normalize();

  /**
   * Tests whether this geometry overlaps the
   * specified geometry.
   * <p>
   * The <code>overlaps</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have at least one point each not shared by the other
   * (or equivalently neither covers the other),
   * they have the same dimension,
   * and the intersection of the interiors of the two geometries has
   * the same dimension as the geometries themselves.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   *   <code>[T*T***T**]</code> (for two points or two surfaces)
   *   or <code>[1*T***T**]</code> (for two curves)
   * </ul>
   * If the geometries are of different dimension this predicate returns <code>false</code>.
   * This predicate is symmetric.
   *
   *@param  geometry  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s overlap.
   */

  default boolean overlaps(final Geometry geometry) {
    // short-circuit test
    if (bboxIntersects(geometry)) {
      final IntersectionMatrix relate = relate(geometry);
      final Dimension dimension1 = getDimension();
      final Dimension dimension2 = geometry.getDimension();
      return relate.isOverlaps(dimension1, dimension2);
    } else {
      return false;
    }
  }

  /**
   * Get vertices from any point component.
   *
   * @return
   */
  default List<Vertex> pointVertices() {
    return Collections.emptyList();
  }

  Geometry prepare();

  /**
   *  Returns the DE-9IM {@link IntersectionMatrix} for the two <code>Geometry</code>s.
   *
   *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        an {@link IntersectionMatrix} describing the intersections of the interiors,
   *      boundaries and exteriors of the two <code>Geometry</code>s
   */

  default IntersectionMatrix relate(final Geometry g) {
    checkNotGeometryCollection(this);
    checkNotGeometryCollection(g);
    return RelateOp.relate(this, g);
  }

  /**
   * Tests whether the elements in the DE-9IM
   * {@link IntersectionMatrix} for the two <code>Geometry</code>s match the elements in <code>intersectionPattern</code>.
   * The pattern is a 9-character string, with symbols drawn from the following set:
   *  <UL>
   *    <LI> 0 (dimension 0)
   *    <LI> 1 (dimension 1)
   *    <LI> 2 (dimension 2)
   *    <LI> T ( matches 0, 1 or 2)
   *    <LI> F ( matches FALSE)
   *    <LI> * ( matches any value)
   *  </UL>
   *  For more information on the DE-9IM, see the <i>OpenGIS Simple Features
   *  Specification</i>.
   *
   *@param  g                the <code>Geometry</code> with which to compare
   *      this <code>Geometry</code>
   *@param  intersectionPattern  the pattern against which to check the
   *      intersection matrix for the two <code>Geometry</code>s
   *@return                      <code>true</code> if the DE-9IM intersection
   *      matrix for the two <code>Geometry</code>s match <code>intersectionPattern</code>
   * @see IntersectionMatrix
   */

  default boolean relate(final Geometry g, final String intersectionPattern) {
    return relate(g).matches(intersectionPattern);
  }

  Geometry removeDuplicatePoints();

  /**
   * Computes a new geometry which has all component coordinate sequences
   * in reverse order (opposite orientation) to this one.
   *
   * @return a reversed geometry
   */

  Geometry reverse();

  default Iterable<Segment> segments() {
    return Collections.emptyList();
  }

  /**
   * Computes a <coe>Geometry </code> representing the closure of the point-set
   * which is the union of the points in this <code>Geometry</code> which are not
   * contained in the <code>other</code> Geometry,
   * with the points in the <code>other</code> Geometry not contained in this
   * <code>Geometry</code>.
   * If the result is empty, it is an atomic geometry
   * with the dimension of the highest input dimension.
   * <p>
   * {@link #isHeterogeneousGeometryCollection()} arguments are not supported.
   *
   *@param  other the <code>Geometry</code> with which to compute the symmetric
   *      difference
   *@return a Geometry representing the point-set symmetric difference of this <code>Geometry</code>
   *      with <code>other</code>
   * @throws TopologyException if a robustness error occurs
   * @throws IllegalArgumentException if either input is a non-empty GeometryCollection
   */

  default Geometry symDifference(final Geometry other) {
    // handle empty geometry cases
    if (this.isEmpty() || other.isEmpty()) {
      // both empty - check dimensions
      if (this.isEmpty() && other.isEmpty()) {
        return OverlayOp.newEmptyResult(OverlayOp.SYMDIFFERENCE, this, other, getGeometryFactory());
      }

      // special case: if either input is empty ==> result = other arg
      if (this.isEmpty()) {
        return other.clone();
      }
      if (other.isEmpty()) {
        return clone();
      }
    }

    checkNotGeometryCollection(this);
    checkNotGeometryCollection(other);
    return SnapIfNeededOverlayOp.overlayOp(this, other, OverlayOp.SYMDIFFERENCE);
  }

  @SuppressWarnings("unchecked")
  default <G extends Geometry> G toClockDirection(final ClockDirection clockDirection) {
    if (clockDirection.isClockwise()) {
      return toClockwise();
    } else if (clockDirection.isCounterClockwise()) {
      return toCounterClockwise();
    } else {
      return (G)this;
    }
  }

  @SuppressWarnings("unchecked")
  default <G extends Geometry> G toClockwise() {
    return (G)this;
  }

  @SuppressWarnings("unchecked")
  default <G extends Geometry> G toCounterClockwise() {
    return (G)this;
  }

  /**
   *  <p>Returns the Extended Well-known Text representation of this <code>Geometry</code>.
   *  For a definition of the Well-known Text format, see the OpenGIS Simple
   *  Features Specification.</p>
   *
   *@return    the Well-known Text representation of this <code>Geometry</code>
   */

  default String toEwkt() {
    return EWktWriter.toString(this, true);
  }

  /**
   * Tests whether this geometry touches the
   * argument geometry.
   * <p>
   * The <code>touches</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have at least one point in common,
   * but their interiors do not intersect.
   * <li>The DE-9IM Intersection Matrix for the two geometries matches
   * at least one of the following patterns
   *  <ul>
   *   <li><code>[FT*******]</code>
   *   <li><code>[F**T*****]</code>
   *   <li><code>[F***T****]</code>
   *  </ul>
   * </ul>
   * If both geometries have dimension 0, the predicate returns <code>false</code>,
   * since points have only interiors.
   * This predicate is symmetric.
   *
   *
   *@param  geometry  the <code>Geometry</code> with which to compare this <code>Geometry</code>
   *@return        <code>true</code> if the two <code>Geometry</code>s touch;
   *      Returns <code>false</code> if both <code>Geometry</code>s are points
   */

  default boolean touches(final Geometry geometry) {
    // short-circuit test
    if (bboxIntersects(geometry)) {
      final IntersectionMatrix relate = relate(geometry);
      final Dimension dimension1 = getDimension();
      final Dimension dimension2 = geometry.getDimension();
      return relate.isTouches(dimension1, dimension2);
    } else {
      return false;
    }
  }

  /**
   *  <p>Returns the Well-known Text representation of this <code>Geometry</code>.
   *  For a definition of the Well-known Text format, see the OpenGIS Simple
   *  Features Specification.</p>
   *
   *@return    the Well-known Text representation of this <code>Geometry</code>
   */

  default String toWkt() {
    return EWktWriter.toString(this, false);
  }

  /**
   * Computes the union of all the elements of this geometry.
   * <p>
   * This method supports
   * {@link GeometryCollection}s
   * (which the other overlay operations currently do not).
   * <p>
   * The result obeys the following contract:
   * <ul>
   * <li>Unioning a set of {@link LineString}s has the effect of fully noding
   * and dissolving the linework.
   * <li>Unioning a set of {@link Polygon}s always
   * returns a {@link Polygonal} geometry (unlike {@link #union(Geometry)},
   * which may return geometries of lower dimension if a topology collapse occurred).
   * </ul>
   *
   * @return the union geometry
   * @throws TopologyException if a robustness error occurs
   *
   * @see UnaryUnionOp
   */

  default Geometry union() {
    if (isEmpty()) {
      return this;
    } else {
      return UnaryUnionOp.union(this);
    }
  }

  /**
   * Computes a <code>Geometry</code> representing the point-set
   * which is contained in both this
   * <code>Geometry</code> and the <code>other</code> Geometry.
   * <p>
   * The union of two geometries of different dimension produces a result
   * geometry of dimension equal to the maximum dimension of the input
   * geometries.
   * The result geometry may be a heterogenous
   * {@link GeometryCollection}.
   * If the result is empty, it is an atomic geometry
   * with the dimension of the highest input dimension.
   * <p>
   * Unioning {@link LineString}s has the effect of
   * <b>noding</b> and <b>dissolving</b> the input linework. In this context
   * "noding" means that there will be a node or endpoint in the result for
   * every endpoint or line segment crossing in the input. "Dissolving" means
   * that any duplicate (i.e. coincident) line segments or portions of line
   * segments will be reduced to a single line segment in the result.
   * If <b>merged</b> linework is required, the {@link LineMerger}
   * class can be used.
   * <p>
   * {@link #isHeterogeneousGeometryCollection()} arguments are not supported.
   *
   * @param other
   *          the <code>Geometry</code> with which to compute the union
   * @return a point-set combining the points of this <code>Geometry</code> and the
   *         points of <code>other</code>
   * @throws TopologyException
   *           if a robustness error occurs
   * @throws IllegalArgumentException
   *           if either input is a non-empty GeometryCollection
   * @see LineMerger
   */

  @SuppressWarnings("unchecked")
  default <G extends Geometry> G union(final Geometry other) {
    // handle empty geometry cases
    if (other == null) {
      return (G)this;
    } else if (isEmpty()) {
      if (other.isEmpty()) {
        return (G)OverlayOp.newEmptyResult(OverlayOp.UNION, this, other, getGeometryFactory());
      } else {
        return (G)other;
      }
    } else if (other.isEmpty()) {
      return (G)this;
    }

    // TODO: optimize if envelopes of geometries do not intersect

    checkNotGeometryCollection(this);
    checkNotGeometryCollection(other);
    return (G)SnapIfNeededOverlayOp.overlayOp(this, other, OverlayOp.UNION);
  }

  /**
   * <p>Get an {@link Iterable} that iterates over the {@link Vertex} of the geometry. For memory
   * efficiency the {@link Vertex} returned is the same instance for each call to next
   * on the iterator. If the vertex is required to track the previous vertex then the
   * {@link Vertex#clone()} method must be called to get a copy of the vertex.</p>
   *
   * <p>The {@link Iterable#iterator()} method always returns the same {@link Iterator} instance.
   * Therefore that method should not be called more than once.</p>
   *
   *
   * @return The iterator over the vertices of the geometry.
   */
  Vertex vertices();

  /**
  * Tests whether this geometry is within the
  * specified geometry.
  * <p>
  * The <code>within</code> predicate has the following equivalent definitions:
  * <ul>
  * <li>Every point of this geometry is a point of the other geometry,
  * and the interiors of the two geometries have at least one point in common.
  * <li>The DE-9IM Intersection Matrix for the two geometries matches
  * <code>[T*F**F***]</code>
  * <li><code>g.contains(this) = true</code>
  * <br>(<code>within</code> is the converse of {@link #contains})
  * </ul>
  * An implication of the definition is that
  * "The boundary of a Geometry is not within the Geometry".
  * In other words, if a geometry A is a subset of
  * the points in the boundary of a geomtry B, <code>A.within(B) = false</code>
  * (As a concrete example, take A to be a LineString which lies in the boundary of a Polygon B.)
  * For a predicate with similar behaviour but avoiding
  * this subtle limitation, see {@link #coveredBy}.
  *
  *@param  g  the <code>Geometry</code> with which to compare this <code>Geometry</code>
  *@return        <code>true</code> if this <code>Geometry</code> is within
  *      <code>g</code>
  *
  * @see Geometry#contains
  * @see Geometry#coveredBy
  */

  default boolean within(final Geometry g) {
    return g.contains(this);
  }
}
