package org.jeometry.coordinatesystem.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;

import org.jeometry.common.math.Angle;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;
import org.jeometry.coordinatesystem.util.Equals;
import org.jeometry.coordinatesystem.util.Md5;

public class Ellipsoid implements Serializable {
  private static final long serialVersionUID = -8349864136575195872L;

  public static Ellipsoid newMajorMinor(final String name, final double semiMajorAxis,
    final double semiMinorAxis) {
    return new Ellipsoid(name, semiMajorAxis, semiMinorAxis, Double.NaN, null, false);
  }

  private final Authority authority;

  private final boolean deprecated;

  private final double eccentricity;

  private final double eccentricitySquared;

  private double inverseFlattening;

  private final String name;

  /** Radius of earth at equator. */
  private final double semiMajorAxis;

  private final double semiMajorAxisSq;

  /** Radius of earth at poles. */
  private double semiMinorAxis;

  private final double semiMinorAxisSq;

  private final double flattening;

  private final CoordinatesOperation geodeticToCartesianOperation = this::geodeticToCartesian;

  private final CoordinatesOperation cartesianToGeodeticOperation = this::cartesianToGeodetic;

  private final double secondEccentricitySquared;

  public Ellipsoid(final String name, final double semiMajorAxis, final double inverseFlattening) {
    this(name, semiMajorAxis, Double.NaN, inverseFlattening, null, false);
  }

  public Ellipsoid(final String name, final double semiMajorAxis, final double inverseFlattening,
    final Authority authority) {
    this(name, semiMajorAxis, Double.NaN, inverseFlattening, authority, false);
  }

  public Ellipsoid(final String name, final double semiMajorAxis, final double semiMinorAxis,
    final double inverseFlattening, final Authority authority, final boolean deprecated) {
    this.name = name;
    this.semiMajorAxis = semiMajorAxis;
    this.inverseFlattening = inverseFlattening;
    this.semiMinorAxis = semiMinorAxis;
    this.authority = authority;
    this.deprecated = deprecated;

    if (Double.isNaN(inverseFlattening)) {
      this.inverseFlattening = semiMajorAxis / (semiMajorAxis - this.semiMinorAxis);
    }
    this.flattening = 1.0 / this.inverseFlattening;

    if (Double.isNaN(semiMinorAxis)) {
      this.semiMinorAxis = semiMajorAxis - semiMajorAxis * this.flattening;
    }
    this.semiMajorAxisSq = semiMajorAxis * semiMajorAxis;
    this.semiMinorAxisSq = this.semiMinorAxis * this.semiMinorAxis;
    // eccentricitySquared = 1.0 - b2 / a2;

    this.eccentricitySquared = this.flattening + this.flattening
      - this.flattening * this.flattening;
    this.eccentricity = Math.sqrt(this.eccentricitySquared);
    this.secondEccentricitySquared = this.flattening * (2 - this.flattening)
      / Math.pow(1 - this.flattening, 2);
  }

  public double astronomicAzimuth(final double lon1, final double lat1, final double h1, double xsi,
    double eta, final double lon2, final double lat2, final double h2) {
    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;

    final double φ1 = Math.toRadians(lat1);
    final double φ2 = Math.toRadians(lat2);
    eta = Math.toRadians(eta);
    xsi = Math.toRadians(xsi);

    final double φm = (φ1 + φ2) / 2;
    final double esq = (a * a - b * b) / (a * a);

    final double sinPh1 = Math.sin(φ1);
    final double d__1 = Math.sqrt(1. - esq * (sinPh1 * sinPh1));
    final double sinPhi2 = Math.sin(φ2);
    final double d__4 = Math.sqrt(1. - esq * (sinPhi2 * sinPhi2));
    final double mm = (a * (1. - esq) / (d__1 * (d__1 * d__1))
      + a * (1. - esq) / (d__4 * (d__4 * d__4))) / 2.;
    final double nm = (a / Math.sqrt(1 - esq * (sinPh1 * sinPh1))
      + a / Math.sqrt(1 - esq * (sinPhi2 * sinPhi2))) / 2;

    final double distance = distanceMetres(lon1, lat1, lon2, lat2);
    final double azimuth = azimuth(lon1, lat1, lon2, lat2);

    // c1 is always 0 as dh is 0

    final double cosPhi2 = Math.cos(φ2);
    final double c2 = h2 / mm * esq * Math.sin(azimuth) * Math.cos(azimuth) * (cosPhi2 * cosPhi2);

    final double cosPhim = Math.cos(φm);
    final double c3 = -esq * (distance * distance) * (cosPhim * cosPhim) * Math.sin(azimuth * 2)
      / (nm * nm * 12);

    double spaz = azimuth + eta * Math.tan(φ1) - c2 - c3;

    if (spaz < 0) {
      spaz = Angle.PI_TIMES_2 + spaz;
    }
    return Math.toDegrees(spaz);
  }

