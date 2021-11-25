package com.revolsys.net.oauth;

import java.net.URI;
import java.util.Collection;
import java.util.function.Function;

import org.apache.http.client.methods.RequestBuilder;

import com.revolsys.io.map.ObjectFactoryConfig;
import com.revolsys.net.http.ApacheHttp;
import com.revolsys.net.http.ApacheHttpException;
import com.revolsys.net.http.exception.AuthenticationException;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Strings;

public class OpenIdConnectClient extends BaseObjectWithProperties {

  public static OpenIdConnectClient google() {
    return newClient("https://accounts.google.com/.well-known/openid-configuration");
  }

  public static OpenIdConnectClient microsoft(final String tenantId) {
    final String url = String.format(
      "https://login.microsoftonline.com/%s/v2.0/.well-known/openid-configuration", tenantId);
    return newClient(url);
  }

  public static OpenIdConnectClient microsoftCommon() {
    return microsoft("common");
  }

  public static OpenIdConnectClient newClient(final ObjectFactoryConfig factoryConfig,
    final JsonObject config, final String defaultPrefix) {
    final String url = config.getString("wellKnownUrl");
    OpenIdConnectClient client;
    if (url == null) {
      client = factoryConfig.getValue(defaultPrefix + "OidcClient");
    } else {
      client = newClient(url);
    }
    if (client == null) {
      throw new IllegalArgumentException("Cannot create OpenIDConnect client for: " + config);
    }
    final String clientId = config.getString("clientId");
    if (clientId != null) {
      client.setClientId(clientId);
    }
    final String clientSecret = config.getString("clientSecret");
    if (clientSecret != null) {
      client.setClientSecret(clientSecret);
    }
    return client;
  }

  public static OpenIdConnectClient newClient(final String url) {
    final Resource resource = Resource.getResource(url);
    final JsonObject config = JsonParser.read((Object)resource);
    if (config == null || config.isEmpty()) {
      throw new IllegalArgumentException("Not a valid .well-known/openid-configuration");
    } else {
      final OpenIdConnectClient client = new OpenIdConnectClient(config);
      client.setUrl(url);
      return client;
    }
  }

  private final String issuer;

  private final String authorizationEndpoint;

  private final String deviceAuthorizationEndpoint;

  private final String tokenEndpoint;

  private final String userinfoEndpoint;

  private final String revocationEndpoint;

  private String clientId;

  private String clientSecret;

  private String url;

  private final String endSessionEndpoint;

  public OpenIdConnectClient(final JsonObject config) {
    this.issuer = config.getString("issuer");
    this.authorizationEndpoint = config.getString("authorization_endpoint");
    this.deviceAuthorizationEndpoint = config.getString("device_authorization_endpoint");
    this.tokenEndpoint = config.getString("token_endpoint");
    this.userinfoEndpoint = config.getString("userinfo_endpoint");
    this.revocationEndpoint = config.getString("revocation_endpoint");
    this.endSessionEndpoint = config.getString("end_session_endpoint");
  }

  protected void addScopes(final RequestBuilder builder, final Collection<String> scopes) {
    final String scope = Strings.toString(" ", scopes);
    builder.addParameter("scope", scope);
  }

  public URI authorizationUrl(final String scope, final String redirectUri, final String state,
    final String nonce, final String prompt) {
    final RequestBuilder builder = authorizationUrlBuilder(scope, redirectUri, state, nonce,
      prompt);
    return builder.build().getURI();
  }

  public RequestBuilder authorizationUrlBuilder(final String scope, final String redirectUri,
    final String state, final String nonce, final String prompt) {
    final RequestBuilder builder = RequestBuilder//
      .get(this.authorizationEndpoint)
      .addParameter("response_type", "code")
      .addParameter("response_mode", "query")
      .addParameter("client_id", this.clientId)
      .addParameter("scope", scope)
      .addParameter("redirect_uri", redirectUri)
      .addParameter("state", state)
      .addParameter("nonce", nonce);
    if (prompt != null) {
      builder.addParameter("prompt", prompt);
    }
    return builder;
  }

  public Function<BearerToken, BearerToken> bearerTokenRefreshFactory(final String scope) {
    return bearerToken -> tokenClientCredentials(scope);
  }

  public DeviceCodeResponse deviceCode(final String scope) {
    final RequestBuilder requestBuilder = RequestBuilder//
      .post(this.deviceAuthorizationEndpoint);
    if (this.clientId != null) {
      requestBuilder.addParameter("client_id", this.clientId);
    }
    if (scope != null) {
      requestBuilder.addParameter("scope", scope);
    }
    final JsonObject response = ApacheHttp.getJson(requestBuilder);
    return new DeviceCodeResponse(this, response, scope);
  }

