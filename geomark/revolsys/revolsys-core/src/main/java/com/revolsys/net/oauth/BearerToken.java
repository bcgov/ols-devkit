package com.revolsys.net.oauth;

import java.time.Instant;

import com.revolsys.record.io.format.json.JsonObject;

public class BearerToken {

  private final String accessToken;

  private long expireTime;

  private String scope;

  private String returnedScope;

  public BearerToken(final JsonObject config) {
    this(config, null);
  }

  public BearerToken(final JsonObject config, final String scope) {
    this.accessToken = config.getString("access_token");
  }

  public String getAccessToken() {
    return this.accessToken;
  }

  public Instant getExpireTime() {
    return Instant.ofEpochMilli(this.expireTime);
  }

  public String getReturnedScope() {
    return this.returnedScope;
  }

  public String getScope() {
    return this.scope;
  }

  public boolean isExpired() {
    return System.currentTimeMillis() >= this.expireTime;
  }

  protected void setExpireTime(final long expireTime) {
    this.expireTime = expireTime;
  }

  public void setScope(final String scope, final String returnedScope) {
    this.returnedScope = returnedScope;
    if (scope == null) {
      this.scope = returnedScope;
    } else {
      this.scope = scope;
    }
  }

  @Override
  public String toString() {
    return this.accessToken;
  }

}
