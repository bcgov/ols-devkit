package com.revolsys.gis.wms.capabilities;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;

public class Request {
  private final List<DcpType> dcpTypes = new ArrayList<>();

  private final List<String> formats = new ArrayList<>();

  private final String name;

  public Request(final Element requestElement) {
    this.name = requestElement.getTagName();
    XmlUtil.forEachElement(requestElement, "Format", (formatElement) -> {
      final String format = formatElement.getTextContent();
      this.formats.add(format);
    });
    XmlUtil.forEachElement(requestElement, "DCPType", (dcpTypeElement) -> {
      final DcpType dcpType = DcpType.newDcpType(dcpTypeElement);
      if (dcpType != null) {
        this.dcpTypes.add(dcpType);
      }
    });

  }

  public List<DcpType> getDcpTypes() {
    return this.dcpTypes;
  }

  public List<String> getFormats() {
    return this.formats;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
