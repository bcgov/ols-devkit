package com.revolsys.gis.grid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Nts125000RectangularMapGrid extends Nts250000RectangularMapGrid {
  private static final Pattern NAME_PATTERN = Pattern
    .compile("^" + NtsConstants.REGEX_125000 + ".*");

  public Nts125000RectangularMapGrid() {
    super(NtsConstants.WIDTH_125000, NtsConstants.HEIGHT_125000);
    setName("NTS 1:125 000");
  }

  @Override
  public String getFormattedMapTileName(final String name) {
    final int length = name.length();
    return (name.substring(0, length - 2) + "/" + name.substring(length - 2)).toUpperCase();
  }

  @Override
  public double getLatitude(final String mapTileName) {
    final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
    if (matcher.matches()) {
      final String name = matcher.group(1);
      final int block = Integer.parseInt(name);
      double latitude = getLatitude(block);

      final String letter = matcher.group(2);
      final int letterRow = GridUtil.getLetter16Row(letter.charAt(0));
      latitude += letterRow * NtsConstants.HEIGHT_250000;

      final String northSouth = matcher.group(3);
      if (northSouth.equalsIgnoreCase("N")) {
        latitude += NtsConstants.HEIGHT_125000;
      }

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

      final String letter = matcher.group(2);
      final int letterCol = GridUtil.getLetter16Col(letter.charAt(0));
      longitude -= letterCol * NtsConstants.WIDTH_250000;

      final String eastWest = matcher.group(4);
      if (eastWest.equalsIgnoreCase("W")) {
        longitude -= NtsConstants.WIDTH_125000;
      }

      return longitude;
    } else {
      throw new IllegalArgumentException(
        mapTileName + " does not start with a valid NTS 1:125,000 tile name");
    }
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final String letterBlock = super.getMapTileName(x, y);
    String northSouth;
    final double yInSheet = Math.abs(92 + y) % NtsConstants.HEIGHT_250000;
    if (yInSheet < NtsConstants.HEIGHT_125000) {
      northSouth = "s";
    } else {
      northSouth = "n";
    }

    String eastWest;
    final double xInSheet = Math.abs(184 + x) % NtsConstants.WIDTH_250000;
    if (xInSheet < NtsConstants.WIDTH_125000) {
      eastWest = "e";
    } else {
      eastWest = "w";
    }
    return letterBlock + "/" + northSouth + "." + eastWest + ".";
  }
}
