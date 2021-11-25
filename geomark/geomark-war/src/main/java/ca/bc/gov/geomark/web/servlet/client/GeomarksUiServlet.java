package ca.bc.gov.geomark.web.servlet.client;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.geomark.web.domain.GeomarkConfig;
import ca.bc.gov.geomark.web.servlet.BaseServlet;

@WebServlet(urlPatterns = "/geomarks/*", loadOnStartup = 1)
@MultipartConfig()
public class GeomarksUiServlet extends BaseServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final String path = request.getPathInfo();
    String redirectPath = null;
    String forwardPath = null;
    request.setAttribute("geomarkConfig", GeomarkConfig.getConfig());
    if (path == null) {
      forwardPath = "/index";
    } else {
      if (path.equals("/index.kml")) {
        forwardPath = "/WEB-INF/jsp/geomarks/index.kml.jsp";
      } else if (path.equals("/created.kml")) {
        forwardPath = "/WEB-INF/jsp/geomarks/created.kml.jsp";
      } else if (path.equals("/createKmlFailure.kml")) {
        forwardPath = "/WEB-INF/jsp/geomarks/createKmlFailure.kml.jsp";
      } else {
        redirectPath = "/api/geomarks" + path;
        if (path.startsWith("/new-ajax")) {
        } else if (path.startsWith("/copy-ajax")) {
        } else if (!path.startsWith("/gm-")) {
          forwardPath = "/index";
        } else {
          final int slashIndex = path.indexOf('/', 1);
          final int dotIndex = path.indexOf('.');
          if (slashIndex == -1 && dotIndex == -1) {
            forwardPath = "/index";
          } else if (slashIndex != -1) {
            final String page = path.substring(slashIndex + 1);
            if (page.equals("map") || page.equals("info") || page.equals("download")) {
              forwardPath = "/index";
            }
          }
        }
      }
    }
    if (forwardPath != null) {
      forward(request, response, forwardPath);
    } else {
      final String geomarkUrl = getAbsoluteUrl(request, redirectPath);
      response.sendRedirect(geomarkUrl);
    }
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    String forwardPath;
    final String path = request.getPathInfo();
    if (path.equals("/created.kml")) {
      forwardPath = "/WEB-INF/jsp/geomarks/created.kml.jsp";
    } else if (path.equals("/createKmlFailure.kml")) {
      forwardPath = "/WEB-INF/jsp/geomarks/createKmlFailure.kml.jsp";
    } else {
      forwardPath = "/api/geomarks" + path;
    }
    forward(request, response, forwardPath);
  }

}
