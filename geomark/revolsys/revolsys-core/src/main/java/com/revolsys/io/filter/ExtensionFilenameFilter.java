/*
 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.io.filter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * The ExtensionFilenameFilter is a {@link FilenameFilter} that only returns
 * files if the have one of the specified file extensions. More than one file
 * extension can be specified by using the {@link #addExtension(String)} method.
 *
 * @author Paul Austin
 */

public class ExtensionFilenameFilter implements FilenameFilter, FileFilter {
  /** The list of extensions to match. */
  private final Set<String> extensions = new HashSet<>();

  /** Flag indicating if the filter can be modified. */
  private boolean readOnly = false;

  /**
   * Construct a new ExtensionFilenameFilter.
   */
  public ExtensionFilenameFilter() {
  }

  /**
   * Construct a new ExtensionFilenameFilter.
   *
   * @param extensions The file extensions.
   * @param readOnly Flag indicating if the filter can be modified.
   */
  public ExtensionFilenameFilter(final boolean readOnly, final String... extensions) {
    this(Arrays.asList(extensions), readOnly);
  }

  /**
   * Construct a new ExtensionFilenameFilters.
   *
   * @param extensions The file extensions.
   */
  public ExtensionFilenameFilter(final Collection<String> extensions) {
    addExtensions(extensions);
  }

  /**
   * Construct a new ExtensionFilenameFilter.
   *
   * @param extensions The file extensions.
   * @param readOnly Flag indicating if the filter can be modified.
   */
  public ExtensionFilenameFilter(final Collection<String> extensions, final boolean readOnly) {
    addExtensions(extensions);
    this.readOnly = readOnly;
  }

  /**
   * Construct a new ExtensionFilenameFilter.
   *
   * @param extensions The file extensions.
   */
  public ExtensionFilenameFilter(final String... extensions) {
    this(Arrays.asList(extensions));
  }

  @Override
  public boolean accept(final File file) {
    final String fileName = file.getName();
    return accept(fileName);
  }

  /**
   * Check to see if the file should be included in the list of matched files
   *
   * @param directory The directory in which the file was found.
   * @param fileName The name of the file.
   * @return True if the file matched, false otherwise.
   */
  @Override
  public boolean accept(final File directory, final String fileName) {
    return accept(fileName);
  }

  public boolean accept(final String fileName) {
    String extension = "";
    final int index = fileName.lastIndexOf(".");
    if (index > -1) {
      extension = fileName.substring(index + 1);
    }
    return this.extensions.contains(extension.toLowerCase());
  }

  /**
   * Add the file extension to the extensions to find.
   *
   * @param extension The file extension.
   */
  public void addExtension(final String extension) {
    if (this.readOnly) {
      throw new IllegalArgumentException("This filname filter is readonly");
    }
    this.extensions.add(extension.toLowerCase());
  }

  /**
   * Add the file extensions to the extensions to find.
   *
   * @param extensions The file extensions.
   */
  public void addExtensions(final Collection<String> extensions) {
    if (this.readOnly) {
      throw new IllegalArgumentException("This filname filter is readonly");
    }
    for (final String extension : extensions) {
      addExtension(extension);
    }
  }

  /**
   * Get the flag indicating if the filter can be modified.
   *
   * @return The flag indicating if the filter can be modified.
   */
  protected final boolean isReadOnly() {
    return this.readOnly;
  }

  /**
   * Set the flag indicating if the filter can be modified. If the flag is read
   * only it cannot be changed to writable.
   *
   * @param readOnly The flag indicating if the filter can be modified.
   */
  protected final void setReadOnly(final boolean readOnly) {
    if (!readOnly && this.readOnly) {
      throw new IllegalArgumentException("This filname filter is readonly");
    }
    this.readOnly = readOnly;
  }

}
