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

package com.revolsys.elevation.tin.quadedge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.algorithm.ConvexHull;
import com.revolsys.geometry.index.kdtree.KdNode;
import com.revolsys.geometry.index.kdtree.KdNodeData;
import com.revolsys.geometry.index.kdtree.KdTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;

/**
 * Computes a Conforming Delaunay Triangulation over a set of sites and a set of
 * linear constraints.
 * <p>
 * A conforming Delaunay triangulation is a true Delaunay triangulation. In it
 * each constraint segment is present as a union of one or more triangulation
 * edges. Constraint segments may be subdivided into two or more triangulation
 * edges by the insertion of additional sites. The additional sites are called
 * Steiner points, and are necessary to allow the segments to be faithfully
 * reflected in the triangulation while maintaining the Delaunay property.
 * Another way of stating this is that in a conforming Delaunay triangulation
 * every constraint segment will be the union of a subset of the triangulation
 * edges (up to tolerance).
 * <p>
 * A Conforming Delaunay triangulation is distinct from a Constrained Delaunay triangulation.
 * A Constrained Delaunay triangulation is not necessarily fully Delaunay,
 * and it contains the constraint segments exactly as edges of the triangulation.
 * <p>
 * A typical usage pattern for the triangulator is:
 * <pre>
 *  ConformingDelaunayTriangulator cdt = new ConformingDelaunayTriangulator(sites, tolerance);
 *
 *   // optional
 *   cdt.setSplitPointFinder(splitPointFinder);
 *   cdt.setVertexFactory(vertexFactory);
 *
 * cdt.setConstraints(segments, new ArrayList(vertexMap.values()));
 * cdt.formInitialDelaunay();
 * cdt.enforceConstraints();
 * subdiv = cdt.getSubdivision();
 * </pre>
 *
 * @author David Skea
 * @author Martin Davis
 */
public class ConformingDelaunayTriangulator extends QuadEdgeDelaunayTinBuilder {
  private final static int MAX_SPLIT_ITER = 99;

  private Geometry convexHull;

  private KdTree pointIndex = null;

  private List<LineSegmentDoubleData> segments = new ArrayList<>();

  private List<Point> segmentVertices;

  private ConstraintSplitPointFinder splitFinder = new NonEncroachingSplitPointFinder();

  // records the last split point computed, for error reporting
  private Point splitPt = null;

  private ConstraintVertexFactory vertexFactory = null;

  /**
   * Creates a Conforming Delaunay Triangulation based on the given
   * unconstrained initial vertices. The initial vertex set should not contain
   * any vertices which appear in the constraint set.
   *
   * @param initialVertices
   *          a collection of {@link ConstraintVertex}
   * @param geometryFactory
   *          the distance tolerance below which points are considered identical
   */
  public ConformingDelaunayTriangulator(final Iterable<? extends Point> initialVertices,
    final GeometryFactory geometryFactory) {
    super(geometryFactory);
    this.pointIndex = new KdTree(KdNodeData::new, geometryFactory);
    insertVertices(initialVertices);
  }

  private void addConstraintVertices() {
    computeConvexHull();
    // insert constraint vertices as sites
    insertSites(this.segmentVertices);
  }

  /**
   * Computes the Delaunay triangulation of the initial sites.
   */
  @Override
  public void buildTin() {
    computeBoundingBox();
    super.buildTin();
  }

  private void computeBoundingBox() {
    expandBoundingBox(this.segmentVertices);

    final BoundingBox boundingBox = getBoundingBox();
    final double deltaX = boundingBox.getWidth() * 0.2;
    final double deltaY = boundingBox.getHeight() * 0.2;

    final double delta = Math.max(deltaX, deltaY);

    expandBounds(delta);
  }

  private void computeConvexHull() {
    final List<Point> points = getPoints();
    this.convexHull = ConvexHull.convexHull(GeometryFactory.DEFAULT_3D, points);
  }

