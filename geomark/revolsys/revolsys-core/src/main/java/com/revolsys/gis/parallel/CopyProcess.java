package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionFactory;

public class CopyProcess extends BaseInOutProcess<Record, Record> {

  private Map<String, String> attributeMap = new HashMap<>();

  private RecordDefinition recordDefinition;

  private RecordDefinitionFactory recordDefinitionFactory;

  private String typeName;

  private Map<String, Map<Object, Object>> valueMaps = new HashMap<>();

  public CopyProcess() {
  }

  protected Record copy(final Record object) {
    Record targetObject;
    if (this.recordDefinition == null) {
      targetObject = object;
    } else {
      targetObject = new ArrayRecord(this.recordDefinition);
      for (final String fieldName : this.recordDefinition.getFieldNames()) {
        copyAttribute(object, fieldName, targetObject, fieldName);
      }
      if (this.attributeMap != null) {
        for (final Entry<String, String> mapping : this.attributeMap.entrySet()) {
          final String sourceFieldName = mapping.getKey();
          final String targetFieldName = mapping.getValue();
          copyAttribute(object, sourceFieldName, targetObject, targetFieldName);
        }
      }
    }
    return targetObject;
  }

  private void copyAttribute(final Record sourceObject, final String sourceFieldName,
    final Record targetObject, final String targetFieldName) {
    Object value = sourceObject.getValueByPath(sourceFieldName);
    final Map<Object, Object> valueMap = this.valueMaps.get(targetFieldName);
    if (valueMap != null) {
      final Object mappedValue = valueMap.get(value);
      if (mappedValue != null) {
        value = mappedValue;
      }
    }
    targetObject.setValue(targetFieldName, value);
  }

  public Map<String, String> getFieldMap() {
    return this.attributeMap;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public RecordDefinitionFactory getRecordDefinitionFactory() {
    return this.recordDefinitionFactory;
  }

  public String getTypeName() {
    return this.typeName;
  }

  public Map<String, Map<Object, Object>> getValueMaps() {
    return this.valueMaps;
  }

  @Override
  public void initializeDo() {
    if (this.recordDefinition == null) {
      this.recordDefinition = this.recordDefinitionFactory.getRecordDefinition(this.typeName);
    }
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    final Record targetObject = copy(object);
    out.write(targetObject);
  }

  public void setAttributeMap(final Map<String, String> attributeMap) {
    this.attributeMap = attributeMap;
  }

  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
  }

  public void setRecordDefinitionFactory(final RecordDefinitionFactory recordDefinitionFactory) {
    this.recordDefinitionFactory = recordDefinitionFactory;
  }

  public void setTypeName(final String typeName) {
    this.typeName = typeName;
  }

  public void setValueMaps(final Map<String, Map<Object, Object>> valueMaps) {
    this.valueMaps = valueMaps;
  }

}
