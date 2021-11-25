/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.model;

import java.io.FileNotFoundException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.WrappedException;
import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Doubles;
import org.jeometry.coordinatesystem.io.EsriCsWktWriter;
import org.jeometry.coordinatesystem.model.Area;
import org.jeometry.coordinatesystem.model.CompoundCoordinateSystem;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.CoordinateSystemType;
import org.jeometry.coordinatesystem.model.EpsgAuthority;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.HorizontalCoordinateSystem;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.VerticalCoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgId;
import org.jeometry.coordinatesystem.model.systems.EsriCoordinateSystems;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.collection.CollectionUtil;
import com.revolsys.collection.map.IntHashMap;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.graph.linemerge.LineMerger;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.model.impl.AbstractLineString;
import com.revolsys.geometry.model.impl.AbstractPoint;
import com.revolsys.geometry.model.impl.AbstractPolygon;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXYGeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxEmpty;
import com.revolsys.geometry.model.impl.GeometryCollectionImpl;
import com.revolsys.geometry.model.impl.LineStringDoubleGf;
import com.revolsys.geometry.model.impl.LinearRingDoubleGf;
import com.revolsys.geometry.model.impl.MultiLineStringImpl;
import com.revolsys.geometry.model.impl.MultiPointImpl;
import com.revolsys.geometry.model.impl.MultiPolygonImpl;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.impl.PointDoubleGf;
import com.revolsys.geometry.model.impl.PointDoubleXYGeometryFactory;
import com.revolsys.geometry.model.impl.PointDoubleXYZGeometryFactory;
import com.revolsys.geometry.model.impl.PolygonImpl;
import com.revolsys.geometry.model.impl.RectangleXY;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;
import com.revolsys.geometry.operation.union.CascadedPolygonUnion;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.io.StringWriter;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.record.io.format.wkt.WktParser;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

import tech.units.indriya.unit.Units;

/**
 * Supplies a set of utility methods for building Geometry objects from lists
 * of Coordinates.
 *
 * Note that the factory constructor methods do <b>not</b> change the input coordinates in any way.
 * In particular, they are not rounded to the supplied <tt>PrecisionModel</tt>.
 * It is assumed that input Point meet the given precision.
 *
 *
 * @version 1.7
 */
public abstract class GeometryFactory implements GeometryFactoryProxy, MapSerializer {
  private class EmptyGeometryCollection implements GeometryCollection {
    private static final long serialVersionUID = -5694727726395021467L;

    /**
     * Creates and returns a full copy of this  object.
     * (including all coordinates contained by it).
     *
     * @return a clone of this instance
     */
    @Override
    public GeometryCollection clone() {
      return this;
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
        return geometry.isEmpty();
      } else {
        return false;
      }
    }

    @Override
    public void forEachGeometry(final Consumer<Geometry> action) {
    }

    @Override
    public void forEachVertex(final Consumer<CoordinatesOperationPoint> action) {
    }

    @Override
    public int getAxisCount() {
      return GeometryFactory.this.axisCount;
    }

    @Override
    public GeometryDataType<?, ?> getDataType() {
      return GeometryDataTypes.GEOMETRY_COLLECTION;
    }

    @Override
    public Dimension getDimension() {
      return Dimension.FALSE;
    }

    @Override
    public <V extends Geometry> List<V> getGeometries() {
      return Collections.emptyList();
    }

    @Override
    public <V extends Geometry> V getGeometry(final int n) {
      return null;
    }

    @Override
    public int getGeometryCount() {
      return 0;
    }

