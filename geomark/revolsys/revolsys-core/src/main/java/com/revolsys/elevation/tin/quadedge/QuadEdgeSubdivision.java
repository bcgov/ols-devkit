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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import com.revolsys.elevation.tin.TriangleConsumer;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Side;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.geometry.model.impl.TriangleDoubleXYZ;

/**
 * A class that contains the {@link QuadEdge}s representing a planar
 * subdivision that models a triangulation.
 * The subdivision is constructed using the
 * quadedge algebra defined in the classs {@link QuadEdge}.
 * All metric calculations
 * are done in the {@link PointDoubleXYZ} class.
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
  private final QuadEdge edge1;

  private final QuadEdge edge2;

  private final QuadEdge edge3;

  private int edgeCount = 0;

  private final double frameX1;

  private final double frameX2;

  private final double frameX3;

  private final double frameYBottom;

  private final double frameYTop;

  private final GeometryFactory geometryFactory;

  private QuadEdge lastEdge = null;

  private final double resolutionXY;

  private int triangleCount = 1;

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
  public QuadEdgeSubdivision(final double[] bounds, final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.resolutionXY = this.geometryFactory.getResolutionX();

    final double minX = bounds[0];
    final double minY = bounds[1];
    final double maxX = bounds[2];
    final double maxY = bounds[3];
    final double width = maxX - minX;
    final double height = maxY - minY;
    double offset = 0.0;
    if (width > height) {
      offset = width * 10.0;
    } else {
      offset = height * 10.0;
    }

    this.frameX1 = this.geometryFactory.makeXPrecise(minX + width / 2.0);
    this.frameYTop = this.geometryFactory.makeYPrecise(maxY + offset);

    this.frameX2 = this.geometryFactory.makeXPrecise(minX - offset);
    this.frameYBottom = this.geometryFactory.makeYPrecise(minY - offset);

    this.frameX3 = this.geometryFactory.makeYPrecise(maxX + offset);

    final Point frameVertex1 = new PointDoubleXY(this.frameX1, this.frameYTop);
    final Point frameVertex2 = new PointDoubleXY(this.frameX2, this.frameYBottom);
    final Point frameVertex3 = new PointDoubleXY(this.frameX3, this.frameYBottom);

    this.edge1 = makeEdge(frameVertex1, frameVertex2);
    this.edge2 = makeEdge(frameVertex2, frameVertex3);
    this.edge1.sym().splice(this.edge2);
    this.edge3 = makeEdge(frameVertex3, frameVertex1);
    this.edge2.sym().splice(this.edge3);
    this.edge3.sym().splice(this.edge1);
    this.lastEdge = this.edge1;
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
      final Point toPoint1 = edge.getToPoint();
      final Point fromPoint2 = edge2.getFromPoint();
      this.edgeCount++;
      base = new QuadEdge(toPoint1, fromPoint2);
      base.splice(leftNext);
      base.sym().splice(edge2);
      edge = base.oPrev();
      this.triangleCount++;
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
        this.lastEdge = this.edge1;
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
  private boolean fetchTriangleToVisit(final QuadEdge edge, final List<QuadEdge> edgeStack,
    final short visitIndex, final double[] coordinates) {
    QuadEdge currentEdge = edge;
    boolean isFrame = false;
    int offset = 0;
    do {
      final Point fromPoint = currentEdge.getFromPoint();
      final double fromX = fromPoint.getX();
      final double fromY = fromPoint.getY();
      coordinates[offset++] = fromX;
      coordinates[offset++] = fromY;
      coordinates[offset++] = fromPoint.getZ();
      if (isFrameCoordinate(fromX, fromY)) {
        isFrame = true;
      }

      // push sym edges to visit next
      final QuadEdge sym = currentEdge.sym();
      if (!sym.isVisited(visitIndex)) {
        edgeStack.add(sym);
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

  public QuadEdge findQuadEdge(final double x, final double y) {
    QuadEdge currentEdge = this.lastEdge;

    final int maxIterations = this.edgeCount;
    for (int interationCount = 1; interationCount < maxIterations; interationCount++) {
      final Point fromPoint = currentEdge.getFromPoint();
      final double x1 = fromPoint.getX();
      final double y1 = fromPoint.getY();
      if (x == x1 && y == y1) {
        this.lastEdge = currentEdge;
        return currentEdge;
      } else {
        final Point toPoint = currentEdge.getToPoint();
        final double x2 = toPoint.getX();
        final double y2 = toPoint.getY();
        if (x == x2 && y == y2) {
          this.lastEdge = currentEdge;
          return currentEdge;
        } else if (Side.getSide(x1, y1, x2, y2, x, y) == Side.RIGHT) {
          currentEdge = currentEdge.sym();
        } else {
          final QuadEdge fromNextEdge = currentEdge.getFromNextEdge();
          final Point fromNextEdgeToPoint = fromNextEdge.getToPoint();
          final double fromNextEdgeX2 = fromNextEdgeToPoint.getX();
          final double fromNextEdgeY2 = fromNextEdgeToPoint.getY();
          if (Side.getSide(x1, y1, fromNextEdgeX2, fromNextEdgeY2, x, y) == Side.LEFT) {
            currentEdge = fromNextEdge;
          } else {
            final QuadEdge toNextEdge = currentEdge.getToNextEdge();
            final Point toNextEdgeFromPoint = toNextEdge.getFromPoint();
            final double toNextEdgeX1 = toNextEdgeFromPoint.getX();
            final double toNextEdgeY1 = toNextEdgeFromPoint.getY();

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
    return null;
  }

  public void forEachTriangle(final Consumer<? super Triangle> action) {
    forEachTriangle((x1, y1, z1, x2, y2, z2, x3, y3, z3) -> {
      final Triangle triangle = new TriangleDoubleXYZ(x1, y1, z1, x2, y2, z2, x3, y3, z3);
      action.accept(triangle);
    });
  }

  public void forEachTriangle(final TriangleConsumer action) {
    if (this.visitIndex == Short.MAX_VALUE) {
      this.visitIndex = Short.MIN_VALUE;
    }
    final short visitIndex = ++this.visitIndex;
    final double[] coordinates = new double[9];
    final List<QuadEdge> edgeStack = new ArrayList<>(64);
    edgeStack.add(this.edge1);
    while (!edgeStack.isEmpty()) {
      final int size = edgeStack.size();
      final QuadEdge edge = edgeStack.remove(size - 1);
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

  public Geometry getBoundary() {
    final LineStringEditor lineBuilder = new LineStringEditor(this.geometryFactory);
    for (final QuadEdge startingEdge : Arrays.asList(this.edge1, this.edge3, this.edge2)) {
      QuadEdge edge = startingEdge;
      do {
        final Point toPoint = edge.getToPoint();
        final double toX = toPoint.getX();
        final double toY = toPoint.getY();
        if (isFrameCoordinate(toX, toY)) {
        } else {
          lineBuilder.appendVertex(toPoint, false);
        }
        edge = edge.getFromNextEdge();
      } while (edge != startingEdge);
    }
    return lineBuilder.newBestGeometry();
  }

  /**
   * Gets the geometry for the edges in the subdivision as a {@link Lineal}
   * containing 2-point lines.
   *
   * @param geometryFactory the GeometryFactory to use
   * @return a MultiLineString
   */
  public Lineal getEdgesLineal(final GeometryFactory geometryFactory) {
    final List<QuadEdge> quadEdges = getPrimaryEdges(false);
    final LineString[] lines = new LineString[quadEdges.size()];
    int i = 0;
    for (final QuadEdge edge : quadEdges) {
      lines[i++] = edge.newLineString(geometryFactory);
    }
    return geometryFactory.lineal(lines);
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  /**
   * Gets all primary quadedges in the subdivision.
   * A primary edge is a {@link QuadEdge}
   * which occupies the 0'th position in its array of associated quadedges.
   * These provide the unique geometric edges of the triangulation.
   *
   * @param includeFrame true if the frame edges are to be included
   * @return a List of QuadEdges
   */
  public List<QuadEdge> getPrimaryEdges(final boolean includeFrame) {
    final List<QuadEdge> edges = new ArrayList<>();
    final Stack<QuadEdge> edgeStack = new Stack<>();
    edgeStack.push(this.edge1);

    final Set<QuadEdge> visitedEdges = new HashSet<>();

    while (!edgeStack.empty()) {
      final QuadEdge edge = edgeStack.pop();
      if (!visitedEdges.contains(edge)) {
        final QuadEdge priQE = edge.getPrimary();

        if (includeFrame || !isFrameEdge(priQE)) {
          edges.add(priQE);
        }

        edgeStack.push(edge.getFromNextEdge());
        edgeStack.push(edge.sym().getFromNextEdge());

        visitedEdges.add(edge);
        visitedEdges.add(edge.sym());
      }
    }
    return edges;
  }

  public int getTriangleCount() {
    return this.triangleCount;
  }

  /**
   * Gets the geometry for the triangles in a triangulated subdivision as a {@link Polygonal}.
   *
   * @param geometryFactory the GeometryFactory to use
   * @return a GeometryCollection of triangular Polygons
   */
  public Polygonal getTrianglesPolygonal(final GeometryFactory geometryFactory) {
    final List<Triangle> triangles = new ArrayList<>();
    forEachTriangle((final double x1, final double y1, final double z1, final double x2,
      final double y2, final double z2, final double x3, final double y3, final double z3) -> {
      final Triangle triangle = new TriangleDoubleXYZ(x1, y1, z1, x2, y2, z2, x3, y3, z3);
      triangles.add(triangle);
    });
    return geometryFactory.polygonal(triangles);
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
  public void insertVertex(final Point vertex) throws LocateFailureException {
    final double x = vertex.getX();
    final double y = vertex.getY();
    /*
     * This code is based on Guibas and Stolfi (1985), with minor modifications
     * and a bug fix from Dani Lischinski (Graphic Gems 1993). (The modification
     * I believe is the test for the inserted site falling exactly on an
     * existing edge. Without this test zero-width triangles have been observed
     * to be created)
     */
    QuadEdge edge = findQuadEdge(x, y);
    if (edge != null) {
      Point edgeFromPoint = edge.getFromPoint();
      {
        final double x1 = edgeFromPoint.getX();
        final double y1 = edgeFromPoint.getY();
        if (x1 == x && y1 == y) {
          return;
        } else {
          final Point toPoint = edge.getToPoint();
          final double x2 = toPoint.getX();
          final double y2 = toPoint.getY();

          if (x2 == x && y2 == y) {
            return;
          } else {
            final double distance = LineSegmentUtil.distanceLinePoint(x1, y1, x2, y2, x, y);
            if (distance < this.resolutionXY) {
              // the point lies exactly on an edge, so delete the edge
              // (it will be replaced by a pair of edges which have the point as
              // a
              // vertex)
              edge = edge.oPrev();
              delete(edge.getFromNextEdge());
              edgeFromPoint = edge.getFromPoint();
              this.triangleCount -= 2;
            }
          }
        }
      }
      /*
       * Connect the new point to the vertices of the containing triangle (or
       * quadrilateral, if the new point fell on an existing edge.)
       */
      final QuadEdge base = makeEdge(edgeFromPoint, vertex);
      base.splice(edge);

      edge = connectEdges(edge, base);

      swapEdges(base, edge, x, y);
    }
  }

  /**
   * Insert all the vertices into the triangulation. The vertices must have the same
   * {@link GeometryFactory} as the this and therefore precision model.
   *
   * @param vertices The point vertices to add.
   *
   * @throws LocateFailureException if the location algorithm fails to converge in a reasonable number of iterations
   */
  public void insertVertices(final Iterable<? extends Point> vertices)
    throws LocateFailureException {
    double lastX = Double.NaN;
    double lastY = Double.NaN;
    for (final Point vertex : vertices) {
      final double x = vertex.getX();
      final double y = vertex.getY();
      if (x != lastX || y != lastY) {
        insertVertex(vertex);
      }
      lastX = x;
      lastY = y;

    }
  }

  private boolean isFrameCoordinate(final double x, final double y) {
    if (y == this.frameYTop && x == this.frameX1) {
      return true;
    } else if (y == this.frameYBottom) {
      if (x == this.frameX2) {
        return true;
      } else if (x == this.frameX3) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests whether a QuadEdge is an edge incident on a frame triangle vertex.
   *
   * @param edge
   *          the edge to test
   * @return true if the edge is connected to the frame triangle
   */
  public boolean isFrameEdge(final QuadEdge edge) {
    {
      final Point point = edge.getFromPoint();
      final double x = point.getX();
      final double y = point.getY();
      if (isFrameCoordinate(x, y)) {
        return true;
      }
    }
    {
      final Point point = edge.getToPoint();
      final double x = point.getX();
      final double y = point.getY();
      if (isFrameCoordinate(x, y)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Creates a new quadedge, recording it in the edges list.
   *
   * @param fromPoint
   * @param toPoint
   * @return a new quadedge
   */
  public QuadEdge makeEdge(final Point fromPoint, final Point toPoint) {
    this.edgeCount++;
    return new QuadEdge(fromPoint, toPoint);
  }

  public void print(final QuadEdge edge) {
    System.out.println(edge + "\t" + edge.hashCode());
  }

  private void swapEdges(final QuadEdge startEdge, QuadEdge edge, final double x, final double y) {
    // Examine suspect edges to ensure that the Delaunay condition
    // is satisfied.
    do {
      if (edge.isSwapRequired(x, y)) {
        edge.swap();
        this.triangleCount++;
        edge = edge.oPrev();
      } else {
        final QuadEdge fromNextEdge = edge.getFromNextEdge();
        if (fromNextEdge == startEdge) {
          return; // no more suspect edges.
        } else {
          edge = fromNextEdge.getLeftPrevious();
        }
      }
    } while (true);
  }

}
