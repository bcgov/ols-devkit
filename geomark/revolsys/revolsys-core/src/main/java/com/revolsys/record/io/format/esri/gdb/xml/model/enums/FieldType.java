package com.revolsys.record.io.format.esri.gdb.xml.model.enums;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.GeometryDataTypes;

public enum FieldType {
  /** Binary Large Object. */
  esriFieldTypeBlob(DataTypes.BLOB),

  /** Date. */
  esriFieldTypeDate(DataTypes.DATE_TIME),

  /** Double-precision floating-point number. */
  esriFieldTypeDouble(DataTypes.DOUBLE),

  /** Geometry. */
  esriFieldTypeGeometry(GeometryDataTypes.GEOMETRY),

  /** ESRI Global ID. */
  esriFieldTypeGlobalID(DataTypes.UUID),

  /** Globally Unique Idendifier. */
  esriFieldTypeGUID(DataTypes.UUID),

  /** Long Integer. */
  esriFieldTypeInteger(DataTypes.INT),

  /** Long Integer representing an object identifier. */
  esriFieldTypeOID(DataTypes.INT),

  /** Raster. */
  esriFieldTypeRaster(DataTypes.BLOB),

  /** Single-precision floating-point number. */
  esriFieldTypeSingle(DataTypes.FLOAT),

  /** Short. */
  esriFieldTypeSmallInteger(DataTypes.SHORT),

  /** Character string. */
  esriFieldTypeString(DataTypes.STRING),

  /** XML Document. */
  esriFieldTypeXML(DataTypes.XML);

  private DataType dataType;

  private FieldType(final DataType dataType) {
    this.dataType = dataType;
  }

  public DataType getDataType() {
    return this.dataType;
  }
}
