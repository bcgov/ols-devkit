package com.revolsys.oracle.recordstore.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordDefinition;
import com.revolsys.jdbc.io.JdbcRecordStoreSchema;
import com.revolsys.oracle.recordstore.OracleRecordStore;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;

public class OracleSdoGeometryFieldAdder extends JdbcFieldAdder {

  private static final Map<DataType, Integer> DATA_TYPE_TO_2D_ID = new HashMap<>();

  private static final Map<String, Integer> GEOMETRY_TYPE_TO_ID = new HashMap<>();

  private static final Map<Integer, DataType> ID_TO_DATA_TYPE = new HashMap<>();

  private static final Map<Integer, String> ID_TO_GEOMETRY_TYPE = new HashMap<>();

  public static final String ORACLE_SRID = "ORACLE_SRID";

  static {
    addGeometryType(GeometryDataTypes.GEOMETRY, "GEOMETRY", 0);
    addGeometryType(GeometryDataTypes.POINT, "POINT", 1);
    addGeometryType(GeometryDataTypes.LINE_STRING, "LINESTRING", 2);
    addGeometryType(GeometryDataTypes.POLYGON, "POLYGON", 3);
    addGeometryType(GeometryDataTypes.MULTI_POINT, "MULTIPOINT", 4);
    addGeometryType(GeometryDataTypes.MULTI_LINE_STRING, "MULTILINESTRING", 5);
    addGeometryType(GeometryDataTypes.MULTI_POLYGON, "MULTIPOLYGON", 6);
    addGeometryType(GeometryDataTypes.GEOMETRY_COLLECTION, "GEOMCOLLECTION", 7);
    addGeometryType(null, "CURVE", 13);
    addGeometryType(null, "SURFACE", 14);
    addGeometryType(null, "POLYHEDRALSURFACE", 15);
    addGeometryType(GeometryDataTypes.GEOMETRY, "GEOMETRYZ", 1000);
    addGeometryType(GeometryDataTypes.POINT, "POINTZ", 1001);
    addGeometryType(GeometryDataTypes.LINE_STRING, "LINESTRINGZ", 1002);
    addGeometryType(GeometryDataTypes.POLYGON, "POLYGONZ", 1003);
    addGeometryType(GeometryDataTypes.MULTI_POINT, "MULTIPOINTZ", 1004);
    addGeometryType(GeometryDataTypes.MULTI_LINE_STRING, "MULTILINESTRINGZ", 1005);
    addGeometryType(GeometryDataTypes.MULTI_POLYGON, "MULTIPOLYGONZ", 1006);
    addGeometryType(GeometryDataTypes.GEOMETRY_COLLECTION, "GEOMCOLLECTIONZ", 1007);
    addGeometryType(null, "CURVEZ", 1013);
    addGeometryType(null, "SURFACEZ", 1014);
    addGeometryType(null, "POLYHEDRALSURFACEZ", 1015);
    addGeometryType(GeometryDataTypes.GEOMETRY, "GEOMETRYM", 2000);
    addGeometryType(GeometryDataTypes.POINT, "POINTM", 2001);
    addGeometryType(GeometryDataTypes.LINE_STRING, "LINESTRINGM", 2002);
    addGeometryType(GeometryDataTypes.POLYGON, "POLYGONM", 2003);
    addGeometryType(GeometryDataTypes.MULTI_POINT, "MULTIPOINTM", 2004);
    addGeometryType(GeometryDataTypes.MULTI_LINE_STRING, "MULTILINESTRINGM", 2005);
    addGeometryType(GeometryDataTypes.MULTI_POLYGON, "MULTIPOLYGONM", 2006);
    addGeometryType(GeometryDataTypes.GEOMETRY_COLLECTION, "GEOMCOLLECTIONM", 2007);
    addGeometryType(null, "CURVEM", 2013);
    addGeometryType(null, "SURFACEM", 2014);
    addGeometryType(null, "POLYHEDRALSURFACEM", 2015);
    addGeometryType(GeometryDataTypes.GEOMETRY, "GEOMETRYZM", 3000);
    addGeometryType(GeometryDataTypes.POINT, "POINTZM", 3001);
    addGeometryType(GeometryDataTypes.LINE_STRING, "LINESTRINGZM", 3002);
    addGeometryType(GeometryDataTypes.POLYGON, "POLYGONZM", 3003);
    addGeometryType(GeometryDataTypes.MULTI_POINT, "MULTIPOINTZM", 3004);
    addGeometryType(GeometryDataTypes.MULTI_LINE_STRING, "MULTILINESTRINGZM", 3005);
    addGeometryType(GeometryDataTypes.MULTI_POLYGON, "MULTIPOLYGONZM", 3006);
    addGeometryType(GeometryDataTypes.GEOMETRY_COLLECTION, "GEOMCOLLECTIONZM", 3007);
    addGeometryType(null, "CURVEZM", 3013);
    addGeometryType(null, "SURFACEZM", 3014);
    addGeometryType(null, "POLYHEDRALSURFACEZM", 3015);
  }

  private static void addGeometryType(final DataType dataType, final String name,
    final Integer id) {
    ID_TO_GEOMETRY_TYPE.put(id, name);
    GEOMETRY_TYPE_TO_ID.put(name, id);
    ID_TO_DATA_TYPE.put(id, dataType);
    if (!DATA_TYPE_TO_2D_ID.containsKey(dataType)) {
      DATA_TYPE_TO_2D_ID.put(dataType, id);
    }
  }

