package com.revolsys.elevation.tin;

import java.util.function.Consumer;

import com.revolsys.geometry.index.quadtree.IdObjectQuadTree;
import com.revolsys.geometry.index.quadtree.QuadTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.spring.resource.Resource;

public class CompactTriangulatedIrregularNetwork extends BaseCompactTriangulatedIrregularNetwork {

  private final QuadTree<Integer> triangleSpatialIndex;

  private final BoundingBox boundingBox;

  public CompactTriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final int vertexCount, final double[] vertexXCoordinates, final double[] vertexYCoordinates,
    final double[] vertexZCoordinates, final int triangleCount, final int[] triangle0VertexIndices,
    final int[] triangle1VertexIndices, final int[] triangle2VertexIndices) {
    super(geometryFactory, vertexCount, vertexXCoordinates, vertexYCoordinates, vertexZCoordinates,
      triangleCount, triangle0VertexIndices, triangle1VertexIndices, triangle2VertexIndices);
    this.triangleSpatialIndex = new IdObjectQuadTree<>(geometryFactory) {
      private static final long serialVersionUID = 1L;

      @Override
      protected boolean intersectsBounds(final Object id, final double x, final double y) {
        final Integer triangleIndex = (Integer)id;
        return newTriangleBoundingBox(triangleIndex).bboxIntersects(x, y);
      }

      @Override
      protected boolean intersectsBounds(final Object id, final double minX, final double minY,
        final double maxX, final double maxY) {
        final Integer triangleIndex = (Integer)id;
        return newTriangleBoundingBox(triangleIndex).bboxIntersects(minX, minY, maxX, maxY);
      }
    };
    this.triangleSpatialIndex.setUseEquals(true);

    final double[] bounds = RectangleUtil.newBounds(2);
    for (int triangleIndex = 0; triangleIndex < triangleCount; triangleIndex++) {
      final BoundingBox triangleBoundingBox = newTriangleBoundingBox(triangleIndex);
      this.triangleSpatialIndex.insertItem(triangleBoundingBox, triangleIndex);
      RectangleUtil.expand(bounds, 2, triangleBoundingBox);
    }
    this.boundingBox = geometryFactory.newBoundingBox(2, bounds);
  }

  @Override
  public void forEachTriangle(final BoundingBox boundingBox,
    final Consumer<? super Triangle> action) {
    this.triangleSpatialIndex.forEach(boundingBox, (triangleIndex) -> {
      final Triangle triangle = newTriangle(triangleIndex);
      if (triangle != null) {
        action.accept(triangle);
      }
    });
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public Resource getResource() {
    // TODO Auto-generated method stub
    return null;
  }
}
