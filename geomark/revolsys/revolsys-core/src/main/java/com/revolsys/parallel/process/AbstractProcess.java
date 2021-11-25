package com.revolsys.parallel.process;

import org.springframework.beans.factory.BeanNameAware;

public abstract class AbstractProcess implements Process, BeanNameAware {
  private String beanName;

  private ProcessNetwork processNetwork;

  public AbstractProcess() {
    this(null);
  }

  public AbstractProcess(final String beanName) {
    setProcessNetwork(ProcessNetwork.forThread());
    if (beanName == null) {
      this.beanName = getClass().getName();
    } else {
      this.beanName = beanName;
    }
  }

  @Override
  public String getBeanName() {
    return this.beanName;
  }

  /**
   * @return the processNetwork
   */
  @Override
  public ProcessNetwork getProcessNetwork() {
    return this.processNetwork;
  }

  @Override
  public void setBeanName(final String beanName) {
    this.beanName = beanName;
  }

  /**
   * @param processNetwork the processNetwork to set
   */
  @Override
  public void setProcessNetwork(final ProcessNetwork processNetwork) {
    this.processNetwork = processNetwork;
    if (processNetwork != null) {
      processNetwork.addProcess(this);
    }
  }

  public void stop() {
  }

  @Override
  public String toString() {
    final String className = getClass().getSimpleName();
    if (this.beanName == null) {
      return className;
    } else {
      return this.beanName + " (" + className + ")";
    }
  }
}
