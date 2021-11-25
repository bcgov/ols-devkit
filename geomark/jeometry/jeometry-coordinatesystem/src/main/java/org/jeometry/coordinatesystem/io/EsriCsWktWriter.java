package org.jeometry.coordinatesystem.io;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Map.Entry;

import org.jeometry.coordinatesystem.model.CoordinateOperationMethod;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.ParameterName;
import org.jeometry.coordinatesystem.model.ParameterValue;
import org.jeometry.coordinatesystem.model.PrimeMeridian;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.VerticalCoordinateSystem;
import org.jeometry.coordinatesystem.model.datum.GeodeticDatum;
import org.jeometry.coordinatesystem.model.datum.VerticalDatum;
import org.jeometry.coordinatesystem.model.unit.AngularUnit;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;

public class EsriCsWktWriter {

  protected static int incrementIndent(final int indentLevel) {
    if (indentLevel < 0) {
      return -1;
    } else {
      return indentLevel + 1;
    }
  }

  public static void indent(final Writer out, final int indentLevel) throws IOException {
    if (indentLevel >= 0) {
      out.write('\n');
      for (int i = 0; i < indentLevel; i++) {
        out.write("  ");
      }
    }
  }

  public static String toString(final CoordinateSystem coordinateSystem) {
    final StringWriter string = new StringWriter();
    write(string, coordinateSystem, 0);
    return string.toString();
  }

  public static String toWkt(final CoordinateSystem coordinateSystem) {
    final StringWriter string = new StringWriter();
    write(string, coordinateSystem, -1);
    return string.toString();
  }

  public static void write(final Writer out, final AngularUnit unit, final int indentLevel)
    throws IOException {
    out.write(",");
    indent(out, indentLevel);
    out.write("UNIT[");
    write(out, unit.getName(), -1);
    out.write(',');
    write(out, unit.getConversionFactor(), -1);
    out.write(']');
  }

