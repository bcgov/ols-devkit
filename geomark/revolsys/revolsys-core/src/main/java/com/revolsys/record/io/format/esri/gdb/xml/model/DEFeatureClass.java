package com.revolsys.record.io.format.esri.gdb.xml.model;

import com.revolsys.record.io.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.GeometryType;
import com.revolsys.util.Property;

public class DEFeatureClass extends DETable {
  private String areaFieldName = "";

  private Envelope extent;

  private String featureType = EsriGeodatabaseXmlConstants.FEATURE_TYPE_SIMPLE;

  private boolean hasM;

  private boolean hasSpatialIndex = true;

  private boolean hasZ;

  private String lengthFieldName = "";

  private String shapeFieldName;

  private GeometryType shapeType;

  private SpatialReference spatialReference;

  public DEFeatureClass() {
    super("{52353152-891A-11D0-BEC6-00805F7C4268}");
    setDatasetType(EsriGeodatabaseXmlConstants.DATASET_TYPE_FEATURE_CLASS);
  }

  public String getAreaFieldName() {
    return this.areaFieldName;
  }

  public Envelope getExtent() {
    return this.extent;
  }

  public String getFeatureType() {
    return this.featureType;
  }

  public String getLengthFieldName() {
    return this.lengthFieldName;
  }

  public String getShapeFieldName() {
    return this.shapeFieldName;
  }

  public GeometryType getShapeType() {
    return this.shapeType;
  }

  public SpatialReference getSpatialReference() {
    return this.spatialReference;
  }

  public boolean isHasM() {
    return this.hasM;
  }

  public boolean isHasSpatialIndex() {
    return this.hasSpatialIndex;
  }

  public boolean isHasZ() {
    return this.hasZ;
  }

  public void setAreaFieldName(final String areaFieldName) {
    if (Property.hasValue(areaFieldName)) {
      this.areaFieldName = areaFieldName;
    } else {
      this.areaFieldName = "";
    }
  }

  public void setExtent(final Envelope extent) {
    this.extent = extent;
  }

  public void setFeatureType(final String featureType) {
    this.featureType = featureType;
  }

  public void setHasM(final boolean hasM) {
    this.hasM = hasM;
  }

  public void setHasSpatialIndex(final boolean hasSpatialIndex) {
    this.hasSpatialIndex = hasSpatialIndex;
  }

  public void setHasZ(final boolean hasZ) {
    this.hasZ = hasZ;
  }

  public void setLengthFieldName(final String lengthFieldName) {
    if (Property.hasValue(lengthFieldName)) {
      this.lengthFieldName = lengthFieldName;
    } else {
      this.lengthFieldName = "";
    }
  }

  public void setShapeFieldName(final String shapeFieldName) {
    this.shapeFieldName = shapeFieldName;
  }

  public void setShapeType(final GeometryType shapeType) {
    this.shapeType = shapeType;
  }

  public void setSpatialReference(final SpatialReference spatialReference) {
    this.spatialReference = spatialReference;
  }
}
