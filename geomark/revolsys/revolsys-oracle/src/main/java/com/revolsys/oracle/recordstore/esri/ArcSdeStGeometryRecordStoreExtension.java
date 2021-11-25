package com.revolsys.oracle.recordstore.esri;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.io.JdbcRecordStoreSchema;
import com.revolsys.oracle.recordstore.OracleRecordStore;
import com.revolsys.record.io.RecordStoreExtension;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.util.Property;

public class ArcSdeStGeometryRecordStoreExtension implements RecordStoreExtension {

  public ArcSdeStGeometryRecordStoreExtension() {
  }

  @Override
  public void initialize(final RecordStore recordStore,
    final Map<String, Object> connectionProperties) {
    final OracleRecordStore oracleRecordStore = (OracleRecordStore)recordStore;
    final JdbcFieldAdder stGeometryAttributeAdder = new ArcSdeStGeometryFieldAdder(
      oracleRecordStore);
    oracleRecordStore.addFieldAdder("ST_GEOMETRY", stGeometryAttributeAdder);
    oracleRecordStore.addFieldAdder("SDE.ST_GEOMETRY", stGeometryAttributeAdder);
  }

  @Override
  public boolean isEnabled(final RecordStore recordStore) {
    return ArcSdeConstants.isSdeAvailable(recordStore);
  }

  private void loadColumnProperties(final RecordStoreSchema schema, final String schemaName,
    final Connection connection) throws SQLException {
    final String sql = "SELECT GC.F_TABLE_NAME, GC.F_GEOMETRY_COLUMN, GC.SRID, GC.GEOMETRY_TYPE, GC.COORD_DIMENSION, SG.GEOMETRY_TYPE GEOMETRY_DATA_TYPE FROM SDE.GEOMETRY_COLUMNS GC LEFT OUTER JOIN SDE.ST_GEOMETRY_COLUMNS SG ON GC.F_TABLE_SCHEMA = SG.OWNER AND GC.F_TABLE_NAME = SG.TABLE_NAME WHERE GC.F_TABLE_SCHEMA = ?";
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      statement.setString(1, schemaName);
      final ResultSet resultSet = statement.executeQuery();
      try {
        while (resultSet.next()) {
          PathName typePath = null;
          String columnName = null;
          try {
            final String tableName = resultSet.getString(1);
            columnName = resultSet.getString(2);

            typePath = PathName.newPathName(PathUtil.toPath(schemaName, tableName));

            final int esriSrid = resultSet.getInt(3);
            JdbcFieldAdder.setColumnProperty(schema, typePath, columnName,
              ArcSdeConstants.ESRI_SRID_PROPERTY, esriSrid);

            int axisCount = resultSet.getInt(5);
            axisCount = Math.max(axisCount, 2);
            JdbcFieldAdder.setColumnProperty(schema, typePath, columnName,
              JdbcFieldAdder.AXIS_COUNT, axisCount);

            try {
              final ArcSdeSpatialReference spatialReference = ArcSdeSpatialReferenceCache
                .getSpatialReference(connection, schema, esriSrid);
              JdbcFieldAdder.setColumnProperty(schema, typePath, columnName,
                ArcSdeConstants.SPATIAL_REFERENCE, spatialReference);

              GeometryFactory geometryFactory = JdbcFieldAdder.getColumnProperty(schema, typePath,
                columnName, JdbcFieldAdder.GEOMETRY_FACTORY);
              int srid = spatialReference.getCoordinateSystemId();
              if (srid <= 0) {
                srid = geometryFactory.getHorizontalCoordinateSystemId();
              }
              axisCount = Math.min(axisCount, 3);
              final double[] scales = spatialReference.getGeometryFactory().newScales(axisCount);
              geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);

              JdbcFieldAdder.setColumnProperty(schema, typePath, columnName,
                JdbcFieldAdder.GEOMETRY_FACTORY, geometryFactory);
            } catch (final Exception e) {
              Logs.error(this, "Invalid spatial reference for " + typePath + "." + columnName, e);
            }

            final int geometryType = resultSet.getInt(4);
            JdbcFieldAdder.setColumnProperty(schema, typePath, columnName,
              JdbcFieldAdder.GEOMETRY_TYPE, ArcSdeConstants.getGeometryDataType(geometryType));

            String geometryColumnType = resultSet.getString(6);
            if (!Property.hasValue(geometryColumnType)) {
              geometryColumnType = ArcSdeConstants.SDEBINARY;
            }
            JdbcFieldAdder.setColumnProperty(schema, typePath, columnName,
              ArcSdeConstants.GEOMETRY_COLUMN_TYPE, geometryColumnType);
          } catch (final Exception e) {
            Logs.error(this, "Invalid geometry configuration for " + typePath + "." + columnName,
              e);
          }
        }
      } finally {
        JdbcUtils.close(resultSet);
      }
    } finally {
      JdbcUtils.close(statement);
    }
  }

  private void loadTableProperties(final Connection connection, final RecordStoreSchema schema,
    final String schemaName) {
    final String sql = "SELECT registration_id, table_name, rowid_column FROM sde.table_registry WHERE owner = ?";
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.prepareStatement(sql);
      statement.setString(1, schemaName);
      resultSet = statement.executeQuery();
      while (resultSet.next()) {
        final String tableName = resultSet.getString(2);
        final String typePath = PathUtil.toPath(schemaName, tableName).toUpperCase();

        final int registrationId = resultSet.getInt(1);
        JdbcFieldAdder.setTableProperty(schema, typePath, ArcSdeConstants.REGISTRATION_ID,
          registrationId);

        final String rowidColumn = resultSet.getString(3);
        JdbcFieldAdder.setTableProperty(schema, typePath, ArcSdeConstants.ROWID_COLUMN,
          rowidColumn);
      }
    } catch (final Throwable e) {
      Logs.error(this, "Unable to load rowid columns for " + schemaName, e);
    } finally {
      JdbcUtils.close(statement, resultSet);
    }
  }

  @Override
  public void postProcess(final RecordStoreSchema schema) {
    final String schemaName = schema.getName();
    for (final RecordDefinition recordDefinition : schema.getRecordDefinitions()) {
      final String typePath = recordDefinition.getPath();
      final Integer registrationId = JdbcFieldAdder.getTableProperty(schema, typePath,
        ArcSdeConstants.REGISTRATION_ID);
      final String rowIdColumn = JdbcFieldAdder.getTableProperty(schema, typePath,
        ArcSdeConstants.ROWID_COLUMN);
      if (registrationId != null && rowIdColumn != null) {
        ArcSdeObjectIdJdbcFieldDefinition.replaceAttribute(schemaName, recordDefinition,
          registrationId, rowIdColumn);
      }
    }
  }

  @Override
  public void preProcess(final RecordStoreSchema schema) {
    final JdbcRecordStoreSchema jdbcSchema = (JdbcRecordStoreSchema)schema;
    final RecordStore recordStore = schema.getRecordStore();
    final OracleRecordStore oracleRecordStore = (OracleRecordStore)recordStore;
    try {
      try (
        final Connection connection = oracleRecordStore.getJdbcConnection()) {
        final String schemaName = jdbcSchema.getDbName();
        loadTableProperties(connection, schema, schemaName);
        loadColumnProperties(schema, schemaName, connection);
      }
    } catch (final Throwable e) {
      Logs.error(this, "Unable to get ArcSDE metadata for schema " + schema.getName(), e);
    }
  }
}
