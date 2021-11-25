package com.revolsys.spring.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeometry.common.logging.Logs;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.StringValueResolver;

import com.revolsys.spring.BeanReference;
import com.revolsys.spring.TargetBeanFactoryBean;
import com.revolsys.spring.factory.Parameter;
import com.revolsys.spring.util.PlaceholderResolvingStringValueResolver;

public class BeanConfigurrer
  implements BeanFactoryPostProcessor, ApplicationContextAware, BeanNameAware, PriorityOrdered {

  public static final Pattern KEY_PATTERN = Pattern
    .compile("(\\w[\\w\\d]*)(?:(?:\\[([\\w\\d]+)\\])|(?:\\.([\\w\\d]+)))?");

  public static void newParameterBeanDefinition(final ConfigurableListableBeanFactory factory,
    final String beanName, final Object value) {
    final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
    beanDefinition.setBeanClass(Parameter.class);
    final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
    propertyValues.add("type", value.getClass());
    propertyValues.add("value", value);
    ((DefaultListableBeanFactory)factory).registerBeanDefinition(beanName, beanDefinition);
  }

  /**
   * Apply the given property value to the corresponding bean.
   */
  public static void setAttributeValue(final ConfigurableListableBeanFactory factory,
    final String beanName, final String property, final Object value) {
    final ClassLoader classLoader = factory.getBeanClassLoader();

    BeanDefinition bd = factory.getBeanDefinition(beanName);
    while (bd.getOriginatingBeanDefinition() != null) {
      bd = bd.getOriginatingBeanDefinition();
    }
    final MutablePropertyValues propertyValues = bd.getPropertyValues();
    PropertyValue propertyValue = new PropertyValue(property, value);
    final String beanClassName = bd.getBeanClassName();
    if (!TargetBeanFactoryBean.class.getName().equals(beanClassName)) {
      if (Parameter.class.getName().equals(beanClassName)) {
        final PropertyValue typeValue = propertyValues.getPropertyValue("type");
        if (typeValue != null) {
          final String typeClassName = typeValue.getValue().toString();
          try {
            final Class<?> typeClass = Class.forName(typeClassName, true, classLoader);

            final Object convertedValue = new SimpleTypeConverter().convertIfNecessary(value,
              typeClass);
            propertyValue = new PropertyValue(property, convertedValue);
          } catch (final Throwable e) {
            Logs.error(BeanConfigurrer.class,
              "Unable to set " + beanName + "." + property + "=" + value, e);
          }
        }
      }
      propertyValues.addPropertyValue(propertyValue);
    }
  }

  private ApplicationContext applicationContext;

  private final Map<String, Object> attributes = new LinkedHashMap<>();

  private String beanName;

  private boolean ignoreInvalidKeys = true;

  private boolean ignoreUnresolvablePlaceholders = true;

  private int order = Ordered.LOWEST_PRECEDENCE;

  public BeanConfigurrer() {
  }

  public BeanConfigurrer(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public BeanConfigurrer(final ApplicationContext applicationContext,
    final Map<String, Object> attributes) {
    this.applicationContext = applicationContext;
    setAttributes(attributes);
  }

  public String getBeanName() {
    return this.beanName;
  }

  public Map<String, Object> getFields() {
    return this.attributes;
  }

  @Override
  public int getOrder() {
    return this.order;
  }

  public boolean isIgnoreInvalidKeys() {
    return this.ignoreInvalidKeys;
  }

  public boolean isIgnoreUnresolvablePlaceholders() {
    return this.ignoreUnresolvablePlaceholders;
  }

  @Override
  public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory)
    throws BeansException {
    processPlaceholderAttributes(beanFactory, this.attributes);
    processOverrideAttributes(beanFactory, this.attributes);
  }

  /**
   * Process the given key as 'beanName.property' entry.
   */
  protected void processOverride(final ConfigurableListableBeanFactory factory, final String key,
    Object value) {

    try {
      if (value instanceof BeanReference) {
        final BeanReference reference = (BeanReference)value;
        value = reference.getBean();
      }
      final Matcher matcher = BeanConfigurrer.KEY_PATTERN.matcher(key);
      if (matcher.matches()) {
        final String beanName = matcher.group(1);
        final String mapKey = matcher.group(2);
        final String propertyName = matcher.group(3);

        if (mapKey == null) {
          if (propertyName == null) {
            if (factory.containsBean(beanName)) {
              BeanDefinition beanDefinition = factory.getBeanDefinition(beanName);
              try {
                final ClassLoader classLoader = this.applicationContext.getClassLoader();
                final String beanClassName = beanDefinition.getBeanClassName();
                final Class<?> beanClass = Class.forName(beanClassName, true, classLoader);
                if (Parameter.class.isAssignableFrom(beanClass)) {
                  while (beanDefinition.getOriginatingBeanDefinition() != null) {
                    beanDefinition = beanDefinition.getOriginatingBeanDefinition();
                  }
                  final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
                  PropertyValue propertyValue = new PropertyValue("value", value);
                  final PropertyValue typeValue = propertyValues.getPropertyValue("type");
                  if (typeValue != null) {
                    try {
                      final Class<?> typeClass;
                      final Object typeValueObject = typeValue.getValue();
                      if (typeValueObject instanceof Class<?>) {
                        typeClass = (Class<?>)typeValueObject;
                      } else {
                        final String typeClassName = typeValueObject.toString();
                        typeClass = Class.forName(typeClassName, true, classLoader);
                      }
                      final Object convertedValue = new SimpleTypeConverter()
                        .convertIfNecessary(value, typeClass);
                      propertyValue = new PropertyValue("value", convertedValue);
                    } catch (final Throwable e) {
                      Logs.error(this, "Unable to set " + beanName + ".value=" + value, e);
                    }
                  }
                  propertyValues.addPropertyValue(propertyValue);
                }
              } catch (final ClassNotFoundException e) {
                Logs.error(this, "Unable to set " + beanName + ".value=" + value, e);
              }
            } else if (value != null) {
              newParameterBeanDefinition(factory, beanName, value);
            }
          } else {
            setAttributeValue(factory, beanName, propertyName, value);
            Logs.debug(this, "Property '" + key + "' set to value [" + value + "]");
          }
        } else if (propertyName == null) {
          setMapValue(factory, key, beanName, mapKey, value);
        } else {
          Logs.error(this, "Invalid syntax unable to set " + key + "=" + value);
        }
      }
    } catch (final BeansException ex) {
      final String msg = "Could not process key '" + key + "' in PropertyOverrideConfigurer";
      if (!this.ignoreInvalidKeys) {
        throw new BeanInitializationException(msg, ex);
      }
      Logs.debug(this, msg, ex);
    }
  }

  protected void processOverrideAttributes(final ConfigurableListableBeanFactory beanFactory,
    final Map<String, Object> attributes) {

    for (final Entry<String, Object> attribute : attributes.entrySet()) {
      final String key = attribute.getKey();
      final Object value = attribute.getValue();
      processOverride(beanFactory, key, value);
    }
  }

  protected void processPlaceholderAttributes(final ConfigurableListableBeanFactory beanFactory,
    final Map<String, Object> attributes) throws BeansException {
    final Map<String, Object> attributeMap = new LinkedHashMap<>();
    for (final Entry<String, Object> entry : attributes.entrySet()) {
      final String key = entry.getKey();
      final Object value = entry.getValue();
      if (!(value instanceof BeanReference)) {
        attributeMap.put(key, value);
      }
    }
    final StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver("${", "}",
      this.ignoreUnresolvablePlaceholders, null, attributeMap);
    final BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);

    final String[] beanNames = beanFactory.getBeanDefinitionNames();
    for (final String beanName2 : beanNames) {
      // Check that we're not parsing our own bean definition,
      // to avoid failing on unresolvable placeholders in properties file
      // locations.
      if (!(beanName2.equals(this.beanName) && beanFactory.equals(this.applicationContext))) {
        final BeanDefinition bd = beanFactory.getBeanDefinition(beanName2);
        try {
          visitor.visitBeanDefinition(bd);
        } catch (final BeanDefinitionStoreException ex) {
          throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName2,
            ex.getMessage());
        }
      }
    }

    // NEW in Spring 2.5: resolve placeholders in alias target names and aliases
    // as well.
    beanFactory.resolveAliases(valueResolver);
  }

  protected void processPlaceholderAttributes(final ConfigurableListableBeanFactory beanFactory,
    final String beanName, final Map<String, Object> attributes) throws BeansException {

    final StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver("${", "}",
      this.ignoreUnresolvablePlaceholders, null, attributes);
    final BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);

    // Check that we're not parsing our own bean definition,
    // to avoid failing on unresolvable placeholders in properties file
    // locations.
    if (!(beanName.equals(this.beanName) && beanFactory.equals(this.applicationContext))) {
      final BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
      try {
        visitor.visitBeanDefinition(bd);
      } catch (final BeanDefinitionStoreException ex) {
        throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
          ex.getMessage());
      }
    }

    // NEW in Spring 2.5: resolve placeholders in alias target names and aliases
    // as well.
    beanFactory.resolveAliases(valueResolver);
  }

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  protected void setAttribute(final String name, final Object value) {
    this.attributes.put(name, value);
  }

  public void setAttributes(final Map<String, ? extends Object> attributes) {
    this.attributes.clear();
    if (attributes != null) {
      this.attributes.putAll(attributes);
    }
  }

  @Override
  public void setBeanName(final String beanName) {
    this.beanName = beanName;
  }

  /**
   * Set whether to ignore invalid keys. Default is "false".
   * <p>
   * If you ignore invalid keys, keys that do not follow the 'beanName.property'
   * format will just be logged as warning. This allows to have arbitrary other
   * keys in a properties file.
   */
  public void setIgnoreInvalidKeys(final boolean ignoreInvalidKeys) {
    this.ignoreInvalidKeys = ignoreInvalidKeys;
  }

  public void setIgnoreUnresolvablePlaceholders(final boolean ignoreUnresolvablePlaceholders) {
    this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
  }

  public void setMapValue(final ConfigurableListableBeanFactory factory, final String key,
    final String beanName, final String mapKey, final Object value) {
    final BeanDefinition beanDefinition = factory.getBeanDefinition(beanName);
    final String beanClassName = beanDefinition.getBeanClassName();
    try {
      final ClassLoader classLoader = this.applicationContext.getClassLoader();
      final Class<?> beanClass = Class.forName(beanClassName, true, classLoader);
      if (MapFactoryBean.class.isAssignableFrom(beanClass)) {
        final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        final PropertyValue sourceMapProperty = propertyValues.getPropertyValue("sourceMap");
        @SuppressWarnings("unchecked")
        final Map<Object, Object> sourceMap = (Map<Object, Object>)sourceMapProperty.getValue();
        boolean found = false;
        for (final Entry<Object, Object> entry : sourceMap.entrySet()) {
          final Object mapEntryKey = entry.getKey();
          if (mapEntryKey instanceof TypedStringValue) {
            final TypedStringValue typedKey = (TypedStringValue)mapEntryKey;
            if (typedKey.getValue().equals(mapKey)) {
              entry.setValue(value);
              found = true;
            }
          }
        }
        if (!found) {
          sourceMap.put(new TypedStringValue(mapKey), value);
        }
      } else if (!TargetBeanFactoryBean.class.isAssignableFrom(beanClass)) {
        Logs.error(this, "Bean class must be a MapFactoryBean, unable to set " + key + "=" + value);
      }
    } catch (final ClassNotFoundException e) {
      Logs.error(this, "Unable to set " + key + "=" + value, e);
    }
  }

  public void setOrder(final int order) {
    this.order = order;
  }

}
