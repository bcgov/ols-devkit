package com.revolsys.geopackage;

import java.io.PrintWriter;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.date.Dates;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.io.JdbcDdlWriter;
import com.revolsys.record.property.ShortNameProperty;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public class GeoPackageSqlDdlWriter extends JdbcDdlWriter {

  public GeoPackageSqlDdlWriter(final PrintWriter out) {
    super(out);
    this.primaryKeyOnColumn = true;
    this.quoteColumns = true;
  }

  @Override
  public String getSequenceName(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    final String schema = JdbcUtils.getSchemaName(typePath);
    final ShortNameProperty shortNameProperty = ShortNameProperty.getProperty(recordDefinition);
    String shortName = null;
    if (shortNameProperty != null) {
      shortName = shortNameProperty.getShortName();
    }
    if (Property.hasValue(shortName) && shortNameProperty.isUseForSequence()) {
      final String sequenceName = schema + "." + shortName.toLowerCase() + "_seq";
      return sequenceName;
    } else {
      final String tableName = PathUtil.getName(typePath).toLowerCase();
      final String idFieldName = recordDefinition.getIdFieldName().toLowerCase();
      return schema + "." + tableName + "_" + idFieldName + "_seq";
    }
  }

  public String insertGpkgContents(final RecordDefinition recordDefinition) {
    final StringBuilder string = new StringBuilder();
    final String typePath = recordDefinition.getPath();
    final String tableName = PathUtil.getName(typePath);
    String tableType; // data_type
    if (recordDefinition.hasGeometryField()) {
      tableType = "features";
    } else {
      tableType = "attributes";
    }

    string.append(
      "INSERT INTO gpkg_contents (table_name, data_type, identifier, description, last_change, min_x, min_y, max_x, max_y, srs_id) VALUES ('");
    string.append(tableName);
    string.append("','");
    string.append(tableType);
    string.append("','");
    string.append(tableName);
    string.append("','");
    string.append(""); // description
    string.append("','");
    string.append(Dates.format("yyyy-MM-dd'T'HH:mm'Z'"));
    string.append("',");
    final BoundingBox boundingBox = recordDefinition.getBoundingBox();
    if (recordDefinition.hasGeometryField() && !boundingBox.isBboxEmpty()) {
      final double minX = boundingBox.getMinX();
      if (Double.isFinite(minX)) {
        string.append(minX);
      } else {
        string.append("NULL");
      }

      string.append(',');
      final double minY = boundingBox.getMinY();
      if (Double.isFinite(minY)) {
        string.append(minY);
      } else {
        string.append("NULL");
      }
      string.append(',');
      final double maxX = boundingBox.getMaxX();
      if (Double.isFinite(maxX)) {
        string.append(maxX);
      } else {
        string.append("NULL");
      }
      string.append(',');
      final double maxY = boundingBox.getMaxY();
      if (Double.isFinite(maxY)) {
        string.append(maxY);
      } else {
        string.append("NULL");
      }
      string.append(',');
      final int coordinateSystemId = boundingBox.getHorizontalCoordinateSystemId();
      string.append(coordinateSystemId);
    } else {
      string.append("NULL, NULL, NULL, NULL, 0");
    }
    string.append(")");
    return string.toString();
  }

  public String insertGpkgGeometryColumns(final FieldDefinition field) {
    final StringBuilder string = new StringBuilder();
    final String tableName = field.getPathName().getName();
    final String fieldName = field.getName();

    final GeometryFactory geometryFactory = field.getGeometryFactory();
    String geometryType = "GEOMETRY";
    final DataType dataType = field.getDataType();
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
    string.append(
      "INSERT INTO gpkg_geometry_columns (table_name, column_name, geometry_type_name, srs_id, z, m) VALUES ('");
    string.append(tableName);
    string.append("','");
    string.append(fieldName);
    string.append("','");
    string.append(geometryType);
    string.append("',");
    string.append(geometryFactory.getCoordinateSystemId());
    string.append(',');
    string.append(geometryFactory.getAxisCount() >= 3 ? 2 : 0);
    string.append(',');
    string.append(geometryFactory.getAxisCount() >= 4 ? 2 : 0);
    string.append(")");
    return string.toString();

  }

  @Override
  public void writeColumnDataType(final FieldDefinition field) {
    final PrintWriter out = getOut();
    final int fieldLength = field.getLength();
    final DataType dataType = field.getDataType();
    if (dataType == DataTypes.BOOLEAN) {
      out.print("BOOLEAN");
    } else if (dataType == DataTypes.BYTE) {
      out.print("TINYINT");
    } else if (dataType == DataTypes.SHORT) {
      out.print("SMALLINT");
    } else if (dataType == DataTypes.INT) {
      out.print("MEDIUMINT");
    } else if (dataType == DataTypes.LONG) {
      out.print("INTEGER");
    } else if (dataType == DataTypes.FLOAT) {
      out.print("FLOAT");
    } else if (dataType == DataTypes.DOUBLE) {
      out.print("DOUBLE");
    } else if (dataType == DataTypes.SQL_DATE) {
      out.print("DATE");
    } else if (dataType == DataTypes.DATE_TIME || dataType == DataTypes.UTIL_DATE) {
      out.print("DATETIME");
    } else if (dataType == DataTypes.BIG_INTEGER) {
      out.print("INTEGER");
    } else if (dataType == DataTypes.DECIMAL) {
      out.print("DOUBLE");
    } else if (dataType == DataTypes.STRING) {
      out.print("TEXT");
      if (fieldLength > 0) {
        out.print('(');
        out.print(fieldLength);
        out.print(')');
      }
    } else if (dataType == DataTypes.BLOB) {
      out.print("BLOB");
      if (fieldLength > 0) {
        out.print('(');
        out.print(fieldLength);
        out.print(')');
      }
    } else if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
      out.print("GEOMETRY");
    } else {
      out.print("TEXT");
    }
  }

  @Override
  public String writeCreateSequence(final RecordDefinition recordDefinition) {
    return "";
  }

  @Override
  public void writeGeometryRecordDefinition(final RecordDefinition recordDefinition) {
  }

  @Override
  protected void writePrimaryKeyFieldContstaint(final PrintWriter out) {
    super.writePrimaryKeyFieldContstaint(out);
    out.write(" AUTOINCREMENT");
  }

}
