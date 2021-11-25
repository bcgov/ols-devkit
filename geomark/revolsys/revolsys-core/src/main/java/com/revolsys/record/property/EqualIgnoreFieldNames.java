package com.revolsys.record.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public class EqualIgnoreFieldNames extends AbstractRecordDefinitionProperty {
  public static final String PROPERTY_NAME = EqualIgnoreFieldNames.class.getName()
    + ".propertyName";

  public static EqualIgnoreFieldNames getProperty(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static EqualIgnoreFieldNames getProperty(final RecordDefinition recordDefinition) {
    if (recordDefinition == null) {
      return null;
    } else {
      EqualIgnoreFieldNames property = recordDefinition.getProperty(PROPERTY_NAME);
      if (property == null) {
        property = new EqualIgnoreFieldNames();
        property.setRecordDefinition(recordDefinition);
      }
      return property;
    }
  }

  private Set<String> fieldNames = new LinkedHashSet<>();

  public EqualIgnoreFieldNames() {
  }

  public EqualIgnoreFieldNames(final Collection<String> fieldNames) {
    this.fieldNames.addAll(fieldNames);
  }

  public EqualIgnoreFieldNames(final String... fieldNames) {
    this(Arrays.asList(fieldNames));
  }

  public void addFieldNames(final Collection<String> fieldNames) {
    this.fieldNames.addAll(fieldNames);
  }

  public void addFieldNames(final String... fieldNames) {
    addFieldNames(Arrays.asList(fieldNames));
  }

  public Set<String> getFieldNames() {
    return this.fieldNames;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public boolean isFieldIgnored(final String fieldName) {
    return this.fieldNames.contains(fieldName);
  }

  public void setFieldNames(final Collection<String> fieldNames) {
    setFieldNames(new LinkedHashSet<>(fieldNames));
  }

  public void setFieldNames(final Set<String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  public void setFieldNames(final String... fieldNames) {
    setFieldNames(Arrays.asList(fieldNames));
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    super.setRecordDefinition(recordDefinition);
    if (this.fieldNames.contains(Record.EXCLUDE_ID)) {
      final String idFieldName = recordDefinition.getIdFieldName();
      this.fieldNames.add(idFieldName);
    }
    if (this.fieldNames.contains(Record.EXCLUDE_GEOMETRY)) {
      final String geometryFieldName = recordDefinition.getGeometryFieldName();
      this.fieldNames.add(geometryFieldName);
    }
  }

  @Override
  public String toString() {
    return "EqualIgnore " + this.fieldNames;
  }
}
