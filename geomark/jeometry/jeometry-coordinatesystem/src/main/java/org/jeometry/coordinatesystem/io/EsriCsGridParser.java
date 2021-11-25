package org.jeometry.coordinatesystem.io;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.HorizontalCoordinateSystem;
import org.jeometry.coordinatesystem.model.ParameterName;
import org.jeometry.coordinatesystem.model.ParameterNames;
import org.jeometry.coordinatesystem.model.ParameterValue;
import org.jeometry.coordinatesystem.model.ParameterValueNumber;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;
import org.jeometry.coordinatesystem.model.systems.EpsgId;
import org.jeometry.coordinatesystem.model.systems.EsriCoordinateSystems;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;

public class EsriCsGridParser {
  private static void addParameter(final Map<ParameterName, ParameterValue> parameterValues,
    final ParameterName parameterName, final List<String> parameters, final int index) {
    final String valueString = parameters.get(index);
    final double value = Double.parseDouble(valueString);
    final ParameterValueNumber parameterValue = new ParameterValueNumber(value);
    parameterValues.put(parameterName, parameterValue);

  }

  private static ProjectedCoordinateSystem getAlbers(
    final GeographicCoordinateSystem geographicCoordinateSystem,
    final Map<String, String> properties, final List<String> parameters) {
    ProjectedCoordinateSystem coordinateSystem;

    final Map<ParameterName, ParameterValue> parameterValues = new LinkedHashMap<>();
    addParameter(parameterValues, ParameterNames.STANDARD_PARALLEL_1, parameters, 0);
    addParameter(parameterValues, ParameterNames.STANDARD_PARALLEL_2, parameters, 1);
    addParameter(parameterValues, ParameterNames.LATITUDE_OF_CENTER, parameters, 2);
    addParameter(parameterValues, ParameterNames.LONGITUDE_OF_CENTER, parameters, 3);
    addParameter(parameterValues, ParameterNames.FALSE_EASTING, parameters, 4);
    addParameter(parameterValues, ParameterNames.FALSE_NORTHING, parameters, 5);

    final String name = geographicCoordinateSystem.getCoordinateSystemName() + " / Unknown Albers";
    final LinearUnit linearUnit = EpsgCoordinateSystems.getLinearUnit("metres");
    coordinateSystem = new ProjectedCoordinateSystem(-1, name, geographicCoordinateSystem,
      "Transverse_Mercator", parameterValues, linearUnit, null);
    return coordinateSystem;
  }

  private static GeographicCoordinateSystem getGeographicCoordinateSystem(
    final Map<String, String> properties, final List<String> projectionParameters) {
    final String datum = properties.getOrDefault("datum", "").toUpperCase();
    int coordinateSystemId = -1;
    if ("NAD83".equals(datum)) {
      coordinateSystemId = EpsgId.NAD83;
    } else if ("NAD27".equals(datum)) {
      coordinateSystemId = EpsgId.NAD27;
    } else if ("WGS84".equals(datum)) {
      coordinateSystemId = EpsgId.WGS84;
    } else if ("WGS72".equals(datum)) {
      coordinateSystemId = EpsgId.WGS72;
    } else if ("EUR".equals(datum) || "ED50".equals(datum)) {
      coordinateSystemId = 4230;
    } else if ("GDA94".equals(datum)) {
      coordinateSystemId = 4283;
    } else {
      final String spheroid = properties.getOrDefault("spheroid", "").toUpperCase();
      if ("INT1909".equals(spheroid) || "INTERNATIONAL1909".equals(spheroid)) {
        coordinateSystemId = 4230;
      } else if ("AIRY".equals(datum)) {
        coordinateSystemId = 4001;
      } else if ("CLARKE1866".equals(datum)) {
        coordinateSystemId = 4008;
      } else if ("GRS80".equals(datum)) {
        coordinateSystemId = 4019;
      } else if ("KRASOVSKY".equals(spheroid) || "KRASSOVSKY".equals(spheroid)
        || "KRASSOWSKY".equals(spheroid)) {
        coordinateSystemId = 4024;
      } else if ("BESSEL".equals(datum)) {
        coordinateSystemId = 4004;
      } else {
        if (projectionParameters.size() == 2) {
          final double semiMajorAxis = Double.parseDouble(projectionParameters.get(0));
          final double semiMinorAxis = Double.parseDouble(projectionParameters.get(1));
          final double inverseFlattening = semiMajorAxis / (semiMajorAxis - semiMinorAxis);
          final GeographicCoordinateSystem coordinateSystem = new GeographicCoordinateSystem(
            "Unknown", new Ellipsoid("Unknown", semiMajorAxis, inverseFlattening));
          coordinateSystemId = EsriCoordinateSystems.getIdUsingDigest(coordinateSystem);
          if (coordinateSystemId <= 0) {
            return coordinateSystem;
          }
        } else {
          return null;
        }
      }
    }
    return EsriCoordinateSystems.getCoordinateSystem(coordinateSystemId);
  }

