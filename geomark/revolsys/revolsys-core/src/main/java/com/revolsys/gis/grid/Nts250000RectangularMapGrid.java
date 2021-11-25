package com.revolsys.gis.grid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Nts250000RectangularMapGrid extends Nts1000000RectangularMapGrid {
  private static final Pattern NAME_PATTERN = Pattern
    .compile("^" + NtsConstants.REGEX_250000 + ".*");

  public Nts250000RectangularMapGrid() {
    this(NtsConstants.WIDTH_250000, NtsConstants.HEIGHT_250000);
  }

  protected Nts250000RectangularMapGrid(final double width, final double height) {
    super(width, height);
    setName("NTS 1:250 000");
  }

  @Override
  public String getFormattedMapTileName(final String name) {
    return name.toUpperCase();
  }

  @Override
  public double getLatitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      return getLatitude(blockName, letter);
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid NTS 1:500,000 tile name");
    }
  }

  protected double getLatitude(final String blockName, final String letter) {
    final int block = Integer.parseInt(blockName);
    double latitude = getLatitude(block);
    final int letterRow = GridUtil.getLetter16Row(letter.charAt(0));

    latitude += letterRow * NtsConstants.HEIGHT_250000;
    return latitude;
  }

  public char getLetter(final String sheet) {
    final char firstChar = sheet.charAt(0);
    if (firstChar == '0' || firstChar == '1') {
      return sheet.charAt(3);
    } else {
      return sheet.charAt(2);
    }
  }

  @Override
  public double getLongitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      final double longitude = getLongitude(blockName, letter);
      return longitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid NTS 1:250,000 tile name");
    }
  }

  public double getLongitude(final String blockName, final String letter) {
    final int block = Integer.parseInt(blockName);
    double longitude = getLongitude(block);
    final int letterCol = GridUtil.getLetter16Col(letter.charAt(0));

    longitude -= letterCol * NtsConstants.WIDTH_250000;
    return longitude;
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final String block = super.getMapTileName(x, y);
    final double yInSheet = Math.abs(92 + y) % NtsConstants.HEIGHT_1000000;
    final int row = (int)Math.floor(yInSheet / NtsConstants.HEIGHT_250000);

    final double xInSheet = 8 - Math.abs(184 + x) % NtsConstants.WIDTH_1000000;
    final int col = (int)Math.floor(xInSheet / NtsConstants.WIDTH_250000) % 4;

    return block + GridUtil.getLetter16(row, col);
  }
}
