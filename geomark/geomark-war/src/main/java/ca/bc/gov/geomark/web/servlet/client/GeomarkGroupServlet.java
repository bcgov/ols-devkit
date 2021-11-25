package ca.bc.gov.geomark.web.servlet.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.geomark.client.util.UrlUtil;
import ca.bc.gov.geomark.web.domain.GeomarkConstants;
import ca.bc.gov.geomark.web.servlet.BaseServlet;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.Strings;

@WebServlet(urlPatterns = {
  "/geomarkGroups/*", "/api/geomarkGroups/*"
}, loadOnStartup = 1)
@MultipartConfig()
public class GeomarkGroupServlet extends BaseServlet {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final String[] parts = pathParts(request);
    if (parts.length == 3) {
      if (parts[1].equals("geomarks")) {
        final String geomarkGroupId = parts[0];
        if (parts[2].startsWith("add")) {
          doPostAddGeomarkToGroup(request, response, geomarkGroupId);
          return;
        } else if (parts[2].startsWith("delete")) {
          doPostDeleteGeomarkFromGroup(request, response, geomarkGroupId);
          return;
        }
      }
    }
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  private void doPostAddGeomarkToGroup(final HttpServletRequest request,
    final HttpServletResponse response, final String geomarkGroupId)
    throws ServletException, IOException {
    final String[] geomarkIds = request.getParameterValues("geomarkId");
    final long time = Long.parseLong(request.getParameter("time"));
    final String signature = request.getParameter("signature");
    final Record geomarkGroup = this.recordStore.getRecord(GeomarkConstants.GEOMARK_GROUP,
      geomarkGroupId);
    if (geomarkGroup == null) {
      writeError(response, "Geomark group not found: " + geomarkGroupId);
    } else if (geomarkIds == null || geomarkIds.length == 0) {
      writeError(response, "At least one geomarkId must be specified.");
    } else {
      final String geomarkGroupSecretKey = geomarkGroup.getValue(GeomarkConstants.SECRET_KEY);
      final String path = "/geomarkGroups/" + geomarkGroupId + "/geomarks/add";
      final Map<String, List<String>> parameters = Collections.singletonMap("geomarkId",
        Arrays.asList(geomarkIds));
      if (!validateSignature(geomarkGroupSecretKey, path, parameters, time, signature)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid signature");
        return;
      }
      final List<String> notFoundIds = new ArrayList<>();
      final List<String> addedIds = new ArrayList<>();

      for (String geomarkId : geomarkIds) {
        geomarkId = Strings.trim(geomarkId);
        try (
          final RecordWriter writer = this.recordStore.newRecordWriter()) {
          if (addGeomarkToGroup(writer, geomarkGroupId, geomarkId)) {
            addedIds.add(geomarkId);
          } else {
            notFoundIds.add(geomarkId);
          }
        }
      }
      final JsonObject result = JsonObject.hash();
      if (notFoundIds.isEmpty()) {
        result.put("status", "Added");
        result.put("geomarkGroupId", geomarkGroupId);
        result.put("geomarkIds", addedIds);
      } else {
        if (addedIds.isEmpty()) {
          result.put("status", "AddedAndNotFound");
          result.put("geomarkGroupId", geomarkGroupId);
        } else {
          result.put("status", "NotFound");
          result.put("geomarkGroupId", geomarkGroupId);
          result.put("geomarkIds", addedIds);
        }
        result.put("notFoundGeomarkIds", addedIds);
      }
      writeJsonMap(response, result);
    }
  }

  private void doPostDeleteGeomarkFromGroup(final HttpServletRequest request,
    final HttpServletResponse response, final String geomarkGroupId) throws IOException {
    final String[] geomarkIds = request.getParameterValues("geomarkId");
    final String expiry = request.getParameter("expiryDays");
    final int expiryDays;
    if (expiry == null) {
      expiryDays = -1;
    } else {
      expiryDays = Integer.parseInt(expiry);
    }
    final long time = Long.parseLong(request.getParameter("time"));
    final String signature = request.getParameter("signature");
    try (
      Transaction transaction = this.recordStore.newTransaction();
      RecordWriter writer = this.recordStore.newRecordWriter();) {
      final Record geomarkGroup = this.recordStore.getRecord(GeomarkConstants.GEOMARK_GROUP,
        geomarkGroupId);
      if (geomarkGroup == null) {
        writeError(response, "Geomark group not found: " + geomarkGroupId);
      } else if (geomarkIds == null || geomarkIds.length == 0) {
        writeError(response, "At least one geomarkId must be specified.");
      } else {
        final String geomarkGroupSecretKey = geomarkGroup.getValue(GeomarkConstants.SECRET_KEY);
        final String path = "/geomarkGroups/" + geomarkGroupId + "/geomarks/delete";
        final Map<String, Object> parameters = new TreeMap<>();
        if (expiryDays != -1) {
          parameters.put("expiryDays", expiryDays);
        }
        parameters.put("geomarkId", geomarkIds);
        if (!validateSignature(geomarkGroupSecretKey, path, parameters, time, signature)) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid signature");
          return;
        }
        final Map<String, Object> filter = new LinkedHashMap<>();
        filter.put(GeomarkConstants.GEOMARK_GROUP_ID, geomarkGroupId);
        for (String geomarkId : geomarkIds) {
          geomarkId = Strings.trim(geomarkId);
          filter.put(GeomarkConstants.GEOMARK_ID, geomarkId);
          deleteGeomarkFromGroup(writer, geomarkGroupId, geomarkId, expiryDays);
        }
      }
      final JsonObject result = JsonObject.hash();
      result.put("status", "DELETED");
      result.put("geomarkGroupId", geomarkGroupId);
      result.put("geomarkIds", Arrays.asList(geomarkIds));
      writeJsonMap(response, result);
    }
  }

  /**
  * Validate the signature.
  *
  * @param secretKey The secret key used to validate the signature.
  * @param path The path.
  * @param parameters The map of parameters.
  * @param signature The signature of the request.
  * @param time The time in milliseconds.
  */
  private boolean validateSignature(final String secretKey, final String path,
    final Map<String, ? extends Object> parameters, final long time, final String signature) {
    final String expectedSignature = UrlUtil.sign(secretKey, path, time, parameters);
    return signature.equals(expectedSignature);
  }

}
