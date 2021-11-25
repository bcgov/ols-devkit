package org.jeometry.coordinatesystem.model.systems;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jeometry.coordinatesystem.model.Area;
import org.jeometry.coordinatesystem.model.Authority;
import org.jeometry.coordinatesystem.model.Axis;
import org.jeometry.coordinatesystem.model.AxisName;
import org.jeometry.coordinatesystem.model.CompoundCoordinateSystem;
import org.jeometry.coordinatesystem.model.CoordinateOperation;
import org.jeometry.coordinatesystem.model.CoordinateOperationMethod;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.EngineeringCoordinateSystem;
import org.jeometry.coordinatesystem.model.EpsgAuthority;
import org.jeometry.coordinatesystem.model.GeocentricCoordinateSystem;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.HorizontalCoordinateSystem;
import org.jeometry.coordinatesystem.model.ParameterName;
import org.jeometry.coordinatesystem.model.ParameterNames;
import org.jeometry.coordinatesystem.model.ParameterValue;
import org.jeometry.coordinatesystem.model.ParameterValueNumber;
import org.jeometry.coordinatesystem.model.ParameterValueString;
import org.jeometry.coordinatesystem.model.PrimeMeridian;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.VerticalCoordinateSystem;
import org.jeometry.coordinatesystem.model.datum.Datum;
import org.jeometry.coordinatesystem.model.datum.EngineeringDatum;
import org.jeometry.coordinatesystem.model.datum.GeodeticDatum;
import org.jeometry.coordinatesystem.model.datum.VerticalDatum;
import org.jeometry.coordinatesystem.model.unit.AngularUnit;
import org.jeometry.coordinatesystem.model.unit.Degree;
import org.jeometry.coordinatesystem.model.unit.DegreeSexagesimalDMS;
import org.jeometry.coordinatesystem.model.unit.EpsgSystemOfUnits;
import org.jeometry.coordinatesystem.model.unit.Grad;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;
import org.jeometry.coordinatesystem.model.unit.Metre;
import org.jeometry.coordinatesystem.model.unit.Radian;
import org.jeometry.coordinatesystem.model.unit.ScaleUnit;
import org.jeometry.coordinatesystem.model.unit.TimeUnit;
import org.jeometry.coordinatesystem.model.unit.UnitOfMeasure;
import org.slf4j.LoggerFactory;

public final class EpsgCoordinateSystems {

  public static class EpsgCoordinateSystemType {
    public static final List<String> TYPE_NAMES = Arrays.asList("spherical", "ellipsoidal",
      "Cartesian", "vertical");

    private final int id;

    private final int type;

    private final boolean deprecated;

    public EpsgCoordinateSystemType(final int id, final int type, final boolean deprecated) {
      this.id = id;
      this.type = type;
      this.deprecated = deprecated;
    }

    public int getId() {
      return this.id;
    }

    public int getType() {
      return this.type;
    }

    public String getTypeName() {
      return TYPE_NAMES.get(this.type);
    }

    public boolean isDeprecated() {
      return this.deprecated;
    }

    @Override
    public String toString() {
      return getTypeName();
    }
  }

  private static final HashMap<Integer, Area> AREA_BY_ID = new HashMap<>();

  private static final Map<String, AxisName> AXIS_NAME_BY_NAME = new HashMap<>();

  private static final HashMap<Integer, AxisName> AXIS_NAMES = new HashMap<>();

  private static final HashMap<Integer, CoordinateSystem> COORDINATE_SYSTEM_BY_ID = new HashMap<>();

  private static final Map<String, CoordinateSystem> COORDINATE_SYSTEM_BY_NAME = new TreeMap<>();

  private static final HashMap<Integer, EpsgCoordinateSystemType> COORDINATE_SYSTEM_TYPE_BY_ID = new HashMap<>();

  private static final HashMap<Integer, List<CoordinateSystem>> COORDINATE_SYSTEMS_BY_HASH_CODE = new HashMap<>();

  private static Set<CoordinateSystem> coordinateSystems;

  private static final HashMap<Integer, Datum> DATUM_BY_ID = new HashMap<>();

  private static boolean initialized = false;

  private static int nextSrid = 2000000;

  private static final HashMap<Integer, CoordinateOperation> OPERATION_BY_ID = new HashMap<>();

  private static final HashMap<Integer, ParameterName> PARAM_NAME_BY_ID = new HashMap<>();

  private static final HashMap<Integer, PrimeMeridian> PRIME_MERIDIAN_BY_ID = new HashMap<>();;

  private static final EpsgSystemOfUnits SYSTEM_OF_UNITS = new EpsgSystemOfUnits();

  private static final HashMap<Integer, UnitOfMeasure> UNIT_BY_ID = new HashMap<>();

  private static final Map<String, UnitOfMeasure> UNIT_BY_NAME = new TreeMap<>();

