package com.revolsys.geometry.edgegraph;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.algorithm.CGAlgorithmsDD;
import com.revolsys.geometry.geomgraph.Quadrant;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.Assert;

/**
 * Represents a directed component of an edge in an {@link EdgeGraph}.
 * HalfEdges link vertices whose locations are defined by {@link Coordinates}s.
 * HalfEdges start at an <b>origin</b> vertex,
 * and terminate at a <b>destination</b> vertex.
 * HalfEdges always occur in symmetric pairs, with the {@link #sym()} method
 * giving access to the oppositely-oriented component.
 * HalfEdges and the methods on them form an edge algebra,
 * which can be used to traverse and query the topology
 * of the graph formed by the edges.
 * <p>
 * By design HalfEdges carry minimal information
 * about the actual usage of the graph they represent.
 * They can be subclassed to carry more information if required.
 * <p>
 * HalfEdges form a complete and consistent data structure by themselves,
 * but an {@link EdgeGraph} is useful to allow retrieving edges
 * by vertex and edge location, as well as ensuring
 * edges are created and linked appropriately.
 *
 * @author Martin Davis
 *
 */
public class HalfEdge {

  /**
   * Initialize a symmetric pair of halfedges.
   * Intended for use by {@link EdgeGraph} subclasses.
   * The edges are initialized to have each other
   * as the sym edge, and to have {@link next} pointers
   * which point to edge other.
   * This effectively creates a graph containing a single edge.
   *
   * @param e0 a halfedge
   * @param e1 a symmetric halfedge
   * @return the initialized edge e0
   */
  public static HalfEdge init(final HalfEdge e0, final HalfEdge e1) {
    // ensure only newly created edges can be initialized, to prevent
    // information loss
    if (e0.sym != null || e1.sym != null || e0.next != null || e1.next != null) {
      throw new IllegalStateException("Edges are already initialized");
    }
    e0.init(e1);
    return e0;
  }

  /**
   * Creates a HalfEdge pair representing an edge
   * between two vertices located at coordinates p0 and p1.
   *
   * @param p0 a vertex coordinate
   * @param p1 a vertex coordinate
   * @return the HalfEdge with origin at p0
   */
  public static HalfEdge newHalfEdge(final Point p0, final Point p1) {
    final HalfEdge e0 = new HalfEdge(p0);
    final HalfEdge e1 = new HalfEdge(p1);
    e0.init(e1);
    return e0;
  }

  private HalfEdge next;

  private final Point orig;

  private HalfEdge sym;

  /**
   * Creates an edge originating from a given coordinate.
   *
   * @param orig the origin coordinate
   */
  public HalfEdge(final Point orig) {
    this.orig = orig;
  }

  /**
   * Implements the total order relation:
   * <p>
   *    The angle of edge a is greater than the angle of edge b,
   *    where the angle of an edge is the angle made by
   *    the first segment of the edge with the positive x-axis
   * <p>
   * When applied to a list of edges originating at the same point,
   * this produces a CCW ordering of the edges around the point.
   * <p>
   * Using the obvious algorithm of computing the angle is not robust,
   * since the angle calculation is susceptible to roundoff error.
   * A robust algorithm is:
   * <ul>
   * <li>First, compare the quadrants the edge vectors lie in.
   * If the quadrants are different,
   * it is trivial to determine which edge has a greater angle.
   *
   * <li>if the vectors lie in the same quadrant, the
   * {@link CGAlgorithms#computeOrientation(Point, Point, Point)} function
   * can be used to determine the relative orientation of the vectors.
   * </ul>
   */
  public int compareAngularDirection(final HalfEdge e) {
    final double dx = deltaX();
    final double dy = deltaY();
    final double dx2 = e.deltaX();
    final double dy2 = e.deltaY();

    // same vector
    if (dx == dx2 && dy == dy2) {
      return 0;
    }

    final double quadrant = Quadrant.quadrant(dx, dy);
    final double quadrant2 = Quadrant.quadrant(dx2, dy2);

    // if the vectors are in different quadrants, determining the ordering is
    // trivial
    if (quadrant > quadrant2) {
      return 1;
    }
    if (quadrant < quadrant2) {
      return -1;
    }
    // vectors are in the same quadrant
    // Check relative orientation of direction vectors
    // this is > e if it is CCW of e
    /**
     * MD - 9 Aug 2010 It seems that the basic algorithm is slightly orientation
     * dependent, when computing the orientation of a point very close to a
     * line. This is possibly due to the arithmetic in the translation to the
     * origin.
     *
     * For instance, the following situation produces identical results in spite
     * of the inverse orientation of the line segment:
     *
     * Point p0 = new PointDouble((double)219.3649559090992, 140.84159161824724);
     * Point p1 = new PointDouble((double)168.9018919682399, -5.713787599646864);
     *
     * Point p = new PointDouble((double)186.80814046338352, 46.28973405831556); int
     * orient = orientationIndex(p0, p1, p); int orientInv =
     * orientationIndex(p1, p0, p);
     *
     * A way to force consistent results is to normalize the orientation of the
     * vector using the following code. However, this may make the results of
     * orientationIndex inconsistent through the triangle of points, so it's not
     * clear this is an appropriate patch.
     *
     */
    return CGAlgorithmsDD.orientationIndex(e.orig, e.dest(), dest());
    // testing only
    // return ShewchuksDeterminant.orientationIndex(p1, p2, q);
    // previous implementation - not quite fully robust
    // return RobustDeterminant.orientationIndex(p1, p2, q);
  }

