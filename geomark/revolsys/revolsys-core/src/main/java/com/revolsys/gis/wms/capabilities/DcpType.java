package com.revolsys.gis.wms.capabilities;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;

public class DcpType {
  public static DcpType newDcpType(final Element dcpTypeElement) {
    final Element httpDcpTypeElement = XmlUtil.getFirstElement(dcpTypeElement, "HTTP");
    if (httpDcpTypeElement != null) {
      return new HttpDcpType(httpDcpTypeElement);
    }
    return null;
  }

  private final String type;

  public DcpType(final String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return this.type;
  }

}