  /**
   * Enforces the supplied constraints into the triangulation.
   *
   * @throws ConstraintEnforcementException
   *           if the constraints cannot be enforced
   */
  public void enforceConstraints() {
    addConstraintVertices();
    // if (true) return;

    int count = 0;
    int splits = 0;
    do {
      splits = enforceGabriel(this.segments);

      count++;
    } while (splits > 0 && count < MAX_SPLIT_ITER);
    if (count == MAX_SPLIT_ITER) {
      throw new ConstraintEnforcementException(
        "Too many splitting iterations while enforcing constraints.  Last split point was at: ",
        this.splitPt);
    }
  }

  private int enforceGabriel(final Collection segsToInsert) {
    final List newSegments = new ArrayList();
    int splits = 0;
    final List segsToRemove = new ArrayList();

    /**
     * On each iteration must always scan all constraint (sub)segments, since
     * some constraints may be rebroken by Delaunay triangle flipping caused by
     * insertion of another constraint. However, this process must converge
     * eventually, with no splits remaining to find.
     */
    for (final Object element : segsToInsert) {
      final LineSegmentDoubleData seg = (LineSegmentDoubleData)element;
      // System.out.println(seg);

      final Point encroachPt = findNonGabrielPoint(seg);
      // no encroachment found - segment must already be in subdivision
      if (encroachPt == null) {
        continue;
      }

      // compute split point
      this.splitPt = this.splitFinder.findSplitPoint(seg, encroachPt);
      final ConstraintVertex splitVertex = newVertex(this.splitPt, seg);

      // DebugFeature.addLineSegment(DEBUG_SEG_SPLIT, encroachPt, splitPt, "");
      // Debug.println(WKTWriter.toLineString(encroachPt, splitPt));

      /**
       * Check whether the inserted point still equals the split pt. This will
       * not be the case if the split pt was too close to an existing site. If
       * the point was snapped, the triangulation will not respect the inserted
       * constraint - this is a failure. This can be caused by:
       * <ul>
       * <li>An initial site that lies very close to a constraint segment The
       * cure for this is to remove any initial sites which are close to
       * constraint segments in a preprocessing phase.
       * <li>A narrow constraint angle which causing repeated splitting until
       * the split segments are too small. The cure for this is to either choose
       * better split points or "guard" narrow angles by cracking the segments
       * equidistant from the corner.
       * </ul>
       */
      final ConstraintVertex insertedVertex = insertSite(splitVertex);
      if (!insertedVertex.equals(2, this.splitPt)) {
        // throw new ConstraintEnforcementException("Split point snapped to
        // existing point
        // (tolerance too large or constraint interior narrow angle?)",
        // splitPt);
      }

      // split segment and record the new halves
      final LineSegmentDoubleData s1 = new LineSegmentDoubleData(seg.getX(0), seg.getY(0),
        seg.getZ(0), splitVertex.getX(), splitVertex.getY(), splitVertex.getZ(), seg.getData());
      final LineSegmentDoubleData s2 = new LineSegmentDoubleData(splitVertex.getX(),
        splitVertex.getY(), splitVertex.getZ(), seg.getX(1), seg.getY(1), seg.getZ(1),
        seg.getData());
      newSegments.add(s1);
      newSegments.add(s2);
      segsToRemove.add(seg);

      splits = splits + 1;
    }
    segsToInsert.removeAll(segsToRemove);
    segsToInsert.addAll(newSegments);

    return splits;
  }

