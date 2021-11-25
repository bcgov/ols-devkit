package com.revolsys.elevation.gridded.rasterizer.gradient;

import java.awt.Color;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jeometry.common.logging.Logs;

import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.spring.resource.Resource;

// http://soliton.vm.bytemark.co.uk/pub/cpt-city/
public class MultiStopLinearGradient extends BaseObjectWithProperties
  implements LinearGradient, MapSerializer {

  private static final GradientStop NULL_STOP = new GradientStop(0, new Color(0, 0, 0, 0));

  private List<GradientStop> stops = new ArrayList<>();

  private GradientStop minStop = NULL_STOP;

  private GradientStop maxStop = NULL_STOP;

  public MultiStopLinearGradient() {
    this(new ArrayList<>());
  }

  public MultiStopLinearGradient(final GradientStop... stops) {
    this.stops = Arrays.asList(stops);
  }

  public MultiStopLinearGradient(final List<GradientStop> stops) {
    this.stops = stops;
  }

  public MultiStopLinearGradient(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  public MultiStopLinearGradient(final Object source) {
    try (
      BufferedReader reader = Resource.getResource(source).newBufferedReader()) {
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        final String[] values = line.split("\\s+");
        if (values.length == 5) {
          final double value = Double.parseDouble(values[0]);
          final int red = Integer.parseInt(values[1]);
          final int green = Integer.parseInt(values[2]);
          final int blue = Integer.parseInt(values[3]);
          final int alpha = Integer.parseInt(values[4]);
          final Color color = new Color(red, green, blue, alpha);
          addStop(value, color);
        }
      }
      Collections.reverse(this.stops);
      updateValues();
    } catch (final Exception e) {
      Logs.error(this, "Error reading gradient: " + source, e);
    }
  }

  public void addStop(final double value, final Color color) {
    this.stops.add(new GradientStop(value, color));
  }

  @Override
  public MultiStopLinearGradient clone() {
    final MultiStopLinearGradient clone = (MultiStopLinearGradient)super.clone();
    clone.stops = new ArrayList<>();
    for (final GradientStop stop : this.stops) {
      clone.stops.add(stop.clone());
    }
    return clone;
  }

  @Override
  public int getColorIntForValue(final double value) {
    if (Double.isFinite(value)) {
      for (final GradientStop stop : this.stops) {
        final int color = stop.getValueFast(value);
        if (color != NULL_COLOR) {
          return color;
        }
      }
      return this.maxStop.getMaxColourInt();
    } else {
      return NULL_COLOR;
    }
  }

  public GradientStop getMaxStop() {
    return this.maxStop;
  }

  public GradientStop getMinStop() {
    return this.minStop;
  }

  public List<GradientStop> getStops() {
    return this.stops;
  }

  @Override
  public double getValueMax() {
    return this.maxStop.getValue();
  }

  @Override
  public double getValueMin() {
    return this.minStop.getValue();
  }

  public void removeStop(final int index) {
    this.stops.remove(index);
  }

  public void setStops(final List<GradientStop> stops) {
    this.stops = stops;
    updateValues();
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = newTypeMap("multiStopLinearGradient");
    addToMap(map, "stops", this.stops);
    return map;
  }

  @Override
  public void updateValues() {
    Collections.sort(this.stops);

    if (!this.stops.isEmpty()) {
      final GradientStop minStop = this.stops.get(0);
      GradientStop previousStop = minStop;
      previousStop.setPrevious(previousStop);
      for (int i = 1; i < this.stops.size(); i++) {
        final GradientStop stop = this.stops.get(i);
        stop.setPrevious(previousStop);

        previousStop = stop;
      }
      this.maxStop = previousStop;
    } else {
      this.minStop = NULL_STOP;
      this.maxStop = NULL_STOP;
    }
  }
}
