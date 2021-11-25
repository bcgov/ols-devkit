package com.revolsys.io.filter;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.filechooser.FileFilter;

import com.revolsys.collection.list.Lists;

public final class FileNameExtensionFilter extends FileFilter {
  private final String description;

  private final List<String> extensions;

  private final String[] lowerCaseExtensions;

  public FileNameExtensionFilter(final String description, final String... extensions) {
    this.description = description;
    this.extensions = Collections.unmodifiableList(Lists.newArray(extensions));
    this.lowerCaseExtensions = new String[extensions.length];
    for (int i = 0; i < extensions.length; i++) {
      this.lowerCaseExtensions[i] = "." + extensions[i].toLowerCase(Locale.ENGLISH);
    }
  }

  @Override
  public boolean accept(final File file) {
    if (file == null) {
      return false;
    } else if (file.isDirectory()) {
      return true;
    } else {
      final String fileName = file.getName().toLowerCase();
      for (final String extension : this.lowerCaseExtensions) {
        if (fileName.endsWith(extension)) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  public List<String> getExtensions() {
    return this.extensions;
  }

  @Override
  public String toString() {
    return super.toString() + "[description=" + getDescription() + " extensions="
      + java.util.Arrays.asList(getExtensions()) + "]";
  }
}
