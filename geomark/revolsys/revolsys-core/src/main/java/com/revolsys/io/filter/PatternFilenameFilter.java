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
import java.util.regex.Pattern;

/**
 * The PatternFileNameFilter is a {@link FileFilter} that only returns files if they
 * match the regular expression.
 *
 * @author Paul Austin
 */
public class PatternFilenameFilter implements FilenameFilter {
  private boolean ignoreCase;

  /** The regular expression pattern to match file names. */
  private final Pattern pattern;

  /**
   * Construct a new PatternFileNameFilter.
   *
   * @param regex The regular expression.
   */
  public PatternFilenameFilter(final String regex) {
    this.pattern = Pattern.compile(regex);
  }

  public PatternFilenameFilter(String regex, final boolean ignoreCase) {
    if (ignoreCase) {
      regex = regex.toLowerCase();
    }
    this.pattern = Pattern.compile(regex);
    this.ignoreCase = ignoreCase;
  }

  /**
   * Check to see if the file should be included in the list of matched files
   *
   * @param directory The file directory.
   * @param fileName The file name.
   * @return True if the file matched, false otherwise.
   */
  @Override
  public boolean accept(final File directory, String fileName) {
    if (this.ignoreCase) {
      fileName = fileName.toLowerCase();
    }
    final boolean matches = this.pattern.matcher(fileName).matches();
    return matches;
  }
}
