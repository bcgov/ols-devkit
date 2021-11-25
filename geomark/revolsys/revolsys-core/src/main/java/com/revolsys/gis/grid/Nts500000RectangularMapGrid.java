package com.revolsys.gis.grid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Nts500000RectangularMapGrid extends Nts1000000RectangularMapGrid {
  private static final Pattern NAME_PATTERN = Pattern
    .compile("^" + NtsConstants.REGEX_500000 + ".*");

  public Nts500000RectangularMapGrid() {
    super(NtsConstants.WIDTH_500000, NtsConstants.HEIGHT_500000);
    setName("NTS 1:500 000");
  }

  @Override
  public double getLatitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String name = matcher.group(1);
      final int block = Integer.parseInt(name);
      double latitude = getLatitude(block);
      final String northSouth = matcher.group(2);
      if (northSouth.equalsIgnoreCase("N")) {
        latitude += NtsConstants.HEIGHT_500000;
      }
      return latitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid NTS 1:500,000 tile name");
    }
  }

  @Override
  public double getLongitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String name = matcher.group(1);
      final int block = Integer.parseInt(name);
      double longitude = getLongitude(block);
      final String eastWest = matcher.group(3);
      if (eastWest.equalsIgnoreCase("W")) {
        longitude -= NtsConstants.WIDTH_500000;
      }
      return longitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid NTS 1:500,000 tile name");
    }
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final String block = super.getMapTileName(x, y);
    String northSouth;
    final double yInSheet = Math.abs(92 + y) % NtsConstants.HEIGHT_1000000;
    if (yInSheet < NtsConstants.HEIGHT_500000) {
      northSouth = "S";
    } else {
      northSouth = "N";
    }

    String eastWest;
    final double xInSheet = Math.abs(184 + x) % NtsConstants.WIDTH_1000000;
    if (xInSheet < NtsConstants.WIDTH_500000) {
      eastWest = "E";
    } else {
      eastWest = "W";
    }
    return block + northSouth + "." + eastWest + ".";
  }
}
