package com.revolsys.gis.grid;

public interface BcgsConstants {
  double HEIGHT_1000 = 0.005;

  double HEIGHT_10000 = 0.05;

  double HEIGHT_1250 = 0.00625;

  double HEIGHT_2000 = 0.01;

  double HEIGHT_20000 = 0.1;

  double HEIGHT_2500 = 0.0125;

  double HEIGHT_500 = 0.0025;

  double HEIGHT_5000 = 0.025;

  String REGEX_20000 = NtsConstants.REGEX_250000 + "\\.?" + "(100|0[1-9][0-9]|00[1-9])";

  String REGEX_10000 = REGEX_20000 + "\\.?" + "([1-4])";

  String REGEX_5000 = REGEX_10000 + "\\.?" + "([1-4])";

  String REGEX_2500 = REGEX_5000 + "\\.?" + "([1-4])";

  String REGEX_2000 = REGEX_20000 + "\\.?" + "(100|0[1-9][0-9]|00[1-9])";

  String REGEX_1250 = REGEX_2500 + "\\.?" + "([1-4])";

  String REGEX_1000 = REGEX_2000 + "\\.?" + "([1-4])";

  String REGEX_500 = REGEX_1000 + "\\.?" + "([1-4])";

  String REGEX_EIGHTH_LETTER = "([a-hA-H])";

  public static final String REGEX_HUNDRETH_NUMBER = "(100|0[1-9][0-9]|00[1-9])";

  String REGEX_QUARTER = "([nsNS])\\.?([ewEW])\\.?";

  String REGEX_QUARTER_NUMBER = "([1-4])";

  String REGEX_SIXTEENTH_LETTER = "([a-pA-P])";

  double WIDTH_1000 = 0.01;

  double WIDTH_10000 = 0.1;

  double WIDTH_1250 = 0.0125;

  double WIDTH_2000 = 0.02;

  double WIDTH_20000 = 0.2;

  double WIDTH_2500 = 0.025;

  double WIDTH_500 = 0.005;

  double WIDTH_5000 = 0.05;

}
