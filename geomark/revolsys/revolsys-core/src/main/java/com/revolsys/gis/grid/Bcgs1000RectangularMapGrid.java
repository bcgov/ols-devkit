package com.revolsys.gis.grid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bcgs1000RectangularMapGrid extends Bcgs2000RectangularMapGrid {
  private static final Pattern NAME_PATTERN = Pattern
    .compile("^" + BcgsConstants.REGEX_1000 + ".*");

  public Bcgs1000RectangularMapGrid() {
    this(BcgsConstants.WIDTH_1000, BcgsConstants.HEIGHT_1000);
  }

  protected Bcgs1000RectangularMapGrid(final double width, final double height) {
    super(width, height);
    setName("BCGS 1:1 000");
  }

  @Override
  public double getLatitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      final String number20k = matcher.group(3);
      final String number2000 = matcher.group(4);
      final String number1000 = matcher.group(5);
      final double latitude = getLatitude(blockName, letter, number20k, number2000, number1000);
      return latitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid BCGS 1:1,000 tile name");
    }
  }

  protected double getLatitude(final String blockName, final String letter, final String number20k,
    final String number2000, final String number1000) {
    double latitude = getLatitude(blockName, letter, number20k, number2000);
    final int numberRow = GridUtil.getNumberRow4(number1000);
    latitude += numberRow * BcgsConstants.HEIGHT_1000;
    return latitude;
  }

  @Override
  public double getLongitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      final String number20k = matcher.group(3);
      final String number2000 = matcher.group(4);
      final String number1000 = matcher.group(5);
      final double longitude = getLongitude(blockName, letter, number20k, number2000, number1000);
      return longitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid BCGS 1:1,000 tile name");
    }
  }

  protected double getLongitude(final String blockName, final String letter, final String number20k,
    final String number2000, final String number1000) {
    double longitude = getLongitude(blockName, letter, number20k, number2000);
    final int numberCol = GridUtil.getNumberCol4(number1000);
    longitude -= numberCol * BcgsConstants.WIDTH_1000;
    return longitude;
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final String letterBlock = super.getMapTileName(x, y);

    final double xSheet = (x + 180) * 100;
    final int col = (int)(Math.ceil(xSheet - 0.00000000001) % 2);

    final double ySheet = (y + 90) * 200;
    final int row = (int)Math.floor(ySheet + 0.00000000001) % 2;

    return letterBlock + "." + GridUtil.getNumber4(row, col);
  }
}
