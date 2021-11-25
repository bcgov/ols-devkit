package com.revolsys.record.io.format.gml.type;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.record.io.format.xml.XsiConstants;

public abstract class AbstractGmlFieldType implements GmlFieldType {

  private final DataType dataType;

  private final String xmlSchemaTypeName;

  public AbstractGmlFieldType(final DataType dataType, final String xmlSchemaTypeName) {
    this.dataType = dataType;
    this.xmlSchemaTypeName = xmlSchemaTypeName;
  }

  @Override
  public DataType getDataType() {
    return this.dataType;
  }

  protected String getType(final Object value) {
    return this.xmlSchemaTypeName;
  }

  @Override
  public String getXmlSchemaTypeName() {
    return this.xmlSchemaTypeName;
  }

  @Override
  public void writeValue(final XmlWriter out, final Object value) {
    if (value == null) {
      out.attribute(XsiConstants.NIL, true);
    } else {
      writeValueText(out, value);
    }
  }

  protected abstract void writeValueText(XmlWriter out, Object value);

}
