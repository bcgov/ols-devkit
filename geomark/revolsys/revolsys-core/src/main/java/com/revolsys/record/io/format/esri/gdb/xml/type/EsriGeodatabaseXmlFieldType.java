package com.revolsys.record.io.format.esri.gdb.xml.type;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.record.io.format.xml.XmlWriter;

public interface EsriGeodatabaseXmlFieldType {

  DataType getDataType();

  FieldType getEsriFieldType();

  int getFixedLength();

  String getXmlSchemaTypeName();

  boolean isUsePrecision();

  void writeValue(XmlWriter out, Object value);

}
