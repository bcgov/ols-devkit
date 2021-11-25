package com.revolsys.beans.propertyeditor;

import java.beans.PropertyEditorSupport;

import com.revolsys.spring.resource.Resource;

public class ResourceEditor extends PropertyEditorSupport {

  @Override
  public String getAsText() {
    final Resource value = (Resource)getValue();
    if (value == null) {
      return "";
    } else {
      return value.toString();
    }
  }

  @Override
  public void setAsText(final String text) throws IllegalArgumentException {
    final Resource resource = Resource.getResource(text);
    setValue(resource);
  }

}
