package com.revolsys.gis.wms.capabilities;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.revolsys.gis.wms.WmsClient;
import com.revolsys.record.io.format.xml.XmlUtil;

public class Capability {

  private final List<String> exceptionFormats = new ArrayList<>();

  private WmsLayerDefinition layer;

  private final List<Request> requests = new ArrayList<>();

  public Capability(final WmsClient wmsClient, final Element capabilityElement) {
    XmlUtil.forFirstElement(capabilityElement, "Request", (requestsElement) -> {
      XmlUtil.forEachElement(requestsElement, (requestElement) -> {
        final Request request = new Request(requestElement);
        this.requests.add(request);
      });
    });
    XmlUtil.forFirstElement(capabilityElement, "Exception", exceptionElement -> {
      XmlUtil.forEachElement(exceptionElement, "Format", (exceptionFormatElement) -> {
        final String exceptionFormat = exceptionFormatElement.getTextContent();
        this.exceptionFormats.add(exceptionFormat);
      });
    });
    XmlUtil.forFirstElement(capabilityElement, "Layer", (layerElement) -> {
      this.layer = new WmsLayerDefinition(layerElement);
      this.layer.setParent(wmsClient);
    });
  }

  public List<String> getExceptionFormats() {
    return this.exceptionFormats;
  }

  public WmsLayerDefinition getLayer() {
    return this.layer;
  }

  public List<Request> getRequests() {
    return this.requests;
  }
}
