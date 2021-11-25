package com.revolsys.gis.grid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MtoRectangularMapGrid extends Nts50000RectangularMapGrid {
  private static final Pattern NAME_PATTERN = Pattern.compile("^" + MtoConstants.REGEX_MTO + ".*");

  public MtoRectangularMapGrid() {
    this(MtoConstants.WIDTH_QUARTER, MtoConstants.HEIGHT_QUARTER);
  }

  protected MtoRectangularMapGrid(final double width, final double height) {
    super(width, height);
    setName("MTO");
  }

  @Override
  public double getLatitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String blockName = matcher.group(1);
      final String letter = matcher.group(2);
      final String number50k = matcher.group(3);
      final String letter12 = matcher.group(4);
      final String number100 = matcher.group(5);
      final String letter4 = matcher.group(6);

      double latitude = getLatitude(blockName, letter, number50k);
      latitude += GridUtil.getLetter16Row(letter12.charAt(0)) * MtoConstants.HEIGHT_TWELTH;
      latitude += GridUtil.getNumberRow100(number100) * MtoConstants.HEIGHT_HUNDRETH;
      latitude += GridUtil.getLetter4Row(letter4.charAt(0)) * MtoConstants.HEIGHT_QUARTER;

      return latitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid MTO tile name");
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
      final String letter12 = matcher.group(4);
      final String number100 = matcher.group(5);
      final String letter4 = matcher.group(6);

      double longitude = getLongitude(blockName, letter, number50k);
      longitude -= GridUtil.getLetter16Col(letter12.charAt(0)) * MtoConstants.WIDTH_TWELTH;
      longitude -= GridUtil.getNumberCol100(number100) * MtoConstants.WIDTH_HUNDRETH;
      longitude -= GridUtil.getLetter4Col(letter4.charAt(0)) * MtoConstants.WIDTH_QUARTER;

      return longitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid MTO tile name");
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
    final StringBuilder tileName = new StringBuilder(super.getMapTileName(x, y));
    for (int index = tileName.indexOf("/"); index != -1; index = tileName.indexOf("/")) {
      tileName.delete(index, index + 1);
    }

    if (tileName.charAt(0) != '1') {
      tileName.insert(0, '0');
    }
    if (tileName.length() == 5) {
      tileName.insert(4, "0");
    }

    tileName
      .append(GridUtil.getLetter12(x, y, MtoConstants.WIDTH_TWELTH, MtoConstants.HEIGHT_TWELTH));
    tileName.append(
      GridUtil.getNumber100(x, y, MtoConstants.WIDTH_HUNDRETH, MtoConstants.HEIGHT_HUNDRETH));
    tileName
      .append(GridUtil.getLetter4(x, y, MtoConstants.WIDTH_QUARTER, MtoConstants.HEIGHT_QUARTER));

    return tileName.toString();

  }
}
