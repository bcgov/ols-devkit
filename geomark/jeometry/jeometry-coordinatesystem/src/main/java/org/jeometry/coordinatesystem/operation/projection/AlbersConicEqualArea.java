package org.jeometry.coordinatesystem.operation.projection;

import org.jeometry.common.math.Angle;
import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.NormalizedParameterNames;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

/**
 * <p> * (EPSG dataset coordinate operation method code 9822)
 * </p>
 * <p>
 * To derive the projected coordinates of a point, geodetic latitude (φ) is
 * converted to authalic latitude (ß). The formulas to convert geodetic latitude
 * and longitude (φ, λ) to Easting (E) and Northing (N) are:
 * </p>
 *
 * <pre>
 * Easting (E) = EF + (ρ sin θ)
 * Northing(N) = NF +ρO –(ρ cosθ)
 * </pre>
 * <p>
 * where
 * </p>
 *
 * <pre>
 * θ =n (λ–λO)
 * ρ =[a (C–nα)0.5]/n ρO =[a (C–nαO)0.5]/n
 * </pre>
 * <p>
 * and
 * </p>
 *
 * <pre>
 * C = m12 + (n α1)
 * n =(m12 –m22)/(α2 –α1)
 * m1 = cos φ1 / (1 – e2sin2φ1)0.5
 * m2 = cos φ2 / (1 – e2sin2φ2)0.5
 * α = (1 – e2) {[sinφ / (1 – e2sin2φ)] – [1/(2e)] ln [(1 – esinφ) / (1 + esinφ)]}
 * αO = (1 – e2) {[sinφO / (1 – e2sin2φO)] – [1/(2e)] ln [(1 – e sinφO) / (1 + e sinφO)]}
 * α1 = (1 – e2) {[sinφ1 / (1 – e2sin2φ1)] – [1/(2e)] ln [(1 – e sinφ1) / (1 + e sinφ1)]}
 * α2 = (1 – e2) {[sinφ2 / (1 – e2sin2φ2)] – [1/(2e)] ln [(1 – e sinφ2) / (1 + e sinφ2)]}
 * </pre>
 * <p>
 * The reverse formulas to derive the geodetic latitude and longitude of a point
 * from its Easting and Northing values are: φ = ß' + (e2/3 + 31e4/180 +
 * 517e6/5040) sin 2ß'] + [(23e4/360 + 251e6/3780) sin 4ß'] + [(761e6/45360) sin
 * 6ß']
 * </p>
 *
 * <pre>
 * λ = λO + (θ / n)
 * </pre>
 * <p>
 * where
 * </p>
 *
 * <pre>
 * ß'= asin(α'/{1–[(1–e2)/(2 e)] ln[(1–e)/(1+e)] α'= [C–(ρ2 n2 /a2)]/n
 * ρ= {(E–EF)2 +[ρO –(N–NF)]2 }0.5
 * θ= atan[(E–EF)/[ρO –(N–NF)]
 * </pre>
 * <p>
 * and C, n and ρO are as in the forward equations.
 * </p>
 */
public class AlbersConicEqualArea extends AbstractCoordinatesProjection {

  /** Constant c = sq(m(φ1) + n * q(φ1) */
  private final double c;

  private final double e;

  /** sq(e). */
  private final double ePow2;

  /** Central Meridian. */
  private final double λo;

  /** Constant n = ( sq(m(φ1)) - sq(m(φ2) ) /( q(φ2) - q(φ1) ) */
  private final double n;

  /** Lattitude of CoordinateOperationMethod. */
  private final double φo;

  /** First standard parallel. */
  private final double φ1;

  /** Second standard parallel. */
  private final double φ2;

  /** Constant ρo = semiMajorAxis * sqrt( C - n * q(φo) ) / n. */
  private final double ρo;

  private final double a;

  /** The spheriod. */
  private final Ellipsoid ellipsoid;

  /** The false Easting. */
  private final double xo;

  /** The false Northing. */
  private final double yo;

  private final double aPow2;

  private final double nPow2;

  private final double oneOver2e;

  public AlbersConicEqualArea(final ProjectedCoordinateSystem cs) {
    final double firstStandardParallel = cs
      .getDoubleParameter(NormalizedParameterNames.STANDARD_PARALLEL_1);
    final double secondStandardParallel = cs
      .getDoubleParameter(NormalizedParameterNames.STANDARD_PARALLEL_2);
    final double centralMeridian = cs.getDoubleParameter(NormalizedParameterNames.CENTRAL_MERIDIAN,
      0);
    final double latitudeOfProjection = cs
      .getDoubleParameter(NormalizedParameterNames.LATITUDE_OF_ORIGIN);
    this.ellipsoid = cs.getEllipsoid();
    this.xo = cs.getDoubleParameter(NormalizedParameterNames.FALSE_EASTING);
    this.yo = cs.getDoubleParameter(NormalizedParameterNames.FALSE_NORTHING);
    this.λo = Math.toRadians(centralMeridian);
    this.φo = Math.toRadians(latitudeOfProjection);
    this.φ1 = Math.toRadians(firstStandardParallel);
    this.φ2 = Math.toRadians(secondStandardParallel);

    this.a = this.ellipsoid.getSemiMajorAxis();
    this.aPow2 = Math.pow(this.a, 2);

    this.e = this.ellipsoid.getEccentricity();
    this.oneOver2e = 1 / (this.e * 2);
    this.ePow2 = this.ellipsoid.getEccentricitySquared();

    final double m1 = m(this.φ1);
    final double m2 = m(this.φ2);
    final double q0 = q(this.φo);
    final double q1 = q(this.φ1);
    final double q2 = q(this.φ2);

    this.n = (m1 * m1 - m2 * m2) / (q2 - q1);
    this.nPow2 = Math.pow(this.n, 2);
    this.c = m1 * m1 + this.n * q1;
    this.ρo = this.a * Math.sqrt(this.c - this.n * q0) / this.n;
  }

