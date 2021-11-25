package ca.bc.gov.geomark.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.revolsys.util.Property;

@WebFilter(urlPatterns = {
  "/api/*", "/geomarks/*", "/geomarkGroups/*"
})
public class BasicCorsFilter implements Filter {

  public BasicCorsFilter() {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
    final FilterChain filterChain) throws ServletException, IOException {
    final HttpServletRequest request = (HttpServletRequest)servletRequest;
    final HttpServletResponse response = (HttpServletResponse)servletResponse;
    final String method = request.getMethod();
    final String origin = request.getHeader("Origin");
    final String requestMethod = request.getHeader("Access-Control-Request-Method");
    response.addHeader("Access-Control-Allow-Origin", "*");
    if ("OPTIONS".equals(method) && Property.hasValue(origin) && Property.hasValue(requestMethod)) {
      response.addHeader("Access-Control-Allow-Methods", "GET,POST");
      final String requestHeaders = request.getHeader("Access-Control-Request-Headers");
      if (Property.hasValue(requestHeaders)) {
        response.addHeader("Access-Control-Allow-Headers",
          "Accept-Encoding,Accept,Accept-Language");
      }
      response.addHeader("Access-Control-Max-Age", "3600");
    }
    filterChain.doFilter(request, response);
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
  }
}
