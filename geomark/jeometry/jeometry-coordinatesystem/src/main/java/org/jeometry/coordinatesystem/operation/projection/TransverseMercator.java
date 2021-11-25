package org.jeometry.coordinatesystem.operation.projection;

import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.NormalizedParameterNames;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;

public abstract class TransverseMercator extends AbstractCoordinatesProjection {

  private final String name;

  /** Scale Factor. */
  protected final double ko;

  /** False Easting. */
  protected final double xo;

  /** False Northing. */
  protected final double yo;

  protected final double λo;

  protected final double a;

  protected final double b;

  protected final double φo;

  /**
   * Construct a new TransverseMercator projection.
   *
   * @param coordinateSystem The coordinate system.
   */
  public TransverseMercator(final ProjectedCoordinateSystem coordinateSystem) {
    this(//
      coordinateSystem.getCoordinateSystemName(), //
      coordinateSystem.getEllipsoid(), //
      coordinateSystem.getDoubleParameter(NormalizedParameterNames.CENTRAL_MERIDIAN), //
      coordinateSystem.getDoubleParameter(NormalizedParameterNames.LATITUDE_OF_ORIGIN), //
      coordinateSystem.getDoubleParameter(NormalizedParameterNames.SCALE_FACTOR), //
      coordinateSystem.getDoubleParameter(NormalizedParameterNames.FALSE_EASTING), //
      coordinateSystem.getDoubleParameter(NormalizedParameterNames.FALSE_NORTHING) //
    );
  }

  public TransverseMercator(final String name, final Ellipsoid ellipsoid,
    final double longitudeOrigin, final double latitudeOrigin, final double ko, final double xo,
    final double yo) {
    this.name = name;
    this.λo = Math.toRadians(longitudeOrigin);
    this.φo = Math.toRadians(latitudeOrigin);
    this.ko = ko;
    this.xo = xo;
    this.yo = yo;
    this.a = ellipsoid.getSemiMajorAxis();
    this.b = ellipsoid.getSemiMinorAxis();
  }

  /**
   * Return the string representation of the projection.
   *
   * @return The string.
   */
  @Override
  public String toString() {
    return this.name;
  }
}
