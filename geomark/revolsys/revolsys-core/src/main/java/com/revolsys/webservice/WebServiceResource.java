package com.revolsys.webservice;

import org.jeometry.common.io.PathName;

import com.revolsys.collection.NameProxy;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.IconNameProxy;

public interface WebServiceResource extends NameProxy, IconNameProxy {
  default <R extends WebServiceResource> R getChild(final String name) {
    return null;
  }

  default <R extends WebServiceResource> R getParent() {
    return null;
  }

  default String getPathElement() {
    return getName();
  }

  default PathName getPathName() {
    final WebServiceResource parent = getParent();
    if (parent == null) {
      return PathName.ROOT;
    } else {
      final PathName parentPathName = parent.getPathName();
      final String name = getPathElement();
      return parentPathName.newChild(name);
    }
  }

  default WebServiceResource getRoot() {
    WebServiceResource element = this;
    for (WebServiceResource parent = element.getParent(); parent != null; parent = element
      .getParent()) {
      element = parent;
    }
    return element;
  }

  default UrlResource getRootServiceUrl() {
    final WebServiceResource root = getRoot();
    return root.getServiceUrl();
  }

  UrlResource getServiceUrl();

  default UrlResource getServiceUrl(final String child) {
    final UrlResource serviceUrl = getServiceUrl();
    return serviceUrl.newChildResource(child);
  }

  WebService<?> getWebService();

  default boolean isHasError() {
    return false;
  }
}
