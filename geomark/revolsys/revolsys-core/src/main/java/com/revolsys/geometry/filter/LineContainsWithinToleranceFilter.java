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

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;

public class LineContainsWithinToleranceFilter implements Predicate<LineString> {
  private final BoundingBox envelope;

  private boolean flip = false;

  private final LineString points;

  private double tolerance;

  public LineContainsWithinToleranceFilter(final LineString line) {
    this.points = line;
    this.envelope = line.getBoundingBox();
  }

  public LineContainsWithinToleranceFilter(final LineString line, final double tolerance) {
    this.points = line;
    this.tolerance = tolerance;
    this.envelope = line.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(tolerance);
  }

  public LineContainsWithinToleranceFilter(final LineString line, final double tolerance,
    final boolean flip) {
    this(line, tolerance);
    this.flip = flip;
  }

  @Override
  public boolean test(final LineString line) {
    if (this.envelope.bboxIntersects(line.getBoundingBox())) {
      final LineString points = line;

      final boolean contains;
      if (this.flip) {
        contains = CoordinatesListUtil.containsWithinTolerance(points, this.points, this.tolerance);
      } else {
        contains = CoordinatesListUtil.containsWithinTolerance(this.points, points, this.tolerance);
      }
      if (contains) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

}
