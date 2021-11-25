package com.revolsys.oracle.recordstore;

import java.io.PrintWriter;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class OracleDdlUtil {

  public static void writeCreateTable(final PrintWriter out,
    final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    out.println();
    out.print("CREATE TABLE ");
    final String tableName = JdbcUtils.getQualifiedTableName(typePath);
    out.print(tableName);
    out.println(" (");
    for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
      if (i > 1) {
        out.println(",");
      }
      final FieldDefinition attribute = recordDefinition.getField(i);
      final String name = attribute.getName();
      out.print("  ");
      out.print(name);
      for (int j = name.length(); j < 32; j++) {
        out.print(' ');
      }
      out.print(" : ");
      final DataType dataType = attribute.getDataType();
      if (dataType == DataTypes.BOOLEAN) {
        out.print("NUMBER(1)");
      } else if (dataType == DataTypes.BYTE) {
        out.print("NUMBER(3)");
      } else if (dataType == DataTypes.SHORT) {
        out.print("NUMBER(5)");
      } else if (dataType == DataTypes.INT) {
        out.print("NUMBER(9)");
      } else if (dataType == DataTypes.LONG) {
        out.print("NUMBER(19)");
      } else if (dataType == DataTypes.FLOAT) {
        out.print("NUMBER");
      } else if (dataType == DataTypes.DOUBLE) {
        out.print("NUMBER");
      } else if (dataType == DataTypes.STRING) {
        out.print("VARCHAR2(");
        out.print(attribute.getLength());
        out.print(")");
      } else if (dataType == GeometryDataTypes.GEOMETRY) {
        out.print("MDSYS.SDO_GEOMETRY");
      }
      if (attribute.isRequired()) {
        out.print(" NOT NULL");
      }
    }
    out.println();
    out.println(");");

  }
}
