package com.revolsys.record;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class RecordTransformFactoryBuilder {
  private final RecordDefinition sourceRecordDefinition;

  private final RecordDefinitionBuilder targetRecordDefinition;

  public Supplier<Record> targetRecordFactory;

  private final List<BiConsumer<Record, Record>> sourceToTargetMappings = new ArrayList<>();

  public RecordTransformFactoryBuilder(final RecordDefinition sourceRecordDefinition) {
    this.sourceRecordDefinition = sourceRecordDefinition;
    this.targetRecordDefinition = new RecordDefinitionBuilder(sourceRecordDefinition.getPathName());
  }

  public RecordTransformFactoryBuilder(final RecordDefinitionProxy sourceRecordDefinition) {
    this(sourceRecordDefinition.getRecordDefinition());
  }

  public void addField(final String fieldName) {
    addField(fieldName, fieldName);

  }

  public void addField(final String sourceFieldName, final String targetFieldName) {
    final FieldDefinition targetField = this.sourceRecordDefinition.getField(sourceFieldName)
      .clone()
      .setName(targetFieldName);
    this.targetRecordDefinition.addField(targetField);
    this.sourceToTargetMappings.add((sourceRecord, targetRecord) -> {
      final Object value = sourceRecord.getValue(sourceFieldName);
      if (value != null) {
        targetRecord.setValue(targetFieldName, value);
      }
    });

  }

  public RecordTransformFactoryBuilder addFields(final Iterable<String> fieldNames) {
    for (final String fieldName : fieldNames) {
      addField(fieldName);
    }
    return this;
  }

  public RecordTransformFactoryBuilder addSourceFields() {
    final List<String> fieldNames = this.sourceRecordDefinition.getFieldNames();
    return addFields(fieldNames);
  }

  public RecordTransformFactory build() {
    return new RecordTransformFactory(this.sourceRecordDefinition,
      this.targetRecordDefinition.getRecordDefinition(), this.targetRecordFactory,
      this.sourceToTargetMappings);
  }

  public RecordDefinitionBuilder getTargetRecordDefinition() {
    return this.targetRecordDefinition;
  }

  public void setTargetRecordFactory(final Supplier<Record> targetRecordFactory) {
    this.targetRecordFactory = targetRecordFactory;
  }

  public RecordTransformFactoryBuilder withTargetRecordDefinition(
    final Consumer<RecordDefinitionBuilder> action) {
    action.accept(this.targetRecordDefinition);
    return this;
  }
}
