package com.revolsys.geopackage.field;

import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geopackage.GeoPackageRecordStore;
import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordDefinition;
import com.revolsys.record.schema.FieldDefinition;

public class GeoPackageGeometryFieldAdder extends JdbcFieldAdder {
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

  public GeoPackageGeometryFieldAdder() {
  }

  @Override
  public FieldDefinition addField(final AbstractJdbcRecordStore recordStore,
    final JdbcRecordDefinition recordDefinition, final String dbName, final String name,
    final String columnDataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final PathName typePath = recordDefinition.getPathName();
    final String tableName = recordDefinition.getDbTableName();
    final String columnName = name.toLowerCase();
    try {
      int srid = 0;
      String type = "geometry";
      int axisCount = 2;
      try {
        final String sql = "select geometry_type_name, srs_id, Z, M from gpkg_geometry_columns where UPPER(TABLE_NAME) = UPPER(?) AND UPPER(COLUMN_NAME) = UPPER(?)";
        final MapEx values = ((GeoPackageRecordStore)recordStore).selectMapNoFunctions(sql,
          tableName, columnName);
        srid = values.getInteger("srs_id", 0);
        type = values.getString("geometry_type_name", "GEOMETRY");
        if (values.getInteger("z", 0) > 0) {
          axisCount = 3;
        }
        if (values.getInteger("m", 0) > 0) {
          axisCount = 4;
        }
      } catch (final IllegalArgumentException e) {
        Logs.warn(this, "Cannot get geometry column metadata for " + typePath + "." + columnName);
      }

      final DataType dataType = DATA_TYPE_MAP.get(type);
      final GeometryFactory recordDefinitionGeometryFactory = recordDefinition.getGeometryFactory();
      final GeometryFactory geometryFactory;
      if (recordDefinitionGeometryFactory == null) {
        geometryFactory = GeometryFactory.floating(srid, axisCount);
      } else {
        final double[] scales = recordDefinitionGeometryFactory.newScales(axisCount);
        geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);
      }
      final FieldDefinition field = new GeoPackageGeometryJdbcFieldDefinition(dbName, name,
        dataType, required, description, null, srid, axisCount, geometryFactory);
      recordDefinition.addField(field);
      return field;
    } catch (final Throwable e) {
      Logs.error(this,
        "Attribute not registered in GEOMETRY_COLUMN table " + tableName + "." + name, e);
      return null;
    }
  }
}
