package com.revolsys.record.io.format.esri.gdb.xml.type;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.io.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.record.io.format.xml.XsiConstants;

public abstract class AbstractEsriGeodatabaseXmlFieldType
  implements EsriGeodatabaseXmlFieldType, EsriGeodatabaseXmlConstants {

  private final DataType dataType;

  private final FieldType esriFieldType;

  private final String xmlSchemaTypeName;

  public AbstractEsriGeodatabaseXmlFieldType(final DataType dataType,
    final String xmlSchemaTypeName, final FieldType esriFieldType) {
    this.dataType = dataType;
    this.xmlSchemaTypeName = xmlSchemaTypeName;
    this.esriFieldType = esriFieldType;
  }

  @Override
  public DataType getDataType() {
    return this.dataType;
  }

  @Override
  public FieldType getEsriFieldType() {
    return this.esriFieldType;
  }

  @Override
  public int getFixedLength() {
    return -1;
  }

  protected String getType(final Object value) {
    return this.xmlSchemaTypeName;
  }

  @Override
  public String getXmlSchemaTypeName() {
    return this.xmlSchemaTypeName;
  }

  @Override
  public boolean isUsePrecision() {
    return false;
  }

  @Override
  public void writeValue(final XmlWriter out, final Object value) {
    out.startTag(EsriGeodatabaseXmlConstants.VALUE);
    if (value == null) {
      out.attribute(XsiConstants.NIL, true);
    } else {
      out.attribute(XsiConstants.TYPE, getType(value));
      writeValueText(out, value);
    }
    out.endTag(EsriGeodatabaseXmlConstants.VALUE);
  }

  protected abstract void writeValueText(XmlWriter out, Object value);

}
