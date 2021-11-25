package com.revolsys.gis.grid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bcgs20000RectangularMapGrid extends Nts250000RectangularMapGrid {
  private static final Pattern FIND_NAME_PATTERN = Pattern
    .compile(".*(" + BcgsConstants.REGEX_20000 + ").*");

  public static final Bcgs20000RectangularMapGrid INSTANCE = new Bcgs20000RectangularMapGrid();

  private static final Pattern NAME_PATTERN = Pattern
    .compile("^" + BcgsConstants.REGEX_20000 + ".*");

  public static String getTileName(final String mapTileName) {
    final Matcher matcher = FIND_NAME_PATTERN.matcher(mapTileName);
    if (matcher.find()) {
      return matcher.group(0).replaceAll("\\.", "").toLowerCase();
    } else {
      return mapTileName;
    }
  }

  public Bcgs20000RectangularMapGrid() {
    this(BcgsConstants.WIDTH_20000, BcgsConstants.HEIGHT_20000);
  }

  protected Bcgs20000RectangularMapGrid(final double width, final double height) {
    super(width, height);
    setPrecisionScale(10);
    setName("BCGS 1:20 000");
  }

  @Override
  public String getFormattedMapTileName(final String name) {
    String tileName = getTileName(name);
    final int length = tileName.length();
    tileName = tileName.substring(0, length - 3).toUpperCase() + "."
      + tileName.substring(length - 3);
    return tileName;
  }

  @Override
  public double getLatitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      final String numberString = matcher.group(3);
      final double latitude = getLatitude(blockName, letter, numberString);
      return latitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid BCGS 1:20,000 tile name");
    }
  }

  protected double getLatitude(final String blockName, final String letter,
    final String numberString) {
    double latitude = getLatitude(blockName, letter);

    final int number = Integer.parseInt(numberString);
    final int numberRow = GridUtil.getNumberRow100(number);
    latitude += numberRow * BcgsConstants.HEIGHT_20000;
    return latitude;
  }

  @Override
  public double getLongitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      final String numberString = matcher.group(3);
      final double longitude = getLongitude(blockName, letter, numberString);
      return longitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid BCGS 1:20,000 tile name");
    }
  }

  protected double getLongitude(final String blockName, final String letter,
    final String numberString) {
    double longitude = getLongitude(blockName, letter);
    final int number = Integer.parseInt(numberString);
    final int numberCol = GridUtil.getNumberCol100(number);
    longitude -= numberCol * BcgsConstants.WIDTH_20000;
    return longitude;
  }

  public String getMapsheetWithPath(final String mapsheet) {
    final String mapTileName = getTileName(mapsheet);
    final int block = getBlock(mapTileName);
    final char letter = getLetter(mapTileName);
    final StringBuilder path = new StringBuilder();
    path.append(block);
    path.append('/');
    path.append(letter);
    path.append('/');
    path.append(mapTileName);
    return path.toString();
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final String letterBlock = super.getMapTileName(x, y);

    final double xSheet = (x + 180) * 5;
    final int col = (int)((Math.ceil(xSheet - 0.000000000001) - 1) % 10);

    final double ySheet = (y + 90) * 10;
    final int row = (int)Math.floor(ySheet + 0.000000000001) % 10;

    return letterBlock + GridUtil.getNumber100(row, col);
  }
}
