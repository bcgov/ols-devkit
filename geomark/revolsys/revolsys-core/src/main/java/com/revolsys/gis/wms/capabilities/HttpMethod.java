package com.revolsys.gis.wms.capabilities;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;
import com.revolsys.spring.resource.UrlResource;

public class HttpMethod {
  private final String name;

  private final UrlResource onlineResource;

  public HttpMethod(final Element httpMethodElement) {
    this.name = httpMethodElement.getTagName();
    final String onlineResourceText = XmlUtil.getFirstElementAttribute(httpMethodElement,
      "OnlineResource", "http://www.w3.org/1999/xlink", "href");
    this.onlineResource = new UrlResource(onlineResourceText);
  }

  public String getName() {
    return this.name;
  }

  public UrlResource getOnlineResource() {
    return this.onlineResource;
  }

  @Override
  public String toString() {
    return this.name + " " + this.onlineResource;
  }
}
