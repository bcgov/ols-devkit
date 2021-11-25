package com.revolsys.gis.wms.capabilities;

import java.net.URL;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;
import com.revolsys.util.UrlUtil;

public class FormatUrl {
  private final String format;

  private final URL onlineResource;

  public FormatUrl(final Element formatElement) {
    this.format = XmlUtil.getFirstElementText(formatElement, "Format");
    final String onlineResourceText = XmlUtil.getFirstElementAttribute(formatElement,
      "OnlineResource", "http://www.w3.org/1999/xlink", "href");
    this.onlineResource = UrlUtil.getUrl(onlineResourceText);
  }

  public String getFormat() {
    return this.format;
  }

  public URL getOnlineResource() {
    return this.onlineResource;
  }
}