  public static void write(final Writer out,
    final CoordinateOperationMethod coordinateOperationMethod, final int indentLevel)
    throws IOException {
    out.write("PROJECTION[");
    write(out, coordinateOperationMethod.getName(), incrementIndent(indentLevel));
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final Writer out, final CoordinateSystem coordinateSystem,
    final int indentLevel) {
    try {
      if (coordinateSystem instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem projCs = (ProjectedCoordinateSystem)coordinateSystem;
        write(out, projCs, indentLevel);
      } else if (coordinateSystem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSystem;
        write(out, geoCs, indentLevel);
      } else if (coordinateSystem instanceof VerticalCoordinateSystem) {
        final VerticalCoordinateSystem verticalCs = (VerticalCoordinateSystem)coordinateSystem;
        write(out, verticalCs, indentLevel);
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void write(final Writer out, final Ellipsoid ellipsoid, final int indentLevel)
    throws IOException {
    out.write("SPHEROID[");
    write(out, ellipsoid.getName(), incrementIndent(indentLevel));
    out.write(',');
    final double semiMajorAxis = ellipsoid.getSemiMajorAxis();
    write(out, semiMajorAxis, incrementIndent(indentLevel));
    out.write(',');
    final double inverseFlattening = ellipsoid.getInverseFlattening();
    write(out, inverseFlattening, incrementIndent(indentLevel));
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final Writer out, final GeodeticDatum geodeticDatum,
    final int indentLevel) throws IOException {
    out.write("DATUM[");
    write(out, geodeticDatum.getName(), incrementIndent(indentLevel));
    final Ellipsoid ellipsoid = geodeticDatum.getEllipsoid();
    if (ellipsoid != null) {
      out.write(",");
      indent(out, incrementIndent(indentLevel));
      write(out, ellipsoid, incrementIndent(indentLevel));
    }
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final Writer out, final GeographicCoordinateSystem coordinateSystem,
    final int indentLevel) throws IOException {
    out.write("GEOGCS[");
    write(out, coordinateSystem.getCoordinateSystemName(), incrementIndent(indentLevel));
    final GeodeticDatum geodeticDatum = coordinateSystem.getGeodeticDatum();
    if (geodeticDatum != null) {
      out.write(",");
      indent(out, incrementIndent(indentLevel));
      write(out, geodeticDatum, incrementIndent(indentLevel));
    }
    final PrimeMeridian primeMeridian = coordinateSystem.getPrimeMeridian();
    if (primeMeridian != null) {
      out.write(",");
      indent(out, incrementIndent(indentLevel));
      write(out, primeMeridian, incrementIndent(indentLevel));
    }
    final AngularUnit unit = coordinateSystem.getAngularUnit();
    if (unit != null) {
      write(out, unit, incrementIndent(indentLevel));
    }
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final Writer out, final LinearUnit unit, final int indentLevel)
    throws IOException {
    out.write(",");
    indent(out, indentLevel);
    out.write("UNIT[");
    write(out, unit.getName(), -1);
    out.write(',');
    write(out, unit.getConversionFactor(), -1);
    out.write(']');
  }

  private static void write(final Writer out, final Number number, final int indentLevel)
    throws IOException {
    indent(out, indentLevel);
    out.write(new DecimalFormat("#0.0#################").format(number));
  }

  public static void write(final Writer out, final ParameterName name, final ParameterValue value,
    final int indentLevel) throws IOException {
    out.write(",");
    indent(out, indentLevel);
    out.write("PARAMETER[");
    write(out, name.getName(), -1);
    out.write(',');
    if (value instanceof Number) {
      final Number number = (Number)value.getOriginalValue();
      write(out, number, -1);
    } else {
      out.write(value.toString());
    }
    out.write(']');
  }

  public static void write(final Writer out, final PrimeMeridian primeMeridian,
    final int indentLevel) throws IOException {
    out.write("PRIMEM[");
    write(out, primeMeridian.getName(), incrementIndent(indentLevel));
    out.write(',');
    final double longitude = primeMeridian.getLongitude();
    write(out, longitude, incrementIndent(indentLevel));
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final Writer out, final ProjectedCoordinateSystem coordinateSystem,
    final int indentLevel) throws IOException {
    out.write("PROJCS[");
    write(out, coordinateSystem.getCoordinateSystemName(), incrementIndent(indentLevel));
    final GeographicCoordinateSystem geoCs = coordinateSystem.getGeographicCoordinateSystem();
    if (geoCs != null) {
      out.write(",");
      indent(out, incrementIndent(indentLevel));
      write(out, geoCs, incrementIndent(indentLevel));
    }
    final CoordinateOperationMethod coordinateOperationMethod = coordinateSystem
      .getCoordinateOperationMethod();
    if (coordinateOperationMethod != null) {
      out.write(",");
      indent(out, incrementIndent(indentLevel));
      write(out, coordinateOperationMethod, incrementIndent(indentLevel));
    }
    for (final Entry<ParameterName, ParameterValue> parameter : coordinateSystem
      .getParameterValues()
      .entrySet()) {
      final ParameterName name = parameter.getKey();
      final ParameterValue value = parameter.getValue();
      write(out, name, value, incrementIndent(indentLevel));
    }
    final LinearUnit unit = coordinateSystem.getLinearUnit();
    if (unit != null) {
      write(out, unit, incrementIndent(indentLevel));
    }
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final Writer out, final String value, final int indentLevel)
    throws IOException {
    indent(out, indentLevel);
    out.write('"');
    if (value != null) {
      out.write(value);
    }
    out.write('"');
  }

  public static void write(final Writer out, final VerticalCoordinateSystem coordinateSystem,
    final int indentLevel) throws IOException {
    out.write("VERTCS[");
    write(out, coordinateSystem.getCoordinateSystemName(), incrementIndent(indentLevel));
    final VerticalDatum datum = coordinateSystem.getDatum();
    if (datum != null) {
      out.write(",");
      indent(out, incrementIndent(indentLevel));
      write(out, datum, incrementIndent(indentLevel));
    }
    for (final Entry<ParameterName, ParameterValue> parameter : coordinateSystem
      .getParameterValues()
      .entrySet()) {
      final ParameterName name = parameter.getKey();
      final ParameterValue value = parameter.getValue();
      write(out, name, value, incrementIndent(indentLevel));
    }
    final LinearUnit unit = coordinateSystem.getLinearUnit();
    if (unit != null) {
      write(out, unit, incrementIndent(indentLevel));
    }
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final Writer out, final VerticalDatum verticalDatum,
    final int indentLevel) throws IOException {
    out.write("VDATUM[");
    write(out, verticalDatum.getName(), incrementIndent(indentLevel));
    final int type = verticalDatum.getDatumType();
    if (type > 0) {
      out.write(",");
      write(out, type, incrementIndent(indentLevel));
    }
    indent(out, indentLevel);
    out.write(']');
  }
}
