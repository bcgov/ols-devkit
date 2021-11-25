package com.revolsys.spring.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * BeanDefinitionVisitor that resolves placeholders in String values, delegating
 * to the <code>parseStringValue</code> method of the containing class.
 */
public class PlaceholderResolvingStringValueResolver implements StringValueResolver {
  private final Map<String, Object> attributes;

  private final boolean ignoreUnresolvablePlaceholders;

  private final String nullValue;

  private final String placeholderPrefix;

  private final String placeholderSuffix;

  public PlaceholderResolvingStringValueResolver(final String placeholderPrefix,
    final String placeholderSuffix, final boolean ignoreUnresolvablePlaceholders,
    final String nullValue, final Map<String, Object> attributes) {
    super();
    this.placeholderPrefix = placeholderPrefix;
    this.placeholderSuffix = placeholderSuffix;
    this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    this.nullValue = nullValue;
    this.attributes = attributes;
  }

  private int findPlaceholderEndIndex(final CharSequence buf, final int startIndex) {
    int index = startIndex + this.placeholderPrefix.length();
    int withinNestedPlaceholder = 0;
    while (index < buf.length()) {
      if (StringUtils.substringMatch(buf, index, this.placeholderSuffix)) {
        if (withinNestedPlaceholder > 0) {
          withinNestedPlaceholder--;
          index = index + this.placeholderSuffix.length();
        } else {
          return index;
        }
      } else if (StringUtils.substringMatch(buf, index, this.placeholderPrefix)) {
        withinNestedPlaceholder++;
        index = index + this.placeholderPrefix.length();
      } else {
        index++;
      }
    }
    return -1;
  }

  /**
   * Parse the given String value recursively, to be able to resolve nested
   * placeholders (when resolved property values in turn contain placeholders
   * again).
   *
   * @param strVal the String value to parse
   * @param props the Properties to resolve placeholders against
   * @param visitedPlaceholders the placeholders that have already been visited
   *          during the current resolution attempt (used to detect circular
   *          references between placeholders). Only non-null if we're parsing a
   *          nested placeholder.
   * @throws BeanDefinitionStoreException if invalid values are encountered
   * @see #resolvePlaceholder(String, java.util.Properties, int)
   */
  protected String parseStringValue(final String strVal, final Map<String, Object> attributes,
    final Set<String> visitedPlaceholders) throws BeanDefinitionStoreException {

    final StringBuilder buf = new StringBuilder(strVal);

    int startIndex = strVal.indexOf(this.placeholderPrefix);
    while (startIndex != -1) {
      final int endIndex = findPlaceholderEndIndex(buf, startIndex);
      if (endIndex != -1) {
        String placeholder = buf.substring(startIndex + this.placeholderPrefix.length(), endIndex);
        if (!visitedPlaceholders.add(placeholder)) {
          throw new BeanDefinitionStoreException(
            "Circular placeholder reference '" + placeholder + "' in property definitions");
        }
        // Recursive invocation, parsing placeholders contained in the
        // placeholder key.
        placeholder = parseStringValue(placeholder, attributes, visitedPlaceholders);
        // Now obtain the value for the fully resolved key...
        final Object propValue = attributes.get(placeholder);
        if (propValue != null) {
          String propVal = propValue.toString();
          // Recursive invocation, parsing placeholders contained in the
          // previously resolved placeholder value.
          propVal = parseStringValue(propVal, attributes, visitedPlaceholders);
          buf.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);

          startIndex = buf.indexOf(this.placeholderPrefix, startIndex + propVal.length());
        } else if (this.ignoreUnresolvablePlaceholders) {
          // Proceed with unprocessed value.
          startIndex = buf.indexOf(this.placeholderPrefix,
            endIndex + this.placeholderSuffix.length());
        } else {
          throw new BeanDefinitionStoreException(
            "Could not resolve placeholder '" + placeholder + "'");
        }
        visitedPlaceholders.remove(placeholder);
      } else {
        startIndex = -1;
      }
    }

    return buf.toString();
  }

  @Override
  public String resolveStringValue(final String strVal) throws BeansException {
    final String value = parseStringValue(strVal, this.attributes, new HashSet<String>());
    return value.equals(this.nullValue) ? null : value;
  }
}
