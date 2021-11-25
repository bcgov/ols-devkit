package com.revolsys.record.io.format.esri.rest.map;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.exception.WrappedException;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.BufferedImages;
import com.revolsys.record.io.format.esri.rest.ArcGisResponse;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.record.io.format.esri.rest.ArcGisRestServiceContainer;
import com.revolsys.record.io.format.esri.rest.CatalogElement;
import com.revolsys.util.Strings;
import com.revolsys.util.UrlUtil;

public class MapService extends ArcGisRestAbstractLayerService {
  public static MapService getMapService(String url) {
    url = url.replaceAll("/*MapServer/*(\\?.*)?", "") + "/MapServer";
    final ArcGisRestCatalog catalog = ArcGisRestCatalog.newArcGisRestCatalog(url);
    final PathName path = PathName
      .newPathName(url.substring(catalog.getServiceUrl().toString().length()));
    final MapService service = catalog.getWebServiceResource(path, MapService.class);
    return service;
  }

  private TileInfo tileInfo;

  private String mapName;

  private String supportedImageFormatTypes;

  private boolean singleFusedMapCache;

  private boolean exportTilesAllowed;

  private int maxImageHeight;

  private int maxImageWidth;

  private long minScale = Long.MAX_VALUE;

  private long maxScale = 0;

  private boolean supportsDynamicLayers;

  protected MapService() {
    super("MapServer");
  }

  public MapService(final ArcGisRestServiceContainer parent) {
    super(parent, "MapServer");
  }

  public BoundingBox getBoundingBox(final int zoomLevel, final int tileX, final int tileY) {
    final TileInfo tileInfo = getTileInfo();

    final double originX = tileInfo.getOriginX();
    final double tileWidth = tileInfo.getModelWidth(zoomLevel);
    final double x1 = originX + tileWidth * tileX;
    final double x2 = x1 + tileWidth;

    final double originY = tileInfo.getOriginY();
    final double tileHeight = tileInfo.getModelHeight(zoomLevel);
    final double y1 = originY - tileHeight * tileY;
    final double y2 = y1 - tileHeight;

    final GeometryFactory geometryFactory = tileInfo.getGeometryFactory();
    return geometryFactory.newBoundingBox(x1, y1, x2, y2);
  }

  public BufferedImage getExportImage(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight) {
    final String url = getExportUrl(boundingBox, imageWidth, imageHeight);
    boolean retry = true;
    while (true) {
      try {
        return BufferedImages.readImageIo(url);
      } catch (final WrappedException e) {
        if (Exceptions.isException(e, FileNotFoundException.class)) {
          return null;
        } else if (!retry) {
          throw Exceptions.wrap(e);
        }
      }
      retry = false;
    }
  }

  public String getExportUrl(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight) {
    final MapEx parameters = new LinkedHashMapEx();
    final String bbox = Strings.toString(",", boundingBox.getMinX(), boundingBox.getMinY(),
      boundingBox.getMaxX(), boundingBox.getMaxY());
    parameters.put("bbox", bbox);
    parameters.put("bboxSR", boundingBox.getCoordinateSystemId());
    parameters.put("imageSR", boundingBox.getCoordinateSystemId());
    parameters.put("size", imageWidth + "," + imageHeight);
    parameters.put("transparent", "true");
    parameters.put("dpi", "144");
    parameters.put("scale", 20000000);
    parameters.put("f", "image");

    return UrlUtil.getUrl(getServiceUrl() + "/export/", parameters);

  }

  @Override
  public String getIconName() {
    return "folder:map";
  }

  public String getMapName() {
    return this.mapName;
  }

  public int getMaxImageHeight() {
    return this.maxImageHeight;
  }

  public int getMaxImageWidth() {
    return this.maxImageWidth;
  }

  public long getMaxScale() {
    return this.maxScale;
  }

  public long getMinScale() {
    return this.minScale;
  }

  public double getResolution(final int zoomLevel) {
    final TileInfo tileInfo = getTileInfo();
    final List<LevelOfDetail> levelOfDetails = tileInfo.getLevelOfDetails();
    for (final LevelOfDetail levelOfDetail : levelOfDetails) {
      final int level = levelOfDetail.getLevel();
      if (level == zoomLevel) {
        return levelOfDetail.getResolution();
      }
    }
    return 0;
  }

