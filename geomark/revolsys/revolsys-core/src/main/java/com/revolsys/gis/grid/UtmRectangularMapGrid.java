package com.revolsys.gis.grid;

import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.number.Doubles;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;
import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;

public class UtmRectangularMapGrid extends AbstractRectangularMapGrid {

  private static final CoordinateSystem COORDINATE_SYSTEM = EpsgCoordinateSystems
    .getCoordinateSystem(EpsgId.WGS84);

  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.floating3d(EpsgId.WGS84);

  public static final UtmRectangularMapGrid INSTANCE = new UtmRectangularMapGrid();

  public static final double MIN_LAT = -80;

  public static final double MIN_LON = -180;

  private final double tileHeight = 8;

  private final double tileWidth = 6;

  public BoundingBox getBoundingBox(final String mapTileName) {
    final double lat = getLatitude(mapTileName);
    final double lon = getLongitude(mapTileName);
    return GEOMETRY_FACTORY.newBoundingBox(lon, lat, lon - this.tileWidth, lat + this.tileHeight);
  }

  @Override
  public String getFormattedMapTileName(final String name) {
    return name.toUpperCase();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return GEOMETRY_FACTORY;
  }

  @Override
  public <C extends CoordinateSystem> C getHorizontalCoordinateSystem() {
    return (C)COORDINATE_SYSTEM;
  }

  public int getHorizontalZone(final double lat, final double lon) {
    final String mapTileName = getMapTileName(lon, lat);
    return getHorizontalZone(mapTileName);
  }

  public int getHorizontalZone(final String sheet) {
    return Integer.parseInt(sheet.substring(0, sheet.length() - 1));
  }

  public double getLatitude(final String mapTileName) {
    final char zone = getVerticalZone(mapTileName);
    int row;
    if (zone == 'x') {
      return 72.0;
    } else if (zone >= 'p') {
      row = zone - 'e';
    } else if (zone >= 'n') {
      row = zone - 'd';
    } else {
      row = zone - 'c';
    }
    return MIN_LAT + row * this.tileHeight;
  }

  public double getLongitude(final String sheet) {
    final int zone = getHorizontalZone(sheet);
    return MIN_LON + (zone - 1) * this.tileWidth;
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    char letter;
    if (y >= 72) {
      letter = 'x';
    } else {
      final double latFloor = Math.floor(y);
      final int row = (int)((latFloor - MIN_LAT) / this.tileHeight);
      letter = (char)('c' + row);
      if (letter >= 'n') {
        letter += 2;
      } else if (letter >= 'i') {
        letter += 1;
      }
    }
    final double lonFloor = Math.floor(x);
    final int zone = (int)((lonFloor - MIN_LON) / this.tileWidth) + 1;

    return zone + String.valueOf(letter);

  }

  /**
   * Get the sheet which is the specified number of sheets east and/or north
   * from the current sheet.
   *
   * @param sheet The current sheet.
   * @param east The number of sheets east.
   * @param north The number of sheets north.
   * @return The new map sheet.
   */
  public String getMapTileName(final String sheet, final int east, final int north) {
    final double lon = Doubles.makePrecise(1.0, getLongitude(sheet) + east * getTileHeight());
    final double lat = getLatitude(sheet) + north * getTileHeight();
    return getMapTileName(lon, lat);
  }

  public int getNad27Srid(final double lon, final double lat) {
    return getNad27Srid(getMapTileName(lon, lat));
  }

  public int getNad27Srid(final Geometry geometry) {
    return getNad27Srid(getMapTileName(geometry));
  }

  public int getNad27Srid(final String sheet) {
    final int horizontalZone = getHorizontalZone(sheet);
    final int verticalZone = getVerticalZone(sheet);
    if (horizontalZone < 24 && verticalZone >= 'n') {
      return 26700 + horizontalZone;
    } else {
      throw new IllegalArgumentException("UTM Zone " + sheet + " is not in North America");
    }
  }

  public int getNad83Srid(final double lon, final double lat) {
    return getNad83Srid(getMapTileName(lon, lat));
  }

  public int getNad83Srid(final Geometry geometry) {
    return getNad83Srid(getMapTileName(geometry));
  }

  public int getNad83Srid(final String sheet) {
    final int horizontalZone = getHorizontalZone(sheet);
    final int verticalZone = getVerticalZone(sheet);
    if (horizontalZone < 24 && verticalZone >= 'n') {
      return EpsgId.nad83Utm(horizontalZone);
    } else {
      throw new IllegalArgumentException("UTM Zone " + sheet + " is not in North America");
    }
  }

  @Override
  public RectangularMapTile getTileByLocation(final double x, final double y) {
    final String mapTileName = getMapTileName(x, y);
    final BoundingBox boundingBox = getBoundingBox(mapTileName);
    final String formattedMapTileName = getFormattedMapTileName(mapTileName);
    return new SimpleRectangularMapTile(this, formattedMapTileName, mapTileName, boundingBox);
  }

  @Override
  public RectangularMapTile getTileByName(final String mapTileName) {
    final BoundingBox boundingBox = getBoundingBox(mapTileName);
    final double lon = boundingBox.getMaxX();
    final double lat = boundingBox.getMinY();
    final String tileName = getMapTileName(lon, lat);
    final String formattedMapTileName = getFormattedMapTileName(mapTileName);
    return new SimpleRectangularMapTile(this, formattedMapTileName, tileName, boundingBox);
  }

  @Override
  public double getTileHeight() {
    return this.tileHeight;
  }

  @Override
  public List<RectangularMapTile> getTiles(final BoundingBox boundingBox) {
    final com.revolsys.geometry.model.BoundingBox envelope = boundingBox
      .bboxToCs(getGeometryFactory());
    final List<RectangularMapTile> tiles = new ArrayList<>();
    final int minXCeil = (int)Math.ceil(envelope.getMinX() / this.tileWidth);
    final double minX = minXCeil * this.tileWidth;

    final int maxXCeil = (int)Math.ceil(envelope.getMaxX() / this.tileWidth) + 1;

    final int minYFloor = (int)Math.floor(envelope.getMinY() / this.tileHeight);
    final double minY = minYFloor * this.tileHeight;

    final int maxYCeil = (int)Math.ceil(envelope.getMaxY() / this.tileHeight);

    final int numX = maxXCeil - minXCeil;
    final int numY = maxYCeil - minYFloor;
    if (numX > 8 || numY > 8) {
      return tiles;
    }
    for (int y = 0; y < numY; y++) {
      final double lat = minY + y * this.tileHeight;
      for (int x = 0; x < numX; x++) {
        final double lon = minX + x * this.tileWidth;
        final RectangularMapTile tile = getTileByLocation(lon, lat);
        tiles.add(tile);
      }
    }
    return tiles;
  }

  @Override
  public double getTileWidth() {
    return this.tileWidth;
  }

  public char getVerticalZone(final String sheet) {
    return Character.toLowerCase(sheet.charAt(sheet.length() - 1));
  }

}
