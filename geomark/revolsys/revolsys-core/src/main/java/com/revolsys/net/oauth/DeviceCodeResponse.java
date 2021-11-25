package com.revolsys.net.oauth;

import com.revolsys.net.http.ApacheHttpException;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonParser;

public class DeviceCodeResponse {

  private final OpenIdConnectClient client;

  private final String deviceCode;

  private final String expiresIn;

  private final int interval;

  private final String message;

  private final String userCode;

  private final String verificationUri;

  private final String scope;

  public DeviceCodeResponse(final OpenIdConnectClient client, final JsonObject response,
    final String scope) {
    this.client = client;
    this.deviceCode = response.getString("device_code");
    this.userCode = response.getString("user_code");
    this.verificationUri = response.getString("verification_uri");
    this.expiresIn = response.getString("expires_in");
    this.interval = response.getInteger("interval");
    this.message = response.getString("message");
    this.scope = scope;
  }

  public String getDeviceCode() {
    return this.deviceCode;
  }

  public String getExpiresIn() {
    return this.expiresIn;
  }

  public int getInterval() {
    return this.interval;
  }

  public String getMessage() {
    return this.message;
  }

  public OpenIdBearerToken getToken() {
    while (true) {
      synchronized (this) {
        try {
          this.wait(this.interval * 1000);
        } catch (final InterruptedException e) {
          return null;
        }
      }
      try {
        return this.client.tokenDeviceCode(this.deviceCode, this.scope);
      } catch (final ApacheHttpException e) {
        final String errorText = e.getContent();
        try {
          final JsonObject json = JsonParser.read(errorText);
          final String error = json.getString("error");
          if ("authorization_pending".equals(error)) {
            // wait and try again
          } else if ("authorization_declined".equals(error)) {
            throw e;
          } else if ("bad_verification_code".equals(error)) {
            System.err.println(this.message);
          } else if ("expired_token".equals(error)) {
            throw e;
          } else {
            throw e;
          }
        } catch (final Exception e1) {
          throw e;
        }
      }
    }
  }

  public String getUserCode() {
    return this.userCode;
  }

  public String getVerificationUri() {
    return this.verificationUri;
  }

  @Override
  public String toString() {
    return this.deviceCode;
  }
}
