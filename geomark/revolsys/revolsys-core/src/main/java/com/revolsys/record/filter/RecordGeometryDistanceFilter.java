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
package com.revolsys.record.filter;

import java.util.Comparator;
import java.util.function.Predicate;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;

public class RecordGeometryDistanceFilter implements Predicate<Record>, Comparator<Record> {
  /** The geometry to compare the data objects to to. */
  private Geometry geometry;

  /** The maximum maxDistance the object can be from the source geometry. */
  private final double maxDistance;

  /**
   * Construct a new LineStringLessThanDistanceFilter.
   *
   * @param geometry The geometry to compare the data objects to to.
   * @param maxDistance
   */
  public RecordGeometryDistanceFilter(final Geometry geometry, final double maxDistance) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
  }

  @Override
  public int compare(final Record record1, final Record record2) {
    if (record1 == record2) {
      return 0;
    } else {
      final double distance1 = getDistance(record1);
      final double distance2 = getDistance(record2);
      int compare = Double.compare(distance1, distance2);
      if (compare == 0) {
        compare = record1.compareTo(record2);
      }
      return compare;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    this.geometry = null;
  }

  public double getDistance(final Record record) {
    final Geometry recordGeometry = record.getGeometry();
    final double distance = recordGeometry.distanceGeometry(this.geometry);
    return distance;
  }

  /**
   * Get the geometry to compare the data objects to to.
   *
   * @return The geometry to compare the data objects to to.
   */
  public Geometry getGeometry() {
    return this.geometry;
  }

  /**
   * Get the maximum maxDistance the object can be from the source geometry.
   *
   * @return The maximum maxDistance the object can be from the source geometry.
   */
  public double getMaxDistance() {
    return this.maxDistance;
  }

  @Override
  public boolean test(final Record record) {
    final Geometry recordGeometry = record.getGeometry();
    final double distance = recordGeometry.distanceGeometry(this.geometry, this.maxDistance);
    if (distance <= this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }

}
