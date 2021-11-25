package com.revolsys.gis.wms.capabilities;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;

public class HttpDcpType extends DcpType {
  private final List<HttpMethod> methods = new ArrayList<>();

  public HttpDcpType(final Element httpDcpTypeElement) {
    super("HTTP");
    XmlUtil.forEachElement(httpDcpTypeElement, (childElement) -> {
      this.methods.add(new HttpMethod(childElement));
    });
  }

  public List<HttpMethod> getMethods() {
    return this.methods;
  }
}
