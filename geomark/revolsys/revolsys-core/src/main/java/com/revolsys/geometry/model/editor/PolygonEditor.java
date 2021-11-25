package com.revolsys.geometry.model.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.common.function.Consumer4Double;
import org.jeometry.common.function.Function4Double;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;

public class PolygonEditor extends AbstractGeometryEditor<PolygonEditor>
  implements Polygon, PolygonalEditor {
  private static final long serialVersionUID = 1L;

  private Polygon polygon;

  private final List<LinearRingEditor> editors = new ArrayList<>();

  public PolygonEditor(final AbstractGeometryCollectionEditor<?, ?, ?> parentEditor,
    final LinearRingEditor linearRingEditor) {
    super(parentEditor);
    this.editors.add(linearRingEditor);
  }

  public PolygonEditor(final AbstractGeometryCollectionEditor<?, ?, ?> parentEditor,
    final Polygon polygon) {
    super(parentEditor, polygon);
    this.polygon = polygon;
    revertChanges();
  }

  public PolygonEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public PolygonEditor(final Polygon polygon) {
    this(null, polygon);
  }

  public LinearRingEditor addRing() {
    final LinearRingEditor editor = new LinearRingEditor(this);
    this.editors.add(editor);
    return editor;
  }

  public LinearRingEditor addRing(final int index) {
    final LinearRingEditor editor = new LinearRingEditor(this);
    this.editors.add(index, editor);
    return editor;
  }

  public LinearRingEditor addRing(final int index, final LinearRing ring) {
    final LinearRingEditor editor = new LinearRingEditor(this, ring);
    this.editors.add(index, editor);
    return editor;
  }

  public LinearRingEditor addRing(final LinearRing ring) {
    final LinearRingEditor editor = new LinearRingEditor(this, ring);
    this.editors.add(editor);
    return editor;
  }

  public PolygonalEditor appendVertex(final int ringIndex, final Point point) {
    LinearRingEditor editor = getEditor(ringIndex);
    if (editor == null) {
      if (ringIndex == 0) {
        editor = new LinearRingEditor(this);
        this.editors.add(editor);
      } else {
        return this;
      }
    }
    final int vertexCount = editor.getVertexCount();
    if (vertexCount < 2) {
      editor.appendVertex(point);
    } else if (vertexCount == 2) {
      editor.appendVertex(point);
      final Point firstPoint = editor.getPoint(0);
      editor.appendVertex(firstPoint);
    } else {
      editor.insertVertex(vertexCount - 1, point);
    }
    return this;
  }

  @Override
  public PolygonalEditor appendVertex(final int[] geometryId, final Point point) {
    if (geometryId == null || geometryId.length != 1) {
    } else {
      final int ringIndex = geometryId[0];
      appendVertex(ringIndex, point);
    }
    return this;
  }

  @Override
  public Polygon clone() {
    return (Polygon)super.clone();
  }

  @Override
  public PolygonalEditor deleteVertex(final int[] vertexId) {
    if (vertexId == null || vertexId.length < 2) {
    } else {
      final int partIndex = vertexId[0];
      final LinearRingEditor editor = getEditor(partIndex);
      if (editor != null) {
        final int[] childVertexId = Arrays.copyOfRange(vertexId, 1, vertexId.length);
        editor.deleteVertex(childVertexId);
      }
    }
    return this;
  }

  @Override
  public Iterable<PolygonEditor> editors() {
    return Collections.singleton(this);
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
    for (final LinearRingEditor editor : this.editors) {
      action.accept(editor);
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
    for (final LinearRingEditor editor : this.editors) {
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
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return geometryFactory.polygon();
    }
    final List<LinearRing> rings = new ArrayList<>();
    final List<Geometry> geometries = new ArrayList<>();
    boolean shell = true;
    for (final LinearRingEditor editor : this.editors) {
      final Geometry ringGeometry = editor.getCurrentGeometry();
      if (ringGeometry instanceof LinearRing) {
        final LinearRing ring = (LinearRing)ringGeometry;
        if (shell || !rings.isEmpty()) {
          rings.add(ring);
        } else {
          geometries.add(ringGeometry);
        }
      } else {
        geometries.add(ringGeometry);
      }
      shell = false;
    }
    if (!rings.isEmpty()) {
      final Polygon polygon = geometryFactory.polygon(rings);
      if (geometries.isEmpty()) {
        return polygon;
      } else {
        geometries.add(polygon);
      }
    }
    return geometryFactory.geometry(geometries);
  }

  public LinearRingEditor getEditor(final int ringIndex) {
    if (ringIndex < 0 || ringIndex >= this.editors.size()) {
      return null;
    } else {
      return this.editors.get(ringIndex);
    }
  }

  @Override
  public int[] getFirstGeometryId() {
    return new int[] {
      0
    };
  }

  @Override
  public GeometryEditor<?> getGeometryEditor(final int[] geometryId, final int idOffset,
    final int idLength) {
    if (geometryId != null && idOffset == idLength - 1) {
      final int partIndex = geometryId[idOffset];
      final LineStringEditor geometryEditor = getEditor(partIndex);
      if (geometryEditor != null) {
        return geometryEditor;
      }
    }
    return null;
  }

  @Override
  public LinearRing getRing(final int ringIndex) {
    return getEditor(ringIndex);
  }

  @Override
  public int getRingCount() {
    return this.editors.size();
  }

  @Override
  public List<LinearRing> getRings() {
    return Lists.toArray(this.editors);
  }

  @Override
  public int getVertexCount(final int[] geometryId, final int idLength) {
    if (geometryId == null || idLength < 1) {
      return 0;
    } else {
      final int partIndex = geometryId[0];
      final LinearRingEditor editor = getEditor(partIndex);
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
      final int ringIndex = vertexId[0];
      final LinearRingEditor editor = getEditor(ringIndex);
      if (editor != null) {
        final int[] childVertexId = Arrays.copyOfRange(vertexId, 1, vertexId.length);
        editor.insertVertex(childVertexId, point);
      }
    }
    return this;
  }

  @Override
  public boolean isEmpty() {
    return this.editors.isEmpty();
  }

  @Override
  public Polygon newGeometry() {
    final int ringCount = this.editors.size();
    final LinearRing[] rings = new LinearRing[ringCount];
    int ringIndex = 0;
    for (final LinearRingEditor editor : this.editors) {
      rings[ringIndex++] = editor.newGeometry();
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (this.polygon == null) {
      return geometryFactory.polygon(rings);
    } else {
      return this.polygon.newPolygon(geometryFactory, rings);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <GA extends Geometry> GA newGeometryAny() {
    return (GA)newGeometry();
  }

  @Override
  public Polygon newPolygon(final GeometryFactory geometryFactory, final LinearRing... rings) {
    return this.polygon.newPolygon(geometryFactory, rings);
  }

  @Override
  public Iterable<PolygonEditor> polygonEditors() {
    return Collections.singletonList(this);
  }

  @Override
  public void removeGeometry(final int partIndex) {
    this.editors.clear();
    setModified(true);
  }

  @Override
  public Polygon removeHoles() {
    while (this.editors.size() > 1) {
      this.editors.remove(this.editors.size() - 1);
    }
    return this;
  }

  public void removeRing(final int index) {
    this.editors.remove(index);
    setModified(true);
  }

  @Override
  public void revertChanges() {
    this.editors.clear();
    for (final LinearRing ring : this.polygon.rings()) {
      final LinearRingEditor editor = new LinearRingEditor(this, ring);
      this.editors.add(editor);
    }
    setModified(false);
  }

  public Iterable<LinearRingEditor> ringEditors() {
    return Lists.toArray(this.editors);
  }

  @Override
  public PolygonEditor setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      super.setAxisCount(axisCount);
      for (final LinearRingEditor editor : this.editors) {
        editor.setAxisCount(axisCount);
      }
    }
    return this;
  }

  @Override
  public double setCoordinate(final int ringIndex, final int vertexIndex, final int axisIndex,
    final double coordinate) {
    final LinearRingEditor editor = getEditor(ringIndex);
    if (editor == null) {
      return Double.NaN;
    } else {
      return editor.setCoordinate(vertexIndex, axisIndex, coordinate);
    }
  }

  @Override
  public double setCoordinate(final int partIndex, final int ringIndex, final int vertexIndex,
    final int axisIndex, final double coordinate) {
    if (partIndex == 0) {
      return setCoordinate(ringIndex, vertexIndex, axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public PolygonEditor setCoordinate(final int[] vertexId, final int axisIndex,
    final double coordinate) {
    if (vertexId == null || vertexId.length != 2) {
      throw new IllegalArgumentException("Invalid vertex Id");
    } else {
      final int ringIndex = vertexId[0];
      final int vertexIndex = vertexId[1];
      setCoordinate(ringIndex, vertexIndex, axisIndex, coordinate);
    }
    return this;
  }

  @Override
  public void simplifyStraightLines() {
    for (final LinearRingEditor ringEditor : ringEditors()) {
      ringEditor.simplifyStraightLines();
    }
  }
}
