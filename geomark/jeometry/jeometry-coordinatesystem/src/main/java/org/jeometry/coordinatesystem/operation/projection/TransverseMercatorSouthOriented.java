package org.jeometry.coordinatesystem.operation.projection;

import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.NormalizedParameterNames;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

public class TransverseMercatorSouthOriented extends AbstractCoordinatesProjection {

  private final double a;

  private final double a0;

  private final double a2;

  private final double a4;

  private final double a6;

  private final double a8;

  private final double e4;

  private final double e6;

  private final double e8;

  private final double ep2;

  private final double eSq;

  private final double k0;

  private final double λ0; // central meridian

  private final Ellipsoid ellipsoid;

  private final double x0;

  private final double y0;

  public TransverseMercatorSouthOriented(final ProjectedCoordinateSystem cs) {
    final double centralMeridian = cs.getDoubleParameter(NormalizedParameterNames.CENTRAL_MERIDIAN);
    final double scaleFactor = cs.getDoubleParameter(NormalizedParameterNames.SCALE_FACTOR);

    this.ellipsoid = cs.getEllipsoid();
    this.x0 = cs.getDoubleParameter(NormalizedParameterNames.FALSE_EASTING);
    this.y0 = cs.getDoubleParameter(NormalizedParameterNames.FALSE_NORTHING);
    this.λ0 = Math.toRadians(centralMeridian);
    this.a = this.ellipsoid.getSemiMajorAxis();
    this.k0 = scaleFactor;
    this.ep2 = (this.a * this.a
      - this.ellipsoid.getSemiMinorAxis() * this.ellipsoid.getSemiMinorAxis())
      / (this.ellipsoid.getSemiMinorAxis() * this.ellipsoid.getSemiMinorAxis());
    this.eSq = this.ellipsoid.getEccentricitySquared();
    this.e4 = this.eSq * this.eSq;
    this.e6 = this.e4 * this.eSq;
    this.e8 = this.e4 * this.e4;
    this.a0 = 1.0 - this.eSq / 4.0 - 3.0 * this.e4 / 64.0 - 5.0 * this.e6 / 256.0
      - 175.0 * this.e8 / 16384.0;
    this.a2 = 3.0 / 8.0
      * (this.eSq + this.e4 / 4.0 + 15.0 * this.e6 / 128.0 - 455.0 * this.e8 / 4096.0);
    this.a4 = 15.0 / 256.0 * (this.e4 + 3.0 * this.e6 / 4.0 - 77.0 * this.e8 / 128.0);
    this.a6 = 35.0 / 3072.0 * (this.e6 - 41.0 * this.e8 / 32.0);
    this.a8 = -315.0 * this.e8 / 131072.0;
  }

  protected double footPointLatitude(final double y) {
    double lat1;
    double newlat = y / this.a;
    int i = 0;
    do {
      lat1 = newlat;
      final double flat = s0(lat1) - y;
      final double dflat = this.a
        * (this.a0 - 2.0 * this.a2 * Math.cos(2.0 * lat1) + 4.0 * this.a4 * Math.cos(4.0 * lat1)
          - 6.0 * this.a6 * Math.cos(6.0 * lat1) + 8.0 * this.a8 * Math.cos(8.0 * lat1));
      newlat = lat1 - flat / dflat;
      // Increased tolerance from 1E-16 to 1E-15. 1E-16 was causing an infinite
      // loop.
      i++;
    } while (i < 100 && Math.abs(newlat - lat1) > 1.0e-015);
    lat1 = newlat;
    return lat1;
  }

