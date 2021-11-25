package com.revolsys.record.io.format.esri.gdb.xml.model;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.GeometryType;

public class GeometryDef {
  private int avgNumPoints;

  private GeometryType geometryType;

  private double gridSize0;

  private Double gridSize1;

  private Double gridSize2;

  private boolean hasM;

  private boolean hasZ;

  private SpatialReference spatialReference;

  public GeometryDef() {
  }

  public GeometryDef(final GeometryType geometryType, final SpatialReference spatialReference) {
    this.geometryType = geometryType;
    this.spatialReference = spatialReference;
    final GeometryFactory geometryFactory = spatialReference.getGeometryFactory();
    this.hasZ = geometryFactory.hasZ();
    this.hasM = geometryFactory.hasM();
  }

  public int getAvgNumPoints() {
    return this.avgNumPoints;
  }

  public GeometryType getGeometryType() {
    return this.geometryType;
  }

  public double getGridSize0() {
    return this.gridSize0;
  }

  public Double getGridSize1() {
    return this.gridSize1;
  }

  public Double getGridSize2() {
    return this.gridSize2;
  }

  public SpatialReference getSpatialReference() {
    return this.spatialReference;
  }

  public boolean isHasM() {
    return this.hasM;
  }

  public boolean isHasZ() {
    return this.hasZ;
  }

  public void setAvgNumPoints(final int avgNumPoints) {
    this.avgNumPoints = avgNumPoints;
  }

  public void setGeometryType(final GeometryType geometryType) {
    this.geometryType = geometryType;
  }

  public void setGridSize0(final double gridSize0) {
    this.gridSize0 = gridSize0;
  }

  public void setGridSize1(final Double gridSize1) {
    this.gridSize1 = gridSize1;
  }

  public void setGridSize2(final Double gridSize2) {
    this.gridSize2 = gridSize2;
  }

  public void setHasM(final boolean hasM) {
    this.hasM = hasM;
  }

  public void setHasZ(final boolean hasZ) {
    this.hasZ = hasZ;
  }

  public void setSpatialReference(final SpatialReference spatialReference) {
    this.spatialReference = spatialReference;
  }

}
