package com.revolsys.oracle.recordstore.esri;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class ArcSdeStGeometryFieldDefinition extends JdbcFieldDefinition {

  public static List<List<Geometry>> getParts(final Geometry geometry) {
    final List<List<Geometry>> partsList = new ArrayList<>();
    if (geometry != null) {
      final ClockDirection expectedRingOrientation = ClockDirection.COUNTER_CLOCKWISE;
      for (final Geometry part : geometry.geometries()) {
        if (!part.isEmpty()) {
          if (part instanceof Point) {
            final Point point = (Point)part;
            partsList.add(Collections.<Geometry> singletonList(point));
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            partsList.add(Collections.<Geometry> singletonList(line));
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<Geometry> ringList = new ArrayList<>();

            ClockDirection partExpectedRingOrientation = expectedRingOrientation;
            for (LinearRing ring : polygon.rings()) {
              final ClockDirection ringOrientation = ring.getClockDirection();
              if (ringOrientation != partExpectedRingOrientation) {
                ring = ring.reverse();
              }
              ringList.add(ring);
              if (partExpectedRingOrientation == expectedRingOrientation) {
                partExpectedRingOrientation = expectedRingOrientation.opposite();
              }
            }
            partsList.add(ringList);
          }
        }
      }
    }
    return partsList;
  }

  private final int axisCount;

  private final ArcSdeSpatialReference spatialReference;

  public ArcSdeStGeometryFieldDefinition(final String dbName, final String name,
    final DataType type, final boolean required, final String description,
    final Map<String, Object> properties, final ArcSdeSpatialReference spatialReference,
    final int axisCount) {
    super(dbName, name, type, -1, 0, 0, required, description, properties);
    this.spatialReference = spatialReference;
    final GeometryFactory geometryFactory = spatialReference.getGeometryFactory();

    final double scaleX = geometryFactory.getScaleX();
    final double scaleY = geometryFactory.getScaleY();
    final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
    if (axisCount >= 3) {
      final double[] scales = geometryFactory.newScales(axisCount);
      setGeometryFactory(GeometryFactory.fixed(coordinateSystemId, axisCount, scales));
    } else {
      setGeometryFactory(GeometryFactory.fixed2d(coordinateSystemId, scaleX, scaleY));
    }
    this.axisCount = axisCount;
  }

  @Override
  public void addStatementPlaceHolder(final StringBuilder sql) {
    sql.append("SDE.ST_GEOMETRY(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
  }

  @Override
  public void appendSelect(final Query query, final RecordStore recordStore, final Appendable sql) {
    try {
      super.appendName(sql);
      sql.append(".ENTITY, ");
      super.appendName(sql);
      sql.append(".NUMPTS, ");
      super.appendName(sql);
      sql.append(".POINTS");
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public ArcSdeStGeometryFieldDefinition clone() {
    return new ArcSdeStGeometryFieldDefinition(getDbName(), getName(), getDataType(), isRequired(),
      getDescription(), getProperties(), this.spatialReference, this.axisCount);
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    final int geometryType = resultSet.getInt(indexes.incrementAndGet());
    if (resultSet.wasNull()) {
      return null;
    } else {
      final int numPoints = resultSet.getInt(indexes.incrementAndGet());
      final Blob blob = resultSet.getBlob(indexes.incrementAndGet());
      try (
        final InputStream pointsIn = new BufferedInputStream(blob.getBinaryStream(), 32000)) {

        final Double xOffset = this.spatialReference.getXOffset();
        final Double yOffset = this.spatialReference.getYOffset();
        final Double xyScale = this.spatialReference.getXyScale();
        final Double zScale = this.spatialReference.getZScale();
        final Double zOffset = this.spatialReference.getZOffset();
        final Double mScale = this.spatialReference.getMScale();
        final Double mOffset = this.spatialReference.getMOffset();

        final GeometryFactory geometryFactory = getGeometryFactory();
        final Geometry geometry = PackedCoordinateUtil.getGeometry(pointsIn, geometryFactory,
          geometryType, numPoints, xOffset, yOffset, xyScale, zOffset, zScale, mOffset, mScale);
        return geometry;
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  @Override
  public boolean isSortable() {
    return false;
  }

  public void setFloat(final PreparedStatement statement, int index, final Double value,
    final Number defaultValue) throws SQLException {
    if (value == null || Double.isInfinite(value) || Double.isNaN(value)) {
      if (defaultValue == null) {
        statement.setNull(index++, Types.FLOAT);
      } else {
        statement.setFloat(index, defaultValue.floatValue());
      }
    } else {
      statement.setFloat(index, value.floatValue());
    }
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    Object value) throws SQLException {
    final GeometryFactory geometryFactory = getGeometryFactory();
    int index = parameterIndex;

    if (value instanceof BoundingBox) {
      final BoundingBox boundingBox = (BoundingBox)value;
      value = boundingBox.bboxToCs(geometryFactory).toPolygon(1);
    }
    if (value instanceof Geometry) {
      Geometry geometry = (Geometry)value;
      geometry = geometry.newGeometry(geometryFactory);

      final int sdeSrid = this.spatialReference.getEsriSrid();
      final Double xOffset = this.spatialReference.getXOffset();
      final Double yOffset = this.spatialReference.getYOffset();
      final Double xyScale = this.spatialReference.getXyScale();
      final Double zScale = this.spatialReference.getZScale();
      final Double zOffset = this.spatialReference.getZOffset();
      final Double mScale = this.spatialReference.getMScale();
      final Double mOffset = this.spatialReference.getMOffset();

      final BoundingBox envelope = geometry.getBoundingBox();

      final double minX = envelope.getMinX();
      final double minY = envelope.getMinY();
      final double maxX = envelope.getMaxX();
      final double maxY = envelope.getMaxY();
      final double area = geometry.getArea();
      final double length = geometry.getLength();

      final boolean hasZ = this.axisCount > 2 && zOffset != null && zScale != null;
      final boolean hasM = this.axisCount > 3 && mOffset != null && mScale != null;

      int numPoints = 0;
      byte[] data;

      final List<List<Geometry>> parts = getParts(geometry);
      final int entityType = ArcSdeConstants.getStGeometryType(geometry);
      numPoints = PackedCoordinateUtil.getNumPoints(parts);
      data = PackedCoordinateUtil.getPackedBytes(xOffset, yOffset, xyScale, hasZ, zOffset, zScale,
        hasM, mScale, mOffset, parts);

      statement.setInt(index++, entityType);
      statement.setInt(index++, numPoints);
      setFloat(statement, index++, minX, 0);
      setFloat(statement, index++, minY, 0);
      setFloat(statement, index++, maxX, 0);
      setFloat(statement, index++, maxY, 0);
      if (hasZ) {
        final double minZ = envelope.getMin(2);
        final double maxZ = envelope.getMax(2);
        setFloat(statement, index++, minZ, 0);
        setFloat(statement, index++, maxZ, 0);
      } else {
        statement.setNull(index++, Types.FLOAT);
        statement.setNull(index++, Types.FLOAT);
      }
      if (hasM) {
        final double minM = envelope.getMin(3);
        final double maxM = envelope.getMax(3);
        setFloat(statement, index++, minM, 0);
        setFloat(statement, index++, maxM, 0);
      } else {
        statement.setNull(index++, Types.FLOAT);
        statement.setNull(index++, Types.FLOAT);
      }
      statement.setFloat(index++, (float)area);
      statement.setFloat(index++, (float)length);
      statement.setInt(index++, sdeSrid);
      statement.setBytes(index++, data);
    } else {
      throw new IllegalArgumentException("Geometry cannot be null");
    }
    return index;
  }
}
