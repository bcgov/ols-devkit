package ca.bc.gov.geomark.web.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import ca.bc.gov.geomark.web.domain.GeomarkConfig;

public class XForwardHttpServletRequest extends HttpServletRequestWrapper {

  private String serverName;

  private final String scheme;

  private final int port;

  private final String serverUrl;

  public XForwardHttpServletRequest(final HttpServletRequest request) {
    super(request);
    if (GeomarkConfig.getConfig().isUseXForwardFilter()) {
      this.serverName = request.getHeader("x-forwarded-host");
      if (this.serverName == null) {
        this.serverName = request.getServerName();
      }
      this.scheme = "https";
      this.port = 443;
    } else {
      this.serverName = request.getServerName();
      this.scheme = request.getScheme();
      this.port = request.getServerPort();

    }
    this.serverUrl = GeomarkConfig.getConfig().getServerUrl(request);

    setAttribute("serverUrl", this.serverUrl);
  }

  @Override
  public StringBuffer getRequestURL() {
    final StringBuffer url = new StringBuffer(this.serverUrl);
    final String contextPath = getContextPath();
    if (contextPath != null) {
      url.append(contextPath);
    }
    final String servletPath = getServletPath();
    if (servletPath != null) {
      url.append(servletPath);
    }
    final String pathInfo = getPathInfo();
    if (pathInfo != null) {
      url.append(pathInfo);
    }
    return url;
  }

  @Override
  public String getScheme() {
    return this.scheme;
  }

  @Override
  public String getServerName() {
    return this.serverName;
  }

  @Override
  public int getServerPort() {
    return this.port;
  }
}