  private static ProjectedCoordinateSystem getUtm(
    final GeographicCoordinateSystem geographicCoordinateSystem, final int zone,
    final boolean north) {
    ProjectedCoordinateSystem coordinateSystem;
    final Map<ParameterName, ParameterValue> parameters = new LinkedHashMap<>();
    parameters.put(ParameterNames.LATITUDE_OF_ORIGIN, new ParameterValueNumber(0));
    parameters.put(ParameterNames.CENTRAL_MERIDIAN, new ParameterValueNumber(zone * 6 - 183));
    parameters.put(ParameterNames.SCALE_FACTOR, new ParameterValueNumber(0.9996));
    parameters.put(ParameterNames.FALSE_EASTING, new ParameterValueNumber(500000.0));

    String suffix;
    if (north) {
      suffix = "N";
      parameters.put(ParameterNames.FALSE_NORTHING, new ParameterValueNumber(0));
    } else {
      parameters.put(ParameterNames.FALSE_NORTHING, new ParameterValueNumber(10000000));
      suffix = "S";
    }

    final String name = geographicCoordinateSystem.getCoordinateSystemName() + " / UTM zone " + zone
      + suffix;
    final LinearUnit linearUnit = EpsgCoordinateSystems.getLinearUnit("metres");
    coordinateSystem = new ProjectedCoordinateSystem(-1, name, geographicCoordinateSystem,
      "Transverse_Mercator", parameters, linearUnit, null);
    return coordinateSystem;
  }

  private static ProjectedCoordinateSystem getUtm(
    final GeographicCoordinateSystem geographicCoordinateSystem,
    final Map<String, String> properties, final List<String> parameters) {
    final String datum = properties.getOrDefault("datum", "");
    int zone = Integer.parseInt(properties.getOrDefault("zone", "-1"));
    int coordinateSystemId = -1;
    ProjectedCoordinateSystem coordinateSystem = null;
    if (zone > 0 || zone < 61) {
      if ("NAD83".equalsIgnoreCase(datum)) {
        coordinateSystemId = EpsgId.nad83Utm(zone);
      } else if ("NAD27".equalsIgnoreCase(datum)) {
        coordinateSystemId = EpsgId.nad27Utm(zone);
      } else if ("WGS84".equalsIgnoreCase(datum)) {
        coordinateSystemId = EpsgId.wgs84Utm(zone);
      } else {
        // Indicates North vs South
        final double yShift = Double.parseDouble(properties.getOrDefault("yshift", "0"));
        final boolean north = yShift >= 0;

        coordinateSystem = getUtm(geographicCoordinateSystem, zone, north);
      }
    } else if (parameters.size() == 2) {
      final double centralMeridian = Double.parseDouble(parameters.get(0));
      final double northIndicator = Double.parseDouble(parameters.get(1));
      if (centralMeridian >= -180.0 && centralMeridian <= 180.0) {
        zone = (int)Math.round((centralMeridian + 183.0) / 6.0);
        return getUtm(geographicCoordinateSystem, zone, northIndicator >= 0.0);
      } else {
        return null;
      }
    }
    if (coordinateSystem == null) {
      return EpsgCoordinateSystems.getCoordinateSystem(coordinateSystemId);
    } else {
      return coordinateSystem;
    }
  }

  @SuppressWarnings("unchecked")
  public static <C extends HorizontalCoordinateSystem> C parse(final String projectionText) {
    final String[] lines = projectionText.split("[\\n\\r]+");
    final Map<String, String> properties = new LinkedHashMap<>();
    final List<String> projectionParameters = new ArrayList<>();
    boolean inParameters = false;
    for (String line : lines) {
      line = line.trim().replaceAll("/\\*", "");
      if (line.equals("projection")) {
        inParameters = true;
      } else if (inParameters) {
        projectionParameters.add(line);
      } else {
        final String[] parts = line.split("\\s+");
        if (parts.length == 2) {
          final String name = parts[0].toLowerCase();
          final String value = parts[1];
          properties.put(name, value);
        }
      }
    }
    final String projection = properties.getOrDefault("projection", "");

    HorizontalCoordinateSystem coordinateSystem = null;
    final GeographicCoordinateSystem geographicCoordinateSystem = getGeographicCoordinateSystem(
      properties, projectionParameters);
    if ("GEOGRAPHIC".equalsIgnoreCase(projection)) {
      coordinateSystem = geographicCoordinateSystem;
    } else {
      final int coordinateSystemId = -1;
      if ("UTM".equalsIgnoreCase(projection)) {
        coordinateSystem = getUtm(geographicCoordinateSystem, properties, projectionParameters);
      } else if ("ALBERS".equalsIgnoreCase(projection)) {
        coordinateSystem = getAlbers(geographicCoordinateSystem, properties, projectionParameters);
      } else {
        coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(coordinateSystemId);
      }
    }
    coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(coordinateSystem);
    if (coordinateSystem == null) {
      throw new IllegalArgumentException("Projection not supported: " + projectionText);
    } else {
      return (C)coordinateSystem;
    }
  }
}
