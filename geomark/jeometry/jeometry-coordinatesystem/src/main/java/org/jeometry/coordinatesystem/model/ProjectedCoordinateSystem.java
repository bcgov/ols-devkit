package org.jeometry.coordinatesystem.model;

import java.security.MessageDigest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.coordinatesystem.model.datum.GeodeticDatum;
import org.jeometry.coordinatesystem.model.unit.AngularUnit;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;
import org.jeometry.coordinatesystem.model.unit.Radian;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.projection.CoordinatesProjection;
import org.jeometry.coordinatesystem.util.Equals;

public class ProjectedCoordinateSystem extends AbstractHorizontalCoordinateSystem {

  private CoordinatesProjection coordinatesProjection;

  private final GeographicCoordinateSystem geographicCoordinateSystem;

  private final LinearUnit linearUnit;

  private final Map<ParameterName, Object> parameters = new LinkedHashMap<>();

  private final Map<ParameterName, ParameterValue> parameterValues;

  private final CoordinateOperationMethod coordinateOperationMethod;

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem, final Area area,
    final CoordinateOperationMethod coordinateOperationMethod,
    final Map<ParameterName, ParameterValue> parameterValues, final LinearUnit linearUnit,
    final List<Axis> axis, final Authority authority, final boolean deprecated) {
    super(id, name, axis, area, deprecated);
    this.geographicCoordinateSystem = geographicCoordinateSystem;
    this.coordinateOperationMethod = coordinateOperationMethod;
    if (parameterValues == null) {
      this.parameterValues = new LinkedHashMap<>();
    } else {
      this.parameterValues = parameterValues;
    }
    for (final Entry<ParameterName, ParameterValue> entry : this.parameterValues.entrySet()) {
      final ParameterName parameterName = entry.getKey();
      final ParameterValue parameterValue = entry.getValue();
      final Object value;
      if (parameterValue == null) {
        value = parameterName.getDefaultValue();
      } else {
        value = parameterValue.getValue();
      }
      this.parameters.put(parameterName, value);
    }
    this.linearUnit = linearUnit;
  }

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem,
    final CoordinateOperationMethod coordinateOperationMethod,
    final Map<ParameterName, ParameterValue> parameters, final LinearUnit linearUnit) {
    this(id, name, geographicCoordinateSystem, null, coordinateOperationMethod, parameters,
      linearUnit, null, null, false);
  }

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem,
    final CoordinateOperationMethod coordinateOperationMethod,
    final Map<ParameterName, ParameterValue> parameters, final LinearUnit linearUnit,
    final List<Axis> axis, final Authority authority) {
    this(id, name, geographicCoordinateSystem, null, coordinateOperationMethod, parameters,
      linearUnit, axis, authority, false);
  }

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem, final String methodName,
    final Map<ParameterName, ParameterValue> parameterValues, final LinearUnit linearUnit,
    final Authority authority) {
    this(id, name, geographicCoordinateSystem, null, new CoordinateOperationMethod(methodName),
      parameterValues, linearUnit, Collections.emptyList(), authority, false);
  }

  @Override
  protected void addCoordinatesOperations(final List<CoordinatesOperation> operations,
    final GeographicCoordinateSystem targetGeoCs) {
    addInverseOperations(operations);

    final Radian radian = Radian.getInstance();
    final AngularUnit targetAngularUnit = targetGeoCs.getAngularUnit();
    this.geographicCoordinateSystem.addConversionOperation(operations, targetGeoCs, radian,
      targetAngularUnit);
  }

  @Override
  protected void addCoordinatesOperations(final List<CoordinatesOperation> operations,
    final ProjectedCoordinateSystem coordinateSystem) {
    addInverseOperations(operations);

    final GeographicCoordinateSystem targetGeoCs = coordinateSystem.geographicCoordinateSystem;
    final Radian radian = Radian.getInstance();
    this.geographicCoordinateSystem.addConversionOperation(operations, targetGeoCs, radian, radian);

    coordinateSystem.addProjectionOperations(operations);
  }

  public void addInverseOperations(final List<CoordinatesOperation> operations) {
    final CoordinatesProjection projection = getCoordinatesProjection();
    if (projection != null) {
      final CoordinatesOperation inverseOperation = projection.getInverseOperation();
      if (inverseOperation != null) {
        this.linearUnit.addToMetresOperation(operations);
        operations.add(inverseOperation);
        return;
      }
    }
    throw new IllegalArgumentException("No inverse operation found for " + this);
  }

  public void addProjectionOperations(final List<CoordinatesOperation> operations) {
    final CoordinatesProjection projection = getCoordinatesProjection();
    if (projection != null) {
      final CoordinatesOperation projectionOperation = projection.getProjectOperation();
      if (projectionOperation != null) {
        operations.add(projectionOperation);
        this.linearUnit.addFromMetresOperation(operations);
        return;
      }
    }
    throw new IllegalArgumentException("No projection operation found for " + this);
  }

  @Override
  public ProjectedCoordinateSystem clone() {
    return (ProjectedCoordinateSystem)super.clone();
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem cs = (ProjectedCoordinateSystem)object;
      if (!this.geographicCoordinateSystem.equals(cs.geographicCoordinateSystem)) {
        return false;
      } else if (this.coordinateOperationMethod == null ? cs.coordinateOperationMethod != null
        : !this.coordinateOperationMethod.equals(cs.coordinateOperationMethod)) {
        return false;
      } else if (!this.parameters.equals(cs.parameters)) {
        return false;
      } else if (!this.linearUnit.equals(cs.linearUnit)) {
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
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
      return equalsExact(projectedCoordinateSystem);
    }
    return false;
  }

  public boolean equalsExact(final ProjectedCoordinateSystem cs) {
    if (super.equalsExact(cs)) {
      if (!this.geographicCoordinateSystem.equals(cs.geographicCoordinateSystem)) {
        return false;
      } else if (!Equals.equals(this.linearUnit, cs.linearUnit)) {
        return false;
      } else if (!Equals.equals(this.parameters, cs.parameters)) {
        return false;
      } else if (!Equals.equals(this.coordinateOperationMethod, cs.coordinateOperationMethod)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public CoordinateOperationMethod getCoordinateOperationMethod() {
    return this.coordinateOperationMethod;
  }

  @SuppressWarnings("unchecked")
  public synchronized <P extends CoordinatesProjection> P getCoordinatesProjection() {
    if (this.coordinatesProjection == null) {
      this.coordinatesProjection = this.coordinateOperationMethod.newCoordinatesProjection(this);
    }
    return (P)this.coordinatesProjection;
  }

  @Override
  public CoordinateSystemType getCoordinateSystemType() {
    return CoordinateSystemType.PROJECTED;
  }

  public double getDoubleParameter(final ParameterName key) {
    final Number value = getParameter(key);
    if (value == null) {
      return Double.NaN;
    } else {
      return value.doubleValue();
    }
  }

  public double getDoubleParameter(final ParameterName key, final double defaultValue) {
    final Number value = getParameter(key);
    if (value == null) {
      return defaultValue;
    } else {
      return value.doubleValue();
    }
  }

  @Override
  public GeodeticDatum getGeodeticDatum() {
    return this.geographicCoordinateSystem.getGeodeticDatum();
  }

  public GeographicCoordinateSystem getGeographicCoordinateSystem() {
    return this.geographicCoordinateSystem;
  }

  public int getGeographicCoordinateSystemId() {
    return this.geographicCoordinateSystem.getHorizontalCoordinateSystemId();
  }

  @Override
  public Unit<Length> getLengthUnit() {
    return this.linearUnit.getUnit();
  }

  @Override
  public LinearUnit getLinearUnit() {
    return this.linearUnit;
  }

  @SuppressWarnings("unchecked")
  public <V> V getParameter(final ParameterName key) {
    return (V)key.getValue(this.parameters);
  }

  public Map<ParameterName, Object> getParameters() {
    return this.parameters;
  }

  public Map<ParameterName, ParameterValue> getParameterValues() {
    return this.parameterValues;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Unit<Length> getUnit() {
    return this.linearUnit.getUnit();
  }

  @Override
  public String getUnitLabel() {
    return this.linearUnit.getLabel();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (this.geographicCoordinateSystem != null) {
      result = prime * result + this.geographicCoordinateSystem.hashCode();
    }
    if (this.coordinateOperationMethod != null) {
      result = prime * result + this.coordinateOperationMethod.hashCode();
    }
    for (final Entry<ParameterName, Object> entry : this.parameters.entrySet()) {
      final ParameterName key = entry.getKey();
      result = prime * result + key.hashCode();
      final Object value = entry.getValue();
      if (value != null) {
        result = prime * result + value.hashCode();
      }
    }
    if (this.linearUnit != null) {
      result = prime * result + this.linearUnit.hashCode();
    }
    return result;
  }

  @Override
  public boolean isSame(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      return isSame((ProjectedCoordinateSystem)coordinateSystem);
    } else {
      return false;
    }
  }

  public boolean isSame(final ProjectedCoordinateSystem coordinateSystem) {
    if (this.coordinateOperationMethod.isSame(coordinateSystem.coordinateOperationMethod)) {
      if (this.geographicCoordinateSystem.isSame(coordinateSystem.geographicCoordinateSystem)) {
        if (this.linearUnit.isSame(coordinateSystem.linearUnit)) {
          final Set<ParameterName> parameterNames = this.parameterValues.keySet();
          final Set<ParameterName> parameterNames2 = coordinateSystem.parameterValues.keySet();
          if (parameterNames.equals(parameterNames2)) {
            for (final ParameterName parameterName : parameterNames) {
              final ParameterValue parameterValue1 = this.parameterValues.get(parameterName);
              final ParameterValue parameterValue2 = coordinateSystem.parameterValues
                .get(parameterName);
              if (parameterValue1 == null) {
                if (parameterValue2 != null) {
                  return false;
                }
              } else if (!parameterValue1.isSame(parameterValue2)) {
                return false;
              }
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public void updateDigest(final MessageDigest digest) {
    if (this.geographicCoordinateSystem != null) {
      this.geographicCoordinateSystem.updateDigest(digest);
    }
    if (this.coordinateOperationMethod != null) {
      this.coordinateOperationMethod.updateDigest(digest);
    }
    if (this.linearUnit != null) {
      this.linearUnit.updateDigest(digest);
    }
    final Map<ParameterName, ParameterValue> params = new TreeMap<>(this.parameterValues);
    for (final Entry<ParameterName, ParameterValue> entry : params.entrySet()) {
      final ParameterName name = entry.getKey();
      name.updateDigest(digest);
      final ParameterValue value = entry.getValue();
      value.updateDigest(digest);
    }
  }
}
