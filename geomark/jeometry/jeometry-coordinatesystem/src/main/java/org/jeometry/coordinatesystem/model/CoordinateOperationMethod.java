package org.jeometry.coordinatesystem.model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.jeometry.coordinatesystem.operation.projection.AlbersConicEqualArea;
import org.jeometry.coordinatesystem.operation.projection.CoordinatesProjection;
import org.jeometry.coordinatesystem.operation.projection.LambertConicConformal;
import org.jeometry.coordinatesystem.operation.projection.LambertConicConformal1SP;
import org.jeometry.coordinatesystem.operation.projection.Mercator1SP;
import org.jeometry.coordinatesystem.operation.projection.Mercator1SPSpherical;
import org.jeometry.coordinatesystem.operation.projection.Mercator2SP;
import org.jeometry.coordinatesystem.operation.projection.ProjectionFactory;
import org.jeometry.coordinatesystem.operation.projection.TransverseMercatorUsgs;
import org.jeometry.coordinatesystem.operation.projection.WebMercator;
import org.jeometry.coordinatesystem.util.Equals;
import org.jeometry.coordinatesystem.util.Md5;

public class CoordinateOperationMethod
  implements Serializable, Comparable<CoordinateOperationMethod> {
  public static final String ALBERS_EQUAL_AREA = "Albers_Equal_Area";

  public static final String LAMBERT_CONIC_CONFORMAL_1SP = "Lambert_Conic_Conformal_1SP";

  public static final String LAMBERT_CONIC_CONFORMAL_2SP = "Lambert_Conic_Conformal_2SP";

  public static final String LAMBERT_CONIC_CONFORMAL_2SP_BELGIUM = "Lambert_Conic_Conformal_2SP_Belgium";

  public static final String MERCATOR = "Mercator";

  public static final String MERCATOR_1SP = "Mercator_1SP";

  public static final String MERCATOR_1SP_SPHERICAL = "Mercator_1SP_Spherical";

  public static final String MERCATOR_2SP = "Mercator_2SP";

  public static final String POPULAR_VISUALISATION_PSEUDO_MERCATOR = "Popular_Visualisation_Pseudo_Mercator";

  private static final Map<String, String> PROJECTION_ALIASES = new TreeMap<>();

  private static final long serialVersionUID = 6199958151692874551L;

  public static final String TRANSVERSE_MERCATOR = "Transverse_Mercator";

  private static final Map<String, Function<ProjectedCoordinateSystem, CoordinatesProjection>> FACTORY_BY_NAME = new HashMap<>();

  static {
    for (final String alias : Arrays.asList(ALBERS_EQUAL_AREA, "Albers", "Albers_Equal_Area_Conic",
      "Albers_Conic_Equal_Area")) {
      addAlias(alias, ALBERS_EQUAL_AREA);
    }

    addAlias(LAMBERT_CONIC_CONFORMAL_1SP, LAMBERT_CONIC_CONFORMAL_1SP);

    addAlias(LAMBERT_CONIC_CONFORMAL_2SP, LAMBERT_CONIC_CONFORMAL_2SP);

    addAlias(LAMBERT_CONIC_CONFORMAL_2SP_BELGIUM, LAMBERT_CONIC_CONFORMAL_2SP_BELGIUM);

    addAlias(MERCATOR, MERCATOR);

    addAlias(MERCATOR_1SP, MERCATOR_1SP);

    addAlias(MERCATOR_1SP_SPHERICAL, MERCATOR_1SP_SPHERICAL);

    addAlias(MERCATOR_2SP, MERCATOR_2SP);

    addAlias(POPULAR_VISUALISATION_PSEUDO_MERCATOR, POPULAR_VISUALISATION_PSEUDO_MERCATOR);

    registerCoordinatesProjection(AlbersConicEqualArea::new, ALBERS_EQUAL_AREA);
    registerCoordinatesProjection(TransverseMercatorUsgs::new, TRANSVERSE_MERCATOR);
    registerCoordinatesProjection(Mercator1SP::new, MERCATOR);
    registerCoordinatesProjection(WebMercator::new, POPULAR_VISUALISATION_PSEUDO_MERCATOR);
    registerCoordinatesProjection(Mercator1SP::new, MERCATOR_1SP);
    registerCoordinatesProjection(Mercator2SP::new, MERCATOR_2SP);
    registerCoordinatesProjection(Mercator1SPSpherical::new, MERCATOR_1SP_SPHERICAL);
    registerCoordinatesProjection(LambertConicConformal1SP::new, LAMBERT_CONIC_CONFORMAL_1SP);
    registerCoordinatesProjection(LambertConicConformal::new, LAMBERT_CONIC_CONFORMAL_2SP);
    registerCoordinatesProjection(LambertConicConformal::new, LAMBERT_CONIC_CONFORMAL_2SP_BELGIUM);
  }

  public static void addAlias(final String name, final String alias) {
    PROJECTION_ALIASES.put(normalizeName(name).toLowerCase(), normalizeName(alias));
  }

  public static CoordinateOperationMethod getMethod(String methodName,
    final Map<ParameterName, ?> parameters) {
    if (methodName != null) {
      if ("Stereographic_North_Pole".equals(methodName)) {
        if (parameters.size() == 5) {
          methodName = "Oblique_Stereographic";
        }
      } else if ("Stereographic_South_Pole".equals(methodName)) {
        if (parameters.size() == 5) {
          methodName = "Polar_Stereographic_variant_B";
        }
      } else if ("Lambert_Conformal_Conic".equals(methodName)) {
        if (parameters.containsKey(ParameterNames.STANDARD_PARALLEL_2)) {
          methodName = "Lambert_Conic_Conformal_2SP";
        } else {
          methodName = "Lambert_Conic_Conformal_1SP";
        }
      } else if ("Mercator".equals(methodName)) {
        if (parameters.containsKey(ParameterNames.STANDARD_PARALLEL_1)) {
          if (parameters.containsKey(ParameterNames.LATITUDE_OF_ORIGIN)) {
            methodName = "Mercator_2SP";
          } else {
            methodName = "Mercator_2SP";
          }
        } else {
          methodName = "Mercator_1SP";
        }
      }
      return new CoordinateOperationMethod(methodName);
    } else {
      return null;
    }

  }

  public static String getNormalizedName(final String name) {
    if (name == null) {
      return null;
    } else {
      final String normalizedName = name.replaceAll("[^a-z0-9_]", "");
      return PROJECTION_ALIASES.getOrDefault(normalizedName.toLowerCase(), normalizedName);
    }
  }

  public static String normalizeName(final String name) {
    return name.replaceAll(" ", "_").replaceAll("[^a-zA-Z0-9_]", "");
  }

  private static void registerCoordinatesProjection(
    final Function<ProjectedCoordinateSystem, CoordinatesProjection> factory,
    final String... names) {
    for (final String name : names) {
      FACTORY_BY_NAME.put(name, factory);
    }
  }

  private Authority authority;

  private final String name;

  private final String normalizedName;

  private boolean reverse;

  private boolean deprecated;

  private List<ParameterName> parameterNames = new ArrayList<>();

  private final List<Boolean> parameterReversal = new ArrayList<>();

  private final Function<ProjectedCoordinateSystem, CoordinatesProjection> coordinatesProjectionFactory;

  public CoordinateOperationMethod(final int id, final String name, final boolean reverse,
    final boolean deprecated, final List<ParameterName> parameterNames,
    final List<Byte> parameterReversal) {
    this(name);
    this.authority = new EpsgAuthority(id);
    this.reverse = reverse;
    this.deprecated = deprecated;
    this.parameterNames = parameterNames;
    for (final Byte reverseParamater : parameterReversal) {
      this.parameterReversal.add(reverseParamater == 1);
    }
  }

  public CoordinateOperationMethod(final String name) {
    this.name = name;

    if (name == null) {
      this.normalizedName = null;
    } else {
      final String normalizedName = normalizeName(name);
      this.normalizedName = PROJECTION_ALIASES.getOrDefault(normalizedName.toLowerCase(),
        normalizedName);
    }

    this.coordinatesProjectionFactory = FACTORY_BY_NAME.get(this.normalizedName);
  }

  @Override
  public int compareTo(final CoordinateOperationMethod coordinateOperationMethod) {
    return getNormalizedName().compareTo(coordinateOperationMethod.getNormalizedName());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof CoordinateOperationMethod) {
      final CoordinateOperationMethod coordinateOperationMethod = (CoordinateOperationMethod)obj;
      if (Equals.equals(this.authority, coordinateOperationMethod.authority)) {
        return true;
      } else {
        return getNormalizedName().equals(coordinateOperationMethod.getNormalizedName());
      }
    }
    return false;
  }

  public Authority getAuthority() {
    return this.authority;
  }

  public String getName() {
    return this.name;
  }

  public String getNormalizedName() {
    return this.normalizedName;
  }

  public List<ParameterName> getParameterNames() {
    return this.parameterNames;
  }

  @Override
  public int hashCode() {
    return getNormalizedName().hashCode();
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  public boolean isReverse() {
    return this.reverse;
  }

  public boolean isSame(final CoordinateOperationMethod coordinateOperationMethod) {
    if (coordinateOperationMethod == null) {
      return false;
    } else {
      return this.normalizedName.equals(coordinateOperationMethod.normalizedName);
    }
  }

  public synchronized CoordinatesProjection newCoordinatesProjection(
    final ProjectedCoordinateSystem coordinateSystem) {
    if (this.coordinatesProjectionFactory == null) {
      return ProjectionFactory.newCoordinatesProjection(coordinateSystem);
    } else {
      return this.coordinatesProjectionFactory.apply(coordinateSystem);
    }
  }

  public boolean setParameter(final Map<ParameterName, ParameterValue> parameterValues,
    final ParameterName parameterName, final ParameterValue parameterValue) {
    if (parameterValues.containsKey(parameterName)) {
      parameterValues.put(parameterName, parameterValue);
      return true;
    } else {
      for (final ParameterName methodParameterName : this.parameterNames) {
        if (methodParameterName.equals(parameterName)) {
          parameterValues.put(methodParameterName, parameterValue);
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public String toString() {
    return getNormalizedName();
  }

  public void updateDigest(final MessageDigest digest) {
    Md5.update(digest, this.normalizedName);
  }
}
