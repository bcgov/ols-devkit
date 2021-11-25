package com.revolsys.gis.wms;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.coordinatesystem.model.systems.EpsgId;
import org.w3c.dom.Document;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.wms.capabilities.WmsCapabilities;
import com.revolsys.gis.wms.capabilities.WmsLayerDefinition;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.raster.BufferedImages;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;
import com.revolsys.webservice.AbstractWebService;
import com.revolsys.webservice.WebServiceResource;

public class WmsClient extends AbstractWebService<WmsLayerDefinition>
  implements WebServiceResource {

  public static final String J_TYPE = "ogcWmsServer";

  public static int getCoordinateSystemId(final String srs) {
    int coordinateSystemId = EpsgId.WGS84;
    try {
      final int colonIndex = srs.indexOf(':');
      if (colonIndex != -1) {
        coordinateSystemId = Integer.valueOf(srs.substring(colonIndex + 1));
      }
    } catch (final Throwable e) {
    }
    return coordinateSystemId;
  }

  public static GeometryFactory getGeometryFactory(final String srs) {
    final int coordinateSystemId = getCoordinateSystemId(srs);
    final GeometryFactory geometryFactory = GeometryFactory.floating2d(coordinateSystemId);
    return geometryFactory;
  }

  public static WmsClient newOgcWmsClient(final Map<String, ? extends Object> properties) {
    final String serviceUrl = (String)properties.get("serviceUrl");
    if (Property.hasValue(serviceUrl)) {
      final WmsClient client = new WmsClient(serviceUrl);
      client.setProperties(properties);
      return client;
    } else {
      throw new IllegalArgumentException("Missing serviceUrl");
    }
  }

  private WmsCapabilities capabilities;

  public WmsClient(final String serviceUrl) {
    super(serviceUrl);
  }

  public WmsCapabilities getCapabilities() {
    if (this.capabilities == null) {
      loadCapabilities();
    }
    return this.capabilities;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends WebServiceResource> C getChild(final String name) {
    if (name == null) {
      return null;
    } else {
      final WmsCapabilities capabilities = getCapabilities();
      return (C)capabilities.getLayer(name);
    }
  }

  @Override
  public List<WmsLayerDefinition> getChildren() {
    final WmsCapabilities capabilities = getCapabilities();
    return capabilities.getLayers();
  }

  public WmsLayerDefinition getLayer(final String layerName) {
    final WmsCapabilities capabilities = getCapabilities();
    if (capabilities == null) {
      return null;
    } else {
      return capabilities.getLayer(layerName);
    }
  }

  public GeoreferencedImage getMapImage(final List<String> layers, final List<String> styles,
    final String srid, final BoundingBox boundingBox, final String format, final int width,
    final int height) {
    final UrlResource mapUrl = getMapUrl(layers, styles, srid, boundingBox, format, width, height);
    final BufferedImage image = BufferedImages.readImageIo(mapUrl);
    if (image == null) {
      return new BufferedGeoreferencedImage(boundingBox, width, height);
    } else {
      return new BufferedGeoreferencedImage(boundingBox, image);
    }
  }

  public GeoreferencedImage getMapImage(final String layer, final String style, final String srid,
    final BoundingBox boundingBox, final String format, final int width, final int height) {
    return getMapImage(Collections.singletonList(layer), Collections.singletonList(style), srid,
      boundingBox, format, width, height);
  }

  public UrlResource getMapUrl(final List<String> layers, final List<String> styles,
    final String srid, final BoundingBox envelope, final String format, final int width,
    final int height) {
    final WmsCapabilities capabilities = getCapabilities();
    final String version = capabilities.getVersion();
    final Map<String, Object> parameters = new LinkedHashMap<>();
    parameters.put("SERVICE", "WMS");
    if (version.equals("1.0.0")) {
      parameters.put(WmsParameters.WMTVER, version);
      parameters.put(WmsParameters.REQUEST, WmsParameterValues.MAP);
    } else {
      parameters.put(WmsParameters.VERSION, version);
      parameters.put(WmsParameters.REQUEST, WmsParameterValues.GET_MAP);
    }
    parameters.put(WmsParameters.LAYERS, Strings.toString(layers));
    String style;
    if (styles == null) {
      style = "";
    } else {
      style = Strings.toString(styles);
      for (int i = styles.size(); i < layers.size(); i++) {
        style += ",";
      }
    }

    parameters.put(WmsParameters.STYLES, style);
    if (version.equals("1.3.0")) {
      parameters.put(WmsParameters.CRS, srid);
    } else {
      parameters.put(WmsParameters.SRS, srid);
    }
    final String bbox = envelope.getMinX() + "," + envelope.getMinY() + "," + envelope.getMaxX()
      + "," + envelope.getMaxY();
    parameters.put(WmsParameters.BBOX, bbox);
    parameters.put(WmsParameters.WIDTH, width);
    parameters.put(WmsParameters.HEIGHT, height);
    parameters.put(WmsParameters.FORMAT, format);
    final String exceptionFormat = capabilities.getExceptionFormat();
    parameters.put(WmsParameters.EXCEPTIONS, exceptionFormat);
    parameters.put(WmsParameters.TRANSPARENT, "TRUE");
    UrlResource requestUrl = null;// = getCapabilities().getRequestUrl("GetMap",
                                  // "GET");
    if (requestUrl == null) {
      requestUrl = getServiceUrl();
    }
    return requestUrl.newUrlResource(parameters);
  }

  public UrlResource getMapUrl(final String layer, final String style, final String srid,
    final BoundingBox envelope, final String format, final int width, final int height) {
    return getMapUrl(Collections.singletonList(layer), Collections.singletonList(style), srid,
      envelope, format, width, height);
  }

  @Override
  public String getWebServiceTypeName() {
    return J_TYPE;
  }

  public boolean isConnected() {
    return this.capabilities != null;
  }

  public WmsCapabilities loadCapabilities() {
    final Map<String, Object> parameters = new LinkedHashMap<>();
    parameters.put(WmsParameters.SERVICE, WmsParameterValues.WMS);
    parameters.put(WmsParameters.REQUEST, WmsParameterValues.GET_CAPABILITIES);
    final UrlResource capabilitiesUrl = newServiceUrlResource(parameters);
    try (
      InputStream in = capabilitiesUrl.getInputStream()) {
      final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setValidating(false);
      documentBuilderFactory.setNamespaceAware(true);
      final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      final Document document = documentBuilder.parse(in);
      this.capabilities = new WmsCapabilities(this, document.getDocumentElement());
      return this.capabilities;
    } catch (final Throwable e) {
      throw Exceptions.wrap("Unable to read capabilities: " + capabilitiesUrl, e);
    }
  }

  @Override
  public void refresh() {
    loadCapabilities();
  }

}
