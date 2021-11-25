package com.revolsys.geometry.model;

import java.util.Arrays;

import org.jeometry.coordinatesystem.model.CoordinateSystem;

public class GeometryFactoryFloating extends GeometryFactory {

  public GeometryFactoryFloating(final GeometryFactories instances, final int axisCount) {
    super(instances, axisCount);
  }

  @Override
  public GeometryFactory convertAxisCount(final int axisCount) {
    if (axisCount == this.axisCount) {
      return this;
    } else {
      return this.instances.floating(axisCount);
    }
  }

  @Override
  public GeometryFactory convertCoordinateSystem(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return this;
    } else {
      if (coordinateSystem == this.coordinateSystem) {
        return this;
      } else {
        final GeometryFactories instances = instances(coordinateSystem);
        return instances.floating(this.axisCount);
      }
    }
  }

  @Override
  public GeometryFactory convertToFixed(final double defaultScale) {
    final double[] scales = new double[this.axisCount];
    Arrays.fill(scales, defaultScale);
    return convertScales(scales);
  }

  @Override
  public GeometryFactory newWithOffsets(final double offsetX, final double offsetY,
    final double offsetZ) {
    return new GeometryFactoryWithOffsets(this.instances, offsetX, 0, offsetY, 0, offsetZ, 0);
  }

  @Override
  public GeometryFactory newWithOffsetsAndScales(final double offsetX, final double scaleX,
    final double offsetY, final double scaleY, final double offsetZ, final double scaleZ) {
    return new GeometryFactoryWithOffsets(this.instances, offsetX, scaleX, offsetY, scaleY, offsetZ,
      scaleZ);
  }
}
