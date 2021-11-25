package com.revolsys.parallel.tools;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.logging.Logs;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyBatchUpdateException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

import com.revolsys.beans.propertyeditor.ResourceEditorRegistrar;
import com.revolsys.collection.map.ThreadSharedProperties;
import com.revolsys.parallel.AbstractRunnable;
import com.revolsys.parallel.process.ProcessNetwork;
import com.revolsys.spring.factory.Parameter;

public class ScriptExecutorRunnable extends AbstractRunnable {
  private static Throwable getBeanExceptionCause(final BeanCreationException e) {
    Throwable cause = e.getCause();
    while (cause instanceof BeanCreationException || cause instanceof MethodInvocationException
      || cause instanceof PropertyAccessException || cause instanceof PropertyBatchUpdateException
      || cause instanceof InvalidPropertyException) {
      Throwable newCause;
      if (cause instanceof PropertyBatchUpdateException) {
        final PropertyBatchUpdateException batchEx = (PropertyBatchUpdateException)cause;
        newCause = batchEx.getPropertyAccessExceptions()[0];
      } else {
        newCause = cause.getCause();
      }
      if (newCause != null) {
        cause = newCause;
      } else {
        return cause;
      }
    }
    return cause;
  }

  private Map<String, Object> attributes = new LinkedHashMap<>();

  private Map<String, Object> beans = new LinkedHashMap<>();

  private boolean logScriptInfo = true;

  private final String script;

  public ScriptExecutorRunnable(final String script) {
    this.script = script;
  }

  public ScriptExecutorRunnable(final String script, final Map<String, Object> attributes) {
    this.script = script;
    this.attributes = attributes;
  }

  public void addBean(final String name, final Object value) {
    this.beans.put(name, value);
  }

  public void addBeans(final Map<String, ?> beans) {
    this.beans.putAll(beans);
  }

  public Map<String, Object> getBeans() {
    return this.beans;
  }

  public boolean isLogScriptInfo() {
    return this.logScriptInfo;
  }

  @Override
  public void runDo() {
    final long startTime = System.currentTimeMillis();
    try {
      String logPath = null;
      final String logFileName = (String)this.attributes.get("logFile");
      if (logFileName != null && logFileName.trim().length() > 0) {
        final File logFile = new File(logFileName);
        final File parentFile = logFile.getParentFile();
        if (parentFile != null) {
          parentFile.mkdirs();
        }
        logPath = logFile.getAbsolutePath();
      }
      if (this.logScriptInfo) {
        final StringBuilder message = new StringBuilder("Processing ");
        message.append(" -s ");
        message.append(this.script);
        if (logPath != null) {
          message.append(" -l ");
          message.append(logPath);

        }
        for (final Entry<String, Object> parameter : this.attributes.entrySet()) {
          message.append(" ");
          message.append(parameter.getKey());
          message.append("=");
          message.append(parameter.getValue());
        }
        Logs.info(this, message.toString());
      }
      ThreadSharedProperties.setProperties(this.attributes);

      final GenericApplicationContext applicationContext = new GenericApplicationContext();
      applicationContext.getBeanFactory().addPropertyEditorRegistrar(new ResourceEditorRegistrar());

      for (final Entry<String, Object> entry : this.beans.entrySet()) {
        final String key = entry.getKey();
        if (key.indexOf('.') == -1 && key.indexOf('[') == -1) {
          final Object value = entry.getValue();
          final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
          beanDefinition.setBeanClass(Parameter.class);
          final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
          propertyValues.add("type", value.getClass());
          propertyValues.add("value", value);
          applicationContext.registerBeanDefinition(key, beanDefinition);
        }
      }

      final XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(applicationContext);
      if (new File(this.script).exists()) {
        beanReader.loadBeanDefinitions("file:" + this.script);
      } else {
        beanReader.loadBeanDefinitions("classpath:" + this.script);
      }
      applicationContext.refresh();
      try {
        final Object bean = applicationContext.getBean("processNetwork");
        final ProcessNetwork pipeline = (ProcessNetwork)bean;
        pipeline.startAndWait();
      } finally {
        applicationContext.close();
      }
    } catch (final BeanCreationException e) {
      final Throwable cause = getBeanExceptionCause(e);
      Logs.error(this, cause.getMessage(), cause);
      System.err.println(cause.getMessage());
      System.err.flush();
    } catch (final Throwable t) {
      Logs.error(this, t.getMessage(), t);
    }
    if (this.logScriptInfo) {
      final long endTime = System.currentTimeMillis();
      final long time = endTime - startTime;
      long seconds = time / 1000;
      final long minutes = seconds / 60;
      seconds = seconds % 60;
      Logs.info(this, minutes + " minutes " + seconds + " seconds");
    }
  }

  public void setBeans(final Map<String, Object> beans) {
    this.beans = beans;
  }

  public void setLogScriptInfo(final boolean logScriptInfo) {
    this.logScriptInfo = logScriptInfo;
  }
}
