package com.revolsys.record.schema;

import java.util.Collection;
import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.code.CodeTable;

public class RecordDefinitionBuilder {

  private final RecordDefinitionImpl recordDefinition;

  public RecordDefinitionBuilder() {
    this("");
  }

  public RecordDefinitionBuilder(final PathName pathName) {
    this.recordDefinition = new RecordDefinitionImpl(pathName);
  }

  public RecordDefinitionBuilder(final RecordDefinitionProxy recordDefinition) {
    this(recordDefinition.getPathName());
    addFields(recordDefinition);
    this.recordDefinition.setIdFieldNames(recordDefinition.getIdFieldNames());
    this.recordDefinition.setGeometryFieldName(recordDefinition.getGeometryFieldName());
    this.recordDefinition.setGeometryFactory(recordDefinition.getGeometryFactory());
  }

  public RecordDefinitionBuilder(final RecordDefinitionProxy recordDefinition,
    final Collection<String> fieldNames) {
    this(recordDefinition.getPathName());
    for (final String fieldName : fieldNames) {
      final FieldDefinition fieldDefinition = recordDefinition.getFieldDefinition(fieldName);
      addField(fieldDefinition);
    }
    this.recordDefinition.setIdFieldNames(
      Lists.filter(recordDefinition.getIdFieldNames(), (name) -> fieldNames.contains(name)));
    this.recordDefinition.setGeometryFieldName(recordDefinition.getGeometryFieldName());
    this.recordDefinition.setGeometryFactory(recordDefinition.getGeometryFactory());
  }

  public RecordDefinitionBuilder(final RecordStoreSchema schema, final String pathName) {
    this.recordDefinition = new RecordDefinitionImpl(schema, PathName.newPathName(pathName));
  }

  public RecordDefinitionBuilder(final String pathName) {
    this(PathName.newPathName(pathName));
  }

  public RecordDefinitionBuilder addField(final DataType type) {
    final String fieldName = type.getName();
    this.recordDefinition.addField(fieldName, type);
    return this;
  }

  public RecordDefinitionBuilder addField(final FieldDefinition field) {
    this.recordDefinition.addField(field.clone());
    return this;
  }

  public RecordDefinitionBuilder addField(final String fieldName, final DataType type) {
    this.recordDefinition.addField(fieldName, type);
    return this;
  }

  public RecordDefinitionBuilder addField(final String fieldName, final DataType type,
    final boolean required) {
    this.recordDefinition.addField(fieldName, type, required);
    return this;
  }

  public RecordDefinitionBuilder addField(final String fieldName, final DataType type,
    final int length) {
    this.recordDefinition.addField(fieldName, type, length, false);
    return this;
  }

  public RecordDefinitionBuilder addField(final String fieldName, final DataType type,
    final int length, final boolean required) {
    this.recordDefinition.addField(fieldName, type, length, required);
    return this;
  }

  public RecordDefinitionBuilder addField(final String fieldName, final DataType type,
    final int length, final int scale) {
    this.recordDefinition.addField(fieldName, type, length, scale);
    return this;
  }

  public RecordDefinitionBuilder addField(final String fieldName, final DataType type,
    final int length, final int scale, final boolean required) {
    this.recordDefinition.addField(fieldName, type, length, scale, required);
    return this;
  }

  public RecordDefinitionBuilder addFields(final RecordDefinitionProxy recordDefinition) {
    for (final FieldDefinition fieldDefinition : recordDefinition.getFieldDefinitions()) {
      addField(fieldDefinition);
    }
    return this;
  }

  public RecordDefinitionBuilder addIdField(final FieldDefinition field) {
    this.recordDefinition.addIdField(field.clone());
    return this;
  }

  public RecordDefinitionBuilder changeCodeFieldsToValues() {
    for (final FieldDefinition field : this.recordDefinition.getFields()) {
      final String fieldName = field.getName();
      final CodeTable codeTable = field.getCodeTable();
      if (codeTable != null) {
        final int length = codeTable.getValueFieldLength();
        final boolean required = field.isRequired();
        final FieldDefinition newField = new FieldDefinition(fieldName, DataTypes.STRING, length,
          required);
        this.recordDefinition.replaceField(field, newField);
      }
    }
    return this;
  }

  public PathName getPathName() {
    return this.recordDefinition.getPathName();
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public RecordDefinition newRecordDefinition(final RecordStore recordStore) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordStore.getRecordDefinition(recordDefinition);
  }

  public RecordDefinitionBuilder setGeometryFactory(final GeometryFactory geometryFactory) {
    this.recordDefinition.setGeometryFactory(geometryFactory);
    return this;
  }

  public RecordDefinitionBuilder setGeometryFieldName(final String fieldName) {
    this.recordDefinition.setGeometryFieldName(fieldName);
    return this;
  }

  public void setIdFieldName(final String name) {
    this.recordDefinition.setIdFieldName(name);
  }

  public RecordDefinitionBuilder setIdFieldNames(final List<String> idFieldNames) {
    this.recordDefinition.setIdFieldNames(idFieldNames);
    return this;
  }

  public RecordDefinitionBuilder setPathName(final String path) {
    this.recordDefinition.setPathName(path);
    return this;
  }

}
