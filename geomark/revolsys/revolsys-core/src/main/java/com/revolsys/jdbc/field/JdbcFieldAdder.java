package com.revolsys.jdbc.field;

import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.io.PathName;

import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordDefinition;
import com.revolsys.jdbc.io.JdbcRecordStoreSchema;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordStoreSchema;

public class JdbcFieldAdder {
  public static String AXIS_COUNT = "axisCount";

  public static final String COLUMN_PROPERTIES = "columnProperties";

  public static String GEOMETRY_FACTORY = "geometryFactory";

  public static String GEOMETRY_TYPE = "geometryType";

  public static final String TABLE_PROPERTIES = "tableProperties";

  public static Map<PathName, Map<String, Map<String, Object>>> getColumnProperties(
    final RecordStoreSchema schema) {
    synchronized (schema) {
      Map<PathName, Map<String, Map<String, Object>>> columnProperties = schema
        .getProperty(COLUMN_PROPERTIES);
      if (columnProperties == null) {
        columnProperties = new HashMap<>();
        schema.setProperty(COLUMN_PROPERTIES, columnProperties);
      }
      return columnProperties;
    }
  }

  @SuppressWarnings("unchecked")
  public static <V> V getColumnProperty(final RecordStoreSchema schema, final PathName typePath,
    final String columnName, final String propertyName) {
    final Map<String, Map<String, Object>> columnsProperties = getTypeColumnProperties(schema,
      typePath);
    final Map<String, Object> properties = columnsProperties.get(columnName);
    if (properties != null) {
      final Object value = properties.get(propertyName);
      return (V)value;
    }
    return null;
  }

  public static double getDoubleColumnProperty(final RecordStoreSchema schema,
    final PathName typePath, final String columnName, final String propertyName) {
    final Object value = getColumnProperty(schema, typePath, columnName, propertyName);
    if (value instanceof Number) {
      final Number number = (Number)value;
      return number.doubleValue();
    } else {
      return 11;
    }
  }

