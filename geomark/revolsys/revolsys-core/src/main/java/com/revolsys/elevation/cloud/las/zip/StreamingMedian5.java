/*
 * Copyright 2007-2012, martin isenburg, rapidlasso - fast tools to catch reality
 *
 * This is free software; you can redistribute and/or modify it under the
 * terms of the GNU Lesser General Licence as published by the Free Software
 * Foundation. See the LICENSE.txt file for more information.
 *
 * This software is distributed WITHOUT ANY WARRANTY and without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.revolsys.elevation.cloud.las.zip;

public class StreamingMedian5 {

  public static StreamingMedian5[] newStreamingMedian5(final int num) {
    final StreamingMedian5[] medians = new StreamingMedian5[num];
    for (int i = 0; i < num; i++) {
      medians[i] = new StreamingMedian5();
    }
    return medians;
  }

  public int[] values = new int[5];

  public boolean high;

  StreamingMedian5() {
    init();
  }

  public void add(final int v) {
    if (this.high) {
      if (v < this.values[2]) {
        this.values[4] = this.values[3];
        this.values[3] = this.values[2];
        if (v < this.values[0]) {
          this.values[2] = this.values[1];
          this.values[1] = this.values[0];
          this.values[0] = v;
        } else if (v < this.values[1]) {
          this.values[2] = this.values[1];
          this.values[1] = v;
        } else {
          this.values[2] = v;
        }
      } else {
        if (v < this.values[3]) {
          this.values[4] = this.values[3];
          this.values[3] = v;
        } else {
          this.values[4] = v;
        }
        this.high = false;
      }
    } else {
      if (this.values[2] < v) {
        this.values[0] = this.values[1];
        this.values[1] = this.values[2];
        if (this.values[4] < v) {
          this.values[2] = this.values[3];
          this.values[3] = this.values[4];
          this.values[4] = v;
        } else if (this.values[3] < v) {
          this.values[2] = this.values[3];
          this.values[3] = v;
        } else {
          this.values[2] = v;
        }
      } else {
        if (this.values[1] < v) {
          this.values[0] = this.values[1];
          this.values[1] = v;
        } else {
          this.values[0] = v;
        }
        this.high = true;
      }
    }
  }

  public int get() {
    return this.values[2];
  }

  public void init() {
    this.values[0] = this.values[1] = this.values[2] = this.values[3] = this.values[4] = 0;
    this.high = true;
  }
}