  public URI endSessionUrl(final String redirectUrl) {
    final RequestBuilder builder = RequestBuilder//
      .get(this.endSessionEndpoint)
      .addParameter("post_logout_redirect_uri", redirectUrl);
    return builder.build().getURI();
  }

  public String getAuthorizationEndpoint() {
    return this.authorizationEndpoint;
  }

  public String getDeviceAuthorizationEndpoint() {
    return this.deviceAuthorizationEndpoint;
  }

  public String getEndSessionEndpoint() {
    return this.endSessionEndpoint;
  }

  public String getIssuer() {
    return this.issuer;
  }

  private OpenIdBearerToken getOpenIdBearerToken(final RequestBuilder requestBuilder,
    final String scope) {
    try {
      final JsonObject response = ApacheHttp.getJson(requestBuilder);
      return new OpenIdBearerToken(this, response, scope);
    } catch (final ApacheHttpException e) {
      if (e.getStatusCode() == 400) {
        JsonObject error = null;
        try {
          final String content = e.getContent();
          error = JsonParser.read(content);
        } catch (final Exception e2) {
        }
        if (error != null) {
          String errorDescription = error.getString("error_description");
          if (errorDescription != null) {
            final int index = errorDescription.indexOf("Trace ID:");
            if (index != -1) {
              errorDescription = errorDescription.substring(0, index).trim();
            }
            throw new AuthenticationException(errorDescription);
          }
        }
      }
      throw e;
    }
  }

  public String getRevocationEndpoint() {
    return this.revocationEndpoint;
  }

  public String getTokenEndpoint() {
    return this.tokenEndpoint;
  }

  public String getUrl() {
    return this.url;
  }

  public String getUserinfoEndpoint() {
    return this.userinfoEndpoint;
  }

  public OpenIdConnectClient setClientId(final String clientId) {
    this.clientId = clientId;
    return this;
  }

  public OpenIdConnectClient setClientSecret(final String clientSecret) {
    this.clientSecret = clientSecret;
    return this;
  }

  protected void setUrl(final String url) {
    this.url = url;
  }

  public OpenIdBearerToken tokenAuthorizationCode(final String code, final String redirectUri,
    final String scope) {
    final RequestBuilder builder = RequestBuilder//
      .post(this.tokenEndpoint)
      .addParameter("grant_type", "authorization_code")
      .addParameter("client_id", this.clientId)
      .addParameter("client_secret", this.clientSecret)
      .addParameter("redirect_uri", redirectUri)
      .addParameter("code", code);
    return getOpenIdBearerToken(builder, scope);
  }

  protected RequestBuilder tokenBuilder(final String grantType, final boolean useClientSecret) {
    final RequestBuilder builder = RequestBuilder//
      .post(this.tokenEndpoint)
      .addParameter("grant_type", grantType);
    if (this.clientId != null) {
      builder.addParameter("client_id", this.clientId);
    }
    if (this.clientSecret != null && useClientSecret) {
      builder.addParameter("client_secret", this.clientSecret);
    }
    return builder;
  }

  public OpenIdBearerToken tokenClientCredentials(final String scope) {
    final RequestBuilder requestBuilder = tokenBuilder("client_credentials", true);
    if (scope != null) {
      requestBuilder.addParameter("scope", scope);
    }
    return getOpenIdBearerToken(requestBuilder, scope);
  }

  public OpenIdBearerToken tokenDeviceCode(final String deviceCode, final String scope) {
    final String grantType = "urn:ietf:params:oauth:grant-type:device_code";
    final RequestBuilder requestBuilder = tokenBuilder(grantType, false) //
      .addParameter("device_code", deviceCode);

    return getOpenIdBearerToken(requestBuilder, scope);
  }

  public OpenIdBearerToken tokenPassword(final String username, final String password,
    final String scope) {
    final RequestBuilder requestBuilder = tokenBuilder("password", true)//
      .addParameter("username", username)
      .addParameter("password", password);
    if (scope != null) {
      requestBuilder.addParameter("scope", scope);
    }
    return getOpenIdBearerToken(requestBuilder, scope);
  }

  public OpenIdBearerToken tokenRefresh(final String refreshToken, final String scope) {
    final RequestBuilder requestBuilder = tokenBuilder("refresh_token", true);
    requestBuilder //
      .addParameter("refresh_token", refreshToken);
    if (scope != null) {
      requestBuilder.addParameter("scope", scope);
    }
    return getOpenIdBearerToken(requestBuilder, scope);
  }
}
