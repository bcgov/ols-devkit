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

package com.revolsys.elevation.tin.quadedge.intscale;

import java.util.Deque;
import java.util.LinkedList;

import com.revolsys.elevation.tin.TriangleConsumer;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Side;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;

/**
 * A class that contains the {@link QuadEdge}s representing a planar
 * subdivision that models a triangulation.
 * The subdivision is constructed using the
 * quadedge algebra defined in the classs {@link QuadEdge}.
 * In addition to a triangulation, subdivisions
 * support extraction of Voronoi diagrams.
 * This is easily accomplished, since the Voronoi diagram is the dual
 * of the Delaunay triangulation.
 * <p>
 * Subdivisions can be provided with a tolerance value. Inserted vertices which
 * are closer than this value to vertices already in the subdivision will be
 * ignored. Using a suitable tolerance value can prevent robustness failures
 * from happening during Delaunay triangulation.
 * <p>
 * Subdivisions maintain a <b>frame</b> triangle around the client-created
 * edges. The frame is used to provide a bounded "container" for all edges
 * within a TIN. Normally the frame edges, frame connecting edges, and frame
 * triangles are not included in client processing.
 *
 * @author David Skea
 * @author Martin Davis
 */
public class QuadEdgeSubdivision {
  private final int[] frameCoordinates;

  private final GeometryFactory geometryFactory;

  private QuadEdge lastEdge = null;

  private int edgeCount = 3;

  private final QuadEdge startingEdge;

  private final int minX;

  private final int minY;

  private short visitIndex = 0;

  /**
   * Creates a new instance of a quad-edge subdivision based on a frame triangle
   * that encloses a supplied bounding box. A new super-bounding box that
   * contains the triangle is computed and stored.
   *
   * @param bounds
   *          the bounding box to surround
   * @param geometryFactory The geometry factory including precision model.
   */
  public QuadEdgeSubdivision(final int[] bounds, final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;

    this.minX = bounds[0];
    this.minY = bounds[1];
    final int maxX = bounds[2];
    final int maxY = bounds[3];
    final int width = maxX - this.minX;
    final int height = maxY - this.minY;
    int offset = 0;
    if (width > height) {
      offset = width * 10;
    } else {
      offset = height * 10;
    }

    final int x1 = this.minX + width / 2;
    final int y1 = maxY + offset;

    final int x2 = this.minX - offset;
    final int y2 = this.minY - offset;
    final int x3 = maxX + offset;

    final PointIntXYZ frameVertex1 = new PointIntXYZ(x1, y1);
    final PointIntXYZ frameVertex2 = new PointIntXYZ(x2, y2);
    final PointIntXYZ frameVertex3 = new PointIntXYZ(x3, y2);
    this.frameCoordinates = new int[] {
      x1, y1, x2, y2, x3, y2
    };

    this.startingEdge = new QuadEdge(frameVertex1, frameVertex2);
    final QuadEdge edge2 = new QuadEdge(frameVertex2, frameVertex3);
    this.startingEdge.sym().splice(edge2);
    final QuadEdge edge3 = new QuadEdge(frameVertex3, frameVertex1);
    edge2.sym().splice(edge3);
    edge3.sym().splice(this.startingEdge);
    this.lastEdge = this.startingEdge;
  }

  /**
   * Creates a new QuadEdge connecting the destination of edge1 to the origin of
   * edge2, in such a way that all three have the same left face after the
   * connection is complete. Additionally, the data pointers of the new edge
   * are set.
   *
   * @return the connected edge.
   */
  private QuadEdge connectEdges(QuadEdge edge, final QuadEdge startEdge) {
    QuadEdge base = startEdge;
    QuadEdge leftNext = edge.getLeftNext();
    do {
      final QuadEdge edge2 = base.sym();
      final PointIntXYZ toPoint1 = edge.getToPoint();
      final PointIntXYZ fromPoint2 = edge2.getFromPoint();
      this.edgeCount++;
      base = new QuadEdge(toPoint1, fromPoint2);
      base.splice(leftNext);
      base.sym().splice(edge2);
      edge = base.oPrev();
      leftNext = edge.getLeftNext();
    } while (leftNext != startEdge);
    return edge;
  }

