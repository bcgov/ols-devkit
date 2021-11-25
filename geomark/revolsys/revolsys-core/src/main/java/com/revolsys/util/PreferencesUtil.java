package com.revolsys.util;

import java.util.prefs.Preferences;

public class PreferencesUtil {

  public static boolean getBoolean(final Class<?> preferencesClass, final String preferenceName) {
    final Preferences preferences = getPreferences(preferencesClass);
    return preferences.getBoolean(preferenceName, false);
  }

  public static double getDouble(final Class<?> preferencesClass, final String preferenceName) {
    final Preferences preferences = getPreferences(preferencesClass);
    return preferences.getDouble(preferenceName, 0);
  }

  public static float getFloat(final Class<?> preferencesClass, final String preferenceName) {
    final Preferences preferences = getPreferences(preferencesClass);
    return preferences.getFloat(preferenceName, 0);
  }

  public static int getInt(final Class<?> preferencesClass, final String preferenceName) {
    final Preferences preferences = getPreferences(preferencesClass);
    return preferences.getInt(preferenceName, 0);
  }

  public static Preferences getPreferences(final Class<?> preferencesClass) {
    final String preferenceGroup = preferencesClass.getName();
    return getUserPreferences(preferenceGroup);
  }

  public static String getString(final Class<?> preferencesClass, final String preferenceName) {
    final Preferences preferences = getPreferences(preferencesClass);
    return preferences.get(preferenceName, "");
  }

  public static Preferences getUserPreferences(final String preferenceGroup) {
    final Preferences userRoot = Preferences.userRoot();
    final Preferences preferences = userRoot.node(preferenceGroup);
    return preferences;
  }

  public static String getUserString(final String preferencesGroup, final String preferenceName) {
    final Preferences preferences = getUserPreferences(preferencesGroup);
    return preferences.get(preferenceName, "");
  }

  public static String getUserString(final String preferencesGroup, final String preferenceName,
    final String defaultValue) {
    final Preferences preferences = getUserPreferences(preferencesGroup);
    return preferences.get(preferenceName, defaultValue);
  }

  public static void setBoolean(final Class<?> preferencesClass, final String preferenceName,
    final boolean value) {
    final Preferences preferences = getPreferences(preferencesClass);
    preferences.putBoolean(preferenceName, value);
  }

  public static void setDouble(final Class<?> preferencesClass, final String preferenceName,
    final double value) {
    final Preferences preferences = getPreferences(preferencesClass);
    preferences.putDouble(preferenceName, value);
  }

  public static void setFloat(final Class<?> preferencesClass, final String preferenceName,
    final float value) {
    final Preferences preferences = getPreferences(preferencesClass);
    preferences.putFloat(preferenceName, value);
  }

  public static void setInt(final Class<?> preferencesClass, final String preferenceName,
    final int value) {
    final Preferences preferences = getPreferences(preferencesClass);
    preferences.putInt(preferenceName, value);
  }

  public static void setString(final Class<?> preferencesClass, final String preferenceName,
    final String value) {
    final Preferences preferences = getPreferences(preferencesClass);
    preferences.put(preferenceName, value);
  }

  public static void setUserString(final String preferencesGroup, final String preferenceName,
    final String value) {
    final Preferences preferences = getUserPreferences(preferencesGroup);
    preferences.put(preferenceName, value);
  }
}
