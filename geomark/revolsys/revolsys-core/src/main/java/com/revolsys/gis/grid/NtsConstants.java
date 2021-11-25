package com.revolsys.gis.grid;

public interface NtsConstants {
  double HEIGHT_1000000 = 4;

  double HEIGHT_125000 = 0.5;

  double HEIGHT_25000 = 0.125;

  double HEIGHT_250000 = 1;

  double HEIGHT_50000 = 0.25;

  double HEIGHT_500000 = 2;

  double MAX_LATITUDE = 40;

  double MAX_LONGITUDE = -48;

  String REGEX_1000000 = "([0-9]{2,3})";

  String REGEX_250000 = REGEX_1000000 + "([a-pA-P])";

  String REGEX_125000 = REGEX_250000 + "/?" + "([nsNS])\\.?([ewEW])\\.?";

  String REGEX_50000 = REGEX_250000 + "/?" + "(1[0-6]|0?[1-9])";

  String REGEX_25000 = REGEX_50000 + "([a-hA-H])";

  String REGEX_500000 = REGEX_1000000 + "([nsNS])\\.?([ewEW])\\.?";

  String REGEX_EIGHTH_LETTER = "([a-hA-H])";

  String REGEX_QUARTER = "([nsNS])\\.?([ewEW])\\.?";

  String REGEX_SIXTEENTH_LETTER = "([a-pA-P])";

  String REGEX_SIXTEENTH_NUMBER = "(1[0-6]|0?[1-9])";

  double WIDTH_1000000 = 8;

  double WIDTH_125000 = 1;

  double WIDTH_25000 = 0.125;

  double WIDTH_250000 = 2;

  double WIDTH_50000 = 0.5;

  double WIDTH_500000 = 4;
}