  public double azimuth(final double lon1, final double lat1, final double lon2,
    final double lat2) {
    final double λ1 = Math.toRadians(lon1);
    final double φ1 = Math.toRadians(lat1);
    final double λ2 = Math.toRadians(lon2);
    final double φ2 = Math.toRadians(lat2);
    return distanceMetresRadians(λ1, φ1, λ2, φ2);
  }

  public double azimuthRadians(final double λ1, final double φ1, final double λ2, final double φ2) {
    final double f = this.flattening;

    final double deltaLambda = λ2 - λ1;
    final double tanU1 = (1 - f) * Math.tan(φ1);
    final double cosU1 = 1 / Math.sqrt(1 + tanU1 * tanU1);
    final double sinU1 = tanU1 * cosU1;
    final double tanU2 = (1 - f) * Math.tan(φ2);
    final double cosU2 = 1 / Math.sqrt(1 + tanU2 * tanU2);
    final double sinU2 = tanU2 * cosU2;

    double λ = deltaLambda;
    double lastLambda;
    double iterationLimit = 100;
    double cosSqAlpha;
    double sinSigma;
    double cos2SigmaM;
    double sigma;
    double cosSigma;
    double sinLambda;
    double cosLambda;

    do {
      sinLambda = Math.sin(λ);
      cosLambda = Math.cos(λ);
      final double sinSqSigma = cosU2 * sinLambda * (cosU2 * sinLambda)
        + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
      sinSigma = Math.sqrt(sinSqSigma);
      if (sinSigma == 0) { // co-incident points
        return 0;
      }
      cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
      sigma = Math.atan2(sinSigma, cosSigma);
      final double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
      cosSqAlpha = 1 - sinAlpha * sinAlpha;
      cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
      if (!Double.isFinite(cos2SigmaM)) {// equatorial line: cosSqAlpha=0 (§6)
        cos2SigmaM = 0;
      }
      final double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
      lastLambda = λ;
      λ = deltaLambda + (1 - C) * f * sinAlpha
        * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
    } while (Math.abs(λ - lastLambda) > 1e-12 && --iterationLimit > 0);
    if (iterationLimit == 0) {
      throw new IllegalStateException("Formula failed to converge");
    }
    double azimuth = Math.atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
    if (azimuth > Angle.PI_TIMES_2) {
      azimuth -= Angle.PI_TIMES_2;
    } else if (azimuth < 0) {
      azimuth += Angle.PI_TIMES_2;
    }
    return azimuth;
  }

