package com.revolsys.geometry.geoid;

import java.util.List;

import org.jeometry.coordinatesystem.operation.ChainedCoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;
import org.jeometry.coordinatesystem.operation.gridshift.VerticalShiftOperation;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.io.IoFactory;
import com.revolsys.spring.resource.Resource;

public interface GeoidModel extends BoundingBoxProxy {

  static GeoidModel newGeoidModel(final Object source) {
    final MapEx properties = MapEx.EMPTY;
    return newGeoidModel(source, properties);
  }

  static GeoidModel newGeoidModel(final Object source, final MapEx properties) {
    final GeoidModelReaderFactory factory = IoFactory.factory(GeoidModelReaderFactory.class,
      source);
    if (factory == null) {
      return null;
    } else {
      final Resource resource = Resource.getResource(source);
      final GeoidModel dem = factory.newGeoidModel(resource, properties);
      return dem;
    }
  }

  default void addCoordinatesOperations(final List<CoordinatesOperation> operations,
    final GeoidModel targetGeoidModel) {
    if (this != targetGeoidModel) {
      final VerticalShiftOperation thisToEllipsoid = this.orthometricToGeodeticHeightOperation();
      final VerticalShiftOperation ellipsoidToTarget = targetGeoidModel
        .geodeticToOrthometricHeightOperation();
      operations.add(thisToEllipsoid);
      operations.add(ellipsoidToTarget);
    }
  }

  /**
   * Convert a geodetic (ellipsoid) height (h) to an orthometric (geoid) height (H).
   *
   * <pre>H = h - N</pre>
   *
   * @param point The point to convert.
   * @return True if the point was converted.
   */
  default boolean geodeticToOrthometricHeight(final CoordinatesOperationPoint point) {
    final double x = point.x;
    final double y = point.y;
    final double geoidHeight = getGeoidHeight(x, y);
    if (Double.isFinite(geoidHeight)) {
      point.z -= geoidHeight;
      return true;
    } else {
      return false;
    }
  }

  default VerticalShiftOperation geodeticToOrthometricHeightOperation() {
    return this::geodeticToOrthometricHeight;
  }

  default CoordinatesOperation getCoordinatesOperation(final GeoidModel targetGeoidModel) {
    if (this == targetGeoidModel) {
      return null;
    } else {
      final VerticalShiftOperation thisToEllipsoid = targetGeoidModel
        .orthometricToGeodeticHeightOperation();
      final VerticalShiftOperation ellipsoidToTarget = targetGeoidModel
        .geodeticToOrthometricHeightOperation();
      return new ChainedCoordinatesOperation(thisToEllipsoid, ellipsoidToTarget);
    }
  }

  /**
   * Get the height (N) of the geoid from the Ellipsoid.
   *
   * @param lon The point's longitude.
   * @param lon The point's latitude.
   */
  double getGeoidHeight(double lon, double lat);

  String getGeoidName();

  /**
   * Convert a orthometric (geoid) height (H) to an geodetic (ellipsoid) height(h).
   *
   * <pre>h = H + N</pre>
   *
   * @param point The point to convert.
   * @return True if the point was converted.
   */
  default boolean orthometricToGeodeticHeight(final CoordinatesOperationPoint point) {
    final double x = point.x;
    final double y = point.y;
    final double geoidHeight = getGeoidHeight(x, y);
    if (Double.isFinite(geoidHeight)) {
      point.z += geoidHeight;
      return true;
    } else {
      return false;
    }
  }

  default VerticalShiftOperation orthometricToGeodeticHeightOperation() {
    return this::orthometricToGeodeticHeight;
  }

}
