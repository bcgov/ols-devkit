package com.revolsys.gis.grid;

import java.nio.file.Path;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public interface RectangularMapGrid extends GeometryFactoryProxy, MapSerializer {

  static String getTileFileName(final String filePrefix, final int coordinateSystemId,
    final String gridTileOrCellSize, final int tileMinX, final int tileMinY,
    final String fileExtension) {
    return getTileFileName(filePrefix, coordinateSystemId, gridTileOrCellSize, tileMinX, tileMinY,
      null, fileExtension);
  }

  static String getTileFileName(final String filePrefix, final int coordinateSystemId,
    final String gridTileOrCellSize, final int tileMinX, final int tileMinY,
    final String fileSuffix, final String fileExtension) {
    final StringBuilder fileName = new StringBuilder(filePrefix);
    fileName.append('_');
    fileName.append(coordinateSystemId);
    fileName.append('_');
    fileName.append(gridTileOrCellSize);
    fileName.append('_');
    fileName.append(tileMinX);
    fileName.append('_');
    fileName.append(tileMinY);
    if (Property.hasValue(fileSuffix)) {
      fileName.append('_');
      fileName.append(fileSuffix);
    }
    fileName.append('.');
    fileName.append(fileExtension);
    return fileName.toString();
  }

  static Path getTilePath(final Path basePath, final String filePrefix,
    final int coordinateSystemId, final String gridTileOrCellSize, final int tileMinX,
    final int tileMinY, final String fileExtension) {
    final String fileName = getTileFileName(filePrefix, coordinateSystemId, gridTileOrCellSize,
      tileMinX, tileMinY, fileExtension);
    return basePath //
      .resolve(fileExtension) //
      .resolve(Integer.toString(coordinateSystemId)) //
      .resolve(gridTileOrCellSize) //
      .resolve(Integer.toString(tileMinX)) //
      .resolve(fileName);
  }

  static Resource getTileResource(final Resource basePath, final String filePrefix,
    final int coordinateSystemId, final String gridTileOrCellSize, final int tileMinX,
    final int tileMinY, final String fileExtension) {
    return getTileResource(basePath, filePrefix, coordinateSystemId, gridTileOrCellSize, tileMinX,
      tileMinY, null, fileExtension);
  }

  static Resource getTileResource(final Resource basePath, final String filePrefix,
    final int coordinateSystemId, final String gridTileOrCellSize, final int tileMinX,
    final int tileMinY, final String fileSuffix, final String fileExtension) {
    final String fileName = getTileFileName(filePrefix, coordinateSystemId, gridTileOrCellSize,
      tileMinX, tileMinY, fileSuffix, fileExtension);
    return basePath //
      .createRelative(fileExtension) //
      .createRelative(Integer.toString(coordinateSystemId)) //
      .createRelative(gridTileOrCellSize) //
      .createRelative(Integer.toString(tileMinX)) //
      .createRelative(fileName);
  }

  default BoundingBox getBoundingBox(final String mapTileName, final int srid) {
    final GeometryFactory geometryFactory = GeometryFactory.floating3d(srid);
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    return boundingBox.bboxToCs(geometryFactory);
  }

  String getFormattedMapTileName(String name);

  String getMapTileName(final double x, final double y);

  default String getMapTileName(final Geometry geometry) {
    final Geometry projectedGeometry = geometry.convertGeometry(getGeometryFactory());
    final Point centroid = projectedGeometry.getCentroid();
    final Point coordinate = centroid.getPoint();
    final String mapsheet = getMapTileName(coordinate.getX(), coordinate.getY());
    return mapsheet;
  }

  String getName();

  default Polygon getPolygon(final String mapTileName, final GeometryFactory geometryFactory) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory);
    return polygon;
  }

  default Polygon getPolygon(final String mapTileName, final GeometryFactory geometryFactory,
    final int numX, final int numY) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory, numX, numY);
    return polygon;
  }

  RecordDefinition getRecordDefinition();

  RectangularMapTile getTileByLocation(double x, double y);

  RectangularMapTile getTileByName(String name);

  double getTileHeight();

  List<RectangularMapTile> getTiles(final BoundingBox boundingBox);

  double getTileWidth();

  @Override
  default JsonObject toMap() {
    return new JsonObjectHash();
  }
}
