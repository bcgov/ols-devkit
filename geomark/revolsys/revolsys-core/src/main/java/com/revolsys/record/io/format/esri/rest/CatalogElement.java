package com.revolsys.record.io.format.esri.rest;

import com.revolsys.spring.resource.UrlResource;
import com.revolsys.webservice.WebServiceResource;

public interface CatalogElement extends WebServiceResource {

  @Override
  default UrlResource getServiceUrl(final String child) {
    if (isUseProxy()) {
      final UrlResource serviceUrl = getServiceUrl();
      final String newUrl = serviceUrl.getUriString() + "%2F" + child;
      final String username = serviceUrl.getUsername();
      final String password = serviceUrl.getPassword();
      return new UrlResource(newUrl, username, password);
    } else {
      return WebServiceResource.super.getServiceUrl(child);
    }
  }

  default boolean isUseProxy() {
    return false;
  }
}
