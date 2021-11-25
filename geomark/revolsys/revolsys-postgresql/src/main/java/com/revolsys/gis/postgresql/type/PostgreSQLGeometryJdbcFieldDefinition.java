package com.revolsys.gis.postgresql.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public class PostgreSQLGeometryJdbcFieldDefinition extends JdbcFieldDefinition {
  private final int axisCount;

  private final int srid;

  public PostgreSQLGeometryJdbcFieldDefinition(final String dbName, final String name,
    final DataType dataType, final int sqlType, final boolean required, final String description,
    final Map<String, Object> properties, final int srid, final int axisCount,
    final GeometryFactory geometryFactory) {
    super(dbName, name, dataType, sqlType, 0, 0, required, description, properties);
    this.srid = srid;
    this.axisCount = axisCount;
    setGeometryFactory(geometryFactory.convertAxisCount(axisCount));
  }

  @Override
  public JdbcFieldDefinition clone() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new PostgreSQLGeometryJdbcFieldDefinition(getDbName(), getName(), getDataType(),
      getSqlType(), isRequired(), getDescription(), getProperties(), this.srid, this.axisCount,
      geometryFactory);
  }

  public Object getInsertUpdateValue(final Object value) throws SQLException {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (value == null) {
      return null;
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      if (geometry.isEmpty()) {
        return geometry;
      } else {
        final DataType dataType = getDataType();
        return new PostgreSQLGeometryWrapper(dataType, geometryFactory, geometry);
      }
    } else if (value instanceof BoundingBox) {
      BoundingBox boundingBox = (BoundingBox)value;
      boundingBox = boundingBox.bboxToCs(geometryFactory);
      return new PostgreSQLBoundingBoxWrapper(boundingBox);
    } else if (Property.hasValue(value)) {
      return value;
    } else {
      return null;
    }
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    final Object postgresValue = resultSet.getObject(indexes.incrementAndGet());
    final Object value = toJava(postgresValue);
    return value;
  }

  @Override
  public boolean isSortable() {
    return false;
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      super.setGeometryFactory(geometryFactory.convertAxisCount(this.axisCount));
    }
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    final Object jdbcValue = getInsertUpdateValue(value);
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
    if (object instanceof PostgreSQLGeometryWrapper) {
      final PostgreSQLGeometryWrapper geometryType = (PostgreSQLGeometryWrapper)object;
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Geometry geometry = geometryType.getGeometry(geometryFactory);
      return geometry;
    } else {
      return object;
    }
  }

  public Object toJdbc(final Object object) throws SQLException {
    if (object instanceof Geometry) {
      final Geometry geometry = (Geometry)object;
      if (geometry.isEmpty()) {
        return null;
      } else {
        final DataType dataType = GeometryDataTypes.GEOMETRY;
        final GeometryFactory geometryFactory = getGeometryFactory();
        return new PostgreSQLGeometryWrapper(dataType, geometryFactory, geometry);
      }
    } else if (object instanceof BoundingBox) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      BoundingBox boundingBox = (BoundingBox)object;
      boundingBox = boundingBox.bboxToCs(geometryFactory);
      return new PostgreSQLBoundingBoxWrapper(boundingBox);
    } else {
      return object;
    }
  }

}
