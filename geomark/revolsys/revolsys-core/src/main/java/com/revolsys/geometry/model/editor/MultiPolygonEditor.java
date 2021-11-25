package com.revolsys.geometry.model.editor;

import java.util.function.Consumer;

import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Function4Double;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataType;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;

public class MultiPolygonEditor
  extends AbstractGeometryCollectionEditor<Polygonal, Polygon, PolygonEditor>
  implements MultiPolygon, PolygonalEditor {
  private static final long serialVersionUID = 1L;

  private Polygonal polygonal;

  public MultiPolygonEditor(final GeometryCollectionImplEditor parentEditor,
    final Polygonal polygonal) {
    super(parentEditor, polygonal);
    this.polygonal = polygonal;
  }

  public MultiPolygonEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public MultiPolygonEditor(final Polygonal polygonal) {
    this(null, polygonal);
  }

  public void addPolygon(final LinearRing ring) {
    final LinearRingEditor linearRingEditor = LinearRingEditor.getEditor(ring);
    final PolygonEditor editor = new PolygonEditor(this, linearRingEditor);
    addEditor(editor);
  }

  @Override
  public MultiPolygonEditor clone() {
    return (MultiPolygonEditor)super.clone();
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
    for (final GeometryEditor<?> editor : this.editors) {
      action.accept(editor);
    }
  }

  @Override
  public void forEachPolygon(final Consumer<Polygon> action) {
    for (int i = 0; i < getGeometryCount(); i++) {
      final Polygon polygon = getEditor(i);
      action.accept(polygon);
    }
  }

  @Override
  public GeometryDataType<Polygon, PolygonEditor> getPartDataType() {
    return GeometryDataTypes.POLYGON;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <GE extends Geometry> GE newGeometryEmpty() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (GE)geometryFactory.polygon();
  }

  @Override
  public Polygonal newPolygonal(final GeometryFactory geometryFactory, final Polygon... polygons) {
    return this.polygonal.newPolygonal(geometryFactory, polygons);
  }

  @Override
  public Iterable<PolygonEditor> polygonEditors() {
    return this.editors;
  }

  @Override
  public double setCoordinate(final int partIndex, final int ringIndex, final int vertexIndex,
    final int axisIndex, final double coordinate) {
    final PolygonEditor editor = getEditor(partIndex);
    if (editor == null) {
      return Double.NaN;
    } else {
      return editor.setCoordinate(ringIndex, vertexIndex, axisIndex, coordinate);
    }
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
