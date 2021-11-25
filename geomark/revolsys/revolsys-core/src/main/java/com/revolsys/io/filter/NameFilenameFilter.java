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
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * The NameFilenameFilter is a {@link FilenameFilter} that only returns files if
 * the have one of the specified file names. More than one file name can be
 * specified by using the {@link #addName(String)} method.
 *
 * @author Paul Austin
 */
public class NameFilenameFilter implements FilenameFilter {
  /** The list of file names to match. */
  private final Set names = new HashSet();

  /** Flag indicating if the filter can be modified. */
  private boolean readOnly = false;

  /**
   * Construct a new NameFilenameFilter with no file names.
   */
  public NameFilenameFilter() {
  }

  /**
   * Construct a new NameFilenameFilter with the file names.
   *
   * @param filenames The file names.
   */
  public NameFilenameFilter(final Collection filenames) {
    addNames(filenames);
  }

  /**
   * Construct a new NameFilenameFilter with the single file name.
   *
   * @param filenames The file names.
   * @param readOnly Flag indicating if the filter can be modified.
   */
  public NameFilenameFilter(final Collection filenames, final boolean readOnly) {
    addNames(filenames);
    this.readOnly = readOnly;
  }

  /**
   * Construct a new NameFilenameFilter with the single file name.
   *
   * @param filename The file name.
   */
  public NameFilenameFilter(final String filename) {
    addName(filename);
  }

  /**
   * Construct a new NameFilenameFilter with the single file name.
   *
   * @param filename The file name.
   * @param readOnly Flag indicating if the filter can be modified.
   */
  public NameFilenameFilter(final String filename, final boolean readOnly) {
    addName(filename);
    this.readOnly = readOnly;
  }

  /**
   * Check to see if the file should be included in the list of matched files
   *
   * @param directory The directory in which the file was found.
   * @param filename The name of the file.
   * @return True if the file matched, false otherwise.
   */
  @Override
  public boolean accept(final File directory, final String filename) {
    return this.names.contains(filename);
  }

  /**
   * Add the file name to the names to find.
   *
   * @param name The file name.
   */
  public void addName(final String name) {
    if (this.readOnly) {
      throw new IllegalArgumentException("This filname filter is readonly");
    }
    this.names.add(name);
  }

  /**
   * Add the file names to the names to find.
   *
   * @param names The file names.
   */
  public void addNames(final Collection names) {
    if (this.readOnly) {
      throw new IllegalArgumentException("This filname filter is readonly");
    }
    for (final Object name2 : names) {
      final String name = (String)name2;
      addName(name);
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
