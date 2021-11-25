package com.revolsys.spring.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.context.ApplicationContext;

import com.revolsys.collection.map.AttributeMap;
import com.revolsys.collection.map.ThreadSharedProperties;
import com.revolsys.spring.TargetBeanFactoryBean;

public class AttributesBeanConfigurer extends BeanConfigurrer {

  public AttributesBeanConfigurer() {
    this(null, null);
  }

  public AttributesBeanConfigurer(final ApplicationContext applicationContext) {
    super(applicationContext, null);
  }

  public AttributesBeanConfigurer(final ApplicationContext applicationContext,
    final Map<String, Object> attributes) {
    super(applicationContext, attributes);
    setOrder(LOWEST_PRECEDENCE - 1000);
  }

  @SuppressWarnings("unchecked")
  protected void addFields(final Map<String, Object> attributes,
    final ConfigurableListableBeanFactory beanFactory, final BeanDefinition beanDefinition,
    final String beanName, final String beanClassName) {
    if (beanClassName != null) {
      if (beanClassName.equals(AttributeMap.class.getName())
        || beanName.endsWith("-AttributeMap")) {
        processPlaceholderAttributes(beanFactory, beanName, attributes);
        final Map<String, Object> otherAttributes = (Map<String, Object>)beanFactory
          .getBean(beanName);
        processPlaceholderAttributes(beanFactory, otherAttributes);
        attributes.putAll(otherAttributes);
      } else if (beanClassName.equals(MapFactoryBean.class.getName())) {
        final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        final PropertyValue targetMapClass = propertyValues.getPropertyValue("targetMapClass");
        if (targetMapClass != null) {
          final Object mapClass = targetMapClass.getValue();
          if (AttributeMap.class.getName().equals(mapClass)) {
            processPlaceholderAttributes(beanFactory, beanName, attributes);
            final Map<String, Object> otherAttributes = (Map<String, Object>)beanFactory
              .getBean(beanName);
            processPlaceholderAttributes(beanFactory, otherAttributes);
            attributes.putAll(otherAttributes);
          }
        }
      }
    }
  }

  @Override
  public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory)
    throws BeansException {
    final Map<String, Object> allAttributes = new LinkedHashMap<>();
    final Map<String, Object> threadAttributes = ThreadSharedProperties.getProperties();
    allAttributes.putAll(threadAttributes);
    processPlaceholderAttributes(beanFactory, threadAttributes);
    final Map<String, Object> attributes = getFields();
    processPlaceholderAttributes(beanFactory, attributes);
    for (final Entry<String, Object> entry : attributes.entrySet()) {
      final String key = entry.getKey();
      if (!allAttributes.containsKey(key)) {
        final Object value = entry.getValue();
        allAttributes.put(key, value);
      }
    }

    final String configBeanName = getBeanName();
    for (final String beanName : beanFactory.getBeanDefinitionNames()) {
      // Check that we're not parsing our own bean definition,
      // to avoid failing on non-resolvable place-holders in properties file
      // locations.
      if (!beanName.equals(configBeanName)) {
        final BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
        final String beanClassName = bd.getBeanClassName();

        if (beanClassName != null) {
          addFields(allAttributes, beanFactory, bd, beanName, beanClassName);
          if (beanClassName.equals(TargetBeanFactoryBean.class.getName())) {
            final MutablePropertyValues propertyValues = bd.getPropertyValues();
            final BeanDefinition targetBeanDefinition = (BeanDefinition)propertyValues
              .getPropertyValue("targetBeanDefinition")
              .getValue();
            final String targetBeanClassName = targetBeanDefinition.getBeanClassName();
            addFields(allAttributes, beanFactory, targetBeanDefinition, beanName,
              targetBeanClassName);
          }
        }
      }
    }

    processOverrideAttributes(beanFactory, allAttributes);
  }
}
