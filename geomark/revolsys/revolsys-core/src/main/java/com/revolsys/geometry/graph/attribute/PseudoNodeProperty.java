package com.revolsys.geometry.graph.attribute;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.geometry.graph.Node;
import com.revolsys.properties.ObjectPropertyProxy;
import com.revolsys.record.Record;
import com.revolsys.record.property.AbstractRecordDefinitionProperty;
import com.revolsys.record.schema.RecordDefinition;

public class PseudoNodeProperty extends AbstractRecordDefinitionProperty {
  protected static final List<String> DEFAULT_EXCLUDE = Arrays.asList(Record.EXCLUDE_ID,
    Record.EXCLUDE_GEOMETRY);

  public static final String PROPERTY_NAME = PseudoNodeProperty.class.getName() + ".propertyName";

  public static AbstractRecordDefinitionProperty getProperty(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static PseudoNodeProperty getProperty(final RecordDefinition recordDefinition) {
    PseudoNodeProperty property = recordDefinition.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new PseudoNodeProperty();
      property.setRecordDefinition(recordDefinition);
    }
    return property;
  }

  private Set<String> equalExcludeFieldNames = new HashSet<>(DEFAULT_EXCLUDE);

  public PseudoNodeProperty() {
  }

  public Collection<String> getEqualExcludeFieldNames() {
    return this.equalExcludeFieldNames;
  }

  public PseudoNodeAttribute getProperty(final Node<Record> node) {
    final String fieldName = PseudoNodeProperty.PROPERTY_NAME;
    if (!node.hasProperty(fieldName)) {
      final ObjectPropertyProxy<PseudoNodeAttribute, Node<Record>> proxy = new FunctionObjectPropertyProxy<>(
        this::newProperty);
      node.setProperty(fieldName, proxy);
    }
    final PseudoNodeAttribute value = node.getProperty(fieldName);
    return value;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public PseudoNodeAttribute newProperty(final Node<Record> node) {
    return new PseudoNodeAttribute(node, getTypePath(), this.equalExcludeFieldNames);
  }

  public void setEqualExcludeFieldNames(final Collection<String> equalExcludeFieldNames) {
    if (equalExcludeFieldNames == null) {
      this.equalExcludeFieldNames.clear();
    } else {
      this.equalExcludeFieldNames = new HashSet<>(equalExcludeFieldNames);
    }
    this.equalExcludeFieldNames.addAll(DEFAULT_EXCLUDE);
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    super.setRecordDefinition(recordDefinition);
  }

  @Override
  public String toString() {
    return "Pseudo Node";
  }
}
