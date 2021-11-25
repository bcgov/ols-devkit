package ca.bc.gov.geomark.web.servlet;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeometry.coordinatesystem.model.CoordinateSystem;

import ca.bc.gov.geomark.web.domain.GeomarkConfig;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.io.format.json.JsonList;
import com.revolsys.record.io.format.json.JsonObject;

@WebServlet(urlPatterns = "/api/config")
public class ConfigServlet extends BaseServlet {

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final GeomarkConfig config = GeomarkConfig.getConfig();
    final MapEx footerLinks = config.getFooterLinks();

    final JsonList coordinateSystems = JsonList.array();
    for (final CoordinateSystem coordinateSystem : config.getCoordinateSystems()) {
      final JsonObject coordinateSystemObject = JsonObject.hash() //
        .add("id", coordinateSystem.getHorizontalCoordinateSystemId()) //
        .add("name", coordinateSystem.getCoordinateSystemName()) //
      ;
      coordinateSystems.add(coordinateSystemObject);
    }

    final JsonList fileFormats = config.getFileFormats();
    final JsonObject configResponse = JsonObject.hash() //
      .add("coordinateSystems", coordinateSystems)//
      .add("fileFormats", fileFormats)//
      .add("footerLinks", footerLinks)//
    ;
    for (final String propertyName : Arrays.asList("createFromClipboardTutorialUrl",
      "createFromGeomarkTutorialUrl", "createFromFileTutorialUrl", "googleEarthTurorialUrl",
      "businessHomeUrl")) {
      final String value = config.getProperty(propertyName);
      configResponse.put(propertyName, value);
    }
    writeJsonMap(response, configResponse);
  }
}
