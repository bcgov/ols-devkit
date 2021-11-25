package com.revolsys.record.io.format.esri.rest.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.io.format.esri.rest.ArcGisResponse;
import com.revolsys.record.io.format.esri.rest.CatalogElement;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.webservice.WebService;
import com.revolsys.webservice.WebServiceResource;

public class TileInfo extends BaseObjectWithProperties implements CatalogElement {
  private double originX = Double.NaN;

  private double originY = Double.NaN;

  private MapService mapService;

  private GeometryFactory geometryFactory;

  private int compressionQuality;

  private int dpi;

  private String format;

  private int rows;

  private int cols;

  private List<LevelOfDetail> levelOfDetails = new ArrayList<>();

  public TileInfo() {
  }

  public int getCols() {
    return this.cols;
  }

  public int getCompressionQuality() {
    return this.compressionQuality;
  }

  public int getDpi() {
    return this.dpi;
  }

  public String getFormat() {
    return this.format;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public String getIconName() {
    return "map";
  }

  public LevelOfDetail getLevelOfDetail(final int zoomLevel) {
    final List<LevelOfDetail> levelOfDetails = getLevelOfDetails();
    for (final LevelOfDetail levelOfDetail : levelOfDetails) {
      final Integer level = levelOfDetail.getLevel();
      if (level == zoomLevel) {
        return levelOfDetail;
      }
    }
    return null;
  }

  public List<LevelOfDetail> getLevelOfDetails() {
    return this.levelOfDetails;
  }

  public MapService getMapService() {
    return this.mapService;
  }

  public double getModelHeight(final int zoomLevel) {
    return getModelValue(zoomLevel, getRows());
  }

  public double getModelValue(final int zoomLevel, final int pixels) {
    final LevelOfDetail levelOfDetail = getLevelOfDetail(zoomLevel);
    final double modelValue = pixels * levelOfDetail.getResolution();
    return modelValue;
  }

  public double getModelWidth(final int zoomLevel) {
    return getModelValue(zoomLevel, getCols());
  }

  @Override
  public String getName() {
    return "Tile Cache";
  }

  public Point getOriginPoint() {
    if (Double.isNaN(this.originX)) {
      return null;
    } else {
      final GeometryFactory spatialReference = getGeometryFactory();
      return spatialReference.point(this.originX, this.originY);
    }
  }

  public double getOriginX() {
    return this.originX;
  }

  public double getOriginY() {
    return this.originY;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends WebServiceResource> R getParent() {
    return (R)this.mapService;
  }

  @Override
  public String getPathElement() {
    return "tile";
  }

  public double getPixelSize() {
    final int dpi = getDpi();
    final double pixelSize = 0.0254 / dpi;
    return pixelSize;
  }

  public int getRows() {
    return this.rows;
  }

  @Override
  public UrlResource getServiceUrl() {
    return this.mapService.getServiceUrl("tile");
  }

  @Override
  public WebService<?> getWebService() {
    return this.mapService.getWebService();
  }

  public void setCols(final int cols) {
    this.cols = cols;
  }

  public void setCompressionQuality(final int compressionQuality) {
    this.compressionQuality = compressionQuality;
  }

  public void setDpi(final int dpi) {
    this.dpi = dpi;
  }

  public void setFormat(final String format) {
    this.format = format;
  }

  public void setMapService(final MapService mapService) {
    this.mapService = mapService;
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> values) {
    super.setProperties(values);
    final MapEx origin = (MapEx)values.get("origin");
    if (origin == null) {
      this.originX = Double.NaN;
      this.originY = Double.NaN;
    } else {
      this.originX = origin.getDouble("x");
      this.originY = origin.getDouble("y");
    }
    this.geometryFactory = ArcGisResponse.newGeometryFactory((MapEx)values, "spatialReference");
    this.levelOfDetails = ArcGisResponse.newList(LevelOfDetail.class, (MapEx)values, "lods");
  }

  public void setRows(final int rows) {
    this.rows = rows;
  }
}
