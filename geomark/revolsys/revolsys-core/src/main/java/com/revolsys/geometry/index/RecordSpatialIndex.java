package com.revolsys.geometry.index;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.index.quadtree.QuadTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.Record;
import com.revolsys.record.Records;
import com.revolsys.record.filter.RecordEqualsFilter;
import com.revolsys.visitor.CreateListVisitor;

public class RecordSpatialIndex<R extends Record> implements SpatialIndex<R> {

  public static <R2 extends Record> RecordSpatialIndex<R2> quadTree(
    final GeometryFactory geometryFactory) {
    final QuadTree<R2> spatialIndex = new QuadTree<>(geometryFactory);
    return new RecordSpatialIndex<>(spatialIndex);
  }

  private final SpatialIndex<R> spatialIndex;

  public RecordSpatialIndex(final SpatialIndex<R> spatialIndex) {
    this.spatialIndex = spatialIndex;
  }

  public RecordSpatialIndex<R> addRecord(final R record) {
    if (record != null) {
      final Geometry geometry = record.getGeometry();
      if (geometry != null && !geometry.isEmpty()) {
        final BoundingBox boundingBox = geometry.getBoundingBox();
        insertItem(boundingBox, record);
      }
    }
    return this;
  }

  public RecordSpatialIndex<R> addRecords(final Iterable<? extends R> records) {
    if (records != null) {
      for (final R record : records) {
        addRecord(record);
      }
    }
    return this;
  }

  @Override
  public void clear() {
    this.spatialIndex.clear();
  }

  @Override
  public boolean forEach(final Consumer<? super R> action) {
    return this.spatialIndex.forEach(action);
  }

  @Override
  public boolean forEach(final double x, final double y, final Consumer<? super R> action) {
    return this.spatialIndex.forEach(x, y, action);
  }

  @Override
  public boolean forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super R> action) {
    return this.spatialIndex.forEach(minX, minY, maxX, maxY, action);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.spatialIndex.getGeometryFactory();
  }

  @Override
  public List<R> getItems(final BoundingBoxProxy boundingBoxProxy) {
    final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
    final List<R> results = this.spatialIndex.getItems(boundingBoxProxy);
    for (final Iterator<R> iterator = results.iterator(); iterator.hasNext();) {
      final R record = iterator.next();
      final Geometry geometry = record.getGeometry();
      if (geometry == null) {
        iterator.remove();
      } else {
        final BoundingBox recordBoundingBox = geometry.getBoundingBox();
        if (!boundingBox.bboxIntersects(recordBoundingBox)) {
          iterator.remove();
        }
      }
    }
    return results;
  }

  public List<R> getRecordsDistance(final Geometry geometry, final double distance) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      final BoundingBox boundingBox = geometry.getBoundingBox() //
        .bboxEditor() //
        .expandDelta(distance);
      final Predicate<R> filter = Records.newFilter(geometry, distance);
      return queryList(boundingBox, filter);
    }
  }

  @Override
  public int getSize() {
    return this.spatialIndex.getSize();
  }

  @Override
  public void insertItem(final BoundingBox boundingBox, final R item) {
    this.spatialIndex.insertItem(boundingBox, item);
  }

  public void query(final Geometry geometry, final Consumer<R> visitor) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    forEach(boundingBox, visitor);
  }

  public List<R> queryEnvelope(final R record) {
    if (record == null) {
      return Collections.emptyList();
    } else {
      final Geometry geometry = record.getGeometry();
      return getItems(geometry);
    }
  }

  public R queryFirst(final R record, final Predicate<R> filter) {
    if (record == null) {
      return null;
    } else {
      final Geometry geometry = record.getGeometry();
      return getFirstBoundingBox(geometry, filter);
    }
  }

  public R queryFirstEquals(final R record, final Collection<String> excludedAttributes) {
    if (record == null) {
      return null;
    } else {
      final RecordEqualsFilter<R> filter = new RecordEqualsFilter<>(record, excludedAttributes);
      return queryFirst(record, filter);
    }
  }

  public List<R> queryIntersects(final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox convertedBoundingBox = boundingBox.bboxToCs(geometryFactory);
    if (convertedBoundingBox.isEmpty()) {
      return Arrays.asList();
    } else {
      final Predicate<R> filter = Records.newFilter(convertedBoundingBox);
      return queryList(convertedBoundingBox, filter);
    }
  }

  public List<R> queryIntersects(Geometry geometry) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory != null) {
      geometry = geometry.convertGeometry(geometryFactory);
    }
    final Predicate<R> filter = Records.newFilterGeometryIntersects(geometry);
    return queryList(geometry, filter);
  }

  public List<R> queryList(final BoundingBox boundingBox, final Predicate<R> filter) {
    return queryList(boundingBox, filter, null);
  }

  public List<R> queryList(final BoundingBox boundingBox, final Predicate<R> filter,
    final Comparator<R> comparator) {
    final CreateListVisitor<R> listVisitor = new CreateListVisitor<>(filter);
    forEach(boundingBox, listVisitor);

    final List<R> list = listVisitor.getList();
    if (comparator != null) {
      Collections.sort(list, comparator);
    }
    return list;
  }

  public List<R> queryList(final Geometry geometry, final Predicate<R> filter) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return queryList(boundingBox, filter);
  }

  public List<R> queryList(final Geometry geometry, final Predicate<R> filter,
    final Comparator<R> comparator) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return queryList(boundingBox, filter, comparator);
  }

  public List<R> queryList(final R record, final Predicate<R> filter) {
    final Geometry geometry = record.getGeometry();
    return queryList(geometry, filter);
  }

  @Override
  public boolean removeItem(final BoundingBox getItems, final R item) {
    return this.spatialIndex.removeItem(getItems, item);
  }

  public boolean removeRecord(final BoundingBox boundinBox, final R record) {
    if (record != null) {
      if (boundinBox != null) {
        return this.spatialIndex.removeItem(boundinBox, record);
      }
    }
    return false;
  }

  public boolean removeRecord(final R record) {
    if (record != null) {
      final BoundingBox boundinBox = record.getBoundingBox();
      return this.spatialIndex.removeItem(boundinBox, record);
    }
    return false;
  }

  public void removeRecords(final Iterable<? extends R> records) {
    if (records != null) {
      for (final R record : records) {
        removeRecord(record);
      }
    }
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.spatialIndex.setGeometryFactory(geometryFactory);
  }
}
