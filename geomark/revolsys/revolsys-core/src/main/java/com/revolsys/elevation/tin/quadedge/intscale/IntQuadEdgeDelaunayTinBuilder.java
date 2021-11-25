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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.revolsys.elevation.tin.IntArrayScaleTriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangleConsumer;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

/**
 * A utility class which creates Delaunay Trianglulations
 * from collections of points and extract the resulting
 * triangulation edges or triangles as geometries.
 *
 * @author Martin Davis
 *
 */
public class IntQuadEdgeDelaunayTinBuilder {
  private int[] bounds = new int[] {
    Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE
  };

  private final List<PointIntXYZ> vertices = new ArrayList<>();

  private QuadEdgeSubdivision subdivision;

  private GeometryFactory geometryFactory;

  private boolean sortVertices = false;

  public IntQuadEdgeDelaunayTinBuilder(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      throw new NullPointerException("A geometryFactory must be specified");
    } else {
      this.geometryFactory = geometryFactory.convertAxisCount(3);
      if (this.geometryFactory.getScaleX() == 0) {
        throw new IllegalArgumentException("scaleX must not be 0");
      }
      if (this.geometryFactory.getScaleY() == 0) {
        throw new IllegalArgumentException("scaleY must not be 0");
      }
      if (this.geometryFactory.getScaleZ() == 0) {
        throw new IllegalArgumentException("scaleZ must not be 0");
      }
    }
  }

  public IntQuadEdgeDelaunayTinBuilder(final GeometryFactory geometryFactory, final int minX,
    final int minY, final int maxX, final int maxY) {
    this(geometryFactory);
    this.bounds = new int[] {
      minX, minY, maxX, maxY
    };
  }

  public void addVertex(final PointIntXYZ vertex) {
    this.vertices.add(vertex);
  }

  public void buildTin() {
    if (this.subdivision == null) {
      this.subdivision = new QuadEdgeSubdivision(this.bounds, this.geometryFactory);
      insertVertices(this.subdivision, this.vertices);
    }
  }

  public void forEachTriangle(final TriangleConsumer action) {
    buildTin();
    this.subdivision.forEachTriangle(action);
  }

  public void forEachTriangleInt(final TriangleConsumerInt action) {
    buildTin();
    this.subdivision.forEachTriangle(action);
  }

  public BoundingBox getBoundingBox() {
    final double minX = this.geometryFactory.toDoubleX(this.bounds[0]);
    final double minY = this.geometryFactory.toDoubleY(this.bounds[1]);
    final double maxX = this.geometryFactory.toDoubleX(this.bounds[2]);
    final double maxY = this.geometryFactory.toDoubleY(this.bounds[3]);
    return this.geometryFactory.newBoundingBox(2, minX, minY, maxX, maxY);
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  /**
   * Gets the {@link QuadEdgeSubdivision} which models the computed triangulation.
   *
   * @return the subdivision containing the triangulation
   */
  public QuadEdgeSubdivision getSubdivision() {
    buildTin();
    return this.subdivision;
  }

  public void insertVertex(final int x, final int y, final int z) {
    if (x < this.bounds[0]) {
      this.bounds[0] = x;
    }
    if (x > this.bounds[2]) {
      this.bounds[2] = x;
    }
    if (y < this.bounds[1]) {
      this.bounds[1] = y;
    }
    if (y > this.bounds[3]) {
      this.bounds[3] = y;
    }
    final PointIntXYZ vertex = new PointIntXYZ(x, y, z);
    this.vertices.add(vertex);
    if (this.subdivision != null) {
      this.subdivision.insertVertex(vertex);
    }
  }

  public void insertVertex(final Point point) {
    final Point convertedPoint = point.convertPoint2d(this.geometryFactory);
    final double x = convertedPoint.getX();
    final double y = convertedPoint.getY();
    final double z = point.getZ();
    final int xInt = this.geometryFactory.toIntX(x);
    final int yInt = this.geometryFactory.toIntY(y);
    final int zInt = this.geometryFactory.toIntZ(z);
    insertVertex(xInt, yInt, zInt);
  }

  protected void insertVertices(final QuadEdgeSubdivision subdivision,
    final List<PointIntXYZ> vertices) {
    if (this.sortVertices) {
      Collections.sort(vertices);
    }
    subdivision.insertVertices(vertices);
  }

  public boolean isSortVertices() {
    return this.sortVertices;
  }

  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork() {
    buildTin();
    final BoundingBox boundingBox = getBoundingBox();
    final AtomicInteger triangleCounter = new AtomicInteger();
    forEachTriangleInt((x1, y1, z1, x2, y2, z2, x3, y3, z3) -> {
      triangleCounter.incrementAndGet();
    });
    final int triangleCount = triangleCounter.get();
    final int[] triangleXCoordinates = new int[triangleCount * 3];
    final int[] triangleYCoordinates = new int[triangleCount * 3];
    final int[] triangleZCoordinates = new int[triangleCount * 3];
    forEachTriangleInt(new TriangleConsumerInt() {

      private int coordinateIndex = 0;

      @Override
      public void accept(final int x1, final int y1, final int z1, final int x2, final int y2,
        final int z2, final int x3, final int y3, final int z3) {
        triangleXCoordinates[this.coordinateIndex] = x1;
        triangleYCoordinates[this.coordinateIndex] = y1;
        triangleZCoordinates[this.coordinateIndex++] = z1;
        triangleXCoordinates[this.coordinateIndex] = x2;
        triangleYCoordinates[this.coordinateIndex] = y2;
        triangleZCoordinates[this.coordinateIndex++] = z2;
        triangleXCoordinates[this.coordinateIndex] = x3;
        triangleYCoordinates[this.coordinateIndex] = y3;
        triangleZCoordinates[this.coordinateIndex++] = z3;
      }
    });
    return new IntArrayScaleTriangulatedIrregularNetwork(this.geometryFactory, boundingBox,
      triangleCount, triangleXCoordinates, triangleYCoordinates, triangleZCoordinates);
  }

  public void setSortVertices(final boolean sortVertices) {
    this.sortVertices = sortVertices;
  }
}
