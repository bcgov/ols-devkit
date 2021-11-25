package com.revolsys.gis.wms.capabilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

import com.revolsys.gis.wms.WmsClient;
import com.revolsys.record.io.format.xml.XmlUtil;
import com.revolsys.spring.resource.UrlResource;

public class WmsCapabilities {
  private Capability capability;

  private Service service;

  private final String updateSequence;

  private final String version;

  private String exceptionFormat;

  public WmsCapabilities(final WmsClient wmsClient, final Element element) {
    this.version = element.getAttribute("version");
    this.updateSequence = element.getAttribute("updateSequence");
    XmlUtil.forFirstElement(element, "Service", (serviceElement) -> {
      this.service = new Service(serviceElement);
    });
    XmlUtil.forFirstElement(element, "Capability", (capabilityElement) -> {
      this.capability = new Capability(wmsClient, capabilityElement);
    });
  }

  public Capability getCapability() {
    return this.capability;
  }

  public String getExceptionFormat() {
    if (this.exceptionFormat == null) {
      final List<String> exceptionFormats = this.capability.getExceptionFormats();
      for (final String exceptionFormat : Arrays.asList("application/vnd.ogc.se_inimage", "INIMAGE",
        "application/vnd.ogc.se_blank", "BLANK", "application/vnd.ogc.se_xml", "XML")) {
        if (exceptionFormats.contains(exceptionFormat)) {
          this.exceptionFormat = exceptionFormat;
          return exceptionFormat;
        }
      }
    }
    return this.exceptionFormat;
  }

  public WmsLayerDefinition getLayer(final String name) {
    return getLayer(this.capability.getLayer(), name);
  }

  private WmsLayerDefinition getLayer(final WmsLayerDefinition layer, final String name) {

    final String layerName = layer.getName();
    if (layerName != null && layerName.equals(name)) {
      return layer;
    }
    for (final WmsLayerDefinition childLayer : layer.getLayers()) {
      final WmsLayerDefinition matchedLayer = getLayer(childLayer, name);
      if (matchedLayer != null) {
        return matchedLayer;
      }
    }
    return null;
  }

  public List<WmsLayerDefinition> getLayers() {
    final WmsLayerDefinition rootLayer = this.capability.getLayer();
    return Collections.singletonList(rootLayer);
  }

  public Request getRequest(final String requestName) {
    for (final Request request : this.capability.getRequests()) {
      if (request.getName().equalsIgnoreCase(requestName)) {
        return request;
      }
    }
    return null;
  }

  public UrlResource getRequestUrl(final String requestName, final String methodName) {
    final Request request = getRequest(requestName);
    if (request != null) {
      for (final DcpType type : request.getDcpTypes()) {
        if (type instanceof HttpDcpType) {
          final HttpDcpType httpType = (HttpDcpType)type;
          for (final HttpMethod httpMethod : httpType.getMethods()) {
            if (httpMethod.getName().equalsIgnoreCase(methodName)) {
              return httpMethod.getOnlineResource();
            }
          }
        }
      }
    }
    return null;
  }

  public Service getService() {
    return this.service;
  }

  public String getUpdateSequence() {
    return this.updateSequence;
  }

  public String getVersion() {
    return this.version;
  }

  public boolean hasLayer(final String name) {
    return getLayer(name) != null;
  }

  public boolean isSrsSupported(final String srsId, final List<String> layerNames) {
    final WmsLayerDefinition layer = this.capability.getLayer();
    return isSrsSupported(srsId, layer, layerNames, false);
  }

  private boolean isSrsSupported(final String srsId, final WmsLayerDefinition layer,
    final List<String> layerNames, final boolean parentHasSrs) {
    final boolean hasSrs = layer.getSrs().contains(srsId) || parentHasSrs;
    if (layerNames.contains(layer.getName())) {
      if (hasSrs) {
        return true;
      }
    }
    for (final WmsLayerDefinition childLayer : layer.getLayers()) {
      if (isSrsSupported(srsId, childLayer, layerNames, hasSrs)) {
        return true;
      }
    }
    return false;
  }
}
