package com.revolsys.geometry.model.editor;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;

public abstract class AbstractGeometryEditor<GE extends GeometryEditor<?>>
  implements GeometryEditor<GE> {
  private static final long serialVersionUID = 1L;

  private AbstractGeometryEditor<?> parentEditor;

  private GeometryFactory geometryFactory;

  private boolean modified;

  public AbstractGeometryEditor(final AbstractGeometryEditor<?> parentEditor) {
    this(parentEditor.getGeometryFactory());
    this.parentEditor = parentEditor;
  }

  public AbstractGeometryEditor(final AbstractGeometryEditor<?> parentEditor,
    final Geometry geometry) {
    this(geometry.getGeometryFactory());
    this.parentEditor = parentEditor;
  }

  public AbstractGeometryEditor(final AbstractGeometryEditor<?> parentEditor,
    final GeometryFactory geometryFactory) {
    this.parentEditor = parentEditor;
    if (parentEditor == null) {
      setGeometryFactory(geometryFactory);
    } else {
      this.geometryFactory = parentEditor.getGeometryFactory();
    }
  }

  public AbstractGeometryEditor(final Geometry geometry) {
    this(null, geometry);
  }

  public AbstractGeometryEditor(final GeometryFactory geometryFactory) {
    setGeometryFactory(geometryFactory);
  }

  /**
   * Creates and returns a full copy of this {@link Polygon} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public Geometry clone() {
    try {
      return (Geometry)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  /**
   * Tests whether this geometry is structurally and numerically equal
   * to a given <code>Object</code>.
   * If the argument <code>Object</code> is not a <code>Geometry</code>,
   * the result is <code>false</code>.
   * Otherwise, the result is computed using
   * {@link #equals(2,Geometry)}.
   * <p>
   * This method is provided to fulfill the Java contract
   * for value-based object equality.
   * In conjunction with {@link #hashCode()}
   * it provides semantics which are most useful
   * for using
   * <code>Geometry</code>s as keys and values in Java collections.
   * <p>
   * Note that to produce the expected result the input geometries
   * should be in normal form.  It is the caller's
   * responsibility to perform this where required
   * (using {@link Geometry#norm()
   * or {@link #normalize()} as appropriate).
   *
   * @param other the Object to compare
   * @return true if this geometry is exactly equal to the argument
   *
   * @see #equals(2,Geometry)
   * @see #hashCode()
   * @see #norm()
   * @see #normalize()
   */
  @Override
  public boolean equals(final Object other) {
    if (other instanceof Geometry) {
      final Geometry geometry = (Geometry)other;
      return equals(2, geometry);
    } else {
      return false;
    }
  }

  @Override
  public int getAxisCount() {
    if (this.geometryFactory == null) {
      return 2;
    } else {
      return this.geometryFactory.getAxisCount();
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public AbstractGeometryEditor<?> getParentEditor() {
    return this.parentEditor;
  }

  /**
   * Gets a hash code for the Geometry.
   *
   * @return an integer value suitable for use as a hashcode
   */

  @Override
  public int hashCode() {
    return getBoundingBox().hashCode();
  }

  @Override
  public boolean isModified() {
    return this.modified;
  }

  @Override
  public GeometryEditor<?> setAxisCount(final int axisCount) {
    final int oldAxisCount = this.geometryFactory.getAxisCount();
    if (oldAxisCount != axisCount) {
      this.modified = true;
      this.geometryFactory = this.geometryFactory.convertAxisCount(axisCount);
    }
    return this;
  }

  /**
   * Does not project
   * @param geometryFactory
   */
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      this.geometryFactory = GeometryFactory.DEFAULT_2D;
    } else {
      this.geometryFactory = geometryFactory;
    }
  }

  @Override
  public GeometryEditor<?> setM(final int[] vertexId, final double m) {
    return setCoordinate(vertexId, M, m);
  }

  protected void setModified(final boolean modified) {
    this.modified = modified;
    if (this.parentEditor != null) {
      this.parentEditor.setModified(modified);
    }
  }

  @Override
  public GeometryEditor<?> setVertex(final int[] vertexId, final Point newPoint) {
    final int axisCount = getAxisCount();
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double coordinate = newPoint.getCoordinate(axisIndex);
      setCoordinate(vertexId, axisIndex, coordinate);
    }
    return this;
  }

  @Override
  public GeometryEditor<?> setX(final int[] vertexId, final double x) {
    return setCoordinate(vertexId, X, x);
  }

  @Override
  public GeometryEditor<?> setY(final int[] vertexId, final double y) {
    return setCoordinate(vertexId, Y, y);
  }

  @Override
  public GeometryEditor<?> setZ(final int[] vertexId, final double z) {
    return setCoordinate(vertexId, Z, z);
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
