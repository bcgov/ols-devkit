package com.revolsys.elevation.gridded.rasterizer;

import java.awt.Color;
import java.util.Map;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.gradient.LinearGradient;
import com.revolsys.elevation.gridded.rasterizer.gradient.MultiStopLinearGradient;
import com.revolsys.grid.Grid;
import com.revolsys.record.io.format.json.JsonObject;

public class SlopeColorGradientGriddedElevationModelRasterizer
  extends AbstractGriddedElevationModelRasterizer {

  private LinearGradient gradient;

  private double oneDivCellSizeTimes8;

  public SlopeColorGradientGriddedElevationModelRasterizer() {
    super("slopeColorGradientGriddedElevationModelRasterizer", "style_slope_color_gradient");
  }

  public SlopeColorGradientGriddedElevationModelRasterizer(
    final GriddedElevationModel elevationModel) {
    this();
    setElevationModel(elevationModel);
  }

  public SlopeColorGradientGriddedElevationModelRasterizer(final LinearGradient gradient) {
    this();
    setGradient(gradient);
  }

  public SlopeColorGradientGriddedElevationModelRasterizer(
    final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  public LinearGradient getGradient() {
    return this.gradient;
  }

  @Override
  public String getName() {
    return "Slope Color Gradient";
  }

  public double getOneDivCellSizeTimes8() {
    return this.oneDivCellSizeTimes8;
  }

  private int getSlopeColor(final double a, final double b, final double c, final double d,
    final double f, final double g, final double h, final double i) {
    final double oneDivCellSizeTimes8 = this.oneDivCellSizeTimes8;
    final double dzDivDx = (c + 2 * f + i - (a + 2 * d + g)) * oneDivCellSizeTimes8;
    final double dzDivDy = (g + 2 * h + i - (a + 2 * b + c)) * oneDivCellSizeTimes8;
    final double slopeRadians = Math.atan(Math.sqrt(dzDivDx * dzDivDx + dzDivDy * dzDivDy));
    return this.gradient.getColorIntForValue(Math.toDegrees(slopeRadians));
  }

  @Override
  public int getValue(final int gridX, final int gridY) {
    final GriddedElevationModel elevationModel = this.elevationModel;
    final int width = this.width;
    final int height = this.height;

    double a = Double.NaN;
    double b = Double.NaN;
    double c = Double.NaN;
    double d = Double.NaN;
    final double e = elevationModel.getValueFast(gridX, gridY);
    if (Double.isFinite(e)) {
      double f = Double.NaN;
      double g = Double.NaN;
      double h = Double.NaN;
      double i = Double.NaN;

      final boolean firstX = gridX == 0;
      final boolean firstY = gridY == 0;
      final boolean lastX = gridX == width - 1;
      final boolean lastY = gridY == height - 1;
      final int gridX0 = gridX - 1;
      final int gridX2 = gridX + 1;
      if (!lastY) {
        final int gridY2 = gridY + 1;
        if (!firstX) {
          a = elevationModel.getValueFast(gridX0, gridY2);
        }
        b = elevationModel.getValueFast(gridX, gridY2);
        if (!lastX) {
          c = elevationModel.getValueFast(gridX2, gridY2);
        }
      }
      if (!firstX) {
        d = elevationModel.getValueFast(gridX0, gridY);
      }
      if (!lastX) {
        f = elevationModel.getValueFast(gridX2, gridY);
      }
      if (!firstY) {
        final int gridY0 = gridY - 1;
        if (!firstX) {
          g = elevationModel.getValueFast(gridX0, gridY0);
        }
        h = elevationModel.getValueFast(gridX, gridY0);
        if (!lastX) {
          i = elevationModel.getValueFast(gridX2, gridY0);
        }
      }

      if (!Double.isFinite(d)) {
        if (Double.isFinite(f)) {
          d = e - (f - e);
        } else {
          d = e;
          f = e;
        }
      } else if (!Double.isFinite(f)) {
        f = e;
      }
      if (!Double.isFinite(a)) {
        if (Double.isFinite(g)) {
          a = d - (g - d);
        } else {
          a = d;
        }
      }
      if (!Double.isFinite(b)) {
        if (Double.isFinite(h)) {
          b = e - (h - e);
        } else {
          b = e;
        }
      }
      if (!Double.isFinite(c)) {
        if (Double.isFinite(i)) {
          c = f - (i - f);
        } else {
          c = f;
        }
      }
      if (!Double.isFinite(g)) {
        g = d - (a - d);
      }
      if (!Double.isFinite(h)) {
        h = e - (b - e);
      }
      if (!Double.isFinite(i)) {
        i = f - (c - f);
      }
      return getSlopeColor(a, b, c, d, f, g, h, i);
    } else {
      return Grid.NULL_COLOUR;
    }
  }

  @Override
  public void setElevationModel(final GriddedElevationModel elevationModel) {
    super.setElevationModel(elevationModel);
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
    super.updateValues();
    if (this.gradient == null) {
      final MultiStopLinearGradient gradient = new MultiStopLinearGradient();
      gradient.addStop(0, new Color(90, 224, 202));
      gradient.addStop(5, new Color(113, 227, 132));
      gradient.addStop(15, new Color(180, 224, 90));
      gradient.addStop(25, new Color(242, 223, 46));
      gradient.addStop(35, new Color(245, 165, 54));
      gradient.addStop(45, new Color(242, 91, 61));
      gradient.addStop(55, new Color(196, 0, 121));
      gradient.addStop(65, new Color(133, 0, 133));
      gradient.addStop(75, new Color(74, 0, 143));
      gradient.addStop(90, new Color(66, 0, 128));
      this.gradient = gradient;
    }
    if (this.elevationModel != null) {
      final double gridCellWidth = this.elevationModel.getGridCellWidth();
      this.oneDivCellSizeTimes8 = 1.0 / (8 * gridCellWidth);
    }

    this.gradient.updateValues();
    firePropertyChange("styleUpdated", false, true);
  }
}
