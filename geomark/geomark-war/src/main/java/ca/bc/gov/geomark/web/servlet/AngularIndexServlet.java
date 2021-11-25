package ca.bc.gov.geomark.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {
  "/create/*", "/overview", "/secure/admin/*"
}, loadOnStartup = 2)
@MultipartConfig()
public class AngularIndexServlet extends BaseServlet {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    forward(request, response, "/index");
  }

}
