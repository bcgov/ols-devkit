package com.revolsys.record.property;

import com.revolsys.geometry.model.LineString;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public class AreaFieldName extends AbstractRecordDefinitionProperty {
  public static final String PROPERTY_NAME = AreaFieldName.class.getName() + ".propertyName";

  public static AreaFieldName getProperty(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static AreaFieldName getProperty(final RecordDefinition recordDefinition) {
    AreaFieldName property = recordDefinition.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new AreaFieldName();
      property.setRecordDefinition(recordDefinition);
    }
    return property;
  }

  public static void setRecordArea(final Record record) {
    final AreaFieldName property = getProperty(record);
    property.setArea(record);
  }

  private String fieldName;

  public AreaFieldName() {
  }

  public AreaFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public void setArea(final Record record) {
    if (Property.hasValue(this.fieldName)) {
      final LineString line = record.getGeometry();
      final double area = line.getArea();
      record.setValue(this.fieldName, area);
    }
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public String toString() {
    return "AreaField " + this.fieldName;
  }
}
