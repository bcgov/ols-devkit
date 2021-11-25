package com.revolsys.webservice;

import java.util.List;

import org.jeometry.common.io.PathName;

import com.revolsys.collection.Parent;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.spring.resource.UrlResource;

public interface WebService<V>
  extends MapSerializer, Parent<V>, WebServiceResource, ObjectWithProperties {

  @SuppressWarnings("unchecked")
  default <FL extends WebServiceFeatureLayer> FL getFeatureLayer(final PathName pathName) {
    return (FL)getWebServiceResource(pathName, WebServiceFeatureLayer.class);
  }

  String getPassword();

  @Override
  UrlResource getServiceUrl();

  String getUsername();

  @SuppressWarnings("unchecked")
  default <T extends WebServiceResource> T getWebServiceResource(final PathName pathName) {
    final List<String> elements = pathName.getElements();
    if (elements.isEmpty()) {
      return (T)this;
    } else {
      WebServiceResource resource = getChild(elements.get(0));
      for (int i = 1; resource != null && i < elements.size(); i++) {
        final String childLayerName = elements.get(i);
        resource = resource.getChild(childLayerName);
      }
      return (T)resource;
    }
  }

  @SuppressWarnings("unchecked")
  default <T extends WebServiceResource> T getWebServiceResource(final PathName pathName,
    final Class<T> elementClass) {
    if (pathName != null) {
      final List<String> elements = pathName.getElements();
      if (!elements.isEmpty()) {
        final String firstElementName = elements.get(0);
        WebServiceResource resource = getChild(firstElementName);
        for (int i = 1; resource != null && i < elements.size(); i++) {
          if (resource.isHasError()) {
            return null;
          }
          final String childLayerName = elements.get(i);
          resource = resource.getChild(childLayerName);
          if (resource == null || resource.isHasError()) {
            return null;
          }
        }
        if (resource == null) {
          return null;
        } else if (elementClass.isAssignableFrom(resource.getClass())) {
          return (T)resource;
        } else {
          return null;
        }
      }
    }
    return null;
  }

  default boolean isClosed() {
    return false;
  }

  @SuppressWarnings("unchecked")
  default <Q extends WebServiceFeatureLayerQuery> Q newQuery(final PathName pathName) {
    final WebServiceFeatureLayer layer = getWebServiceResource(pathName,
      WebServiceFeatureLayer.class);
    if (layer == null) {
      throw new IllegalArgumentException("Layer not found:" + pathName);
    } else {
      return (Q)layer.newQuery();
    }
  }

  default <Q extends WebServiceFeatureLayerQuery> Q newQuery(final String path) {
    final PathName pathName = PathName.newPathName(path);
    return newQuery(pathName);
  }

  void setName(String name);
}
