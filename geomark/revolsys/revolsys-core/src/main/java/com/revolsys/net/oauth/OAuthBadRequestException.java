package com.revolsys.net.oauth;

import com.revolsys.record.io.format.json.JsonObject;

public class OAuthBadRequestException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final String correlationId;

  private final String error;

  private final String errorCodes;

  private final String errorDescription;

  private final String errorUri;

  private final String traceId;

  public OAuthBadRequestException(final JsonObject error) {
    super(error.getString("error") + "\n" + error.getString("error_description"));
    this.error = error.getString("error");
    this.errorDescription = error.getString("error_description");
    this.errorCodes = error.getString("error_codes");
    this.traceId = error.getString("trace_id");
    this.correlationId = error.getString("correlation_id");
    this.errorUri = error.getString("error_uri");
  }

  public String getCorrelationId() {
    return this.correlationId;
  }

  public String getError() {
    return this.error;
  }

  public String getErrorCodes() {
    return this.errorCodes;
  }

  public String getErrorDescription() {
    return this.errorDescription;
  }

  public String getErrorUri() {
    return this.errorUri;
  }

  public String getTraceId() {
    return this.traceId;
  }

}
