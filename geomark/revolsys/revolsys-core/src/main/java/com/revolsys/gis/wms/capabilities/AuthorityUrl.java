package com.revolsys.gis.wms.capabilities;

import java.net.URL;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;
import com.revolsys.util.UrlUtil;

public class AuthorityUrl {
  private final String name;

  private final URL onlineResource;

  public AuthorityUrl(final Element authorityUrlElement) {
    this.name = authorityUrlElement.getAttribute("name");
    final String onlineResourceText = XmlUtil.getFirstElementAttribute(authorityUrlElement,
      "OnlineResource", "http://www.w3.org/1999/xlink", "href");
    this.onlineResource = UrlUtil.getUrl(onlineResourceText);
  }

  public String getName() {
    return this.name;
  }

  public URL getOnlineResource() {
    return this.onlineResource;
  }
}
