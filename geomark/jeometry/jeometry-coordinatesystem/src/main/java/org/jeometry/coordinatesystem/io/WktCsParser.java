package org.jeometry.coordinatesystem.io;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.jeometry.coordinatesystem.model.Authority;
import org.jeometry.coordinatesystem.model.Axis;
import org.jeometry.coordinatesystem.model.BaseAuthority;
import org.jeometry.coordinatesystem.model.CoordinateOperationMethod;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.ParameterName;
import org.jeometry.coordinatesystem.model.ParameterValue;
import org.jeometry.coordinatesystem.model.ParameterValueBigDecimal;
import org.jeometry.coordinatesystem.model.PrimeMeridian;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.SingleParameterName;
import org.jeometry.coordinatesystem.model.ToWgs84;
import org.jeometry.coordinatesystem.model.VerticalCoordinateSystem;
import org.jeometry.coordinatesystem.model.datum.GeodeticDatum;
import org.jeometry.coordinatesystem.model.datum.VerticalDatum;
import org.jeometry.coordinatesystem.model.unit.AngularUnit;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;

public class WktCsParser {

  public static <C extends CoordinateSystem> C read(final String wkt) {
    if (wkt != null) {
      try {
        final WktCsParser parser = new WktCsParser(wkt);
        return parser.parse();
      } catch (final StringIndexOutOfBoundsException e) {
        return EsriCsGridParser.parse(wkt);
      }
    } else {
      return null;
    }
  }

  private int index = 0;

  private final Stack<String> nameStack = new Stack<>();

  private final String value;

  public WktCsParser(final String value) {
    this.value = value;
  }

  private int getCoordinateSystemId(final Authority authority) {
    int coordinateSystemId = 0;
    if (authority != null) {
      final String code = authority.getCode();
      if (code != null) {
        try {
          coordinateSystemId = Integer.valueOf(code);
        } catch (final Throwable e) {
          final Double value = Double.valueOf(code);
          if (value != null) {
            coordinateSystemId = value.intValue();
          }
        }
      }
    }
    return coordinateSystemId;
  }

  @SuppressWarnings("unchecked")
  public <C extends CoordinateSystem> C parse() {
    if (this.value.length() == 0) {
      return null;
    } else {
      return (C)parseValue();
    }

  }

  private String parseName() {
    final int startIndex = this.index;

    while (this.value.charAt(this.index) != '[' && this.value.charAt(this.index) != ']') {
      this.index++;
    }
    final String name = new String(this.value.substring(startIndex, this.index)).trim();
    return name;
  }

  private BigDecimal parseNumber() {
    final int startIndex = this.index;

    char currentChar = this.value.charAt(this.index);
    while (Character.isDigit(currentChar) || currentChar == '.' || currentChar == '-') {
      this.index++;
      currentChar = this.value.charAt(this.index);
    }
    final String string = this.value.substring(startIndex, this.index);
    return new BigDecimal(string);
  }

  private String parseString() {
    final int startIndex = this.index;

    char currentChar = this.value.charAt(this.index);
    while (currentChar != '"') {
      this.index++;
      currentChar = this.value.charAt(this.index);
    }
    final String string = new String(this.value.substring(startIndex, this.index));
    this.index++;
    return string;
  }

  private Object parseValue() {
    char currentChar = this.value.charAt(this.index);
    if (currentChar == '"') {
      this.index++;
      return parseString();
    } else if (Character.isDigit(currentChar) || currentChar == '-') {
      return parseNumber();
    } else {
      final String name = parseName();
      this.nameStack.push(name);
      try {
        final List<Object> values = new ArrayList<>();
        currentChar = this.value.charAt(this.index);
        if (currentChar == '[') {
          do {
            this.index++;
            currentChar = skipWhitespace();
            if (currentChar != ']') {
              final Object value = parseValue();
              values.add(value);
            }
            currentChar = skipWhitespace();
          } while (currentChar == ',');
          this.index++;
          if (name.equals("AUTHORITY")) {
            return processAuthority(values);
          } else if (name.equals("AXIS")) {
            return processAxis(values);
          } else if (name.equals("DATUM")) {
            return processDatum(values);
          } else if (name.equals("GEOGCS")) {
            return processGeographicCoordinateSystem(values);
          } else if (name.equals("PRIMEM")) {
            return processPrimeMeridian(values);
          } else if (name.equals("PROJCS")) {
            return processProjectedCoordinateSystem(values);
          } else if (name.equals("PROJECTION")) {
            return processProjection(values);
          } else if (name.equals("SPHEROID")) {
            return processSpheroid(values);
          } else if (name.equals("TOWGS84")) {
            return processToWgs84(values);
          } else if (name.equals("UNIT")) {
            if (this.nameStack.get(this.nameStack.size() - 2).equals("GEOGCS")) {
              return processAngularUnit(values);
            } else {
              return processLinearUnit(values);
            }
          } else if (name.equals("VDATUM")) {
            return processVerticalDatum(values);
          } else if (name.equals("VERTCS")) {
            return processVerticalCoordinateSystem(values);
          } else {
            return Collections.singletonMap(name, values);
          }
        } else {
          return name;
        }
      } finally {
        this.nameStack.pop();
      }
    }
  }

