package com.revolsys.spring;

import java.io.Serializable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;

/**
 * Independent inner class - for serialization purposes.
 */
class TargetBeanObjectFactory implements ObjectFactory, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -1734089673416157600L;

  private final BeanFactory beanFactory;

  private final String targetBeanName;

  public TargetBeanObjectFactory(final BeanFactory beanFactory, final String targetBeanName) {
    this.beanFactory = beanFactory;
    this.targetBeanName = targetBeanName;
  }

  @Override
  public Object getObject() throws BeansException {
    return this.beanFactory.getBean(this.targetBeanName);
  }
}
