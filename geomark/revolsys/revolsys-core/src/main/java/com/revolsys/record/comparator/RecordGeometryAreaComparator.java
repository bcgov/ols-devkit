package com.revolsys.record.comparator;

import java.util.Comparator;

import com.revolsys.geometry.model.Polygon;
import com.revolsys.record.Record;

public class RecordGeometryAreaComparator implements Comparator<Record> {

  private boolean clockwise = false;

  private boolean decending = false;

  public RecordGeometryAreaComparator() {
  }

  public RecordGeometryAreaComparator(final boolean decending, final boolean clockwise) {
    this.decending = decending;
    this.clockwise = clockwise;
  }

  @Override
  public int compare(final Record object1, final Record object2) {
    if (object1 == object2) {
      return 0;
    }
    int compare = -1;
    final Polygon geometry1 = object1.getGeometry();
    final Polygon geometry2 = object2.getGeometry();
    final double area1 = geometry1.getArea();
    final double area2 = geometry2.getArea();
    compare = Double.compare(area1, area2);
    if (compare == 0) {
      compare = geometry1.compareTo(geometry2);
      if (compare == 0) {
        final boolean clockwise1 = geometry1.getShell().isClockwise();
        final boolean clockwise2 = geometry2.getShell().isClockwise();
        if (clockwise1) {
          if (clockwise2) {
            return 0;
          } else {
            if (this.clockwise) {
              compare = -1;
            } else {
              compare = 1;
            }
          }
        } else {
          if (clockwise2) {
            if (this.clockwise) {
              compare = 1;
            } else {
              compare = -1;
            }
          } else {
            return 0;
          }
        }
      }
    }
    if (this.decending) {
      return -compare;
    } else {
      return compare;
    }
  }

}
