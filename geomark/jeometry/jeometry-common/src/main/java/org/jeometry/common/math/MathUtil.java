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
package org.jeometry.common.math;

/**
 * The MathUtil class is a utility class for handling integer, percent and
 * currency BigDecimal values.
 *
 * @author Paul Austin
 */
public interface MathUtil {

  static Double toDouble(final Object value) {
    if (value == null) {
      throw new NumberFormatException("Numbers cannot be empty");
    } else {
      final String string = value.toString();
      if ("NaN".equalsIgnoreCase(string)) {
        return Double.NaN;
      } else if ("-Infinity".equalsIgnoreCase(string)) {
        return Double.NEGATIVE_INFINITY;
      } else if ("Infinity".equalsIgnoreCase(string)) {
        return Double.POSITIVE_INFINITY;
      } else {
        return Double.valueOf(string);
      }
    }
  }
}
