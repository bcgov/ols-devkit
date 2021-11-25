package com.revolsys.record.io.format.vrt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.namespace.QName;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class OgrVrtWriter {
  public static void write(final File file, final RecordDefinition recordDefinition,
    final String dataSource) throws IOException {
    try (
      XmlWriter writer = new XmlWriter(new FileWriter(file))) {
      writer.setIndent(true);
      writer.startDocument("UTF-8", "1.0");
      writer.startTag("OGRVRTDataSource");
      writer.startTag("OGRVRTLayer");
      final String typeName = recordDefinition.getName();
      writer.attribute("name", typeName);
      writer.startTag("SrcDataSource");
      writer.attribute("relativeToVRT", "1");
      writer.text(dataSource);
      writer.endTag("SrcDataSource");

      writer.element(new QName("SrcLayer"), typeName);

      for (final FieldDefinition attribute : recordDefinition.getFields()) {
        final String fieldName = attribute.getName();
        final DataType fieldType = attribute.getDataType();
        final Class<?> typeClass = attribute.getTypeClass();
        if (Geometry.class.isAssignableFrom(typeClass)) {
          final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
          writer.element("GeometryType", "wkb" + fieldType);
          if (geometryFactory != null) {
            writer.element("LayerSRS", "EPSG:" + geometryFactory.getHorizontalCoordinateSystemId());
          }
          writer.startTag("GeometryField");
          writer.attribute("encoding", "WKT");
          writer.attribute("field", fieldName);
          writer.attribute("name", fieldName);
          writer.attribute("reportSrcColumn", "FALSE");
          writer.element("GeometryType", "wkb" + fieldType);
          if (geometryFactory != null) {
            writer.element("SRS", "EPSG:" + geometryFactory.getHorizontalCoordinateSystemId());
          }
          writer.endTag("GeometryField");
        } else {
          writer.startTag("Field");
          writer.attribute("name", fieldName);
          String type = "String";
          if (Arrays
            .asList(DataTypes.BYTE, DataTypes.SHORT, DataTypes.INT, DataTypes.LONG,
              DataTypes.BIG_INTEGER)
            .contains(fieldType)) {
            type = "Integer";
          } else if (Arrays.asList(DataTypes.FLOAT, DataTypes.DOUBLE, DataTypes.DECIMAL)
            .contains(fieldType)) {
            type = "Real";
          } else if (DataTypes.SQL_DATE.equals(fieldType)) {
            type = "Date";
          } else if (DataTypes.DATE_TIME.equals(fieldType)
            || DataTypes.UTIL_DATE.equals(fieldType)) {
            type = "DateTime";
          } else {
            type = "String";
          }
          writer.attribute("type", type);
          final int length = attribute.getLength();
          if (length > 0) {
            writer.attribute("width", length);
          }
          final int scale = attribute.getScale();
          if (scale > 0) {
            writer.attribute("scale", scale);
          }
          writer.attribute("src", fieldName);
          writer.endTag("Field");
        }
      }
      writer.endTag("OGRVRTLayer");
      writer.endTag("OGRVRTDataSource");
      writer.endDocument();
    }
  }
}