  /**
   * Given a set of points stored in the kd-tree and a line segment defined by
   * two points in this set, finds a {@link Coordinates} in the circumcircle of
   * the line segment, if one exists. This is called the Gabriel point - if none
   * exists then the segment is said to have the Gabriel condition. Uses the
   * heuristic of finding the non-Gabriel point closest to the midpoint of the
   * segment.
   *
   * @param p
   *          start of the line segment
   * @param q
   *          end of the line segment
   * @return a point which is non-Gabriel
   * or null if no point is non-Gabriel
   */
  private Point findNonGabrielPoint(final LineSegmentDoubleData seg) {
    final Point p = seg.getPoint(0);
    final Point q = seg.getPoint(1);
    // Find the mid point on the line and compute the radius of enclosing circle
    final Point midPt = new PointDoubleXY((p.getX() + q.getX()) / 2.0, (p.getY() + q.getY()) / 2.0);
    final double segRadius = p.distancePoint(midPt);

    // compute envelope of circumcircle
    final BoundingBox env = midPt.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(segRadius);
    // Find all points in envelope
    final List result = this.pointIndex.getItems(env);

    // For each point found, test if it falls strictly in the circle
    // find closest point
    Point closestNonGabriel = null;
    double minDist = Double.MAX_VALUE;
    for (final Object element : result) {
      final KdNode nextNode = (KdNode)element;
      final Point testPt = nextNode;
      // ignore segment endpoints
      if (testPt.equals(2, p) || testPt.equals(2, q)) {
        continue;
      }

      final double testRadius = midPt.distancePoint(testPt);
      if (testRadius < segRadius) {
        // double testDist = seg.distance(testPt);
        final double testDist = testRadius;
        if (closestNonGabriel == null || testDist < minDist) {
          closestNonGabriel = testPt;
          minDist = testDist;
        }
      }
    }
    return closestNonGabriel;
  }

  /**
   * Gets the {@link LineSegmentDoubleData}s which represent the constraints.
   *
   * @return a collection of Segments
   */
  public List<LineSegmentDoubleData> getConstraintSegments() {
    return this.segments;
  }

  /**
   * Gets the convex hull of all the sites in the triangulation,
   * including constraint vertices.
   * Only valid after the constraints have been enforced.
   *
   * @return the convex hull of the sites
   */
  public Geometry getConvexHull() {
    return this.convexHull;
  }

  /**
   * Gets the {@link KdTree} which contains the vertices of the triangulation.
   *
   * @return a KdTree
   */
  public KdTree getKDT() {
    return this.pointIndex;
  }

  // ==================================================================

  private List<Point> getPoints() {
    final List<Point> vertices = getVertices();
    final List<Point> pts = new ArrayList<>(vertices.size() + this.segmentVertices.size());
    pts.addAll(vertices);
    pts.addAll(this.segmentVertices);
    return pts;
  }
  // /**
  // * Adds the segments in the Convex Hull of all sites in the input data as
  // linear constraints.
  // * This is required if TIN Refinement is performed. The hull segments are
  // flagged with a
  // unique
  // * data object to allow distinguishing them.
  // *
  // * @param convexHullSegmentData the data object to attach to each convex
  // hull segment
  // */
  // private void addConvexHullToConstraints(Object convexHullSegmentData) {
  // Point[] coords = convexHull.getCoordinates();
  // for (int i = 1; i < coords.length; i++) {
  // Segment s = new Segment(coords[i - 1], coords[i], convexHullSegmentData);
  // addConstraintIfUnique(s);
  // }
  // }

  // private void addConstraintIfUnique(Segment r) {
  // boolean exists = false;
  // Iterator it = segments.iterator();
  // Segment s = null;
  // while (it.hasNext()) {
  // s = (Segment) it.next();
  // if (r.equalsTopo(s)) {
  // exists = true;
  // }
  // }
  // if (!exists) {
  // segments.add((Object) r);
  // }
  // }

  /**
   * Gets the <tt>ConstraintVertexFactory</tt> used to create new constraint vertices at split points.
   *
   * @return a new constraint vertex
   */
  public ConstraintVertexFactory getVertexFactory() {
    return this.vertexFactory;
  }

  private ConstraintVertex insertSite(final ConstraintVertex vertex) {
    final KdNodeData kdnode = this.pointIndex.insertPoint(vertex);
    if (kdnode.isRepeated()) {
      final ConstraintVertex snappedV = (ConstraintVertex)kdnode.getData();
      snappedV.merge(vertex);
      return snappedV;
    } else {
      kdnode.setData(vertex);
      final QuadEdgeSubdivision subdivision = getSubdivision();
      subdivision.insertVertex(vertex);
    }
    return vertex;
  }

