package com.revolsys.oracle.recordstore.esri;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.oracle.recordstore.OracleRecordStore;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.RecordStoreSchema;

public final class ArcSdeConstants {

  public static final int COLLECTION = 6;

  public static final int CURVE = 2;

  public static final Map<Integer, DataType> DATA_TYPE_MAP = new HashMap<>();

  public static final String ESRI_SRID_PROPERTY = "esriSrid";

  public static final int GEOMETRY = 0;

  public static String GEOMETRY_COLUMN_TYPE = "geometryColumnType";

  public static final Map<DataType, Integer> GEOMETRY_DATA_TYPE_ST_TYPE = new HashMap<>();

  public static final int LINESTRING = 3;

  public static final int MULT_SURFACE = 10;

  public static final int MULTI_CURVE = 8;

  public static final int MULTI_LINESTRING = 9;

  public static final int MULTI_POINT = 7;

  public static final int MULTI_POLYGON = 11;

  public static final int POINT = 1;

  public static final int POLYGON = 5;

  public static final String REGISTRATION_ID = "REGISTRATION_ID";

  public static final String ROWID_COLUMN = "ROWID_COLUMN";

  public static final String SDEBINARY = "SDEBINARY";

  public static final String SPATIAL_REFERENCE = "spatialReference";

  public static final int ST_GEOMETRY_LINESTRING = 4;

  public static final int ST_GEOMETRY_MULTI_LINESTRING = 260;

  public static final int ST_GEOMETRY_MULTI_POINT = 257;

  public static final int ST_GEOMETRY_MULTI_POLYGON = 264;

  public static final int ST_GEOMETRY_POINT = 1;

  public static final int ST_GEOMETRY_POLYGON = 8;

  public static final int SURFACE = 4;

  static {
    DATA_TYPE_MAP.put(POINT, GeometryDataTypes.POINT);
    DATA_TYPE_MAP.put(LINESTRING, GeometryDataTypes.LINE_STRING);
    DATA_TYPE_MAP.put(POLYGON, GeometryDataTypes.POLYGON);
    DATA_TYPE_MAP.put(MULTI_POINT, GeometryDataTypes.MULTI_POINT);
    DATA_TYPE_MAP.put(MULTI_LINESTRING, GeometryDataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put(MULTI_POLYGON, GeometryDataTypes.MULTI_POLYGON);

    GEOMETRY_DATA_TYPE_ST_TYPE.put(GeometryDataTypes.POINT, ST_GEOMETRY_POINT);
    GEOMETRY_DATA_TYPE_ST_TYPE.put(GeometryDataTypes.MULTI_POINT, ST_GEOMETRY_MULTI_POINT);
    GEOMETRY_DATA_TYPE_ST_TYPE.put(GeometryDataTypes.LINE_STRING, ST_GEOMETRY_LINESTRING);
    GEOMETRY_DATA_TYPE_ST_TYPE.put(GeometryDataTypes.MULTI_LINE_STRING,
      ST_GEOMETRY_MULTI_LINESTRING);
    GEOMETRY_DATA_TYPE_ST_TYPE.put(GeometryDataTypes.POLYGON, ST_GEOMETRY_POLYGON);
    GEOMETRY_DATA_TYPE_ST_TYPE.put(GeometryDataTypes.MULTI_POLYGON, ST_GEOMETRY_MULTI_POLYGON);
  }

  public static DataType getGeometryDataType(final int geometryType) {
    final DataType dataType = DATA_TYPE_MAP.get(geometryType);
    if (dataType == null) {
      return GeometryDataTypes.GEOMETRY;
    } else {
      return dataType;
    }
  }

  public static Integer getStGeometryType(final Geometry geometry) {
    final DataType dataType = geometry.getDataType();
    final Integer type = GEOMETRY_DATA_TYPE_ST_TYPE.get(dataType);
    if (type == null) {
      throw new IllegalArgumentException("Unsupported geometry type " + dataType);
    } else {
      return type;
    }
  }

  public static boolean isSdeAvailable(final RecordStore recordStore) {
    if (recordStore instanceof OracleRecordStore) {
      final OracleRecordStore oracleRecordStore = (OracleRecordStore)recordStore;
      final Set<String> allSchemaNames = oracleRecordStore.getAllSchemaNames();
      return allSchemaNames.contains("SDE");
    }
    return false;
  }

  public static boolean isSdeAvailable(final RecordStoreSchema schema) {
    final RecordStore recordStore = schema.getRecordStore();

    return isSdeAvailable(recordStore);
  }
}
