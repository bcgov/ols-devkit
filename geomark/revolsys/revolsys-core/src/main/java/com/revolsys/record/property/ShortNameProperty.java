package com.revolsys.record.property;

import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public class ShortNameProperty extends AbstractRecordDefinitionProperty {
  static final String PROPERTY_NAME = "http://revolsys.com/gis/shortName";

  public static ShortNameProperty getProperty(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static ShortNameProperty getProperty(final RecordDefinition recordDefinition) {
    return recordDefinition.getProperty(PROPERTY_NAME);
  }

  public static String getShortName(final Record object) {
    final ShortNameProperty property = getProperty(object);
    if (property == null) {
      return null;
    } else {
      return property.getShortName();
    }
  }

  public static String getShortName(final RecordDefinition recordDefinition) {
    final ShortNameProperty property = getProperty(recordDefinition);
    if (property == null) {
      return null;
    } else {
      return property.getShortName();
    }
  }

  private String shortName;

  private boolean useForSequence = true;

  public ShortNameProperty() {
  }

  public ShortNameProperty(final String shortName) {
    this.shortName = shortName;
  }

  @Override
  public ShortNameProperty clone() {
    return new ShortNameProperty(this.shortName);
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public String getShortName() {
    return this.shortName;
  }

  public boolean isUseForSequence() {
    return this.useForSequence;
  }

  public void setShortName(final String shortName) {
    this.shortName = shortName;
  }

  public void setUseForSequence(final boolean useForSequence) {
    this.useForSequence = useForSequence;
  }

  @Override
  public String toString() {
    return this.shortName;
  }
}