  private AngularUnit processAngularUnit(final List<Object> values) {
    final String name = (String)values.get(0);
    final Number conversionFactor = (Number)values.get(1);
    Authority authority = null;
    if (values.size() > 2) {
      authority = (Authority)values.get(2);
    }
    return new AngularUnit(name, conversionFactor.doubleValue(), authority);
  }

  private Authority processAuthority(final List<Object> values) {
    final String name = (String)values.get(0);
    final String code = values.get(1).toString();
    return new BaseAuthority(name, code);
  }

  private Axis processAxis(final List<Object> values) {
    final String name = (String)values.get(0);
    final String direction = (String)values.get(1);
    return new Axis(name, direction);
  }

  private GeodeticDatum processDatum(final List<Object> values) {
    final String name = (String)values.get(0);
    Ellipsoid ellipsoid = null;
    Authority authority = null;
    ToWgs84 toWgs84 = null;
    for (int i = 1; i < values.size(); i++) {
      final Object value = values.get(i);
      if (value instanceof Ellipsoid) {
        ellipsoid = (Ellipsoid)value;
      } else if (value instanceof Authority) {
        authority = (Authority)value;
      } else if (value instanceof ToWgs84) {
        toWgs84 = (ToWgs84)value;
      }
    }
    return new GeodeticDatum(name, ellipsoid, toWgs84, authority);
  }

  private GeographicCoordinateSystem processGeographicCoordinateSystem(final List<Object> values) {
    final String name = (String)values.get(0);
    final GeodeticDatum geodeticDatum = (GeodeticDatum)values.get(1);
    final PrimeMeridian primeMeridian = (PrimeMeridian)values.get(2);
    final AngularUnit angularUnit = (AngularUnit)values.get(3);
    int index = 4;
    List<Axis> axis = null;
    if (index < values.size() && values.get(index) instanceof Axis) {
      axis = Arrays.asList((Axis)values.get(index++), (Axis)values.get(index++));

    }
    Authority authority = null;
    if (index < values.size()) {
      authority = (Authority)values.get(index);
    }
    final int coordinateSystemId = getCoordinateSystemId(authority);
    return new GeographicCoordinateSystem(coordinateSystemId, name, geodeticDatum, primeMeridian,
      angularUnit, axis, authority);
  }

  private LinearUnit processLinearUnit(final List<Object> values) {
    final String name = (String)values.get(0);
    final Number conversionFactor = (Number)values.get(1);
    Authority authority = null;
    if (values.size() > 2) {
      authority = (Authority)values.get(2);
    }
    return new LinearUnit(name, conversionFactor.doubleValue(), authority);
  }

  private PrimeMeridian processPrimeMeridian(final List<Object> values) {
    final String name = (String)values.get(0);
    final Number longitude = (Number)values.get(1);
    Authority authority = null;
    if (values.size() > 2) {
      authority = (Authority)values.get(2);
    }
    return new PrimeMeridian(name, longitude.doubleValue(), authority);
  }

  @SuppressWarnings("unchecked")
  private ProjectedCoordinateSystem processProjectedCoordinateSystem(final List<Object> values) {
    int index = 0;
    final String name = (String)values.get(index++);
    final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)values
      .get(index++);
    final Map<ParameterName, ParameterValue> parameters = new LinkedHashMap<>();

