package org.jeometry.coordinatesystem.operation.projection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.jeometry.coordinatesystem.model.CoordinateOperationMethod;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;

public final class ProjectionFactory {
  /** The map from projection names to projection classes. */
  private static final Map<String, Class<? extends CoordinatesProjection>> projectionClasses = new HashMap<>();

  static {
    registerCoordinatesProjection(CoordinateOperationMethod.LAMBERT_CONIC_CONFORMAL_2SP_BELGIUM,
      LambertConicConformal.class);
  }

  public static CoordinatesProjection newCoordinatesProjection(
    final ProjectedCoordinateSystem coordinateSystem) {
    final CoordinateOperationMethod coordinateOperationMethod = coordinateSystem
      .getCoordinateOperationMethod();
    final String projectionName = coordinateOperationMethod.getNormalizedName();
    synchronized (projectionClasses) {
      final Class<? extends CoordinatesProjection> projectionClass = projectionClasses
        .get(projectionName);
      if (projectionClass == null) {
        return null;
      } else {
        try {
          final Constructor<? extends CoordinatesProjection> constructor = projectionClass
            .getConstructor(ProjectedCoordinateSystem.class);
          final CoordinatesProjection coordinateProjection = constructor
            .newInstance(coordinateSystem);
          return coordinateProjection;
        } catch (final NoSuchMethodException e) {
          throw new IllegalArgumentException("Constructor " + projectionClass + "("
            + ProjectedCoordinateSystem.class.getName() + ") does not exist");
        } catch (final InstantiationException e) {
          throw new IllegalArgumentException(projectionClass + " cannot be instantiated", e);
        } catch (final IllegalAccessException e) {
          throw new IllegalArgumentException(projectionClass + " cannot be instantiated", e);
        } catch (final InvocationTargetException e) {
          final Throwable cause = e.getCause();
          if (cause instanceof RuntimeException) {
            throw (RuntimeException)cause;
          } else if (cause instanceof Error) {
            throw (Error)cause;
          } else {
            throw new IllegalArgumentException(projectionClass + " cannot be instantiated", cause);
          }
        }
      }
    }
  }

  /**
   * Register a projection for the named projection.
   *
   * @param name The name.
   * @param projectionClass The projection class.
   */
  public static void registerCoordinatesProjection(final String name,
    final Class<? extends CoordinatesProjection> projectionClass) {
    projectionClasses.put(name, projectionClass);
  }

  private ProjectionFactory() {
  }
}
