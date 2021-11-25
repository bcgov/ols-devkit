package com.revolsys.geometry.model;

import java.util.Arrays;

import org.jeometry.coordinatesystem.model.CoordinateSystem;

import com.revolsys.record.io.format.json.JsonObject;

public class GeometryFactoryWithOffsets extends GeometryFactoryFixed {

  public static GeometryFactory newWithOffsets(final int coordinateSystemId, final double offsetX,
    final double scaleX, final double offsetY, final double scaleY, final double offsetZ,
    final double scaleZ) {
    final GeometryFactories instances = GeometryFactory.instances(coordinateSystemId);
    return instances.fixedWithOffsets(offsetX, scaleX, offsetY, scaleY, offsetZ, scaleZ);
  }

  private final double offsetX;

  private final double offsetY;

  private final double offsetZ;

  public GeometryFactoryWithOffsets(final GeometryFactories instances, final double offsetX,
    final double scaleX, final double offsetY, final double scaleY, final double offsetZ,
    final double scaleZ) {
    super(instances, 3, scaleX, scaleY, scaleZ);
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.offsetZ = offsetZ;
  }

  @Override
  public GeometryFactory convertCoordinateSystem(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return this;
    } else {
      if (coordinateSystem == this.coordinateSystem) {
        return this;
      } else {
        return instances(coordinateSystem).fixedWithOffsets(this.offsetX, this.scaleX, this.offsetY,
          this.scaleY, this.offsetZ, this.scaleZ);
      }
    }
  }

  @Override
  public GeometryFactory convertScales(final double... scales) {
    double scaleX = 0;
    double scaleY = 0;
    final double scaleZ = 0;
    if (scales != null) {
      if (scales.length >= 1) {
        scaleX = scales[0];
      }
      if (scales.length >= 2) {
        scaleY = scales[1];
      }
      if (scales.length >= 3) {
        scaleY = scales[2];
      }
    }
    return this.instances.fixedWithOffsets(this.offsetX, scaleX, this.offsetY, scaleY, this.offsetZ,
      scaleZ);
  }

  @Override
  public GeometryFactory convertToFixed(final double defaultScale) {
    boolean conversionRequired = false;
    for (final double scale : this.scales) {
      if (scale <= 0) {
        conversionRequired = true;
      }
    }
    if (conversionRequired) {
      final double[] scales = Arrays.copyOf(this.scales, 3);
      for (int i = 0; i < scales.length; i++) {
        final double scale = scales[i];
        if (scale <= 0) {
          scales[i] = defaultScale;
        }
      }
      return new GeometryFactoryWithOffsets(this.instances, this.offsetX, scales[0], this.offsetY,
        scales[1], this.offsetZ, scales[2]);
    } else {
      return this;
    }
  }

  @Override
  public double getOffset(final int axisIndex) {
    switch (axisIndex) {
      case 0:
        return this.offsetX;
      case 1:
        return this.offsetY;
      case 2:
        return this.offsetZ;
      default:
        return 0;
    }
  }

  @Override
  public double getOffsetX() {
    return this.offsetX;
  }

  @Override
  public double getOffsetY() {
    return this.offsetY;
  }

  @Override
  public double getOffsetZ() {
    return this.offsetZ;
  }

  @Override
  public double toDoubleX(final int x) {
    return this.offsetX + x / this.scaleX;
  }

  @Override
  public double toDoubleY(final int y) {
    return this.offsetY + y / this.scaleY;
  }

  @Override
  public double toDoubleZ(final int z) {
    return this.offsetZ + z / this.scaleZ;
  }

  @Override
  public int toIntX(final double x) {
    if (Double.isFinite(x)) {
      return (int)Math.round((x - this.offsetX) / this.resolutionX);
    } else {
      return Integer.MIN_VALUE;
    }
  }

  @Override
  public int toIntY(final double y) {
    if (Double.isFinite(y)) {
      return (int)Math.round((y - this.offsetY) / this.resolutionY);
    } else {
      return Integer.MIN_VALUE;
    }
  }

  @Override
  public int toIntZ(final double z) {
    if (Double.isFinite(z)) {
      return (int)Math.round((z - this.offsetZ) / this.resolutionZ);
    } else {
      return Integer.MIN_VALUE;
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "offsetX", this.offsetX, 0.0);
    addToMap(map, "offsetY", this.offsetY, 0.0);
    addToMap(map, "offsetZ", this.offsetZ, 0.0);
    return map;
  }
}
