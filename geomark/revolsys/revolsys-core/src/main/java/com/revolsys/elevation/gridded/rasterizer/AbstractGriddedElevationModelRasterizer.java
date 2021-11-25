package com.revolsys.elevation.gridded.rasterizer;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.properties.BaseObjectWithPropertiesAndChange;
import com.revolsys.record.io.format.json.JsonObject;

public abstract class AbstractGriddedElevationModelRasterizer
  extends BaseObjectWithPropertiesAndChange implements GriddedElevationModelRasterizer {

  protected GriddedElevationModel elevationModel;

  protected double maxZ = Double.NaN;

  protected double minZ = Double.NaN;

  private final String iconName;

  protected int width;

  protected int height;

  private final String type;

  protected double rangeZ;

  public AbstractGriddedElevationModelRasterizer(final String type, final String iconName) {
    this.type = type;
    this.iconName = iconName;
  }

  @Override
  public AbstractGriddedElevationModelRasterizer clone() {
    return (AbstractGriddedElevationModelRasterizer)super.clone();
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.elevationModel.getBoundingBox();
  }

  @Override
  public GriddedElevationModel getElevationModel() {
    return this.elevationModel;
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  @Override
  public String getIconName() {
    return this.iconName;
  }

  @Override
  public double getMaxZ() {
    return this.maxZ;
  }

  @Override
  public double getMinZ() {
    return this.minZ;
  }

  public double getRangeZ() {
    return this.rangeZ;
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public void setElevationModel(final GriddedElevationModel elevationModel) {
    this.elevationModel = elevationModel;
    if (elevationModel != null) {
      this.width = elevationModel.getGridWidth();
      this.height = elevationModel.getGridHeight();
      if (Double.isNaN(this.minZ)) {
        this.minZ = this.elevationModel.getMinValue();
        this.maxZ = this.elevationModel.getMaxValue();
      }
    }
    updateValues();
  }

  @Override
  public void setMaxZ(final double maxZ) {
    this.maxZ = maxZ;
    updateValues();
  }

  @Override
  public void setMinZ(final double minZ) {
    this.minZ = minZ;
    updateValues();
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = newTypeMap(this.type);
    return map;
  }

  @Override
  public void updateValues() {
    this.rangeZ = this.maxZ - this.minZ;
  }

}
