package com.revolsys.record.property;

import com.revolsys.geometry.model.LineString;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public class LengthFieldName extends AbstractRecordDefinitionProperty {
  public static final String PROPERTY_NAME = LengthFieldName.class.getName() + ".propertyName";

  public static LengthFieldName getProperty(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static LengthFieldName getProperty(final RecordDefinition recordDefinition) {
    LengthFieldName property = recordDefinition.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new LengthFieldName();
      property.setRecordDefinition(recordDefinition);
    }
    return property;
  }

  public static void setRecordLength(final Record record) {
    final LengthFieldName property = getProperty(record);
    property.setLength(record);
  }

  private String fieldName;

  public LengthFieldName() {
  }

  public LengthFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  public void setLength(final Record record) {
    if (Property.hasValue(this.fieldName)) {
      final LineString line = record.getGeometry();
      final double length = line.getLength();
      record.setValue(this.fieldName, length);
    }
  }

  @Override
  public String toString() {
    return "LengthField " + this.fieldName;
  }
}
