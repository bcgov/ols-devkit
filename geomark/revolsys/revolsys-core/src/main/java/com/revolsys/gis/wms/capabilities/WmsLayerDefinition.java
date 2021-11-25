package com.revolsys.gis.wms.capabilities;

import java.util.ArrayList;
import java.util.List;

import org.jeometry.coordinatesystem.model.systems.EpsgId;
import org.w3c.dom.Element;

import com.revolsys.collection.Parent;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.wms.WmsClient;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.io.format.xml.XmlUtil;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.webservice.WebServiceResource;

public class WmsLayerDefinition implements Parent<WmsLayerDefinition>, WebServiceResource {
  private final String abstractDescription;

  private Attribution attribution;

  private final List<AuthorityUrl> authorityUrls = new ArrayList<>();

  private final List<WmsBoundingBox> boundingBoxes = new ArrayList<>();

  private final int cascaded;

  private final List<FormatUrl> dataUrls = new ArrayList<>();

  private final List<Dimension> dimensions = new ArrayList<>();

  private final List<Extent> extents = new ArrayList<>();

  private final List<FormatUrl> featureListUrls = new ArrayList<>();

  private final int fixedHeight;

  private final int fixedWidth;

  private final List<WmsIdentifier> identifiers = new ArrayList<>();

  private final List<String> keywords = new ArrayList<>();

  private BoundingBox latLonBoundingBox;

  private final List<WmsLayerDefinition> layers = new ArrayList<>();

  private final String name;

  private final boolean noSubsets;

  private final boolean opaque;

  private WebServiceResource parent;

  private final boolean queryable;

  private final List<MetadataUrl> metadataUrls = new ArrayList<>();

  private final List<String> srs = new ArrayList<>();

  private final List<Style> styles = new ArrayList<>();

  private final String title;

  private double maximumScale = 0;

  private double minimumScale = Long.MAX_VALUE;

