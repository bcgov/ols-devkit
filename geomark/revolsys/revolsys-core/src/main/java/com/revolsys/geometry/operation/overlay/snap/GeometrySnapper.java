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

package com.revolsys.geometry.operation.overlay.snap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.model.util.GeometryTransformer;
import com.revolsys.geometry.model.vertex.Vertex;

/**
 * Snaps the vertices and segments of a {@link Geometry}
 * to another Geometry's vertices.
 * A snap distance tolerance is used to control where snapping is performed.
 * Snapping one geometry to another can improve
 * robustness for overlay operations by eliminating
 * nearly-coincident edges
 * (which cause problems during noding and intersection calculation).
 * It can also be used to eliminate artifacts such as narrow slivers, spikes and gores.
 * <p>
 * Too much snapping can result in invalid topology
 * being created, so the number and location of snapped vertices
 * is decided using heuristics to determine when it
 * is safe to snap.
 * This can result in some potential snaps being omitted, however.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class GeometrySnapper {
  private static final double SNAP_PRECISION_FACTOR = 1e-9;

  /**
   * Estimates the snap tolerance for a Geometry, taking into account its precision model.
   *
   * @param g a Geometry
   * @return the estimated snap tolerance
   */
  public static double computeOverlaySnapTolerance(final Geometry g) {
    double snapTolerance = computeSizeBasedSnapTolerance(g);

    /**
     * Overlay is carried out in the precision model
     * of the two inputs.
     * If this precision model is of type FIXED, then the snap tolerance
     * must reflect the precision grid size.
     * Specifically, the snap tolerance should be at least
     * the distance from a corner of a precision grid cell
     * to the centre point of the cell.
     */
    final GeometryFactory geometryFactory = g.getGeometryFactory();
    if (!geometryFactory.isFloating()) {
      final double fixedSnapTol = 1 / geometryFactory.getScaleXY() * 2 / 1.415;
      if (fixedSnapTol > snapTolerance) {
        snapTolerance = fixedSnapTol;
      }
    }
    return snapTolerance;
  }

  public static double computeOverlaySnapTolerance(final Geometry g0, final Geometry g1) {
    return Math.min(computeOverlaySnapTolerance(g0), computeOverlaySnapTolerance(g1));
  }

  public static double computeSizeBasedSnapTolerance(final Geometry g) {
    final BoundingBox env = g.getBoundingBox();
    final double minDimension = Math.min(env.getHeight(), env.getWidth());
    final double snapTol = minDimension * SNAP_PRECISION_FACTOR;
    return snapTol;
  }

  /**
   * Snaps two geometries together with a given tolerance.
   *
   * @param g0 a geometry to snap
   * @param g1 a geometry to snap
   * @param snapTolerance the tolerance to use
   * @return the snapped geometries
   */
  public static Geometry[] snap(final Geometry g0, final Geometry g1, final double snapTolerance) {
    final Geometry[] snapGeom = new Geometry[2];
    final GeometrySnapper snapper0 = new GeometrySnapper(g0);
    snapGeom[0] = snapper0.snapTo(g1, snapTolerance);

    /**
     * Snap the second geometry to the snapped first geometry
     * (this strategy minimizes the number of possible different points in the result)
     */
    final GeometrySnapper snapper1 = new GeometrySnapper(g1);
    snapGeom[1] = snapper1.snapTo(snapGeom[0], snapTolerance);

    // System.out.println(snap[0]);
    // System.out.println(snap[1]);
    return snapGeom;
  }

  /**
   * Snaps a geometry to itself.
   * Allows optionally cleaning the result to ensure it is
   * topologically valid
   * (which fixes issues such as topology collapses in polygonal inputs).
   * <p>
   * Snapping a geometry to itself can remove artifacts such as very narrow slivers, gores and spikes.
   *
   *@param geom the geometry to snap
   *@param snapTolerance the snapping tolerance
   *@param cleanResult whether the result should be made valid
   * @return a new snapped Geometry
   */
  public static Geometry snapToSelf(final Geometry geom, final double snapTolerance,
    final boolean cleanResult) {
    final GeometrySnapper snapper0 = new GeometrySnapper(geom);
    return snapper0.snapToSelf(snapTolerance, cleanResult);
  }

  private final Geometry srcGeom;

  /**
   * Creates a new snapper acting on the given geometry
   *
   * @param srcGeom the geometry to snap
   */
  public GeometrySnapper(final Geometry srcGeom) {
    this.srcGeom = srcGeom;
  }

  private Collection<Point> extractTargetCoordinates(final Geometry geometry) {
    // TODO: should do this more efficiently. Use CoordSeq filter to get points,
    // KDTree for uniqueness & queries
    final Set<Point> points = new TreeSet<>();
    for (final Vertex vertex : geometry.vertices()) {
      points.add(vertex.newPoint2D());
    }
    return new ArrayList<>(points);
  }

  /**
   * Snaps the vertices in the component {@link LineString}s
   * of the source geometry
   * to the vertices of the given snap geometry.
   *
   * @param snapGeom a geometry to snap the source to
   * @return a new snapped Geometry
   */
  public Geometry snapTo(final Geometry snapGeom, final double snapTolerance) {
    final Collection<Point> snapPoints = extractTargetCoordinates(snapGeom);
    if (snapPoints.isEmpty()) {
      return this.srcGeom;
    } else {
      final SnapTransformer snapTrans = new SnapTransformer(snapTolerance, snapPoints);
      return snapTrans.transform(this.srcGeom);
    }
  }

  /**
   * Snaps the vertices in the component {@link LineString}s
   * of the source geometry
   * to the vertices of the same geometry.
   * Allows optionally cleaning the result to ensure it is
   * topologically valid
   * (which fixes issues such as topology collapses in polygonal inputs).
   *
   *@param snapTolerance the snapping tolerance
   *@param cleanResult whether the result should be made valid
   * @return a new snapped Geometry
   */
  public Geometry snapToSelf(final double snapTolerance, final boolean cleanResult) {
    final Collection<Point> snapPoints = extractTargetCoordinates(this.srcGeom);
    if (snapPoints.isEmpty()) {
      return this.srcGeom;
    } else {
      final SnapTransformer snapTrans = new SnapTransformer(snapTolerance, snapPoints, true);
      final Geometry snappedGeom = snapTrans.transform(this.srcGeom);
      Geometry result = snappedGeom;
      if (cleanResult && result instanceof Polygonal) {
        // TODO: use better cleaning approach
        result = snappedGeom.buffer(0);
      }
      return result;
    }
  }
}

