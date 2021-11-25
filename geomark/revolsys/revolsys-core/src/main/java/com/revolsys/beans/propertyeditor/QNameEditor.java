package com.revolsys.beans.propertyeditor;

import java.beans.PropertyEditorSupport;

import javax.xml.namespace.QName;

public class QNameEditor extends PropertyEditorSupport {
  public QNameEditor() {
  }

  @Override
  public String getAsText() {
    final QName value = (QName)getValue();
    if (value == null) {
      return "";
    } else {
      return value.toString();
    }
  }

  @Override
  public void setAsText(final String text) throws IllegalArgumentException {
    setValue(QName.valueOf(text));
  }
}