    LinearUnit linearUnit = null;
    final List<Axis> axis = new ArrayList<>();
    Authority authority = null;
    String methodName = null;
    while (index < values.size()) {
      final Object value = values.get(index++);
      if (value instanceof String) {
        methodName = (String)value;
      } else if (value instanceof Map) {
        final Map<String, List<Object>> map = (Map<String, List<Object>>)value;
        final String key = map.keySet().iterator().next();
        if (key.equals("PARAMETER")) {
          final List<Object> paramValues = map.get(key);
          final String paramName = (String)paramValues.get(0);
          final ParameterName parameterName = new SingleParameterName(paramName);
          final ParameterValueBigDecimal paramValue = new ParameterValueBigDecimal(
            (BigDecimal)paramValues.get(1));
          parameters.put(parameterName, paramValue);
        }
      } else if (value instanceof LinearUnit) {
        linearUnit = (LinearUnit)value;
      } else if (value instanceof Axis) {
        axis.add((Axis)value);
      } else if (value instanceof Authority) {
        authority = (Authority)value;
      }
    }
    final int coordinateSystemId = getCoordinateSystemId(authority);
    final CoordinateOperationMethod coordinateOperationMethod = new CoordinateOperationMethod(
      methodName);

    return new ProjectedCoordinateSystem(coordinateSystemId, name, geographicCoordinateSystem,
      coordinateOperationMethod, parameters, linearUnit, axis, authority);
  }

  private String processProjection(final List<Object> values) {
    final String name = (String)values.get(0);
    return name;
  }

  private Ellipsoid processSpheroid(final List<Object> values) {
    final String name = (String)values.get(0);
    final Number semiMajorAxis = (Number)values.get(1);
    final Number inverseFlattening = (Number)values.get(2);
    Authority authority = null;
    if (values.size() > 3) {
      authority = (Authority)values.get(3);
    }
    return new Ellipsoid(name, semiMajorAxis.doubleValue(), inverseFlattening.doubleValue(),
      authority);
  }

  private ToWgs84 processToWgs84(final List<Object> values) {
    return new ToWgs84(values);
  }

  private VerticalCoordinateSystem processVerticalCoordinateSystem(final List<Object> values) {
    final String name = (String)values.get(0);
    VerticalDatum verticalDatum = null;
    final Map<ParameterName, ParameterValue> parameters = new LinkedHashMap<>();

    LinearUnit linearUnit = null;
    Authority authority = null;
    final List<Axis> axises = new ArrayList<>();
    for (int i = 1; i < values.size(); i++) {
      final Object value = values.get(i);
      if (value instanceof VerticalDatum) {
        verticalDatum = (VerticalDatum)value;
      } else if (value instanceof LinearUnit) {
        linearUnit = (LinearUnit)value;
      } else if (value instanceof Axis) {
        final Axis axis = (Axis)value;
        axises.add(axis);
      } else if (value instanceof Authority) {
        authority = (Authority)value;
      } else if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        final Map<String, List<Object>> map = (Map<String, List<Object>>)value;
        final String key = map.keySet().iterator().next();
        if (key.equals("PARAMETER")) {
          final List<Object> paramValues = map.get(key);
          final String paramName = (String)paramValues.get(0);
          final ParameterName parameterName = new SingleParameterName(paramName);
          final BigDecimal paramValue = (BigDecimal)paramValues.get(1);
          parameters.put(parameterName, new ParameterValueBigDecimal(paramValue));
        }
      }
    }

    return new VerticalCoordinateSystem(authority, name, verticalDatum, parameters, linearUnit,
      axises);
  }

  private VerticalDatum processVerticalDatum(final List<Object> values) {
    final String name = (String)values.get(0);
    int type = 0;
    Authority authority = null;
    if (values.size() > 1) {
      type = ((Number)values.get(1)).intValue();
      if (values.size() > 2) {
        authority = (Authority)values.get(2);
      }
    }

    return new VerticalDatum(authority, name, type);
  }

  private char skipWhitespace() {
    char currentChar = this.value.charAt(this.index);
    while (Character.isWhitespace(currentChar)) {
      this.index++;
      currentChar = this.value.charAt(this.index);
    }
    return currentChar;
  }
}
