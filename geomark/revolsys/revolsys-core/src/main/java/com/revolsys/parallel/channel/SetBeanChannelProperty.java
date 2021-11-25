package com.revolsys.parallel.channel;

import org.springframework.beans.factory.BeanNameAware;

import com.revolsys.parallel.channel.store.Buffer;
import com.revolsys.spring.config.SetBeanProperties;

public class SetBeanChannelProperty<T> extends SetBeanProperties implements BeanNameAware {
  private int bufferSize;

  private String inBeanName;

  private String name;

  private String outBeanName;

  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.outBeanName != null) {
      addBeanPropertyName(this.outBeanName, "out");
    }
    if (this.inBeanName != null) {
      addBeanPropertyName(this.inBeanName, "in");
    }
    final String ref = getRef();
    if (ref == null) {
      final Object value = getValue();
      if (value == null) {
        if (this.name == null) {
          this.name = getBeanPropertyNames().toString();
        }
        final Channel<T> channel;
        if (this.bufferSize > 0) {
          channel = new Channel<>(this.name, new Buffer<T>(this.bufferSize));
        } else {
          channel = new Channel<>(this.name);
        }
        setValue(channel);
      }
    }
    super.afterPropertiesSet();
  }

  public int getBufferSize() {
    return this.bufferSize;
  }

  public Channel<T> getChannel() {
    return (Channel<T>)getPropertyValue();
  }

  public String getInBeanName() {
    return this.inBeanName;
  }

  public String getOutBeanName() {
    return this.outBeanName;
  }

  @Override
  public void setBeanName(final String name) {
    this.name = name;
  }

  public void setBufferSize(final int bufferSize) {
    this.bufferSize = bufferSize;
  }

  public void setChannel(final Channel<T> channel) {
    setValue(channel);
    setPropertyValue(channel);
  }

  public void setInBeanName(final String inBeanName) {
    this.inBeanName = inBeanName;
  }

  public void setOutBeanName(final String outBeanName) {
    this.outBeanName = outBeanName;
  }
}