  /**
   * Inserts a site into the triangulation, maintaining the conformal Delaunay property.
   * This can be used to further refine the triangulation if required
   * (e.g. to approximate the medial axis of the constraints,
   * or to improve the grading of the triangulation).
   *
   * @param p the location of the site to insert
   */
  public void insertSite(final Point p) {
    insertSite(newVertex(p));
  }

  /**
   * Inserts all sites in a collection
   *
   * @param vertices a collection of ConstraintVertex
   */
  private void insertSites(final Iterable<? extends Point> vertices) {
    for (final Point point : vertices) {
      final ConstraintVertex vertex = (ConstraintVertex)point;
      insertSite(vertex);
    }
  }

  @Override
  protected void insertVertices(final QuadEdgeSubdivision subdivision, final List<Point> vertices) {
    insertSites(vertices);
  }

  // ==================================================================

  @Override
  protected PointDoubleXYZ newVertex(final double x, final double y, final double z) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new ConstraintVertex(geometryFactory, x, y, z);
  }

  // ==================================================================

  private ConstraintVertex newVertex(final Point p) {
    ConstraintVertex v = null;
    if (this.vertexFactory != null) {
      v = this.vertexFactory.newVertex(p, null);
    } else {
      v = new ConstraintVertex(p);
    }
    return v;
  }

  /**
   * Creates a vertex on a constraint segment
   *
   * @param p the location of the vertex to create
   * @param seg the constraint segment it lies on
   * @return the new constraint vertex
   */
  private ConstraintVertex newVertex(final Point p, final LineSegmentDoubleData seg) {
    ConstraintVertex v = null;
    if (this.vertexFactory != null) {
      v = this.vertexFactory.newVertex(p, seg);
    } else {
      v = new ConstraintVertex(p);
    }
    v.setOnConstraint(true);
    return v;
  }

  /**
   * Sets the constraints to be conformed to by the computed triangulation.
   * The constraints must not contain duplicate segments (up to orientation).
   * The unique set of vertices (as {@link ConstraintVertex}es)
   * forming the constraints must also be supplied.
   * Supplying it explicitly allows the ConstraintVertexes to be initialized
   * appropriately(e.g. with external data), and avoids re-computing the unique set
   * if it is already available.
   *
   * @param segments a list of the constraint {@link LineSegmentDoubleData}s
   * @param segVertices the set of unique {@link ConstraintVertex}es referenced by the segments
   */
  public void setConstraints(final List segments, final List segVertices) {
    this.segments = segments;
    this.segmentVertices = segVertices;
  }

  /*
   * private List findMissingConstraints() { List missingSegs = new ArrayList();
   * for (int i = 0; i < segments.size(); i++) { Segment s = (Segment)
   * segments.get(i); QuadEdge q = subdiv.locate(s.getStart(), s.getEnd()); if
   * (q == null) missingSegs.add(s); } return missingSegs; }
   */

  /**
   * Sets the {@link ConstraintSplitPointFinder} to be
   * used during constraint enforcement.
   * Different splitting strategies may be appropriate
   * for special situations.
   *
   * @param splitFinder the ConstraintSplitPointFinder to be used
   */
  public void setSplitPointFinder(final ConstraintSplitPointFinder splitFinder) {
    this.splitFinder = splitFinder;
  }

  // public static final String DEBUG_SEG_SPLIT =
  // "C:\\proj\\CWB\\test\\segSplit.jml";

  /**
   * Sets a custom {@link ConstraintVertexFactory} to be used
   * to allow vertices carrying extra information to be created.
   *
   * @param vertexFactory the ConstraintVertexFactory to be used
   */
  public void setVertexFactory(final ConstraintVertexFactory vertexFactory) {
    this.vertexFactory = vertexFactory;
  }

}
