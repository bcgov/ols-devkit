/*
 * $URL:$
 * $Author:$
 * $Date:$
 * $Revision:$

 * Copyright 2004-2007 Revolution Systems Inc.
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
package com.revolsys.geometry.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.model.LineString;

public class LineStartsSharesStartOrEndFilter implements Predicate<LineString> {
  private final LineString line;

  public LineStartsSharesStartOrEndFilter(final LineString line) {
    this.line = line;
  }

  private boolean endsWith(final LineString line) {
    final int vertexCount1 = this.line.getVertexCount();
    final int vertexCount2 = line.getVertexCount();
    if (vertexCount1 < vertexCount2) {
      return false;
    } else {
      for (int vertexIndex = 0; vertexIndex < vertexCount2; vertexIndex++) {
        if (!this.line.equalsVertex(2, vertexCount1 - 1 - vertexIndex, line,
          vertexCount2 - 1 - vertexIndex)) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean startsWith(final LineString line) {
    final int vertexCount1 = this.line.getVertexCount();
    final int vertexCount2 = line.getVertexCount();
    if (vertexCount1 < vertexCount2) {
      return false;
    } else {
      for (int vertexIndex = 0; vertexIndex < vertexCount2; vertexIndex++) {
        if (!this.line.equalsVertex(2, vertexIndex, line, vertexIndex)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean test(final LineString line) {
    if (startsWith(line)) {
      return true;
    } else if (endsWith(line)) {
      return true;
    } else {
      return false;
    }
  }
}
