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

import ca.bc.gov.geomark.web.domain.GeomarkConfig;

import com.revolsys.util.Property;

@WebFilter(urlPatterns = "/secure/admin/*")
public class AdminSecurityFilter implements Filter {

  public static String getUserId(final HttpServletRequest request) {
    String siteMinderId = request.getHeader("SM_UNIVERSALID");
    if (siteMinderId == null) {
      return request.getRemoteUser();
    } else {
      siteMinderId = siteMinderId.toLowerCase();
      siteMinderId = siteMinderId.replace('\\', ':');
      final int index = siteMinderId.indexOf(':');
      if (index == -1) {
        final String userType = request.getHeader("SMGOV_USERTYPE");
        if (userType.equalsIgnoreCase("INTERNAL") || userType.equalsIgnoreCase("IDIR")) {
          return "idir:" + siteMinderId;
        } else {
          return "bceid:" + siteMinderId;
        }
      } else {
        return siteMinderId;
      }
    }
  }

  public static boolean isAdminUser(final String username) {
    final String adminUserNames = GeomarkConfig.getConfig().getString("adminUserNames");
    if (Property.hasValue(adminUserNames)) {
      final String[] names = adminUserNames.split(",");
      for (String name : names) {
        if (Property.hasValue(name)) {
          name = name.trim();
          name = name.toLowerCase();
          name = name.replace('\\', ':');
          if (!name.startsWith("idir:")) {
            name = "idir:" + name;
          }

          if (username.equals(name.trim())) {
            return true;
          }
        }
      }
      return false;
    } else {
      return true;
    }
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
    final FilterChain filterChain) throws ServletException, IOException {
    final HttpServletRequest request = (HttpServletRequest)servletRequest;
    final HttpServletResponse response = (HttpServletResponse)servletResponse;
    final String userId = getUserId(request);
    if (isAdminUser(userId)) {
      filterChain.doFilter(request, response);
    } else {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
  }

}
