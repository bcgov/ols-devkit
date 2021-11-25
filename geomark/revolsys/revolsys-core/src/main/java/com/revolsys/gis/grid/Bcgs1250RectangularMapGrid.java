package com.revolsys.gis.grid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bcgs1250RectangularMapGrid extends Bcgs2500RectangularMapGrid {
  private static final Pattern NAME_PATTERN = Pattern
    .compile("^" + BcgsConstants.REGEX_1250 + ".*");

  public Bcgs1250RectangularMapGrid() {
    this(BcgsConstants.WIDTH_1250, BcgsConstants.HEIGHT_1250);
  }

  protected Bcgs1250RectangularMapGrid(final double width, final double height) {
    super(width, height);
    setName("BCGS 1:1 250");
  }

  @Override
  public double getLatitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      final String number20k = matcher.group(3);
      final String number10k = matcher.group(4);
      final String number5k = matcher.group(5);
      final String number2500 = matcher.group(6);
      final String number1250 = matcher.group(7);
      final double latitude = getLatitude(blockName, letter, number20k, number10k, number5k,
        number2500, number1250);
      return latitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid BCGS 1:1,250 tile name");
    }
  }

  protected double getLatitude(final String blockName, final String letter, final String number20k,
    final String number10k, final String number5k, final String number2500,
    final String number1250) {
    double latitude = getLatitude(blockName, letter, number20k, number10k, number5k, number2500);
    final int numberRow = GridUtil.getNumberRow4(number1250);
    latitude += numberRow * BcgsConstants.HEIGHT_1250;
    return latitude;
  }

  @Override
  public double getLongitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      final String number20k = matcher.group(3);
      final String number10k = matcher.group(4);
      final String number5k = matcher.group(5);
      final String number2500 = matcher.group(6);
      final String number1250 = matcher.group(7);
      final double longitude = getLongitude(blockName, letter, number20k, number10k, number5k,
        number2500, number1250);
      return longitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid BCGS 1:1,250 tile name");
    }
  }

  protected double getLongitude(final String blockName, final String letter, final String number20k,
    final String number10k, final String number5k, final String number2500,
    final String number1250) {
    double longitude = getLongitude(blockName, letter, number20k, number10k, number5k, number2500);
    final int numberCol = GridUtil.getNumberCol4(number1250);
    longitude -= numberCol * BcgsConstants.WIDTH_1250;
    return longitude;
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final String letterBlock = super.getMapTileName(x, y);

    final double xSheet = (x + 180) * 80;
    final int col = (int)(Math.ceil(xSheet - 0.00000000001) % 2);

    final double ySheet = (y + 90) * 160;
    final int row = (int)Math.floor(ySheet + 0.00000000001) % 2;

    return letterBlock + "." + GridUtil.getNumber4(row, col);
  }
}
