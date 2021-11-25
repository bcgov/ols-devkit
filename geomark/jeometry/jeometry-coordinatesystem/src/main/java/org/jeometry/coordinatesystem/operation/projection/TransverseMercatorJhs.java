package org.jeometry.coordinatesystem.operation.projection;

import org.jeometry.common.math.Angle;
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
public class TransverseMercatorJhs extends TransverseMercator {

  public static TransverseMercatorJhs newUtm(final Ellipsoid ellipsoid,
    final double utmReferenceMeridian) {
    return new TransverseMercatorJhs("UTM", ellipsoid, utmReferenceMeridian, 0, 0.9996, 500000, 0);
  }

  private final double n;

  private final double B;

  private final double h1;

  private final double h2;

  private final double h3;

  private final double h4;

  private final double h1Prime;

  private final double h2Prime;

  private final double h3Prime;

  private final double h4Prime;

  private final double e;

  private final double mo;

  /**
   * Construct a new TransverseMercator projection.
   *
   * @param coordinateSystem The coordinate system.
   */
  public TransverseMercatorJhs(final ProjectedCoordinateSystem coordinateSystem) {
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

  public TransverseMercatorJhs(final String name, final Ellipsoid ellipsoid,
    final double longitudeOrigin, final double latitudeOrigin, final double ko, final double xo,
    final double yo) {
    super(name, ellipsoid, longitudeOrigin, latitudeOrigin, ko, xo, yo);
    final double f = ellipsoid.getFlattening();
    this.e = ellipsoid.getEccentricity();
    this.n = f / (2 - f);
    final double n2 = Math.pow(this.n, 2);
    final double n3 = Math.pow(this.n, 3);
    final double n4 = Math.pow(this.n, 4);
    this.B = this.a / (1 + this.n) * (1 + n2 / 4 + n4 / 64);
    this.h1 = this.n / 2 - 2 / 3 * n2 + 5 / 16 * n3 + 41 / 180 * n4;
    this.h2 = 13 / 48 * n2 - 3 / 5 * n3 + 557 / 1440 * n4;
    this.h3 = 61 / 240 * n3 - 103 / 140 * n4;
    this.h4 = 49561 / 161280 * n4;

    this.h1Prime = this.n / 2 - 2 / 3 * n2 + 37 / 96 * n3 - 1 / 360 * n4;
    this.h2Prime = 1 / 48 * n2 + 1 / 15 * n3 - 437 / 1440 * n4;
    this.h3Prime = 17 / 480 * n3 - 37 / 840 * n4;
    this.h4Prime = 4397 / 161280 * n4;
    this.mo = mo(this.φo);
  }

  /**
   * Project the projected coordinates in metres to lon/lat cordinates in degrees.
   * @param point The coordinates to convert.
   */
  @Override
  public void inverse(final CoordinatesOperationPoint point) {
    final double ηPrime = (point.x - this.xo) / (this.B * this.ko);
    final double ξPrime = (point.y - this.yo + this.ko * this.mo) / (this.B * this.ko);

    final double ξ1Prime = this.h1Prime * Math.sin(2 * ξPrime) * Math.cosh(2 * ηPrime);
    final double η1Prime = this.h1Prime * Math.cos(2 * ξPrime) * Math.sinh(2 * ηPrime);
    final double ξ2Prime = this.h2Prime * Math.sin(4 * ξPrime) * Math.cosh(4 * ηPrime);
    final double η2Prime = this.h2Prime * Math.cos(4 * ξPrime) * Math.sinh(4 * ηPrime);
    final double ξ3Prime = this.h3Prime * Math.sin(6 * ξPrime) * Math.cosh(6 * ηPrime);
    final double η3Prime = this.h3Prime * Math.cos(6 * ξPrime) * Math.sinh(6 * ηPrime);
    final double ξ4Prime = this.h4Prime * Math.sin(8 * ξPrime) * Math.cosh(8 * ηPrime);
    final double η4Prime = this.h4Prime * Math.cos(8 * ξPrime) * Math.sinh(8 * ηPrime);
    final double ξ0Prime = ξPrime - (ξ1Prime + ξ2Prime + ξ3Prime + ξ4Prime);
    final double η0Prime = ηPrime - (η1Prime + η2Prime + η3Prime + η4Prime);
    final double βPrime = Math.asin(Math.sin(ξ0Prime) / Math.cosh(η0Prime));
    final double QPrime = Angle.asinh(Math.tan(βPrime));
    double QPrimePrime = QPrime + this.e * Angle.atanh(this.e * Math.tanh(QPrime));
    final double lastQPrimePrime = QPrimePrime;
    int i = 0;
    do {
      QPrimePrime = QPrime + this.e * Angle.atanh(this.e * Math.tanh(QPrimePrime));
    } while (Math.abs(lastQPrimePrime - QPrimePrime) < 1.0e-011 && ++i < 100);

    point.x = this.λo + Math.asin(Math.tanh(η0Prime) / Math.cos(βPrime));
    point.y = Math.atan(Math.sinh(QPrimePrime));
  }

  private double mo(final double φ) {
    final double B = this.B;
    final double e = this.e;
    if (φ == 0) {
      return 0;
    } else if (φ == Angle.PI_OVER_2) {
      return B * Angle.PI_OVER_2;
    } else if (φ == -Angle.PI_OVER_2) {
      return B * -Angle.PI_OVER_2;
    } else {
      final double QO = Angle.asinh(Math.tan(φ)) - e * Angle.atanh(e * Math.sin(φ));
      final double βO = Math.atan(Math.sinh(QO));
      final double ξO0 = Math.asin(Math.sin(βO));
      // Note: The previous two steps are taken from the generic calculation
      // flow given below for
      // latitude φ, but here for φO may be simplified to ξO0 = βO = atan(sinh
      // QO).
      final double ξO1 = this.h1 * Math.sin(2 * ξO0);
      final double ξO2 = this.h2 * Math.sin(4 * ξO0);
      final double ξO3 = this.h3 * Math.sin(6 * ξO0);
      final double ξO4 = this.h4 * Math.sin(8 * ξO0);
      final double ξO = ξO0 + ξO1 + ξO2 + ξO3 + ξO4;
      return B * ξO;
    }
  }

  /**
   * Project the lon/lat ordinates in degrees to projected coordinates in metres.
   * @param point The coordinates to convert.
   */
  @Override
  public void project(final CoordinatesOperationPoint point) {
    final double λ = point.x;
    final double φ = point.y;

    final double ko = this.ko;
    final double B = this.B;
    final double e = this.e;
    final double h1 = this.h1;
    final double h2 = this.h2;
    final double h3 = this.h3;
    final double h4 = this.h4;

    final double Q = Angle.asinh(Math.tan(φ)) - e * Angle.atanh(e * Math.sin(φ));
    final double β = Math.atan(Math.sinh(Q));

    final double η0 = Angle.atanh(Math.cos(β) * Math.sin(λ - this.λo));
    final double ξ0 = Math.asin(Math.sin(β) * Math.cosh(η0));

    final double η1 = h1 * Math.cos(2 * ξ0) * Math.sinh(2 * η0);
    final double η2 = h2 * Math.cos(4 * ξ0) * Math.sinh(4 * η0);
    final double η3 = h3 * Math.cos(6 * ξ0) * Math.sinh(6 * η0);
    final double η4 = h4 * Math.cos(8 * ξ0) * Math.sinh(8 * η0);
    final double η = η0 + η1 + η2 + η3 + η4;

    final double ξ1 = h1 * Math.sin(2 * ξ0) * Math.cosh(2 * η0);
    final double ξ2 = h2 * Math.sin(4 * ξ0) * Math.cosh(4 * η0);
    final double ξ3 = h3 * Math.sin(6 * ξ0) * Math.cosh(6 * η0);
    final double ξ4 = h4 * Math.sin(8 * ξ0) * Math.cosh(8 * η0);
    final double ξ = ξ0 + ξ1 + ξ2 + ξ3 + ξ4;

    final double x = this.xo + ko * B * η;
    final double y = this.yo + ko * (B * ξ - this.mo);
    point.x = x;
    point.y = y;
  }
}
