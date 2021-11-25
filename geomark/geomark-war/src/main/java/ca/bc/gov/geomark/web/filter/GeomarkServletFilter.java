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

import org.jeometry.common.logging.Logs;

@WebFilter(urlPatterns = "/*")
public class GeomarkServletFilter implements Filter {
  private static final String FILTER_ID = GeomarkServletFilter.class.getName();

  @Override
  public void destroy() {
  }

  @Override
  public final void doFilter(final ServletRequest request, final ServletResponse response,
    final FilterChain filterChain) throws ServletException, IOException {
    try {
      if (request.getAttribute(FILTER_ID) == Boolean.TRUE) {
        filterChain.doFilter(request, response);
      } else {
        request.setAttribute(FILTER_ID, Boolean.TRUE);
        if (request.getCharacterEncoding() == null) {
          request.setCharacterEncoding("UTF-8");
        }
        final HttpServletRequest overrideRequest = new XForwardHttpServletRequest(
          (HttpServletRequest)request);
        filterChain.doFilter(overrideRequest, response);
      }
    } catch (final IOException e) {
      throw e;
    } catch (final ServletException e) {
      throw e;
    } catch (final RuntimeException e) {
      logRequestException(this, request, e);
      throw e;
    } catch (final Error e) {
      logRequestException(this, request, e);
      throw e;
    }
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
  }

  private void logRequestException(final Object logCategory, final HttpServletRequest request,
    final Throwable exception, final String[] headerNames) {
    if (!(exception instanceof IOException) || !exception.getMessage().contains("Broken pipe")) {
      if (request == null) {
        Logs.error(logCategory, exception);
      } else if (request.getAttribute("LogException") != exception) {
        final StringBuilder text = new StringBuilder();
        final String message = exception.getMessage();
        if (message != null) {
          text.append(message);
        }
        final String method = request.getMethod();
        final StringBuffer requestURL = request.getRequestURL();
        final String query = request.getQueryString();

        requestURL.insert(0, method + " ");
        if (query != null) {
          requestURL.append('?').append(query);
        }
        text.append('\n').append("URL\t").append(requestURL);

        final String referer = request.getHeader("Referer");
        if (referer != null) {
          text.append('\n').append("Referer\t").append(referer);
        }

        final String remoteUser = request.getRemoteUser();
        if (remoteUser != null) {
          text.append('\n').append("RemoteUser\t").append(remoteUser);
        }

        if (headerNames != null) {
          for (final String headerName : headerNames) {
            final String value = request.getHeader(headerName);
            if (value != null) {
              text.append('\n').append(headerName).append('\t').append(value);
            }
          }
        }
        Logs.error(logCategory, text.toString(), exception);
        request.setAttribute("LogException", exception);
      }
    }
  }

  private void logRequestException(final Object logCategory, final ServletRequest request,
    final Throwable exception) {
    HttpServletRequest httpRequest = null;
    if (request instanceof HttpServletRequest) {
      httpRequest = (HttpServletRequest)request;
    }
    logRequestException(logCategory, httpRequest, exception, null);
  }

}
