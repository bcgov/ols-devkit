package com.revolsys.util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.io.FileUtil;
import com.revolsys.record.io.format.json.Json;

public class Preferences {
  public static <V> V getValue(final String applicationId, final PreferenceKey key) {
    final Preferences preferences = new Preferences(applicationId);
    return preferences.getValue(key);
  }

  private String applicationId;

  public Preferences(final String applicationId) {
    this.applicationId = applicationId;
  }

  public File getPreferenceFile(final PreferenceKey preference) {
    final String path = preference.getPath();
    if (path.contains("..")) {
      throw new IllegalArgumentException(
        "Path cannot contain the '..' character sequernce: " + path);
    }
    final File preferencesDirectory = getPreferencesDirectory();
    final File file = FileUtil.getFile(preferencesDirectory, path + ".rgobject");
    file.getParentFile().mkdirs();
    return file;
  }

  public Map<String, Object> getPreferences(final PreferenceKey preference) {
    final File file = getPreferenceFile(preference);
    if (file.exists()) {
      return Json.toMap(file);
    } else {
      return new LinkedHashMap<>();
    }
  }

  public File getPreferencesDirectory() {
    String path;
    if (OS.isWindows()) {
      path = System.getenv("APPDATA") + "/" + this.applicationId + "/Preferences";
    } else if (OS.isMac()) {
      path = System.getProperty("user.home") + "/Library/Preferences/" + this.applicationId;
    } else {
      path = System.getProperty("user.home") + "/.config/" + this.applicationId + "/Preferences";
    }
    final File directory = FileUtil.getFile(path);
    directory.mkdirs();
    return directory;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(final PreferenceKey preference) {
    final Map<String, Object> preferences = getPreferences(preference);
    final T defaultValue = (T)preference.getDefaultValue();
    if (preferences == null) {
      return defaultValue;
    } else {
      final String name = preference.getName();
      final Object value = preferences.getOrDefault(name, defaultValue);
      return (T)preference.toValidValue(value);
    }
  }

  public void setApplicationId(final String applicationId) {
    this.applicationId = applicationId;
  }

  public void setValue(final PreferenceKey preference, final Object value) {
    final Map<String, Object> preferences = getPreferences(preference);
    final String name = preference.getName();
    preferences.put(name, value);
    final File file = getPreferenceFile(preference);
    Json.writeMap(preferences, file, true);
  }

}
