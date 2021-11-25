package com.revolsys.record;

import java.util.Collections;
import java.util.List;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class GeometryRecord extends BaseRecord {

  private Geometry geometry;

  public GeometryRecord(final RecordDefinition recordDefinition, final Geometry geometry) {
    super(recordDefinition);
    this.geometry = geometry;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Geometry> T getGeometry() {
    return (T)this.geometry;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getValue(final int index) {
    if (index == 0) {
      return (T)this.geometry;
    } else {
      return null;
    }
  }

  @Override
  public List<Object> getValues() {
    return Collections.singletonList(this.geometry);
  }

  @Override
  public Record setGeometryValue(final Geometry geometry) {
    if (!DataType.equal(geometry, this.geometry)) {
      setState(RecordState.MODIFIED);
    }
    this.geometry = geometry;
    return this;
  }

  @Override
  protected boolean setValue(final FieldDefinition fieldDefinition, final Object value) {
    boolean updated = false;
    final int index = fieldDefinition.getIndex();
    if (index == 0) {
      final Object newValue = fieldDefinition.toFieldValue(value);
      final Object oldValue = this.geometry;
      if (!fieldDefinition.equals(oldValue, newValue)) {
        updated = true;
        updateState();
        fieldDefinition.equals(oldValue, newValue);
      }
      this.geometry = (Geometry)newValue;
    }
    return updated;
  }
}
