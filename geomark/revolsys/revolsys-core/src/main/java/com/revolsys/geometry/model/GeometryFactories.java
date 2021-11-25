package com.revolsys.geometry.model;

import java.util.ArrayList;
import java.util.List;

import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;
import org.jeometry.coordinatesystem.model.systems.EsriCoordinateSystems;

public class GeometryFactories {

  private final CoordinateSystem coordinateSystem;

  private final int coordinateSystemId;

  private final GeometryFactory[] floatingByAxisCount = new GeometryFactory[3];

  @SuppressWarnings("unchecked")
  private final List<GeometryFactory>[] fixedByAxisCount = new List[3];

  public GeometryFactories(final CoordinateSystem coordinateSystem) {
    this.coordinateSystem = coordinateSystem;
    this.coordinateSystemId = coordinateSystem.getCoordinateSystemId();
  }

  public GeometryFactories(final int coordinateSystemId) {
    this.coordinateSystemId = coordinateSystemId;
    CoordinateSystem coordinateSystem = null;
    if (coordinateSystemId > 0) {
      coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(coordinateSystemId);
      if (coordinateSystem == null) {
        coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(coordinateSystemId);
      }
    }
    this.coordinateSystem = coordinateSystem;
  }

  public synchronized GeometryFactory fixed(final int axisCount, final double... scales) {
    if (axisCount < 2 || axisCount > 4) {
      throw new IllegalArgumentException("AxisCount must be in the range 2..4 not " + axisCount);
    } else {
      if (scales == null) {
        return floating(axisCount);
      } else {
        boolean allZero = true;
        for (final double scale : scales) {
          if (scale > 0) {
            allZero = false;
          }
        }
        if (allZero) {
          return floating(axisCount);
        }
      }
      final int index = axisCount - 2;
      List<GeometryFactory> geometryFactories;
      synchronized (this.fixedByAxisCount) {

        geometryFactories = this.fixedByAxisCount[index];
        if (geometryFactories == null) {
          synchronized (this.fixedByAxisCount) {
            if (geometryFactories == null) {
              geometryFactories = new ArrayList<>();
              this.fixedByAxisCount[index] = geometryFactories;
            }
          }
        }
      }
      synchronized (geometryFactories) {
        for (final GeometryFactory matchFactory : geometryFactories) {
          if (matchFactory.equalsScales(scales)) {
            return matchFactory;
          }
        }
        final GeometryFactory geometryFactory = new GeometryFactoryFixed(this, axisCount, scales);
        geometryFactories.add(geometryFactory);
        return geometryFactory;
      }
    }
  }

  // TODO cache offsets?
  public GeometryFactory fixedWithOffsets(final double offsetX, final double scaleX,
    final double offsetY, final double scaleY, final double offsetZ, final double scaleZ) {
    if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
      return fixed(3, scaleX, scaleY, scaleZ);
    } else {
      return new GeometryFactoryWithOffsets(this, offsetX, scaleX, offsetY, scaleY, offsetZ,
        scaleZ);
    }
  }

  public synchronized GeometryFactory floating(final int axisCount) {
    if (axisCount < 2 || axisCount > 4) {
      throw new IllegalArgumentException("AxisCount must be in the range 2..4 not " + axisCount);
    } else {
      final int index = axisCount - 2;
      synchronized (this.floatingByAxisCount) {
        GeometryFactory geometryFactory = this.floatingByAxisCount[index];
        if (geometryFactory == null) {
          geometryFactory = new GeometryFactoryFloating(this, axisCount);
          this.floatingByAxisCount[index] = geometryFactory;
        }
        return geometryFactory;
      }
    }
  }

  public CoordinateSystem getCoordinateSystem() {
    return this.coordinateSystem;
  }

  public int getCoordinateSystemId() {
    return this.coordinateSystemId;
  }

  @Override
  public String toString() {
    if (this.coordinateSystem == null) {
      return "no Coordinate System";
    } else {
      return this.coordinateSystem.toString();
    }
  }

}
