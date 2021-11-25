package com.revolsys.spring.factory;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

public class Parameter implements FactoryBean<Object> {

  public static void registerBeanDefinition(final BeanDefinitionRegistry registry,
    final BeanFactory beanFactory, final String beanName) {
    registerBeanDefinition(registry, beanFactory, beanName, beanName);
  }

  public static void registerBeanDefinition(final BeanDefinitionRegistry registry,
    final BeanFactory beanFactory, final String beanName, final String alias) {
    if (beanFactory.containsBean(beanName)) {
      final Object value = beanFactory.getBean(beanName);
      registerBeanDefinition(registry, alias, value);
    }
  }

  public static void registerBeanDefinition(final BeanDefinitionRegistry registry,
    final String beanName, final Object value) {
    final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
    beanDefinition.setBeanClass(Parameter.class);
    final MutablePropertyValues values = beanDefinition.getPropertyValues();
    values.add("value", value);
    registry.registerBeanDefinition(beanName, beanDefinition);
  }

  private Class<?> type;

  private Object value;

  public Parameter() {
  }

  public Parameter(final Object value) {
    this.value = value;
  }

  @Override
  public Object getObject() throws Exception {
    return this.value;
  }

  @Override
  public Class getObjectType() {
    if (this.type == null && this.value != null) {
      return this.value.getClass();
    } else {
      return this.type;
    }
  }

  public Class getType() {
    return this.type;
  }

  public Object getValue() {
    return this.value;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void setType(final Class type) {
    this.type = type;
  }

  public void setValue(final Object value) {
    this.value = value;
  }

}
