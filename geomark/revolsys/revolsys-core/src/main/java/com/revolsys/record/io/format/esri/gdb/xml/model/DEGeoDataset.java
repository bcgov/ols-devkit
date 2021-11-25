package com.revolsys.record.io.format.esri.gdb.xml.model;

public class DEGeoDataset extends DEDataset {

  private Envelope extent;

  private SpatialReference spatialReference;

  public Envelope getExtent() {
    return this.extent;
  }

  public SpatialReference getSpatialReference() {
    return this.spatialReference;
  }

  public void setExtent(final Envelope extent) {
    this.extent = extent;
  }

  public void setSpatialReference(final SpatialReference spatialReference) {
    this.spatialReference = spatialReference;
  }

}
