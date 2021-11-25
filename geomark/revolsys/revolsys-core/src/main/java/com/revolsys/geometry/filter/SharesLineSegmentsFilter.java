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

public class SharesLineSegmentsFilter implements Predicate<LineString> {
  public SharesLineSegmentsFilter(final LineString line) {
  }

  @Override
  public boolean test(final LineString line) {

    final int vertexCount = line.getVertexCount();
    if (vertexCount > 0) {
      double line1x1 = line.getX(0);
      double line1y1 = line.getY(0);
      for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
        final double line1x2 = line.getX(vertexIndex);
        final double line1y2 = line.getY(vertexIndex);

        final int vertexCount2 = line.getVertexCount();
        if (vertexCount2 > 0) {
          double line2x1 = line.getX(0);
          double line2y1 = line.getY(0);
          for (final int vertexIndex2 = 1; vertexIndex < vertexCount2; vertexIndex++) {
            final double line2x2 = line.getX(vertexIndex2);
            final double line2y2 = line.getY(vertexIndex2);
            if (line1x1 == line2x1 && line1y1 == line2y1) {
              if (line1x2 == line2x2 && line1y2 == line2y2) {
                return true;
              }
            }
            line2x1 = line2x2;
            line2y1 = line2y2;

          }
        }
        line1x1 = line1x2;
        line1y1 = line1y2;
      }
    }
    return false;
  }
}
