package com.revolsys.net.oauth;

import javax.servlet.http.HttpServletRequest;

public class AuthorizeResponse {

  private final String sessionState;

  private final String state;

  private final String code;

  public AuthorizeResponse(final OpenIdConnectClient client, final HttpServletRequest request,
    final String scope) {
    this.code = request.getParameter("code");
    this.state = request.getParameter("state");
    this.sessionState = request.getParameter("session_state");
  }

  public String getCode() {
    return this.code;
  }

  public String getSessionState() {
    return this.sessionState;
  }

  public String getState() {
    return this.state;
  }

  @Override
  public String toString() {
    return this.code;
  }
}
