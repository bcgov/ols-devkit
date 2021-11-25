package com.revolsys.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;

public class TargetBeanFactoryBean extends AbstractFactoryBean<Object> {

  private boolean instanceCreated = false;

  private Class<?> targetBeanClass;

  private BeanDefinition targetBeanDefinition;

  private BeanFactory targetBeanFactory;

  private String targetBeanName;

  public TargetBeanFactoryBean() {
  }

  @Override
  protected Object createInstance() {
    this.instanceCreated = true;
    return this.targetBeanFactory.getBean(this.targetBeanName);
  }

  @Override
  public Class<?> getObjectType() {
    if (this.targetBeanClass == null) {
      return Object.class;
    } else {
      return this.targetBeanClass;
    }
  }

  public Class<?> getTargetBeanClass() {
    return this.targetBeanClass;
  }

  public BeanDefinition getTargetBeanDefinition() {
    return this.targetBeanDefinition;
  }

  public BeanFactory getTargetBeanFactory() {
    return this.targetBeanFactory;
  }

  public String getTargetBeanName() {
    return this.targetBeanName;
  }

  public boolean isInstanceCreated() {
    return this.instanceCreated;
  }

  public void setTargetBeanClass(final Class<?> targetBeanClass) {
    this.targetBeanClass = targetBeanClass;
  }

  public void setTargetBeanDefinition(final BeanDefinition targetBeanDefinition) {
    this.targetBeanDefinition = targetBeanDefinition;
  }

  public void setTargetBeanFactory(final BeanFactory targetBeanFactory) {
    this.targetBeanFactory = targetBeanFactory;
  }

  public void setTargetBeanName(final String targetBeanName) {
    this.targetBeanName = targetBeanName;
  }

  @Override
  public String toString() {
    return "Target=" + this.targetBeanName;
  }
}