  public String getSupportedImageFormatTypes() {
    return this.supportedImageFormatTypes;
  }

  public BufferedImage getTileImage(final int zoomLevel, final int tileX, final int tileY) {
    final String url = getTileUrl(zoomLevel, tileX, tileY);
    boolean retry = true;
    while (true) {
      try {
        return BufferedImages.readImageIo(url);
      } catch (final WrappedException e) {
        if (Exceptions.isException(e, FileNotFoundException.class)) {
          return null;
        } else if (!retry) {
          throw Exceptions.wrap(e);
        }
      }
      retry = false;
    }
  }

  public TileInfo getTileInfo() {
    refreshIfNeeded();
    return this.tileInfo;
  }

  public String getTileUrl(final int zoomLevel, final int tileX, final int tileY) {
    return getServiceUrl() + "/tile/" + zoomLevel + "/" + tileY + "/" + tileX;
  }

  public int getTileX(final int zoomLevel, final double x) {
    final TileInfo tileInfo = getTileInfo();
    final double modelTileSize = tileInfo.getModelWidth(zoomLevel);
    final double originX = tileInfo.getOriginX();
    final double deltaX = x - originX;
    final double ratio = deltaX / modelTileSize;
    int tileX = (int)Math.floor(ratio);
    if (tileX > 0 && ratio - tileX < 0.0001) {
      tileX--;
    }
    return tileX;
  }

  public int getTileY(final int zoomLevel, final double y) {
    final TileInfo tileInfo = getTileInfo();
    final double modelTileSize = tileInfo.getModelHeight(zoomLevel);

    final double originY = tileInfo.getOriginY();
    final double deltaY = originY - y;

    final double ratio = deltaY / modelTileSize;
    final int tileY = (int)Math.floor(ratio);
    return tileY;
  }

  public int getZoomLevel(final double resolution) {
    final TileInfo tileInfo = getTileInfo();
    if (tileInfo == null) {
      return 0;
    }
    final List<LevelOfDetail> levelOfDetails = tileInfo.getLevelOfDetails();
    LevelOfDetail previousLevel = levelOfDetails.get(0);
    for (final LevelOfDetail levelOfDetail : levelOfDetails) {
      final double levelResolution = levelOfDetail.getResolution();
      if (resolution > levelResolution) {
        if (levelOfDetail == previousLevel) {
          return levelOfDetail.getLevel();
        } else {
          final double ratio = levelResolution / resolution;
          if (ratio < 0.95) {
            return previousLevel.getLevel();
          } else {
            return levelOfDetail.getLevel();
          }
        }
      }
      previousLevel = levelOfDetail;
    }
    return previousLevel.getLevel();
  }

  @Override
  protected void initChildren(final MapEx properties, final List<CatalogElement> children,
    final Map<String, LayerDescription> rootLayersByName) {
    this.tileInfo = ArcGisResponse.newObject(TileInfo.class, properties, "tileInfo");
    if (this.tileInfo != null) {
      this.tileInfo.setMapService(this);
      children.add(this.tileInfo);
    }
    super.initChildren(properties, children, rootLayersByName);
  }

  public boolean isExportTilesAllowed() {
    return this.exportTilesAllowed;
  }

  public boolean isSingleFusedMapCache() {
    return this.singleFusedMapCache;
  }

  public boolean isSupportsDynamicLayers() {
    return this.supportsDynamicLayers;
  }

  public void setExportTilesAllowed(final boolean exportTilesAllowed) {
    this.exportTilesAllowed = exportTilesAllowed;
  }

  public void setMapName(final String mapName) {
    this.mapName = mapName;
  }

  public void setMaxImageHeight(final int maxImageHeight) {
    this.maxImageHeight = maxImageHeight;
  }

  public void setMaxImageWidth(final int maxImageWidth) {
    this.maxImageWidth = maxImageWidth;
  }

  public void setMaxScale(final long maxScale) {
    this.maxScale = maxScale;
  }

  public void setMinScale(final long minScale) {
    this.minScale = minScale;
  }

  public void setSupportedImageFormatTypes(final String supportedImageFormatTypes) {
    this.supportedImageFormatTypes = supportedImageFormatTypes;
  }

  public void setSupportsDynamicLayers(final boolean supportsDynamicLayers) {
    this.supportsDynamicLayers = supportsDynamicLayers;
  }
}
