package com.revolsys.gis.wms.capabilities;

import org.w3c.dom.Element;

public class Extent {
  private boolean current = false;

  private final String defaultValue;

  private boolean multipleValues = false;

  private final String name;

  private boolean nearestValue = false;

  public Extent(final Element extentElement) {
    this.name = extentElement.getAttribute("name");
    this.defaultValue = extentElement.getAttribute("default");
    final String nearestValue = extentElement.getAttribute("nearestValue");
    this.nearestValue = "1".equals(nearestValue);
    final String multipleValues = extentElement.getAttribute("multipleValues");
    this.multipleValues = "1".equals(multipleValues);
    final String current = extentElement.getAttribute("current");
    this.current = "1".equals(current);
  }

  public String getDefaultValue() {
    return this.defaultValue;
  }

  public String getName() {
    return this.name;
  }

  public boolean isCurrent() {
    return this.current;
  }

  public boolean isMultipleValues() {
    return this.multipleValues;
  }

  public boolean isNearestValue() {
    return this.nearestValue;
  }
}
