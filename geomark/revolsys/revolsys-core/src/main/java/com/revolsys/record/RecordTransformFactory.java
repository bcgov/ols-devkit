package com.revolsys.record;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class RecordTransformFactory implements RecordDefinitionProxy {

  private RecordDefinition recordDefinition = new RecordDefinitionImpl();

  private Supplier<Record> targetRecordFactory = () -> this.recordDefinition.newRecord();

  private List<BiConsumer<Record, Record>> sourceToTargetMappings = new ArrayList<>();

  public RecordTransformFactory(final RecordDefinition sourceRecordDefinition,
    final RecordDefinition targetRecordDefinition, final Supplier<Record> targetRecordFactory,
    final List<BiConsumer<Record, Record>> sourceToTargetMappings) {
    this.recordDefinition = targetRecordDefinition;
    if (targetRecordFactory == null) {
      this.targetRecordFactory = targetRecordDefinition::newRecord;
    } else {
      this.targetRecordFactory = targetRecordFactory;
    }
    this.sourceToTargetMappings = sourceToTargetMappings;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public Record mapRecord(final Record sourceRecord) {
    final Record targetRecord = this.targetRecordFactory.get();
    for (final BiConsumer<Record, Record> sourceToTargetMapping : this.sourceToTargetMappings) {
      sourceToTargetMapping.accept(sourceRecord, targetRecord);
    }
    return targetRecord;
  }
}
