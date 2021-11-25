package com.revolsys.gis.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.RectangleXY;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.util.Property;

public class CustomRectangularMapGrid extends AbstractRectangularMapGrid {
  private static final double DEFAULT_TILE_SIZE = 1000;

  public static int getGridCeil(final double origin, final double gridSize, final double value) {
    final int xIndex = (int)Math.ceil((value - origin) / gridSize);
    final double gridValue = origin + xIndex * gridSize;
    return (int)gridValue;
  }

  public static int getGridFloor(final double origin, final double gridSize, final double value) {
    final int xIndex = (int)Math.floor((value - origin) / gridSize);
    final double gridValue = origin + xIndex * gridSize;
    return (int)gridValue;
  }

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private double originX = 0.0;

  private double originY = 0.0;

  private double tileHeight = DEFAULT_TILE_SIZE;

  private double tileWidth = DEFAULT_TILE_SIZE;

  public CustomRectangularMapGrid() {
  }

  public CustomRectangularMapGrid(final GeometryFactory geometryFactory) {
    this(geometryFactory, 0, 0, DEFAULT_TILE_SIZE, DEFAULT_TILE_SIZE);
  }

  public CustomRectangularMapGrid(final GeometryFactory geometryFactory, final double tileSize) {
    this(geometryFactory, 0, 0, tileSize, tileSize);
  }

  public CustomRectangularMapGrid(final GeometryFactory geometryFactory, final double tileWidth,
    final double tileHeight) {
    this(geometryFactory, 0, 0, tileWidth, tileHeight);
  }

  public CustomRectangularMapGrid(final GeometryFactory geometryFactory, final double originX,
    final double originY, final double tileWidth, final double tileHeight) {
    this.geometryFactory = geometryFactory;
    this.tileHeight = tileHeight;
    this.tileWidth = tileWidth;
    this.originX = originX;
    this.originY = originY;
  }

