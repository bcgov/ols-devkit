package com.revolsys.csformat.gridshift.nadcon5;

import org.jeometry.coordinatesystem.operation.gridshift.HorizontalShiftOperation;

public interface Nadcon5 {

  static String NAD27 = "NAD27";

  static String NAD83_CURRENT = "NAD83(2011)";

  static HorizontalShiftOperation NAD_27_TO_83 = newGridShiftOperation(NAD27, NAD83_CURRENT);

  static HorizontalShiftOperation NAD_83_TO_27 = newGridShiftOperation(NAD83_CURRENT, NAD27);

  static HorizontalShiftOperation newGridShiftOperation(final String sourceDatumName,
    final String targetDatumName) {
    return new Nadcon5GridShiftOperation(sourceDatumName, targetDatumName);
  }
}
