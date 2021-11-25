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
import com.revolsys.record.Record;
import com.revolsys.record.filter.RecordGeometryFilter;

public class LineStringLessThanDistanceFilter implements Predicate<LineString> {

  public static Predicate<Record> getFilter(final Record object, final double maxDistance) {
    final LineString line = object.getGeometry();
    final LineStringLessThanDistanceFilter lineFilter = new LineStringLessThanDistanceFilter(line,
      maxDistance);
    return new RecordGeometryFilter<>(lineFilter);
  }

  /** The maximum distance the object can be from the source geometry. */
  private double distance;

  private BoundingBox envelope;

  /** The geometry to compare the data objects to to. */
  private LineString geometry;

  public LineStringLessThanDistanceFilter() {
  }

  /**
   * Construct a new LineStringLessThanDistanceFilter.
   *
   * @param geometry The geometry to compare the data objects to to.
   * @param distance
   */
  public LineStringLessThanDistanceFilter(final LineString geometry, final double distance) {
    this.distance = distance;
    setGeometry(geometry);
  }

  /**
   * Get the maximum distance the object can be from the source geometry.
   *
   * @return The maximum distance the object can be from the source geometry.
   */
  public double getDistance() {
    return this.distance;
  }

  public com.revolsys.geometry.model.BoundingBox getEnvelope() {
    return this.envelope;
  }

  /**
   * Get the geometry to compare the data objects to to.
   *
   * @return The geometry to compare the data objects to to.
   */
  public LineString getGeometry() {
    return this.geometry;
  }

  public void setDistance(final double distance) {
    this.distance = distance;
  }

  public void setGeometry(final LineString geometry) {
    this.geometry = geometry;
    this.envelope = geometry.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(this.distance);
  }

  @Override
  public boolean test(final LineString line) {
    if (line.getBoundingBox().bboxIntersects(this.envelope)) {
      return line.isLessThanDistance(this.geometry, this.distance);
    } else {
      return false;
    }

  }
}
