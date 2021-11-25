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
import java.io.IOException;

/**
 * The DirectoryFilenameFilter is a {@link FilenameFilter} that only returns
 * files if they are a directory.
 *
 * @author Paul Austin
 */
public class DirectoryFilenameFilter implements FilenameFilter {
  /** An instance of the filter for use by applications. */
  public static final DirectoryFilenameFilter FILTER = new DirectoryFilenameFilter();

  /**
   * Check to see if the file should be included in the list of matched files
   *
   * @param directory The directory in which the file was found.
   * @param filename The name of the file.
   * @return True if the file matched, false otherwise.
   */
  @Override
  public boolean accept(final File directory, final String filename) {
    try {
      return new File(directory, filename).getCanonicalFile().isDirectory();
    } catch (final IOException e) {
      e.printStackTrace();
      return false;
    }
  }
}
