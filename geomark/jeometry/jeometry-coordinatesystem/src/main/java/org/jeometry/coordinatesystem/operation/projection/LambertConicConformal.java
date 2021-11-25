package org.jeometry.coordinatesystem.operation.projection;

import org.jeometry.common.math.Angle;
import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.NormalizedParameterNames;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

public class LambertConicConformal extends AbstractCoordinatesProjection {
  private final double a;

  private final double e;

  private final double ee;

  private final double f;

  /** The central origin. */
  private final double λ0;

  private final double n;

  private final double rho0;

  private final double x0;

  private final double y0;

  public LambertConicConformal(final ProjectedCoordinateSystem cs) {
    final double latitudeOfProjection = cs
      .getDoubleParameter(NormalizedParameterNames.LATITUDE_OF_ORIGIN);
    final double centralMeridian = cs.getDoubleParameter(NormalizedParameterNames.CENTRAL_MERIDIAN);
    final double firstStandardParallel = cs
      .getDoubleParameter(NormalizedParameterNames.STANDARD_PARALLEL_1);
    final double secondStandardParallel = cs
      .getDoubleParameter(NormalizedParameterNames.STANDARD_PARALLEL_2);

    final Ellipsoid ellipsoid = cs.getEllipsoid();
    this.x0 = cs.getDoubleParameter(NormalizedParameterNames.FALSE_EASTING);
    this.y0 = cs.getDoubleParameter(NormalizedParameterNames.FALSE_NORTHING);
    this.λ0 = Math.toRadians(centralMeridian);
    this.a = ellipsoid.getSemiMajorAxis();
    this.e = ellipsoid.getEccentricity();
    this.ee = this.e * this.e;

    final double φ0 = Math.toRadians(latitudeOfProjection);
    final double φ1 = Math.toRadians(firstStandardParallel);
    final double φ2 = Math.toRadians(secondStandardParallel);
    final double m1 = m(φ1);
    final double logM1 = Math.log(m1);
    final double m2 = m(φ2);
    final double logM2 = Math.log(m2);
    final double t0 = t(φ0);
    final double t1 = t(φ1);
    final double t2 = t(φ2);

    final double logT1 = Math.log(t1);
    final double logT2 = Math.log(t2);
    this.n = (logM1 - logM2) / (logT1 - logT2);
    this.f = m1 / (this.n * Math.pow(t1, this.n));
    this.rho0 = this.a * this.f * Math.pow(t0, this.n);
  }

  @Override
  public void inverse(final CoordinatesOperationPoint point) {

    double dX = point.x - this.x0;
    double dY = point.y - this.y0;

    double rho0 = this.rho0;
    if (this.n < 0) {
      rho0 = -rho0;
      dX = -dX;
      dY = -dY;
    }
    final double theta = Math.atan(dX / (rho0 - dY));
    double rho = Math.sqrt(dX * dX + Math.pow(rho0 - dY, 2));
    if (this.n < 0) {
      rho = -rho;
    }
    final double t = Math.pow(rho / (this.a * this.f), 1 / this.n);
    double φ = Angle.PI_OVER_2 - 2 * Math.atan(t);
    double delta = 10e010;
    do {

      final double sinPhi = Math.sin(φ);
      final double eSinPhi = this.e * sinPhi;
      final double φ1 = Angle.PI_OVER_2
        - 2 * Math.atan(t * Math.pow((1 - eSinPhi) / (1 + eSinPhi), this.e / 2));
      delta = Math.abs(φ1 - φ);
      φ = φ1;
    } while (!Double.isNaN(φ) && delta > 1.0e-011);
    final double λ = theta / this.n + this.λ0;

    point.x = λ;
    point.y = φ;
  }

  private double m(final double φ) {
    final double sinPhi = Math.sin(φ);
    return Math.cos(φ) / Math.sqrt(1 - this.ee * sinPhi * sinPhi);
  }

  @Override
  public void project(final CoordinatesOperationPoint point) {
    final double λ = point.x;
    final double φ = point.y;

    final double t = t(φ);
    final double rho = this.a * this.f * Math.pow(t, this.n);

    final double theta = this.n * (λ - this.λ0);
    point.x = this.x0 + rho * Math.sin(theta);
    point.y = this.y0 + this.rho0 - rho * Math.cos(theta);
  }

  private double t(final double φ) {
    final double sinPhi = Math.sin(φ);
    final double eSinPhi = this.e * sinPhi;

    final double t = Math.tan(Angle.PI_OVER_4 - φ / 2)
      / Math.pow((1 - eSinPhi) / (1 + eSinPhi), this.e / 2);
    return t;
  }
}