  public void cartesianToGeodetic(final CoordinatesOperationPoint point) {
    final double x = point.x;
    final double y = point.y;
    final double z = point.z;

    final double a = this.semiMajorAxis;
    final double b = this.semiMajorAxis;
    final double es = this.eccentricitySquared;
    final double p = Math.hypot(x, y);

    final double θ = Math.atan2(z * a, p * b);

    final double cosθ = Math.cos(θ);
    final double sinθ = Math.sin(θ);
    final double φ = Math.atan2(z + this.secondEccentricitySquared * b * Math.pow(sinθ, 3),
      p - es * a * Math.pow(cosθ, 3));
    final double λ = Math.atan2(y, x);

    final double sinφ = Math.sin(φ);
    final double cosφ = Math.cos(φ);
    final double ePow2 = this.eccentricitySquared;
    double n; // normalRadiusOfCurvature
    if (ePow2 == 0) {
      n = a;
    } else {
      n = a / Math.sqrt(1 - ePow2 * sinφ * sinφ);
    }

    double h;
    if (Math.abs(cosφ) < 1e-6) {
      // geocentricRadius
      final double r = Math.hypot(this.semiMajorAxisSq * cosφ, this.semiMinorAxis * sinφ)
        / Math.hypot(a * cosφ, b * sinφ);
      h = Math.abs(z) - r;
    } else {
      h = p / cosφ - n;
    }
    point.x = λ;
    point.y = φ;
    point.z = h;
  }

  // https://www.movable-type.co.uk/scripts/latlong-vincenty.html
  public double distanceMetres(final double lon1, final double lat1, final double lon2,
    final double lat2) {
    final double λ1 = Math.toRadians(lon1);
    final double φ1 = Math.toRadians(lat1);
    final double λ2 = Math.toRadians(lon2);
    final double φ2 = Math.toRadians(lat2);
    return distanceMetresRadians(λ1, φ1, λ2, φ2);
  }

  public double distanceMetres(final double lon1, final double lat1, final double h1,
    final double lon2, final double lat2, final double h2) {

    final double distance = distanceMetres(lon1, lat1, lon2, lat2);
    final double angleForwards = azimuth(lon1, lat1, lon2, lat2);
    final double angleBackwards = azimuth(lon2, lat2, lon1, lat1);

    final double r1 = radius(lat1, angleForwards);
    final double r2 = radius(lat2, angleBackwards);
    final double deltaH = h2 - h1;
    final double delhsq = deltaH * deltaH;
    final double twor = r1 + r2;
    final double lo = twor * Math.sin(distance / twor);
    final double losq = lo * lo;
    return Math.sqrt(losq * (h1 / r1 + 1.) * (h2 / r2 + 1.) + delhsq);
  }

