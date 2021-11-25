package org.jeometry.coordinatesystem.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.coordinatesystem.operation.ChainedCoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.NoOpOperation;
import org.slf4j.LoggerFactory;

public abstract class AbstractCoordinateSystem implements CoordinateSystem {

  private final Area area;

  private final Authority authority;

  private final List<Axis> axis = new ArrayList<>();

  private final boolean deprecated;

  private final int id;

  private final String name;

  private Map<CoordinateSystem, CoordinatesOperation> coordinatesOperationByCoordinateSystem;

  public AbstractCoordinateSystem(final int id, final String name, final List<Axis> axis,
    final Area area, final boolean deprecated) {
    this(id, name, axis, area, deprecated, new EpsgAuthority(id));
  }

  public AbstractCoordinateSystem(final int id, final String name, final List<Axis> axis,
    final Area area, final boolean deprecated, final Authority authority) {
    this.id = id;
    this.name = name;
    if (axis != null && !axis.isEmpty()) {
      this.axis.addAll(axis);
    }
    this.area = area;
    this.deprecated = deprecated;
    if (id > 0) {
      this.authority = new EpsgAuthority(id);
    } else {
      this.authority = null;
    }
  }

  public AbstractCoordinateSystem(final int id, final String name, final List<Axis> axis,
    final Authority authority) {
    this(id, name, axis, null, false, authority);
  }

  protected void addCoordinatesOperations(final List<CoordinatesOperation> operations,
    final GeographicCoordinateSystem coordinateSystem) {
    throw new IllegalArgumentException(
      "Coordinate system type not supported\nGeographicCoordinateSystem\n" + coordinateSystem);
  }

  protected void addCoordinatesOperations(final List<CoordinatesOperation> operations,
    final ProjectedCoordinateSystem coordinateSystem) {
    throw new IllegalArgumentException(
      "Coordinate system type not supported\nProjectedCoordinateSystem\n" + coordinateSystem);
  }

  @Override
  public AbstractCoordinateSystem clone() {
    try {
      return (AbstractCoordinateSystem)super.clone();
    } catch (final Exception e) {
      return null;
    }
  }

  protected boolean equals(final Object object1, final Object object2) {
    if (object1 == object2) {
      return true;
    } else if (object1 == null || object2 == null) {
      return false;
    } else {
      return object1.equals(object2);
    }
  }

  protected boolean equalsExact(final AbstractCoordinateSystem cs) {
    if (cs == null) {
      return false;
    } else if (cs == this) {
      return true;
    } else {
      if (!equals(this.area, cs.area)) {
        return false;
      } else if (!equals(this.authority, cs.authority)) {
        return false;
      } else if (!equals(this.axis, cs.axis)) {
        return false;
      } else if (this.deprecated != cs.deprecated) {
        return false;
      } else if (this.id != cs.id) {
        return false;
      } else if (!equals(this.name, cs.name)) {
        return false;
      } else {
        return true;
      }
    }
  }

  @Override
  public Area getArea() {
    return this.area;
  }

  @Override
  public Authority getAuthority() {
    return this.authority;
  }

  @Override
  public List<Axis> getAxis() {
    return this.axis;
  }

  @Override
  public CoordinatesOperation getCoordinatesOperation(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == this) {
      return null;
    } else {
      if (this.coordinatesOperationByCoordinateSystem == null) {
        synchronized (this) {
          if (this.coordinatesOperationByCoordinateSystem == null) {
            this.coordinatesOperationByCoordinateSystem = new HashMap<>();
          }
        }
      }
      synchronized (this.coordinatesOperationByCoordinateSystem) {
        CoordinatesOperation coordinatesOperation = this.coordinatesOperationByCoordinateSystem
          .get(coordinateSystem);
        if (coordinatesOperation == null) {
          try {
            coordinatesOperation = newCoordinatesOperation(coordinateSystem);
          } catch (final IllegalArgumentException e) {
            coordinatesOperation = NoOpOperation.INSTANCE;
            LoggerFactory.getLogger(getClass())
              .error("Cannot get conversion from " + this + " to " + coordinateSystem, e);
          }
          this.coordinatesOperationByCoordinateSystem.put(coordinateSystem, coordinatesOperation);
        }
        if (coordinatesOperation == NoOpOperation.INSTANCE) {
          return null;
        } else {
          return coordinatesOperation;
        }
      }
    }
  }

  @Override
  public int getCoordinateSystemId() {
    return this.id;
  }

  @Override
  public String getCoordinateSystemName() {
    return this.name;
  }

  @Override
  public boolean isDeprecated() {
    return this.deprecated;
  }

  protected CoordinatesOperation newCoordinatesOperation(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null || this == coordinateSystem) {
      return null;
    } else {
      final List<CoordinatesOperation> operations = new ArrayList<>();
      if (coordinateSystem instanceof GeographicCoordinateSystem) {
        addCoordinatesOperations(operations, (GeographicCoordinateSystem)coordinateSystem);
      } else if (coordinateSystem instanceof ProjectedCoordinateSystem) {
        addCoordinatesOperations(operations, (ProjectedCoordinateSystem)coordinateSystem);
      } else {
        throw new IllegalArgumentException("Coordinate system type not supported\n"
          + coordinateSystem.getCoordinateSystemType() + "\n" + coordinateSystem);
      }
      final int operationCount = operations.size();
      if (operationCount == 0) {
        return NoOpOperation.INSTANCE;
      } else if (operationCount == 1) {
        return operations.get(0);
      } else {
        return new ChainedCoordinatesOperation(operations);
      }
    }
  }

  @Override
  public String toString() {
    return this.name;
  }

}
