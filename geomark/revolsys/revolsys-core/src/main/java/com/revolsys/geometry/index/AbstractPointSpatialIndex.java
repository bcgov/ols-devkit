package com.revolsys.geometry.index;

import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;

public abstract class AbstractPointSpatialIndex<T> implements PointSpatialIndex<T> {

  @Override
  public List<T> find(final BoundingBox boundingBox) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(boundingBox, visitor);
    return visitor.getList();
  }

  @Override
  public List<T> findAll() {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(visitor);
    return visitor.getList();
  }

  @Override
  public Iterator<T> iterator() {
    return findAll().iterator();
  }

}
