package org.jeometry.coordinatesystem.model.systems;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jeometry.coordinatesystem.io.WktCsParser;
import org.jeometry.coordinatesystem.model.Authority;
import org.jeometry.coordinatesystem.model.BaseAuthority;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.CoordinateSystemType;
import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.ParameterName;
import org.jeometry.coordinatesystem.model.ParameterValue;
import org.jeometry.coordinatesystem.model.ParameterValueBigDecimal;
import org.jeometry.coordinatesystem.model.PrimeMeridian;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.SingleParameterName;
import org.jeometry.coordinatesystem.model.VerticalCoordinateSystem;
import org.jeometry.coordinatesystem.model.datum.GeodeticDatum;
import org.jeometry.coordinatesystem.model.datum.VerticalDatum;
import org.jeometry.coordinatesystem.model.unit.AngularUnit;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;
import org.jeometry.coordinatesystem.util.ByteArray;
import org.slf4j.LoggerFactory;

public class EsriCoordinateSystems {
  private static Map<Integer, CoordinateSystem> COORDINATE_SYSTEM_BY_ID = new HashMap<>();

  private static Map<ByteArray, List<Integer>> COORDINATE_SYSTEM_IDS_BY_DIGEST = new HashMap<>();

  private static final Map<String, AngularUnit> ANGULAR_UNITS_BY_NAME = new TreeMap<>();

  private static final Map<String, LinearUnit> LINEAR_UNITS_BY_NAME = new TreeMap<>();

  private static Map<ParameterName, ParameterValue> convertParameters(
    final Map<String, String> parameters) {
    final Map<ParameterName, ParameterValue> parameterValues = new LinkedHashMap<>();
    for (final Entry<String, String> parameter : parameters.entrySet()) {
      final String name = parameter.getKey();

      final String value = parameter.getValue();
      final ParameterName parameterName = new SingleParameterName(name);
      final ParameterValue parameterValue = new ParameterValueBigDecimal(value);
      parameterValues.put(parameterName, parameterValue);
    }
    return parameterValues;
  }

  @SuppressWarnings("unchecked")
  public static <C extends CoordinateSystem> C getCoordinateSystem(final int crsId) {
    if (crsId >= 2000000) {
      return null;
    } else {
      CoordinateSystem coordinateSystem = COORDINATE_SYSTEM_BY_ID.get(crsId);
      if (coordinateSystem == null) {
        coordinateSystem = getGeographicCoordinateSystem(crsId);
        if (coordinateSystem == null) {
          coordinateSystem = getProjectedCoordinateSystem(crsId);
          if (coordinateSystem == null) {
            coordinateSystem = getVerticalCoordinateSystem(crsId);
          }
        }
      }
      return (C)coordinateSystem;
    }
  }

