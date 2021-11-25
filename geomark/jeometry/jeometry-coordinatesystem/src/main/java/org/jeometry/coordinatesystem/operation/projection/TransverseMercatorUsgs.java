package org.jeometry.coordinatesystem.operation.projection;

import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.NormalizedParameterNames;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

/**
 * An implementation of the Transverse Mercator projection. See section 1.3.5 of
 * <a href="http://www.epsg.org/guides/G7-2.html">OGP Surveying and Positioning
 * Guidance Note number 7, part 2</a>. Snyder in US Geological Survey Professional Paper #1395
 *
 * @author Paul Austin
 */
public class TransverseMercatorUsgs extends TransverseMercator {

  public static TransverseMercatorUsgs newUtm(final Ellipsoid ellipsoid,
    final double utmReferenceMeridian) {
    return new TransverseMercatorUsgs("UTM", ellipsoid, utmReferenceMeridian, 0, 0.9996, 500000, 0);
  }

  /** The eccentricity ^ 4 of the ellipsoid. */
  private final double ePow4;

  /** The eccentricity ^ 6 of the ellipsoid. */
  private final double ePow6;

  /** The eccentricity prime squared of the ellipsoid. */
  private final double ePrimePow2;

  /** The eccentricity ^ 2 of the ellipsoid. */
  private final double ePow2;

  private final double sqrt1MinusESq;

  /** The value of m at the latitude of origin. */
  private final double mo;

  private final double threeTimesE1Div2Minus27TimeE1Pow3Div32;

  private final double e1Pow2Times21Div16MinusE1Pow4Times55Div32;

  private final double e1Pow3Times151Div96;

  private final double e1Pow4Times1097Div512;

  private final double aTimes1MinusEsqDiv4MinesEPow4Times3Div64MinusEPow6Times5Div256;

