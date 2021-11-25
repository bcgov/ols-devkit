package com.revolsys.gis.grid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bcgs500RectangularMapGrid extends Bcgs1000RectangularMapGrid {
  private static final Pattern NAME_PATTERN = Pattern.compile("^" + BcgsConstants.REGEX_500 + ".*");

  public Bcgs500RectangularMapGrid() {
    this(BcgsConstants.WIDTH_500, BcgsConstants.HEIGHT_500);
  }

  protected Bcgs500RectangularMapGrid(final double width, final double height) {
    super(width, height);
    setName("BCGS 1:500");
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
      final String number500 = matcher.group(6);
      final double latitude = getLatitude(blockName, letter, number20k, number2000, number1000,
        number500);
      return latitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid BCGS 1:500 tile name");
    }
  }

  protected double getLatitude(final String blockName, final String letter, final String number20k,
    final String number2000, final String number1000, final String number500) {
    double latitude = getLatitude(blockName, letter, number20k, number2000, number1000);
    final int numberRow = GridUtil.getNumberRow4(number500);
    latitude += numberRow * BcgsConstants.HEIGHT_500;
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
      final String number500 = matcher.group(6);
      final double longitude = getLongitude(blockName, letter, number20k, number2000, number1000,
        number500);
      return longitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid BCGS 1:500 tile name");
    }
  }

  protected double getLongitude(final String blockName, final String letter, final String number20k,
    final String number2000, final String number1000, final String number500) {
    double longitude = getLongitude(blockName, letter, number20k, number2000, number1000);
    final int numberCol = GridUtil.getNumberCol4(number500);
    longitude -= numberCol * BcgsConstants.WIDTH_500;
    return longitude;
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final String letterBlock = super.getMapTileName(x, y);

    final double xSheet = (x + 180) * 200;
    final int col = (int)(Math.ceil(xSheet - 0.00000000001) % 2);

    final double ySheet = (y + 90) * 400;
    final int row = (int)Math.floor(ySheet + 0.00000000001) % 2;

    return letterBlock + "." + GridUtil.getNumber4(row, col);
  }
}
