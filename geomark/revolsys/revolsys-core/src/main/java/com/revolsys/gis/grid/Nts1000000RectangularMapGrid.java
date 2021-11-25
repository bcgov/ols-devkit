package com.revolsys.gis.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Doubles;
import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.util.Property;

public class Nts1000000RectangularMapGrid extends AbstractRectangularMapGrid {

  private static final Pattern NAME_PATTERN = Pattern
    .compile("^" + NtsConstants.REGEX_1000000 + ".*");

  private final GeometryFactory geometryFactory = GeometryFactory.floating2d(EpsgId.WGS84);

  private double precisionScale = 1;

  private final double tileHeight;

  private final double tileWidth;

  public Nts1000000RectangularMapGrid() {
    this(NtsConstants.WIDTH_1000000, NtsConstants.HEIGHT_1000000);
  }

  public Nts1000000RectangularMapGrid(final double width, final double height) {
    this.tileWidth = width;
    this.tileHeight = height;
    setName("NTS 1:1 000 000");
  }

  public int getBlock(final String sheet) {
    return Integer.parseInt(sheet.substring(0, sheet.length() - 4));
  }

  public BoundingBox getBoundingBox(final String mapTileName) {
    final double lat = getLatitude(mapTileName);
    final double lon = getLongitude(mapTileName);
    return getGeometryFactory().newBoundingBox(lon, lat, lon - this.tileWidth,
      lat + this.tileHeight);
  }

  @Override
  public String getFormattedMapTileName(final String name) {
    return name;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public double getLatitude(final int block) {
    final int index = block % 10;
    return NtsConstants.MAX_LATITUDE + index * NtsConstants.HEIGHT_1000000;
  }

  public double getLatitude(final String mapTileName) {
    final int block = getNtsBlock(mapTileName);
    return getLatitude(block);
  }

  public double getLongitude(final int block) {
    final int index = block / 10;
    return NtsConstants.MAX_LONGITUDE - index * NtsConstants.WIDTH_1000000;
  }

  public double getLongitude(final String mapTileName) {
    final int block = getNtsBlock(mapTileName);
    return getLongitude(block);
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final int lonRound = (int)Math.ceil(x);
    final int lonRowIndex = (int)(lonRound - NtsConstants.MAX_LONGITUDE);
    final int lonIndexCol = (int)(-lonRowIndex / NtsConstants.WIDTH_1000000);
    final int colIndex = lonIndexCol * 10;

    final int latRound = (int)Math.floor(y);
    final int latIndexRow = (int)(latRound - NtsConstants.MAX_LATITUDE);
    final int rowIndex = (int)(latIndexRow / NtsConstants.HEIGHT_1000000);

    final int block = rowIndex + colIndex;
    return String.valueOf(block);

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
    final double sourceLon = getLongitude(sheet);
    final double sourceLat = getLatitude(sheet);
    final double lon = Doubles.makePrecise(this.precisionScale, sourceLon + east * getTileWidth());
    final double lat = sourceLat + north * getTileHeight();
    return getMapTileName(lon, lat);
  }

  public int getNtsBlock(final String mapTileName) {
    if (Property.hasValue(mapTileName)) {
      final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
      if (matcher.matches()) {
        final String name = matcher.group(1);
        final int block = Integer.parseInt(name);
        return block;
      }
    }
    throw new IllegalArgumentException(mapTileName + " does not start with a valid NTS block");
  }

  @Override
  public RectangularMapTile getTileByLocation(final double x, final double y) {
    final String mapTileName = getMapTileName(x, y);
    final String formattedMapTileName = getFormattedMapTileName(mapTileName);
    final BoundingBox boundingBox = getBoundingBox(mapTileName);
    return new SimpleRectangularMapTile(this, formattedMapTileName, mapTileName, boundingBox);
  }

  @Override
  public RectangularMapTile getTileByName(final String mapTileName) {
    final BoundingBox boundingBox = getBoundingBox(mapTileName);
    final double lon = boundingBox.getMaxX();
    final double lat = boundingBox.getMinY();
    final String name = getMapTileName(lon, lat);
    final String formattedMapTileName = getFormattedMapTileName(mapTileName);
    return new SimpleRectangularMapTile(this, formattedMapTileName, name, boundingBox);
  }

  @Override
  public double getTileHeight() {
    return this.tileHeight;
  }

  @Override
  public List<RectangularMapTile> getTiles(final BoundingBox boundingBox) {
    final BoundingBox envelope = boundingBox.bboxToCs(getGeometryFactory());
    final List<RectangularMapTile> tiles = new ArrayList<>();
    final int minXCeil = (int)Math.ceil(envelope.getMinX() / this.tileWidth);
    final double minX = minXCeil * this.tileWidth;

    final int maxXCeil = (int)Math.ceil(envelope.getMaxX() / this.tileWidth) + 1;

    final int minYFloor = (int)Math.floor(envelope.getMinY() / this.tileHeight);
    final double minY = minYFloor * this.tileHeight;

    final int maxYCeil = (int)Math.ceil(envelope.getMaxY() / this.tileHeight);

    final int numX = maxXCeil - minXCeil;
    final int numY = maxYCeil - minYFloor;
    final int max = 100;
    if (numX > max || numY > max) {
      Logs.error(this, "Request would return too many tiles width=" + numX + " (max=" + max
        + ") height=" + numY + "(max=" + max + ").");
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

  public void setPrecisionScale(final double precisionScale) {
    this.precisionScale = precisionScale;
  }
}