  /**
   * Compares edges which originate at the same vertex
   * based on the angle they make at their origin vertex with the positive X-axis.
   * This allows sorting edges around their origin vertex in CCW order.
   */
  public int compareTo(final Object obj) {
    final HalfEdge e = (HalfEdge)obj;
    final int comp = compareAngularDirection(e);
    return comp;
  }

  /**
   * Computes the degree of the origin vertex.
   * The degree is the number of edges
   * originating from the vertex.
   *
   * @return the degree of the origin vertex
   */
  public int degree() {
    int degree = 0;
    HalfEdge e = this;
    do {
      degree++;
      e = e.oNext();
    } while (e != this);
    return degree;
  }

  /**
   * The X component of the distance between the orig and dest vertices.
   *
   * @return the X component of the edge length
   */
  public double deltaX() {
    return this.sym.orig.getX() - this.orig.getX();
  }

  /**
   * The Y component of the distance between the orig and dest vertices.
   *
   * @return the Y component of the edge length
   */
  public double deltaY() {
    return this.sym.orig.getY() - this.orig.getY();
  }

  /**
   * Gets the destination coordinate of this edge.
   *
   * @return the destination coordinate
   */
  public Point dest() {
    return this.sym.orig;
  }

  /**
   * Tests whether this edge has the given orig and dest vertices.
   *
   * @param p0 the origin vertex to test
   * @param p1 the destination vertex to test
   * @return true if the vertices are equal to the ones of this edge
   */
  public boolean equals(final Point p0, final Point p1) {
    return this.orig.equals(2, p0) && this.sym.orig.equals(p1);
  }

  /**
   * Finds the edge starting at the origin of this edge
   * with the given dest vertex,
   * if any.
   *
   * @param dest the dest vertex to search for
   * @return the edge with the required dest vertex, if it exists,
   * or null
   */
  public HalfEdge find(final Point dest) {
    HalfEdge oNext = this;
    do {
      if (oNext == null) {
        return null;
      }
      if (oNext.dest().equals(2, dest)) {
        return oNext;
      }
      oNext = oNext.oNext();
    } while (oNext != this);
    return null;
  }

  protected void init(final HalfEdge e) {
    setSym(e);
    e.setSym(this);
    // set next ptrs for a single segment
    setNext(e);
    e.setNext(this);
  }

  /**
   * Inserts an edge
   * into the ring of edges around the origin vertex of this edge.
   * The inserted edge must have the same origin as this edge.
   *
   * @param e the edge to insert
   */
  public void insert(final HalfEdge e) {
    // if no other edge around origin
    if (oNext() == this) {
      // set linkage so ring is correct
      insertAfter(e);
      return;
    }

    // otherwise, find edge to insert after
    final int ecmp = compareTo(e);
    HalfEdge ePrev = this;
    do {
      final HalfEdge oNext = ePrev.oNext();
      final int cmp = oNext.compareTo(e);
      if (cmp != ecmp || oNext == this) {
        ePrev.insertAfter(e);
        return;
      }
      ePrev = oNext;
    } while (ePrev != this);
    Assert.shouldNeverReachHere();
  }

  /**
   * Insert an edge with the same origin after this one.
   * Assumes that the inserted edge is in the correct
   * position around the ring.
   *
   * @param e the edge to insert (with same origin)
   */
  private void insertAfter(final HalfEdge e) {
    Assert.equals(this.orig, e.orig());
    final HalfEdge save = oNext();
    this.sym.setNext(e);
    e.sym().setNext(save);
  }

  /**
   * Gets the next edge CCW around the
   * destination vertex of this edge.
   * If the vertex has degree 1 then this is the <b>sym</b> edge.
   *
   * @return the next edge
   */
  public HalfEdge next() {
    return this.next;
  }

  public HalfEdge oNext() {
    return this.sym.next;
  }

  /**
   * Gets the origin coordinate of this edge.
   *
   * @return the origin coordinate
   */
  public Point orig() {
    return this.orig;
  }

  /**
   * Returns the edge previous to this one
   * (with dest being the same as this orig).
   *
   * @return the previous edge to this one
   */
  public HalfEdge prev() {
    return this.sym.next().sym;
  }

  /**
   * Finds the first node previous to this edge, if any.
   * If no such node exists (i.e the edge is part of a ring)
   * then null is returned.
   *
   * @return an edge originating at the node prior to this edge, if any,
   *   or null if no node exists
   */
  public HalfEdge prevNode() {
    HalfEdge e = this;
    while (e.degree() == 2) {
      e = e.prev();
      if (e == this) {
        return null;
      }
    }
    return e;
  }

  public void setNext(final HalfEdge e) {
    this.next = e;
  }

  /**
   * Sets the sym edge.
   *
   * @param e the sym edge to set
   */
  private void setSym(final HalfEdge e) {
    this.sym = e;
  }

  /**
   * Gets the symmetric pair edge of this edge.
   *
   * @return the symmetric pair edge
   */
  public HalfEdge sym() {
    return this.sym;
  }

  /**
   * Computes a string representation of a HalfEdge.
   *
   * @return a string representation
   */
  @Override
  public String toString() {
    return "HE(" + this.orig.getX() + " " + this.orig.getY() + ", " + this.sym.orig.getX() + " "
      + this.sym.orig.getY() + ")";
  }

}
