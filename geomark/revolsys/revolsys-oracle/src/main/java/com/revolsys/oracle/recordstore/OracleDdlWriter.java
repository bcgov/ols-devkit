package com.revolsys.oracle.recordstore;

import java.io.PrintWriter;
import java.util.List;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.io.JdbcDdlWriter;
import com.revolsys.oracle.recordstore.field.OracleSdoGeometryFieldAdder;
import com.revolsys.record.Record;
import com.revolsys.record.property.ShortNameProperty;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public class OracleDdlWriter extends JdbcDdlWriter {
  public OracleDdlWriter() {
  }

  public OracleDdlWriter(final PrintWriter out) {
    super(out);
  }

  @Override
  public String getSequenceName(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    final ShortNameProperty shortNameProperty = ShortNameProperty.getProperty(recordDefinition);
    String shortName = null;
    if (shortNameProperty != null) {
      shortName = shortNameProperty.getShortName();
    }
    if (Property.hasValue(shortName) && shortNameProperty.isUseForSequence()) {
      final String schema = JdbcUtils.getSchemaName(typePath);
      final String sequenceName = schema + "." + shortName.toUpperCase() + "_SEQ";
      return sequenceName;
    } else {
      final String tableName = JdbcUtils.getQualifiedTableName(typePath).toUpperCase();
      return tableName + "_SEQ";
    }
  }

  public void writeAddGeometryColumn(final RecordDefinition recordDefinition) {
    final PrintWriter out = getOut();
    final String typePath = recordDefinition.getPath();
    String schemaName = JdbcUtils.getSchemaName(typePath);
    if (schemaName.length() == 0) {
      schemaName = "public";
    }
    final String tableName = PathUtil.getName(typePath);
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    if (geometryField != null) {
      final GeometryFactory geometryFactory = geometryField.getGeometryFactory();
      final String name = geometryField.getName();
      String geometryType = "GEOMETRY";
      final DataType dataType = geometryField.getDataType();
      if (dataType == GeometryDataTypes.POINT) {
        geometryType = "POINT";
      } else if (dataType == GeometryDataTypes.LINE_STRING) {
        geometryType = "LINESTRING";
      } else if (dataType == GeometryDataTypes.POLYGON) {
        geometryType = "POLYGON";
      } else if (dataType == GeometryDataTypes.MULTI_POINT) {
        geometryType = "MULTIPOINT";
      } else if (dataType == GeometryDataTypes.MULTI_LINE_STRING) {
        geometryType = "MULTILINESTRING";
      } else if (dataType == GeometryDataTypes.MULTI_POLYGON) {
        geometryType = "MULTIPOLYGON";
      }
      out.print("select addgeometrycolumn('");
      out.print(schemaName.toLowerCase());
      out.print("', '");
      out.print(tableName.toLowerCase());
      out.print("','");
      out.print(name.toLowerCase());
      out.print("',");
      out.print(geometryFactory.getHorizontalCoordinateSystemId());
      out.print(",'");
      out.print(geometryType);
      out.print("', ");
      out.print(geometryFactory.getAxisCount());
      out.println(");");

    }
  }

  public void writeAlterOwner(final String objectType, final String objectName,
    final String owner) {
    final PrintWriter out = getOut();
    out.print("ALTER ");
    out.print(objectType);
    out.print(" ");
    out.print(objectName);
    out.print(" OWNER TO ");
    out.print(owner);
    out.println(";");
  }

  public void writeAlterTableOwner(final String typePath, final String owner) {
    final PrintWriter out = getOut();
    out.print("ALTER ");
    final String objectType = "TABLE";
    out.print(objectType);
    out.print(" ");
    writeTableName(typePath);
    out.print(" OWNER TO ");
    out.print(owner);
    out.println(";");
  }

  @Override
  public void writeColumnDataType(final FieldDefinition attribute) {
    final PrintWriter out = getOut();
    final DataType dataType = attribute.getDataType();
    if (dataType == DataTypes.BOOLEAN) {
      out.print("NUMBER(1,0)");
    } else if (dataType == DataTypes.BYTE) {
      out.print("NUMBER(3)");
    } else if (dataType == DataTypes.SHORT) {
      out.print("NUMBER(5)");
    } else if (dataType == DataTypes.INT) {
      out.print("NUMBER(10)");
    } else if (dataType == DataTypes.LONG) {
      out.print("NUMBER(19)");
    } else if (dataType == DataTypes.FLOAT) {
      out.print("float");
    } else if (dataType == DataTypes.DOUBLE) {
      out.print("double precision");
    } else if (dataType == DataTypes.SQL_DATE) {
      out.print("DATE");
    } else if (dataType == DataTypes.DATE_TIME || dataType == DataTypes.UTIL_DATE) {
      out.print("TIMESTAMP");
    } else if (dataType == DataTypes.STRING) {
      out.print("VARCHAR2(");
      out.print(attribute.getLength());
      out.print(")");
    } else if (dataType == DataTypes.BIG_INTEGER) {
      out.print("NUMBER(");
      out.print(attribute.getLength());
      out.print(')');
    } else if (dataType == DataTypes.DECIMAL) {
      out.print("NUMBER(");
      out.print(attribute.getLength());
      final int scale = attribute.getScale();
      if (scale >= 0) {
        out.print(',');
        out.print(scale);
      }
      out.print(')');
    } else if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
      out.print("MDSYS.SDO_GEOMETRY");
    } else {
      throw new IllegalArgumentException("Unknown data type" + dataType);
    }
  }

  @Override
  public String writeCreateSequence(final RecordDefinition recordDefinition) {
    final String sequenceName = getSequenceName(recordDefinition);
    writeCreateSequence(sequenceName);
    return sequenceName;
  }

  @Override
  public void writeGeometryRecordDefinition(final RecordDefinition recordDefinition) {
    final PrintWriter out = getOut();
    final String typePath = recordDefinition.getPath();
    final String schemaName = JdbcUtils.getSchemaName(typePath);
    final String tableName = PathUtil.getName(typePath);
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    if (geometryField != null) {
      final GeometryFactory geometryFactory = geometryField.getGeometryFactory();
      final String name = geometryField.getName();
      final int axisCount = geometryFactory.getAxisCount();
      final DataType dataType = geometryField.getDataType();
      final int srid = geometryFactory.getHorizontalCoordinateSystemId();

      out.print(
        "INSERT INTO USER_SDO_GEOM_METADATA(TABLE_NAME, COLUMN_NAME, DIMINFO, SRID) VALUES ('");
      out.print(tableName.toUpperCase());
      out.print("','");
      out.print(name.toUpperCase());
      // TODO get from geometry factory
      out.print(
        "',MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 263000, 1876000, 0.001),MDSYS.SDO_DIM_ELEMENT('Y', 356000, 1738000, 0.001)");
      if (axisCount > 2) {
        out.print(",MDSYS.SDO_DIM_ELEMENT('Z',-2500, 5000, 0.001)");
      }
      out.print("),");
      out.println("3005);");

      final int geometryType = OracleSdoGeometryFieldAdder.getGeometryTypeId(dataType, axisCount);
      out.print(
        "INSERT INTO OGIS_GEOMETRY_COLUMNS(F_TABLE_SCHEMA,F_TABLE_NAME,F_GEOMETRY_COLUMN,G_TABLE_SCHEMA,G_TABLE_NAME,GEOMETRY_TYPE,COORD_DIMENSION,SRID) VALUES ('");
      out.print(schemaName.toUpperCase());
      out.print("', '");
      out.print(tableName.toUpperCase());
      out.print("','");
      out.print(name.toUpperCase());
      out.print("', '");
      out.print(schemaName.toUpperCase());
      out.print("', '");
      out.print(tableName.toUpperCase());
      out.print("',");
      out.print(geometryType);
      out.print(",");
      out.print(axisCount);
      out.print(",");
      out.print("100");
      out.print(srid);
      out.println(");");
    }
  }

  @Override
  public void writeResetSequence(final RecordDefinition recordDefinition,
    final List<Record> values) {
    final PrintWriter out = getOut();
    Long nextValue = 0L;
    for (final Record record : values) {
      final Identifier id = record.getIdentifier();

      for (int i = 0; i < id.getValueCount(); i++) {
        final Object value = id.getValue(i);
        if (value instanceof Number) {
          final Number number = (Number)value;
          final long longValue = number.longValue();
          if (longValue > nextValue) {
            nextValue = longValue;
          }
        }
      }
    }
    nextValue++;
    final String sequeneName = getSequenceName(recordDefinition);
    out.println("DECLARE");
    out.println("  cur_val NUMBER;");
    out.println("BEGIN");

    out.print("  SELECT ");
    out.print(sequeneName);
    out.println(".NEXTVAL INTO cur_val FROM DUAL;");

    out.print("  IF cur_val + 1 <> ");
    out.print(nextValue);
    out.println(" THEN");

    out.print("    EXECUTE IMMEDIATE 'ALTER SEQUENCE ");
    out.print(sequeneName);
    out.print(" INCREMENT BY ' || (");
    out.print(nextValue);
    out.println(" -  cur_val -1) || ' MINVALUE 1';");

    out.print("    SELECT ");
    out.print(sequeneName);
    out.println(".NEXTVAL INTO cur_val FROM DUAL;");

    out.print("    EXECUTE IMMEDIATE 'ALTER SEQUENCE ");
    out.print(sequeneName);
    out.println(" INCREMENT BY 1';");
    out.println("  END IF;");
    out.println("END;");
    out.println("/");
  }
}
