package com.revolsys.geometry.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jeometry.coordinatesystem.model.CoordinateSystem;

public class GeometryFactoryFixed extends GeometryFactory {

  protected double resolutionX = 0;

  protected double resolutionY = 0;

  protected double resolutionZ = 0;

  protected double[] scales;

  protected double scaleX = 0;

  protected double scaleY = 0;

  protected double scaleZ = 0;

  public GeometryFactoryFixed(final GeometryFactories instances, final int axisCount,
    final double... scales) {
    super(instances, axisCount);
    initScales(scales);
  }

  @Override
  public GeometryFactory convertAxisCount(final int axisCount) {
    return this.instances.fixed(axisCount, this.scales);
  }

  @Override
  public GeometryFactory convertCoordinateSystem(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return this;
    } else {
      if (coordinateSystem == this.coordinateSystem) {
        return this;
      } else {
        return instances(coordinateSystem).fixed(this.axisCount, this.scales);
      }
    }
  }

  @Override
  public GeometryFactory convertSrid(final int coordinateSystemId) {
    if (coordinateSystemId == this.coordinateSystemId) {
      return this;
    } else {
      return fixed(coordinateSystemId, this.axisCount, this.scales);
    }
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
      final double[] scales = Arrays.copyOf(this.scales, this.scales.length);
      for (int i = 0; i < scales.length; i++) {
        final double scale = scales[i];
        if (scale <= 0) {
          scales[i] = defaultScale;
        }
      }
      return convertScales(scales);
    } else {
      return this;
    }
  }

  @Override
  public boolean equalsScales(final double[] scales) {
    final int minLength = Math.min(this.scales.length, scales.length);
    for (int i = 0; i < minLength; i++) {
      final double scale1 = this.scales[i];
      final double scale2 = scales[i];
      if (scale1 != scale2) {
        return false;
      }
    }
    return true;
  }

  /**
   * Creates a deep copy of the input {@link Geometry}.
   * <p>
   * This is a convenient way to change the <tt>LineString</tt>
   * used to represent a geometry, or to change the
   * factory used for a geometry.
   * <p>
   * {@link Geometry#clone()} can also be used to make a deep copy,
   * but it does not allow changing the LineString type.
   *
   * @return a deep copy of the input geometry, using the LineString type of this factory
   *
   * @see Geometry#clone()
   */
  @Override
  public Geometry geometry(final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else {
      final int coordinateSystemId = getHorizontalCoordinateSystemId();
      final int geometrySrid = geometry.getHorizontalCoordinateSystemId();
      if (coordinateSystemId == 0 && geometrySrid != 0) {
        final GeometryFactory geometryFactory = GeometryFactory.fixed(geometrySrid, this.axisCount,
          this.scales);
        return geometryFactory.geometry(geometry);
      } else if (coordinateSystemId != 0 && geometrySrid != 0
        && geometrySrid != coordinateSystemId) {
        if (geometry instanceof Point) {
          return geometry.newGeometry(this);
        } else if (geometry instanceof LineString) {
          return geometry.newGeometry(this);
        } else if (geometry instanceof Polygon) {
          return geometry.newGeometry(this);
        } else if (geometry instanceof Punctual) {
          final List<Geometry> geometries = new ArrayList<>();
          addGeometries(geometries, geometry);
          return punctual(geometries);
        } else if (geometry instanceof Lineal) {
          final List<Geometry> geometries = new ArrayList<>();
          addGeometries(geometries, geometry);
          return lineal(geometries);
        } else if (geometry instanceof Polygonal) {
          final List<Geometry> geometries = new ArrayList<>();
          addGeometries(geometries, geometry);
          return polygonal(geometries);
        } else if (geometry.isGeometryCollection()) {
          final List<Geometry> geometries = new ArrayList<>();
          addGeometries(geometries, geometry);
          return geometryCollection(geometries);
        } else {
          return geometry.newGeometry(this);
        }
      } else if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        return point.newGeometry(this);
      } else if (geometry instanceof LinearRing) {
        final LinearRing linearRing = (LinearRing)geometry;
        return linearRing.newGeometry(this);
      } else if (geometry instanceof LineString) {
        final LineString lineString = (LineString)geometry;
        return lineString.newGeometry(this);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        return polygon(polygon);
      } else if (geometry instanceof Punctual) {
        final List<Geometry> geometries = new ArrayList<>();
        addGeometries(geometries, geometry);
        return punctual(geometries);
      } else if (geometry instanceof Lineal) {
        final List<Geometry> geometries = new ArrayList<>();
        addGeometries(geometries, geometry);
        return lineal(geometries);
      } else if (geometry instanceof Polygonal) {
        final List<Geometry> geometries = new ArrayList<>();
        addGeometries(geometries, geometry);
        return polygonal(geometries);
      } else if (geometry instanceof GeometryCollection) {
        final List<Geometry> geometries = new ArrayList<>();
        addGeometries(geometries, geometry);
        return geometryCollection(geometries);
      } else {
        return null;
      }
    }
  }

  /**
   * Returns the maximum number of significant digits provided by this
   * precision model.
   * Intended for use by routines which need to print out
   * decimal representations of precise values .
   * <p>
   * This method would be more correctly called
   * <tt>getMinimumDecimalPlaces</tt>,
   * since it actually computes the number of decimal places
   * that is required to correctly display the full
   * precision of an ordinate value.
   * <p>
   * Since it is difficult to compute the required number of
   * decimal places for scale factors which are not powers of 10,
   * the algorithm uses a very rough approximation in this case.
   * This has the side effect that for scale factors which are
   * powers of 10 the value returned is 1 greater than the true value.
   *
   *
   * @return the maximum number of decimal places provided by this precision model
   */
  @Override
  public int getMaximumSignificantDigits() {
    int maxSigDigits = 16;
    if (isFloating()) {
      maxSigDigits = 16;
    } else {
      maxSigDigits = 1 + (int)Math.ceil(Math.log(this.scaleX) / Math.log(10));
    }
    return maxSigDigits;
  }

  @Override
  public double getResolution(final int axisIndex) {
    final double scale = getScale(axisIndex);
    if (scale <= 0) {
      return 0;
    } else {
      return 1 / scale;
    }
  }

  @Override
  public double getResolutionX() {
    return this.resolutionX;
  }

  @Override
  public double getResolutionXy() {
    return this.resolutionX;
  }

  @Override
  public double getResolutionY() {
    return this.resolutionY;
  }

  @Override
  public double getResolutionZ() {
    return this.resolutionZ;
  }

  @Override
  public double getScale(final int axisIndex) {
    switch (axisIndex) {
      case 0:
        return this.scaleX;
      case 1:
        return this.scaleY;
      case 2:
        return this.scaleZ;
      default:
        if (axisIndex < 0 || axisIndex >= this.scales.length) {
          return 0;
        } else {
          return this.scales[axisIndex - 1];
        }
    }
  }

  @Override
  public double getScaleX() {
    return this.scaleX;
  }

  @Override
  public double getScaleXY() {
    return this.scaleX;
  }

  @Override
  public double getScaleY() {
    return this.scaleY;
  }

  @Override
  public double getScaleZ() {
    return this.scaleZ;
  }

  private void initScales(final double... scales) {
    this.scales = new double[this.axisCount];
    for (int axisIndex = 0; axisIndex < this.axisCount && axisIndex < scales.length; axisIndex++) {
      final double scale = scales[axisIndex];
      this.scales[axisIndex] = scale;
    }

    this.scaleX = this.scales[0];
    this.resolutionX = toResolution(this.scaleX);

    this.scaleY = this.scales[1];
    this.resolutionY = toResolution(this.scaleY);

    if (this.axisCount > 2) {
      this.scaleZ = this.scales[2];
      this.resolutionZ = toResolution(this.scaleZ);
    }
  }

  @Override
  public boolean isFloating() {
    return this.scaleX == 0;
  }

  @Override
  public boolean isMoreDetailed(final GeometryFactory geometryFactory) {
    if (isFloating()) {
      return !geometryFactory.isFloating();
    } else if (geometryFactory.isFloating()) {
      return false;
    } else {
      if (this.resolutionX < geometryFactory.getResolutionX()) {
        return true;
      } else if (this.resolutionY < geometryFactory.getResolutionY()) {
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public void makePrecise(final double[] values, final double[] valuesPrecise) {
    final int axisCount = this.axisCount;
    for (int i = 0; i < valuesPrecise.length; i++) {
      final int axisIndex = i % axisCount;
      valuesPrecise[i] = makePrecise(axisIndex, values[i]);
    }
  }

  @Override
  public double makePrecise(final int axisIndex, final double value) {
    final double scale = getScale(axisIndex);
    if (scale > 0 && Double.isFinite(value)) {
      final double multiple = value * scale;
      final double scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public void makePrecise(final int axisCount, final double... coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      final double value = coordinates[i];
      final int axisIndex = i % axisCount;
      final double scale = getScale(axisIndex);
      if (scale > 0) {
        final double multiple = value * scale;
        final long scaledValue = Math.round(multiple);
        final double preciseValue = scaledValue / scale;
        coordinates[i] = preciseValue;
      }
    }
  }

  @Override
  public double makePreciseCeil(final int axisIndex, final double value) {
    final double scale = getScale(axisIndex);
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.ceil(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makePreciseFloor(final int axisIndex, final double value) {
    final double scale = getScale(axisIndex);
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.floor(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makeXPrecise(final double value) {
    final double scale = this.scaleX;
    if (scale > 0 && Double.isFinite(value)) {
      final double multiple = value * scale;
      final double scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makeXPreciseCeil(final double value) {
    final double scale = this.scaleX;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.ceil(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makeXPreciseFloor(final double value) {
    final double scale = this.scaleX;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.floor(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makeXyPrecise(final double value) {
    final double scale = this.scaleX;
    if (scale > 0 && Double.isFinite(value)) {
      final double multiple = value * scale;
      final double scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makeXyPreciseCeil(final double value) {
    final double scale = this.scaleX;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.ceil(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makeXyPreciseFloor(final double value) {
    final double scale = this.scaleX;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.floor(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makeYPrecise(final double value) {
    final double scale = this.scaleY;
    if (scale > 0 && Double.isFinite(value)) {
      final double multiple = value * scale;
      final double scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makeYPreciseCeil(final double value) {
    final double scale = this.scaleY;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.ceil(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makeYPreciseFloor(final double value) {
    final double scale = this.scaleY;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.floor(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makeZPrecise(final double value) {
    final double scale = this.scaleZ;
    if (scale > 0 && Double.isFinite(value)) {
      final double multiple = value * scale;
      final double scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makeZPreciseCeil(final double value) {
    final double scale = this.scaleZ;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.ceil(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double makeZPreciseFloor(final double value) {
    final double scale = this.scaleZ;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.floor(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  @Override
  public double[] newScales(final int axisCount) {
    final double[] scales = new double[axisCount];
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      double scale;
      if (axisIndex < this.scales.length) {
        scale = this.scales[axisIndex];
      } else {
        scale = this.scales[this.scales.length - 1];
      }
      scales[axisIndex] = scale;
    }
    return scales;
  }

  @Override
  public GeometryFactory newWithOffsets(final double offsetX, final double offsetY,
    final double offsetZ) {
    if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
      return this;
    } else {
      return new GeometryFactoryWithOffsets(this.instances, offsetX, this.scaleX, offsetY,
        this.scaleY, offsetZ, this.scaleZ);
    }
  }

  @Override
  public GeometryFactory newWithOffsetsAndScales(final double offsetX, final double scaleX,
    final double offsetY, final double scaleY, final double offsetZ, final double scaleZ) {
    if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
      return convertScales(scaleX, scaleY, scaleZ);
    } else {
      return new GeometryFactoryWithOffsets(this.instances, offsetX, scaleX, offsetY, scaleY,
        offsetZ, scaleZ);
    }
  }

  @Override
  public double toDoubleX(final int x) {
    return x / this.scaleX;
  }

  @Override
  public double toDoubleY(final int y) {
    return y / this.scaleY;
  }

  @Override
  public double toDoubleZ(final int z) {
    return z / this.scaleZ;
  }

  @Override
  public int toIntX(final double x) {
    if (Double.isFinite(x)) {
      return (int)Math.round(x * this.scaleX);
    } else {
      return Integer.MIN_VALUE;
    }
  }

  @Override
  public int toIntY(final double y) {
    if (Double.isFinite(y)) {
      return (int)Math.round(y * this.scaleY);
    } else {
      return Integer.MIN_VALUE;
    }
  }

  @Override
  public int toIntZ(final double z) {
    if (Double.isFinite(z)) {
      return (int)Math.round(z * this.scaleZ);
    } else {
      return Integer.MIN_VALUE;
    }
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder(super.toString());
    string.append(", scales=");
    string.append(Arrays.toString(this.scales));
    return string.toString();
  }

}