  @Override
  public void inverse(final CoordinatesOperationPoint point) {
    final double φ1 = footPointLatitude(point.y - this.y0);
    final double cosPhi1 = Math.cos(φ1);
    final double sinPhi = Math.sin(φ1);
    final double sinPhi1Sq = sinPhi * sinPhi;
    final double tanPhi1 = Math.tan(φ1);

    final double nu1 = this.a / Math.sqrt(1 - this.eSq * sinPhi1Sq);
    final double rho1 = this.a * (1 - this.eSq) / Math.pow(1 - this.eSq * sinPhi1Sq, 1.5);
    final double c1 = this.eSq * cosPhi1 * cosPhi1;
    final double d = (point.x - this.x0) / (nu1 * this.k0);
    final double d2 = d * d;
    final double d3 = d2 * d;
    final double d4 = d2 * d2;
    final double d5 = d2 * d;
    final double d6 = d4 * d;
    final double t1 = tanPhi1 * tanPhi1;

    final double c1Sq = c1 * c1;
    final double t1Sq = t1 * t1;
    point.x = this.λ0 + (d - (1 + 2 * t1 + c1) * d3 / 6
      + (5 - 2 * c1 + 28 * t1 - 3 * c1Sq + 8 * this.eSq + 24 * t1Sq) * d5 / 120) / cosPhi1;
    point.y = φ1 - nu1 * Math.tan(φ1 / rho1)
      * (d2 / 2 - (5 + 3 * t1 + 10 * c1 - 4 * c1Sq - 9 * this.eSq) * d4 / 24
        + (61 + 90 * t1 + 298 * c1 + 45 * t1Sq - 252 * this.eSq - 3 * c1Sq) * d6 / 720);
  }

  @Override
  public void project(final CoordinatesOperationPoint point) {
    final double λ = point.x;
    final double φ = point.y;

    // ep2 = the second eccentricity squared.
    // N = the radius of curvature of the ellipsoid in the prime vertical plane
    final double n = this.ellipsoid.primeVerticalRadiusOfCurvature(φ);
    final double n2 = this.ep2 * Math.pow(Math.cos(φ), 2.0);
    final double n4 = n2 * n2;
    final double n6 = n4 * n2;
    final double n8 = n4 * n4;
    final double t = Math.tan(φ);
    final double t2 = t * t;
    final double t4 = t2 * t2;
    final double t6 = t4 * t2;
    final double cosLat = Math.cos(φ);
    final double sinLat = Math.sin(φ);
    final double l = λ - this.λ0;
    final double l2 = l * l;
    final double l3 = l2 * l;
    final double l4 = l2 * l2;
    final double l5 = l4 * l;
    final double l6 = l4 * l2;
    final double l7 = l5 * l2;
    final double l8 = l4 * l4;
    double u0 = l * cosLat;
    double u1 = l3 * Math.pow(cosLat, 3.0) / 6.0;
    double u2 = l5 * Math.pow(cosLat, 5.0) / 120.0;
    double u3 = l7 * Math.pow(cosLat, 7.0) / 5040.0;
    double v1 = 1.0 - t2 + n2;
    double v2 = 5.0 - 18.0 * t2 + t4 + 14.0 * n2 - 58.0 * t2 * n2 + 13.0 * n4 + 4.0 * n6
      - 64.0 * n4 * t2 - 24.0 * n6 * t2;
    double v3 = 61.0 - 479.0 * t2 + 179.0 * t4 - t6;
    final double x = u0 + u1 * v1 + u2 * v2 + u3 * v3;

    u0 = l2 / 2.0 * sinLat * cosLat;
    u1 = l4 / 24.0 * sinLat * Math.pow(cosLat, 3.0);
    u2 = l6 / 720.0 * sinLat * Math.pow(cosLat, 5.0);
    u3 = l8 / 40320.0 * sinLat * Math.pow(cosLat, 7.0);
    v1 = 5.0 - t2 + 9.0 * n2 + 4.0 * n4;
    v2 = 61.0 - 58.0 * t2 + t4 + 270.0 * n2 - 330.0 * t2 * n2 + 445.0 * n4 + 324.0 * n6
      - 680.0 * n4 * t2 + 88.0 * n8 - 600.0 * n6 * t2 - 192.0 * n8 * t2;
    v3 = 1385.0 - 311.0 * t2 + 543.0 * t4 - t6;
    final double y = s0(φ) / n + u0 + u1 * v1 + u2 * v2 + u3 * v3;

    point.x = this.x0 - n * x * this.k0;
    point.y = this.y0 - n * y * this.k0;
  }

  private double s0(final double lat) {
    return this.a * (this.a0 * lat - this.a2 * Math.sin(2.0 * lat) + this.a4 * Math.sin(4.0 * lat)
      - this.a6 * Math.sin(6.0 * lat) + this.a8 * Math.sin(8.0 * lat));
  }
}
