package com.revolsys.record.io.format.esri.gdb.xml.type;

import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.record.io.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.record.io.format.json.Json;

public class EsriGeodatabaseXmlFieldTypeRegistry implements EsriGeodatabaseXmlConstants {

  public static final EsriGeodatabaseXmlFieldTypeRegistry INSTANCE = new EsriGeodatabaseXmlFieldTypeRegistry();

  private final Map<FieldType, DataType> esriToDataType = new HashMap<>();

  private final Map<DataType, EsriGeodatabaseXmlFieldType> typeMapping = new HashMap<>();

  public EsriGeodatabaseXmlFieldTypeRegistry() {
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeOID, DataTypes.INT, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeGlobalID, DataTypes.STRING, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeGUID, DataTypes.STRING, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeBlob, DataTypes.BASE64_BINARY, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeBlob, DataTypes.BLOB, false));

    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeString, DataTypes.ANY_URI, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeString, DataTypes.BOOLEAN, false));
    addFieldType(
      new SimpleFieldType(FieldType.esriFieldTypeString, DataTypes.QNAME, "xs:string", true, -1));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeString, DataTypes.CLOB, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeString, DataTypes.STRING, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeString, Json.JSON_OBJECT, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeString, Json.JSON_LIST, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeString, Json.JSON_TYPE, false));

    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeSmallInteger, DataTypes.BYTE,
      "xs:short", false, 2));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeDate, DataTypes.UTIL_DATE, false, 8));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeDate, DataTypes.SQL_DATE, false, 8));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeDate, DataTypes.DATE_TIME, false, 8));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeDate, DataTypes.TIMESTAMP, false, 8));
    addFieldType(
      new SimpleFieldType(FieldType.esriFieldTypeDouble, DataTypes.DECIMAL, "xs:double", false, 8));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeDouble, DataTypes.DOUBLE, false, 8));
    addFieldType(
      new SimpleFieldType(FieldType.esriFieldTypeSingle, DataTypes.FLOAT, "xs:double", false, 4));
    addFieldType(
      new SimpleFieldType(FieldType.esriFieldTypeInteger, DataTypes.INT, "xs:int", false, 4));
    addFieldType(
      new SimpleFieldType(FieldType.esriFieldTypeInteger, DataTypes.LONG, "xs:int", false, 4));
    addFieldType(
      new SimpleFieldType(FieldType.esriFieldTypeInteger, DataTypes.BIG_INTEGER, false, 4));
    addFieldType(
      new SimpleFieldType(FieldType.esriFieldTypeSmallInteger, DataTypes.SHORT, false, 2));

    addFieldType(
      new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, GeometryDataTypes.POINT));
    addFieldType(
      new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, GeometryDataTypes.MULTI_POINT));
    addFieldType(
      new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, GeometryDataTypes.LINE_STRING));
    addFieldType(
      new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, GeometryDataTypes.LINEAR_RING));
    addFieldType(new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry,
      GeometryDataTypes.MULTI_LINE_STRING));
    addFieldType(
      new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, GeometryDataTypes.POLYGON));
    addFieldType(
      new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, GeometryDataTypes.MULTI_POLYGON));
    addFieldType(
      new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, GeometryDataTypes.GEOMETRY));
  }

  public void addFieldType(final DataType dataType, final EsriGeodatabaseXmlFieldType fieldType) {
    this.typeMapping.put(dataType, fieldType);
    this.esriToDataType.put(fieldType.getEsriFieldType(), dataType);
  }

  public void addFieldType(final EsriGeodatabaseXmlFieldType fieldType) {
    final DataType dataType = fieldType.getDataType();
    addFieldType(dataType, fieldType);
  }

  public DataType getDataType(final FieldType fieldType) {
    return this.esriToDataType.get(fieldType);
  }

  public EsriGeodatabaseXmlFieldType getFieldType(final DataType dataType) {
    return this.typeMapping.get(dataType);
  }
}
