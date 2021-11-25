package com.revolsys.geometry.index;

import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Point;

public interface PointSpatialIndex<T> extends Iterable<T> {
  void clear();

  List<T> find(BoundingBox boundingBox);

  List<T> findAll();

  void forEach(final BoundingBoxProxy boundinBoxProxy, final Consumer<? super T> action);

  @Override
  void forEach(final Consumer<? super T> action);

  void put(Point point, T object);

  boolean remove(Point point, T object);
}
