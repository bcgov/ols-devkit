package org.jeometry.coordinatesystem.operation.projection;

import org.jeometry.common.math.Angle;
import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.NormalizedParameterNames;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

public class Mercator1SP extends AbstractCoordinatesProjection {

  private final double a;

  private final double e;

  private final double eOver2;

  private final double λ0; // central meridian

  private final double x0;

  private final double y0;

  public Mercator1SP(final ProjectedCoordinateSystem cs) {
    final double centralMeridian = cs.getDoubleParameter(NormalizedParameterNames.CENTRAL_MERIDIAN);

    final Ellipsoid ellipsoid = cs.getEllipsoid();
    this.x0 = cs.getDoubleParameter(NormalizedParameterNames.FALSE_EASTING);
    this.y0 = cs.getDoubleParameter(NormalizedParameterNames.FALSE_NORTHING);
    this.λ0 = Math.toRadians(centralMeridian);
    this.a = ellipsoid.getSemiMajorAxis();
    this.e = ellipsoid.getEccentricity();
    this.eOver2 = this.e / 2;

  }

  @Override
  public void inverse(final CoordinatesOperationPoint point) {
    final double dX = point.x - this.x0;
    final double dY = point.y - this.y0;

    final double λ = dX / this.a + this.λ0;

    final double t = Math.pow(Math.E, -dY / this.a);
    // TODO φ
    double φ = Angle.PI_OVER_2 - 2 * Math.atan(t);
    double delta = 10e010;
    do {
      final double eSinPhi = this.e * Math.sin(φ);
      final double φ1 = Angle.PI_OVER_2
        - 2 * Math.atan(t * Math.pow((1 - eSinPhi) / (1 + eSinPhi), this.eOver2));
      delta = Math.abs(φ1 - φ);
      φ = φ1;
    } while (delta > 1.0e-011);

    point.x = λ;
    point.y = φ;
  }

  @Override
  public void project(final CoordinatesOperationPoint point) {
    final double λ = point.x;
    final double φ = point.y;

    final double a2 = this.a;
    final double eSinPhi = this.e * Math.sin(φ);
    point.x = a2 * (λ - this.λ0);
    point.y = a2 * Math.log(
      Math.tan(Angle.PI_OVER_4 + φ / 2) * Math.pow((1 - eSinPhi) / (1 + eSinPhi), this.eOver2));
  }

}
