package com.revolsys.record.io.format.csv;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;

public class GeometryFieldDefinition extends FieldDefinition {

  public GeometryFieldDefinition(final GeometryFactory geometryFactory, final String name,
    final DataType type, final boolean required) {
    super(name, type, required);
    setGeometryFactory(geometryFactory);
  }

  @Override
  public FieldDefinition clone() {
    final String name = getName();
    final DataType dataType = getDataType();
    final boolean required = isRequired();
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new GeometryFieldDefinition(geometryFactory, name, dataType, required);
  }

  @Override
  public boolean isSortable() {
    return false;
  }

  @Override
  public <V> V toFieldValueException(final Object value) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    Geometry geometry;
    if (value == null) {
      return null;
    } else if (value instanceof Geometry) {
      geometry = (Geometry)value;
      geometry = geometry.convertGeometry(geometryFactory);
    } else {
      geometry = geometryFactory.geometry(value.toString(), false);
    }
    return getDataType().toObject(geometry);
  }

  @Override
  public <V> V toFieldValueException(final RecordState state, final Object value) {
    return toFieldValueException(value);
  }
}
