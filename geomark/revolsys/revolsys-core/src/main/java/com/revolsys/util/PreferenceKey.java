package com.revolsys.util;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

public class PreferenceKey {

  private final String path;

  private final String name;

  private final Object defaultValue;

  private final DataType dataType;

  private String categoryTitle;

  public PreferenceKey(final String path, final String name) {
    this(path, name, DataTypes.STRING, null);
  }

  public PreferenceKey(final String path, final String name, final DataType dataType,
    final Object defaultValue) {
    this.path = path;
    this.name = name;
    this.dataType = dataType;
    this.defaultValue = defaultValue;
  }

  public String getCategoryTitle() {
    return this.categoryTitle;
  }

  public DataType getDataType() {
    return this.dataType;
  }

  public Object getDefaultValue() {
    return this.defaultValue;
  }

  public String getName() {
    return this.name;
  }

  public String getPath() {
    return this.path;
  }

  public PreferenceKey setCategoryTitle(final String categoryTitle) {
    this.categoryTitle = categoryTitle;
    return this;
  }

  @Override
  public String toString() {
    return this.path + ":" + this.name;
  }

  public <V> V toValidValue(final Object value) {
    return this.dataType.toObject(value);
  }

  public <V> V toValidValue(final String value) {
    return this.dataType.toObject(value);
  }
}
