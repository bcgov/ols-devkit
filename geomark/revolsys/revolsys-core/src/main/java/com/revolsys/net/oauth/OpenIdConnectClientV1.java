package com.revolsys.net.oauth;

import java.util.function.Function;

import org.apache.http.client.methods.RequestBuilder;

import com.revolsys.net.http.ApacheHttp;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.spring.resource.Resource;

public class OpenIdConnectClientV1 extends OpenIdConnectClient {

  public static OpenIdConnectClientV1 microsoftV1(final String tenantId) {
    final String url = String
      .format("https://login.microsoftonline.com/%s/.well-known/openid-configuration", tenantId);
    return newClientV1(url);
  }

  public static OpenIdConnectClientV1 microsoftV1Common() {
    return microsoftV1("common");
  }

  private static OpenIdConnectClientV1 newClientV1(final String url) {
    final Resource resource = Resource.getResource(url);
    final JsonObject config = JsonParser.read((Object)resource);
    if (config == null || config.isEmpty()) {
      throw new IllegalArgumentException("Not a valid .well-known/openid-configuration");
    } else {
      final OpenIdConnectClientV1 client = new OpenIdConnectClientV1(config);
      client.setUrl(url);
      return client;
    }
  }

  public OpenIdConnectClientV1(final JsonObject config) {
    super(config);
  }

  public Function<BearerToken, BearerToken> bearerTokenRefreshFactory(
    final OpenIdResource resource) {
    return bearerToken -> tokenClientCredentials(resource);
  }

  private OpenIdBearerToken getOpenIdBearerToken(final RequestBuilder requestBuilder,
    final OpenIdResource resource) {
    final JsonObject response = ApacheHttp.getJson(requestBuilder);
    return new OpenIdBearerToken(this, response, resource);
  }

  public OpenIdBearerToken tokenClientCredentials(final OpenIdResource resource) {
    final RequestBuilder requestBuilder = tokenBuilder("client_credentials", true);
    if (resource != null) {
      requestBuilder.addParameter("resource", resource.getResource());
    }
    return getOpenIdBearerToken(requestBuilder, resource);
  }

  public OpenIdBearerToken tokenRefresh(final String refreshToken, final OpenIdResource resource) {
    final RequestBuilder requestBuilder = tokenBuilder("refresh_token", true);
    requestBuilder.addParameter("refresh_token", refreshToken);
    if (resource != null) {
      requestBuilder.addParameter("resource", resource.getResource());
    }
    return getOpenIdBearerToken(requestBuilder, resource);
  }
}
