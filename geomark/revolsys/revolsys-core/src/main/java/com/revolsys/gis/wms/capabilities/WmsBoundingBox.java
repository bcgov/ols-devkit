package com.revolsys.gis.wms.capabilities;

import org.w3c.dom.Element;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.wms.WmsClient;
import com.revolsys.record.io.format.xml.XmlUtil;

public class WmsBoundingBox {
  private final BoundingBox boundingBox;

  private final double resX;

  private final double resY;

  private final String srs;

  public WmsBoundingBox(final Element boundingBoxElement) {
    final double minX = XmlUtil.getAttributeDouble(boundingBoxElement, "minx", Double.NaN);
    final double maxX = XmlUtil.getAttributeDouble(boundingBoxElement, "maxx", Double.NaN);
    this.resX = XmlUtil.getAttributeDouble(boundingBoxElement, "resx", Double.NaN);
    final double minY = XmlUtil.getAttributeDouble(boundingBoxElement, "miny", Double.NaN);
    final double maxY = XmlUtil.getAttributeDouble(boundingBoxElement, "maxy", Double.NaN);
    this.resY = XmlUtil.getAttributeDouble(boundingBoxElement, "resy", Double.NaN);
    this.srs = boundingBoxElement.getAttribute("SRS");
    final GeometryFactory geometryFactory = WmsClient.getGeometryFactory(this.srs);
    this.boundingBox = geometryFactory.newBoundingBox(minX, minY, maxX, maxY);
  }

  public BoundingBox getEnvelope() {
    return this.boundingBox;
  }

  public double getResX() {
    return this.resX;
  }

  public double getResY() {
    return this.resY;
  }

  public String getSrs() {
    return this.srs;
  }
}
