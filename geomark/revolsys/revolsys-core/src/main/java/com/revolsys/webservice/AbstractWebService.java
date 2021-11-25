package com.revolsys.webservice;

import java.util.Map;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.PasswordUtil;
import com.revolsys.util.Property;

public abstract class AbstractWebService<V> extends BaseObjectWithProperties
  implements WebService<V> {

  private String name;

  private UrlResource serviceUrl;

  private String username;

  private String password;

  public AbstractWebService() {
  }

  public AbstractWebService(final String serviceUrl) {
    this(new UrlResource(serviceUrl));
  }

  public AbstractWebService(final UrlResource serviceUrl) {
    this.username = serviceUrl.getUsername();
    this.password = serviceUrl.getPassword();
    this.serviceUrl = serviceUrl;
  }

  public AbstractWebService(final UrlResource serviceUrl, final String username,
    final String password) {
    this.username = username;
    this.password = PasswordUtil.decrypt(password);
    this.serviceUrl = serviceUrl.newUrlResourceAuthorization(username, password);
  }

  public MapEx getConnectionProperties() {
    return new LinkedHashMapEx().add("name", this.name);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public UrlResource getServiceUrl() {
    return this.serviceUrl;
  }

  @Override
  public String getUsername() {
    return this.username;
  }

  @Override
  public WebService<?> getWebService() {
    return this;
  }

  public abstract String getWebServiceTypeName();

  protected UrlResource newServiceUrlResource(final Map<String, Object> parameters) {
    return this.serviceUrl.newUrlResource(parameters);
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  public void setPassword(final String password) {
    this.password = PasswordUtil.decrypt(password);
    final UrlResource serviceUrl = this.serviceUrl;
    if (serviceUrl != null) {

      String username = this.username;
      if (username == null) {
        username = serviceUrl.getUsername();
      }
      this.serviceUrl = serviceUrl.newUrlResourceAuthorization(username, this.password);
    }
  }

  public void setServiceUrl(final String serviceUrl) {

    final UrlResource resource;
    if (serviceUrl == null) {
      resource = null;
    } else {
      resource = new UrlResource(serviceUrl);
    }
    setServiceUrl(resource);
  }

  public void setServiceUrl(final UrlResource serviceUrl) {
    final String username = serviceUrl.getUsername();
    if (Property.hasValue(username)) {
      this.username = username;
    }
    final String password = serviceUrl.getPassword();
    if (Property.hasValue(password)) {
      this.password = password;
    }
    this.serviceUrl = serviceUrl.newUrlResourceAuthorization(this.username, this.password);
  }

  public void setServiceUrl(final UrlResource serviceUrl, final String username,
    final String password) {
    this.username = username;
    this.password = PasswordUtil.decrypt(password);
    this.serviceUrl = serviceUrl.newUrlResourceAuthorization(username, password);
  }

  public void setUsername(final String username) {
    this.username = username;
    final UrlResource serviceUrl = this.serviceUrl;
    if (serviceUrl != null) {
      String password = this.password;
      if (password == null) {
        password = serviceUrl.getPassword();
      }
      this.serviceUrl = serviceUrl.newUrlResourceAuthorization(username, password);
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = newTypeMap(getWebServiceTypeName());
    final String name = getName();
    addToMap(map, "name", name);
    map.put("serviceUrl", this.serviceUrl);
    addToMap(map, "username", this.username);
    addToMap(map, "password", PasswordUtil.encrypt(this.password));
    return map;
  }

  @Override
  public String toString() {
    return this.name + " " + this.serviceUrl;
  }
}
