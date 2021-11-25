package org.jeometry.coordinatesystem.model;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.jeometry.coordinatesystem.model.datum.GeodeticDatum;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;
import org.jeometry.coordinatesystem.model.unit.AngularUnit;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;
import org.jeometry.coordinatesystem.model.unit.Radian;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.gridshift.HorizontalShiftOperation;

import tech.units.indriya.unit.Units;

public class GeographicCoordinateSystem extends AbstractHorizontalCoordinateSystem {
  private static PrimeMeridian getPrimeMeridian(final GeodeticDatum datum) {
    if (datum == null) {
      return null;
    } else {
      return datum.getPrimeMeridian();
    }
  }

  private Map<GeographicCoordinateSystem, GeographicCoordinateSystemGridShiftOperation> gridShiftOperationsByCoordinateSystem;

  private final AngularUnit angularUnit;

  private final GeodeticDatum geodeticDatum;

  private final PrimeMeridian primeMeridian;

  private CoordinateSystem sourceCoordinateSystem;

  private CoordinateOperation coordinateOperation;

  public GeographicCoordinateSystem(final int id, final String name,
    final GeodeticDatum geodeticDatum, final List<Axis> axis, final Area area,
    final CoordinateSystem sourceCoordinateSystem, final CoordinateOperation coordinateOperation,
    final boolean deprecated) {
    this(id, name, geodeticDatum, getPrimeMeridian(geodeticDatum), axis, area,
      sourceCoordinateSystem, coordinateOperation, deprecated);
  }

  public GeographicCoordinateSystem(final int id, final String name,
    final GeodeticDatum geodeticDatum, final PrimeMeridian primeMeridian,
    final AngularUnit angularUnit, final List<Axis> axis, final Authority authority) {
    super(id, name, axis, authority);
    this.geodeticDatum = geodeticDatum;
    this.primeMeridian = primeMeridian;
    this.angularUnit = angularUnit;
  }

  public GeographicCoordinateSystem(final int id, final String name,
    final GeodeticDatum geodeticDatum, final PrimeMeridian primeMeridian, final List<Axis> axis,
    final Area area, final CoordinateSystem sourceCoordinateSystem,
    final CoordinateOperation coordinateOperation, final boolean deprecated) {
    super(id, name, axis, area, deprecated);
    this.geodeticDatum = geodeticDatum;
    this.primeMeridian = primeMeridian;
    this.angularUnit = (AngularUnit)axis.get(0).getUnit();
    this.sourceCoordinateSystem = sourceCoordinateSystem;
    this.coordinateOperation = coordinateOperation;
  }

  public GeographicCoordinateSystem(final String name, final Ellipsoid ellipsoid) {
    this(0, name, new GeodeticDatum(name, ellipsoid, null, null), new PrimeMeridian(name, 0, null),
      EpsgCoordinateSystems.getUnit(9102), null, null);
  }

  protected void addConversionOperation(final List<CoordinatesOperation> operations,
    final GeographicCoordinateSystem targetGeoCs, final AngularUnit sourceAngularUnit,
    final AngularUnit targetAngularUnit) {
    if (this != targetGeoCs && this.gridShiftOperationsByCoordinateSystem != null) {
      final GeographicCoordinateSystemGridShiftOperation gridShiftOperation = this.gridShiftOperationsByCoordinateSystem
        .get(targetGeoCs);
      if (gridShiftOperation != null) {
        sourceAngularUnit.addToDegreesOperation(operations);
        operations.add(gridShiftOperation);
        targetAngularUnit.addFromDegreesOperation(operations);
        return;
      }
    }
    sourceAngularUnit.addConversionOperation(operations, targetAngularUnit);

  }

  @Override
  protected void addCoordinatesOperations(final List<CoordinatesOperation> operations,
    final GeographicCoordinateSystem targetGeoCs) {
    if (this != targetGeoCs) {
      final AngularUnit angularUnit = getAngularUnit();
      final AngularUnit targetAngularUnit = targetGeoCs.getAngularUnit();
      addConversionOperation(operations, targetGeoCs, angularUnit, targetAngularUnit);
    }
  }

  @Override
  protected void addCoordinatesOperations(final List<CoordinatesOperation> operations,
    final ProjectedCoordinateSystem targetProjCs) {
    final AngularUnit angularUnit = getAngularUnit();
    final GeographicCoordinateSystem targetGeoCs = targetProjCs.getGeographicCoordinateSystem();

    // Convert units, datum conversion if supported and convert to radians
    final Radian radian = Radian.getInstance();
    addConversionOperation(operations, targetGeoCs, angularUnit, radian);

    targetProjCs.addProjectionOperations(operations);
  }

