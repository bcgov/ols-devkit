package com.revolsys.gis.grid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bcgs2000RectangularMapGrid extends Bcgs20000RectangularMapGrid {
  private static final Pattern NAME_PATTERN = Pattern
    .compile("^" + BcgsConstants.REGEX_2000 + ".*");

  public Bcgs2000RectangularMapGrid() {
    this(BcgsConstants.WIDTH_2000, BcgsConstants.HEIGHT_2000);
  }

  protected Bcgs2000RectangularMapGrid(final double width, final double height) {
    super(width, height);
    setName("BCGS 1:2 000");
  }

  @Override
  public double getLatitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      final String number20k = matcher.group(3);
      final String number2k = matcher.group(4);
      final double latitude = getLatitude(blockName, letter, number20k, number2k);
      return latitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid BCGS 1:2,000 tile name");
    }
  }

  protected double getLatitude(final String blockName, final String letter, final String number20k,
    final String number2k) {
    double latitude = getLatitude(blockName, letter, number20k);

    final int number = Integer.parseInt(number2k);
    final int numberRow = GridUtil.getNumberRow100(number);
    latitude += numberRow * BcgsConstants.HEIGHT_2000;
    return latitude;
  }

  @Override
  public double getLongitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      final String numberString = matcher.group(3);
      final String number2k = matcher.group(4);
      final double longitude = getLongitude(blockName, letter, numberString, number2k);
      return longitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid BCGS 1:2,000 tile name");
    }
  }

  protected double getLongitude(final String blockName, final String letter, final String number20k,
    final String number2k) {
    double longitude = getLongitude(blockName, letter, number20k);
    final int number = Integer.parseInt(number2k);
    final int numberCol = GridUtil.getNumberCol100(number);
    longitude -= numberCol * BcgsConstants.WIDTH_2000;
    return longitude;
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final String parentTileName = super.getMapTileName(x, y);

    final double xSheet = (x + 180) * 50;
    final int col = (int)((Math.ceil(xSheet - 0.00000000001) - 1) % 10);

    final double ySheet = (y + 90) * 100;
    final int row = (int)Math.floor(ySheet + 0.00000000001) % 10;

    return parentTileName + "." + GridUtil.getNumber100(row, col);
  }
}