  public static int getIntegerColumnProperty(final RecordStoreSchema schema,
    final PathName typePath, final String columnName, final String propertyName) {
    final Object value = getColumnProperty(schema, typePath, columnName, propertyName);
    if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      return -1;
    }
  }

  public static Map<String, Map<String, Object>> getTableProperties(
    final RecordStoreSchema schema) {
    synchronized (schema) {
      Map<String, Map<String, Object>> tableProperties = schema.getProperty(TABLE_PROPERTIES);
      if (tableProperties == null) {
        tableProperties = new HashMap<>();
        schema.setProperty(TABLE_PROPERTIES, tableProperties);
      }
      return tableProperties;
    }
  }

  public static Map<String, Object> getTableProperties(final RecordStoreSchema schema,
    final String typePath) {
    final Map<String, Map<String, Object>> tableProperties = getTableProperties(schema);
    synchronized (tableProperties) {
      Map<String, Object> properties = tableProperties.get(typePath);
      if (properties == null) {
        properties = new HashMap<>();
        tableProperties.put(typePath, properties);
      }
      return properties;
    }
  }

  @SuppressWarnings("unchecked")
  public static <V> V getTableProperty(final RecordStoreSchema schema, final String typePath,
    final String propertyName) {
    final Map<String, Object> properties = getTableProperties(schema, typePath);
    final Object value = properties.get(propertyName);
    return (V)value;
  }

  public static Map<String, Map<String, Object>> getTypeColumnProperties(
    final RecordStoreSchema schema, final PathName typePath) {
    final Map<PathName, Map<String, Map<String, Object>>> esriColumnProperties = getColumnProperties(
      schema);
    final Map<String, Map<String, Object>> typeColumnProperties = esriColumnProperties
      .get(typePath);
    if (typeColumnProperties == null) {
      return Collections.emptyMap();
    } else {
      return typeColumnProperties;
    }
  }

  public static void setColumnProperty(final RecordStoreSchema schema, final PathName typePath,
    final String columnName, final String propertyName, final Object propertyValue) {
    final Map<PathName, Map<String, Map<String, Object>>> tableColumnProperties = getColumnProperties(
      schema);
    synchronized (tableColumnProperties) {

      Map<String, Map<String, Object>> typeColumnMap = tableColumnProperties.get(typePath);
      if (typeColumnMap == null) {
        typeColumnMap = new HashMap<>();
        tableColumnProperties.put(typePath, typeColumnMap);
      }
      Map<String, Object> columnProperties = typeColumnMap.get(columnName);
      if (columnProperties == null) {
        columnProperties = new HashMap<>();
        typeColumnMap.put(columnName, columnProperties);
      }
      columnProperties.put(propertyName, propertyValue);
    }
  }

  public static void setTableProperty(final RecordStoreSchema schema, final String typePath,
    final String propertyName, final Object value) {
    final Map<String, Object> properties = getTableProperties(schema, typePath);
    properties.put(propertyName, value);
  }

  private DataType dataType;

  public JdbcFieldAdder() {
  }

  public JdbcFieldAdder(final DataType dataType) {
    this.dataType = dataType;
  }

  public FieldDefinition addField(final AbstractJdbcRecordStore recordStore,
    final JdbcRecordDefinition recordDefinition, final String dbName, final String name,
    final String dataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final JdbcFieldDefinition field = newField(recordStore, recordDefinition, dbName, name,
      dataType, sqlType, length, scale, required, description);
    field.setQuoteName(recordStore.isQuoteNames());
    recordDefinition.addField(field);
    return field;
  }

  public void initialize(final JdbcRecordStoreSchema schema) {
  }

  public JdbcFieldDefinition newField(final AbstractJdbcRecordStore recordStore,
    final JdbcRecordDefinition recordDefinition, final String dbName, final String name,
    final String dbDataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    JdbcFieldDefinition field;
    if (dbDataType.equals("oid")) {
      field = new JdbcBlobFieldDefinition(dbName, name, sqlType, length, required, description,
        null);
    } else {
      switch (sqlType) {
        case Types.CHAR:
        case Types.CLOB:
        case Types.LONGVARCHAR:
        case Types.VARCHAR:
          field = new JdbcStringFieldDefinition(dbName, name, sqlType, length, required,
            description, null);
        break;
        case Types.BIGINT:
          field = new JdbcLongFieldDefinition(dbName, name, sqlType, required, description, null);
        break;
        case Types.INTEGER:
          field = new JdbcIntegerFieldDefinition(dbName, name, sqlType, required, description,
            null);
        break;
        case Types.SMALLINT:
          field = new JdbcShortFieldDefinition(dbName, name, sqlType, required, description, null);
        break;
        case Types.TINYINT:
          field = new JdbcByteFieldDefinition(dbName, name, sqlType, required, description, null);
        break;
        case Types.DOUBLE:
          field = new JdbcDoubleFieldDefinition(dbName, name, sqlType, required, description, null);
        break;
        case Types.REAL:
          field = new JdbcFloatFieldDefinition(dbName, name, sqlType, required, description, null);
        break;
        case Types.DECIMAL:
        case Types.NUMERIC:
        case Types.FLOAT:
          if (scale > 0) {
            field = new JdbcBigDecimalFieldDefinition(dbName, name, sqlType, length, scale,
              required, description, null);
          } else if (length == 131089 || length == 0) {
            field = new JdbcBigDecimalFieldDefinition(dbName, name, sqlType, -1, -1, required,
              description, null);
          } else {
            if (length <= 2) {
              field = new JdbcByteFieldDefinition(dbName, name, sqlType, required, description,
                null);
            } else if (length <= 4) {
              field = new JdbcShortFieldDefinition(dbName, name, sqlType, required, description,
                null);
            } else if (length <= 9) {
              field = new JdbcIntegerFieldDefinition(dbName, name, sqlType, required, description,
                null);
            } else if (length <= 18) {
              field = new JdbcLongFieldDefinition(dbName, name, sqlType, required, description,
                null);
            } else {
              field = new JdbcBigIntegerFieldDefinition(dbName, name, sqlType, length, required,
                description, null);
            }
          }
        break;
        case Types.DATE:
          field = new JdbcDateFieldDefinition(dbName, name, sqlType, required, description, null);
        break;
        case Types.TIMESTAMP:
        case Types.TIMESTAMP_WITH_TIMEZONE:
          field = new JdbcTimestampFieldDefinition(dbName, name, sqlType, required, description,
            null);
        break;
        case Types.BIT:
          field = new JdbcBooleanFieldDefinition(dbName, name, sqlType, length, required,
            description, null);
        break;
        case Types.BLOB:
          field = new JdbcBlobFieldDefinition(dbName, name, sqlType, length, required, description,
            null);
        break;
        default:
          field = new JdbcFieldDefinition(dbName, name, this.dataType, sqlType, length, scale,
            required, description, null);
        break;
      }
    }
    return field;
  }
}