  public synchronized void addGridShiftOperation(final GeographicCoordinateSystem coordinateSystem,
    final HorizontalShiftOperation operation) {
    if (this.gridShiftOperationsByCoordinateSystem == null) {
      this.gridShiftOperationsByCoordinateSystem = new HashMap<>();
    }
    GeographicCoordinateSystemGridShiftOperation operations = this.gridShiftOperationsByCoordinateSystem
      .get(coordinateSystem);
    if (operations == null) {
      operations = new GeographicCoordinateSystemGridShiftOperation(this, coordinateSystem);
      this.gridShiftOperationsByCoordinateSystem.put(coordinateSystem, operations);
    }
    operations.addOperation(operation);
  }

  @Override
  public GeographicCoordinateSystem clone() {
    return (GeographicCoordinateSystem)super.clone();
  }

  public double distanceMetres(final double lon1, final double lat1, final double lon2,
    final double lat2) {
    final Ellipsoid ellipsoid = getEllipsoid();
    return ellipsoid.distanceMetres(lon1, lat1, lon2, lat2);
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem cs = (GeographicCoordinateSystem)object;
      if (!equals(this.geodeticDatum, cs.geodeticDatum)) {
        return false;
      } else if (!equals(this.primeMeridian, cs.primeMeridian)) {
        return false;
      } else if (!equals(this.angularUnit, cs.angularUnit)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean equalsExact(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)coordinateSystem;
      return equalsExact(geographicCoordinateSystem);
    }
    return false;
  }

  public boolean equalsExact(final GeographicCoordinateSystem cs) {
    if (super.equalsExact(cs)) {
      if (!equals(this.angularUnit, cs.angularUnit)) {
        return false;
      } else if (!equals(this.geodeticDatum, cs.geodeticDatum)) {
        return false;
      } else if (!equals(this.primeMeridian, cs.primeMeridian)) {
        return false;
      } else {
        return true;
      }
    }
    return false;
  }

  public AngularUnit getAngularUnit() {
    return this.angularUnit;
  }

  public CoordinateOperation getCoordinateOperation() {
    return this.coordinateOperation;
  }

  @Override
  public CoordinateSystemType getCoordinateSystemType() {
    return CoordinateSystemType.GEOGRAPHIC;
  }

  public GeodeticDatum getDatum() {
    return this.geodeticDatum;
  }

  @Override
  public GeodeticDatum getGeodeticDatum() {
    return this.geodeticDatum;
  }

  @Override
  public Unit<Length> getLengthUnit() {
    final Unit<Angle> unit = this.angularUnit.getUnit();
    final UnitConverter radianConverter = unit.getConverterTo(Units.RADIAN);

    final Ellipsoid ellipsoid = getEllipsoid();
    final double radius = ellipsoid.getSemiMajorAxis();
    final double radianFactor = radianConverter.convert(1);
    return Units.METRE.multiply(radius).multiply(radianFactor);
  }

  @Override
  public LinearUnit getLinearUnit() {

    final Ellipsoid ellipsoid = getEllipsoid();
    final double radius = ellipsoid.getSemiMajorAxis();
    final double radianFactor = this.angularUnit.toRadians(1);
    final double metres = radius * radianFactor;
    return new LinearUnit("custom", metres);
  }

  public PrimeMeridian getPrimeMeridian() {
    return this.primeMeridian;
  }

  public CoordinateSystem getSourceCoordinateSystem() {
    return this.sourceCoordinateSystem;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Unit<Angle> getUnit() {
    return this.angularUnit.getUnit();
  }

  @Override
  public String getUnitLabel() {
    return this.angularUnit.getLabel();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (this.geodeticDatum != null) {
      result = prime * result + this.geodeticDatum.hashCode();
    }
    if (getPrimeMeridian() != null) {
      result = prime * result + getPrimeMeridian().hashCode();
    }
    return result;
  }

  @Override
  public boolean isSame(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof GeographicCoordinateSystem) {
      return isSame((GeographicCoordinateSystem)coordinateSystem);
    } else {
      return false;
    }
  }

  public boolean isSame(final GeographicCoordinateSystem coordinateSystem) {
    if (this.primeMeridian == null) {
      if (coordinateSystem.primeMeridian != null) {
        return false;
      }
    } else if (!this.primeMeridian.isSame(coordinateSystem.primeMeridian)) {
      return false;
    }

    if (this.geodeticDatum.isSame(coordinateSystem.geodeticDatum)) {
      if (this.angularUnit.isSame(coordinateSystem.angularUnit)) {
        return true;
      }
    }
    return false;
  }

  public synchronized void removeGridShiftOperation(
    final GeographicCoordinateSystem coordinateSystem, final HorizontalShiftOperation operation) {
    if (this.gridShiftOperationsByCoordinateSystem != null) {
      final GeographicCoordinateSystemGridShiftOperation operations = this.gridShiftOperationsByCoordinateSystem
        .get(coordinateSystem);
      if (operations != null) {
        operations.removeOperation(operation);
      }
    }
  }

  @Override
  public void updateDigest(final MessageDigest digest) {
    this.geodeticDatum.updateDigest(digest);
    this.primeMeridian.updateDigest(digest);
    this.angularUnit.updateDigest(digest);
  }
}