  public double distanceMetresRadians(final double λ1, final double φ1, final double λ2,
    final double φ2) {
    final double f = this.flattening;
    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;

    final double deltaLambda = λ2 - λ1;
    final double tanU1 = (1 - f) * Math.tan(φ1);
    final double cosU1 = 1 / Math.sqrt(1 + tanU1 * tanU1);
    final double sinU1 = tanU1 * cosU1;
    final double tanU2 = (1 - f) * Math.tan(φ2);
    final double cosU2 = 1 / Math.sqrt(1 + tanU2 * tanU2);
    final double sinU2 = tanU2 * cosU2;

    double λ = deltaLambda;
    double lastLambda;
    double iterationLimit = 100;
    double cosSqAlpha;
    double sinSigma;
    double cos2SigmaM;
    double sigma;
    double cosSigma;
    double sinLambda;
    double cosLambda;

    do {
      sinLambda = Math.sin(λ);
      cosLambda = Math.cos(λ);
      final double sinSqSigma = cosU2 * sinLambda * (cosU2 * sinLambda)
        + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
      sinSigma = Math.sqrt(sinSqSigma);
      if (sinSigma == 0) { // co-incident points
        return 0;
      }
      cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
      sigma = Math.atan2(sinSigma, cosSigma);
      final double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
      cosSqAlpha = 1 - sinAlpha * sinAlpha;
      cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
      if (!Double.isFinite(cos2SigmaM)) {// equatorial line: cosSqAlpha=0 (§6)
        cos2SigmaM = 0;
      }
      final double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
      lastLambda = λ;
      λ = deltaLambda + (1 - C) * f * sinAlpha
        * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
    } while (Math.abs(λ - lastLambda) > 1e-12 && --iterationLimit > 0);
    if (iterationLimit == 0) {
      throw new IllegalStateException("Formula failed to converge");
    }

    final double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
    final double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
    final double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
    final double deltaSigmaSigma = B * sinSigma
      * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
        * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));

    final double s = b * A * (sigma - deltaSigmaSigma);
    return s;
  }

  public double ellipsoidDirection(final double lon1, final double lat1, final double h1,
    double xsi, double eta, final double lon2, final double lat2, final double h2, final double x0,
    final double y0, final double z0, double spatialDirection) {
    final double λ1 = Math.toRadians(lon1);
    final double φ1 = Math.toRadians(lat1);
    final double λ2 = Math.toRadians(lon2);
    final double φ2 = Math.toRadians(lat2);
    eta = Math.toRadians(eta);
    xsi = Math.toRadians(xsi);
    spatialDirection = Math.toRadians(spatialDirection);
    final double radians = ellipsoidDirectionRadians(λ1, φ1, h1, xsi, eta, λ2, φ2, h2, x0, y0, z0,
      spatialDirection);
    return Math.toDegrees(radians);
  }

  public double ellipsoidDirectionRadians(final double λ1, final double φ1, final double h1,
    final double xsi, final double eta, final double λ2, final double φ2, final double h2,
    final double x0, final double y0, final double z0, final double spatialDirection) {

    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;

    final double esq = (a * a - b * b) / (a * a);

    final double sinPhi1 = Math.sin(φ1);
    final double sinPhi2 = Math.sin(φ2);
    final double d__1 = Math.sqrt(1. - esq * (sinPhi1 * sinPhi1));
    final double d__4 = Math.sqrt(1 - esq * (sinPhi2 * sinPhi2));
    final double mm = (a * (1 - esq) / (d__1 * (d__1 * d__1))
      + a * (1 - esq) / (d__4 * (d__4 * d__4))) / 2;
    final double nm = (a / Math.sqrt(1 - esq * (sinPhi1 * sinPhi1))
      + a / Math.sqrt(1 - esq * (sinPhi2 * sinPhi2))) / 2;

    final double s12 = distanceMetresRadians(λ1, φ1, λ2, φ2);
    final double a12 = azimuthRadians(λ1, φ1, λ2, φ2);

    final double slopeDistance = this.slopeDistanceRadians(λ1, φ1, h1, λ2, φ2, h2, x0, y0, z0);

    final double dh = h2 - h1;
    final double c1 = (-xsi * Math.sin(a12) + eta * Math.cos(a12)) * dh
      / Math.sqrt(slopeDistance * slopeDistance - dh * dh);

    final double cosPhi2 = Math.cos(φ2);
    final double c2 = h2 * esq * Math.sin(a12) * Math.cos(a12) * cosPhi2 * cosPhi2 / mm;

    final double φm = (φ1 + φ2) / 2;
    final double cosPhim = Math.cos(φm);
    final double c3 = -esq * s12 * s12 * cosPhim * cosPhim * Math.sin(a12 * 2) / (nm * nm * 12);

    return spatialDirection + c1 + c2 + c3;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    }
    if (object == this) {
      return true;
    } else if (object instanceof Ellipsoid) {
      final Ellipsoid ellipsoid = (Ellipsoid)object;
      if (Math.round(1000000.0 * this.inverseFlattening) != Math
        .round(1000000.0 * ellipsoid.inverseFlattening)) {
        return false;
      } else if (this.semiMajorAxis != ellipsoid.semiMajorAxis) {
        return false;
      }
      return true;
    } else {
      return false;
    }

  }

  public boolean equalsExact(final Ellipsoid ellipsoid) {
    if (!Equals.equals(this.authority, ellipsoid.authority)) {
      return false;
      // } else if (deprecated != spheroid.deprecated) {
      // return false;
    } else if (this.inverseFlattening != ellipsoid.inverseFlattening) {
      return false;
      // } else if (!Equals.equal(name, spheroid.name)) {
      // return false;
    } else if (this.semiMajorAxis != ellipsoid.semiMajorAxis) {
      return false;
    } else if (this.semiMinorAxis != ellipsoid.semiMinorAxis) {
      return false;
    } else {
      return true;
    }
  }

  public double geocentricRadius(final double φ) {
    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;
    final double cosφ = Math.cos(φ);
    final double sinφ = Math.sin(φ);
    return Math.hypot(this.semiMajorAxisSq * cosφ, this.semiMinorAxis * sinφ)
      / Math.hypot(a * cosφ, b * sinφ);
  }

  public double geodeticAzimuth(final double lon1, final double lat1, final double h1, double xsi,
    double eta, final double lon2, final double lat2, final double h2, final double x0,
    final double y0, final double z0, double spaz) {
    final double λ1 = Math.toRadians(lon1);
    final double φ1 = Math.toRadians(lat1);
    final double λ2 = Math.toRadians(lon2);
    final double φ2 = Math.toRadians(lat2);
    spaz = Math.toRadians(spaz);
    xsi = Math.toRadians(xsi);
    eta = Math.toRadians(eta);
    final double radians = geodeticAzimuthRadians(λ1, φ1, h1, xsi, eta, λ2, φ2, h2, x0, y0, z0,
      spaz);
    return Math.toDegrees(radians);
  }

  public double geodeticAzimuthRadians(final double λ1, final double φ1, final double h1,
    final double xsi, final double eta, final double λ2, final double φ2, final double h2,
    final double x0, final double y0, final double z0, final double spaz) {
    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;

    final double φm = (φ1 + φ2) / 2.;
    final double esq = (a * a - b * b) / (a * a);
    final double sinPhi1 = Math.sin(φ1);
    final double mm1 = Math.sqrt(1. - esq * (sinPhi1 * sinPhi1));
    final double sinPhi2 = Math.sin(φ2);
    final double mm2 = Math.sqrt(1. - esq * (sinPhi2 * sinPhi2));
    final double mm = (a * (1 - esq) / (mm1 * (mm1 * mm1)) + a * (1 - esq) / (mm2 * (mm2 * mm2)))
      / 2;
    final double nm = (a / mm1 + a / mm2) / 2;

    final double a12 = azimuthRadians(λ1, φ1, λ2, φ2);

    final double s12 = distanceMetresRadians(λ1, φ1, λ2, φ2);

    // Always 0 as dh = 0
    final double c1 = 0;// (-(xsi) * Math.sin(a12) + eta * Math.cos(a12)) * 0 /
                        // sqrt(ssq - 0 * 0);

    final double cosPhi2 = Math.cos(φ2);
    final double c2 = h2 / mm * esq * Math.sin(a12) * Math.cos(a12) * (cosPhi2 * cosPhi2);

    final double cosPhim = Math.cos(φm);
    final double c3 = -esq * (s12 * s12) * (cosPhim * cosPhim) * Math.sin(a12 * 2) / (nm * nm * 12);

    double geodeticAzimuth = spaz - eta * Math.tan(φ1) + c1 + c2 + c3;
    if (geodeticAzimuth < 0) {
      geodeticAzimuth = Angle.PI_TIMES_2 + geodeticAzimuth;
    }
    return geodeticAzimuth;
  }

  public void geodeticToCartesian(final CoordinatesOperationPoint point) {
    final double λ = point.x;
    final double φ = point.y;
    double h = point.z;

    if (!Double.isFinite(h)) {
      h = 0;
    }
    final double sinφ = Math.sin(φ);
    final double cosφ = Math.cos(φ);
    final double ePow2 = this.eccentricitySquared;
    double n; // normalRadiusOfCurvature
    if (ePow2 == 0) {
      n = this.semiMajorAxis;
    } else {
      n = this.semiMajorAxis / Math.sqrt(1 - ePow2 * sinφ * sinφ);
    }
    final double nPlusHcosφ = (n + h) * cosφ;
    point.x = nPlusHcosφ * Math.cos(λ);
    point.y = nPlusHcosφ * Math.sin(λ);
    point.z = (n * (1 - ePow2) + h) * sinφ;
  }

  public Authority getAuthority() {
    return this.authority;
  }

  public CoordinatesOperation getCartesianToGeodeticOperation() {
    return this.cartesianToGeodeticOperation;
  }

  public double getEccentricity() {
    return this.eccentricity;
  }

  public double getEccentricitySquared() {
    return this.eccentricitySquared;
  }

  public double getFlattening() {
    return this.flattening;
  }

  public CoordinatesOperation getGeodeticToCartesianOperation() {
    return this.geodeticToCartesianOperation;
  }

  public double getInverseFlattening() {
    return this.inverseFlattening;
  }

  public String getName() {
    return this.name;
  }

  public double getRadiusFromDegrees(final double lat) {
    final double φ = Math.toRadians(lat);
    return getRadiusFromRadians(φ);
  }

  // R(φ)=sqrt(((a² cos(φ))²+(b² sin(φ))²)/((a cos(φ))²+(b sin(φ))²))
  public double getRadiusFromRadians(final double lat) {
    final double cosLat = Math.cos(lat);
    final double sinLat = Math.sin(lat);
    final double aCosLat = this.semiMajorAxis * cosLat;
    final double aSqCosLat = this.semiMajorAxisSq * cosLat;
    final double bSinLat = this.semiMinorAxis * sinLat;
    final double bSqSinLat = this.semiMinorAxisSq * sinLat;
    return Math.sqrt( //
      (aSqCosLat * aSqCosLat + bSqSinLat * bSinLat) / //
        (aCosLat * aCosLat + bSinLat * bSinLat)//
    );
  }

  public double getSemiMajorAxis() {
    return this.semiMajorAxis;
  }

  public double getSemiMinorAxis() {
    return this.semiMinorAxis;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(Math.round(1000000.0 * this.inverseFlattening) / 1000000.0);
    result = prime * result + (int)(temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(this.semiMajorAxis);
    result = prime * result + (int)(temp ^ temp >>> 32);
    return result;
  }

  public double horizontalEllipsoidFactor(final double lon1, final double lat1, final double h1,
    final double lon2, final double lat2, final double h2, final double spatialDistance) {
    final double λ1 = Math.toRadians(lon1);
    final double φ1 = Math.toRadians(lat1);
    final double λ2 = Math.toRadians(lon2);
    final double φ2 = Math.toRadians(lat2);
    return this.horizontalEllipsoidFactorRadians(λ1, φ1, h1, λ2, φ2, h2, spatialDistance);
  }

  public double horizontalEllipsoidFactorRadians(final double λ1, final double φ1, final double h1,
    final double λ2, final double φ2, final double h2, final double spatialDistance) {
    final double a12 = azimuthRadians(λ1, φ1, λ2, φ2);
    final double a21 = azimuthRadians(λ2, φ2, λ1, φ1);
    final double r1 = radius(φ1, a12);
    final double r2 = radius(φ2, a21);

    final double deltaH = Math.abs(h2 - h1);
    if (deltaH > 30) {
      return r1 / (r1 + h1);
    } else {
      return 1 / Math.sqrt((h1 / r1 + 1) * (h2 / r2 + 1));
    }
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  public boolean isSame(final Ellipsoid ellipsoid) {
    if (ellipsoid == null) {
      return false;
    } else if (Math.floor(1000000.0 * this.inverseFlattening) != Math
      .floor(1000000.0 * ellipsoid.inverseFlattening)) {
      return false;
    } else if (this.semiMajorAxis != ellipsoid.semiMajorAxis) {
      return false;
    }
    return true;
  }

  public double meridianRadiusOfCurvature(final double latitude) {
    final double er = 1.0 - this.eccentricitySquared * Math.sin(latitude) * Math.sin(latitude);
    final double el = Math.pow(er, 1.5);
    final double m0 = this.semiMajorAxis * (1.0 - this.eccentricitySquared) / el;
    return m0;
  }

  public double normalRadiusOfCurvature(final double φ) {
    final double sinφ = Math.sin(φ);
    if (this.eccentricitySquared == 0) {
      return this.semiMajorAxis;
    } else {
      return this.semiMajorAxis / Math.sqrt(1 - this.eccentricitySquared * sinφ * sinφ);
    }
  }

  public double primeVerticalRadiusOfCurvature(final double latitude) {
    final double t1 = this.semiMajorAxis * this.semiMajorAxis;
    final double t2 = t1 * Math.cos(latitude) * Math.cos(latitude);
    final double t3 = this.semiMinorAxis * this.semiMinorAxis * Math.sin(latitude)
      * Math.sin(latitude);
    final double n0 = t1 / Math.sqrt(t2 + t3);
    return n0;
  }

  protected void print(final String fn, final double a12) {
    System.out
      .println(fn + "=" + new BigDecimal(a12).setScale(18, RoundingMode.HALF_UP).toPlainString());
  }

  public double radius(final double lat, final double alpha) {
    final double φ = Math.toRadians(lat);
    final double eccentricitySquared = this.eccentricitySquared;
    final double sinPhi = Math.sin(φ);
    final double denom = Math.sqrt(1 - eccentricitySquared * (sinPhi * sinPhi));
    final double a = this.semiMajorAxis;
    final double pvrad = a / denom;
    final double merrad = a * (1 - eccentricitySquared) / (denom * (denom * denom));
    final double cosAlpha = Math.cos(alpha);
    final double sinAlpha = Math.sin(alpha);
    return pvrad * merrad / (pvrad * (cosAlpha * cosAlpha) + merrad * (sinAlpha * sinAlpha));
  }

  double radiusRadians(final double φ, final double alpha) {
    final double a = this.semiMajorAxis;
    final double ecc = this.eccentricity;
    final double sinPhi = Math.sin(φ);
    final double denom = Math.sqrt(1. - ecc * (sinPhi * sinPhi));
    final double pvrad = a / denom;
    final double merrad = a * (1. - ecc) / (denom * (denom * denom));
    final double cosAlpha = Math.cos(alpha);
    final double sinAlpha = Math.sin(alpha);
    return pvrad * merrad / (pvrad * (cosAlpha * cosAlpha) + merrad * (sinAlpha * sinAlpha));
  }

  public double slopeDistance(final double lon1, final double lat1, final double h1,
    final double xsi, final double eta, final double lon2, final double lat2, final double h2,
    final double x0, final double y0, final double z0) {
    final double λ1 = Math.toRadians(lon1);
    final double φ1 = Math.toRadians(lat1);
    final double λ2 = Math.toRadians(lon2);
    final double φ2 = Math.toRadians(lat2);

    return slopeDistanceRadians(λ1, φ1, h1, λ2, φ2, h2, x0, y0, z0);
  }

  public double slopeDistanceRadians(final double λ1, final double φ1, final double h1,
    final double λ2, final double φ2, final double h2, final double x0, final double y0,
    final double z0) {
    final double[] p1 = toCartesian(λ1, φ1, h1, x0, y0, z0);
    final double[] p2 = toCartesian(λ2, φ2, h2, x0, y0, z0);

    final double deltaX = p1[0] - p2[0];
    final double deltaY = p1[1] - p2[1];
    final double deltaZ = p1[2] - p2[2];
    final double ssq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    final double slopeDistance = Math.sqrt(ssq);

    return slopeDistance;
  }

  public double spatialDirection(final double lon1, final double lat1, final double h1, double xsi,
    double eta, final double lon2, final double lat2, final double h2, final double x0,
    final double y0, final double z0, final double d12) {

    final double a = this.semiMajorAxis;
    final double b = this.semiMinorAxis;

    final double λ1 = Math.toRadians(lon1);
    final double φ1 = Math.toRadians(lat1);
    final double λ2 = Math.toRadians(lon2);
    final double φ2 = Math.toRadians(lat2);
    eta = Math.toRadians(eta);
    xsi = Math.toRadians(xsi);

    final double φm = (φ1 + φ2) / 2;
    final double esq = (a * a - b * b) / (a * a);

    final double sinPhi1 = Math.sin(φ1);
    final double sinPhi2 = Math.sin(φ2);
    final double d__1 = Math.sqrt(1. - esq * (sinPhi1 * sinPhi1));
    final double d__4 = Math.sqrt(1 - esq * (sinPhi2 * sinPhi2));
    final double mm = (a * (1 - esq) / (d__1 * (d__1 * d__1))
      + a * (1 - esq) / (d__4 * (d__4 * d__4))) / 2.;
    final double nm = (a / Math.sqrt(1 - esq * (sinPhi1 * sinPhi1))
      + a / Math.sqrt(1 - esq * (sinPhi2 * sinPhi2))) / 2.;

    final double s12 = distanceMetresRadians(λ1, φ1, λ2, φ2);
    final double a12 = azimuthRadians(λ1, φ1, λ2, φ2);

    // final double slopeDistance = this.slopeDistanceRadians(λ1, φ1, h1, λ2,
    // φ2, h2,
    // x0,
    // y0, z0);

    // final double dh = 0;
    final double c1 = 0;// (-xsi * Math.sin(a12) + eta * Math.cos(a12)) * dh /
                        // Math.sqrt(ssq - dh *
                        // dh);

    final double cosPhi2 = Math.cos(φ2);
    final double c2 = h2 / mm * esq * Math.sin(a12) * Math.cos(a12) * (cosPhi2 * cosPhi2);

    final double cosPhim = Math.cos(φm);
    final double c3 = -esq * (s12 * s12) * (cosPhim * cosPhim) * Math.sin(a12 * 2.)
      / (nm * nm * 12.);

    return d12 - c1 - c2 - c3;
  }

  public double spatialDistance(final double lon1, final double lat1, final double h1,
    final double heightOfInstrument, final double heightOfTarget, final double lon2,
    final double lat2, final double h2, final double spatialDistance) {
    final double λ1 = Math.toRadians(lon1);
    final double φ1 = Math.toRadians(lat1);
    final double λ2 = Math.toRadians(lon2);
    final double φ2 = Math.toRadians(lat2);
    return spatialDistanceRadians(λ1, φ1, h1, heightOfInstrument, heightOfTarget, λ2, φ2, h2,
      spatialDistance);
  }

  public double spatialDistanceRadians(final double λ1, final double φ1, double h1,
    final double heightOfInstrument, final double heightOfTarget, final double λ2, final double φ2,
    double h2, final double spatialDistance) {

    final double a12 = azimuthRadians(λ1, φ1, λ2, φ2);
    final double a21 = azimuthRadians(λ2, φ2, λ1, φ1);
    final double r1 = radius(φ1, a12);
    final double r2 = radius(φ2, a21);

    h1 += heightOfInstrument;
    h2 += heightOfTarget;
    final double deltaH = h2 - h1;
    final double deltaHSq = deltaH * deltaH;
    if (spatialDistance * spatialDistance - deltaHSq >= 0) {
      final double twor = r1 + r2;
      final double lo = Math
        .sqrt((spatialDistance * spatialDistance - deltaHSq) / ((h1 / r1 + 1) * (h2 / r2 + 1)));
      return twor * Math.asin(lo / twor);
    } else {
      return spatialDistance;
    }
  }

  private double[] toCartesian(final double λ, final double φ, final double h, final double x0,
    final double y0, final double z0) {
    double n; // normalRadiusOfCurvature
    final double sinφ = Math.sin(φ);
    final double cosφ = Math.cos(φ);
    final double ePow2 = this.eccentricitySquared;
    if (ePow2 == 0) {
      n = this.semiMajorAxis;
    } else {
      n = this.semiMajorAxis / Math.sqrt(1 - ePow2 * sinφ * sinφ);
    }
    final double nPlusHcosφ = (n + h) * cosφ;
    final double x = nPlusHcosφ * Math.cos(λ);
    final double y = nPlusHcosφ * Math.sin(λ);
    final double z = (n * (1 - ePow2) + h) * sinφ;
    return new double[] {
      x0 + x, y0 + y, z0 + z
    };
  }

  @Override
  public String toString() {
    return this.name;
  }

  public void updateDigest(final MessageDigest digest) {
    Md5.update(digest, Math.floor(10 * this.semiMajorAxis));
    Md5.update(digest, Math.floor(1e6 * this.inverseFlattening));
  }

}
