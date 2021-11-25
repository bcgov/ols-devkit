package com.revolsys.record.io.format.gml.type;

import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.GeometryDataTypes;

public class GmlFieldTypeRegistry {

  public static final GmlFieldTypeRegistry INSTANCE = new GmlFieldTypeRegistry();

  private final Map<DataType, GmlFieldType> typeMapping = new HashMap<>();

  public GmlFieldTypeRegistry() {
    addFieldType(new SimpleFieldType(DataTypes.ANY_URI));
    addFieldType(new SimpleFieldType(DataTypes.BASE64_BINARY));
    addFieldType(new SimpleFieldType(DataTypes.BOOLEAN));
    addFieldType(new SimpleFieldType(DataTypes.BYTE));
    addFieldType(new SimpleFieldType(DataTypes.SQL_DATE));
    addFieldType(new SimpleFieldType(DataTypes.DATE_TIME));
    addFieldType(new SimpleFieldType(DataTypes.DECIMAL));
    addFieldType(new SimpleFieldType(DataTypes.DOUBLE));
    addFieldType(new SimpleFieldType(DataTypes.FLOAT));
    addFieldType(new SimpleFieldType(DataTypes.INT));
    addFieldType(new SimpleFieldType(DataTypes.BIG_INTEGER));
    addFieldType(new SimpleFieldType(DataTypes.LONG));
    addFieldType(new SimpleFieldType(DataTypes.QNAME));
    addFieldType(new SimpleFieldType(DataTypes.SHORT));
    addFieldType(new SimpleFieldType(DataTypes.STRING));
    addFieldType(new GmlGeometryFieldType(GeometryDataTypes.GEOMETRY));
    addFieldType(new GmlGeometryFieldType(GeometryDataTypes.POINT));
    addFieldType(new GmlGeometryFieldType(GeometryDataTypes.LINE_STRING));
    addFieldType(new GmlGeometryFieldType(GeometryDataTypes.POLYGON));
    addFieldType(new GmlGeometryFieldType(GeometryDataTypes.MULTI_POINT));
    addFieldType(new GmlGeometryFieldType(GeometryDataTypes.MULTI_LINE_STRING));
    addFieldType(new GmlGeometryFieldType(GeometryDataTypes.MULTI_POLYGON));
  }

  public void addFieldType(final DataType dataType, final GmlFieldType fieldType) {
    this.typeMapping.put(dataType, fieldType);
  }

  public void addFieldType(final GmlFieldType fieldType) {
    final DataType dataType = fieldType.getDataType();
    addFieldType(dataType, fieldType);
  }

  public GmlFieldType getFieldType(final DataType dataType) {
    GmlFieldType gmlFieldType = this.typeMapping.get(dataType);
    if (gmlFieldType == null) {
      gmlFieldType = SimpleFieldType.OBJECT;
    }
    return gmlFieldType;
  }
}
