package com.revolsys.gis.wms.capabilities;

import org.w3c.dom.Element;

public class WmsIdentifier {
  private final String authority;

  private final String value;

  public WmsIdentifier(final Element identifierElement) {
    this.authority = identifierElement.getAttribute("authority");
    this.value = identifierElement.getTextContent();
  }

  public String getAuthority() {
    return this.authority;
  }

  public String getValue() {
    return this.value;
  }
}
