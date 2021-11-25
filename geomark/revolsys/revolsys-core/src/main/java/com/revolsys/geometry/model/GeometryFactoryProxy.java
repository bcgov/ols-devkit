package com.revolsys.geometry.model;

import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.HorizontalCoordinateSystemProxy;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;

public interface GeometryFactoryProxy extends HorizontalCoordinateSystemProxy {

  default BoundingBox convertBoundingBox(final BoundingBoxProxy boundingBoxProxy) {
    if (boundingBoxProxy != null) {
      final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
      if (boundingBox != null) {

        final GeometryFactory geometryFactory = getGeometryFactory();
        if (geometryFactory != null) {
          return boundingBox.bboxToCs(geometryFactory);
        }
      }
      return boundingBox;
    }
    return null;
  }

  default <G extends Geometry> G convertGeometry(final G geometry) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (geometryFactory != null) {
        return geometry.convertGeometry(geometryFactory);
      }
    }
    return geometry;
  }

  default <G extends Geometry> G convertGeometry(final G geometry, final int axisCount) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (geometryFactory != null) {
        return geometry.convertGeometry(geometryFactory, axisCount);
      }
    }
    return geometry;
  }

  default BoundingBox getAreaBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory != null) {
      return geometryFactory.getAreaBoundingBox();
    }
    return BoundingBox.empty();
  }

  default int getAxisCount() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return 2;
    } else {
      return geometryFactory.getAxisCount();
    }
  }

  default CoordinatesOperation getCoordinatesOperation(final GeometryFactoryProxy geometryFactory) {
    if (geometryFactory == null) {
      return null;
    } else {
      final GeometryFactory geometryFactoryThis = getGeometryFactory();
      if (geometryFactoryThis == null) {
        return null;
      } else {
        final GeometryFactory geometryFactoryOther = geometryFactory.getGeometryFactory();
        return geometryFactoryThis.getCoordinatesOperation(geometryFactoryOther);
      }
    }
  }

  @Override
  default <C extends CoordinateSystem> C getCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return null;
    } else {
      return geometryFactory.getCoordinateSystem();
    }
  }

  @Override
  default int getCoordinateSystemId() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return 0;
    } else {
      return geometryFactory.getCoordinateSystemId();
    }
  }

  default GeometryFactory getGeometryFactory() {
    return GeometryFactory.DEFAULT_3D;
  }

  @Override
  default <C extends CoordinateSystem> C getHorizontalCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return null;
    } else {
      return geometryFactory.getHorizontalCoordinateSystem();
    }
  }

  default GeometryFactory getNonZeroGeometryFactory(GeometryFactory geometryFactory) {
    final GeometryFactory geometryFactoryThis = getGeometryFactory();
    if (geometryFactory == null) {
      return geometryFactoryThis;
    } else {
      final int srid = geometryFactory.getHorizontalCoordinateSystemId();
      if (srid == 0) {
        final int geometrySrid = geometryFactoryThis.getHorizontalCoordinateSystemId();
        if (geometrySrid != 0) {
          geometryFactory = geometryFactory.convertSrid(geometrySrid);
        }
      }
      return geometryFactory;
    }
  }

  default boolean isHasHorizontalCoordinateSystem() {
    final CoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    return coordinateSystem != null;
  }

  default boolean isProjectionRequired(final GeometryFactory geometryFactory) {
    final GeometryFactory geometryFactoryThis = getGeometryFactory();
    if (geometryFactoryThis == null) {
      return false;
    } else {
      return geometryFactoryThis.isProjectionRequired(geometryFactory);
    }
  }

  default boolean isProjectionRequired(final GeometryFactoryProxy geometryFactoryProxy) {
    if (geometryFactoryProxy == null) {
      return false;
    } else {
      final GeometryFactory geometryFactory = geometryFactoryProxy.getGeometryFactory();
      return isProjectionRequired(geometryFactory);
    }
  }

  default boolean isSameCoordinateSystem(final GeometryFactory geometryFactory) {
    final GeometryFactory geometryFactory2 = getGeometryFactory();
    if (geometryFactory == null || geometryFactory2 == null) {
      return false;
    } else {
      return geometryFactory.isSameCoordinateSystem(geometryFactory2);
    }
  }

  default boolean isSameCoordinateSystem(final GeometryFactoryProxy proxy) {
    if (proxy == null) {
      return false;
    } else {
      final GeometryFactory geometryFactory = proxy.getGeometryFactory();
      final GeometryFactory geometryFactory2 = getGeometryFactory();
      if (geometryFactory == null || geometryFactory2 == null) {
        return false;
      } else {
        return geometryFactory.isSameCoordinateSystem(geometryFactory2);
      }
    }
  }

  default void notNullSameCs(final GeometryFactoryProxy geometryFactory) {
    if (geometryFactory == null) {
      throw new NullPointerException("Argument rectangle cannot be null");
    } else if (!isSameCoordinateSystem(geometryFactory)) {
      throw new IllegalArgumentException(
        "Rectangle operations require the same coordinate system this != rectangle\n  "
          + getHorizontalCoordinateSystemName() + "\n  "
          + geometryFactory.getHorizontalCoordinateSystemName());
    }
  }

  default <G extends Geometry> G toCoordinateSystem(final G geometry) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (geometryFactory != null) {
        if (!geometry.isSameCoordinateSystem(geometryFactory)) {
          return geometry.convertGeometry(geometryFactory);
        }
      }
    }
    return geometry;
  }

  default double toDoubleX(final int x) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.toDoubleX(x);
  }

  default double toDoubleY(final int y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.toDoubleY(y);
  }

  default double toDoubleZ(final int z) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.toDoubleZ(z);
  }

  default int toIntX(final double x) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.toIntX(x);
  }

  default int toIntY(final double y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.toIntY(y);
  }

  default int toIntZ(final double z) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.toIntZ(z);
  }

  default void writePrjFile(final Object target) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory != null) {
      geometryFactory.writePrjFile(target);
    }
  }
}
