package com.revolsys.gis.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.converter.process.SourceToTargetProcess;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Strings;

public class SimpleRecordConveter implements Converter<Record, Record> {
  private RecordFactory factory;

  private List<SourceToTargetProcess<Record, Record>> processors = new ArrayList<>();

  private RecordDefinition recordDefinition;

  public SimpleRecordConveter() {
  }

  public SimpleRecordConveter(final RecordDefinition recordDefinition) {
    setRecordDefinition(recordDefinition);
  }

  public SimpleRecordConveter(final RecordDefinition recordDefinition,
    final List<SourceToTargetProcess<Record, Record>> processors) {
    setRecordDefinition(recordDefinition);
    this.processors = processors;
  }

  public SimpleRecordConveter(final RecordDefinition recordDefinition,
    final SourceToTargetProcess<Record, Record>... processors) {
    this(recordDefinition, Arrays.asList(processors));
  }

  public void addProcessor(final SourceToTargetProcess<Record, Record> processor) {
    this.processors.add(processor);
  }

  @Override
  public Record convert(final Record sourceObject) {
    final Record targetObject = this.factory.newRecord(this.recordDefinition);
    final Geometry sourceGeometry = sourceObject.getGeometry();
    final GeometryFactory geometryFactory = sourceGeometry.getGeometryFactory();
    final Geometry targetGeometry = geometryFactory.geometry(sourceGeometry);
    targetObject.setGeometryValue(targetGeometry);
    for (final SourceToTargetProcess<Record, Record> processor : this.processors) {
      processor.process(sourceObject, targetObject);
    }
    return targetObject;
  }

  public List<SourceToTargetProcess<Record, Record>> getProcessors() {
    return this.processors;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public void setProcessors(final List<SourceToTargetProcess<Record, Record>> processors) {
    this.processors = processors;
  }

  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
    this.factory = recordDefinition.getRecordFactory();
  }

  @Override
  public String toString() {
    return this.recordDefinition.getPath() + "\n  " + Strings.toString("\n  ", this.processors);
  }
}
