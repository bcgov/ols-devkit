package com.revolsys.record.schema;

import com.revolsys.properties.ObjectWithProperties;

public interface RecordDefinitionFactory extends ObjectWithProperties {
  <RD extends RecordDefinition> RD getRecordDefinition(String path);
}