  /**
   * Deletes a quadedge from the subdivision. Linked quadedges are updated to
   * reflect the deletion.
   *
   * @param edge
   *          the quadedge to delete
   */
  public void delete(final QuadEdge edge) {
    if (edge == this.lastEdge) {
      this.lastEdge = edge.getFromNextEdge();
      if (!this.lastEdge.isLive()) {
        this.lastEdge = this.startingEdge;
      }
    }
    final QuadEdge eSym = edge.sym();
    final QuadEdge eRot = edge.rot();
    final QuadEdge eRotSym = edge.rot().sym();

    this.edgeCount--;
    edge.splice(edge.oPrev());
    edge.sym().splice(edge.sym().oPrev());

    edge.delete();
    eSym.delete();
    eRot.delete();
    eRotSym.delete();
  }

  /**
   * Stores the edges for a visited triangle. Also pushes sym (neighbour) edges
   * on stack to visit later.
   *
   * @param edge
   * @param edgeStack
   * @return the visited triangle edges
   * or null if the triangle should not be visited (for instance, if it is
   *         outer)
   */
  private boolean fetchTriangleToVisit(final QuadEdge edge, final Deque<QuadEdge> edgeStack,
    final short visitIndex, final double[] coordinates) {
    QuadEdge currentEdge = edge;
    boolean isFrame = false;
    int offset = 0;
    do {
      final PointIntXYZ fromPoint = currentEdge.getFromPoint();
      final int fromX = fromPoint.getX();
      final int fromY = fromPoint.getY();
      final int fromZ = fromPoint.getZ();
      coordinates[offset++] = this.geometryFactory.toDoubleX(fromX);
      coordinates[offset++] = this.geometryFactory.toDoubleX(fromY);
      coordinates[offset++] = this.geometryFactory.toDoubleX(fromZ);
      if (isFrameCoordinate(fromX, fromY)) {
        isFrame = true;
      }

      // push sym edges to visit next
      final QuadEdge sym = currentEdge.sym();
      if (!sym.isVisited(visitIndex)) {
        edgeStack.push(sym);
      }
      currentEdge.setVisited(visitIndex);

      currentEdge = currentEdge.getLeftNext();
    } while (currentEdge != edge);

    if (isFrame) {
      return false;
    } else {
      return true;
    }
  }

