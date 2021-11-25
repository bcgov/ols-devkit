package com.revolsys.properties;

import java.util.Map;

import com.revolsys.io.map.MapSerializer;
import com.revolsys.util.CaseConverter;

public abstract class AbstractNameTitle extends BaseObjectWithPropertiesAndChange
  implements MapSerializer {
  private String name;

  private String title;

  public AbstractNameTitle() {
  }

  public AbstractNameTitle(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  public AbstractNameTitle(final String name) {
    this(name, CaseConverter.toCapitalizedWords(name));
  }

  public AbstractNameTitle(final String name, final String title) {
    this.name = name;
    this.title = title;
  }

  public String getName() {
    return this.name;
  }

  public String getTitle() {
    return this.title;
  }

  @Override
  public int hashCode() {
    if (this.name != null) {
      return this.name.hashCode();
    } else {
      return super.hashCode();
    }
  }

  public void setName(final String name) {
    final Object oldValue = this.name;
    this.name = name;
    firePropertyChange("name", oldValue, this.name);
  }

  public void setTitle(final String title) {
    final Object oldValue = this.title;
    this.title = title;
    firePropertyChange("title", oldValue, this.title);
  }

  @Override
  public String toString() {
    return getTitle();
  }
}