  public WmsLayerDefinition(final Element layerElement) {
    final String queryable = layerElement.getAttribute("queryable");
    this.queryable = "1".equals(queryable);

    final String opaque = layerElement.getAttribute("opaque");
    this.opaque = "1".equals(opaque);

    final String noSubsets = layerElement.getAttribute("noSubsets");
    this.noSubsets = "1".equals(noSubsets);

    this.cascaded = XmlUtil.getAttributeInt(layerElement, "cascaded", 0);
    this.fixedWidth = XmlUtil.getAttributeInt(layerElement, "fixedWidth", 0);
    this.fixedHeight = XmlUtil.getAttributeInt(layerElement, "fixedHeight", 0);

    this.name = XmlUtil.getFirstElementText(layerElement, "Name");
    this.title = XmlUtil.getFirstElementText(layerElement, "Title");
    this.abstractDescription = XmlUtil.getFirstElementText(layerElement, "Abstract");
    XmlUtil.forFirstElement(layerElement, "KeywordList", keywordsElement -> {
      XmlUtil.forEachElement(keywordsElement, "Keyword", (keywordElement) -> {
        final String keyword = keywordElement.getTextContent();
        this.keywords.add(keyword);
      });
    });
    XmlUtil.forEachElement(layerElement, "CRS", (srsElement) -> {
      final String srs = srsElement.getTextContent();
      this.srs.add(srs);
    });
    XmlUtil.forEachElement(layerElement, "SRS", (srsElement) -> {
      final String srs = srsElement.getTextContent();
      this.srs.add(srs);
    });
    XmlUtil.forFirstElement(layerElement, "LatLonBoundingBox", (boundingBoxElement) -> {
      final double minX = XmlUtil.getAttributeDouble(boundingBoxElement, "minx", -180);
      final double maxX = XmlUtil.getAttributeDouble(boundingBoxElement, "maxx", 180);
      final double minY = XmlUtil.getAttributeDouble(boundingBoxElement, "miny", -90);
      final double maxY = XmlUtil.getAttributeDouble(boundingBoxElement, "maxy", 90);
      final GeometryFactory geometryFactory = GeometryFactory.floating2d(EpsgId.WGS84);
      this.latLonBoundingBox = geometryFactory.newBoundingBox(minX, minY, maxX, maxY);
    });
    XmlUtil.forFirstElement(layerElement, "EX_GeographicBoundingBox", (boundingBoxElement) -> {
      final double minX = XmlUtil.getFirstElementDouble(boundingBoxElement, "westBoundLongitude",
        -180);
      final double maxX = XmlUtil.getFirstElementDouble(boundingBoxElement, "eastBoundLongitude",
        180);
      final double minY = XmlUtil.getFirstElementDouble(boundingBoxElement, "southBoundLatitude",
        -90);
      final double maxY = XmlUtil.getFirstElementDouble(boundingBoxElement, "northBoundLatitude",
        90);
      final GeometryFactory geometryFactory = GeometryFactory.floating2d(EpsgId.WGS84);
      this.latLonBoundingBox = geometryFactory.newBoundingBox(minX, minY, maxX, maxY);
    });

    XmlUtil.forEachElement(layerElement, "DataURL", (formatUrlElement) -> {
      final FormatUrl formatUrl = new FormatUrl(formatUrlElement);
      this.dataUrls.add(formatUrl);
    });
    XmlUtil.forEachElement(layerElement, "FeatureListURL", (formatUrlElement) -> {
      final FormatUrl formatUrl = new FormatUrl(formatUrlElement);
      this.featureListUrls.add(formatUrl);
    });
    XmlUtil.forEachElement(layerElement, "BoundingBox", (formatUrlElement) -> {
      final WmsBoundingBox boundingBox = new WmsBoundingBox(formatUrlElement);
      this.boundingBoxes.add(boundingBox);
    });
    XmlUtil.forEachElement(layerElement, "Dimension", (dimensionElement) -> {
      final Dimension dimension = new Dimension(dimensionElement);
      this.dimensions.add(dimension);
    });
    XmlUtil.forEachElement(layerElement, "Extent", (extentElement) -> {
      final Extent extent = new Extent(extentElement);
      this.extents.add(extent);
    });
    XmlUtil.forFirstElement(layerElement, "Attribution", (attributionElement) -> {
      this.attribution = new Attribution(attributionElement);
    });
    XmlUtil.forEachElement(layerElement, "AuthorityUrl", (authorityUrlElement) -> {
      final AuthorityUrl authorityUrl = new AuthorityUrl(authorityUrlElement);
      this.authorityUrls.add(authorityUrl);
    });

    XmlUtil.forEachElement(layerElement, "Identifier", (identifierElement) -> {
      final WmsIdentifier identifier = new WmsIdentifier(identifierElement);
      this.identifiers.add(identifier);
    });
    XmlUtil.forEachElement(layerElement, "MetaDataURL", (metadataElement) -> {
      final MetadataUrl metadataUrl = new MetadataUrl(metadataElement);
      this.metadataUrls.add(metadataUrl);
    });
    XmlUtil.forEachElement(layerElement, "Style", (styleElement) -> {
      final Style style = new Style(styleElement);
      this.styles.add(style);
    });
    this.maximumScale = XmlUtil.getFirstElementDouble(layerElement, "MinScaleDenominator", 0);
    this.minimumScale = XmlUtil.getFirstElementDouble(layerElement, "MaxScaleDenominator",
      Long.MAX_VALUE);
    XmlUtil.forFirstElement(layerElement, "ScaleHint", (scaleHintElement) -> {
      this.maximumScale = XmlUtil.getAttributeDouble(scaleHintElement, "min", 0);
      this.minimumScale = XmlUtil.getAttributeDouble(scaleHintElement, "max", Long.MAX_VALUE);
    });
    XmlUtil.forEachElement(layerElement, "Layer", (childLayerElement) -> {
      final WmsLayerDefinition childLayer = new WmsLayerDefinition(childLayerElement);
      this.layers.add(childLayer);
      childLayer.parent = this;
    });
  }

  public String getAbstractDescription() {
    return this.abstractDescription;
  }

  public Attribution getAttribution() {
    return this.attribution;
  }

  public List<AuthorityUrl> getAuthorityUrls() {
    return this.authorityUrls;
  }

  public List<WmsBoundingBox> getBoundingBoxes() {
    return this.boundingBoxes;
  }

  public int getCascaded() {
    return this.cascaded;
  }

  @Override
  public List<WmsLayerDefinition> getChildren() {
    return this.layers;
  }

