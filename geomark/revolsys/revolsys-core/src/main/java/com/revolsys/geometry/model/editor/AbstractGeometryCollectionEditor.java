package com.revolsys.geometry.model.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.common.function.Consumer4Double;
import org.jeometry.common.function.Function4Double;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryDataType;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public abstract class AbstractGeometryCollectionEditor<GC extends Geometry, G extends Geometry, GE extends GeometryEditor<?>>
  extends AbstractGeometryEditor<GE> implements GeometryCollection {
  private static final long serialVersionUID = 1L;

  private GC geometry;

  protected final List<GE> editors = new ArrayList<>();

  public AbstractGeometryCollectionEditor(final AbstractGeometryEditor<?> parentEditor,
    final GC geometry) {
    super(parentEditor, geometry);
    this.geometry = geometry;
    for (final Geometry part : geometry.geometries()) {
      @SuppressWarnings("unchecked")
      final GE editor = (GE)part.newGeometryEditor(parentEditor);
      this.editors.add(editor);
    }
  }

  public AbstractGeometryCollectionEditor(final AbstractGeometryEditor<?> parentEditor,
    final GeometryFactory geometryFactory, final GE[] editors) {
    super(parentEditor, geometryFactory);
    if (editors != null) {
      for (final GE editor : editors) {
        this.editors.add(editor);
      }
    }
  }

  public AbstractGeometryCollectionEditor(final GC geometry) {
    this(null, geometry);
  }

  public AbstractGeometryCollectionEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
    setModified(true);
  }

  public AbstractGeometryCollectionEditor(final GeometryFactory geometryFactory,
    final List<GE> editors) {
    super(geometryFactory);
    this.editors.addAll(editors);
  }

  protected void addEditor(final GE editor) {
    this.editors.add(editor);
  }

  @SuppressWarnings("unchecked")
  public GE appendEditor() {
    final GeometryDataType<?, ?> partDataType = getPartDataType();
    if (partDataType == null) {
      throw new IllegalArgumentException("Part data type not specified");
    } else {
      final GE editor = (GE)partDataType.newGeometryEditor(getGeometryFactory());
      addEditor(editor);
      return editor;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public GeometryEditor<?> appendVertex(final int[] geometryId,
    final GeometryDataType<?, ?> partDataType, final Point point) {
    if (geometryId == null || geometryId.length < 1) {
    } else {
      final int partIndex = geometryId[0];
      GE editor = getEditor(partIndex);
      if (editor == null && partIndex == this.editors.size()) {
        final GeometryDataType<?, ?> thisPartDataType = getPartDataType();
        if (thisPartDataType == null) {
          throw new IllegalArgumentException("Part data type not specified");
        } else if (thisPartDataType.equals(partDataType)) {
          editor = (GE)partDataType.newGeometryEditor(getGeometryFactory());
          addEditor(editor);
        }
      }
      if (editor != null) {
        final int[] childGeometryId = Arrays.copyOfRange(geometryId, 1, geometryId.length);
        final GeometryEditor<?> newEditor = editor.appendVertex(childGeometryId, point);
        if (newEditor != editor) {
          final List<GeometryEditor<?>> editors = new ArrayList<>(this.editors);
          editors.set(partIndex, newEditor);
          final GeometryFactory geometryFactory = getGeometryFactory();
          return new GeometryCollectionImplEditor(geometryFactory, editors);
        }
      }
    }
    return this;
  }

  @Override
  public GeometryEditor<?> appendVertex(final int[] geometryId, final Point point) {
    return appendVertex(geometryId, null, point);
  }

  @Override
  public GeometryCollection clone() {
    return (GeometryCollection)super.clone();
  }

  @Override
  public AbstractGeometryCollectionEditor<?, ?, ?> deleteVertex(final int[] vertexId) {
    if (vertexId == null || vertexId.length < 2) {
    } else {
      final int partIndex = vertexId[0];
      final GE editor = getEditor(partIndex);
      if (editor != null) {
        final int[] childVertexId = Arrays.copyOfRange(vertexId, 1, vertexId.length);
        editor.deleteVertex(childVertexId);
      }
    }
    return this;
  }

  @Override
  public Iterable<GE> editors() {
    return this.editors;
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Geometry) {
      final Geometry geometry = (Geometry)other;
      return equals(2, geometry);
    } else {
      return false;
    }
  }

  @Override
  public boolean equalsVertex(final int axisCount, final int[] geometryId, final int vertexIndex,
    final Point point) {
    final GeometryEditor<?> geometryEditor = getGeometryEditor(geometryId, 0);
    if (geometryEditor == null) {
      return false;
    } else {
      return geometryEditor.equalsVertex(axisCount, vertexIndex, point);
    }
  }

  @Override
  public <R> R findSegment(final Function4Double<R> action) {
    for (final Geometry geometry : this.editors) {
      final R result = geometry.findSegment(action);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public void forEachGeometry(final Consumer<Geometry> action) {
    for (final Geometry geometry : this.editors) {
      action.accept(geometry);
    }
  }

  @Override
  public void forEachSegment(final Consumer4Double action) {
    for (final Geometry geometry : this.editors) {
      geometry.forEachSegment(action);
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
  public void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final CoordinatesOperationPoint point, final Consumer<CoordinatesOperationPoint> action) {
    for (final Geometry geometry : this.editors) {
      geometry.forEachVertex(coordinatesOperation, point, action);
    }
  }

  @Override
  public void forEachVertex(final CoordinatesOperationPoint coordinates,
    final Consumer<CoordinatesOperationPoint> action) {
    for (final Geometry geometry : this.editors) {
      geometry.forEachVertex(coordinates, action);
    }
  }

  @Override
  public Geometry getCurrentGeometry() {
    final List<Geometry> geometries = new ArrayList<>();
    for (final GE editor : this.editors) {
      final Geometry currentGeometry = editor.getCurrentGeometry();
      geometries.add(currentGeometry);
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.geometry(geometries);
  }

  public GE getEditor(final int partIndex) {
    final int editorCount = this.editors.size();
    if (partIndex < 0) {
      throw new ArrayIndexOutOfBoundsException(partIndex);
    } else if (partIndex < editorCount) {
      return this.editors.get(partIndex);
    } else if (partIndex == editorCount) {
      return appendEditor();
    } else {
      throw new ArrayIndexOutOfBoundsException(partIndex);
    }
  }

  @Override
  public int[] getFirstGeometryId() {
    return new int[] {
      0
    };
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> List<V> getGeometries() {
    return (List<V>)Lists.toArray(this.editors);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry(final int partIndex) {
    return (V)getEditor(partIndex);
  }

  @Override
  public int getGeometryCount() {
    return this.editors.size();
  }

  @Override
  public GeometryEditor<?> getGeometryEditor(final int[] geometryId, final int idOffset,
    final int idLength) {
    if (geometryId != null && idOffset < idLength) {
      final int partIndex = geometryId[idOffset];
      final GeometryEditor<?> geometryEditor = getEditor(partIndex);
      if (geometryEditor != null) {
        final int nextIdOffset = idOffset + 1;
        if (nextIdOffset < idLength) {
          return geometryEditor.getGeometryEditor(geometryId, nextIdOffset);
        } else {
          return geometryEditor;
        }
      }
    }
    return null;
  }

  public GE getLastEditor() {
    if (this.editors.isEmpty()) {
      return appendEditor();
    } else {
      return this.editors.get(this.editors.size() - 1);
    }
  }

  @Override
  public int getVertexCount(final int[] geometryId, final int idLength) {
    if (geometryId == null || idLength < 1) {
      return 0;
    } else {
      final int partIndex = geometryId[0];
      final GE editor = getEditor(partIndex);
      if (editor != null) {
        final int[] childGeometryId = Arrays.copyOfRange(geometryId, 1, idLength);
        return editor.getVertexCount(childGeometryId);
      }
    }
    return 0;
  }

  @Override
  public int hashCode() {
    return getBoundingBox().hashCode();
  }

  @Override
  public GeometryEditor<?> insertVertex(final int[] vertexId, final Point point) {
    if (vertexId == null || vertexId.length < 1) {
    } else {
      final int partIndex = vertexId[0];
      final GE editor = getEditor(partIndex);
      if (editor != null) {
        final int[] childVertexId = Arrays.copyOfRange(vertexId, 1, vertexId.length);
        final GeometryEditor<?> newEditor = editor.insertVertex(childVertexId, point);
        if (newEditor != editor) {
          final List<GeometryEditor<?>> editors = new ArrayList<>(this.editors);
          editors.set(partIndex, newEditor);
          final GeometryFactory geometryFactory = getGeometryFactory();
          return new GeometryCollectionImplEditor(geometryFactory, editors);
        }
      }
    }
    return this;
  }

  @Override
  public boolean isEmpty() {
    return this.editors.isEmpty();
  }

  @Override
  public boolean isModified() {
    for (final GE editor : this.editors) {
      if (editor.isModified()) {
        return true;
      }
    }
    return super.isModified();
  }

  @Override
  @SuppressWarnings("unchecked")
  public GC newGeometry() {
    if (isModified() || this.geometry == null) {
      final List<G> geometries = new ArrayList<>();
      for (final GE editor : this.editors) {
        final G newGeometry = (G)editor.newGeometry();
        if (!newGeometry.isEmpty()) {
          geometries.add(newGeometry);
        }
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.geometry(geometries);
    } else {
      return this.geometry;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <GA extends Geometry> GA newGeometryAny() {
    if (isModified() || this.geometry == null) {
      final List<G> geometries = new ArrayList<>();
      for (final GE editor : this.editors) {
        final G newGeometry = (G)editor.newGeometryAny();
        if (!newGeometry.isEmpty()) {
          geometries.add(newGeometry);
        }
      }
      if (geometries.isEmpty()) {
        return newGeometryEmpty();
      } else {
        final GeometryFactory geometryFactory = getGeometryFactory();
        return geometryFactory.geometry(geometries);
      }
    } else {
      return (GA)this.geometry;
    }
  }

  public abstract <NGE extends Geometry> NGE newGeometryEmpty();

  @Override
  public void removeGeometry(final int index) {
    this.editors.remove(index);
    setModified(true);
  }

  @Override
  public void revertChanges() {
    for (final GeometryEditor<?> editor : this.editors) {
      editor.revertChanges();
    }
    setModified(false);
  }

  @Override
  public GeometryEditor<?> setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      super.setAxisCount(axisCount);
      for (final GE editor : this.editors) {
        editor.setAxisCount(axisCount);
      }
    }
    return this;
  }

  @Override
  public AbstractGeometryCollectionEditor<?, ?, ?> setCoordinate(final int[] vertexId,
    final int axisIndex, final double coordinate) {
    if (vertexId == null || vertexId.length < 2) {
    } else {
      final int partIndex = vertexId[0];
      final GE editor = getEditor(partIndex);
      if (editor != null) {
        final int[] childVertexId = Arrays.copyOfRange(vertexId, 1, vertexId.length);
        editor.setCoordinate(childVertexId, axisIndex, coordinate);
      }
    }
    return this;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
