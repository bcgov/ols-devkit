/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

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
package com.revolsys.record.io.format.saif.util;

import java.io.File;

import com.revolsys.io.FileUtil;

public final class ObjectSetUtil {
  public static final String[] OBJECT_SUBSET_NUMBERS = {
    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i",
    "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
  };

  public static String getObjectSubsetName(final String prefix, final int index) {
    return prefix + getObjectSubsetNumber(index) + ".osn";
  }

  public static String getObjectSubsetNumber(final int index) {
    final int highByte = index / 36;
    final int lowByte = index % 36;
    return OBJECT_SUBSET_NUMBERS[highByte] + OBJECT_SUBSET_NUMBERS[lowByte];
  }

  public static String getObjectSubsetPrefix(final File file) {
    final String fileName = FileUtil.getFileName(file);
    return getObjectSubsetPrefix(fileName);
  }

  public static String getObjectSubsetPrefix(final String fileName) {
    return fileName.replaceAll("(\\d\\d)?.osn$", "");
  }

  private ObjectSetUtil() {
  }
}
