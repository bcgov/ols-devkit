package com.revolsys.i18n;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.revolsys.beans.AbstractPropertyChangeSupportProxy;

public final class I18n extends AbstractPropertyChangeSupportProxy {
  /** The map from category names to I18n instances. */
  private static Map<String, WeakReference<I18n>> instances = new HashMap<>();

  private static Locale locale = Locale.getDefault();

  private static final I18n NULL_INSTANCE = new I18n();

  public static CharSequence getCharSequence(final Class<?> clazz, final String key) {
    final I18n i18n = I18n.getInstance(clazz);
    String fullKey;
    if (key != null && key.trim().length() > 0) {
      fullKey = clazz.getName() + "." + key;
    } else {
      fullKey = clazz.getName();
    }
    return new I18nCharSequence(i18n, fullKey);
  }

  private static I18n getExactInstance(final ClassLoader classLoader, final String resourcePath) {
    synchronized (instances) {
      WeakReference<I18n> i18nRef = instances.get(resourcePath);
      I18n i18n = null;
      if (i18nRef != null) {
        i18n = i18nRef.get();
      }
      if (i18n == null) {
        i18n = new I18n(classLoader, resourcePath);
        i18nRef = new WeakReference<>(i18n);
        instances.put(resourcePath, i18nRef);
      }
      return i18n;
    }
  }

  public static I18n getInstance(final Class clazz) {
    final ClassLoader classLoader = clazz.getClassLoader();
    final String className = clazz.getName();
    return getInstance(classLoader, className);
  }

  private static I18n getInstance(final ClassLoader classLoader, final String resourceName) {
    synchronized (instances) {
      final String resourcePath = resourceName.replace('.', '/');
      try {

        final I18n i18n = getExactInstance(classLoader, resourcePath);
        return i18n;
      } catch (final MissingResourceException e) {
        try {

          final I18n i18n = getExactInstance(classLoader, resourcePath + "/i18n");
          return i18n;
        } catch (final MissingResourceException e2) {

          final int dotIndex = resourceName.lastIndexOf('.');
          if (dotIndex != -1) {
            final String parentName = resourceName.substring(0, dotIndex);
            final I18n i18n = getInstance(classLoader, parentName);
            return i18n;
          }
        }
        return NULL_INSTANCE;
      }

    }
  }

  public static String getString(final Class<?> clazz, final String key) {
    return getCharSequence(clazz, key).toString();
  }

  public static void setLocale(final Locale locale) {
    synchronized (instances) {
      final Locale oldLocale = I18n.locale;
      I18n.locale = locale;
      for (final WeakReference<I18n> reference : instances.values()) {
        final I18n i18n = reference.get();
        if (i18n != null) {
          i18n.loadResourceBundle();
          i18n.firePropertyChange("locale", oldLocale, locale);
        }
      }
    }
  }

  private WeakReference<ClassLoader> classLoaderReference;

  private ResourceBundle resourceBundle;

  private String resourcePath;

  private I18n() {
  }

  /**
   * Construct an I18n instance for the category.
   *
   * @param resourcePath The path to the language files.
   */
  private I18n(final ClassLoader classLoader, final String resourcePath) {
    this.classLoaderReference = new WeakReference<>(classLoader);
    this.resourcePath = resourcePath;
    loadResourceBundle();
  }

  public CharSequence getCharSequence(final String key) {
    return new I18nCharSequence(this, key);
  }

  /**
   * Get the I18n text from the language file associated with this instance. If
   * no label is defined then a default string is created from the last part of
   * the key.
   *
   * @param key The key of the text in the language file.
   * @return The I18Nized text.
   */
  public String getString(final String key) {
    if (this.resourceBundle != null) {
      try {
        return this.resourceBundle.getString(key);
      } catch (final java.util.MissingResourceException e) {

      }
    }
    final String[] labelpath = key.split("\\.");
    return labelpath[labelpath.length - 1];
  }

  private void loadResourceBundle() {
    final ClassLoader classLoader = this.classLoaderReference.get();
    this.resourceBundle = ResourceBundle.getBundle(this.resourcePath, Locale.getDefault(),
      classLoader);
  }

  @Override
  public String toString() {
    return this.resourcePath;
  }
}
