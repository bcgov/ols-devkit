package com.revolsys.record.io.format.esri.rest;

import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;

public class ArcGisRestService extends ArcGisResponse<CatalogElement>
  implements GeometryFactoryProxy {
  private String serviceType;

  private String serviceDescription;

  private String supportedQueryFormats;

  private String capabilities;

  private String copyrightText;

  private String description;

  private MapEx documentInfo;

  private String units;

  private BoundingBox fullExtent = BoundingBox.empty();

  private BoundingBox initialExtent = BoundingBox.empty();

  private GeometryFactory spatialReference = GeometryFactory.DEFAULT_3D;

  public ArcGisRestService() {
  }

  public ArcGisRestService(final ArcGisRestServiceContainer container, final String serviceType) {
    super(container, serviceType);
    this.serviceType = serviceType;
  }

  public ArcGisRestService(final String serviceType) {
    this.serviceType = serviceType;
  }

  public String getCapabilities() {
    return this.capabilities;
  }

  @Override
  public List<CatalogElement> getChildren() {
    return Collections.emptyList();
  }

  public String getCopyrightText() {
    return this.copyrightText;
  }

  public String getDescription() {
    return this.description;
  }

  public MapEx getDocumentInfo() {
    return this.documentInfo;
  }

  public BoundingBox getFullExtent() {
    return this.fullExtent;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.spatialReference;
  }

  @Override
  public String getIconName() {
    return "file";
  }

  public BoundingBox getInitialExtent() {
    return this.initialExtent;
  }

  public String getServiceDescription() {
    return this.serviceDescription;
  }

  public String getServiceType() {
    return this.serviceType;
  }

  public GeometryFactory getSpatialReference() {
    return this.spatialReference;
  }

  public String getSupportedQueryFormats() {
    return this.supportedQueryFormats;
  }

  public String getUnits() {
    return this.units;
  }

  @Override
  public String getWebServiceTypeName() {
    return null;
  }

  @Override
  protected void initialize(final MapEx properties) {
    super.initialize(properties);
    this.spatialReference = ArcGisResponse.newGeometryFactory(properties, "spatialReference");
    this.initialExtent = ArcGisResponse.newBoundingBox(properties, "initialExtent");
    this.fullExtent = ArcGisResponse.newBoundingBox(properties, "fullExtent");
  }

  public void setCapabilities(final String capabilities) {
    this.capabilities = capabilities;
  }

  public void setCopyrightText(final String copyrightText) {
    this.copyrightText = copyrightText;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setServiceDescription(final String serviceDescription) {
    this.serviceDescription = StringUtils.trimWhitespace(serviceDescription);
  }

  public void setSupportedQueryFormats(final String supportedQueryFormats) {
    this.supportedQueryFormats = supportedQueryFormats;
  }

  public void setUnits(final String units) {
    this.units = units;
  }
}
