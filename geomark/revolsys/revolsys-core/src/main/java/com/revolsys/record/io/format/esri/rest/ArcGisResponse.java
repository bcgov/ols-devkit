package com.revolsys.record.io.format.esri.rest;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.exception.WrappedException;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.net.urlcache.FileResponseCache;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;
import com.revolsys.webservice.AbstractWebService;
import com.revolsys.webservice.WebService;
import com.revolsys.webservice.WebServiceResource;

public abstract class ArcGisResponse<V> extends AbstractWebService<V> implements CatalogElement {
  public static final Map<String, ? extends Object> FORMAT_PARAMETER = Collections.singletonMap("f",
    "json");

  public static BoundingBox newBoundingBox(final MapEx properties, final String name) {
    final MapEx extent = properties.getValue(name);
    if (extent == null) {
      return null;
    } else {
      final double minX = extent.getDouble("xmin");
      final double minY = extent.getDouble("ymin");
      final double maxX = extent.getDouble("xmax");
      final double maxY = extent.getDouble("ymax");

      final GeometryFactory geometryFactory = newGeometryFactory(extent, "spatialReference");
      return geometryFactory.newBoundingBox(minX, minY, maxX, maxY);
    }
  }

  public static GeometryFactory newGeometryFactory(final MapEx properties, final String fieldName) {
    final MapEx spatialReference = properties.getValue(fieldName);
    if (spatialReference == null) {
      return GeometryFactory.DEFAULT_3D;
    } else {
      Integer srid = spatialReference.getInteger("latestWkid");
      if (srid == null) {
        srid = spatialReference.getInteger("wkid");
        if (srid == null) {
          final String wkt = spatialReference.getString("wkt");
          if (Property.hasValue(wkt)) {
            return GeometryFactory.floating3d(wkt);
          } else {
            return GeometryFactory.DEFAULT_3D;
          }
        } else if (srid == 102100) {
          srid = 3857;
        } else if (srid == 102190) {
          srid = 3005;
        }
      }
      return GeometryFactory.floating3d(srid);
    }
  }

  public static <T extends ObjectWithProperties> List<T> newList(final Class<T> clazz,
    final MapEx properties, final String name) {
    final List<T> objects = new ArrayList<>();

    final List<MapEx> maps = properties.getValue(name);
    if (maps != null) {
      for (final MapEx map : maps) {
        try {
          final T value = clazz.newInstance();
          value.setProperties(map);
          objects.add(value);
        } catch (final Throwable t) {
          t.printStackTrace();
        }
      }
    }
    return objects;
  }

  public static <T extends ObjectWithProperties> T newObject(final Class<T> clazz,
    final MapEx properties, final String name) {
    final MapEx values = properties.getValue(name);
    if (values == null) {
      return null;
    } else {
      try {
        final T value = clazz.newInstance();
        value.setProperties(values);
        return value;
      } catch (final Throwable t) {
        t.printStackTrace();
        return null;
      }
    }
  }

  private CatalogElement parent;

  private double currentVersion;

  private boolean useProxy;

  private final Object resfreshSync = new Object();

  private boolean initialized = false;

  private boolean hasError = false;

  private boolean cannotFindHost;

  public ArcGisResponse() {
  }

  protected ArcGisResponse(final ArcGisResponse<?> parent) {
    super(parent.getServiceUrl());
    this.parent = parent;
    this.useProxy = parent.isUseProxy();
  }

  protected ArcGisResponse(final ArcGisResponse<?> parent, final String name) {
    super(parent.getServiceUrl().newChildResource(name));
    setName(name);
    this.parent = parent;
    this.useProxy = parent.isUseProxy();
  }

  public double getCurrentVersion() {
    return this.currentVersion;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends WebServiceResource> R getParent() {
    return (R)this.parent;
  }

  @Override
  public synchronized MapEx getProperties() {
    final MapEx properties = super.getProperties();
    if (Property.isEmpty(properties)) {
      properties.put("initializing", true);

      properties.put("initializing", false);
    }
    return properties;
  }

  public Resource getResource(final String child, final Map<String, ? extends Object> parameters) {
    final UrlResource serviceUrl = getServiceUrl(child);

    if (isUseProxy()) {
      final StringBuilder queryUrl = new StringBuilder(serviceUrl.getUriString());
      final String query = '?' + UrlUtil.getQueryString(parameters);
      queryUrl.append(UrlUtil.percentEncode(query));
      final String username = serviceUrl.getUsername();
      final String password = serviceUrl.getPassword();
      return new UrlResource(queryUrl, username, password);
    } else {
      return serviceUrl.newUrlResource(parameters);
    }

  }

  @Override
  public WebService<?> getWebService() {
    if (this.parent == null) {
      return this;
    } else {
      return this.parent.getWebService();
    }
  }

  protected void initialize(final MapEx properties) {
    setProperties(properties);
  }

  protected boolean isCannotFindHost() {
    if (this.parent instanceof ArcGisResponse<?>) {
      return ((ArcGisResponse<?>)this.parent).isCannotFindHost();
    } else {
      return this.cannotFindHost;
    }
  }

  @Override
  public boolean isHasError() {
    return this.hasError;
  }

  public boolean isInitialized() {
    return this.initialized;
  }

  @Override
  public boolean isUseProxy() {
    return this.useProxy;
  }

  @Override
  public final void refresh() {
    synchronized (this.resfreshSync) {
      try (
        BaseCloseable noCache = FileResponseCache.disable()) {
        refreshDo();
        setCannotFindHost(false);
        this.hasError = false;
      } catch (final WrappedException e) {
        this.hasError = true;
        final Throwable cause = Exceptions.unwrap(e);
        if (cause instanceof UnknownHostException) {
          if (!isCannotFindHost()) {
            setCannotFindHost(true);
            Logs.error(this, getPathName() + " Cannot find host " + cause.getMessage());
          }
        }
      } catch (final Throwable e) {
        this.hasError = true;
        throw Exceptions.wrap("Unable to initialize: " + this, e);
      }
    }
  }

  protected void refreshDo() {
    final UrlResource serviceUrl = getServiceUrl();

    Resource resource;
    if (isUseProxy()) {
      final String url = serviceUrl.getUriString() + "%3ff%3djson";
      final String username = serviceUrl.getUsername();
      final String password = serviceUrl.getPassword();
      resource = new UrlResource(url, username, password);
    } else {
      resource = serviceUrl.newUrlResource(Collections.singletonMap("f", "json"));
    }
    final MapEx newProperties = Json.toMap(resource);
    initialize(newProperties);
  }

  public final void refreshIfNeeded() {
    synchronized (this.resfreshSync) {
      if (!this.initialized) {
        this.initialized = true;
        refresh();
      }
    }
  }

  private void setCannotFindHost(final boolean cannotFindHost) {
    if (this.parent instanceof ArcGisResponse<?>) {
      ((ArcGisResponse<?>)this.parent).cannotFindHost = cannotFindHost;
    } else {
      this.cannotFindHost = cannotFindHost;
    }
  }

  public void setCurrentVersion(final double currentVersion) {
    this.currentVersion = currentVersion;
  }

  protected void setInitialized(final boolean initialized) {
    this.initialized = initialized;
  }

  public void setParent(final CatalogElement parent) {
    this.parent = parent;
  }

  @Override
  public void setServiceUrl(final UrlResource serviceUrl) {
    super.setServiceUrl(serviceUrl);
    this.useProxy = serviceUrl.getUriString().matches(".+\\?.+rest%2fservices.*");
  }

  @Override
  public String toString() {
    return getName() + "\t" + getServiceUrl();
  }
}
