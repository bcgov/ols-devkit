package com.revolsys.geometry.index.kdtree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jeometry.common.function.BiFunctionDouble;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.util.Emptyable;

public class KdTree implements Emptyable {

  private long size;

  private KdNode root = null;

  private final GeometryFactory geometryFactory;

  private final BiFunctionDouble<KdNode> nodeFactory;

  private final double scaleXY;

  public KdTree() {
    this(GeometryFactory.DEFAULT_2D);
  }

  public KdTree(final BiFunctionDouble<KdNode> nodeFactory) {
    this(nodeFactory, GeometryFactory.DEFAULT_2D);
  }

  public KdTree(final BiFunctionDouble<KdNode> nodeFactory, final GeometryFactory geometryFactory) {
    this.nodeFactory = nodeFactory;
    this.geometryFactory = geometryFactory;
    this.scaleXY = geometryFactory.getScaleXY();
  }

  public KdTree(final GeometryFactory geometryFactory) {
    this(KdNode::new, geometryFactory);
  }

  public <N extends KdNode> void forEachNode(final BoundingBox boundingBox,
    final Consumer<N> result) {
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    forEachNode(minX, minY, maxX, maxY, result);
  }

  public <N extends KdNode> void forEachNode(final BoundingBox boundingBox,
    final Predicate<? super N> filter, final Consumer<N> result) {
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    forEachNode(minX, minY, maxX, maxY, filter, result);
  }

  public <N extends KdNode> void forEachNode(final double minX, final double minY,
    final double maxX, final double maxY, final Consumer<N> result) {
    if (this.root != null) {
      this.root.forEachNode(true, minX, minY, maxX, maxY, result);
    }
  }

  public <N extends KdNode> void forEachNode(final double minX, final double minY,
    final double maxX, final double maxY, final Predicate<? super N> filter,
    final Consumer<N> result) {
    if (this.root != null) {
      this.root.forEachNode(true, minX, minY, maxX, maxY, filter, result);
    }
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public <N extends KdNode> List<N> getItems(final BoundingBox boundingBox) {
    final List<N> result = new ArrayList<>();
    final Consumer<N> action = result::add;
    forEachNode(boundingBox, action);
    return result;
  }

  public long getSize() {
    return this.size;
  }

  @SuppressWarnings("unchecked")
  public <N extends KdNode> N insertPoint(double x, double y) {
    if (this.scaleXY > 0) {
      x = Math.round(x * this.scaleXY) / this.scaleXY;
      y = Math.round(y * this.scaleXY) / this.scaleXY;
    }
    if (this.root == null) {
      this.root = this.nodeFactory.accept(x, y);
      return (N)this.root;
    } else {

      KdNode currentNode = this.root;
      KdNode leafNode = this.root;
      boolean isAxisX = true;
      boolean isLessThan = true;

      while (currentNode != null) {
        final double x2 = currentNode.getX();
        final double y2 = currentNode.getY();
        if (x2 == x && y2 == y) {
          currentNode.increment();
          return (N)currentNode;
        } else {
          if (isAxisX) {
            isLessThan = x < x2;
          } else {
            isLessThan = y < y2;
          }
          leafNode = currentNode;
          if (isLessThan) {
            currentNode = currentNode.getLeft();
          } else {
            currentNode = currentNode.getRight();
          }
          isAxisX = !isAxisX;
        }
      }

      this.size = this.size + 1;
      final KdNode node = this.nodeFactory.accept(x, y);
      if (isLessThan) {
        leafNode.setLeft(node);
      } else {
        leafNode.setRight(node);
      }
      return (N)node;
    }
  }

  public <N extends KdNode> N insertPoint(final Point point) {
    final Point convertedPoint = point.convertPoint2d(this.geometryFactory);
    final double x = convertedPoint.getX();
    final double y = convertedPoint.getY();
    return insertPoint(x, y);
  }

  @Override
  public boolean isEmpty() {
    return this.size == 0;
  }
}
