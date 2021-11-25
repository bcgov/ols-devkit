package com.revolsys.gis.postgresql.type;

import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.postgresql.PostgreSQLRecordStore;
import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordDefinition;
import com.revolsys.jdbc.io.JdbcRecordStoreSchema;
import com.revolsys.util.Property;

public class PostgreSQLGeometryFieldAdder extends JdbcFieldAdder {
  private static final Map<String, DataType> DATA_TYPE_MAP = new HashMap<>();

  static {
    DATA_TYPE_MAP.put("GEOMETRY", GeometryDataTypes.GEOMETRY);
    DATA_TYPE_MAP.put("POINT", GeometryDataTypes.POINT);
    DATA_TYPE_MAP.put("LINESTRING", GeometryDataTypes.LINE_STRING);
    DATA_TYPE_MAP.put("POLYGON", GeometryDataTypes.POLYGON);
    DATA_TYPE_MAP.put("MULTIPOINT", GeometryDataTypes.MULTI_POINT);
    DATA_TYPE_MAP.put("MULTILINESTRING", GeometryDataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put("MULTIPOLYGON", GeometryDataTypes.MULTI_POLYGON);
  }

  private final PostgreSQLRecordStore recordStore;

  public PostgreSQLGeometryFieldAdder(final PostgreSQLRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  @Override
  public JdbcFieldDefinition newField(final AbstractJdbcRecordStore recordStore,
    final JdbcRecordDefinition recordDefinition, final String dbName, final String name,
    final String dbDataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final JdbcRecordStoreSchema schema = recordDefinition.getSchema();
    final PathName typePath = recordDefinition.getPathName();
    String dbSchemaName = schema.getDbName();
    if (!Property.hasValue(dbSchemaName)) {
      dbSchemaName = "public";
    }
    final String tableName = recordDefinition.getDbTableName().replace("\"", "");
    final String columnName = name.toLowerCase();
    try {
      int srid = 0;
      String type = "geometry";
      int axisCount = 3;
      try {
        final String sql = "select SRID, TYPE, COORD_DIMENSION from GEOMETRY_COLUMNS where UPPER(F_TABLE_SCHEMA) = UPPER(?) AND UPPER(F_TABLE_NAME) = UPPER(?) AND UPPER(F_GEOMETRY_COLUMN) = UPPER(?)";
        final Map<String, Object> values = this.recordStore.selectMap(sql, dbSchemaName, tableName,
          columnName);
        srid = (Integer)values.get("srid");
        type = (String)values.get("type");
        axisCount = (Integer)values.get("coord_dimension");
      } catch (final IllegalArgumentException e) {
        Logs.warn(this, "Cannot get geometry column metadata for " + typePath + "." + columnName);
      }

      final DataType dataType = DATA_TYPE_MAP.get(type);
      final GeometryFactory storeGeometryFactory = this.recordStore.getGeometryFactory();
      GeometryFactory geometryFactory = GeometryFactory.floating(srid, axisCount);
      if (storeGeometryFactory != null) {
        if (storeGeometryFactory.isSameCoordinateSystem(geometryFactory)) {
          final double[] scales = storeGeometryFactory.newScales(axisCount);
          geometryFactory = storeGeometryFactory.convertAxisCountAndScales(axisCount, scales);
        }
      }
      final PostgreSQLGeometryJdbcFieldDefinition field = new PostgreSQLGeometryJdbcFieldDefinition(
        dbName, name, dataType, sqlType, required, description, null, srid, axisCount,
        geometryFactory);
      field.setGeometryFactory(geometryFactory);
      return field;
    } catch (final Throwable e) {
      Logs.error(this, "Attribute not registered in GEOMETRY_COLUMN table " + dbSchemaName + "."
        + tableName + "." + name, e);
      return null;
    }
  }

}