    @Override
    public GeometryFactory getGeometryFactory() {
      return GeometryFactory.this;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public Geometry intersectionBbox(final BoundingBox boundingBox) {
      return this;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public boolean isHomogeneousGeometryCollection() {
      return false;
    }

    @Override
    public Geometry prepare() {
      return this;
    }

    @Override
    public String toString() {
      return toEwkt();
    }
  }

  private class EmptyLineString extends AbstractLineString {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int getAxisCount() {
      return GeometryFactory.this.axisCount;
    }

    @Override
    public double getCoordinate(final int vertexIndex, final int axisIndex) {
      return Double.NaN;
    }

    @Override
    public double[] getCoordinates() {
      return Doubles.EMPTY_ARRAY;
    }

    @Override
    public GeometryFactory getGeometryFactory() {
      return GeometryFactory.this;
    }

    @Override
    public int getVertexCount() {
      return 0;
    }
  }

  private class EmptyPoint extends AbstractPoint {
    private static final long serialVersionUID = 1L;

    @Override
    public Point clone() {
      return this;
    }

    @Override
    public void copyCoordinates(final double[] coordinates) {
      Arrays.fill(coordinates, java.lang.Double.NaN);
    }

    @Override
    public <R> R findVertex(final BiFunctionDouble<R> action) {
      return null;
    }

    @Override
    public void forEachGeometry(final Consumer<Geometry> action) {
    }

    @Override
    public void forEachVertex(final BiConsumerDouble action) {
    }

    @Override
    public void forEachVertex(final Consumer3Double action) {
    }

    @Override
    public int getAxisCount() {
      return GeometryFactory.this.axisCount;
    }

    @Override
    public double getCoordinate(final int axisIndex) {
      return java.lang.Double.NaN;
    }

    @Override
    public GeometryFactory getGeometryFactory() {
      return GeometryFactory.this;
    }

    @Override
    public double getM() {
      return java.lang.Double.NaN;
    }

    @Override
    public double getX() {
      return java.lang.Double.NaN;
    }

    @Override
    public double getY() {
      return java.lang.Double.NaN;
    }

    @Override
    public double getZ() {
      return java.lang.Double.NaN;
    }

    @Override
    public Point intersectionBbox(final BoundingBox boundingBox) {
      return this;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public String toString() {
      return toEwkt();
    }
  }

  private class EmptyPolygon extends AbstractPolygon {
    private static final long serialVersionUID = 1L;

    @Override
    public Polygon clone() {
      return this;
    }

    @Override
    public int getAxisCount() {
      return GeometryFactory.this.axisCount;
    }

    @Override
    public double getCoordinate(final int ringIndex, final int vertexIndex, final int axisIndex) {
      return Double.NaN;
    }

    @Override
    public double getCoordinate(final int partIndex, final int ringIndex, final int vertexIndex,
      final int axisIndex) {
      return Double.NaN;
    }

    @Override
    public GeometryFactory getGeometryFactory() {
      return GeometryFactory.this;
    }

    @Override
    public double getM(final int ringIndex, final int vertexIndex) {
      return java.lang.Double.NaN;
    }

    @Override
    public double getM(final int partIndex, final int ringIndex, final int vertexIndex) {
      return java.lang.Double.NaN;
    }

    @Override
    public double getX(final int ringIndex, final int vertexIndex) {
      return java.lang.Double.NaN;
    }

    @Override
    public double getX(final int partIndex, final int ringIndex, final int vertexIndex) {
      return java.lang.Double.NaN;
    }

    @Override
    public double getY(final int ringIndex, final int vertexIndex) {
      return java.lang.Double.NaN;
    }

    @Override
    public double getY(final int partIndex, final int ringIndex, final int vertexIndex) {
      return java.lang.Double.NaN;
    }

    @Override
    public double getZ(final int ringIndex, final int vertexIndex) {
      return java.lang.Double.NaN;
    }

    @Override
    public double getZ(final int partIndex, final int ringIndex, final int vertexIndex) {
      return java.lang.Double.NaN;
    }

    @Override
    public Polygon intersectionBbox(final BoundingBox boundingBox) {
      return this;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public String toString() {
      return toEwkt();
    }
  }

  private static final double[] SCALES_FLOATING_2 = new double[2];

  public static final double[] SCALES_FLOATING_3 = new double[3];

  private static final IntHashMap<GeometryFactories> INSTANCES_BY_COORDINATE_SYSTEM_ID = new IntHashMap<>();

  private static final HashMap<CoordinateSystem, GeometryFactories> INSTANCES_BY_COORDINATE_SYSTEM = new HashMap<>();

  public static final GeometryFactory DEFAULT_2D = floating(0, 2);

  /**
   * The default GeometryFactory with no coordinate system, 3D axis (x, y &amp; z) and a floating precision model.
   */
  public static final GeometryFactory DEFAULT_3D = floating(0, 3);

  public static void clear() {
    INSTANCES_BY_COORDINATE_SYSTEM_ID.clear();
  }

  public static GeometryFactory fixed(final CoordinateSystem coordinateSystem, final int axisCount,
    final double... scales) {
    final GeometryFactories instances = instances(coordinateSystem);
    return instances.fixed(axisCount, scales);
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, number of axis and a
   * fixed x, y &amp; fixed z precision models.
   * </p>
   *
   * @param coordinateSystemId The <a href="https://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @param axisCount The number of coordinate axis. 2 for 2D x &amp; y
   *          coordinates. 3 for 3D x, y &amp; z coordinates.
   * @param scaleXY The scale factor used to round the x, y coordinates. The
   *          precision is 1 / scaleXy. A scale factor of 1000 will give a
   *          precision of 1 / 1000 = 1mm for projected coordinate systems using
   *          metres.
   * @param scaleZ The scale factor used to round the z coordinates. The
   *          precision is 1 / scaleZ. A scale factor of 1000 will give a
   *          precision of 1 / 1000 = 1mm for projected coordinate systems using
   *          metres.
   * @return The geometry factory.
   */
  public static GeometryFactory fixed(final int coordinateSystemId, final int axisCount,
    final double... scales) {
    final GeometryFactories instances = instances(coordinateSystemId);
    return instances.fixed(axisCount, scales);
  }

  public static GeometryFactory fixed(final String wkt, final int axisCount,
    final double... scales) {
    final CoordinateSystem coordinateSystem = EsriCoordinateSystems.readCoordinateSystem(wkt);
    final GeometryFactories instances = instances(coordinateSystem);
    return instances.fixed(axisCount, scales);
  }

  public static GeometryFactory fixed2d(final double scaleX, final double scaleY) {
    return fixed(0, 2, scaleX, scaleY);
  }

  public static GeometryFactory fixed2d(final int coordinateSystemId, final double scaleX,
    final double scaleY) {
    return fixed(coordinateSystemId, 2, scaleX, scaleY);
  }

  public static GeometryFactory fixed2d(final String wkt, final double scaleX,
    final double scaleY) {
    return fixed(wkt, 2, scaleX, scaleY);
  }

  public static GeometryFactory fixed3d(final double scaleX, final double scaleY,
    final double scaleZ) {
    return fixed(0, 3, scaleX, scaleY, scaleZ);
  }

  public static GeometryFactory fixed3d(final int coordinateSystemId, final double scaleX,
    final double scaleY, final double scaleZ) {
    return fixed(coordinateSystemId, 3, scaleX, scaleY, scaleZ);
  }

  public static GeometryFactory fixed3d(final String wkt, final double scaleX, final double scaleY,
    final double scaleZ) {
    return fixed(wkt, 3, scaleX, scaleY, scaleZ);
  }

  public static GeometryFactory floating(final CoordinateSystem coordinateSystem,
    final int axisCount) {
    final GeometryFactories instances = instances(coordinateSystem);
    return instances.floating(axisCount);
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, number of axis and a
   * floating precision model.
   * </p>
   *
   * @param coordinateSystemId The <a href="https://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @param axisCount The number of coordinate axis. 2 for 2D x &amp; y
   *          coordinates. 3 for 3D x, y &amp; z coordinates.
   * @return The geometry factory.
   */
  public static GeometryFactory floating(final int coordinateSystemId, final int axisCount) {
    final GeometryFactories instances = instances(coordinateSystemId);
    return instances.floating(axisCount);
  }

  public static GeometryFactory floating(final Resource resource, final int axisCount) {
    if (resource == null) {
      return DEFAULT_2D;
    } else {
      final Resource projResource = resource.newResourceChangeExtension("prj");
      if (projResource != null) {
        try {
          final String wkt = projResource.contentsAsString();
          final CoordinateSystem coordinateSystem = EsriCoordinateSystems.readCoordinateSystem(wkt);
          final GeometryFactories instances = instances(coordinateSystem);
          return instances.floating(axisCount);
        } catch (final WrappedException e) {
          final Throwable cause = e.getCause();
          if (cause instanceof FileNotFoundException) {
          } else if (cause instanceof FileSystemException) {
          } else {
            Logs.error(GeometryFactory.class, "Unable to load projection from " + projResource, e);
          }
        } catch (final Exception e) {
          Logs.error(GeometryFactory.class, "Unable to load projection from " + projResource, e);
        }
      }
      return null;
    }
  }

  public static GeometryFactory floating(final String wkt, final int axisCount) {
    final CoordinateSystem coordinateSystem = EsriCoordinateSystems.readCoordinateSystem(wkt);
    if (coordinateSystem == null) {
      return null;
    } else {
      final GeometryFactories instances = instances(coordinateSystem);
      return instances.floating(axisCount);
    }
  }

  public static GeometryFactory floating2d(final CoordinateSystem coordinateSystem) {
    final GeometryFactories instances = instances(coordinateSystem);
    return instances.floating(2);
  }

  public static GeometryFactory floating2d(final int coordinateSystemId) {
    return floating(coordinateSystemId, 2);
  }

  public static GeometryFactory floating2d(final Resource resource) {
    return floating(resource, 2);
  }

  public static GeometryFactory floating2d(final String wkt) {
    return floating(wkt, 2);
  }

  public static GeometryFactory floating3d(final CoordinateSystem coordinateSystem) {
    return floating(coordinateSystem, 3);
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, 3D axis (x, y &amp; z)
   * and a floating precision models.
   * </p>
   *
   * @param coordinateSystemId The <a href="https://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @return The geometry factory.
   */
  public static GeometryFactory floating3d(final int coordinateSystemId) {
    return floating(coordinateSystemId, 3);
  }

  public static GeometryFactory floating3d(final Resource resource) {
    return floating(resource, 3);
  }

  public static GeometryFactory floating3d(Resource resource, final GeometryFactory defaultValue) {
    final String filenameExtension = resource.getFileNameExtension();
    if (filenameExtension.equals("gz") || filenameExtension.equals("zip")) {
      final String baseName = resource.getBaseName();
      final Resource parentResource = resource.getParent();
      resource = parentResource.newChildResource(baseName);
    }
    final GeometryFactory geometryFactory = floating3d(resource);
    if (geometryFactory == null) {
      return defaultValue;
    } else {
      return geometryFactory;
    }
  }

  public static GeometryFactory floating3d(final String wkt) {
    return floating(wkt, 3);
  }

  public static GeometryFactory floating3d(final ZipFile zipFile, final ZipEntry zipEntry,
    final GeometryFactory defaultValue) {
    final String entryName = zipEntry.getName();
    final String prjFileName = entryName.replaceAll(".las$", ".prj");
    final ZipEntry prjZipEntry = zipFile.getEntry(prjFileName);
    if (prjZipEntry != null) {
      final GeometryFactory geometryFactoryFromPrj = floating3d(
        Resource.newResource(zipFile, prjZipEntry));

      if (geometryFactoryFromPrj != null) {
        return geometryFactoryFromPrj;
      }
    }
    return defaultValue;
  }

  public static GeometryFactory get(final Object factory) {
    if (factory instanceof GeometryFactory) {
      return (GeometryFactory)factory;
    } else if (factory instanceof Map) {
      @SuppressWarnings("unchecked")
      final Map<String, Object> properties = (Map<String, Object>)factory;
      return newGeometryFactory(properties);
    } else {
      return null;
    }
  }

  public static String getAxisName(final int axisIndex) {
    switch (axisIndex) {
      case 0:
        return "X";
      case 1:
        return "Y";
      case 2:
        return "Z";
      case 3:
        return "M";
      default:
        return String.valueOf(axisIndex);
    }
  }

  private static Set<DataType> getGeometryDataTypes(
    final Collection<? extends Geometry> geometries) {
    final Set<DataType> dataTypes = new LinkedHashSet<>();
    for (final Geometry geometry : geometries) {
      final DataType dataType = geometry.getDataType();
      dataTypes.add(dataType);
    }
    return dataTypes;
  }

  protected static GeometryFactories instances(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return instances(0);
    } else {
      final int coordinateSystemId = coordinateSystem.getCoordinateSystemId();
      if (coordinateSystemId > 0) {
        if (coordinateSystem.getAuthority() instanceof EpsgAuthority) {
          return instances(coordinateSystemId);
        }
      }
      synchronized (INSTANCES_BY_COORDINATE_SYSTEM) {
        GeometryFactories instances = INSTANCES_BY_COORDINATE_SYSTEM.get(coordinateSystem);
        if (instances == null) {
          instances = new GeometryFactories(coordinateSystem);
          INSTANCES_BY_COORDINATE_SYSTEM.put(coordinateSystem, instances);
        }
        return instances;
      }
    }
  }

  protected static GeometryFactories instances(int coordinateSystemId) {
    if (coordinateSystemId < 0) {
      coordinateSystemId = 0;
    }
    synchronized (INSTANCES_BY_COORDINATE_SYSTEM_ID) {
      GeometryFactories instances = INSTANCES_BY_COORDINATE_SYSTEM_ID.get(coordinateSystemId);
      if (instances == null) {
        instances = new GeometryFactories(coordinateSystemId);
        INSTANCES_BY_COORDINATE_SYSTEM_ID.put(coordinateSystemId, instances);
      }
      return instances;
    }
  }

  public static GeometryFactory nad83() {
    return floating3d(EpsgId.NAD83);
  }

  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G newGeometry(final List<? extends Geometry> geometries) {
    if (geometries == null || geometries.size() == 0) {
      return (G)GeometryFactory.DEFAULT_3D.geometry();
    } else {
      final GeometryFactory geometryFactory = geometries.get(0).getGeometryFactory();
      return geometryFactory.geometry(geometries);
    }
  }

  public static GeometryFactory newGeometryFactory(final Map<String, ? extends Object> properties) {
    final int coordinateSystemId = Maps.getInteger(properties, "srid", 0);
    final int axisCount = Maps.getInteger(properties, "axisCount", 2);
    final double scaleXY = Maps.getDouble(properties, "scaleXy", 0.0);
    final double scaleX = Maps.getDouble(properties, "scaleX", scaleXY);
    final double scaleY = Maps.getDouble(properties, "scaleY", scaleXY);
    final double scaleZ = Maps.getDouble(properties, "scaleZ", 0.0);
    return GeometryFactory.fixed(coordinateSystemId, axisCount, scaleX, scaleY, scaleZ);
  }

  public static GeometryFactory newGeometryFactory(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof GeometryFactory) {
      return (GeometryFactory)value;
    } else if (value instanceof Map) {
      @SuppressWarnings("unchecked")
      final Map<String, ? extends Object> properties = (Map<String, ? extends Object>)value;
      return newGeometryFactory(properties);
    } else {
      throw new RuntimeException("Cannot convert " + value + " to GeometryFactory");
    }
  }

  public static double[] newScalesFixed(final int axisCount, final double scale) {
    final double[] scales = new double[Math.max(axisCount, 2)];
    Arrays.fill(scales, scale);
    return scales;
  }

  public static double[] newScalesFloating(final int axisCount) {
    if (axisCount < 3) {
      return SCALES_FLOATING_2;
    } else if (axisCount == 3) {
      return SCALES_FLOATING_3;
    } else {
      return new double[axisCount];
    }
  }

  public static GeometryFactory newWithOffsets(final CoordinateSystem coordinateSystem,
    final double offsetX, final double scaleX, final double offsetY, final double scaleY,
    final double offsetZ, final double scaleZ) {
    final GeometryFactories instances = instances(coordinateSystem);
    return instances.fixedWithOffsets(offsetX, scaleX, offsetY, scaleY, offsetZ, scaleZ);
  }

  public static GeometryFactory offsetScaled3d(final int coordinateSystemId, final double offsetX,
    final double scaleX, final double offsetY, final double scaleY, final double offsetZ,
    final double scaleZ) {
    if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
      return fixed3d(coordinateSystemId, scaleX, scaleY, scaleZ);
    } else {
      final GeometryFactories instances = instances(coordinateSystemId);
      return new GeometryFactoryWithOffsets(instances, offsetX, scaleX, offsetY, scaleY, offsetZ,
        scaleZ);
    }
  }

  public static GeometryFactory readOffsetScaled3d(final DataReader reader) {
    final int coordinateSystemId = reader.getInt();
    final double offsetX = reader.getDouble();
    final double scaleX = reader.getDouble();
    final double offsetY = reader.getDouble();
    final double scaleY = reader.getDouble();
    final double offsetZ = reader.getDouble();
    final double scaleZ = reader.getDouble();
    return offsetScaled3d(coordinateSystemId, offsetX, scaleX, offsetY, scaleY, offsetZ, scaleZ);
  }

  public static double toResolution(final double scale) {
    if (scale > 0) {
      return 1 / scale;
    } else {
      return 0;
    }
  }

  public static GeometryFactory wgs84() {
    return floating3d(EpsgId.WGS84);
  }

  public static GeometryFactory worldMercator() {
    return floating3d(3857);
  }

  protected final int axisCount;

  private final BoundingBox boundingBoxEmpty = new BoundingBoxEmpty(this);

  protected final CoordinateSystem coordinateSystem;

  private HorizontalCoordinateSystem horizontalCoordinateSystem;

  protected final int coordinateSystemId;

  private final EmptyLineString emptyLine = new EmptyLineString();

  private final EmptyPoint emptyPoint = new EmptyPoint();

  private final EmptyGeometryCollection emptyGeometryCollection = new EmptyGeometryCollection();

  private final EmptyPolygon emptyPolygon = new EmptyPolygon();

  private transient final WktParser parser = new WktParser(this);

  private BoundingBox areaBoundingBox = this.boundingBoxEmpty;

  protected GeometryFactories instances;

  private final CoordinateSystemType coordinateSystemType;

  private final CoordinateSystemType horizontalCoordinateSystemType;

  private final CoordinateSystemType verticalCoordinateSystemType;

  private VerticalCoordinateSystem verticalCoordinateSystem;

  protected GeometryFactory(final GeometryFactories instances, final int axisCount) {
    if (axisCount < 2) {
      this.axisCount = 2;
    } else {
      this.axisCount = axisCount;
    }
    this.instances = instances;
    this.coordinateSystemId = instances.getCoordinateSystemId();
    this.coordinateSystem = instances.getCoordinateSystem();

    if (this.coordinateSystem == null) {
      this.horizontalCoordinateSystem = null;
      this.verticalCoordinateSystem = null;
    } else {
      this.horizontalCoordinateSystem = this.coordinateSystem.getHorizontalCoordinateSystem();
      if (this.coordinateSystem instanceof CompoundCoordinateSystem) {
        final CompoundCoordinateSystem compoundCoordinateSystem = (CompoundCoordinateSystem)this.coordinateSystem;
        this.verticalCoordinateSystem = compoundCoordinateSystem.getVerticalCoordinateSystem();
      }
    }
    if (this.coordinateSystem == null) {
      this.coordinateSystemType = CoordinateSystemType.NONE;
    } else {
      this.coordinateSystemType = this.coordinateSystem.getCoordinateSystemType();
    }
    if (this.horizontalCoordinateSystem == null) {
      this.horizontalCoordinateSystemType = CoordinateSystemType.NONE;
      this.areaBoundingBox = this.boundingBoxEmpty;
    } else {
      this.horizontalCoordinateSystemType = this.horizontalCoordinateSystem
        .getCoordinateSystemType();
      double minX;
      double minY;
      double maxX;
      double maxY;

      final Area area = this.horizontalCoordinateSystem.getArea();
      if (area == null) {
        minX = -180;
        minY = -90;
        maxX = 180;
        maxY = 90;
      } else {
        minX = area.getMinX();
        minY = area.getMinY();
        maxX = area.getMaxX();
        maxY = area.getMaxY();
      }
      this.areaBoundingBox = getGeographicGeometryFactory() //
        .bboxEditor() //
        .addBbox(minX, minY, maxX, maxY) //
        .setGeometryFactory(this) //
        .newBoundingBox();
    }
    if (this.verticalCoordinateSystem == null) {
      this.verticalCoordinateSystemType = CoordinateSystemType.NONE;
    } else {
      this.verticalCoordinateSystemType = this.verticalCoordinateSystem.getCoordinateSystemType();
    }
  }

  public void addGeometries(final List<Geometry> geometryList, final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      for (final Geometry part : geometry.geometries()) {
        if (part != null && !part.isEmpty()) {
          geometryList.add(part.newGeometry(this));
        }
      }
    }
  }

  public void addInverseOperations(final List<CoordinatesOperation> operations) {
    if (isProjected()) {
      final ProjectedCoordinateSystem projCs = getHorizontalCoordinateSystem();
      projCs.addInverseOperations(operations);
    }
  }

  public void addProjectionOperations(final List<CoordinatesOperation> operations) {
    if (isProjected()) {
      final ProjectedCoordinateSystem projCs = getHorizontalCoordinateSystem();
      projCs.addProjectionOperations(operations);
    }
  }

  public BoundingBoxEditor bboxEditor() {
    return new BoundingBoxEditor(this);
  }

  public BoundingBox bboxEmpty() {
    return this.boundingBoxEmpty;
  }

  /**
   *  Build an appropriate <code>Geometry</code>, <code>MultiGeometry</code>, or
   *  <code>GeometryCollection</code> to contain the <code>Geometry</code>s in
   *  it.
   * For example:<br>
   *
   *  <ul>
   *    <li> If <code>geomList</code> contains a single <code>Polygon</code>,
   *    the <code>Polygon</code> is returned.
   *    <li> If <code>geomList</code> contains several <code>Polygon</code>s, a
   *    <code>MultiPolygon</code> is returned.
   *    <li> If <code>geomList</code> contains some <code>Polygon</code>s and
   *    some <code>LineString</code>s, a <code>GeometryCollection</code> is
   *    returned.
   *    <li> If <code>geomList</code> is empty, an empty <code>GeometryCollection</code>
   *    is returned
   *  </ul>
   *
   * Note that this method does not "flatten" Geometries in the input, and hence if
   * any MultiGeometries are contained in the input a GeometryCollection containing
   * them will be returned.
   *
   *@param  geometries  the <code>Geometry</code>s to combine
   *@return           a <code>Geometry</code> of the "smallest", "most
   *      type-specific" class that can contain the elements of <code>geomList</code>
   *      .
   */
  public Geometry buildGeometry(final Iterable<? extends Geometry> geometries) {
    DataType collectionDataType = null;
    boolean isHeterogeneous = false;
    boolean hasGeometryCollection = false;
    final List<Geometry> geometryList = new ArrayList<>();
    for (final Geometry geometry : geometries) {
      if (geometry != null) {
        geometryList.add(geometry);
        DataType geometryDataType = geometry.getDataType();
        if (geometry instanceof LinearRing) {
          geometryDataType = GeometryDataTypes.LINE_STRING;
        }
        if (collectionDataType == null) {
          collectionDataType = geometryDataType;
        } else if (geometryDataType != collectionDataType) {
          isHeterogeneous = true;
        }
        if (geometry.isGeometryCollection()) {
          hasGeometryCollection = true;
        }
      }
    }

    /**
     * Now construct an appropriate geometry to return
     */
    if (collectionDataType == null) {
      return geometryCollection();
    } else if (isHeterogeneous || hasGeometryCollection) {
      return geometryCollection(geometryList);
    } else if (geometryList.size() == 1) {
      return geometryList.iterator().next();
    } else if (GeometryDataTypes.POINT.equals(collectionDataType)) {
      return punctual(geometryList);
    } else if (GeometryDataTypes.LINE_STRING.equals(collectionDataType)) {
      return lineal(geometryList);
    } else if (GeometryDataTypes.POLYGON.equals(collectionDataType)) {
      return polygonal(geometryList);
    } else {
      throw new IllegalArgumentException("Unknown geometry type " + collectionDataType);
    }
  }

  @Override
  public GeometryFactory clone() {
    return this;
  }

  public abstract GeometryFactory convertAxisCount(final int axisCount);

  public GeometryFactory convertAxisCountAndScales(final int axisCount, final double... scales) {
    return this.instances.fixed(axisCount, scales);
  }

  public abstract GeometryFactory convertCoordinateSystem(final CoordinateSystem coordinateSystem);

  public GeometryFactory convertCoordinateSystem(final GeometryFactoryProxy geometryFactory) {
    if (geometryFactory != null && geometryFactory != this) {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem == null) {
        final int coordinateSystemId = geometryFactory.getCoordinateSystemId();
        return convertSrid(coordinateSystemId);
      } else {
        return convertCoordinateSystem(coordinateSystem);
      }
    }
    return this;
  }

  public GeometryFactory convertScales(final double... scales) {
    return this.instances.fixed(this.axisCount, scales);
  }

  public GeometryFactory convertSrid(final int coordinateSystemId) {
    if (coordinateSystemId == getCoordinateSystemId()) {
      return this;
    } else {
      return GeometryFactory.floating(coordinateSystemId, this.axisCount);
    }
  }

  public abstract GeometryFactory convertToFixed(double defaultScale);

  public double[] copyPrecise(final double[] values) {
    final double[] valuesPrecise = new double[values.length];
    makePrecise(values, valuesPrecise);
    return valuesPrecise;
  }

  public boolean equalsScales(final double[] scales) {
    for (final double scale2 : scales) {
      if (0 != scale2) {
        return false;
      }
    }
    return true;
  }

  public Geometry geometry() {
    return this.emptyPoint;
  }

  /**
   * <p>
   * Construct a new new geometry of the requested target geometry class.
   * <p>
   *
   * @param targetClass
   * @param geometry
   * @return
   */
  @SuppressWarnings({
    "unchecked"
  })
  public <V extends Geometry> V geometry(final Class<?> targetClass, Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      geometry = geometry.newGeometry(this);
      if (geometry.isGeometryCollection()) {
        if (geometry.getGeometryCount() == 1) {
          geometry = geometry.getGeometry(0);
        } else {
          geometry = geometry.union();
          // Union doesn't use this geometry factory
          geometry = geometry.newGeometry(this);
        }
      }
      final Class<?> geometryClass = geometry.getClass();
      if (targetClass.isAssignableFrom(geometryClass)) {
        // TODO if geometry collection then clean up
        return (V)geometry;
      } else if (Point.class.isAssignableFrom(targetClass)) {
        if (geometry.getGeometryCount() == 1) {
          final Geometry part = geometry.getGeometry(0);
          if (part instanceof Point) {
            return (V)part;
          }
        }
      } else if (LineString.class.isAssignableFrom(targetClass)) {
        if (geometry.getGeometryCount() == 1) {
          final Geometry part = geometry.getGeometry(0);
          if (part instanceof LineString) {
            return (V)part;
          }
        } else {
          final List<LineString> mergedLineStrings = LineMerger.merge(geometry);
          if (mergedLineStrings.size() == 1) {
            return (V)mergedLineStrings.get(0);
          }
        }
      } else if (Polygon.class.isAssignableFrom(targetClass)) {
        if (geometry.getGeometryCount() == 1) {
          final Geometry part = geometry.getGeometry(0);
          if (part instanceof Polygon) {
            return (V)part;
          }
        }
      } else if (Punctual.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof Punctual) {
          return (V)geometry;
        }
      } else if (Lineal.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof Lineal) {
          return (V)geometry;
        }
      } else if (Polygonal.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof Polygonal) {
          return (V)polygonal(geometry);
        }
      }
    }
    return null;
  }

  /**
   * Construct a new new geometry by flattening the input geometries, ignoring and null or empty
   * geometries. If there are no geometries, then an empty {@link Geometry} will be returned.
   * If there is one geometry that single geometry will be returned. Otherwise the result
   * will be a subclass of {@link GeometryCollection}.
   *
   *
   * @param geometries
   * @return
   */
  @SuppressWarnings("unchecked")
  public <V extends Geometry> V geometry(final Collection<? extends Geometry> geometries) {
    final List<Geometry> geometryList = getGeometries(geometries);
    if (geometryList == null || geometryList.size() == 0) {
      return (V)geometryCollection();
    } else if (geometryList.size() == 1) {
      return (V)geometryList.get(0);
    } else {
      final Set<DataType> dataTypes = getGeometryDataTypes(geometryList);
      if (dataTypes.size() == 1) {
        final DataType dataType = CollectionUtil.get(dataTypes, 0);
        if (dataType.equals(GeometryDataTypes.POINT)) {
          return (V)punctual(geometryList);
        } else if (dataType.equals(GeometryDataTypes.LINE_STRING)) {
          return (V)lineal(geometryList);
        } else if (dataType.equals(GeometryDataTypes.POLYGON)) {
          return (V)polygonal(geometryList);
        }
      }
      return (V)geometryCollection(geometries);
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends Geometry> V geometry(final Geometry... geometries) {
    return (V)geometry(Arrays.asList(geometries));
  }

  /**
   * Creates a deep copy of the input {@link Geometry}.
   * <p>
   * This is a convenient way to change the <tt>LineString</tt>
   * used to represent a geometry, or to change the
   * factory used for a geometry.
   * <p>
   * {@link Geometry#clone()} can also be used to make a deep copy,
   * but it does not allow changing the LineString type.
   *
   * @return a deep copy of the input geometry, using the LineString type of this factory
   *
   * @see Geometry#clone()
   */
  public Geometry geometry(final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else {
      final int coordinateSystemId = getCoordinateSystemId();
      final int geometrySrid = geometry.getCoordinateSystemId();
      if (coordinateSystemId == 0 && geometrySrid != 0) {
        final GeometryFactory geometryFactory = GeometryFactory.floating(geometrySrid,
          this.axisCount);
        return geometryFactory.geometry(geometry);
      } else if (coordinateSystemId != 0 && geometrySrid != 0
        && geometrySrid != coordinateSystemId) {
        if (geometry instanceof Point) {
          return geometry.newGeometry(this);
        } else if (geometry instanceof LineString) {
          return geometry.newGeometry(this);
        } else if (geometry instanceof Polygon) {
          return geometry.newGeometry(this);
        } else if (geometry instanceof Punctual) {
          final List<Geometry> geometries = new ArrayList<>();
          addGeometries(geometries, geometry);
          return punctual(geometries);
        } else if (geometry instanceof Lineal) {
          final List<Geometry> geometries = new ArrayList<>();
          addGeometries(geometries, geometry);
          return lineal(geometries);
        } else if (geometry instanceof Polygonal) {
          final List<Geometry> geometries = new ArrayList<>();
          addGeometries(geometries, geometry);
          return polygonal(geometries);
        } else if (geometry.isGeometryCollection()) {
          final List<Geometry> geometries = new ArrayList<>();
          addGeometries(geometries, geometry);
          return geometryCollection(geometries);
        } else {
          return geometry.newGeometry(this);
        }
      } else if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        return point.newGeometry(this);
      } else if (geometry instanceof LinearRing) {
        final LinearRing linearRing = (LinearRing)geometry;
        return linearRing.newGeometry(this);
      } else if (geometry instanceof LineString) {
        final LineString lineString = (LineString)geometry;
        return lineString.newGeometry(this);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        return polygon(polygon);
      } else if (geometry instanceof Punctual) {
        final List<Geometry> geometries = new ArrayList<>();
        addGeometries(geometries, geometry);
        return punctual(geometries);
      } else if (geometry instanceof Lineal) {
        final List<Geometry> geometries = new ArrayList<>();
        addGeometries(geometries, geometry);
        return lineal(geometries);
      } else if (geometry instanceof Polygonal) {
        final List<Geometry> geometries = new ArrayList<>();
        addGeometries(geometries, geometry);
        return polygonal(geometries);
      } else if (geometry instanceof GeometryCollection) {
        final List<Geometry> geometries = new ArrayList<>();
        addGeometries(geometries, geometry);
        return geometryCollection(geometries);
      } else {
        return null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T geometry(final String wkt) {
    if (Property.hasValue(wkt)) {
      return (T)this.parser.parseGeometry(wkt);
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T geometry(final String wkt,
    final boolean useAxisCountFromGeometryFactory) {
    return (T)this.parser.parseGeometry(wkt, useAxisCountFromGeometryFactory);
  }

  public Geometry geometryCollection() {
    return this.emptyGeometryCollection;
  }

  /**
   * Does not flattern nested geometry collections.
   *
   * @param geometries
   * @return
   */
  @SuppressWarnings("unchecked")
  public <G extends Geometry> G geometryCollection(final Iterable<? extends Geometry> geometries) {
    if (geometries == null) {
      return (G)geometryCollection();
    } else {
      DataType dataType = null;
      boolean heterogeneous = false;
      final List<Geometry> geometryList = new ArrayList<>();
      if (geometries != null) {
        for (final Geometry geometry : geometries) {
          if (geometry != null) {
            if (heterogeneous) {
            } else {
              final DataType geometryDataType = geometry.getDataType();
              if (dataType == null) {
                dataType = geometryDataType;
              } else if (dataType != geometryDataType) {
                heterogeneous = true;
                dataType = null;
              }
            }

            final Geometry copy = geometry.newGeometry(this);
            geometryList.add(copy);
          }
        }
      }
      if (geometryList.size() == 0) {
        return (G)geometryCollection();
      } else if (geometryList.size() == 1) {
        return (G)geometryList.get(0);
      } else if (dataType == GeometryDataTypes.POINT) {
        return (G)punctual(geometryList);
      } else if (dataType == GeometryDataTypes.LINE_STRING) {
        return (G)lineal(geometryList);
      } else if (dataType == GeometryDataTypes.POLYGON) {
        return (G)polygonal(geometryList);
      } else {
        final Geometry[] geometryArray = new Geometry[geometryList.size()];
        geometryList.toArray(geometryArray);
        return (G)new GeometryCollectionImpl(this, geometryArray);
      }
    }
  }

  @Override
  public BoundingBox getAreaBoundingBox() {
    return this.areaBoundingBox;
  }

  @Override
  public final int getAxisCount() {
    return this.axisCount;
  }

  public Point getCoordinates(final Point point) {
    final Point convertedPoint = project(point);
    return convertedPoint;
  }

  /**
   * <p>Get the {@link CoordinatesOperation} to convert between this factory's and the other factory's
   * {@link CoordinateSystem}.</p>
   *
   * @param geometryFactory The geometry factory to convert to.
   * @return The coordinates operation or null if no conversion is available.
   */
  public CoordinatesOperation getCoordinatesOperation(final GeometryFactory geometryFactory) {
    if (geometryFactory != this && geometryFactory != null) {
      final CoordinateSystem coordinateSystemThis = this.horizontalCoordinateSystem;
      if (coordinateSystemThis != null) {
        final CoordinateSystem coordinateSystemOther = geometryFactory
          .getHorizontalCoordinateSystem();
        if (coordinateSystemThis == coordinateSystemOther || coordinateSystemOther == null) {
          return null;
        } else {
          return coordinateSystemThis.getCoordinatesOperation(coordinateSystemOther);
        }
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends CoordinateSystem> C getCoordinateSystem() {
    return (C)this.coordinateSystem;
  }

  @Override
  public int getCoordinateSystemId() {
    return this.coordinateSystemId;
  }

  public GeometryFactory getGeographicGeometryFactory() {
    if (isGeographic()) {
      return this;
    } else if (isProjected()) {
      final ProjectedCoordinateSystem projectedCs = (ProjectedCoordinateSystem)this.horizontalCoordinateSystem;
      final GeographicCoordinateSystem geographicCs = projectedCs.getGeographicCoordinateSystem();
      final int coordinateSystemId = geographicCs.getCoordinateSystemId();
      return floating(coordinateSystemId, this.axisCount);
    } else {
      return floating(EpsgId.WGS84, this.axisCount);
    }
  }

  public List<Geometry> getGeometries(final Collection<? extends Geometry> geometries) {
    if (geometries == null) {
      return Collections.emptyList();
    } else {
      final List<Geometry> geometryList = new ArrayList<>();
      for (final Geometry geometry : geometries) {
        addGeometries(geometryList, geometry);
      }
      return geometryList;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C extends CoordinateSystem> C getHorizontalCoordinateSystem() {
    return (C)this.horizontalCoordinateSystem;
  }

  public GeometryFactory getHorizontalGeometryFactory() {
    if (isHasHorizontalCoordinateSystem()) {
      return this;
    } else if (isHasHorizontalCoordinateSystem()) {
      final int horizontalCoordinateSystemId = getHorizontalCoordinateSystemId();
      if (horizontalCoordinateSystemId > 0) {
        return convertSrid(horizontalCoordinateSystemId);
      } else {
        return convertCoordinateSystem(this.horizontalCoordinateSystem);
      }
    } else {
      return null;
    }
  }

  public Unit<Length> getHorizontalLengthUnit() {
    if (this.horizontalCoordinateSystem == null) {
      return Units.METRE;
    } else {
      return this.horizontalCoordinateSystem.getLengthUnit();
    }
  }

  private LinearRing getLinearRing(final Object ring) {
    if (ring instanceof LinearRing) {
      return (LinearRing)ring;
    } else if (ring instanceof LineString) {
      final LineString points = (LineString)ring;
      return linearRing(points);
    } else if (ring instanceof LineString) {
      final LineString line = (LineString)ring;
      final LineString points = line;
      return linearRing(points);
    } else if (ring instanceof double[]) {
      final double[] coordinates = (double[])ring;
      return linearRing(this.axisCount, coordinates);
    } else {
      return null;
    }
  }

  /**
   * Returns the maximum number of significant digits provided by this
   * precision model.
   * Intended for use by routines which need to print out
   * decimal representations of precise values .
   * <p>
   * This method would be more correctly called
   * <tt>getMinimumDecimalPlaces</tt>,
   * since it actually computes the number of decimal places
   * that is required to correctly display the full
   * precision of an ordinate value.
   * <p>
   * Since it is difficult to compute the required number of
   * decimal places for scale factors which are not powers of 10,
   * the algorithm uses a very rough approximation in this case.
   * This has the side effect that for scale factors which are
   * powers of 10 the value returned is 1 greater than the true value.
   *
   *
   * @return the maximum number of decimal places provided by this precision model
   */
  public int getMaximumSignificantDigits() {
    return 16;
  }

  public double getOffset(final int axisIndex) {
    return 0;
  }

  public double getOffsetX() {
    return 0;
  }

  public double getOffsetY() {
    return 0;
  }

  public double getOffsetZ() {
    return 0;
  }

  public Point[] getPointArray(final Iterable<?> pointsList) {
    final List<Point> points = new ArrayList<>();
    for (final Object object : pointsList) {
      final Point point = point(object);
      if (point != null && !point.isEmpty()) {
        points.add(point);
      }
    }
    return points.toArray(new Point[points.size()]);
  }

  @SuppressWarnings("unchecked")
  public Polygon[] getPolygonArray(final Iterable<?> polygonList) {
    final List<Polygon> polygons = new ArrayList<>();
    for (final Object value : polygonList) {
      Polygon polygon;
      if (value instanceof Polygon) {
        polygon = (Polygon)value;
      } else if (value instanceof List) {
        final List<LineString> coordinateList = (List<LineString>)value;
        polygon = polygon(coordinateList);
      } else if (value instanceof LineString) {
        final LineString coordinateList = (LineString)value;
        polygon = polygon(coordinateList);
      } else {
        polygon = null;
      }
      if (polygon != null) {
        polygons.add(polygon);
      }
    }
    return polygons.toArray(new Polygon[polygons.size()]);
  }

  public Point[] getPrecise(final Point... points) {
    final Point[] precisesPoints = new Point[points.length];
    for (int i = 0; i < points.length; i++) {
      final Point point = points[i];
      precisesPoints[i] = getPreciseCoordinates(point);
    }
    return precisesPoints;
  }

  public Point getPreciseCoordinates(final Point point) {
    final double[] coordinates = point.getCoordinates();
    makePrecise(coordinates.length, coordinates);
    return new PointDouble(coordinates);
  }

  public double getResolution(final int axisIndex) {
    return 0;
  }

  public double getResolutionX() {
    return 0;
  }

  public double getResolutionXy() {
    return 0;
  }

  public double getResolutionY() {
    return 0;
  }

  public double getResolutionZ() {
    return 0;
  }

  public double getScale(final int axisIndex) {
    return 0;
  }

  public double[] getScales() {
    final int axisCount = getAxisCount();
    final double[] scales = new double[axisCount];
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double scale = getScale(axisIndex);
      scales[axisIndex] = scale;
    }
    return scales;
  }

  public double getScaleX() {
    return 0;
  }

  public double getScaleXY() {
    return 0;
  }

  public double getScaleY() {
    return 0;
  }

  public double getScaleZ() {
    return 0;
  }

  public VerticalCoordinateSystem getVerticalCoordinateSystem() {
    return this.verticalCoordinateSystem;
  }

  @Override
  public int hashCode() {
    return this.coordinateSystemId;
  }

  public boolean hasM() {
    return this.axisCount > 3;
  }

  public boolean hasSameCoordinateSystem(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return false;
    } else {
      final int coordinateSystemId1 = getCoordinateSystemId();
      final int coordinateSystemId2 = geometryFactory.getCoordinateSystemId();
      if (coordinateSystemId1 == coordinateSystemId2) {
        if (coordinateSystemId1 >= 0) {
          return true;
        }
      }
      final CoordinateSystem coordinateSystem1 = getHorizontalCoordinateSystem();
      final CoordinateSystem coordinateSystem2 = geometryFactory.getHorizontalCoordinateSystem();
      if (coordinateSystem1 == coordinateSystem2) {
        return true;
      } else if (coordinateSystem1 == null || coordinateSystem2 == null) {
        return false;
      } else if (coordinateSystem1.equals(coordinateSystem2)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public boolean hasZ() {
    return this.axisCount > 2;
  }

  public boolean isCompound() {
    return this.coordinateSystemType.isCompound();
  }

  public boolean isFloating() {
    return true;
  }

  public boolean isGeocentric() {
    return this.horizontalCoordinateSystemType.isGeocentric();
  }

  public boolean isGeographic() {
    return this.horizontalCoordinateSystemType.isGeographic();
  }

  @Override
  public boolean isHasHorizontalCoordinateSystem() {
    return this.horizontalCoordinateSystem != null;
  }

  public boolean isMoreDetailed(final GeometryFactory geometryFactory) {
    return !geometryFactory.isFloating();
  }

  public boolean isProjected() {
    return this.horizontalCoordinateSystemType.isProjected();
  }

  private boolean isProjectionRequired(final CoordinateSystem coordinateSystem) {
    final CoordinateSystem coordinateSystemThis = getHorizontalCoordinateSystem();
    if (coordinateSystemThis == coordinateSystem //
      || coordinateSystemThis == null //
      || coordinateSystem == null //
      || coordinateSystemThis.getHorizontalCoordinateSystemId() == coordinateSystem
        .getHorizontalCoordinateSystemId() //
      || coordinateSystemThis.equals(coordinateSystem)) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  public boolean isProjectionRequired(final GeometryFactory geometryFactory) {
    if (this == geometryFactory) {
      return false;
    } else if (geometryFactory == null) {
      return false;
    } else {
      final CoordinateSystem coordinateSystem = geometryFactory.getHorizontalCoordinateSystem();
      return isProjectionRequired(coordinateSystem);
    }
  }

  public boolean isSameCoordinateSystem(final CoordinateSystem coordinateSystem) {
    final int coordinateSystemId = getHorizontalCoordinateSystemId();
    if (coordinateSystem == null) {
      return this.coordinateSystem == null;
    } else {
      final int coordinateSystemId2 = coordinateSystem.getHorizontalCoordinateSystemId();
      if (coordinateSystemId == coordinateSystemId2) {
        return true;
      } else {
        final CoordinateSystem coordinateSystem2 = this.coordinateSystem;
        if (coordinateSystem2 == null) {
          if (coordinateSystemId2 <= 0) {
            return true;
          } else if (coordinateSystemId <= 0) {
            return true;
          } else {
            return false;
          }
        } else {
          return coordinateSystem.equals(coordinateSystem2);
        }
      }
    }
  }

  @Override
  public boolean isSameCoordinateSystem(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return false;
    } else {
      final int coordinateSystemId = getHorizontalCoordinateSystemId();
      final int coordinateSystemId2 = geometryFactory.getHorizontalCoordinateSystemId();
      if (coordinateSystemId == coordinateSystemId2) {
        return true;
      } else {
        final CoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
        final CoordinateSystem coordinateSystem2 = geometryFactory.getHorizontalCoordinateSystem();
        if (coordinateSystem == null) {
          if (coordinateSystemId <= 0) {
            return true;
          } else if (coordinateSystem2 == null && coordinateSystemId2 <= 0) {
            return true;
          } else {
            return false;
          }
        } else if (coordinateSystem2 == null) {
          if (coordinateSystemId2 <= 0) {
            return true;
          } else if (coordinateSystemId <= 0) {
            return true;
          } else {
            return false;
          }
        } else {
          return coordinateSystem.equals(coordinateSystem2);
        }
      }
    }
  }

  public boolean isVertical() {
    return this.verticalCoordinateSystemType.isVertical();
  }

  public Lineal lineal(final Geometry geometry) {
    if (geometry instanceof Lineal) {
      final Lineal lineal = (Lineal)geometry;
      return lineal.convertGeometry(this);
    } else if (geometry.isGeometryCollection()) {
      final List<LineString> lines = new ArrayList<>();
      for (final Geometry part : geometry.geometries()) {
        if (part instanceof LineString) {
          lines.add((LineString)part);
        } else {
          throw new IllegalArgumentException(
            "Cannot convert class " + part.getGeometryType() + " to Lineal\n" + geometry);
        }
      }
      return lineal(lines);
    } else {
      throw new IllegalArgumentException(
        "Cannot convert class " + geometry.getGeometryType() + " to Lineal\n" + geometry);
    }
  }

  public Lineal lineal(final Geometry... lines) {
    return lineal(Arrays.asList(lines));
  }

  public Lineal lineal(final int axisCount, final double[]... linesCoordinates) {
    if (linesCoordinates == null) {
      return lineString();
    } else {
      final int lineCount = linesCoordinates.length;
      final LineString[] lines = new LineString[lineCount];
      for (int i = 0; i < lineCount; i++) {
        final double[] coordinates = linesCoordinates[i];
        lines[i] = lineString(axisCount, coordinates);
      }
      return lineal(lines);
    }
  }

  public Lineal lineal(final Iterable<?> lines) {
    if (Property.isEmpty(lines)) {
      return lineString();
    } else {
      final List<LineString> lineStrings = new ArrayList<>();
      for (final Object value : lines) {
        if (value instanceof LineString) {
          final LineString line = (LineString)value;
          lineStrings.add(line.convertGeometry(this));
        } else if (value instanceof Lineal) {
          for (final LineString line : ((Lineal)value).lineStrings()) {
            lineStrings.add(line.convertGeometry(this));
          }
        } else if (value instanceof double[]) {
          final double[] points = (double[])value;
          final int axisCount = this.axisCount;
          final LineString line = lineString(axisCount, points);
          lineStrings.add(line);
        }
      }
      final int lineCount = lineStrings.size();
      if (lineCount == 0) {
        return lineString();
      } else if (lineCount == 1) {
        return lineStrings.get(0);
      } else {
        final LineString[] lineArray = new LineString[lineCount];
        lineStrings.toArray(lineArray);
        return lineal(lineArray);
      }
    }
  }

  /**
   * Creates a MultiLineString using the given LineStrings; a null or empty
   * array will Construct a new empty MultiLineString.
   *
   * @param lineStrings LineStrings, each of which may be empty but not null
   * @return the created MultiLineString
   */
  public Lineal lineal(final LineString... lines) {
    if (lines == null || lines.length == 0) {
      return lineString();
    } else if (lines.length == 1) {
      return lines[0];
    } else {
      return new MultiLineStringImpl(this, lines);
    }
  }

  public LinearRing linearRing() {
    return new LinearRingDoubleGf(this);
  }

  public LinearRing linearRing(final Collection<?> points) {
    if (points.isEmpty()) {
      return linearRing();
    } else {
      final LineStringEditor lineBuilder = newLineStringEditor(points);
      return lineBuilder.newLinearRing();
    }
  }

  public LinearRing linearRing(final int axisCount, double... coordinates) {
    final int vertexCount = coordinates.length / axisCount;
    coordinates = LineStringDoubleGf.getNewCoordinates(this, axisCount, vertexCount, coordinates);
    return new LinearRingDoubleGf(this, this.axisCount, vertexCount, coordinates);
  }

  public LinearRing linearRing(final int axisCount, final int vertexCount, double... coordinates) {
    coordinates = LineStringDoubleGf.getNewCoordinates(this, axisCount, vertexCount, coordinates);
    return new LinearRingDoubleGf(this, this.axisCount, vertexCount, coordinates);
  }

  /**
   * Creates a {@link LinearRing} using the given {@link LineString}.
   * A null or empty array creates an empty LinearRing.
   * The points must form a closed and simple linestring.
   *
   * @param coordinates a LineString (possibly empty), or null
   * @return the created LinearRing
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   */
  public LinearRing linearRing(final LineString line) {
    if (line == null || line.isEmpty()) {
      return linearRing();
    } else {
      final int vertexCount = line.getVertexCount();
      final double[] coordinates = LineStringDoubleGf.getNewCoordinates(this, line);
      return new LinearRingDoubleGf(this, this.axisCount, vertexCount, coordinates);
    }
  }

  /**
   * Creates a {@link LinearRing} using the given {@link Coordinates}s.
   * A null or empty array creates an empty LinearRing.
   * The points must form a closed and simple linestring.
   * @param coordinates an array without null elements, or an empty array, or null
   * @return the created LinearRing
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   */
  public LinearRing linearRing(final Point... points) {
    if (points == null || points.length == 0) {
      return linearRing();
    } else {
      return linearRing(Arrays.asList(points));
    }
  }

  public LineSegment lineSegment(final Point p0, final Point p1) {
    return new LineSegmentDoubleGF(this, p0, p1);
  }

  public LineString lineString() {
    return this.emptyLine;
  }

  public LineString lineString(final Collection<?> points) {
    if (points == null || points.isEmpty()) {
      return lineString();
    } else {
      final LineStringEditor lineBuilder = newLineStringEditor(points);
      return lineBuilder.newLineString();
    }
  }

  public LineString lineString(final int axisCount, double... coordinates) {
    if (coordinates == null || coordinates.length == 1) {
      return lineString();
    } else if (axisCount < 2) {
      return lineString();
    } else {
      final int vertexCount = coordinates.length / axisCount;
      coordinates = LineStringDoubleGf.getNewCoordinates(this, axisCount, vertexCount, coordinates);
      return new LineStringDoubleGf(this, this.axisCount, vertexCount, coordinates);
    }
  }

  public LineString lineString(final int axisCount, final int vertexCount, double... coordinates) {
    coordinates = LineStringDoubleGf.getNewCoordinates(this, axisCount, vertexCount, coordinates);
    return new LineStringDoubleGf(this, this.axisCount, vertexCount, coordinates);
  }

  public LineString lineString(final int axisCount, final Number[] coordinates) {
    final int vertexCount = coordinates.length / axisCount;
    final double[] coordinatesDouble = LineStringDoubleGf.getNewCoordinates(this, axisCount,
      vertexCount, coordinates);
    return new LineStringDoubleGf(this, this.axisCount, vertexCount, coordinatesDouble);
  }

  public LineString lineString(final LineString line) {
    if (line == null || line.isEmpty()) {
      return lineString();
    } else {
      final int vertexCount = line.getVertexCount();
      final double[] coordinates = LineStringDoubleGf.getNewCoordinates(this, line);
      return new LineStringDoubleGf(this, this.axisCount, vertexCount, coordinates);
    }
  }

  public LineString lineString(final Point... points) {
    if (points == null) {
      return lineString();
    } else {
      final List<Point> linePoints = new ArrayList<>();
      for (final Point point : points) {
        if (point != null && !point.isEmpty()) {
          linePoints.add(point);
        }
      }
      return lineString(linePoints);
    }
  }

  public void makePrecise(final double[] values, final double[] valuesPrecise) {
    for (int i = 0; i < valuesPrecise.length; i++) {
      valuesPrecise[i] = values[i];
    }
  }

  public double makePrecise(final int axisIndex, final double value) {
    return value;
  }

  public void makePrecise(final int axisCount, final double... coordinates) {
  }

  public double makePreciseCeil(final int axisIndex, final double value) {
    return value;
  }

  public double makePreciseFloor(final int axisIndex, final double value) {
    return value;
  }

  public double makeXPrecise(final double value) {
    return value;
  }

  public double makeXPreciseCeil(final double value) {
    return value;
  }

  public double makeXPreciseFloor(final double value) {
    return value;
  }

  public double makeXyPrecise(final double value) {
    return value;
  }

  public double makeXyPreciseCeil(final double value) {
    return value;
  }

  public double makeXyPreciseFloor(final double value) {
    return value;
  }

  public double makeYPrecise(final double value) {
    return value;
  }

  public double makeYPreciseCeil(final double value) {
    return value;
  }

  public double makeYPreciseFloor(final double value) {
    return value;
  }

  public double makeZPrecise(final double value) {
    return value;
  }

  public double makeZPreciseCeil(final double value) {
    return value;
  }

  public double makeZPreciseFloor(final double value) {
    return value;
  }

  public BoundingBox newBoundingBox(final BoundingBox boundingBox) {
    final int axisCount = this.axisCount;
    final double[] bounds = boundingBox.getMinMaxValues(axisCount);
    return newBoundingBox(axisCount, bounds);
  }

  public BoundingBox newBoundingBox(final double x, final double y) {
    return newBoundingBox(x, y, x, y);
  }

  public BoundingBox newBoundingBox(final double minX, final double minY, final double maxX,
    final double maxY) {
    if (isHasHorizontalCoordinateSystem()) {
      return new BoundingBoxDoubleXYGeometryFactory(this, minX, minY, maxX, maxY);
    } else {
      return new BoundingBoxDoubleXY(minX, minY, maxX, maxY);
    }
  }

  public BoundingBox newBoundingBox(final int axisCount) {
    return new BoundingBoxDoubleGeometryFactory(this, axisCount);
  }

  public BoundingBox newBoundingBox(final int axisCount, final double... bounds) {
    if (axisCount == 2) {
      final double x1 = bounds[0];
      final double y1 = bounds[1];
      final double x2 = bounds[2];
      final double y2 = bounds[3];
      return newBoundingBox(x1, y1, x2, y2);
    } else {
      return new BoundingBoxDoubleGeometryFactory(this, axisCount, bounds);
    }
  }

  public BoundingBox newBoundingBox(int axisCount, final Iterable<? extends Point> points) {
    axisCount = Math.min(axisCount, this.axisCount);
    double[] bounds = null;
    if (points != null) {
      for (final Point point : points) {
        if (point != null) {
          if (bounds == null) {
            bounds = RectangleUtil.newBounds(this, axisCount, point);
          } else {
            RectangleUtil.expand(this, bounds, point);
          }
        }
      }
    }
    if (bounds == null) {
      return this.boundingBoxEmpty;
    } else {
      return newBoundingBox(axisCount, bounds);
    }
  }

  public BoundingBox newBoundingBox(int axisCount, final Point... points) {
    axisCount = Math.min(axisCount, this.axisCount);
    double[] bounds = null;
    if (points != null) {
      for (final Point point : points) {
        if (point != null) {
          if (bounds == null) {
            bounds = RectangleUtil.newBounds(this, axisCount, point);
          } else {
            RectangleUtil.expand(this, bounds, point);
          }
        }
      }
    }
    if (bounds == null) {
      return this.boundingBoxEmpty;
    } else {
      return newBoundingBox(axisCount, bounds);
    }
  }

  public BoundingBox newBoundingBox(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return newBoundingBox(x, y);
  }

  public double[] newBounds(final int axisCount) {
    return RectangleUtil.newBounds(axisCount);
  }

  public LineStringEditor newLineStringEditor() {
    return new LineStringEditor(this);
  }

  private LineStringEditor newLineStringEditor(final Collection<?> points) {
    final LineStringEditor lineBuilder = new LineStringEditor(this, points.size());
    for (final Object object : points) {
      if (object == null) {
      } else if (object instanceof Point) {
        final Point point = (Point)object;
        lineBuilder.appendVertex(point);
      } else if (object instanceof double[]) {
        final double[] coordinates = (double[])object;
        lineBuilder.appendVertex(coordinates);
      } else if (object instanceof List<?>) {
        @SuppressWarnings("unchecked")
        final List<Number> list = (List<Number>)object;
        final double[] coordinates = Doubles.toDoubleArray(list);
        lineBuilder.appendVertex(coordinates);
      } else if (object instanceof LineString) {
        final LineString LineString = (LineString)object;
        final Point point = LineString.getPoint(0);
        lineBuilder.appendVertex(point);
      } else {
        throw new IllegalArgumentException("Unexepected data type: " + object);
      }
    }
    return lineBuilder;
  }

  public LineStringEditor newLineStringEditor(final int vertexCapacity) {
    return new LineStringEditor(this, vertexCapacity);
  }

  public RectangleXY newRectangle(final double x, final double y, final double width,
    final double height) {
    return new RectangleXY(this, x, y, width, height);
  }

  public RectangleXY newRectangleCorners(double x1, double y1, double x2, double y2) {
    if (x1 > x2) {
      final double t = x1;
      x1 = x2;
      x2 = t;
    }
    if (y1 > y2) {
      final double t = y1;
      y1 = y2;
      y2 = t;
    }
    return new RectangleXY(this, x1, y1, x2 - x1, y2 - y1);
  }

  public double[] newScales(final int axisCount) {
    return new double[axisCount];
  }

  public abstract GeometryFactory newWithOffsets(double offsetX, double offsetY, double offsetZ);

  public abstract GeometryFactory newWithOffsetsAndScales(final double offsetX, final double scaleX,
    final double offsetY, final double scaleY, final double offsetZ, final double scaleZ);

  /**
   * <p>Construct a newn empty {@link Point}.</p>
   *
   * @return The point.
   */
  public Point point() {
    return this.emptyPoint;
  }

  /**
   * <p>Construct a new new {@link Point} from the specified point coordinates.
   * If the point is null or has length < 2 an empty point will be returned.
   * The result point will have the same  {@link #getAxisCount()} from this factory.
   * Additional coordinates in the point will be ignored. If the point length is &lt;
   * {@link #getAxisCount()} then {@link Double#NaN} will be used for that axis.</p>
   *
   * @param point The coordinates to create the point from.
   * @return The point.
   */
  public Point point(final double... coordinates) {
    if (coordinates == null) {
      return point();
    } else {
      int axisCount = this.axisCount;
      if (coordinates.length < axisCount) {
        axisCount = coordinates.length;
      }
      for (int axisIndex = axisCount - 1; axisIndex > 1; axisIndex--) {
        if (Double.isFinite(coordinates[axisIndex])) {
          break;
        } else {
          axisCount--;
        }
      }
      if (axisCount < 2) {
        return point();
      } else if (axisCount == 2) {
        return new PointDoubleXYGeometryFactory(this, coordinates[0], coordinates[1]);
      } else if (axisCount == 3) {
        return new PointDoubleXYZGeometryFactory(this, coordinates[0], coordinates[1],
          coordinates[2]);
      } else {
        return new PointDoubleGf(this, coordinates);
      }
    }
  }

  public Point point(final double x, final double y) {
    return new PointDoubleXYGeometryFactory(this, x, y);
  }

  /**
   * Creates a Point using the given LineString; a null or empty
   * LineString will Construct a newn empty Point.
   *
   * @param points a LineString (possibly empty), or null
   * @return the created Point
   */
  public Point point(final LineString points) {
    if (points == null) {
      return point();
    } else {
      final int size = points.getVertexCount();
      if (size == 0) {
        return point();
      } else if (size == 1) {
        final int axisCount = Math.min(points.getAxisCount(), this.axisCount);
        final double[] coordinates = new double[axisCount];
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          final double coordinate = points.getCoordinate(0, axisIndex);
          coordinates[axisIndex] = coordinate;
        }
        return point(coordinates);
      } else {
        throw new IllegalArgumentException("Point can only have 1 vertex not " + size);
      }
    }
  }

  /**
   * <p>Construct a new new {@link Point} from the object using the following rules.<p>
   * <ul>
   *   <li><code>null</code> using {@link #point()}</li>
   *   <li>Instances of {@link Point} using {@link Point#newGeometry(GeometryFactory)}</li>
   *   <li>Instances of {@link Coordinates} using {@link #point(Point)}</li>
   *   <li>Instances of {@link LineString} using {@link #point(LineString)}</li>
   *   <li>Instances of {@link double[]} using {@link #point(double[])}</li>
   *   <li>Instances of any other class throws {@link IllegalArgumentException}.<li>
   * </ul>
   *
   * @param point The coordinates to create the point from.
   * @return The point.
   * @throws IllegalArgumentException If the object is not an instance of a supported class.
   */
  public Point point(final Object object) {
    if (object == null) {
      return point();
    } else if (object instanceof Point) {
      final Point point = (Point)object;
      return point.newGeometry(this);
    } else if (object instanceof double[]) {
      return point((double[])object);
    } else if (object instanceof List<?>) {
      @SuppressWarnings("unchecked")
      final List<Number> list = (List<Number>)object;
      final double[] pointCoordinates = Doubles.toDoubleArray(list);
      return point(pointCoordinates);
    } else if (object instanceof Point) {
      return point((Point)object);
    } else if (object instanceof LineString) {
      return point((LineString)object);
    } else {
      throw new IllegalArgumentException("Cannot Construct a new point from " + object.getClass());
    }
  }

  /**
   * <p>Construct a new new {@link Point} from the specified point ({@link Coordinates}).
   * If the point is null or has {@link Coordinates#getAxisCount()} &lt; 2 an empty
   * point will be returned. The result point will have the same  {@link #getAxisCount()} from this
   * factory. Additional axis in the point will be ignored. If the point has a smaller
   * {@link Point#getAxisCount()} then {@link Double#NaN} will be used for that axis.</p>
   *
   * @param point The coordinates to create the point from.
   * @return The point.
   */
  public Point point(final Point point) {
    if (point == null || point.isEmpty()) {
      return point();
    } else {
      if (point.isSameCoordinateSystem(this)) {
        final double[] coordinates = point.getCoordinates();
        return point(coordinates);
      } else {
        return point.newGeometry(this);
      }
    }
  }

  public Polygon polygon() {
    return this.emptyPolygon;
  }

  public Polygon polygon(final Geometry... rings) {
    return polygon(Arrays.asList(rings));
  }

  public Polygon polygon(final int axisCount, final double... ringCoordinates) {
    if (ringCoordinates != null) {
      final LinearRing ring = linearRing(axisCount, ringCoordinates);
      if (!ring.isEmpty()) {
        return new PolygonImpl(this, ring);
      }
    }
    return polygon();
  }

  public Polygon polygon(final int axisCount, final double[]... ringsCoordinates) {
    if (ringsCoordinates != null) {
      final int ringCordinatesCount = ringsCoordinates.length;
      final LinearRing[] rings = new LinearRing[ringCordinatesCount];
      int ringCount = 0;
      for (int i = 0; i < ringCordinatesCount; i++) {
        final double[] ringCoordinates = ringsCoordinates[i];
        final LinearRing ring = linearRing(axisCount, ringCoordinates);
        if (!ring.isEmpty()) {
          if (i > 0 && ringCount == 0) {
            throw new IllegalArgumentException("shell is empty but hole " + (i - 1) + " is not");
          }
          rings[ringCount++] = ring;
        }
      }
      if (ringCount > 0) {
        return new PolygonImpl(this, rings, ringCount);
      }
    }
    return polygon();
  }

  /**
   * Constructs a <code>Polygon</code> with the given exterior boundary.
   *
   * @param shell
   *            the outer boundary of the new <code>Polygon</code>, or
   *            <code>null</code> or an empty <code>LinearRing</code> if
   *            the empty geometry is to be created.
   * @throws IllegalArgumentException if the boundary ring is invalid
   */
  public Polygon polygon(final LinearRing shell) {
    if (shell == null || shell.isEmpty()) {
      return polygon();
    } else {
      return new PolygonImpl(this, shell);
    }
  }

  public Polygon polygon(final LineString... rings) {
    final List<LineString> ringList = Arrays.asList(rings);
    return polygon(ringList);
  }

  public Polygon polygon(final List<?> rings) {
    if (rings != null) {
      final LinearRing[] linearRings = new LinearRing[rings.size()];
      int ringCount = 0;
      int i = 0;
      for (final Object ringObject : rings) {
        final LinearRing ring = getLinearRing(ringObject);
        if (!ring.isEmpty()) {
          if (i > 0 && ringCount == 0) {
            throw new IllegalArgumentException("shell is empty but hole " + (i - 1) + " is not");
          }
          linearRings[ringCount++] = ring;
        }
        i++;
      }
      if (ringCount > 0) {
        return new PolygonImpl(this, linearRings, ringCount);
      }
    }
    return polygon();
  }

  public Polygon polygon(final Polygon polygon) {
    return polygon.newGeometry(this);
  }

  public Polygonal polygonal(final Geometry geometry) {
    if (geometry instanceof Polygonal) {
      final Polygonal polygonal = (Polygonal)geometry.convertGeometry(this);
      return polygonal;
    } else if (geometry.isGeometryCollection()) {
      final List<Polygon> polygons = new ArrayList<>();
      for (final Geometry part : geometry.geometries()) {
        if (part instanceof Polygon) {
          polygons.add((Polygon)part);
        } else {
          throw new IllegalArgumentException(
            "Cannot convert class " + part.getGeometryType() + " to Polygonal\n" + geometry);
        }
      }
      return polygonal(polygons);
    } else {
      throw new IllegalArgumentException(
        "Cannot convert class " + geometry.getGeometryType() + " to Polygonal\n" + geometry);
    }
  }

  public Polygonal polygonal(final Iterable<?> polygons) {
    final Polygon[] polygonArray = getPolygonArray(polygons);
    return polygonal(polygonArray);
  }

  public Polygonal polygonal(final Object... polygons) {
    return polygonal(Arrays.asList(polygons));
  }

  /**
   * Creates a MultiPolygon using the given Polygons; a null or empty array
   * will Construct a newn empty Polygon. The polygons must conform to the
   * assertions specified in the <A
   * HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple Features
   * Specification for SQL</A>.
   *
   * @param polygons
   *            Polygons, each of which may be empty but not null
   * @return the created MultiPolygon
   */
  public Polygonal polygonal(final Polygon... polygons) {
    if (polygons == null || polygons.length == 0) {
      return polygon();
    } else if (polygons.length == 1) {
      return polygons[0];
    } else {
      return new MultiPolygonImpl(this, polygons);
    }
  }

  /**
   * Project the geometry if it is in a different coordinate system
   *
   * @param geometry
   * @return
   */
  public <G extends Geometry> G project(final G geometry) {
    return geometry.convertGeometry(this);
  }

  public Punctual punctual(final Geometry... points) {
    return punctual(Arrays.asList(points));
  }

  public Punctual punctual(final Geometry geometry) {
    if (geometry instanceof Punctual) {
      final Punctual punctual = (Punctual)geometry.convertGeometry(this);
      return punctual;
    } else if (geometry.isGeometryCollection()) {
      final List<Point> points = new ArrayList<>();
      for (final Geometry part : geometry.geometries()) {
        if (part instanceof Point) {
          points.add((Point)part);
        } else {
          throw new IllegalArgumentException(
            "Cannot convert class " + part.getGeometryType() + " to Punctual\n" + geometry);
        }
      }
      return punctual(points);
    } else {
      throw new IllegalArgumentException(
        "Cannot convert class " + geometry.getGeometryType() + " to Punctual\n" + geometry);
    }
  }

  public Punctual punctual(final int axisCount, final double... coordinates) {
    if (coordinates == null || coordinates.length == 0 || axisCount < 2) {
      return point();
    } else if (coordinates.length % axisCount != 0) {
      throw new IllegalArgumentException(
        "Coordinates length=" + coordinates.length + " must be a multiple of " + axisCount);
    } else {
      final Point[] points = new Point[coordinates.length / axisCount];
      for (int i = 0; i < points.length; i++) {
        final double[] newCoordinates = new double[axisCount];
        System.arraycopy(coordinates, i * axisCount, newCoordinates, 0, axisCount);
        final Point point = point(newCoordinates);
        points[i] = point;
      }
      return punctual(points);
    }
  }

  public Punctual punctual(final Iterable<?> points) {
    final Point[] pointArray = getPointArray(points);
    return punctual(pointArray);
  }

  /**
   * Creates a {@link Punctual} using the
   * points in the given {@link LineString}.
   * A <code>null</code> or empty LineString creates an empty {@link Point}.
   *
   * @param coordinates a LineString (possibly empty), or <code>null</code>
   * @return a MultiPoint geometry
   */
  public Punctual punctual(final LineString coordinatesList) {
    if (coordinatesList == null) {
      return punctual();
    } else {
      final Point[] points = new Point[coordinatesList.getVertexCount()];
      for (int i = 0; i < points.length; i++) {
        final Point coordinates = coordinatesList.getPoint(i);
        final Point point = point(coordinates);
        points[i] = point;
      }
      return punctual(points);
    }
  }

  /**
   * Creates a {@link Punctual} using the given {@link Point}s.
   * A null or empty array will Construct a new empty Point.
   *
   * @param coordinates an array (without null elements), or an empty array, or <code>null</code>
   * @return a {@link Punctual} object
   */
  public Punctual punctual(final Point... points) {
    if (points == null || points.length == 0) {
      return point();
    } else if (points.length == 1) {
      return points[0];
    } else {
      return new MultiPointImpl(this, points);
    }
  }

  @Override
  public double toDoubleX(final int x) {
    return x;
  }

  @Override
  public double toDoubleY(final int y) {
    return y;
  }

  @Override
  public double toDoubleZ(final int z) {
    return z;
  }

  public GeometryFactory toFloating2d() {
    return this.instances.floating(2);
  }

  public GeometryFactory toFloating3d() {
    return this.instances.floating(3);
  }

  @Override
  public int toIntX(final double x) {
    if (Double.isFinite(x)) {
      return (int)Math.round(x);
    } else {
      return Integer.MIN_VALUE;
    }
  }

  @Override
  public int toIntY(final double y) {
    if (Double.isFinite(y)) {
      return (int)Math.round(y);
    } else {
      return Integer.MIN_VALUE;
    }
  }

  @Override
  public int toIntZ(final double z) {
    if (Double.isFinite(z)) {
      return (int)Math.round(z);
    } else {
      return Integer.MIN_VALUE;
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    addTypeToMap(map, "geometryFactory");
    map.put("srid", getCoordinateSystemId());
    map.put("axisCount", this.axisCount);

    final double scaleX = getScaleX();
    addToMap(map, "scaleX", scaleX, 0.0);

    final double scaleY = getScaleY();
    addToMap(map, "scaleY", scaleY, 0.0);

    if (this.axisCount > 2) {
      final double scaleZ = getScaleZ();
      addToMap(map, "scaleZ", scaleZ, 0.0);
    }
    return map;
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder();
    final int coordinateSystemId = getCoordinateSystemId();
    if (this.coordinateSystem != null) {
      final String coordinateSystemName = getCoordinateSystemName();
      string.append(coordinateSystemName);
      string.append(", ");
    }
    string.append("coordinateSystemId=");
    string.append(coordinateSystemId);
    string.append(", axisCount=");
    string.append(this.axisCount);
    return string.toString();
  }

  public String toWktCs() {
    try (
      StringWriter stringWriter = new StringWriter()) {
      writeWktCs(stringWriter, -1);
      return stringWriter.toString();
    }
  }

  public String toWktCsFormatted() {
    try (
      StringWriter stringWriter = new StringWriter()) {
      writeWktCs(stringWriter, 0);
      return stringWriter.toString();
    }
  }

  public Polygonal union(final List<Polygon> polygons) {
    final Polygonal polygonal = CascadedPolygonUnion.union(polygons);
    if (polygonal == null) {
      return polygon();
    } else {
      return polygonal;
    }
  }

  public void writeOffsetScaled3d(final ChannelWriter writer) {
    final int coordinateSystemId = getHorizontalCoordinateSystemId();
    writer.putInt(coordinateSystemId);
    writer.putDouble(getOffsetX());
    writer.putDouble(getScaleX());
    writer.putDouble(getOffsetY());
    writer.putDouble(getScaleY());
    writer.putDouble(getOffsetZ());
    writer.putDouble(getScaleZ());
  }

  @Override
  public void writePrjFile(final Object target) {
    if (isHasHorizontalCoordinateSystem()) {
      final Resource resource = Resource.getResource(target);
      if (resource != null) {
        final Resource prjResource = resource.newResourceChangeExtension("prj");
        if (prjResource != null) {
          try (
            final Writer writer = prjResource.newWriter(StandardCharsets.ISO_8859_1)) {
            writeWktCs(writer, -1);
          } catch (final Throwable e) {
            Logs.error(this, "Unable to create: " + resource, e);
          }
        }
      }
    }
  }

  private boolean writeWktCs(final Writer writer, final int indentLevel) {
    final CoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    if (coordinateSystem == null) {
      return false;
    } else {
      final int coordinateSystemId = getHorizontalCoordinateSystemId();
      final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems
        .getCoordinateSystem(coordinateSystemId);
      if (esriCoordinateSystem == null) {
        EsriCsWktWriter.write(writer, coordinateSystem, indentLevel);
      } else {
        EsriCsWktWriter.write(writer, esriCoordinateSystem, indentLevel);
      }
      return true;
    }
  }

}