  private static void addCoordinateSystem(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem != null) {
      final Integer id = coordinateSystem.getCoordinateSystemId();
      final String name = coordinateSystem.getCoordinateSystemName();
      COORDINATE_SYSTEM_BY_ID.put(id, coordinateSystem);
      final int hashCode = coordinateSystem.hashCode();
      List<CoordinateSystem> coordinateSystems = COORDINATE_SYSTEMS_BY_HASH_CODE.get(hashCode);
      if (coordinateSystems == null) {
        coordinateSystems = new ArrayList<>();
        COORDINATE_SYSTEMS_BY_HASH_CODE.put(hashCode, coordinateSystems);
      }
      coordinateSystems.add(coordinateSystem);
      COORDINATE_SYSTEM_BY_NAME.put(name, coordinateSystem);
    }
  }

  public static void addCoordinateSystemAlias(final int id, final int targetId) {
    final ProjectedCoordinateSystem worldMercator = (ProjectedCoordinateSystem)COORDINATE_SYSTEM_BY_ID
      .get(targetId);
    COORDINATE_SYSTEM_BY_ID.put(id, worldMercator);
  }

  public static synchronized void clear() {
    initialized = false;
    coordinateSystems = null;
    COORDINATE_SYSTEMS_BY_HASH_CODE.clear();
    COORDINATE_SYSTEM_BY_ID.clear();
    COORDINATE_SYSTEM_BY_NAME.clear();
  }

  public static AxisName getAxisName(final String name) {
    if (name == null) {
      return null;
    } else {
      final AxisName axisName = AXIS_NAME_BY_NAME.get(name.toLowerCase());
      if (axisName == null) {
        return new AxisName(0, name);
      } else {
        return axisName;
      }
    }
  }

  private static <V> V getCode(final HashMap<Integer, V> valueById, final int id) {
    if (id == 0) {
      return null;
    } else {
      final V value = valueById.get(id);
      if (value == null) {
        throw new IllegalArgumentException("Invalid code for id=" + id);
      }
      return value;
    }
  }

  public static CompoundCoordinateSystem getCompound(final int horizontalCoordinateSystemId,
    final int verticalCoordinateSystemId) {
    final HorizontalCoordinateSystem horizontalCoordinateSystem = getCoordinateSystem(
      horizontalCoordinateSystemId);
    final VerticalCoordinateSystem verticalCoordinateSystem = getCoordinateSystem(
      verticalCoordinateSystemId);
    if (horizontalCoordinateSystem == null) {
      throw new IllegalArgumentException(
        "horizontalCoordinateSystemId=" + horizontalCoordinateSystemId + " doesn't exist");
    }
    if (verticalCoordinateSystem == null) {
      throw new IllegalArgumentException(
        "verticalCoordinateSystemId=" + verticalCoordinateSystemId + " doesn't exist");
    }
    final CompoundCoordinateSystem compoundCoordinateSystem = new CompoundCoordinateSystem(
      horizontalCoordinateSystem, verticalCoordinateSystem);
    return getCoordinateSystem(compoundCoordinateSystem);
  }

  @SuppressWarnings("unchecked")
  public synchronized static <C extends CoordinateSystem> C getCoordinateSystem(
    final C coordinateSystem) {
    initialize();
    if (coordinateSystem == null) {
      return null;
    } else {
      int srid = coordinateSystem.getCoordinateSystemId();
      CoordinateSystem matchedCoordinateSystem = COORDINATE_SYSTEM_BY_ID.get(srid);
      if (matchedCoordinateSystem == null) {
        matchedCoordinateSystem = COORDINATE_SYSTEM_BY_NAME
          .get(coordinateSystem.getCoordinateSystemName());
        if (matchedCoordinateSystem == null) {
          final int hashCode = coordinateSystem.hashCode();
          int matchCoordinateSystemId = EsriCoordinateSystems.getIdUsingDigest(coordinateSystem);
          if (matchCoordinateSystemId > 0) {
            matchedCoordinateSystem = getCoordinateSystem(matchCoordinateSystemId);
          } else {
            final List<CoordinateSystem> coordinateSystems = COORDINATE_SYSTEMS_BY_HASH_CODE
              .get(hashCode);
            if (coordinateSystems != null) {
              for (final CoordinateSystem coordinateSystem3 : coordinateSystems) {
                if (coordinateSystem3.equals(coordinateSystem)) {
                  final int srid3 = coordinateSystem3.getCoordinateSystemId();
                  if (matchedCoordinateSystem == null) {
                    matchedCoordinateSystem = coordinateSystem3;
                    matchCoordinateSystemId = srid3;
                  } else if (srid3 < matchCoordinateSystemId) {
                    if (!coordinateSystem3.isDeprecated()
                      || matchedCoordinateSystem.isDeprecated()) {
                      matchedCoordinateSystem = coordinateSystem3;
                      matchCoordinateSystemId = srid3;
                    }
                  }
                }
              }
            }
          }

          if (matchedCoordinateSystem == null) {
            if (srid <= 0) {
              srid = nextSrid++;
            }
            final String name = coordinateSystem.getCoordinateSystemName();
            final List<Axis> axis = coordinateSystem.getAxis();
            final Area area = coordinateSystem.getArea();
            final Authority authority = coordinateSystem.getAuthority();
            final boolean deprecated = coordinateSystem.isDeprecated();
            if (coordinateSystem instanceof GeographicCoordinateSystem) {
              final GeographicCoordinateSystem geographicCs = (GeographicCoordinateSystem)coordinateSystem;
              final GeodeticDatum geodeticDatum = geographicCs.getGeodeticDatum();
              final PrimeMeridian primeMeridian = geographicCs.getPrimeMeridian();
              final CoordinateSystem sourceCoordinateSystem = geographicCs
                .getSourceCoordinateSystem();
              final CoordinateOperation coordinateOperation = geographicCs.getCoordinateOperation();
              final GeographicCoordinateSystem newCs = new GeographicCoordinateSystem(srid, name,
                geodeticDatum, primeMeridian, axis, area, sourceCoordinateSystem,
                coordinateOperation, deprecated);
              addCoordinateSystem(newCs);
              return (C)newCs;
            } else if (coordinateSystem instanceof ProjectedCoordinateSystem) {
              final ProjectedCoordinateSystem projectedCs = (ProjectedCoordinateSystem)coordinateSystem;
              GeographicCoordinateSystem geographicCs = projectedCs.getGeographicCoordinateSystem();
              geographicCs = getCoordinateSystem(geographicCs);
              final CoordinateOperationMethod coordinateOperationMethod = projectedCs
                .getCoordinateOperationMethod();
              final Map<ParameterName, ParameterValue> parameters = projectedCs
                .getParameterValues();
              final LinearUnit linearUnit = projectedCs.getLinearUnit();
              final ProjectedCoordinateSystem newCs = new ProjectedCoordinateSystem(srid, name,
                geographicCs, area, coordinateOperationMethod, parameters, linearUnit, axis,
                authority, deprecated);
              addCoordinateSystem(newCs);
              return (C)newCs;
            }
            return coordinateSystem;
          }
        }
      }
      return (C)matchedCoordinateSystem;
    }
  }

  @SuppressWarnings("unchecked")
  public static <C extends CoordinateSystem> C getCoordinateSystem(final int crsId) {
    if (crsId > 0) {
      initialize();
      return (C)COORDINATE_SYSTEM_BY_ID.get(crsId);
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static <C extends CoordinateSystem> C getCoordinateSystem(final String name) {
    initialize();
    return (C)COORDINATE_SYSTEM_BY_NAME.get(name);
  }

  public static Set<CoordinateSystem> getCoordinateSystems() {
    initialize();
    return coordinateSystems;
  }

  /**
   * Get the coordinate systems for the list of coordinate system identifiers.
   * Null identifiers will be ignored. If the coordinate system does not exist
   * then it will be ignored.
   *
   * @param coordinateSystemIds The coordinate system identifiers.
   * @return The list of coordinate systems.
   */
  public static List<CoordinateSystem> getCoordinateSystems(
    final Collection<Integer> coordinateSystemIds) {
    final List<CoordinateSystem> coordinateSystems = new ArrayList<>();
    for (final Integer coordinateSystemId : coordinateSystemIds) {
      if (coordinateSystemId != null) {
        final CoordinateSystem coordinateSystem = getCoordinateSystem(coordinateSystemId);
        if (coordinateSystem != null) {
          coordinateSystems.add(coordinateSystem);
        }
      }
    }
    return coordinateSystems;
  }

  public static Map<Integer, CoordinateSystem> getCoordinateSystemsById() {
    initialize();
    return new TreeMap<>(COORDINATE_SYSTEM_BY_ID);
  }

  public static int getCrsId(final CoordinateSystem coordinateSystem) {
    final Authority authority = coordinateSystem.getAuthority();
    if (authority != null) {
      final String name = authority.getName();
      final String code = authority.getCode();
      if (name.equals("EPSG")) {
        return Integer.parseInt(code);
      }
    }
    return 0;
  }

  public static String getCrsName(final CoordinateSystem coordinateSystem) {
    final Authority authority = coordinateSystem.getAuthority();
    if (authority != null) {
      final String name = authority.getName();
      final String code = authority.getCode();
      if (name.equals("EPSG")) {
        return name + ":" + code;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <D extends Datum> D getDatum(final int id) {
    initialize();
    return (D)DATUM_BY_ID.get(id);
  }

  public static List<GeographicCoordinateSystem> getGeographicCoordinateSystems() {
    final List<GeographicCoordinateSystem> coordinateSystems = new ArrayList<>();
    for (final CoordinateSystem coordinateSystem : COORDINATE_SYSTEM_BY_NAME.values()) {
      if (coordinateSystem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)coordinateSystem;
        coordinateSystems.add(geographicCoordinateSystem);
      }
    }
    return coordinateSystems;
  }

  public static HorizontalCoordinateSystem getHorizontalCoordinateSystem(final int crsId) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem(crsId);
    if (coordinateSystem instanceof HorizontalCoordinateSystem) {
      return (HorizontalCoordinateSystem)coordinateSystem;
    } else {
      return null;
    }
  }

  public static List<HorizontalCoordinateSystem> getHorizontalCoordinateSystems() {
    final List<HorizontalCoordinateSystem> coordinateSystems = new ArrayList<>();
    for (final CoordinateSystem coordinateSystem : COORDINATE_SYSTEM_BY_NAME.values()) {
      if (coordinateSystem instanceof HorizontalCoordinateSystem) {
        final HorizontalCoordinateSystem projectedCoordinateSystem = (HorizontalCoordinateSystem)coordinateSystem;
        coordinateSystems.add(projectedCoordinateSystem);
      }
    }
    return coordinateSystems;
  }

  @SuppressWarnings("unchecked")
  public static <U extends UnitOfMeasure> U getLinearUnit(final String name) {
    loadUnitOfMeasure();
    return (U)UNIT_BY_NAME.get(name);
  }

  public static List<ProjectedCoordinateSystem> getProjectedCoordinateSystems() {
    final List<ProjectedCoordinateSystem> coordinateSystems = new ArrayList<>();
    for (final CoordinateSystem coordinateSystem : COORDINATE_SYSTEM_BY_NAME.values()) {
      if (coordinateSystem instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
        coordinateSystems.add(projectedCoordinateSystem);
      }
    }
    return coordinateSystems;
  }

  @SuppressWarnings("unchecked")
  public static <U extends UnitOfMeasure> U getUnit(final int id) {
    loadUnitOfMeasure();
    return (U)UNIT_BY_ID.get(id);
  }

  public static VerticalCoordinateSystem getVerticalCoordinateSystem(final int crsId) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem(crsId);
    if (coordinateSystem instanceof VerticalCoordinateSystem) {
      return (VerticalCoordinateSystem)coordinateSystem;
    } else {
      return null;
    }
  }

  public static List<VerticalCoordinateSystem> getVerticalCoordinateSystems() {
    final List<VerticalCoordinateSystem> coordinateSystems = new ArrayList<>();
    for (final CoordinateSystem coordinateSystem : COORDINATE_SYSTEM_BY_NAME.values()) {
      if (coordinateSystem instanceof VerticalCoordinateSystem) {
        final VerticalCoordinateSystem projectedCoordinateSystem = (VerticalCoordinateSystem)coordinateSystem;
        coordinateSystems.add(projectedCoordinateSystem);
      }
    }
    return coordinateSystems;
  }

  public synchronized static void initialize() {
    if (!initialized) {
      initialized = true;
      try {
        loadUnitOfMeasure();
        loadCoordinateAxisNames();
        final HashMap<Integer, List<Axis>> axisMap = loadCoordinateAxis();
        loadArea();
        loadPrimeMeridians();
        loadDatum();
        loadCoordOperationParam();
        final HashMap<Integer, List<ParameterName>> paramOrderByMethodId = new HashMap<>();
        final HashMap<Integer, List<Byte>> paramReversalByMethodId = new HashMap<>();
        loadCoordOperationParamUsage(paramOrderByMethodId, paramReversalByMethodId);
        final HashMap<Integer, CoordinateOperationMethod> methodById = loadCoordOperationMethod(
          paramOrderByMethodId, paramReversalByMethodId);
        final HashMap<Integer, Map<ParameterName, ParameterValue>> operationParameters = new HashMap<>();
        loadCoordOperationParamValue(methodById, operationParameters, paramReversalByMethodId);
        loadCoordOperation(methodById, operationParameters, paramReversalByMethodId);
        loadCoordinateSystem();
        loadCoordinateReferenceSystem(axisMap);

        addCoordinateSystemAlias(42102, 3005);
        addCoordinateSystemAlias(900913, 3857);
        coordinateSystems = Collections
          .unmodifiableSet(new LinkedHashSet<>(COORDINATE_SYSTEM_BY_ID.values()));
      } catch (final Throwable t) {
        t.printStackTrace();
      }
    }
  }

  private static void loadArea() {
    try (
      DataInputStream reader = newDataInputStream("area")) {
      if (reader != null) {
        while (true) {
          final int code = reader.readInt();
          final String name = readStringUtf8ByteCount(reader);
          double minX = reader.readDouble();
          final double minY = reader.readDouble();
          final double maxX = reader.readDouble();
          final double maxY = reader.readDouble();
          final boolean deprecated = readBoolean(reader);
          final Authority authority = new EpsgAuthority(code);

          if (minX > maxX) {
            if (minX > 0) {
              minX -= 360;
            }
          }
          final Area area = new Area(name, minX, minY, maxX, maxY, authority, deprecated);
          AREA_BY_ID.put(code, area);
        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("area", e);
    }
  }

  private static HashMap<Integer, List<Axis>> loadCoordinateAxis() {
    final HashMap<Integer, List<Axis>> axisesByCoordinateSystemId = new HashMap<>();
    try (
      DataInputStream reader = newDataInputStream("coordinateAxis")) {
      if (reader != null) {
        while (true) {
          final int coordinateSystemId = reader.readInt();
          final AxisName axisName = readCode(reader, AXIS_NAMES);
          final String orientation = readStringUtf8ByteCount(reader);
          final Character abbreviation = (char)reader.readByte();
          final UnitOfMeasure unitOfMeasure = readCode(reader, UNIT_BY_ID);

          final Axis axis = new Axis(axisName, orientation, abbreviation.toString(), unitOfMeasure);
          List<Axis> axises = axisesByCoordinateSystemId.get(coordinateSystemId);
          if (axises == null) {
            axises = new ArrayList<>();
            axisesByCoordinateSystemId.put(coordinateSystemId, axises);
          }
          axises.add(axis);
        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("coordinateAxis", e);
    }
    return axisesByCoordinateSystemId;
  }

  private static void loadCoordinateAxisNames() {
    try (
      DataInputStream reader = newDataInputStream("coordinateAxisName")) {
      if (reader != null) {
        while (true) {
          final int code = reader.readInt();
          final String name = readStringUtf8ByteCount(reader);

          final AxisName axisName = new AxisName(code, name);
          AXIS_NAMES.put(code, axisName);
          AXIS_NAME_BY_NAME.put(name.toLowerCase(), axisName);
        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("coordinateAxisName", e);
    }
  }

  private static void loadCoordinateReferenceSystem(final HashMap<Integer, List<Axis>> axisMap) {
    try (
      DataInputStream reader = newDataInputStream("coordinateReferenceSystem")) {
      if (reader != null) {
        while (true) {
          final int id = reader.readInt();
          final String name = readStringUtf8ByteCount(reader);
          final Area area = readCode(reader, AREA_BY_ID);
          final int type = reader.readByte();
          final EpsgCoordinateSystemType coordinateSystemType = readCode(reader,
            COORDINATE_SYSTEM_TYPE_BY_ID);
          final Datum datum = readCode(reader, DATUM_BY_ID);
          final CoordinateSystem sourceCoordinateSystem = readCode(reader, COORDINATE_SYSTEM_BY_ID);

          final CoordinateOperation operation = readCode(reader, OPERATION_BY_ID);

          final HorizontalCoordinateSystem horizontalCoordinateSystem = (HorizontalCoordinateSystem)readCode(
            reader, COORDINATE_SYSTEM_BY_ID);
          final VerticalCoordinateSystem verticalCoordinateSystem = (VerticalCoordinateSystem)readCode(
            reader, COORDINATE_SYSTEM_BY_ID);
          final boolean deprecated = readBoolean(reader);
          final List<Axis> axis;
          if (coordinateSystemType == null) {
            axis = null;
          } else {
            axis = axisMap.get(coordinateSystemType.getId());
          }
          CoordinateSystem coordinateSystem = null;
          if (type == 0) {
            // geocentric
            coordinateSystem = newCoordinateSystemGeocentric(id, name, datum, axis, area,
              deprecated);
          } else if (type == 1) {
            // geographic 3D
            coordinateSystem = new GeographicCoordinateSystem(id, name, (GeodeticDatum)datum, axis,
              area, sourceCoordinateSystem, operation, deprecated);
          } else if (type == 2) {
            // geographic 2D
            coordinateSystem = new GeographicCoordinateSystem(id, name, (GeodeticDatum)datum, axis,
              area, sourceCoordinateSystem, operation, deprecated);
          } else if (type == 3) {
            // projected
            coordinateSystem = newCoordinateSystemProjected(id, name, area, sourceCoordinateSystem,
              operation, axis, deprecated);
          } else if (type == 4) {
            // engineering
            coordinateSystem = new EngineeringCoordinateSystem(id, name, (EngineeringDatum)datum,
              axis, area, deprecated);
          } else if (type == 5) {
            // vertical
            coordinateSystem = new VerticalCoordinateSystem(id, name, (VerticalDatum)datum, axis,
              area, deprecated);
          } else if (type == 6) {
            coordinateSystem = new CompoundCoordinateSystem(id, name, horizontalCoordinateSystem,
              verticalCoordinateSystem, area, deprecated);
          } else {
            coordinateSystem = null;
          }

          addCoordinateSystem(coordinateSystem);
        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("coordinateReferenceSystem", e);
    }
  }

  private static void loadCoordinateSystem() {
    try (
      DataInputStream reader = newDataInputStream("coordinateSystem")) {
      if (reader != null) {
        while (true) {
          final int id = reader.readInt();
          final int type = reader.readByte();
          final boolean deprecated = readBoolean(reader);

          final EpsgCoordinateSystemType coordinateSystemType = new EpsgCoordinateSystemType(id,
            type, deprecated);
          COORDINATE_SYSTEM_TYPE_BY_ID.put(id, coordinateSystemType);
        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("coordinateSystem", e);
    }
  }

  private static void loadCoordOperation(
    final HashMap<Integer, CoordinateOperationMethod> methodById,
    final HashMap<Integer, Map<ParameterName, ParameterValue>> operationParameters,
    final HashMap<Integer, List<Byte>> paramReversal) {
    try (
      DataInputStream reader = newDataInputStream("coordOperation")) {
      if (reader != null) {
        while (true) {
          final int id = reader.readInt();
          final CoordinateOperationMethod method = readCode(reader, methodById);
          final String name = readStringUtf8ByteCount(reader);
          final byte type = reader.readByte();
          final int sourceCrsCode = reader.readInt();
          final int targetCrsCode = reader.readInt();
          final String transformationVersion = readStringUtf8ByteCount(reader);
          final int variant = reader.readInt();
          final Area area = readCode(reader, AREA_BY_ID);
          final double accuracy = reader.readDouble();
          final boolean deprecated = readBoolean(reader);

          final Map<ParameterName, ParameterValue> parameters = operationParameters.getOrDefault(id,
            Collections.emptyMap());
          final CoordinateOperation coordinateOperation = new CoordinateOperation(id, method, name,
            type, sourceCrsCode, targetCrsCode, transformationVersion, variant, area, accuracy,
            parameters, deprecated);
          OPERATION_BY_ID.put(id, coordinateOperation);

        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("coordOperation", e);
    }
  }

  private static HashMap<Integer, CoordinateOperationMethod> loadCoordOperationMethod(
    final HashMap<Integer, List<ParameterName>> paramOrderByMethodId,
    final HashMap<Integer, List<Byte>> paramReversalByMethodId) {
    final HashMap<Integer, CoordinateOperationMethod> methodById = new HashMap<>();

    try (
      DataInputStream reader = newDataInputStream("coordOperationMethod")) {
      if (reader != null) {
        while (true) {
          final int id = reader.readInt();
          final String name = readStringUtf8ByteCount(reader);
          final boolean reverse = readBoolean(reader);
          final boolean deprecated = readBoolean(reader);

          final List<ParameterName> parameterNames = paramOrderByMethodId.getOrDefault(id,
            Collections.emptyList());
          final List<Byte> parameterReversal = paramReversalByMethodId.getOrDefault(id,
            Collections.emptyList());
          final CoordinateOperationMethod method = new CoordinateOperationMethod(id, name, reverse,
            deprecated, parameterNames, parameterReversal);
          methodById.put(id, method);
        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("coordOperationMethod", e);
    }
    return methodById;
  }

  private static void loadCoordOperationParam() {
    try (
      DataInputStream reader = newDataInputStream("coordOperationParam")) {
      if (reader != null) {
        while (true) {
          final int id = reader.readInt();
          String name = readStringUtf8ByteCount(reader);
          if (name != null) {
            name = name.toLowerCase().replaceAll(" ", "_");
          }
          readBoolean(reader);
          final ParameterName parameterName = ParameterNames.getParameterName(id, name);
          PARAM_NAME_BY_ID.put(id, parameterName);
        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("coordOperationParam", e);
    }
  }

  private static void loadCoordOperationParamUsage(
    final HashMap<Integer, List<ParameterName>> paramOrderByMethodId,
    final HashMap<Integer, List<Byte>> paramReversal) {
    try (
      DataInputStream reader = newDataInputStream("coordOperationParamUsage")) {
      if (reader != null) {
        while (true) {
          final int methodId = reader.readInt();
          final ParameterName parameterName = readCode(reader, PARAM_NAME_BY_ID);
          @SuppressWarnings("unused")
          final int sortOrder = reader.readInt();
          final byte signReversal = reader.readByte();
          List<ParameterName> names = paramOrderByMethodId.get(methodId);
          if (names == null) {
            names = new ArrayList<>();
            paramOrderByMethodId.put(methodId, names);
          }
          names.add(parameterName);

          List<Byte> reversals = paramReversal.get(methodId);
          if (reversals == null) {
            reversals = new ArrayList<>();
            paramReversal.put(methodId, reversals);
          }
          reversals.add(signReversal);
        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("coordOperationParamUsage", e);
    }
  }

  private static void loadCoordOperationParamValue(
    final HashMap<Integer, CoordinateOperationMethod> methodById,
    final HashMap<Integer, Map<ParameterName, ParameterValue>> operationParameters,
    final HashMap<Integer, List<Byte>> paramReversal) {
    try (
      DataInputStream reader = newDataInputStream("coordOperationParamValue")) {
      if (reader != null) {
        while (true) {
          final int operationId = reader.readInt();
          final CoordinateOperationMethod method = readCode(reader, methodById);
          final ParameterName parameterName = readCode(reader, PARAM_NAME_BY_ID);
          final double value = reader.readDouble();
          final String fileRef = readStringUtf8ByteCount(reader);
          final UnitOfMeasure unit = readCode(reader, UNIT_BY_ID);
          final ParameterValue parameterValue;
          if (Double.isFinite(value)) {
            if (fileRef != null) {
              throw new IllegalArgumentException(
                "Cannot have a value and fileRef for coordOperationParamValue=" + operationId + " "
                  + parameterName);
            } else {
              parameterValue = new ParameterValueNumber(unit, value);
            }
          } else {
            if (fileRef != null) {
              parameterValue = new ParameterValueString(fileRef);
            } else {
              parameterValue = null;
            }
          }
          Map<ParameterName, ParameterValue> parameterValues = operationParameters.get(operationId);
          if (parameterValues == null) {
            parameterValues = new LinkedHashMap<>();
            final List<ParameterName> parameterOrder = method.getParameterNames();
            for (final ParameterName orderParameterName : parameterOrder) {
              parameterValues.put(orderParameterName, null);
            }
            operationParameters.put(operationId, parameterValues);
          }
          method.setParameter(parameterValues, parameterName, parameterValue);
        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("coordOperationParamValue", e);
    }
  }

  private static void loadDatum() {
    final HashMap<Integer, Ellipsoid> ellipsoids = loadEllipsoid();

    try (
      DataInputStream reader = newDataInputStream("datum")) {
      if (reader != null) {
        while (true) {

          final int id = reader.readInt();
          final String name = readStringUtf8ByteCount(reader);
          final int datumType = reader.readByte();
          final Ellipsoid ellipsoid = readCode(reader, ellipsoids);
          final PrimeMeridian primeMeridian = readCode(reader, PRIME_MERIDIAN_BY_ID);
          final Area area = readCode(reader, AREA_BY_ID);

          final boolean deprecated = readBoolean(reader);
          final EpsgAuthority authority = new EpsgAuthority(id);

          Datum datum;
          if (datumType == 0) {
            datum = new GeodeticDatum(authority, name, area, deprecated, ellipsoid, primeMeridian);
          } else if (datumType == 1) {
            datum = new VerticalDatum(authority, name, area, deprecated);
          } else if (datumType == 2) {
            datum = new EngineeringDatum(authority, name, area, deprecated);
          } else {
            throw new IllegalArgumentException("Unknown datumType=" + datumType);
          }
          DATUM_BY_ID.put(id, datum);

        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("datum", e);
    }
  }

  private static HashMap<Integer, Ellipsoid> loadEllipsoid() {
    final HashMap<Integer, Ellipsoid> ellipsoids = new HashMap<>();
    try (
      DataInputStream reader = newDataInputStream("ellipsoid")) {
      if (reader != null) {
        while (true) {
          final int id = reader.readInt();
          final String name = readStringUtf8ByteCount(reader);
          final int unitId = reader.readInt();
          final LinearUnit unit = (LinearUnit)UNIT_BY_ID.get(unitId);
          final double semiMinorAxis = unit.toBase(reader.readDouble());
          final double semiMajorAxis = unit.toBase(reader.readDouble());
          final double inverseFlattening = unit.toBase(reader.readDouble());
          @SuppressWarnings("unused")
          final int ellipsoidShape = reader.readByte();
          final boolean deprecated = readBoolean(reader);
          final EpsgAuthority authority = new EpsgAuthority(id);
          final Ellipsoid ellipsoid = new Ellipsoid(name, semiMajorAxis, semiMinorAxis,
            inverseFlattening, authority, deprecated);
          ellipsoids.put(id, ellipsoid);
        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("ellipsoid", e);
    }
    return ellipsoids;
  }

  private static void loadPrimeMeridians() {
    try (
      DataInputStream reader = newDataInputStream("primeMeridian")) {
      if (reader != null) {
        while (true) {
          final int id = reader.readInt();
          final String name = readStringUtf8ByteCount(reader);
          final AngularUnit unit = (AngularUnit)readCode(reader, UNIT_BY_ID);
          final double longitude = reader.readDouble();
          final double longitudeDegrees = unit.toDegrees(longitude);
          final EpsgAuthority authority = new EpsgAuthority(id);
          final PrimeMeridian primeMeridian = new PrimeMeridian(name, longitudeDegrees, authority,
            false);
          PRIME_MERIDIAN_BY_ID.put(id, primeMeridian);
        }
      }
    } catch (final EOFException e) {
    } catch (final IOException e) {
      log("primeMeridian", e);
    }
  }

  private static void loadUnitOfMeasure() {
    if (UNIT_BY_ID.isEmpty()) {
      try (
        DataInputStream reader = newDataInputStream("unitOfMeasure")) {
        if (reader != null) {
          while (true) {
            final int id = reader.readInt();
            final byte type = reader.readByte();
            final int baseId = reader.readInt();
            final boolean deprecated = readBoolean(reader);
            final double conversionFactorB = reader.readDouble();
            final double conversionFactorC = reader.readDouble();
            double conversionFactor;
            if (Double.isFinite(conversionFactorB)) {
              if (Double.isFinite(conversionFactorC)) {
                conversionFactor = conversionFactorB / conversionFactorC;
              } else {
                conversionFactor = conversionFactorB;
              }
            } else {
              conversionFactor = conversionFactorC;
            }

            final String name = readStringUtf8ByteCount(reader);
            final EpsgAuthority authority = new EpsgAuthority(id);

            UnitOfMeasure unit;
            switch (type) {
              case 0:
                final ScaleUnit baseScaleUnit = (ScaleUnit)UNIT_BY_ID.get(baseId);
                unit = new ScaleUnit(name, baseScaleUnit, conversionFactor, authority, deprecated);
              break;
              case 1:
                final LinearUnit baseLinearUnit = (LinearUnit)UNIT_BY_ID.get(baseId);
                if (id == 9001) {
                  unit = new Metre(name, baseLinearUnit, conversionFactor, authority, deprecated);
                } else {
                  unit = new LinearUnit(name, baseLinearUnit, conversionFactor, authority,
                    deprecated);
                }
              break;
              case 2:
                final AngularUnit baseAngularUnit = (AngularUnit)UNIT_BY_ID.get(baseId);
                if (id == 9101) {
                  unit = new Radian(name, baseAngularUnit, conversionFactor, authority, deprecated);
                } else if (id == 9102) {
                  unit = new Degree(name, baseAngularUnit, conversionFactor, authority, deprecated);
                  SYSTEM_OF_UNITS.addUnit(unit, "Degree", "deg");
                } else if (id == 9105) {
                  unit = new Grad(name, baseAngularUnit, conversionFactor, authority, deprecated);
                } else if (id == 9110) {
                  unit = new DegreeSexagesimalDMS(name, baseAngularUnit, conversionFactor,
                    authority, deprecated);
                } else if (id == 9122) {
                  unit = new Degree(name, baseAngularUnit, conversionFactor, authority, deprecated);
                } else {
                  unit = new AngularUnit(name, baseAngularUnit, conversionFactor, authority,
                    deprecated);
                }
              break;
              case 3:
                final TimeUnit baseTimeUnit = (TimeUnit)UNIT_BY_ID.get(baseId);
                unit = new TimeUnit(name, baseTimeUnit, conversionFactor, authority, deprecated);

              break;

              default:
                throw new IllegalArgumentException("Invalid unitId=" + id);
            }
            UNIT_BY_NAME.put(name, unit);
            UNIT_BY_ID.put(id, unit);

          }
        }
      } catch (final EOFException e) {
      } catch (final IOException e) {
        log("unitOfMeasure", e);
      }
    }
  }

  private static void log(final String message) {
    LoggerFactory.getLogger(EpsgCoordinateSystems.class).error(message);
  }

  private static void log(final String message, final IOException e) {
    LoggerFactory.getLogger(EpsgCoordinateSystems.class).error(message, e);
  }

  private static GeocentricCoordinateSystem newCoordinateSystemGeocentric(final int id,
    final String name, final Datum datum, final List<Axis> axis, final Area area,
    final boolean deprecated) {
    final EpsgAuthority authority = new EpsgAuthority(id);
    final LinearUnit linearUnit = (LinearUnit)axis.get(0).getUnit();
    final GeodeticDatum geodeticDatum = (GeodeticDatum)datum;
    return new GeocentricCoordinateSystem(id, name, geodeticDatum, linearUnit, axis, area,
      authority, deprecated);
  }

  private static ProjectedCoordinateSystem newCoordinateSystemProjected(final int id,
    final String name, final Area area, final CoordinateSystem sourceCoordinateSystem,
    final CoordinateOperation operation, final List<Axis> axis, final boolean deprecated) {
    final EpsgAuthority authority = new EpsgAuthority(id);
    final LinearUnit linearUnit = (LinearUnit)axis.get(0).getUnit();
    final CoordinateOperationMethod method = operation.getMethod();
    final Map<ParameterName, ParameterValue> parameterValues = operation.getParameterValues();
    if (sourceCoordinateSystem instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)sourceCoordinateSystem;
      return new ProjectedCoordinateSystem(id, name, geographicCoordinateSystem, area, method,
        parameterValues, linearUnit, axis, authority, deprecated);
    } else if (!Arrays.asList(5819, 5820, 5821).contains(id)) {
      log(id + " " + name + " has a projected coordinate system");
      return null;
    } else {
      return null;
    }
  }

  private static DataInputStream newDataInputStream(final String fileName) {
    final InputStream in = EpsgCoordinateSystems.class
      .getResourceAsStream("/org/jeometry/coordinatesystem/epsg/" + fileName + ".bin");
    final BufferedInputStream bufferedIn = new BufferedInputStream(in);
    return new DataInputStream(bufferedIn);
  }

  private static boolean readBoolean(final DataInputStream reader) throws IOException {
    return reader.readByte() == (byte)1;
  }

  private static <V> V readCode(final DataInputStream reader, final HashMap<Integer, V> valueById)
    throws IOException {
    final int id = reader.readInt();
    return getCode(valueById, id);
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

  public static GeographicCoordinateSystem wgs84() {
    return EpsgCoordinateSystems.getCoordinateSystem(EpsgId.WGS84);
  }
}
