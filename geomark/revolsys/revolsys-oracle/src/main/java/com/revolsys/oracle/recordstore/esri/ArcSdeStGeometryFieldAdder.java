package com.revolsys.oracle.recordstore.esri;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordDefinition;
import com.revolsys.jdbc.io.JdbcRecordStoreSchema;

public class ArcSdeStGeometryFieldAdder extends JdbcFieldAdder {
  public ArcSdeStGeometryFieldAdder(final AbstractJdbcRecordStore recordStore) {

  }

  @Override
  public ArcSdeStGeometryFieldDefinition newField(final AbstractJdbcRecordStore recordStore,
    final JdbcRecordDefinition recordDefinition, final String dbName, final String name,
    final String dbDataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final JdbcRecordStoreSchema schema = recordDefinition.getSchema();
    final PathName typePath = recordDefinition.getPathName();
    final String owner = schema.getDbName();
    final String tableName = recordDefinition.getDbTableName();
    final String columnName = name.toUpperCase();
    final int esriSrid = JdbcFieldAdder.getIntegerColumnProperty(schema, typePath, columnName,
      ArcSdeConstants.ESRI_SRID_PROPERTY);
    if (esriSrid == -1) {
      Logs.error(this,
        "Column not registered in SDE.ST_GEOMETRY table " + owner + "." + tableName + "." + name);
    }
    final int axisCount = JdbcFieldAdder.getIntegerColumnProperty(schema, typePath, columnName,
      JdbcFieldAdder.AXIS_COUNT);
    if (axisCount == -1) {
      Logs.error(this,
        "Column not found in SDE.GEOMETRY_COLUMNS table " + owner + "." + tableName + "." + name);
    }
    final DataType dataType = JdbcFieldAdder.getColumnProperty(schema, typePath, columnName,
      JdbcFieldAdder.GEOMETRY_TYPE);
    if (dataType == null) {
      Logs.error(this,
        "Column not found in SDE.GEOMETRY_COLUMNS table " + owner + "." + tableName + "." + name);
    }

    final ArcSdeSpatialReference spatialReference = JdbcFieldAdder.getColumnProperty(schema,
      typePath, columnName, ArcSdeConstants.SPATIAL_REFERENCE);

    final GeometryFactory geometryFactory = JdbcFieldAdder.getColumnProperty(schema, typePath,
      columnName, JdbcFieldAdder.GEOMETRY_FACTORY);

    final ArcSdeStGeometryFieldDefinition field = new ArcSdeStGeometryFieldDefinition(dbName, name,
      dataType, required, description, null, spatialReference, axisCount);
    field.setGeometryFactory(geometryFactory);
    return field;
  }

}
