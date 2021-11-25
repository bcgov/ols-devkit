package com.revolsys.spring.factory;

import org.springframework.beans.factory.config.AbstractFactoryBean;

public class SystemPropertyFactoryBean extends AbstractFactoryBean<String> {
  private String defaultValue;

  private String name;

  @Override
  protected String createInstance() throws Exception {
    final String propertyValue = System.getProperty(this.name);
    if (propertyValue == null) {
      return this.defaultValue;
    } else {
      return propertyValue;
    }
  }

  @Override
  protected void destroyInstance(final String instance) throws Exception {
    this.name = null;
    this.defaultValue = null;
  }

  public String getDefaultValue() {
    return this.defaultValue;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public Class<?> getObjectType() {
    return String.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void setDefaultValue(final String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