  public CustomRectangularMapGrid(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  public void forEachTile(final BoundingBoxProxy boundingBox, final BiConsumerDouble action) {
    final BoundingBox convertedBoundingBox = boundingBox.getBoundingBox()
      .bboxToCs(getGeometryFactory());

    final int minX = getGridFloor(this.originX, this.tileWidth, convertedBoundingBox.getMinX());
    final int minY = getGridFloor(this.originY, this.tileHeight, convertedBoundingBox.getMinY());
    final int maxX = getGridCeil(this.originX, this.tileWidth, convertedBoundingBox.getMaxX());
    final int maxY = getGridCeil(this.originY, this.tileHeight, convertedBoundingBox.getMaxY());

    final int numX = (int)Math.ceil((maxX - minX) / this.tileWidth);
    final int numY = (int)Math.ceil((maxY - minY) / this.tileWidth);
    for (int i = 0; i < numY; i++) {
      final double y = minY + i * this.tileHeight;
      for (int j = 0; j < numX; j++) {
        final double x = minX + j * this.tileWidth;
        action.accept(x, y);
      }
    }
  }

  public void forEachTile(final BoundingBoxProxy boundingBox,
    final Consumer<RectangularMapTile> action) {
    final BoundingBox convertedBoundingBox = boundingBox.getBoundingBox()
      .bboxToCs(getGeometryFactory());

    final int minX = getGridFloor(this.originX, this.tileWidth, convertedBoundingBox.getMinX());
    final int minY = getGridFloor(this.originY, this.tileHeight, convertedBoundingBox.getMinY());
    final int maxX = getGridCeil(this.originX, this.tileWidth, convertedBoundingBox.getMaxX());
    final int maxY = getGridCeil(this.originY, this.tileHeight, convertedBoundingBox.getMaxY());

    final int numX = (int)Math.ceil((maxX - minX) / this.tileWidth);
    final int numY = (int)Math.ceil((maxY - minY) / this.tileWidth);
    for (int i = 0; i < numY; i++) {
      final double y = minY + i * this.tileHeight;
      for (int j = 0; j < numX; j++) {
        final double x = minX + j * this.tileWidth;
        final RectangularMapTile tile = getTileByLocation(x, y);
        action.accept(tile);
      }
    }
  }

  public void forEachTile(final Geometry geometry, final Consumer<RectangularMapTile> action) {
    final Geometry convertedGeometry = toCoordinateSystem(geometry);
    if (!Property.isEmpty(convertedGeometry)) {
      final BoundingBox boundingBox = convertedGeometry.getBoundingBox();
      forEachTile(boundingBox, tile -> {
        final BoundingBox tileBoundingBox = tile.getBoundingBox();
        if (convertedGeometry.intersectsBbox(tileBoundingBox)) {
          action.accept(tile);
        }
      });
    }
  }

  public List<RectangularMapTile> getAllTiles(final BoundingBox boundingBox) {
    final List<RectangularMapTile> tiles = new ArrayList<>();
    final Consumer<RectangularMapTile> action = tiles::add;
    forEachTile(boundingBox, action);
    return tiles;
  }

  public BoundingBox getBoundingBox(final double x1, final double y1) {
    final double x2 = x1 + this.tileWidth;
    final double y2 = y1 + this.tileHeight;
    return this.geometryFactory.newBoundingBox(x1, y1, x2, y2);
  }

  public BoundingBox getBoundingBox(final String name) {
    final double[] coordinates = Doubles.toDoubleArraySplit(name, "[_:,\\s|]+");
    if (coordinates.length == 2) {
      final double x1 = coordinates[0];
      final double y1 = coordinates[1];
      return getBoundingBox(x1, y1);
    } else {
      return null;
    }
  }

  @Override
  public String getFormattedMapTileName(final String name) {
    return name;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final int tileX = getTileX(x);
    final int tileY = getTileY(y);

    return tileX + "_" + tileY;
  }

  public String getMapTileName(final Point coordinates) {
    final double x = coordinates.getX();
    final double y = coordinates.getY();
    return getMapTileName(x, y);
  }

  @Override
  public String getName() {
    final StringBuilder string = new StringBuilder();
    if (this.geometryFactory != null) {
      string.append(this.geometryFactory.getHorizontalCoordinateSystemName());
      string.append(" ");
    }
    if (this.originX != 0 && this.originY != 0) {
      string.append(Doubles.toString(this.originX));
      string.append(',');
      string.append(Doubles.toString(this.originY));
    }

    string.append(Doubles.toString(this.tileWidth));
    string.append('x');
    string.append(Doubles.toString(this.tileHeight));

    return string.toString();
  }

  public double getOriginX() {
    return this.originX;
  }

  public double getOriginY() {
    return this.originY;
  }

  @Override
  public RectangularMapTile getTileByLocation(final double x, final double y) {
    final String name = getMapTileName(x, y);
    if (name == null) {
      return null;
    } else {
      return getTileByName(name);
    }
  }

  @Override
  public RectangularMapTile getTileByName(final String name) {
    final BoundingBox boundingBox = getBoundingBox(name);
    if (boundingBox == null) {
      return null;
    } else {
      return new SimpleRectangularMapTile(this, name, name, boundingBox);
    }
  }

  @Override
  public double getTileHeight() {
    return this.tileHeight;
  }

  public RectangleXY getTileRectangleByLocation(final double x, final double y) {
    final int tileX = getTileX(x);
    final int tileY = getTileY(y);
    return getGeometryFactory().newRectangle(tileX, tileY, this.tileWidth, this.tileHeight);
  }

  @Override
  public List<RectangularMapTile> getTiles(final BoundingBox boundingBox) {
    final BoundingBox envelope = boundingBox.bboxToCs(getGeometryFactory());

    final List<RectangularMapTile> tiles = new ArrayList<>();
    final int minX = getGridFloor(this.originX, this.tileWidth, envelope.getMinX());
    final int minY = getGridFloor(this.originY, this.tileHeight, envelope.getMinY());
    final int maxX = getGridCeil(this.originX, this.tileWidth, envelope.getMaxX());
    final int maxY = getGridCeil(this.originY, this.tileHeight, envelope.getMaxY());

    final int numX = (int)Math.ceil((maxX - minX) / this.tileWidth);
    final int numY = (int)Math.ceil((maxY - minY) / this.tileWidth);
    if (numX > 40 || numY > 40) {
      return tiles;
    }
    for (int i = 0; i < numY; i++) {
      final double y = minY + i * this.tileHeight;
      for (int j = 0; j < numX; j++) {
        final double x = minX + j * this.tileWidth;
        final RectangularMapTile tile = getTileByLocation(x, y);
        tiles.add(tile);
      }
    }
    return tiles;
  }

  @Override
  public double getTileWidth() {
    return this.tileWidth;
  }

  public int getTileX(final double x) {
    return getGridFloor(this.originX, this.tileWidth, x);
  }

  public int getTileY(final double y) {
    return getGridFloor(this.originY, this.tileHeight, y);
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setOriginX(final double originX) {
    this.originX = originX;
  }

  public void setOriginY(final double originY) {
    this.originY = originY;
  }

  public void setSrid(final int srid) {
    setGeometryFactory(GeometryFactory.fixed2d(srid, 1.0, 1.0));
  }

  public void setTileHeight(final double tileHeight) {
    this.tileHeight = tileHeight;
  }

  public void setTileSize(final double tileSize) {
    setTileWidth(tileSize);
    setTileHeight(tileSize);
  }

  public void setTileWidth(final double tileWidth) {
    this.tileWidth = tileWidth;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addTypeToMap(map, "customRectangularMapGrid");
    addToMap(map, "geometryFactory", getGeometryFactory());
    addToMap(map, "originX", getOriginX());
    addToMap(map, "originY", getOriginY());
    addToMap(map, "tileWidth", getTileWidth());
    addToMap(map, "tileHeight", getTileHeight());
    return map;
  }

  @Override
  public String toString() {
    return getName();
  }
}
