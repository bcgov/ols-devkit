package com.revolsys.gis.grid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Nts25000RectangularMapGrid extends Nts50000RectangularMapGrid {
  private static final Pattern NAME_PATTERN = Pattern
    .compile("^" + NtsConstants.REGEX_25000 + ".*");

  public Nts25000RectangularMapGrid() {
    super(NtsConstants.WIDTH_25000, NtsConstants.HEIGHT_25000);
    setName("NTS 1:25 000");
  }

  @Override
  public double getLatitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String name = matcher.group(1);
      final int block = Integer.parseInt(name);
      double latitude = getLatitude(block);

      final String letter250k = matcher.group(2);
      final int letter16Row = GridUtil.getLetter16Row(letter250k.charAt(0));
      latitude += letter16Row * NtsConstants.HEIGHT_250000;

      final int number = Integer.parseInt(matcher.group(3));
      final int numberRow = GridUtil.getNumberRow16(number);
      latitude += numberRow * NtsConstants.HEIGHT_50000;

      final String letter8 = matcher.group(4);
      final int letter8Row = GridUtil.getLetter8Row(letter8.charAt(0));
      latitude += letter8Row * NtsConstants.HEIGHT_25000;

      return latitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid NTS 1:125,000 tile name");
    }
  }

  @Override
  public double getLongitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String name = matcher.group(1);
      final int block = Integer.parseInt(name);
      double longitude = getLongitude(block);

      final String letter16 = matcher.group(2);
      final int letter16Col = GridUtil.getLetter16Col(letter16.charAt(0));
      longitude -= letter16Col * NtsConstants.WIDTH_250000;

      final int number = Integer.parseInt(matcher.group(3));
      final int numberCol = GridUtil.getNumberCol16(number);
      longitude -= numberCol * NtsConstants.WIDTH_50000;

      final String letter8 = matcher.group(4);
      final int letter8Col = GridUtil.getLetter8Col(letter8.charAt(0));
      longitude -= letter8Col * NtsConstants.WIDTH_25000;

      return longitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid NTS 1:125,000 tile name");
    }
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final String letterBlock = super.getMapTileName(x, y);
    final double yInSheet = Math.abs(92 + y) % NtsConstants.HEIGHT_50000;
    final int row = (int)Math.floor(yInSheet / NtsConstants.HEIGHT_25000);

    final double xInSheet = NtsConstants.WIDTH_50000 - Math.abs(184 + x) % NtsConstants.WIDTH_50000;
    final int col = (int)Math.floor(xInSheet / NtsConstants.WIDTH_25000) % 4;
    return letterBlock + GridUtil.getLetter8(row, col);

  }

}
