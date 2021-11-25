package com.revolsys.geometry.operation.valid;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

/**
 * Contains information about the nature and location of a {@link Geometry}
 * validation error
 *
 * @version 1.7
 */
public class TopologyValidationError extends AbstractGeometryValidationError {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Indicates that the interior of a polygon is disjoint
   * (often caused by set of contiguous holes splitting the polygon into two parts)
   */
  public static final int DISCONNECTED_INTERIOR = 4;

  /**
   * Indicates that a polygonal geometry contains two rings which are identical
   */
  public static final int DUPLICATE_RINGS = 8;

  /**
   * Messages corresponding to error codes
   */
  public static final String[] errMsg = {
    "Topology Validation Error", "Repeated Point", "Hole lies outside shell", "Holes are nested",
    "Interior is disconnected", "Self-intersection", "Ring Self-intersection", "Nested shells",
    "Duplicate Rings", "Too few distinct points in geometry component", "Invalid Coordinate",
    "Ring is not closed"
  };

  /**
   * Indicates that a hole of a polygon lies partially or completely in the exterior of the shell
   */
  public static final int HOLE_OUTSIDE_SHELL = 2;

  /**
   * Indicates that a hole lies in the interior of another hole in the same polygon
   */
  public static final int NESTED_HOLES = 3;

  /**
   * Indicates that a polygon component of a MultiPolygon lies inside another polygonal component
   */
  public static final int NESTED_SHELLS = 7;

  /**
   * Indicates that a ring is not correctly closed
   * (the first and the last coordinate are different)
   */
  public static final int RING_NOT_CLOSED = 11;

  /**
   * Indicates that a ring self-intersects
   */
  public static final int RING_SELF_INTERSECTION = 6;

  /**
   * Indicates that two rings of a polygonal geometry intersect
   */
  public static final int SELF_INTERSECTION = 5;

  /**
   * Indicates that either
   * <ul>
   * <li>a LineString contains a single point
   * <li>a LinearRing contains 2 or 3 points
   * </ul>
   */
  public static final int TOO_FEW_POINTS = 9;

  private final int errorType;

  private Point pt;

  /**
   * Creates a validation error with the given type and location
   *
   * @param errorType the type of the error
   * @param pt the location of the error
   */
  public TopologyValidationError(final int errorType, final Point pt) {
    super(errMsg[errorType], pt);
    this.errorType = errorType;
  }

  /**
   * Returns the location of this error (on the {@link Geometry} containing the error).
   *
   * @return a {@link Coordinates} on the input geometry
   */
  public Point getCoordinate() {
    return this.pt;
  }

  /**
   * Gets the type of this error.
   *
   * @return the error type
   */
  public int getErrorType() {
    return this.errorType;
  }

  /**
   * Gets an error message describing this error.
   * The error message does not describe the location of the error.
   *
   * @return the error message
   */
  @Override
  public String getMessage() {
    return errMsg[this.errorType];
  }

  /**
   * Gets a message describing the type and location of this error.
   * @return the error message
   */
  @Override
  public String toString() {
    String locStr = "";
    if (this.pt != null) {
      locStr = " at or near point " + this.pt;
    }
    return getMessage() + locStr;
  }
}
