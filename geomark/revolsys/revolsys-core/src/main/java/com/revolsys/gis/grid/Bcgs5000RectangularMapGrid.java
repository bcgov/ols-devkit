package com.revolsys.gis.grid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bcgs5000RectangularMapGrid extends Bcgs10000RectangularMapGrid {
  private static final Pattern NAME_PATTERN = Pattern
    .compile("^" + BcgsConstants.REGEX_5000 + ".*");

  public Bcgs5000RectangularMapGrid() {
    this(BcgsConstants.WIDTH_5000, BcgsConstants.HEIGHT_5000);
  }

  protected Bcgs5000RectangularMapGrid(final double width, final double height) {
    super(width, height);
    setName("BCGS 1:5 000");
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
      final double latitude = getLatitude(blockName, letter, number20k, number10k, number5k);
      return latitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid BCGS 1:5,000 tile name");
    }
  }

  protected double getLatitude(final String blockName, final String letter, final String number20k,
    final String number10k, final String number5k) {
    double latitude = getLatitude(blockName, letter, number20k, number10k);
    final int numberRow = GridUtil.getNumberRow4(number5k);
    latitude += numberRow * BcgsConstants.HEIGHT_5000;
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
      final double longitude = getLongitude(blockName, letter, number20k, number10k, number5k);
      return longitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid BCGS 1:5,000 tile name");
    }
  }

  protected double getLongitude(final String blockName, final String letter, final String number20k,
    final String number10k, final String number5k) {
    double longitude = getLongitude(blockName, letter, number20k, number10k);
    final int numberCol = GridUtil.getNumberCol4(number5k);
    longitude -= numberCol * BcgsConstants.WIDTH_5000;
    return longitude;
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final String letterBlock = super.getMapTileName(x, y);

    final double xSheet = (x + 180) * 20;
    final int col = (int)(Math.ceil(xSheet - 0.000000000001) % 2);

    final double ySheet = (y + 90) * 40;
    final int row = (int)Math.floor(ySheet + 0.000000000001) % 2;

    return letterBlock + "." + GridUtil.getNumber4(row, col);
  }
}
