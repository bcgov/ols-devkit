package com.revolsys.record.property;

import com.revolsys.record.schema.RecordDefinition;

public abstract class AbstractRecordDefinitionProperty implements RecordDefinitionProperty {
  private RecordDefinition recordDefinition;

  @Override
  public AbstractRecordDefinitionProperty clone() {
    try {
      final AbstractRecordDefinitionProperty clone = (AbstractRecordDefinitionProperty)super.clone();
      clone.recordDefinition = null;
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public String getTypePath() {
    return getRecordDefinition().getPath();
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    final String propertyName = getPropertyName();
    if (this.recordDefinition != null) {
      this.recordDefinition.setProperty(propertyName, null);
    }
    this.recordDefinition = recordDefinition;
    if (recordDefinition != null) {
      recordDefinition.setProperty(propertyName, this);
    }
  }

}