  /**
   * n = sin(φ1) + sin (φ2)
   * <p>
   * φ = sin-1( ( C - (ρ ^ 2) * (n ^ 2) ) / 2 * n )
   * <p>
   * λ =
   */
  @Override
  public void inverse(final CoordinatesOperationPoint point) {
    final double ΔX = point.x - this.xo;
    final double ΔY = point.y - this.yo;
    final double e = this.e;
    final double ePow2 = this.ePow2;
    final double oneOver2e = this.oneOver2e;
    final double n = this.n;

    final double ρo = this.ρo;

    final double θ = Math.atan(ΔX / (ρo - ΔY));
    final double ρ = Math.sqrt(Math.pow(ΔX, 2) + Math.pow(ρo - ΔY, 2));
    final double ρPow2 = Math.pow(ρ, 2);
    final double q = (this.c - ρPow2 * this.nPow2 / this.aPow2) / n;
    final double λ = this.λo + θ / n;
    double φ = Math.asin(q / 2.0);
    if (Double.isFinite(φ)) {
      double Δφ;
      final double maxIter = 1000;
      int i = 0;
      do {
        final double sinφ = Math.sin(φ);
        final double eSinφ = e * sinφ;

        final double oneMinusEpow2TimesSinφPow2 = 1.0 - ePow2 * Math.pow(sinφ, 2);
        final double j1 = Math.pow(oneMinusEpow2TimesSinφPow2, 2) / (2 * Math.cos(φ));
        final double k1 = q / (1 - ePow2);
        final double k2 = sinφ / oneMinusEpow2TimesSinφPow2;
        final double k3 = oneOver2e * Math.log((1.0 - eSinφ) / (1 + eSinφ));
        final double newφ = φ + j1 * (k1 - k2 + k3);
        Δφ = Math.abs(newφ - φ);
        φ = newφ;
        i++;
      } while (Double.isFinite(φ) && Δφ > 1e-011 && i < maxIter);
    }
    if (!Double.isFinite(φ)) {
      φ = Angle.PI_OVER_2;
    }
    point.x = λ;
    point.y = φ;
  }

  /**
   * <pre>
   * cos(φ) / sqrt(1 - sq(e) * sq(sin(φ)))
   * </pre>
   *
   * @param φ The lattitude in radians.
   * @return
   */
  private double m(final double φ) {
    final double sinφ = Math.sin(φ);
    return Math.cos(φ) / Math.sqrt(1.0 - this.ePow2 * Math.pow(sinφ, 2));
  }

  /**
   * <pre>
   * a = semiMajorAxis
   * λ = lon;
   * φ = lat;
   *
   * x = ρ * sin(θ)
   * y = ρo - ρ * sin(θ)
   *
   * ρ = a * sqrt(C - n * q) / n
   *
   * </pre>
   */
  @Override
  public void project(final CoordinatesOperationPoint point) {
    final double λ = point.x;
    final double φ = point.y;
    final double q = q(φ);
    final double Δλ = λ - this.λo;
    final double n = this.n;
    final double θ = n * Δλ;
    final double ρ = this.a * (Math.sqrt(this.c - n * q) / n);

    point.x = this.xo + ρ * Math.sin(θ);
    point.y = this.yo + this.ρo - ρ * Math.cos(θ);
  }

  /**
   * <pre>
   * (1 - sq(e)) *
   * (
   *   sin(φ) / (1 - sq(e) * sq(sin(φ))) ) -
   *   (1 / (2 * e)) *
   *   ln( ( 1 - e * sin(φ) ) / ( 1 + e sin(φ)))
   * )
   *
   * </pre>
   *
   * @param φ The lattitude in radians
   * @return
   */
  private double q(final double φ) {
    final double sinφ = Math.sin(φ);
    final double eSinφ = this.e * sinφ;
    final double ePow2 = this.ePow2;
    final double q = (1.0 - ePow2) * (sinφ / (1.0 - ePow2 * Math.pow(sinφ, 2))
      - this.oneOver2e * Math.log((1.0 - eSinφ) / (1.0 + eSinφ)));
    return q;
  }
}
