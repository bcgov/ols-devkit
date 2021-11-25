package com.revolsys.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class BeanReference implements BeanFactoryAware {
  private BeanFactory beanFactory;

  private String name;

  public Object getBean() {
    return this.beanFactory.getBean(this.name);
  }

  public String getName() {
    return this.name;
  }

  @Override
  public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