  public static int getGeometryTypeId(final DataType dataType, final int axisCount) {
    final int id = DATA_TYPE_TO_2D_ID.get(dataType);
    if (axisCount > 3) {
      return 3000 + id;
    } else if (axisCount > 2) {
      return 1000 + id;
    } else {
      return id;
    }
  }

  private final OracleRecordStore recordStore;

  public OracleSdoGeometryFieldAdder(final OracleRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  protected double getScale(final Object[] values, final int axisIndex) throws SQLException {
    if (axisIndex >= values.length) {
      return 0;
    } else {
      final Struct dim = (Struct)values[axisIndex];
      final Object[] attributes = dim.getAttributes();
      final double precision = ((Number)attributes[3]).doubleValue();
      if (precision <= 0) {
        return 0;
      } else {
        return 1 / precision;
      }
    }
  }

  @Override
  public void initialize(final JdbcRecordStoreSchema schema) {
    try (
      Transaction transaction = this.recordStore.newTransaction(Propagation.REQUIRED);
      final JdbcConnection connection = this.recordStore.getJdbcConnection()) {
      final String schemaName = schema.getDbName();
      final String sridSql = "select M.TABLE_NAME, M.COLUMN_NAME, M.SRID, M.DIMINFO, C.GEOMETRY_TYPE "
        + "from ALL_SDO_GEOM_METADATA M "
        + "LEFT OUTER JOIN ALL_GEOMETRY_COLUMNS C ON (M.OWNER = C.F_TABLE_SCHEMA AND M.TABLE_NAME = C.F_TABLE_NAME AND M.COLUMN_NAME = C.F_GEOMETRY_COLUMN) "
        + "where OWNER = ?";
      try (
        final PreparedStatement statement = connection.prepareStatement(sridSql)) {
        statement.setString(1, schemaName);
        try (
          final ResultSet resultSet = statement.executeQuery()) {
          while (resultSet.next()) {
            final String tableName = resultSet.getString(1);
            final String columnName = resultSet.getString(2);
            final PathName typePath = schema.getPathName().newChild(tableName);

            int srid = resultSet.getInt(3);
            if (resultSet.wasNull() || srid < 0) {
              srid = 0;
            }
            final Object[] dimInfo = (Object[])resultSet.getArray("DIMINFO").getArray();
            int axisCount = dimInfo.length;
            setColumnProperty(schema, typePath, columnName, AXIS_COUNT, axisCount);
            if (axisCount < 2) {
              axisCount = 2;
            } else if (axisCount > 4) {
              axisCount = 4;
            }
            final double[] scales = new double[axisCount];
            for (int i = 0; i < scales.length; i++) {
              scales[i] = getScale(dimInfo, i);
            }
            final GeometryFactory geometryFactory = this.recordStore.getGeometryFactory(srid,
              axisCount, scales);
            setColumnProperty(schema, typePath, columnName, GEOMETRY_FACTORY, geometryFactory);

            setColumnProperty(schema, typePath, columnName, ORACLE_SRID, srid);

            final int geometryType = resultSet.getInt(5);
            DataType geometryDataType;
            if (resultSet.wasNull()) {
              geometryDataType = GeometryDataTypes.GEOMETRY;
            } else {
              geometryDataType = ID_TO_DATA_TYPE.get(geometryType);
              if (geometryDataType == null) {
                geometryDataType = GeometryDataTypes.GEOMETRY;
              }
            }
            setColumnProperty(schema, typePath, columnName, GEOMETRY_TYPE, geometryDataType);
          }
        }
      } catch (final SQLException e) {
        Logs.error(this, "Unable to initialize", e);
      }
    }
  }

  @Override
  public JdbcFieldDefinition newField(final AbstractJdbcRecordStore recordStore,
    final JdbcRecordDefinition recordDefinition, final String dbName, final String name,
    final String dbDataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final PathName typePath = recordDefinition.getPathName();
    final String columnName = name.toUpperCase();
    final RecordStoreSchema schema = recordDefinition.getSchema();

    GeometryFactory geometryFactory = getColumnProperty(schema, typePath, columnName,
      GEOMETRY_FACTORY);
    if (geometryFactory == null) {
      geometryFactory = schema.getGeometryFactory();
    }
    if (geometryFactory == null) {
      geometryFactory = GeometryFactory.DEFAULT_2D;
    }
    DataType dataType = getColumnProperty(schema, typePath, columnName, GEOMETRY_TYPE);
    if (dataType == null) {
      dataType = GeometryDataTypes.GEOMETRY;
    }

    int axisCount = getIntegerColumnProperty(schema, typePath, columnName, AXIS_COUNT);
    if (axisCount == -1) {
      axisCount = geometryFactory.getAxisCount();
    }
    int oracleSrid = getIntegerColumnProperty(schema, typePath, columnName, ORACLE_SRID);
    if (oracleSrid == -1) {
      oracleSrid = 0;
    }
    final OracleSdoGeometryJdbcFieldDefinition field = new OracleSdoGeometryJdbcFieldDefinition(
      dbName, name, dataType, sqlType, required, description, null, geometryFactory, axisCount,
      oracleSrid);
    return field;
  }
}