  public List<FormatUrl> getDataUrls() {
    return this.dataUrls;
  }

  public GeometryFactory getDefaultGeometryFactory() {
    if (this.srs.isEmpty()) {
      if (this.parent instanceof WmsLayerDefinition) {
        final WmsLayerDefinition parentLayer = (WmsLayerDefinition)this.parent;
        return parentLayer.getDefaultGeometryFactory();
      } else {
        return GeometryFactory.floating2d(EpsgId.WGS84);
      }
    } else {
      return WmsClient.getGeometryFactory(this.srs.get(0));
    }
  }

  public String getDefaultStyleName() {
    if (this.styles.isEmpty()) {
      if (this.parent instanceof WmsLayerDefinition) {
        final WmsLayerDefinition parentLayer = (WmsLayerDefinition)this.parent;
        return parentLayer.getDefaultStyleName();
      } else {
        return "default";
      }
    } else {
      return this.styles.get(0).getName();
    }
  }

  public List<Dimension> getDimensions() {
    return this.dimensions;
  }

  public List<Extent> getExtents() {
    return this.extents;
  }

  public List<FormatUrl> getFeatureListUrls() {
    return this.featureListUrls;
  }

  public int getFixedHeight() {
    return this.fixedHeight;
  }

  public int getFixedWidth() {
    return this.fixedWidth;
  }

  @Override
  public String getIconName() {
    if (this.layers.isEmpty()) {
      return "map";
    } else {
      return "folder:map";
    }
  }

  public List<WmsIdentifier> getIdentifiers() {
    return this.identifiers;
  }

  public List<String> getKeywords() {
    return this.keywords;
  }

  public com.revolsys.geometry.model.BoundingBox getLatLonBoundingBox() {
    if (this.latLonBoundingBox == null) {
      if (this.parent instanceof WmsLayerDefinition) {
        final WmsLayerDefinition parentLayer = (WmsLayerDefinition)this.parent;
        return parentLayer.getLatLonBoundingBox();
      } else {
        return BoundingBox.empty();
      }
    }
    return this.latLonBoundingBox;
  }

  public List<WmsLayerDefinition> getLayers() {
    return this.layers;
  }

  public GeoreferencedImage getMapImage(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight) {
    final BoundingBox queryBoundingBox = boundingBox.bboxIntersection(getLatLonBoundingBox());
    final String srs = "EPSG:" + queryBoundingBox.getHorizontalCoordinateSystemId();
    final WmsClient wmsClient = getWmsClient();
    return wmsClient.getMapImage(this.name, getDefaultStyleName(), srs, queryBoundingBox,
      "image/png", imageWidth, imageHeight);
  }

  public double getMaximumScale() {
    return this.maximumScale;
  }

  public List<MetadataUrl> getMetadataUrls() {
    return this.metadataUrls;
  }

  public double getMinimumScale() {
    return this.minimumScale;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends WebServiceResource> R getParent() {
    return (R)this.parent;
  }

  @Override
  public UrlResource getServiceUrl() {
    throw new UnsupportedOperationException();
  }

  public List<String> getSrs() {
    return this.srs;
  }

  public List<Style> getStyles() {
    return this.styles;
  }

  public String getTitle() {
    return this.title;
  }

  @Override
  public WmsClient getWebService() {
    if (this.parent instanceof WmsLayerDefinition) {
      final WmsLayerDefinition parentLayer = (WmsLayerDefinition)this.parent;
      return parentLayer.getWmsClient();
    } else if (this.parent instanceof WmsClient) {
      return (WmsClient)this.parent;

    } else {
      return null;
    }
  }

  public WmsClient getWmsClient() {
    return getWebService();
  }

  @Override
  public boolean isAllowsChildren() {
    return !this.layers.isEmpty();
  }

  public boolean isNoSubsets() {
    return this.noSubsets;
  }

  public boolean isOpaque() {
    return this.opaque;
  }

  public boolean isQueryable() {
    return this.queryable;
  }

  public void setParent(final WebServiceResource parent) {
    this.parent = parent;
  }

  @Override
  public String toString() {
    if (this.title != null) {
      return this.title;
    } else {
      return this.name;
    }
  }
}
