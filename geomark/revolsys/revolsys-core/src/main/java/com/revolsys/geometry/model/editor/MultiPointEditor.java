package com.revolsys.geometry.model.editor;

import java.util.function.Consumer;

import org.jeometry.common.function.BiFunctionDouble;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataType;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;

public class MultiPointEditor extends AbstractGeometryCollectionEditor<Punctual, Point, PointEditor>
  implements MultiPoint, PunctualEditor {
  private static final long serialVersionUID = 1L;

  private Punctual punctual;

  public MultiPointEditor(final AbstractGeometryEditor<?> parentEditor, final Punctual punctual) {
    super(parentEditor, punctual);
    this.punctual = punctual;
  }

  public MultiPointEditor(final GeometryCollectionImplEditor parentEditor,
    final GeometryFactory geometryFactory, final PointEditor... editors) {
    super(parentEditor, geometryFactory, editors);
  }

  public MultiPointEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public MultiPointEditor(final Punctual punctual) {
    this(null, punctual);
  }

  @Override
  public GeometryEditor<?> appendVertex(final int[] geometryId, final Point point) {
    if (geometryId == null || geometryId.length == 0) {
      appendVertex(point);
    }
    return this;
  }

  public void appendVertex(final Point point) {
    if (point != null && !point.isEmpty()) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Point newPoint = point.convertGeometry(geometryFactory);
      final PointEditor pointEditor = newPoint.newGeometryEditor(this);
      addEditor(pointEditor);
    }
  }

  @Override
  public MultiPoint clone() {
    return (MultiPoint)super.clone();
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
  public boolean equalsVertex(final int axisCount, final int vertexIndex, final Point point) {
    return getEditor(vertexIndex).equals(2, point);
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
  public int[] getFirstGeometryId() {
    return new int[0];
  }

  @Override
  public GeometryDataType<Point, PointEditor> getPartDataType() {
    return GeometryDataTypes.POINT;
  }

  @Override
  public int getVertexCount(final int[] geometryId) {
    if (geometryId == null || geometryId.length == 0) {
      return getGeometryCount();
    } else {
      return 0;
    }
  }

  @Override
  public boolean hasPoint(final double x, final double y) {
    return this.punctual.hasPoint(x, y);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <GE extends Geometry> GE newGeometryEmpty() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (GE)geometryFactory.point();
  }

  @Override
  public Punctual newPunctual(final GeometryFactory geometryFactory, final Point... points) {
    if (this.punctual == null) {
      return MultiPoint.super.newPunctual(geometryFactory, points);
    } else {
      return this.punctual.newPunctual(geometryFactory, points);
    }
  }

  @Override
  public double setCoordinate(final int partIndex, final int axisIndex, final double coordinate) {
    final PointEditor editor = getEditor(partIndex);
    if (editor == null) {
      return Double.NaN;
    } else {
      return editor.setCoordinate(axisIndex, coordinate);
    }
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
