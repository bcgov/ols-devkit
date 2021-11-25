package org.jeometry.coordinatesystem.io;

import java.io.PrintWriter;
import java.util.Map.Entry;

import org.jeometry.coordinatesystem.model.Authority;
import org.jeometry.coordinatesystem.model.CoordinateOperationMethod;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.ParameterName;
import org.jeometry.coordinatesystem.model.PrimeMeridian;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.datum.GeodeticDatum;
import org.jeometry.coordinatesystem.model.unit.AngularUnit;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;

public class EpsgCsWktWriter {

  public static void write(final PrintWriter out, final AngularUnit unit) {
    if (unit != null) {
      out.print(",UNIT[");
      write(out, unit.getName());
      out.write(',');
      out.print(unit.getConversionFactor());
      final Authority authority = unit.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  public static void write(final PrintWriter out, final Authority authority) {
    if (authority != null) {
      out.print(",AUTHORITY[");
      write(out, authority.getName());
      out.write(",\"");
      out.print(authority.getCode());
      out.write("\"]");
    }
  }

  public static void write(final PrintWriter out,
    final CoordinateOperationMethod coordinateOperationMethod) {
    if (coordinateOperationMethod != null) {
      out.print(",PROJECTION[");
      write(out, coordinateOperationMethod.getName());
      out.write(']');
    }
  }

  public static void write(final PrintWriter out, final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projCs = (ProjectedCoordinateSystem)coordinateSystem;
      write(out, projCs);
    } else if (coordinateSystem instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSystem;
      write(out, geoCs);
    }
  }

  private static void write(final PrintWriter out, final double number) {
    out.print(number);

  }

  public static void write(final PrintWriter out, final Ellipsoid ellipsoid) {
    if (ellipsoid != null) {
      out.print(",SPHEROID[");
      write(out, ellipsoid.getName());
      out.write(',');
      final double semiMajorAxis = ellipsoid.getSemiMajorAxis();
      write(out, semiMajorAxis);
      out.print(',');
      final double inverseFlattening = ellipsoid.getInverseFlattening();
      write(out, inverseFlattening);
      final Authority authority = ellipsoid.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  public static void write(final PrintWriter out, final GeodeticDatum geodeticDatum) {
    if (geodeticDatum != null) {
      out.print(",DATUM[");
      write(out, geodeticDatum.getName());
      final Ellipsoid ellipsoid = geodeticDatum.getEllipsoid();
      if (ellipsoid != null) {
        write(out, ellipsoid);
      }
      final Authority authority = geodeticDatum.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  public static void write(final PrintWriter out,
    final GeographicCoordinateSystem coordinateSystem) {
    if (coordinateSystem != null) {
      out.print("GEOGCS[");
      write(out, coordinateSystem.getCoordinateSystemName());
      final GeodeticDatum geodeticDatum = coordinateSystem.getGeodeticDatum();
      write(out, geodeticDatum);
      final PrimeMeridian primeMeridian = coordinateSystem.getPrimeMeridian();
      write(out, primeMeridian);
      final AngularUnit unit = coordinateSystem.getAngularUnit();
      write(out, unit);
      final Authority authority = coordinateSystem.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  public static void write(final PrintWriter out, final LinearUnit unit) {
    if (unit != null) {
      out.print(",UNIT[");
      write(out, unit.getName());
      out.write(',');
      write(out, unit.getConversionFactor());
      final Authority authority = unit.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  private static void write(final PrintWriter out, final Number number) {
    // out.print(Doubles.toString(number));

  }

  public static void write(final PrintWriter out, final ParameterName name, final Object value) {
    out.print(",PARAMETER[");
    write(out, name.getName());
    out.write(',');
    if (value instanceof Number) {
      final Number number = (Number)value;
      write(out, number);
    } else {
      out.print(value);
    }
    out.write(']');
  }

  public static void write(final PrintWriter out, final PrimeMeridian primeMeridian) {
    if (primeMeridian != null) {
      out.print(",PRIMEM[");
      write(out, primeMeridian.getName());
      out.write(',');
      final double longitude = primeMeridian.getLongitude();
      write(out, longitude);
      final Authority authority = primeMeridian.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  public static void write(final PrintWriter out,
    final ProjectedCoordinateSystem coordinateSystem) {
    if (coordinateSystem != null) {
      out.print("PROJCS[");
      write(out, coordinateSystem.getCoordinateSystemName());
      final GeographicCoordinateSystem geoCs = coordinateSystem.getGeographicCoordinateSystem();
      out.print(",");
      write(out, geoCs);
      final CoordinateOperationMethod coordinateOperationMethod = coordinateSystem
        .getCoordinateOperationMethod();
      write(out, coordinateOperationMethod);
      for (final Entry<ParameterName, Object> parameter : coordinateSystem.getParameters()
        .entrySet()) {
        final ParameterName name = parameter.getKey();
        final Object value = parameter.getValue();
        write(out, name, value);
      }
      final LinearUnit unit = coordinateSystem.getLinearUnit();
      if (unit != null) {
        write(out, unit);
      }
      final Authority authority = coordinateSystem.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  public static void write(final PrintWriter out, final String value) {
    out.write('"');
    if (value != null) {
      out.print(value);
    }
    out.write('"');
  }
}
