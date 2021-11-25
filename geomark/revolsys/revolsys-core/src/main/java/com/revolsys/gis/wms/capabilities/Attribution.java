package com.revolsys.gis.wms.capabilities;

import java.net.URL;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;
import com.revolsys.util.UrlUtil;

public class Attribution {
  private ImageUrl logoUrl;

  private final URL onlineResource;

  private final String title;

  public Attribution(final Element attributionElement) {
    this.title = XmlUtil.getFirstElementText(attributionElement, "Title");
    final String onlineResourceText = XmlUtil.getFirstElementAttribute(attributionElement,
      "OnlineResource", "http://www.w3.org/1999/xlink", "href");
    this.onlineResource = UrlUtil.getUrl(onlineResourceText);
    XmlUtil.forEachElement(attributionElement, "LogoURL", (imageUrlElement) -> {
      this.logoUrl = new ImageUrl(imageUrlElement);
    });
  }

  public ImageUrl getLogoUrl() {
    return this.logoUrl;
  }

  public URL getOnlineResource() {
    return this.onlineResource;
  }

  public String getTitle() {
    return this.title;
  }
}
