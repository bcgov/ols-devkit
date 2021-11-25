package com.revolsys.geometry.model.editor;

import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataType;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.GeometryCollectionImpl;

public class GeometryCollectionImplEditor
  extends AbstractGeometryCollectionEditor<GeometryCollectionImpl, Geometry, GeometryEditor<?>> {
  private static final long serialVersionUID = 1L;

  public GeometryCollectionImplEditor(
    final AbstractGeometryCollectionEditor<?, ?, ?> geometryEditor,
    final GeometryCollectionImpl geometryCollection) {
    super(geometryEditor, geometryCollection);
  }

  public GeometryCollectionImplEditor(final GeometryCollectionImpl geometryCollection) {
    this(null, geometryCollection);
  }

  public GeometryCollectionImplEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public GeometryCollectionImplEditor(final GeometryFactory geometryFactory,
    final List<GeometryEditor<?>> editors) {
    super(geometryFactory, editors);
  }

  @Override
  public GeometryCollectionImplEditor clone() {
    return (GeometryCollectionImplEditor)super.clone();
  }

  @Override
  public <R> R findVertex(final BiFunctionDouble<R> action) {
    for (final GeometryEditor<?> editor : this.editors) {
      final R result = editor.findVertex(action);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public void forEachGeometry(final Consumer<Geometry> action) {
    for (final GeometryEditor<?> editor : this.editors) {
      action.accept(editor);
    }
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    for (final GeometryEditor<?> editor : this.editors) {
      editor.forEachVertex(action);
    }
  }

  @Override
  public void forEachVertex(final Consumer3Double action) {
    for (final GeometryEditor<?> editor : this.editors) {
      editor.forEachVertex(action);
    }
  }

  @Override
  public boolean hasGeometryType(final GeometryDataType<?, ?> dataType) {
    for (final Geometry geometry : geometries()) {
      if (geometry.hasGeometryType(dataType)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <GE extends Geometry> GE newGeometryEmpty() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (GE)geometryFactory.geometry();
  }

  @Override
  public Geometry prepare() {
    return this;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
