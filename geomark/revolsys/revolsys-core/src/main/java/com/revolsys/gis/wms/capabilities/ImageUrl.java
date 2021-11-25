package com.revolsys.gis.wms.capabilities;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;

public class ImageUrl extends FormatUrl {
  private final int height;

  private final int width;

  public ImageUrl(final Element imageUrlElement) {
    super(imageUrlElement);
    this.width = XmlUtil.getAttributeInt(imageUrlElement, "width", 0);
    this.height = XmlUtil.getAttributeInt(imageUrlElement, "height", 0);
  }

  public int getHeight() {
    return this.height;
  }

  public int getWidth() {
    return this.width;
  }
}