class SnapTransformer extends GeometryTransformer {
  private final boolean isSelfSnap;

  private final Collection<Point> snapPoints;

  private final double snapTolerance;

  SnapTransformer(final double snapTolerance, final Collection<Point> snapPoints) {
    this(snapTolerance, snapPoints, false);
  }

  SnapTransformer(final double snapTolerance, final Collection<Point> snapPoints,
    final boolean isSelfSnap) {
    this.snapTolerance = snapTolerance;
    this.snapPoints = snapPoints;
    this.isSelfSnap = isSelfSnap;
  }

  /**
   * Finds a src segment which snaps to (is close to) the given snap point.
   * <p>
   * Only a single segment is selected for snapping.
   * This prevents multiple segments snapping to the same snap vertex,
   * which would almost certainly cause invalid geometry
   * to be created.
   * (The heuristic approach to snapping used here
   * is really only appropriate when
   * snap pts snap to a unique spot on the src geometry.)
   * <p>
   * Also, if the snap vertex occurs as a vertex in the src coordinate list,
   * no snapping is performed.
   *
   * @param snapPt the point to snap to
   * @param line the source segment coordinates
   * @param axisCount
   * @return the index of the snapped segment
   * or -1 if no segment snaps to the snap point
   */
  private int findSegmentIndexToSnap(final Point snapPt, final LineString line) {
    double minDist = Double.MAX_VALUE;
    int snapIndex = -1;
    final double snapX = snapPt.getX();
    final double snapY = snapPt.getY();
    final int vertexCount = line.getVertexCount();
    double x1 = line.getX(0);
    double y1 = line.getY(0);
    for (int i = 0; i < vertexCount - 1; i++) {
      final double x2 = line.getX(i + 1);
      final double y2 = line.getY(i + 1);

      /**
       * Check if the snap pt is equal to one of the segment endpoints.
       *
       * If the snap pt is already in the src list, don't snap at all.
       */
      if (snapPt.equalsVertex(x1, y1) || snapPt.equalsVertex(x2, y2)) {
        if (this.isSelfSnap) {
          continue;
        } else {
          return -1;
        }
      }

      final double dist = LineSegmentUtil.distanceLinePoint(x1, y1, x2, y2, snapX, snapY);
      if (dist < this.snapTolerance && dist < minDist) {
        minDist = dist;
        snapIndex = i;
      }
      x1 = x2;
      y1 = y2;
    }
    return snapIndex;
  }

