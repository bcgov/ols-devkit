package com.revolsys.geometry.index.quadtree;

import java.io.Serializable;
import java.util.function.Consumer;

import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.util.ExitLoopException;

public class QuadTree<T> implements SpatialIndex<T>, Serializable {
  private static final long serialVersionUID = 1L;

  public static double[] ensureExtent(final double[] bounds, final double minExtent) {
    double minX = bounds[0];
    double maxX = bounds[2];
    double minY = bounds[1];
    double maxY = bounds[3];
    if (minX != maxX && minY != maxY) {
      return bounds;
    } else {
      if (minX == maxX) {
        minX = minX - minExtent / 2.0;
        maxX = minX + minExtent / 2.0;
      }
      if (minY == maxY) {
        minY = minY - minExtent / 2.0;
        maxY = minY + minExtent / 2.0;
      }
      return new double[] {
        minX, minY, maxX, maxY
      };
    }
  }

  private double absoluteMinExtent;

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private double minExtent = 1;

  private final AbstractQuadTreeNode<T> root;

  private int size = 0;

  private boolean useEquals = false;

  public QuadTree(final GeometryFactory geometryFactory) {
    this(geometryFactory, new QuadTreeNode<>());
  }

  protected QuadTree(final GeometryFactory geometryFactory, final AbstractQuadTreeNode<T> root) {
    setGeometryFactory(geometryFactory);
    this.root = root;
  }

  @Override
  public void clear() {
    this.root.clear();
    this.minExtent = 1.0;
    if (this.minExtent < this.absoluteMinExtent) {
      this.minExtent = this.absoluteMinExtent;
    }
    this.size = 0;
  }

  public int depth() {
    return this.root.depth();
  }

  protected boolean equalsItem(final T item1, final T item2) {
    if (item1 == item2) {
      return true;
    } else if (this.useEquals) {
      return item1.equals(item2);
    } else {
      return false;
    }
  }

  @Override
  public boolean forEach(final Consumer<? super T> action) {
    try {
      this.root.forEach(this, action);
      return true;
    } catch (final ExitLoopException e) {
      return false;
    }
  }

  // TODO forEach and remove in one call

  @Override
  public boolean forEach(final double x, final double y, final Consumer<? super T> action) {
    try {
      this.root.forEach(this, x, y, action);
      return true;
    } catch (final ExitLoopException e) {
      return false;
    }
  }

  @Override
  public boolean forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super T> action) {
    try {
      this.root.forEach(this, minX, minY, maxX, maxY, action);
      return true;
    } catch (final ExitLoopException e) {
      return false;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public int getSize() {
    return this.size;
  }

  @Override
  public void insertItem(final BoundingBox boundingBox, final T item) {
    final BoundingBox convertedBoundingBox = convertBoundingBox(boundingBox);
    if (convertedBoundingBox == null || convertedBoundingBox.isEmpty()) {
      throw new IllegalArgumentException("Item bounding box " + boundingBox
        + " must not be null or empty in coordinate system: " + getHorizontalCoordinateSystemId());
    } else {
      final double minX = convertedBoundingBox.getMinX();
      final double minY = convertedBoundingBox.getMinY();
      final double maxX = convertedBoundingBox.getMaxX();
      final double maxY = convertedBoundingBox.getMaxY();

      insertItem(minX, minY, maxX, maxY, item);
    }
  }

  public final void insertItem(double minX, double minY, double maxX, double maxY, final T item) {
    final double deltaX = maxX - minX;
    if (deltaX == 0) {
      minX = minX - this.minExtent / 2.0;
      maxX = minX + this.minExtent / 2.0;
    } else if (deltaX < this.minExtent) {
      this.minExtent = deltaX;
    }

    final double deltaY = maxY - minY;
    if (deltaY == 0) {
      minY = minY - this.minExtent / 2.0;
      maxY = minY + this.minExtent / 2.0;
    } else if (deltaY < this.minExtent) {
      this.minExtent = deltaY;
    }

    if (this.root.insertRoot(this, minX, minY, maxX, maxY, item)) {
      this.size++;
    }
  }

  public final void insertItem(final double x, final double y, final T item) {
    insertItem(x, y, x, y, item);
  }

  @Override
  public boolean removeItem(BoundingBox boundingBox, final T item) {
    boundingBox = convertBoundingBox(boundingBox);
    if (boundingBox != null && !boundingBox.isEmpty()) {
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();

      return removeItem(minX, minY, maxX, maxY, item);
    } else {
      return false;
    }
  }

  public boolean removeItem(final double minX, final double minY, final double maxX,
    final double maxY, final T item) {
    final boolean removed = this.root.removeItem(this, minX, minY, maxX, maxY, item);
    if (removed) {
      this.size--;
    }
    return removed;
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      this.geometryFactory = GeometryFactory.DEFAULT_2D;
    } else {
      this.geometryFactory = geometryFactory;
    }
    if (this.geometryFactory.isFloating()) {
      this.absoluteMinExtent = 0.00000001;
    } else {
      this.absoluteMinExtent = this.geometryFactory.getResolutionX();
      if (this.absoluteMinExtent < 0) {
        this.absoluteMinExtent = 0.00000001;
      }
    }
    if (this.minExtent < this.absoluteMinExtent) {
      this.minExtent = this.absoluteMinExtent;
    }
  }

  public void setUseEquals(final boolean useEquals) {
    this.useEquals = useEquals;
  }

  public int size() {
    return getSize();
  }
}
