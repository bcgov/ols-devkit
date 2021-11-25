package com.revolsys.beans.propertyeditor;

import java.beans.PropertyEditorSupport;

public class BooleanEditor extends PropertyEditorSupport {
  public BooleanEditor() {
  }

  @Override
  public String getAsText() {
    final Boolean value = (Boolean)getValue();
    if (value == null) {
      return "";
    } else {
      return value.toString();
    }
  }

  @Override
  public void setAsText(final String text) throws IllegalArgumentException {
    setValue(Boolean.valueOf(text));
  }
}
