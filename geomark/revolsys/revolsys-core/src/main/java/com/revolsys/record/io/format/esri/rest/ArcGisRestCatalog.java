package com.revolsys.record.io.format.esri.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.esri.rest.map.FeatureLayer;
import com.revolsys.record.io.format.esri.rest.map.FeatureService;
import com.revolsys.record.io.format.esri.rest.map.MapService;
import com.revolsys.record.query.Query;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;
import com.revolsys.util.SupplierWithProperties;
import com.revolsys.webservice.WebServiceResource;

public class ArcGisRestCatalog extends ArcGisResponse<CatalogElement> {

  public static final String J_TYPE = "arcGisRestServer";

  private static final Map<String, Function<ArcGisRestServiceContainer, ArcGisRestService>> SERVICE_FACTORY_BY_TYPE = Maps
    .<String, Function<ArcGisRestServiceContainer, ArcGisRestService>> buildHash()//
    .add("MapServer", MapService::new) //
    .add("FeatureServer", FeatureService::new) //
    .getMap();

  public static ArcGisRestCatalog newArcGisRestCatalog(
    final Map<String, ? extends Object> properties) {
    final String serviceUrl = (String)properties.get("serviceUrl");
    if (Property.hasValue(serviceUrl)) {
      final ArcGisRestCatalog service = newArcGisRestCatalog(serviceUrl);
      service.setProperties(properties);
      return service;
    } else {
      throw new IllegalArgumentException("Missing serviceUrl");
    }
  }

  public static ArcGisRestCatalog newArcGisRestCatalog(String rootUrl) {
    rootUrl = rootUrl.replaceAll("/+$", "");
    if (rootUrl.endsWith("/services") || rootUrl.endsWith("%2fservices")) {
      return new ArcGisRestCatalog(rootUrl);
    } else if (rootUrl.endsWith("/rest")) {
      return new ArcGisRestCatalog(rootUrl + "/services");
    } else {
      final int index = rootUrl.indexOf("/rest/services");
      if (index == -1) {
        throw new IllegalArgumentException(
          "ArgGIS rest services must include the path /rest/services");
      } else {
        final String url = rootUrl.substring(0, index + 14);
        return new ArcGisRestCatalog(url);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static Supplier<RecordReader> newRecordReaderFactory(
    final Map<String, ? extends Object> properties) {
    final String serverUrl = (String)properties.get("serverUrl");
    final String layerPath = (String)properties.get("layerPath");

    final Supplier<RecordReader> factory = () -> {
      final FeatureLayer layer = FeatureLayer.getRecordLayerDescription(serverUrl, layerPath);
      if (layer == null) {
        throw new RuntimeException("Cannot find layer: " + layerPath + " on " + serverUrl);
      } else {
        final RecordReader reader = layer.newRecordReader((Query)null, true);
        final Map<String, Object> readerProperties = (Map<String, Object>)properties
          .get("readerProperties");
        reader.setProperties(readerProperties);
        return reader;
      }
    };
    return new SupplierWithProperties<>(factory, properties);
  }

  private Map<String, CatalogElement> childByName = new HashMap<>();

  private List<CatalogElement> children = Collections.emptyList();

  private ArcGisRestCatalog(final ArcGisRestCatalog parent, final String path) {
    super(parent, path);
  }

  public ArcGisRestCatalog(final String rootUrl) {
    setServiceUrl(new UrlResource(rootUrl));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends WebServiceResource> C getChild(final String name) {
    refreshIfNeeded();
    if (name == null) {
      return null;
    } else {
      final String childKey = name.toLowerCase();
      final CatalogElement child = this.childByName.get(childKey);
      if (child == null) {
        final ArcGisRestCatalog folder = new ArcGisRestCatalog(this, name);
        folder.refresh();
        return (C)folder;
      }
      return (C)child;
    }
  }

  @Override
  public List<CatalogElement> getChildren() {
    refreshIfNeeded();
    return this.children;
  }

  @Override
  public String getIconName() {
    return "folder:world";
  }

  @Override
  public String getName() {
    String name = super.getName();
    if (name == null) {
      final PathName pathName = getPathName();
      if (pathName == null || pathName.equalsPath("/")) {
        final UrlResource resourceUrl = getServiceUrl();
        try {
          final URI uri = resourceUrl.getUri();
          return uri.getHost();
        } catch (final Throwable e) {
          return "???";
        }
      } else {
        name = pathName.getName();
        setName(name);
      }
    }
    return name;
  }

  @Override
  public String getPathElement() {
    return super.getName();
  }

  @SuppressWarnings("unchecked")
  public <T extends ArcGisRestService> T getService(final String name,
    final Class<T> serviceClass) {
    final CatalogElement child = getChild(name);
    if (child != null) {
      if (serviceClass.isAssignableFrom(child.getClass())) {
        return (T)child;
      } else {
        throw new IllegalArgumentException(
          "ArcGIS REST service is not a " + serviceClass.getName() + ": " + name);
      }
    }
    return null;
  }

  @Override
  public String getWebServiceTypeName() {
    return J_TYPE;
  }

  @Override
  protected void initialize(final MapEx properties) {
    final List<CatalogElement> children = new ArrayList<>();
    final Map<String, CatalogElement> childByName = new HashMap<>();
    final List<String> folderNames = properties.getValue("folders", Collections.emptyList());
    for (final String folderPath : folderNames) {
      final String folderName = PathName.newPathName(folderPath).getName();
      final ArcGisRestCatalog folder = new ArcGisRestCatalog(this, folderName);
      children.add(folder);
      final String childKey = folderName.toLowerCase();
      childByName.put(childKey, folder);
    }
    final List<MapEx> serviceDescriptions = properties.getValue("services",
      Collections.emptyList());
    for (final MapEx serviceDescription : serviceDescriptions) {
      final String serviceContainerPath = serviceDescription.getString("name");
      final String serviceType = serviceDescription.getString("type");
      final String serviceContainerName = PathName.newPathName(serviceContainerPath).getName();
      try {
        final String childKey = serviceContainerName.toLowerCase();
        ArcGisRestServiceContainer container = (ArcGisRestServiceContainer)childByName
          .get(childKey);
        if (container == null) {
          container = new ArcGisRestServiceContainer(this, serviceContainerName);
          childByName.put(childKey, container);
          children.add(container);
        }
        ArcGisRestService service;
        final Function<ArcGisRestServiceContainer, ArcGisRestService> serviceFactory = SERVICE_FACTORY_BY_TYPE
          .get(serviceType);
        if (serviceFactory == null) {
          service = new ArcGisRestService(container, serviceType);
        } else {
          service = serviceFactory.apply(container);
        }
        container.addService(service);
      } catch (final Throwable e) {
        Logs.error(this, "Unable to get service: " + getServiceUrl() + "/" + serviceContainerPath,
          e);
      }
    }
    this.childByName = Collections.unmodifiableMap(childByName);
    this.children = Collections.unmodifiableList(children);
  }

  @Override
  protected UrlResource newServiceUrlResource(final Map<String, Object> parameters) {
    return getServiceUrl().newUrlResource(parameters);
  }

  @Override
  public String toString() {
    return getName() + " " + getServiceUrl();
  }
}
