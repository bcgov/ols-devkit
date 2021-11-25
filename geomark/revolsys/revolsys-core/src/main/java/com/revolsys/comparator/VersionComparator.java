package com.revolsys.comparator;

import java.util.Comparator;

import org.jeometry.common.number.Doubles;

public class VersionComparator implements Comparator<String> {

  @Override
  public int compare(final String version1, final String version2) {
    final double[] parts1 = Doubles.toDoubleArraySplit(version1, "\\.");
    final double[] parts2 = Doubles.toDoubleArraySplit(version2, "\\.");
    for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
      double v1 = 0;
      if (i < parts1.length) {
        v1 = parts1[i];
      }
      double v2 = 0;
      if (i < parts2.length) {
        v2 = parts2[i];
      }
      final int partCompare = Double.compare(v1, v2);
      if (partCompare != 0) {
        return partCompare;
      }
    }
    return 0;
  }

}
