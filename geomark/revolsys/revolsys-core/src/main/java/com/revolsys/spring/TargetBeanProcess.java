package com.revolsys.spring;

import org.springframework.beans.factory.BeanFactory;

import com.revolsys.parallel.process.Process;
import com.revolsys.parallel.process.ProcessNetwork;

public class TargetBeanProcess implements Process {
  private final TargetBeanFactoryBean bean;

  private String beanName;

  private ProcessNetwork processNetwork;

  public TargetBeanProcess(final TargetBeanFactoryBean bean) {
    this.bean = bean;
  }

  @Override
  public String getBeanName() {
    return this.beanName;
  }

  public Process getProcess() {
    try {
      return (Process)this.bean.getObject();
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get process bean ", e);
    }
  }

  @Override
  public ProcessNetwork getProcessNetwork() {
    return this.processNetwork;
  }

  public BeanFactory getTargetBeanFactory() {
    return this.bean.getTargetBeanFactory();
  }

  public boolean isInstanceCreated() {
    return this.bean.isInstanceCreated();
  }

  @Override
  public void run() {
  }

  @Override
  public void setBeanName(final String beanName) {
    this.beanName = beanName;
  }

  @Override
  public void setProcessNetwork(final ProcessNetwork processNetwork) {
    this.processNetwork = processNetwork;
  }

  @Override
  public String toString() {
    if (this.bean == null) {
      return "Target=" + this.beanName;
    } else {
      return this.bean.toString();
    }
  }
}