  /**
   * Construct a new TransverseMercator projection.
   *
   * @param coordinateSystem The coordinate system.
   */
  public TransverseMercatorUsgs(final ProjectedCoordinateSystem coordinateSystem) {
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

  public TransverseMercatorUsgs(final String name, final Ellipsoid ellipsoid,
    final double longitudeOrigin, final double latitudeOrigin, final double ko, final double xo,
    final double yo) {
    super(name, ellipsoid, longitudeOrigin, latitudeOrigin, ko, xo, yo);
    final double φ0 = Math.toRadians(latitudeOrigin);
    this.ePow2 = ellipsoid.getEccentricitySquared();
    this.sqrt1MinusESq = Math.sqrt(1 - this.ePow2);

    this.ePow4 = Math.pow(this.ePow2, 2);
    this.ePow6 = Math.pow(this.ePow2, 3);
    this.mo = m(φ0);
    this.ePrimePow2 = this.ePow2 / (1 - this.ePow2);

    final double e1 = (1 - this.sqrt1MinusESq) / (1 + this.sqrt1MinusESq);
    final double e1Pow2 = Math.pow(e1, 2);
    final double e1Pow3 = Math.pow(e1, 3);
    final double e1Pow4 = Math.pow(e1, 4);
    this.threeTimesE1Div2Minus27TimeE1Pow3Div32 = 3 * e1 / 2 - 27 * e1Pow3 / 32;
    this.e1Pow2Times21Div16MinusE1Pow4Times55Div32 = 21 * e1Pow2 / 16 - 55 * e1Pow4 / 32;
    this.e1Pow3Times151Div96 = 151 * e1Pow3 / 96;
    this.e1Pow4Times1097Div512 = 1097 * e1Pow4 / 512;
    this.aTimes1MinusEsqDiv4MinesEPow4Times3Div64MinusEPow6Times5Div256 = this.a
      * (1 - this.ePow2 / 4 - this.ePow4 * 3 / 64 - this.ePow6 * 5 / 256);

  }

  /**
   * Project the projected coordinates in metres to lon/lat ordinates in
   * degrees.
   *
   * <pre>
   * ϕ = ϕ1 - (ν1 * tanϕ1 / ρ1 ) * [
   *   D &circ; 2/2 -
   *   (5 + 3 * T1 + 10 * C1 - 4 * C1 &circ; 2 - 9 * ePrime &circ; 2) * D &circ; 4 / 24 +
   *   (61 + 90 * T1 + 298 * C1 + 45 * T1 &circ; 2 - 252 * ePrime &circ; 2 - 3 * C1 &circ; 2) * D &circ; 6 / 720
   * ]
   * λ = λO + [
   *   D -
   *   (1 + 2 * T1 + C1) * D &circ; 3 / 6 +
   *   (5 - 2 * C1 + 28 * T1 -
   *   3 * C1 &circ; 2 + 8 * ePrime &circ; 2 + 24 * T1 &circ; 2) * D &circ; 5 / 120
   * ] / cosϕ1
   *
   * ν1 = a /(1 - e &circ; 2 * sinϕ1 &circ; 2) &circ; 0.5
   * ρ1 = a * (1 - e &circ; 2) / (1 - e &circ; 2 * sinϕ1 &circ; 2) &circ; 1.5
   *
   * ϕ1 = μ1 +
   *   (3 * e1 / 2 - 27 * e1 &circ; 3 /32 + .....) * sin(2 * μ1) +
   *   (21 * e1 &circ; 2 / 16 - 55 * e1 &circ; 4 / 32 + ....) * sin(4 * μ1) +
   *   (151 * e1 &circ; 3 / 96 + .....) * sin(6 * μ1) +
   *   (1097 * e1 &circ; 4 / 512 - ....) * sin(8 * μ1) +
   *   ......
   *
   * e1 = [1 - (1 - e &circ; 2) &circ; 0.5] / [1 + (1 - e &circ; 2) &circ; 0.5]
   * μ1 = M1 / [a * (1 - e &circ; 2 / 4 - 3 * e &circ; 4 / 64 - 5 * e &circ; 6 / 256 - ....)]
   * M1 = MO + (y - yo) / ko
   * T1 = tanϕ1 &circ; 2
   * C1 = ePrime &circ; 2 * cosϕ1 &circ; 2
   * ePrime &circ; 2 = e &circ; 2 / (1 - e &circ; 2)
   * D = (x - xo) / (ν1 * kO)
   * </pre>
   * @param point The coordinates to convert.
   */
  @Override
  public void inverse(final CoordinatesOperationPoint point) {
    final double ePow2 = this.ePow2;
    final double a = this.a;
    final double ko = this.ko;
    final double ePrimePow2 = this.ePrimePow2;

    final double M1 = this.mo + (point.y - this.yo) / ko;
    final double μ1 = M1 / this.aTimes1MinusEsqDiv4MinesEPow4Times3Div64MinusEPow6Times5Div256;
    final double φ1 = μ1 + this.threeTimesE1Div2Minus27TimeE1Pow3Div32 * Math.sin(2 * μ1)
      + this.e1Pow2Times21Div16MinusE1Pow4Times55Div32 * Math.sin(4 * μ1)
      + this.e1Pow3Times151Div96 * Math.sin(6 * μ1) + this.e1Pow4Times1097Div512 * Math.sin(8 * μ1);
    final double cosφ1 = Math.cos(φ1);
    final double sinφ = Math.sin(φ1);
    final double tanφ1 = Math.tan(φ1);

    final double sinφPow2 = Math.pow(sinφ, 2);
    final double oneMinusESqSinφ1Sq = 1 - ePow2 * sinφPow2;
    final double ν1 = a / Math.sqrt(oneMinusESqSinφ1Sq);
    final double ρ1 = a * (1 - ePow2) / Math.pow(oneMinusESqSinφ1Sq, 1.5);
    final double C1 = ePrimePow2 * Math.pow(cosφ1, 2);
    final double D = (point.x - this.xo) / (ν1 * ko);
    final double D2 = Math.pow(D, 2);
    final double D3 = Math.pow(D, 3);
    final double D4 = Math.pow(D, 4);
    final double D5 = Math.pow(D, 5);
    final double D6 = Math.pow(D, 6);
    final double T1 = Math.pow(tanφ1, 2);
    final double T12 = Math.pow(T1, 2);

    final double C12 = Math.pow(C1, 2);

    point.x = this.λo + (D - (1 + 2 * T1 + C1) * D3 / 6
      + (5 - 2 * C1 + 28 * T1 - 3 * C12 + 8 * ePrimePow2 + 24 * T12) * D5 / 120) / cosφ1;
    point.y = φ1
      - ν1 * tanφ1 / ρ1 * (D2 / 2 - (5 + 3 * T1 + 10 * C1 - 4 * C12 - 9 * ePrimePow2) * D4 / 24
        + (61 + 90 * T1 + 298 * C1 + 45 * T12 - 252 * ePrimePow2 - 3 * C12) * D6 / 720);
  }

  /**
   * Calculate the value of m for the given value of φ using the following
   * forumla.
   *
   * <pre>
   * m = a [
   *   (1 - e2/4 - 3e4/64 - 5e6/256 -....)ϕ -
   *   (3e2/8 + 3e4/32 + 45e6/1024+....)sin2ϕ +
   *   (15e4/256 + 45e6/1024 +.....)sin4ϕ -
   *   (35e6/3072 + ....)sin6ϕ + .....
   * ]
   * </pre>
   *
   * @param φ The φ value in radians.
   * @return The value of m.
   */
  private double m(final double φ) {
    return this.a * ((1 - this.ePow2 / 4 - 3 * this.ePow4 / 64 - 5 * this.ePow6 / 256) * φ
      - (3 * this.ePow2 / 8 + 3 * this.ePow4 / 32 + 45 * this.ePow6 / 1024) * Math.sin(2 * φ)
      + (15 * this.ePow4 / 256 + 45 * this.ePow6 / 1024) * Math.sin(4 * φ)
      - 35 * this.ePow6 / 3072 * Math.sin(6 * φ));
  }

  /**
   * Project the lon/lat ordinates in degrees to projected coordinates in
   * metres.
   *
   * <pre>
   * x = xo + kO * ν * [
   *   A + (1 - T + C) * A &circ; 3 / 6 +
   *   (5 - 18 * T + T &circ; 2 + 72 *C - 58 *ePrime &circ; 2 ) * A &circ; 5 / 120
   * ]
   * y = yo + kO * { M - MO + ν * tanϕ * [
   *   A &circ; 2 / 2 +
   *   (5 - T + 9 * C + 4 * C &circ; 2) * A &circ; 4 / 24 +
   *   (61 - 58 * T + T &circ; 2 + 600 * C - 330 * ePrime &circ; 2 ) * A &circ; 6 / 720
   * ]}
   *
   * T = tanϕ * 2
   * C = e &circ; 2 * cosϕ &circ; 2 / (1 - e &circ; 2)
   * A = (λ - λO) * cosϕ
   * ν = a / (1 - e &circ; 2 * sinϕ &circ; 2) &circ; 0.5
   * </pre>
   * @param point The coordinates to convert.
   */
  @Override
  public void project(final CoordinatesOperationPoint point) {
    final double λ = point.x;
    final double φ = point.y;

    final double ePrimePow2 = this.ePrimePow2;
    final double ko = this.ko;

    final double cosφ = Math.cos(φ);
    final double sinφ = Math.sin(φ);
    final double tanφ = Math.tan(φ);

    final double nu = this.a / Math.sqrt(1 - this.ePow2 * sinφ * sinφ);
    final double tanφPow2 = tanφ * tanφ;
    final double tanφPow4 = tanφPow2 * tanφPow2;
    final double c = ePrimePow2 * cosφ * cosφ;
    final double cPow2 = c * c;
    final double a1 = (λ - this.λo) * cosφ;
    final double a1Pow2 = Math.pow(a1, 2);
    final double a1Pow3 = Math.pow(a1, 3);
    final double a1Pow4 = Math.pow(a1, 4);
    final double a1Pow5 = Math.pow(a1, 5);
    final double a1Pow6 = Math.pow(a1, 6);
    final double m = m(φ);

    point.x = this.xo + ko * nu * (a1 + (1 - tanφPow2 + c) * a1Pow3 / 6
      + (5 - 18 * tanφPow2 + tanφPow4 + 72 * c - 58 * ePrimePow2) * a1Pow5 / 120);
    point.y = this.yo + ko
      * (m - this.mo + nu * tanφ * (a1Pow2 / 2 + (5 - tanφPow2 + 9 * c + 4 * cPow2) * a1Pow4 / 24
        + (61 - 58 * tanφPow2 + tanφPow4 + 600 * c - 330 * ePrimePow2) * a1Pow6 / 720));
  }
}
