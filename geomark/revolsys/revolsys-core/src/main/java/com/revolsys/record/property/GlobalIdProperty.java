package com.revolsys.record.property;

import java.util.UUID;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public class GlobalIdProperty extends AbstractRecordDefinitionProperty {
  static final String PROPERTY_NAME = "http://revolsys.com/gis/globalId";

  public static GlobalIdProperty getProperty(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static GlobalIdProperty getProperty(final RecordDefinition recordDefinition) {
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getProperty(PROPERTY_NAME);
    }
  }

  public static Identifier setIdentifier(final Record record) {
    final GlobalIdProperty globalIdProperty = getProperty(record);
    if (globalIdProperty != null) {
      final String globalIdFieldName = globalIdProperty.getFieldName();
      if (!record.hasValue(globalIdFieldName)) {
        final String id = UUID.randomUUID().toString();
        record.setValue(globalIdFieldName, id);
        return Identifier.newIdentifier(id);
      }
    }
    return null;
  }

  private String fieldName;

  public GlobalIdProperty() {
  }

  public GlobalIdProperty(final String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public GlobalIdProperty clone() {
    return (GlobalIdProperty)super.clone();
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

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    if (this.fieldName == null) {
      this.fieldName = recordDefinition.getIdFieldName();
    }
    super.setRecordDefinition(recordDefinition);
  }

}
