package ca.bc.gov.geomark.web.servlet;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.geomark.web.filter.AdminSecurityFilter;

import com.revolsys.record.io.format.json.JsonObject;

@WebServlet(urlPatterns = "/secure/api/authentication", loadOnStartup = 1)
public class AuthenticationServlet extends BaseServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final String username = AdminSecurityFilter.getUserId(request);
    final JsonObject data = JsonObject.hash("name", username);

    if (AdminSecurityFilter.isAdminUser(username)) {
      data.add("roles", Collections.singletonList("admin"));
    }
    writeJsonMap(response, data);
  }
}
