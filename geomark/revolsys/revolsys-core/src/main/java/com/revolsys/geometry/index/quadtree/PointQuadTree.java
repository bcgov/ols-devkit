package com.revolsys.geometry.index.quadtree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.revolsys.geometry.index.AbstractPointSpatialIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.util.ExitLoopException;
import com.revolsys.util.Property;

public class PointQuadTree<T> extends AbstractPointSpatialIndex<T> {
  public static PointQuadTree<int[]> get(final Geometry geometry) {
    if (Property.hasValue(geometry)) {
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();
      final PointQuadTree<int[]> index = new PointQuadTree<>(geometryFactory);
      for (final Vertex vertex : geometry.vertices()) {
        final double x = vertex.getX();
        final double y = vertex.getY();
        final int[] vertexId = vertex.getVertexId();
        index.put(x, y, vertexId);
      }
      return index;
    } else {
      return null;
    }
  }

  private GeometryFactory geometryFactory;

  private PointQuadTreeNode<T> root;

  public PointQuadTree() {
  }

  public PointQuadTree(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public void clear() {
    this.root = null;
  }

  public boolean contains(final Point point) {
    if (this.root == null) {
      return false;
    } else {
      return this.root.contains(point);
    }
  }

  public List<Entry<Point, T>> findEntriesWithinDistance(final Point from, final Point to,
    final double maxDistance) {
    final BoundingBox boundingBox = this.geometryFactory.newBoundingBox(from.getX(), from.getY(),
      to.getX(), to.getY());
    final List<Entry<Point, T>> entries = new ArrayList<>();
    this.root.findEntriesWithin(entries, boundingBox);
    for (final Iterator<Entry<Point, T>> iterator = entries.iterator(); iterator.hasNext();) {
      final Entry<Point, T> entry = iterator.next();
      final Point coordinates = entry.getKey();
      final double distance = LineSegmentUtil.distanceLinePoint(from, to, coordinates);
      if (distance >= maxDistance) {
        iterator.remove();
      }
    }
    return entries;
  }

  public List<T> findWithin(BoundingBox boundingBox) {
    if (this.geometryFactory != null) {
      boundingBox = boundingBox.bboxToCs(this.geometryFactory);
    }
    final List<T> results = new ArrayList<>();
    if (this.root != null) {
      this.root.findWithin(results, boundingBox);
    }
    return results;
  }

  public List<T> findWithinDistance(final Point point, final double maxDistance) {
    final double x = point.getX();
    final double y = point.getY();
    final BoundingBox boundingBox = BoundingBox.bboxNewDelta(x, y, maxDistance);
    final List<T> results = new ArrayList<>();
    if (this.root != null) {
      this.root.findWithin(results, x, y, maxDistance, boundingBox);
    }
    return results;
  }

  public List<T> findWithinDistance(final Point from, final Point to, final double maxDistance) {
    final List<Entry<Point, T>> entries = findEntriesWithinDistance(from, to, maxDistance);
    final List<T> results = new ArrayList<>();
    for (final Entry<Point, T> entry : entries) {
      final T value = entry.getValue();
      results.add(value);
    }
    return results;
  }

  @Override
  public void forEach(final BoundingBoxProxy boundingBoxProxy, final Consumer<? super T> action) {
    if (this.root != null) {
      final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
      this.root.forEach(action, boundingBox);
    }
  }

  @Override
  public void forEach(final Consumer<? super T> action) {
    if (this.root != null) {
      try {
        this.root.forEach(action);
      } catch (final ExitLoopException e) {
      }
    }
  }

  public void put(final double x, final double y, final T value) {
    final PointQuadTreeNode<T> node = new PointQuadTreeNode<>(value, x, y);
    if (this.root == null) {
      this.root = node;
    } else {
      this.root.put(x, y, node);
    }
  }

  @Override
  public void put(final Point point, final T value) {
    if (!point.isEmpty()) {
      final double x = point.getX();
      final double y = point.getY();
      put(x, y, value);
    }
  }

  public boolean remove(final double x, final double y, final T value) {
    if (this.root == null) {
      return false;
    } else {
      this.root = this.root.remove(x, y, value);
      // TODO change so it returns if the item was removed
      return true;
    }
  }

  @Override
  public boolean remove(final Point point, final T value) {
    final double x = point.getX();
    final double y = point.getY();
    return remove(x, y, value);
  }
}
