package com.revolsys.spring.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.TypedStringValue;

public class SetBeanProperties implements BeanFactoryPostProcessor, InitializingBean {
  private Map<String, String> beanPropertyNames = new LinkedHashMap<>();

  private Object propertyValue;

  private String ref;

  private String targetTypeName;

  private Object value;

  public void addBeanPropertyName(final String beanName, final String propertyName) {
    this.beanPropertyNames.put(beanName, propertyName);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    assert this.value != null & this.ref != null : "Cannot have a value and a ref";
    if (this.ref != null) {
      this.propertyValue = new RuntimeBeanNameReference(this.ref);
    } else if (this.value != null) {
      if (this.value instanceof String) {
        if (this.targetTypeName == null) {
          this.propertyValue = new TypedStringValue((String)this.value);
        } else {
          this.propertyValue = new TypedStringValue((String)this.value, this.targetTypeName);
        }
      } else {
        this.propertyValue = this.value;
      }
    }
  }

  public Map<String, String> getBeanPropertyNames() {
    return this.beanPropertyNames;
  }

  protected Object getPropertyValue() {
    return this.propertyValue;
  }

  public String getRef() {
    return this.ref;
  }

  public String getTargetTypeName() {
    return this.targetTypeName;
  }

  public Object getValue() {
    return this.value;
  }

  @Override
  public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory)
    throws BeansException {
    for (final Entry<String, String> beanPropertyName : this.beanPropertyNames.entrySet()) {
      String beanName = beanPropertyName.getKey();
      final String[] aliases = beanFactory.getAliases(beanName);
      if (aliases.length > 0) {
        beanName = aliases[0];
      }
      final String propertyName = beanPropertyName.getValue();
      final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
      beanDefinition.setLazyInit(false);
      final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
      propertyValues.add(propertyName, this.propertyValue);
    }
  }

  public void setBeanPropertyNames(final Map<String, String> beanPropertyNames) {
    this.beanPropertyNames = beanPropertyNames;
  }

  protected void setPropertyValue(final Object propertyValue) {
    this.propertyValue = propertyValue;
  }

  public void setRef(final String ref) {
    this.ref = ref;
  }

  public void setTargetTypeName(final String targetTypeName) {
    this.targetTypeName = targetTypeName;
  }

  public void setValue(final Object value) {
    this.value = value;
  }
}
