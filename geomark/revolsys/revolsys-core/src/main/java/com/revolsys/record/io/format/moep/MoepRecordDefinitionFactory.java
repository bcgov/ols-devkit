package com.revolsys.record.io.format.moep;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionFactory;

public class MoepRecordDefinitionFactory extends BaseObjectWithProperties
  implements RecordDefinitionFactory {
  private static final Map<String, RecordDefinition> RECORD_DEFINITION_CACHE = new HashMap<>();

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    synchronized (RECORD_DEFINITION_CACHE) {
      RecordDefinition recordDefinition = RECORD_DEFINITION_CACHE.get(typePath);
      if (recordDefinition == null) {
        recordDefinition = MoepConstants.newRecordDefinition(typePath);
        RECORD_DEFINITION_CACHE.put(typePath, recordDefinition);
      }
      return recordDefinition;
    }
  }

}
