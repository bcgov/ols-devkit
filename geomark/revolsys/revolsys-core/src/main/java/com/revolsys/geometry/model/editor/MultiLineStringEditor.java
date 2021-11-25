package com.revolsys.geometry.model.editor;

import java.util.function.Consumer;

import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Function4Double;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataType;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.Point;

public class MultiLineStringEditor
  extends AbstractGeometryCollectionEditor<Lineal, LineString, LineStringEditor>
  implements MultiLineString, LinealEditor {
  private static final long serialVersionUID = 1L;

  private Lineal lineal;

  public MultiLineStringEditor(final GeometryCollectionImplEditor geometryEditor,
    final Lineal lineal) {
    super(geometryEditor, lineal);
    this.lineal = lineal;
  }

  public MultiLineStringEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public MultiLineStringEditor(final Lineal lineal) {
    this(null, lineal);
  }

  public MultiLineStringEditor appendVertex(final int lineIndex, final boolean allowRepeated,
    final double... coordinates) {
    final LineStringEditor editor = getEditor(lineIndex);
    editor.appendVertex(allowRepeated, coordinates);
    return this;
  }

  public MultiLineStringEditor appendVertex(final int lineIndex, final double... coordinates) {
    final LineStringEditor editor = getEditor(lineIndex);
    editor.appendVertex(coordinates);
    return this;
  }

  public MultiLineStringEditor appendVertex(final int lineIndex, final double x, final double y) {
    final LineStringEditor editor = getEditor(lineIndex);
    editor.appendVertex(x, y);
    return this;
  }

  public MultiLineStringEditor appendVertex(final int lineIndex, final double x, final double y,
    final boolean allowRepeated) {
    final LineStringEditor editor = getEditor(lineIndex);
    editor.appendVertex(x, y, allowRepeated);
    return this;
  }

  public MultiLineStringEditor appendVertex(final int lineIndex, final double x, final double y,
    final double z) {
    final LineStringEditor editor = getEditor(lineIndex);
    editor.appendVertex(x, y, z);
    return this;
  }

  public MultiLineStringEditor appendVertex(final int lineIndex, final LineString line,
    final int vertexIndex) {
    final LineStringEditor editor = getEditor(lineIndex);
    editor.appendVertex(line, vertexIndex);
    return this;
  }

  public MultiLineStringEditor appendVertex(final int lineIndex, final LineString line,
    final int vertexIndex, final boolean allowRepeated) {
    final LineStringEditor editor = getEditor(lineIndex);
    editor.appendVertex(line, vertexIndex, allowRepeated);
    return this;
  }

  public MultiLineStringEditor appendVertex(final int lineIndex, final Point point) {
    final LineStringEditor editor = getEditor(lineIndex);
    editor.appendVertex(point);
    return this;
  }

  public MultiLineStringEditor appendVertex(final int lineIndex, final Point point,
    final boolean allowRepeated) {
    final LineStringEditor editor = getEditor(lineIndex);
    editor.appendVertex(point, allowRepeated);
    return this;
  }

  public MultiLineStringEditor appendVertices(final int lineIndex, final Geometry points) {
    final LineStringEditor editor = getEditor(lineIndex);
    editor.appendVertices(points);
    return this;
  }

  public MultiLineStringEditor appendVertices(final int lineIndex,
    final Iterable<? extends Point> points) {
    final LineStringEditor editor = getEditor(lineIndex);
    editor.appendVertices(points);
    return this;
  }

  @Override
  public MultiLineStringEditor clone() {
    return (MultiLineStringEditor)super.clone();
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
  public int getLineStringCount() {
    return this.editors.size();
  }

  @Override
  public GeometryDataType<LineString, LineStringEditor> getPartDataType() {
    return GeometryDataTypes.LINE_STRING;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <GE extends Geometry> GE newGeometryEmpty() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (GE)geometryFactory.lineString();
  }

  @Override
  public Lineal newLineal(final GeometryFactory geometryFactory, final LineString... lines) {
    return this.lineal.newLineal(geometryFactory, lines);
  }

  @Override
  public double setCoordinate(final int partIndex, final int vertexIndex, final int axisIndex,
    final double coordinate) {
    final LineStringEditor editor = getEditor(partIndex);
    if (editor == null) {
      return Double.NaN;
    } else {
      return editor.setCoordinate(vertexIndex, axisIndex, coordinate);
    }
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
