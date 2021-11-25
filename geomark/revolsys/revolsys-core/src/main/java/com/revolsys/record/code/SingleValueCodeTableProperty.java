package com.revolsys.record.code;

import java.util.Map;

import com.revolsys.record.property.RecordDefinitionProperty;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class SingleValueCodeTableProperty extends SingleValueRecordStoreCodeTable
  implements RecordDefinitionProperty {

  public static final String PROPERTY_NAME = SingleValueCodeTableProperty.class.getName();

  public SingleValueCodeTableProperty() {
    super();
  }

  public SingleValueCodeTableProperty(final Map<String, ? extends Object> config) {
    setProperties(config);
  }

  @Override
  public SingleValueCodeTableProperty clone() {
    return (SingleValueCodeTableProperty)super.clone();
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  @Override
  protected void setRecordDefinitionAfter(final RecordDefinition recordDefinition) {
    final String propertyName = getPropertyName();
    recordDefinition.setProperty(propertyName, this);
    recordDefinition.setProperty("codeTable", this);
    final RecordStore recordStore = getRecordStore();
    recordStore.addCodeTable(getCodeTable());
  }

  @Override
  protected void setRecordDefinitionBefore(final RecordDefinition oldRecordDefinition) {
    oldRecordDefinition.setProperty(getPropertyName(), null);
  }

}
