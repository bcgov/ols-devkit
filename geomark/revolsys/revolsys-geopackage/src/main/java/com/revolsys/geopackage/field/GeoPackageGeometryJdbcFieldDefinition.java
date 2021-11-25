package com.revolsys.geopackage.field;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.wkb.WkbByteBufferReader;
import com.revolsys.geometry.wkb.WkbChannelWriter;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;

public class GeoPackageGeometryJdbcFieldDefinition extends JdbcFieldDefinition {
  public static Geometry parseWkb(GeometryFactory geometryFactory, final byte[] data) {
    final ByteBuffer buffer = ByteBuffer.wrap(data);
    if (buffer.get() == 'G') {
      if (buffer.get() == 'P') {
        final byte version = buffer.get();
        final byte flags = buffer.get();

        final boolean extended = (flags >> 5 & 1) == 1;

        final boolean empty = (flags >> 4 & 1) == 1;

        final int envelopeType = flags >> 1 & 7;

        ByteOrder byteOrder;
        if ((flags & 1) == 0) {
          byteOrder = ByteOrder.BIG_ENDIAN;
        } else {
          byteOrder = ByteOrder.LITTLE_ENDIAN;
        }
        buffer.order(byteOrder);
        final int coordinateSystemId = buffer.getInt();
        geometryFactory = geometryFactory.convertSrid(coordinateSystemId);
        int envelopeCoordinateCount = 0;
        switch (envelopeType) {
          case 1:
            envelopeCoordinateCount = 4;
          break;
          case 2:
            envelopeCoordinateCount = 6;
          break;
          case 3:
            envelopeCoordinateCount = 6;
          break;
          case 4:
            envelopeCoordinateCount = 8;
          break;

          default:
          break;
        }
        for (int i = 0; i < envelopeCoordinateCount; i++) {
          buffer.getDouble();
        }
        return WkbByteBufferReader.readGeometry(geometryFactory, buffer);
      }
    }
    throw new IllegalArgumentException(
      "Invalid Geometry header, expecting GP\n" + Arrays.toString(data));
  }

  private final int axisCount;

  private final int srid;

  public GeoPackageGeometryJdbcFieldDefinition(final String dbName, final String name,
    final DataType dataType, final boolean required, final String description,
    final Map<String, Object> properties, final int srid, final int axisCount,
    final GeometryFactory geometryFactory) {
    super(dbName, name, dataType, -1, 0, 0, required, description, properties);
    this.srid = srid;
    setGeometryFactory(geometryFactory);
    this.axisCount = axisCount;
  }

  @Override
  public JdbcFieldDefinition clone() {
    return new GeoPackageGeometryJdbcFieldDefinition(getDbName(), getName(), getDataType(),
      isRequired(), getDescription(), getProperties(), this.srid, this.axisCount,
      getGeometryFactory());
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    final Object databaseValue = resultSet.getObject(indexes.incrementAndGet());
    return toJava(databaseValue);
  }

  @Override
  public boolean isSortable() {
    return false;
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    final Object jdbcValue = toJdbc(value);
    if (jdbcValue == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      statement.setObject(parameterIndex, jdbcValue);
    }
    return parameterIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    final Object jdbcValue = toJdbc(value);
    if (jdbcValue == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      statement.setObject(parameterIndex, jdbcValue);
    }
    return parameterIndex + 1;
  }

  public Object toJava(final Object object) throws SQLException {
    if (object instanceof byte[]) {
      final byte[] bytes = (byte[])object;
      return parseWkb(getGeometryFactory(), bytes);
    }
    return object;
  }

  private Object toJdbc(final Object object) throws SQLException {
    if (object == null) {
      return null;
    } else if (object instanceof Geometry) {
      final Geometry geometry = (Geometry)object;
      return toWkb(geometry);
    } else if (object instanceof BoundingBox) {
      BoundingBox boundingBox = (BoundingBox)object;
      boundingBox = boundingBox.bboxToCs(getGeometryFactory());
      // TODO
      return null;
    } else if (object instanceof Double) {
      return object;
    } else {
      throw new IllegalArgumentException("Expecting geometry or boundingBox not: " + object);
    }
  }

  private byte[] toWkb(final Geometry geometry) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (
      WkbChannelWriter writer = new WkbChannelWriter(out)) {
      writer.putString("GP", 2);
      writer.putByte((byte)0);
      byte flags = 0;// Big Endian
      final boolean empty = geometry.isEmpty();
      if (empty) {
        flags |= 1 << 4;
      } else {
        int envelopeType;
        if (this.axisCount == 3) {
          envelopeType = 2;
        } else if (this.axisCount == 4) {
          envelopeType = 4;
        } else {
          envelopeType = 1;
        }
        flags |= envelopeType << 1;
      }
      writer.putByte(flags);
      writer.putInt(this.srid);
      if (!empty) {
        final BoundingBox boundingBox = geometry.getBoundingBox();
        writer.putDouble(boundingBox.getMinX());
        writer.putDouble(boundingBox.getMaxX());
        writer.putDouble(boundingBox.getMinY());
        writer.putDouble(boundingBox.getMaxY());
        if (this.axisCount > 2) {
          writer.putDouble(boundingBox.getMinZ());
          writer.putDouble(boundingBox.getMaxZ());
          if (this.axisCount > 3) {
            writer.putDouble(boundingBox.getMin(3));
            writer.putDouble(boundingBox.getMax(3));
          }
        }
      }
      writer.writeGeometry(geometry);
    }
    return out.toByteArray();
  }

}
