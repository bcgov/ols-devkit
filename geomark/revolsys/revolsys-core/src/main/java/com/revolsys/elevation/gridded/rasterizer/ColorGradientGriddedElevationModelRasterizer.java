package com.revolsys.elevation.gridded.rasterizer;

import java.util.Map;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.gradient.GradientLibrary;
import com.revolsys.elevation.gridded.rasterizer.gradient.LinearGradient;
import com.revolsys.record.io.format.json.JsonObject;

public class ColorGradientGriddedElevationModelRasterizer
  extends AbstractGriddedElevationModelRasterizer {

  private LinearGradient gradient;

  public ColorGradientGriddedElevationModelRasterizer() {
    super("colorGradientGriddedElevationModelRasterizer", "style_color_gradient");
  }

  public ColorGradientGriddedElevationModelRasterizer(final GriddedElevationModel elevationModel) {
    this();
    setElevationModel(elevationModel);
  }

  public ColorGradientGriddedElevationModelRasterizer(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  @Override
  public ColorGradientGriddedElevationModelRasterizer clone() {
    final ColorGradientGriddedElevationModelRasterizer clone = (ColorGradientGriddedElevationModelRasterizer)super.clone();
    if (this.gradient != null) {
      clone.gradient = this.gradient.clone();
    }
    return clone;
  }

  public LinearGradient getGradient() {
    return this.gradient;
  }

  @Override
  public String getName() {
    return "Color Gradient";
  }

  @Override
  public int getValue(final int gridX, final int gridY) {
    final double elevation = this.elevationModel.getValue(gridX, gridY);
    return this.gradient.getColorIntForValue(elevation);

  }

  public void setGradient(final LinearGradient gradient) {
    this.gradient = gradient;
    updateValues();
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "gradient", this.gradient);
    return map;
  }

  @Override
  public void updateValues() {
    if (this.gradient == null) {
      this.gradient = GradientLibrary.getDefaultGradient("wkp/country/wiki-washington");
    }

    this.gradient.updateValues();
    firePropertyChange("styleUpdated", false, true);
  }
}
