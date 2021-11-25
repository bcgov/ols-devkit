package com.revolsys.gis.grid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Nts50000RectangularMapGrid extends Nts250000RectangularMapGrid {
  private static final Pattern NAME_PATTERN = Pattern
    .compile("^" + NtsConstants.REGEX_50000 + ".*");

  public Nts50000RectangularMapGrid() {
    this(NtsConstants.WIDTH_50000, NtsConstants.HEIGHT_50000);
  }

  protected Nts50000RectangularMapGrid(final double width, final double height) {
    super(width, height);
    setName("NTS 1:50 000");
  }

  public String getCellNumber(final String tileName) {
    final int index = tileName.indexOf('/');
    final String cellNumber = tileName.substring(index + 1);
    if (cellNumber.length() == 1) {
      return "0" + cellNumber;
    } else {
      return cellNumber;
    }
  }

  @Override
  public double getLatitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      final String number50k = matcher.group(3);

      final double latitude = getLatitude(blockName, letter, number50k);

      return latitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid NTS 1:50,000 tile name");
    }
  }

  private double getLatitude(final String blockName, final String letter, final String number50k) {
    double latitude = getLatitude(blockName, letter);

    final int number = Integer.parseInt(number50k);
    final int numberRow = GridUtil.getNumberRow16(number);
    latitude += numberRow * NtsConstants.HEIGHT_50000;

    return latitude;
  }

  @Override
  public double getLongitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      final String number50k = matcher.group(3);

      final double longitude = getLongitude(blockName, letter, number50k);

      return longitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid NTS 1:50,000 tile name");
    }
  }

  private double getLongitude(final String blockName, final String letter, final String number50k) {
    double longitude = getLongitude(blockName, letter);

    final int number = Integer.parseInt(number50k);
    final int numberCol = GridUtil.getNumberCol16(number);
    longitude -= numberCol * NtsConstants.WIDTH_50000;

    return longitude;
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final String letterBlock = super.getMapTileName(x, y);
    final double yInSheet = Math.abs(92 + y) % NtsConstants.HEIGHT_250000;
    final int row = (int)Math.floor(yInSheet / NtsConstants.HEIGHT_50000) % 4;

    final double xInSheet = NtsConstants.WIDTH_250000
      - Math.abs(184 + x) % NtsConstants.WIDTH_250000;
    final int col = (int)Math.floor(xInSheet / NtsConstants.WIDTH_50000) % 4;
    return letterBlock + "/" + GridUtil.getNumber16(row, col);

  }
}
