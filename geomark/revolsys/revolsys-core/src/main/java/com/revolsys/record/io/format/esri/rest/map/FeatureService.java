package com.revolsys.record.io.format.esri.rest.map;

import com.revolsys.record.io.format.esri.rest.ArcGisRestServiceContainer;

public class FeatureService extends ArcGisRestAbstractLayerService {
  private boolean hasVersionedData;

  private boolean supportsDisconnectedEditing;

  private boolean allowGeometryUpdates;

  private boolean enableZDefaults;

  private double zDefault = Double.NaN;

  protected FeatureService() {
    super("FeatureServer");
  }

  public FeatureService(final ArcGisRestServiceContainer parent) {
    super(parent, "FeatureServer");
  }

  @Override
  public String getIconName() {
    return "folder:table";
  }

  public double getzDefault() {
    return this.zDefault;
  }

  public boolean isAllowGeometryUpdates() {
    return this.allowGeometryUpdates;
  }

  public boolean isEnableZDefaults() {
    return this.enableZDefaults;
  }

  public boolean isHasVersionedData() {
    return this.hasVersionedData;
  }

  public boolean isSupportsDisconnectedEditing() {
    return this.supportsDisconnectedEditing;
  }

  public void setAllowGeometryUpdates(final boolean allowGeometryUpdates) {
    this.allowGeometryUpdates = allowGeometryUpdates;
  }

  public void setEnableZDefaults(final boolean enableZDefaults) {
    this.enableZDefaults = enableZDefaults;
  }

  public void setHasVersionedData(final boolean hasVersionedData) {
    this.hasVersionedData = hasVersionedData;
  }

  public void setSupportsDisconnectedEditing(final boolean supportsDisconnectedEditing) {
    this.supportsDisconnectedEditing = supportsDisconnectedEditing;
  }

  public void setzDefault(final double zDefault) {
    this.zDefault = zDefault;
  }

}
