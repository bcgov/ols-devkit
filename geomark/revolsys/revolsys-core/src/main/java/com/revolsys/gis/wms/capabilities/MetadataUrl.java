package com.revolsys.gis.wms.capabilities;

import org.w3c.dom.Element;

public class MetadataUrl extends FormatUrl {
  private final String type;

  public MetadataUrl(final Element metadataElement) {
    super(metadataElement);
    this.type = metadataElement.getAttribute("type");
  }

  public String getType() {
    return this.type;
  }
}
