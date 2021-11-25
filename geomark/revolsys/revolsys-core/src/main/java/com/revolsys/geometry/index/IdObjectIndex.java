package com.revolsys.geometry.index;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;

public interface IdObjectIndex<T> extends Iterable<T> {
  public T add(final T object);

  void clear();

  void forEach(BoundingBoxProxy boundingBox, Consumer<? super T> action);

  void forEach(BoundingBoxProxy boundingBox, Predicate<? super T> filter,
    Consumer<? super T> action);

  BoundingBox getBoundingBox(T object);

  int getId(T object);

  T getObject(Integer id);

  List<T> getObjects(List<Integer> ids);

  List<T> query(BoundingBox envelope);

  boolean remove(T object);
}
