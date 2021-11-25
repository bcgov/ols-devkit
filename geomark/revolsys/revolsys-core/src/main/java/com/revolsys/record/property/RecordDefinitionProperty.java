package com.revolsys.record.property;

import com.revolsys.record.schema.RecordDefinition;

public interface RecordDefinitionProperty extends Cloneable {

  RecordDefinitionProperty clone();

  String getPropertyName();

  RecordDefinition getRecordDefinition();

  void setRecordDefinition(RecordDefinition recordDefinition);
}