  private boolean fetchTriangleToVisit(final QuadEdge edge, final Deque<QuadEdge> edgeStack,
    final short visitIndex, final int[] coordinates) {
    QuadEdge currentEdge = edge;
    boolean isFrame = false;
    int offset = 0;
    do {
      final PointIntXYZ fromPoint = currentEdge.getFromPoint();
      final int fromX = fromPoint.getX();
      final int fromY = fromPoint.getY();
      final int fromZ = fromPoint.getZ();
      coordinates[offset++] = fromX;
      coordinates[offset++] = fromY;
      coordinates[offset++] = fromZ;
      if (isFrameCoordinate(fromX, fromY)) {
        isFrame = true;
      }

      // push sym edges to visit next
      final QuadEdge sym = currentEdge.sym();
      if (!sym.isVisited(visitIndex)) {
        edgeStack.push(sym);
      }
      currentEdge.setVisited(visitIndex);

      currentEdge = currentEdge.getLeftNext();
    } while (currentEdge != edge);

    if (isFrame) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * <p>Locates an edge of a triangle which contains a location
   * specified by a point with x, y coordinates.</p>
   *
   * <p>The point is either on the edge or it is contained in a triangle that the edge is part of.</p>
   *
   * This locate algorithm relies on the subdivision being Delaunay. For
   * non-Delaunay subdivisions, this may loop for ever.
   *
   * @param x The point's x coordinate.
   * @param y The point's y coordinate.
   * @returns The QuadEdge which is part of a triangle that intersects the point.
   * @throws LocateFailureException  if the location algorithm fails to converge in a reasonable number of iterations
   */

  public QuadEdge findQuadEdge(final int x, final int y) {
    QuadEdge currentEdge = this.lastEdge;

    final int maxIterations = this.edgeCount;
    for (int interationCount = 1; interationCount < maxIterations; interationCount++) {
      final PointIntXYZ fromPoint = currentEdge.getFromPoint();
      final int x1 = fromPoint.getX();
      final int y1 = fromPoint.getY();
      if (x == x1 && y == y1) {
        this.lastEdge = currentEdge;
        return currentEdge;
      } else {
        final PointIntXYZ toPoint = currentEdge.getToPoint();
        final int x2 = toPoint.getX();
        final int y2 = toPoint.getY();
        if (x == x2 && y == y2) {
          this.lastEdge = currentEdge;
          return currentEdge;
        } else if (Side.getSide(x1, y1, x2, y2, x, y) == Side.RIGHT) {
          currentEdge = currentEdge.sym();
        } else {
          final QuadEdge fromNextEdge = currentEdge.getFromNextEdge();
          final PointIntXYZ fromNextEdgeToPoint = fromNextEdge.getToPoint();
          final int fromNextEdgeX2 = fromNextEdgeToPoint.getX();
          final int fromNextEdgeY2 = fromNextEdgeToPoint.getY();
          if (Side.getSide(x1, y1, fromNextEdgeX2, fromNextEdgeY2, x, y) == Side.LEFT) {
            currentEdge = fromNextEdge;
          } else {
            final QuadEdge toNextEdge = currentEdge.getToNextEdge();
            final PointIntXYZ toNextEdgeFromPoint = toNextEdge.getFromPoint();
            final int toNextEdgeX1 = toNextEdgeFromPoint.getX();
            final int toNextEdgeY1 = toNextEdgeFromPoint.getY();

            if (Side.getSide(toNextEdgeX1, toNextEdgeY1, x2, y2, x, y) == Side.LEFT) {
              currentEdge = toNextEdge;
            } else {
              this.lastEdge = currentEdge; // contained in triangle for edge
              return currentEdge;
            }
          }
        }
      }
    }
    throw new LocateFailureException(currentEdge);
  }

  public void forEachTriangle(final TriangleConsumer action) {
    if (this.visitIndex == Short.MAX_VALUE) {
      this.visitIndex = Short.MIN_VALUE;
    }
    final short visitIndex = ++this.visitIndex;
    final double[] coordinates = new double[9];
    final Deque<QuadEdge> edgeStack = new LinkedList<>();
    edgeStack.push(this.startingEdge);

    while (!edgeStack.isEmpty()) {
      final QuadEdge edge = edgeStack.pop();
      if (!edge.isVisited(visitIndex)) {
        if (fetchTriangleToVisit(edge, edgeStack, visitIndex, coordinates)) {
          final double x1 = coordinates[0];
          final double y1 = coordinates[1];
          final double z1 = coordinates[2];

          final double x2 = coordinates[3];
          final double y2 = coordinates[4];
          final double z2 = coordinates[5];

          final double x3 = coordinates[6];
          final double y3 = coordinates[7];
          final double z3 = coordinates[8];
          action.accept(x1, y1, z1, x2, y2, z2, x3, y3, z3);
        }
      }
    }
  }

  public void forEachTriangle(final TriangleConsumerInt action) {
    if (this.visitIndex == Short.MAX_VALUE) {
      this.visitIndex = Short.MIN_VALUE;
    }
    final short visitIndex = ++this.visitIndex;
    final int[] coordinates = new int[9];
    final Deque<QuadEdge> edgeStack = new LinkedList<>();
    edgeStack.push(this.startingEdge);

    while (!edgeStack.isEmpty()) {
      final QuadEdge edge = edgeStack.pop();
      if (!edge.isVisited(visitIndex)) {
        if (fetchTriangleToVisit(edge, edgeStack, visitIndex, coordinates)) {
          final int x1 = coordinates[0];
          final int y1 = coordinates[1];
          final int z1 = coordinates[2];

          final int x2 = coordinates[3];
          final int y2 = coordinates[4];
          final int z2 = coordinates[5];

          final int x3 = coordinates[6];
          final int y3 = coordinates[7];
          final int z3 = coordinates[8];
          action.accept(x1, y1, z1, x2, y2, z2, x3, y3, z3);
        }
      }
    }
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  /**
   * Inserts a new vertex into a subdivision representing a Delaunay
   * triangulation, and fixes the affected edges so that the result is still a
   * Delaunay triangulation.
   *
   * @param vertices The point vertices to add.
   *
   * @throws LocateFailureException if the location algorithm fails to converge in a reasonable number of iterations
   */
  public void insertVertex(final PointIntXYZ vertex) throws LocateFailureException {
    final int x = vertex.getX();
    final int y = vertex.getY();
    /*
     * This code is based on Guibas and Stolfi (1985), with minor modifications
     * and a bug fix from Dani Lischinski (Graphic Gems 1993). (The modification
     * I believe is the test for the inserted site falling exactly on an
     * existing edge. Without this test zero-width triangles have been observed
     * to be created)
     */
    QuadEdge edge = findQuadEdge(x, y);

    PointIntXYZ edgeFromPoint = edge.getFromPoint();
    {
      final int x1 = edgeFromPoint.getX();
      final int y1 = edgeFromPoint.getY();
      if (x1 == x && y1 == y) {
        return;
      } else {
        final PointIntXYZ toPoint = edge.getToPoint();
        final int x2 = toPoint.getX();
        final int y2 = toPoint.getY();

        if (x2 == x && y2 == y) {
          return;
        } else {
          final double distance = LineSegmentUtil.distanceLinePoint(x1, y1, x2, y2, x, y);
          if (distance < 1) {
            edge = edge.oPrev();
            delete(edge.getFromNextEdge());
            edgeFromPoint = edge.getFromPoint();
          }
        }
      }
    }
    final PointIntXYZ fromPoint = edgeFromPoint;
    /*
     * Connect the new point to the vertices of the containing triangle (or
     * quadrilateral, if the new point fell on an existing edge.)
     */
    this.edgeCount++;
    final QuadEdge base = new QuadEdge(fromPoint, vertex);
    base.splice(edge);
    edge = connectEdges(edge, base);
    swapEdges(base, edge, x, y);
  }

  /**
   * Insert all the vertices into the triangulation. The vertices must have the same
   * {@link GeometryFactory} as the this and therefore precision model.
   *
   * @param vertices The point vertices to add.
   *
   * @throws LocateFailureException if the location algorithm fails to converge in a reasonable number of iterations
   */
  public void insertVertices(final Iterable<? extends PointIntXYZ> vertices)
    throws LocateFailureException {
    int lastX = Integer.MIN_VALUE;
    int lastY = Integer.MIN_VALUE;
    for (final PointIntXYZ vertex : vertices) {
      final int x = vertex.getX();
      final int y = vertex.getY();
      if (x != lastX || y != lastY) {
        insertVertex(vertex);
      }
      lastX = x;
      lastY = y;

    }
  }

  private boolean isFrameCoordinate(final int x, final int y) {
    final int[] frameCoordinates = this.frameCoordinates;
    for (int coordinateIndex = 0; coordinateIndex < 6;) {
      final int frameX = frameCoordinates[coordinateIndex++];
      if (x == frameX) {
        final int frameY = frameCoordinates[coordinateIndex++];
        if (y == frameY) {
          return true;
        }
      }
    }
    return false;
  }

  private void swapEdges(final QuadEdge startEdge, QuadEdge edge, final int x, final int y) {
    // Examine suspect edges to ensure that the Delaunay condition
    // is satisfied.
    do {
      if (edge.isSwapRequired(x, y)) {
        edge.swap();
        edge = edge.oPrev();
      } else {
        final QuadEdge fromNextEdge = edge.getFromNextEdge();
        if (fromNextEdge == startEdge) {
          return;
        } else {
          edge = fromNextEdge.getLeftPrevious();
        }
      }
    } while (true);
  }

}
