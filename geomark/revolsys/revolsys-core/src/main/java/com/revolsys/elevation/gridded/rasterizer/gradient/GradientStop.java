package com.revolsys.elevation.gridded.rasterizer.gradient;

import java.awt.Color;
import java.util.Map;

import org.jeometry.common.awt.WebColors;

import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.io.format.json.JsonObject;

public class GradientStop extends BaseObjectWithProperties
  implements Cloneable, MapSerializer, Comparable<GradientStop> {

  private static final int NULL_COLOUR = WebColors.colorToRGB(0, 0, 0, 0);

  private int alpha;

  private int alphaRange;

  private int blue;

  private int blueRange;

  private Color color = WebColors.Black;

  private int colorInt = this.color.getRGB();

  private int green;

  private int greenRange;

  private Color previousColor = WebColors.White;

  private int previousColorInt = this.previousColor.getRGB();

  private double previousValue;

  private int red;

  private int redRange;

  private double value;

  private double valueRange;

  private double valueRangeMultiple;

  public GradientStop() {
    updateValues();
  }

  public GradientStop(final double value, final Color color) {
    this.value = value;
    setColor(color);
  }

  public GradientStop(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  @Override
  public GradientStop clone() {
    return (GradientStop)super.clone();
  }

  @Override
  public int compareTo(final GradientStop range) {
    final int compare = Double.compare(this.value, range.value);
    if (compare == 0) {
      return 1;
    } else {
      return compare;
    }
  }

  public Color getColor() {
    return this.color;
  }

  public int getMaxColourInt() {
    return this.previousColorInt;
  }

  public int getMinColourInt() {
    return this.colorInt;
  }

  public double getValue() {
    return this.value;
  }

  public int getValue(final double value) {
    if (Double.isNaN(value)) {
      return NULL_COLOUR;
    } else if (value <= this.previousValue) {
      return this.previousColorInt;
    } else if (value > this.value) {
      return this.colorInt;
    } else {
      return getValueFast(value);
    }
  }

  public int getValueFast(final double value) {
    if (this.value == this.previousValue) {
      return NULL_COLOUR;
    } else if (value <= this.previousValue) {
      return this.previousColorInt;
    } else if (value > this.value) {
      return NULL_COLOUR;
    } else {
      final double elevationPercent = 1 - (value - this.previousValue) * this.valueRangeMultiple;
      final int alpha = this.alpha + (int)Math.round(elevationPercent * this.alphaRange);
      final int red = this.red + (int)Math.round(elevationPercent * this.redRange);
      final int green = this.green + (int)Math.round(elevationPercent * this.greenRange);
      final int blue = this.blue + (int)Math.round(elevationPercent * this.blueRange);
      final int colour = WebColors.colorToRGB(alpha, red, green, blue);
      return colour;
    }
  }

  public boolean inRange(final double elevation) {
    return this.previousValue <= elevation && elevation <= this.value;
  }

  public void setColor(final Color color) {
    this.alpha = color.getAlpha();
    this.red = color.getRed();
    this.green = color.getGreen();
    this.blue = color.getBlue();
    this.color = color;
    this.colorInt = color.getRGB();
    updateValues();
  }

  public void setPrevious(final double previousValue, final Color previousColor) {
    this.previousValue = previousValue;
    if (Double.isFinite(this.previousValue)) {
      this.valueRange = this.value - previousValue;
    } else {
      this.valueRange = 0;
    }
    this.previousColor = previousColor;
    this.previousColorInt = this.previousColor.getRGB();
    updateValues();
  }

  public void setPrevious(final GradientStop previousStop) {
    this.previousColor = previousStop.getColor();
    this.previousColorInt = this.previousColor.getRGB();
    this.previousValue = previousStop.getValue();
    updateValues();
  }

  public void setValue(final double value) {
    this.value = value;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = newTypeMap("gradientStop");
    addToMap(map, "color", this.color);
    if (Double.isFinite(this.value)) {
      map.add("value", this.value);
    }
    return map;
  }

  @Override
  public String toString() {
    return this.value + " " + this.color;
  }

  public void updateValues() {
    if (!Double.isFinite(this.previousValue) && !Double.isFinite(this.value)) {
      this.valueRange = 0;
      this.valueRangeMultiple = 0;
    } else {
      this.valueRange = this.value - this.previousValue;
      if (this.valueRange == 0) {
        this.valueRangeMultiple = 0;
      } else {
        this.valueRangeMultiple = 1 / this.valueRange;
      }
    }
    this.alphaRange = this.previousColor.getAlpha() - this.alpha;
    this.redRange = this.previousColor.getRed() - this.red;
    this.greenRange = this.previousColor.getGreen() - this.green;
    this.blueRange = this.previousColor.getBlue() - this.blue;
  }
}
