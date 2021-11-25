package com.revolsys.gis.converter.process;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.schema.RecordDefinition;

public class CopyValues extends AbstractSourceToTargetProcess<Record, Record> {
  private Map<String, String> fieldNames = new LinkedHashMap<>();

  public CopyValues() {
  }

  public CopyValues(final Map<String, String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  public CopyValues(final String sourceName, final String targetName) {
    addFieldName(sourceName, targetName);
  }

  public void addFieldName(final String sourceName, final String targetName) {
    this.fieldNames.put(sourceName, targetName);
  }

  public Map<String, String> getFieldNames() {
    return this.fieldNames;
  }

  @Override
  public void process(final Record source, final Record target) {
    for (final Entry<String, String> entry : this.fieldNames.entrySet()) {
      final String sourceName = entry.getKey();
      final String targetName = entry.getValue();
      final Object value;
      if (sourceName.startsWith("~")) {
        value = sourceName.substring(1);
      } else {
        value = source.getValueByPath(sourceName);
      }
      if (value != null) {
        final RecordDefinition targetRecordDefinition = target.getRecordDefinition();
        final CodeTable codeTable = targetRecordDefinition.getCodeTableByFieldName(targetName);
        if (codeTable == null) {
          target.setValue(targetName, value);
        } else {
          final Object codeId = codeTable.getIdentifier(value);
          target.setValue(targetName, codeId);
        }
      }
    }
  }

  public void setFieldNames(final Map<String, String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  @Override
  public String toString() {
    return "copy" + this.fieldNames;
  }
}
