package com.revolsys.elevation.tin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.index.rtree.RTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.spring.resource.Resource;

public class TriangulatedIrregularNetworkImpl implements TriangulatedIrregularNetwork {

  private final BoundingBox boundingBox;

  private final GeometryFactory geometryFactory;

  private final RTree<Triangle> triangleIndex = new RTree<>();

  private Resource resource;

  private final Set<Point> nodes = new LinkedHashSet<>();

  private List<Triangle> triangles = new ArrayList<>();

  public TriangulatedIrregularNetworkImpl(final BoundingBox boundingBox,
    final Iterable<? extends Triangle> triangles) {
    this.boundingBox = boundingBox;
    this.geometryFactory = boundingBox.getGeometryFactory();
    this.triangles = Lists.toArray(triangles);
    for (final Triangle triangle : this.triangles) {
      for (int i = 0; i < 3; i++) {
        this.nodes.add(triangle.getPoint(i));
      }
      this.triangleIndex.insertItem(triangle.getBoundingBox(), triangle);
    }
  }

  @Override
  public void forEachTriangle(final BoundingBox boundingBox,
    final Consumer<? super Triangle> action) {
    final SpatialIndex<Triangle> index = getTriangleIndex();
    if (index != null) {
      index.forEach(boundingBox, action);
    }
  }

  @Override
  public void forEachTriangle(final BoundingBox boundingBox,
    final Predicate<? super Triangle> filter, final Consumer<? super Triangle> action) {
    final SpatialIndex<Triangle> index = getTriangleIndex();
    if (index != null) {
      index.forEach(boundingBox, filter, action);
    }
  }

  @Override
  public void forEachTriangle(final Consumer<? super Triangle> action) {
    for (final Triangle triangle : this.triangles) {
      action.accept(triangle);
    }
  }

  @Override
  public void forEachTriangle(final Predicate<? super Triangle> filter,
    final Consumer<? super Triangle> action) {
    if (filter == null) {
      forEachTriangle(action);
    } else {
      for (final Triangle triangle : this.triangles) {
        if (filter.test(triangle)) {
          action.accept(triangle);
        }
      }
    }
  }

  @Override
  public void forEachVertex(final Consumer<Point> action) {
    this.nodes.forEach(action);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public Resource getResource() {
    return this.resource;
  }

  @Override
  public int getTriangleCount() {
    return this.triangles.size();
  }

  public SpatialIndex<Triangle> getTriangleIndex() {
    return this.triangleIndex;
  }

  @Override
  public int getVertexCount() {
    return this.nodes.size();
  }
}
