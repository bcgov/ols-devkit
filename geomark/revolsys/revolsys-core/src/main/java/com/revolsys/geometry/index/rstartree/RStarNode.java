package com.revolsys.geometry.index.rstartree;

import java.util.function.Consumer;

import com.revolsys.geometry.model.BoundingBoxProxy;

public interface RStarNode<T> extends BoundingBoxProxy {

  void forEach(final Consumer<? super T> action);

  void forEach(final double x, final double y, final Consumer<? super T> action);

  void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super T> action);

  double getArea();
}