  private static List<Integer> getCoordinateSystemIdsByDigest(
    final CoordinateSystem coordinateSystem, final ByteArray digest) {
    List<Integer> ids = COORDINATE_SYSTEM_IDS_BY_DIGEST.get(digest);
    if (ids == null) {
      final byte[] bytes = new byte[16];
      final ByteArray newDigest = new ByteArray(bytes);
      final CoordinateSystemType type = coordinateSystem.getCoordinateSystemType();
      if (type.isCompound()) {
        return Collections.emptyList();
      } else {
        try (
          DataInputStream reader = newDataInputStream(type + ".digest")) {
          if (reader != null) {
            while (true) {
              reader.read(bytes);
              final short count = reader.readShort();
              if (digest.equals(newDigest)) {
                ids = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                  final int csId = reader.readInt();
                  ids.add(csId);
                }
                COORDINATE_SYSTEM_IDS_BY_DIGEST.put(digest, ids);
                return ids;
              } else {
                for (int i = 0; i < count; i++) {
                  reader.readInt();
                }
              }
            }
          }
        } catch (final EOFException e) {
        } catch (final IOException e) {
          log("ellipsoid", e);
        }
      }
    } else {
      return ids;
    }
    return Collections.emptyList();
  }

  public static GeographicCoordinateSystem getGeographicCoordinateSystem(final int id) {
    GeographicCoordinateSystem coordinateSystem = (GeographicCoordinateSystem)COORDINATE_SYSTEM_BY_ID
      .get(id);
    if (coordinateSystem == null) {
      try (
        final DataInputStream reader = newDataInputStream("Geographic.cs")) {
        if (reader != null) {
          while (true) {
            final int coordinateSystemId = reader.readInt();
            final String csName = readStringUtf8ByteCount(reader);
            final String datumName = readStringUtf8ByteCount(reader);
            final String spheroidName = readStringUtf8ByteCount(reader);
            final double semiMajorAxis = reader.readDouble();
            final double inverseFlattening = reader.readDouble();
            final String primeMeridianName = readStringUtf8ByteCount(reader);
            final double longitude = reader.readDouble();
            final String angularUnitName = readStringUtf8ByteCount(reader);
            final double conversionFactor = reader.readDouble();

            if (id == coordinateSystemId) {
              final Ellipsoid ellipsoid = new Ellipsoid(spheroidName, semiMajorAxis,
                inverseFlattening, null);
              final PrimeMeridian primeMeridian = new PrimeMeridian(primeMeridianName, longitude,
                null);
              final GeodeticDatum geodeticDatum = new GeodeticDatum(null, datumName, null, false,
                ellipsoid, primeMeridian);

              AngularUnit angularUnit = ANGULAR_UNITS_BY_NAME.get(angularUnitName);
              if (angularUnit == null) {
                angularUnit = new AngularUnit(angularUnitName, conversionFactor, null);
                ANGULAR_UNITS_BY_NAME.put(angularUnitName, angularUnit);
              }

              final Authority authority = new BaseAuthority("ESRI", coordinateSystemId);
              coordinateSystem = new GeographicCoordinateSystem(coordinateSystemId, csName,
                geodeticDatum, primeMeridian, angularUnit, null, authority);
              COORDINATE_SYSTEM_BY_ID.put(id, coordinateSystem);
              return coordinateSystem;
            }
          }
        }
      } catch (final EOFException e) {
      } catch (final IOException e) {
        log("ellipsoid", e);
        return null;
      }
    }
    return coordinateSystem;
  }

  public static int getIdUsingDigest(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return 0;
    } else {
      final ByteArray digest = new ByteArray(coordinateSystem.md5Digest());
      final List<Integer> ids = getCoordinateSystemIdsByDigest(coordinateSystem, digest);
      if (ids.isEmpty()) {
        return 0;
      } else if (ids.size() == 1) {
        return ids.get(0);
      } else {
        final List<CoordinateSystem> coordinateSystems = new ArrayList<>();
        for (final int coordinateSystemId : ids) {
          final CoordinateSystem coordinateSystem2 = getCoordinateSystem(coordinateSystemId);
          if (coordinateSystem2 != null) {
            if (coordinateSystem.getCoordinateSystemName()
              .equalsIgnoreCase(coordinateSystem2.getCoordinateSystemName())) {
              return coordinateSystemId;
            } else {
              coordinateSystems.add(coordinateSystem2);
            }
          }
        }
        for (final CoordinateSystem coordinateSystem2 : coordinateSystems) {
          if (coordinateSystem.isSame(coordinateSystem2)) {
            return coordinateSystem2.getCoordinateSystemId();
          }
        }
        // Match base on names etc
        return ids.get(0);
      }
    }
  }

  public static ProjectedCoordinateSystem getProjectedCoordinateSystem(final int id) {
    ProjectedCoordinateSystem coordinateSystem = (ProjectedCoordinateSystem)COORDINATE_SYSTEM_BY_ID
      .get(id);
    if (coordinateSystem == null) {
      try (
        final DataInputStream reader = newDataInputStream("Projected.cs")) {
        if (reader != null) {
          while (true) {
            final int coordinateSystemId = reader.readInt();
            final String csName = readStringUtf8ByteCount(reader);

            final int geographicCoordinateSystemId = reader.readInt();
            final String projectionName = readStringUtf8ByteCount(reader);
            final Map<String, String> parameters = readParameters(reader);
            final String unitName = readStringUtf8ByteCount(reader);
            final double conversionFactor = reader.readDouble();

            if (id == coordinateSystemId) {
              LinearUnit linearUnit = LINEAR_UNITS_BY_NAME.get(unitName);
              if (linearUnit == null) {
                linearUnit = new LinearUnit(unitName, conversionFactor);
                LINEAR_UNITS_BY_NAME.put(unitName, linearUnit);
              }
              final Authority authority = new BaseAuthority("ESRI", coordinateSystemId);
              final GeographicCoordinateSystem geographicCoordinateSystem = getGeographicCoordinateSystem(
                geographicCoordinateSystemId);
              final Map<ParameterName, ParameterValue> parameterValues = convertParameters(
                parameters);
              coordinateSystem = new ProjectedCoordinateSystem(coordinateSystemId, csName,
                geographicCoordinateSystem, projectionName, parameterValues, linearUnit, authority);
              COORDINATE_SYSTEM_BY_ID.put(id, coordinateSystem);
              return coordinateSystem;
            }
          }
        }
      } catch (final EOFException e) {
      } catch (final IOException e) {
        log("ellipsoid", e);
        return null;
      }
    }
    return coordinateSystem;
  }

  public static VerticalCoordinateSystem getVerticalCoordinateSystem(final int id) {
    VerticalCoordinateSystem coordinateSystem = (VerticalCoordinateSystem)COORDINATE_SYSTEM_BY_ID
      .get(id);
    if (coordinateSystem == null) {
      try (
        final DataInputStream reader = newDataInputStream("Vertical.css")) {
        if (reader != null) {
          while (true) {
            final int coordinateSystemId = reader.readInt();
            final String csName = readStringUtf8ByteCount(reader);
            final String datumName = readStringUtf8ByteCount(reader);
            final Map<String, String> parameters = readParameters(reader);
            final String linearUnitName = readStringUtf8ByteCount(reader);
            final double conversionFactor = reader.readDouble();

            if (id == coordinateSystemId) {
              final VerticalDatum verticalDatum = new VerticalDatum(null, datumName, 0);

              LinearUnit linearUnit = LINEAR_UNITS_BY_NAME.get(linearUnitName);
              if (linearUnit == null) {
                linearUnit = new LinearUnit(linearUnitName, conversionFactor, null);
                LINEAR_UNITS_BY_NAME.put(linearUnitName, linearUnit);
              }

              final Authority authority = new BaseAuthority("ESRI", coordinateSystemId);
              final Map<ParameterName, ParameterValue> parameterValues = convertParameters(
                parameters);

              coordinateSystem = new VerticalCoordinateSystem(authority, csName, verticalDatum,
                parameterValues, linearUnit, Collections.emptyList());
              COORDINATE_SYSTEM_BY_ID.put(id, coordinateSystem);
              return coordinateSystem;
            }
          }
        }
      } catch (final EOFException e) {
      } catch (final IOException e) {
        if (!e.getMessage().contains("closed")) {
          log("VerticalCoordinateSystem", e);
        }
        return null;
      }
    }
    return coordinateSystem;
  }

  private static void log(final String message, final IOException e) {
    LoggerFactory.getLogger(EsriCoordinateSystems.class).error(message, e);
  }

  private static DataInputStream newDataInputStream(final String fileName) {
    final InputStream in = EpsgCoordinateSystems.class
      .getResourceAsStream("/org/jeometry/coordinatesystem/esri/" + fileName);
    final BufferedInputStream bufferedIn = new BufferedInputStream(in);
    return new DataInputStream(bufferedIn);
  }

  /**
   * Parse the coordinate system from the WKT. If it is a standard one then
   *  {@link EpsgCoordinateSystems#getCoordinateSystem(int)} will be used to return that
   *  coordinate system.
   *
   *  @param wkt The WKT coordinate system definition
   *  @param <C> The type of coordinate system to cast the result to.
   *  @return The coordinate system or null if not found.
   */
  @SuppressWarnings("unchecked")
  public static <C extends CoordinateSystem> C readCoordinateSystem(final String wkt) {
    final CoordinateSystem coordinateSystem = WktCsParser.read(wkt);
    return (C)readCoordinateSystemPost(coordinateSystem);
  }

  private static CoordinateSystem readCoordinateSystemPost(
    final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return null;
    } else {
      int id = coordinateSystem.getCoordinateSystemId();
      if (id <= 0) {
        id = getIdUsingDigest(coordinateSystem);
      }
      if (id > 0) {
        final CoordinateSystem epsgCoordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(id);
        if (epsgCoordinateSystem == null) {
          final CoordinateSystem esriCoordinateSystem = getCoordinateSystem(id);
          if (esriCoordinateSystem != null) {
            return esriCoordinateSystem;
          }
        } else {
          return epsgCoordinateSystem;
        }
      }
      return coordinateSystem;
    }
  }

  private static Map<String, String> readParameters(final DataInputStream reader)
    throws IOException {
    final byte parameterCount = reader.readByte();
    final Map<String, String> parameters = new LinkedHashMap<>();
    for (int i = 0; i < parameterCount; i++) {
      final String name = readStringUtf8ByteCount(reader);
      final String value = readStringUtf8ByteCount(reader);
      parameters.put(name, value);
    }
    return parameters;
  }

  private static String readStringUtf8ByteCount(final DataInputStream reader) throws IOException {
    final int byteCount = reader.readInt();
    if (byteCount < 0) {
      return null;
    } else if (byteCount == 0) {
      return "";
    } else {
      final byte[] bytes = new byte[byteCount];
      reader.read(bytes);
      int i = 0;
      for (; i < bytes.length; i++) {
        final byte character = bytes[i];
        if (character == 0) {
          return new String(bytes, 0, i, StandardCharsets.UTF_8);
        }
      }
      return new String(bytes, 0, i, StandardCharsets.UTF_8);
    }
  }

}
