/*
 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.logging.Logs;

import com.revolsys.io.PathUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractInOutProcess;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionFactory;

public class AddDefaultValuesProcess extends AbstractInOutProcess<Record, Record> {
  private Set<String> excludedFieldNames = new HashSet<>();

  private RecordDefinitionFactory recordDefinitionFactory;

  private String schemaName;

  private final Map<RecordDefinition, Map<String, Object>> typeDefaultValues = new HashMap<>();

  private void addDefaultValues(final Map<String, Object> defaultValues,
    final RecordDefinition type) {
    if (PathUtil.getPath(type.getPath()).equals(this.schemaName)) {
      defaultValues.putAll(type.getDefaultValues());
    }
  }

  private Map<String, Object> getDefaultValues(final RecordDefinition type) {
    if (this.schemaName == null) {
      return type.getDefaultValues();
    } else {
      Map<String, Object> defaultValues = this.typeDefaultValues.get(type);
      if (defaultValues == null) {
        defaultValues = new HashMap<>();
        addDefaultValues(defaultValues, type);
        this.typeDefaultValues.put(type, defaultValues);
      }
      return defaultValues;
    }
  }

  /**
   * Get the list of attribute names that will be excluded from having the
   * default values set.
   *
   * @return The names of the attributes to exclude.
   */
  public Set<String> getExcludedFieldNames() {
    return this.excludedFieldNames;
  }

  public RecordDefinitionFactory getRecordDefinitionFactory() {
    return this.recordDefinitionFactory;
  }

  /**
   * Get the schema name of the type definitions to get the default values from.
   *
   * @return The schema name.
   */
  public String getSchemaName() {
    return this.schemaName;
  }

  private void process(final Record record) {
    final RecordDefinition type = record.getRecordDefinition();

    boolean process = true;
    if (this.schemaName != null) {
      if (!PathUtil.getPath(type.getPath()).equals(this.schemaName)) {
        process = false;
      }
    }
    if (process) {
      processDefaultValues(record, getDefaultValues(type));
    }

    for (int i = 0; i < type.getFieldCount(); i++) {
      final Object value = record.getValue(i);
      if (value instanceof Record) {
        process((Record)value);
      }
    }
  }

  private void processDefaultValues(final Record record, final Map<String, Object> defaultValues) {
    for (final Entry<String, Object> defaultValue : defaultValues.entrySet()) {
      final String key = defaultValue.getKey();
      final Object value = defaultValue.getValue();
      setDefaultValue(record, key, value);
    }
  }

  @Override
  protected void run(final Channel<Record> in, final Channel<Record> out) {
    for (Record record = in.read(); record != null; record = in.read()) {
      process(record);
      out.write(record);
    }
  }

  private void setDefaultValue(final Record record, final String key, final Object value) {
    final int dotIndex = key.indexOf('.');
    if (dotIndex == -1) {
      if (record.getValue(key) == null && !this.excludedFieldNames.contains(key)) {
        Logs.info(this, "Adding attribute " + key + "=" + value);
        record.setValue(key, value);
      }
    } else {
      final String fieldName = key.substring(0, dotIndex);
      final String subKey = key.substring(dotIndex + 1);
      final Object attributeValue = record.getValue(fieldName);
      if (attributeValue == null) {
        final RecordDefinition type = record.getRecordDefinition();
        final int attrIndex = type.getFieldIndex(fieldName);
        final DataType dataType = type.getFieldType(attrIndex);
        final Class<?> typeClass = dataType.getJavaClass();
        if (typeClass == Record.class) {

          final RecordDefinition subClass = this.recordDefinitionFactory
            .getRecordDefinition(dataType.getName());
          final Record subObject = subClass.newRecord();
          setDefaultValue(subObject, subKey, value);
          record.setValue(fieldName, subObject);
          process(subObject);
        }
      } else if (attributeValue instanceof Record) {
        final Record subObject = (Record)attributeValue;
        setDefaultValue(subObject, subKey, value);
      } else if (!fieldName.equals(record.getRecordDefinition().getGeometryFieldName())) {
        Logs.error(this, "Attribute '" + fieldName + "' must be a Record");
      }
    }
  }

  /**
   * Set the list of attribute names that will be excluded from having the
   * default values set.
   *
   * @param excludedFieldNames The names of the attributes to exclude.
   */
  public void setExcludedFieldNames(final Set<String> excludedFieldNames) {
    this.excludedFieldNames = excludedFieldNames;
  }

  public void setRecordDefinitionFactory(final RecordDefinitionFactory recordDefinitionFactory) {
    this.recordDefinitionFactory = recordDefinitionFactory;
  }

  /**
   * Set the schema name of the type definitions to get the default values from.
   *
   * @param schemaName The schema name.
   */
  public void setSchemaName(final String schemaName) {
    this.schemaName = schemaName;
  }

}
