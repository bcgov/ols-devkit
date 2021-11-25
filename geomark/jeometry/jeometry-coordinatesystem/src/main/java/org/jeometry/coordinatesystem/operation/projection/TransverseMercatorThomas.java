package org.jeometry.coordinatesystem.operation.projection;

import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.NormalizedParameterNames;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

/**
 * An implementation of the Transverse Mercator projection. See section 1.3.5 of
 * <a href="http://www.epsg.org/guides/G7-2.html">OGP Surveying and Positioning
 * Guidance Note number 7, part 2.</a> Krüger and published in Finland as Recommendations for Public Administration (JHS) 154.
 *
 *
 * @author Paul Austin
 */
public class TransverseMercatorThomas extends TransverseMercator {

  private final double e;

  private final double a0;

  private final double a2;

  private final double a4;

  private final double a6;

  private final double a8;

  public TransverseMercatorThomas(final ProjectedCoordinateSystem coordinateSystem) {
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

  public TransverseMercatorThomas(final String name, final Ellipsoid ellipsoid,
    final double longitudeOrigin, final double latitudeOrigin, final double ko, final double xo,
    final double yo) {
    super(name, ellipsoid, longitudeOrigin, latitudeOrigin, ko, xo, yo);
    final double e2 = (this.a * this.a - this.b * this.b) / (this.a * this.a);
    this.e = Math.sqrt(e2);

    final double e4 = e2 * e2;
    final double e6 = e4 * e2;
    final double e8 = e6 * e2;
    this.a0 = 1 - e2 / 4 - e4 * 3 / 64 - e6 * 5 / 256 - e8 * 175 / 16384;
    this.a2 = (e2 + e4 / 4. + e6 * 15. / 128. - e8 * 455. / 4096.) * .375;
    this.a4 = (e4 + e6 * 3. / 4. - e8 * 77. / 128.) * .05859375;
    this.a6 = (e6 - e8 * 41. / 32.) * .011393229166666666;
    this.a8 = e8 * -315. / 131072.;
  }

  /**
   * Project the projected coordinates in metres to lon/lat cordinates in degrees.
   * @param point The coordinates to convert.
   */
  @Override
  public void inverse(final CoordinatesOperationPoint point) {
    final double x = (point.x - this.xo) / this.ko;
    final double y = point.y / this.ko;
    final double a = this.a;
    final double b = this.b;
    final double e = this.e;

    double φ1 = y / a;
    double deltaφ;
    do {
      deltaφ = (a * (this.a0 * φ1 - this.a2 * Math.sin(φ1 * 2) + this.a4 * Math.sin(φ1 * 4)
        - this.a6 * Math.sin(φ1 * 6) + this.a8 * Math.sin(φ1 * 8)) - y)
        / (a * (this.a0 - this.a2 * 2 * Math.cos(φ1 * 2) + this.a4 * 4. * Math.cos(φ1 * 4)
          - this.a6 * 6 * Math.cos(φ1 * 6) + this.a8 * 8 * Math.cos(φ1 * 8)));
      φ1 -= deltaφ;

    } while (Math.abs(deltaφ) >= 1e-15);

    final double tanφ = Math.tan(φ1);
    final double tanφPow2 = tanφ * tanφ;
    final double tanφPow4 = tanφPow2 * tanφPow2;
    final double tanφPow6 = tanφPow2 * tanφPow4;

    final double sinφ = Math.sin(φ1);
    final double sinφPow2 = sinφ * sinφ;
    final double cosφ = Math.cos(φ1);

    final double eta = Math.sqrt((a * a - b * b) / (b * b) * (cosφ * cosφ));
    final double etaPow2 = eta * eta;
    final double etaPow4 = etaPow2 * etaPow2;
    final double etaPow6 = etaPow2 * etaPow4;
    final double etaPow8 = etaPow4 * etaPow4;
    final double ePow2 = e * e;
    final double dn = a / Math.sqrt(1. - ePow2 * (sinφ * sinφ));
    final double d__2 = 1 - ePow2 * sinφPow2;
    final double dm = a * (1 - ePow2) / Math.sqrt(d__2 * (d__2 * d__2));
    final double xOverDn = x / dn;
    final double xOverDnPow2 = xOverDn * xOverDn;
    final double xOverDnPow4 = xOverDnPow2 * xOverDnPow2;
    final double xOverDnPow6 = xOverDnPow2 * xOverDnPow4;

    point.x = this.λo + (xOverDn - xOverDn * xOverDnPow2 / 6 * (tanφPow2 * 2 + 1 + etaPow2)
      + xOverDn * xOverDnPow4 / 120
        * (etaPow2 * 6 + 5 + tanφPow2 * 28 - etaPow4 * 3 + tanφPow2 * 8 * etaPow2 + tanφPow4 * 24
          - etaPow6 * 4 + tanφPow2 * 4 * etaPow4 + tanφPow2 * 24 * etaPow6)
      - xOverDn * xOverDnPow6 / 5040 * (tanφPow2 * 662 + 61 + tanφPow4 * 1320 + tanφPow6 * 720))
      / cosφ;
    point.y = φ1 + tanφ * (-(x * x) / (dm * 2 * dn)
      + xOverDn * xOverDnPow2 * x / (dm * 24)
        * (tanφPow2 * 3 + 5 + etaPow2 - etaPow4 * 4. - etaPow2 * 9 * tanφPow2)
      - xOverDn * xOverDnPow4 * x / (dm * 720)
        * (tanφPow2 * 90 + 61 + etaPow2 * 46 + tanφPow4 * 45 - tanφPow2 * 252 * etaPow2
          - etaPow4 * 3 + etaPow6 * 100 - tanφPow2 * 66 * etaPow4 - tanφPow4 * 90 * etaPow2
          + etaPow8 * 88 + tanφPow4 * 225 * etaPow4 + tanφPow2 * 84 * etaPow6
          - tanφPow2 * 192 * etaPow8)
      + xOverDn * xOverDnPow6 * x / (dm * 40320)
        * (tanφPow2 * 3633 + 1385 + tanφPow4 * 4095 + tanφPow6 * 1574));
  }

  /**
   * Project the lon/lat ordinates in degrees to projected coordinates in metres.
   * @param point The coordinates to convert.
   */
  @Override
  public void project(final CoordinatesOperationPoint point) {
    final double λ = point.x;
    final double φ = point.y;
    final double deltaλ = λ - this.λo;

    final double sinφ = Math.sin(φ);
    final double sinφPow2 = sinφ * sinφ;
    final double cosφ = Math.cos(φ);
    final double cosφPow2 = cosφ * cosφ;
    final double cosφPow4 = cosφPow2 * cosφPow2;
    final double cosφPow6 = cosφPow2 * cosφPow4;
    final double tanφ = Math.tan(φ);
    final double tanφPow2 = tanφ * tanφ;
    final double tanφPow4 = tanφPow2 * tanφPow2;
    final double tanφPow6 = tanφPow2 * tanφPow4;
    final double a = this.a;
    final double b = this.b;
    final double e = this.e;

    final double eta = Math.sqrt((a * a - b * b) / (b * b) * cosφPow2);
    final double sφ = a * (this.a0 * φ - this.a2 * Math.sin(φ * 2) + this.a4 * Math.sin(φ * 4)
      - this.a6 * Math.sin(φ * 6) + this.a8 * Math.sin(φ * 8));

    final double dn = a / Math.sqrt(1. - e * e * sinφPow2);
    double x = 0;
    double y = sφ;
    if (Math.abs(deltaλ) >= 2e-9) {
      final double deltaλPow2 = deltaλ * deltaλ;
      final double deltaλPow4 = deltaλPow2 * deltaλPow2;
      final double deltaλPow6 = deltaλPow2 * deltaλPow4;
      final double etaPow2 = eta * eta;
      final double etaPow4 = etaPow2 * etaPow2;
      final double etaPow6 = etaPow2 * etaPow4;
      final double etaPow8 = etaPow4 * etaPow4;
      x = dn
        * (deltaλ * cosφ + deltaλ * deltaλPow2 * (cosφ * cosφPow2) / 6 * (1 - tanφPow2 + etaPow2)
          + deltaλ * deltaλPow4 * (cosφ * cosφPow4) / 120
            * (5 - tanφPow2 * 18 + tanφPow4 + etaPow2 * 14 - tanφPow2 * 58 * etaPow2 + etaPow4 * 13
              + etaPow6 * 4 - etaPow4 * 64 * tanφPow2 - etaPow6 * 24 * tanφPow2)
          + deltaλ * deltaλPow6 / 5040. * (cosφ * cosφPow6)
            * (61 - tanφPow2 * 479 + tanφPow4 * 179 - tanφPow6));

      y = sφ + dn * (deltaλPow2 / 2 * sinφ * cosφ
        + deltaλPow4 / 24 * sinφ * (cosφ * cosφPow2) * (5 - tanφPow2 + etaPow2 * 9 + etaPow4 * 4)
        + deltaλPow6 / 720. * sinφ * (cosφ * cosφPow4)
          * (61 - tanφPow2 * 58 + tanφPow4 + etaPow2 * 270 - tanφPow2 * 330 * etaPow2
            + etaPow4 * 445 + etaPow6 * 324 - etaPow4 * 680 * tanφPow2 + etaPow6 * 88
            - etaPow6 * 600 * tanφPow2 - etaPow8 * 192 * tanφPow2)
        + deltaλPow4 * deltaλPow4 / 40320 * sinφ * (cosφ * cosφPow6)
          * (1385 - tanφPow2 * 3111 + tanφPow4 * 543 - tanφPow6));
    }

    x = this.xo + this.ko * x;
    y = this.ko * y;
    point.x = x;
    point.y = y;
  }
}