  private Point findSnapForVertex(final double x, final double y) {
    for (final Point snapPt : this.snapPoints) {
      // if point is already equal to a src pt, don't snap
      if (snapPt.equalsVertex(x, y)) {
        return null;
      } else if (snapPt.distancePoint(x, y) < this.snapTolerance) {
        return snapPt;
      }
    }
    return null;
  }

  private LineString snapLine(final LineString line) {
    final LineString newLine = snapVertices(line);
    return snapSegments(newLine);
  }

  /**
   * Snap segments of the source to nearby snap vertices.
   * Source segments are "cracked" at a snap vertex.
   * A single input segment may be snapped several times
   * to different snap vertices.
   * <p>
   * For each distinct snap vertex, at most one source segment
   * is snapped to.  This prevents "cracking" multiple segments
   * at the same point, which would likely cause
   * topology collapse when being used on polygonal linework.
   *
   * @param newCoordinates the coordinates of the source linestring to be snapped
   * @param snapPoints the target snap vertices
   */
  private LineString snapSegments(LineString line) {
    LineStringEditor newLine = null;
    for (final Point snapPoint : this.snapPoints) {
      final int index = findSegmentIndexToSnap(snapPoint, line);
      /**
       * If a segment to snap to was found, "crack" it at the snap pt.
       * The new pt is inserted immediately into the src segment list,
       * so that subsequent snapping will take place on the modified segments.
       * Duplicate points are not added.
       */
      if (index >= 0) {
        if (newLine == null) {
          if (line instanceof LineStringEditor) {
            newLine = (LineStringEditor)line;
          } else {
            newLine = LineStringEditor.newLineStringEditor(line);
            line = newLine;
          }
        }
        newLine.insertVertex(index + 1, snapPoint, false);
      }
    }
    if (newLine == null) {
      return line;
    } else {
      return newLine;
    }
  }

  /**
   * Snap source vertices to vertices in the target.
   *
   * @param newCoordinates the points to snap
   * @param snapPoints the points to snap to
   */
  private LineString snapVertices(final LineString line) {
    LineStringEditor newLine = null;
    final int vertexCount = line.getVertexCount();
    final boolean closed = line.isClosed();
    // if src is a ring then don't snap final vertex
    final int end = closed ? vertexCount - 1 : vertexCount;
    for (int i = 0; i < end; i++) {
      final double x = line.getX(i);
      final double y = line.getY(i);
      final Point snapVert = findSnapForVertex(x, y);
      if (snapVert != null) {
        if (newLine == null) {
          newLine = LineStringEditor.newLineStringEditor(line);
          if (i == 0 && closed) {
            // keep final closing point in synch (rings only)
            newLine.setVertex(vertexCount - 1, snapVert);
          }
        }
        newLine.setVertex(i, snapVert);
      }
    }
    if (newLine == null) {
      return line;
    } else {
      return newLine;
    }
  }

  @Override
  protected LineString transformCoordinates(final LineString line, final Geometry parent) {
    final LineString newLine = snapLine(line);
    return newLine;
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
  @Override
  protected Geometry transformLinearRing(final LinearRing ring, final Geometry parent) {
    if (ring == null) {
      return this.factory.linearRing();
    } else {
      final LineString newLine = transformCoordinates(ring, ring);
      if (newLine == ring) {
        return ring;
      } else {
        final int vertexCount = newLine.getVertexCount();
        // ensure a valid LinearRing
        if (vertexCount > 0 && vertexCount < 4 && !isPreserveType()) {
          return newLine.newLineString();
        } else {
          return newLine.newLinearRing();
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
  @Override
  protected LineString transformLineString(final LineString line) {
    final LineString newLine = transformCoordinates(line, line);
    if (newLine == line) {
      return line;
    } else {
      return newLine.newLineString();
    }
  }

  @Override
  protected Point transformPoint(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    final Point snapVert = findSnapForVertex(x, y);
    if (snapVert == null) {
      return point;
    } else {
      return snapVert;
    }
  }

}
