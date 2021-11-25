/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.util;

/**
 * Implements a timer function which can compute
 * elapsed time as well as split times.
 *
 * @version 1.7
 */
public class Stopwatch {

  public static String getTimeString(final long timeMillis) {
    final String totalTimeStr = timeMillis < 10000 ? timeMillis + " ms"
      : timeMillis / 1000.0 + " s";
    return totalTimeStr;
  }

  private boolean isRunning = false;

  private long startTimestamp;

  private long totalTime = 0;

  public Stopwatch() {
    start();
  }

  public long getTime() {
    updateTotalTime();
    return this.totalTime;
  }

  public String getTimeString() {
    final long totalTime = getTime();
    return getTimeString(totalTime);
  }

  public void reset() {
    this.totalTime = 0;
    this.startTimestamp = System.currentTimeMillis();
  }

  public long split() {
    if (this.isRunning) {
      updateTotalTime();
    }
    return this.totalTime;
  }

  public void start() {
    if (this.isRunning) {
      return;
    }
    this.startTimestamp = System.currentTimeMillis();
    this.isRunning = true;
  }

  public long stop() {
    if (this.isRunning) {
      updateTotalTime();
      this.isRunning = false;
    }
    return this.totalTime;
  }

  private void updateTotalTime() {
    final long endTimestamp = System.currentTimeMillis();
    final long elapsedTime = endTimestamp - this.startTimestamp;
    this.startTimestamp = endTimestamp;
    this.totalTime += elapsedTime;
  }
}
