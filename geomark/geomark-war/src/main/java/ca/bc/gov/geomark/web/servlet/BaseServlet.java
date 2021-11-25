package ca.bc.gov.geomark.web.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.io.PathName;

import ca.bc.gov.geomark.web.domain.GeomarkConfig;
import ca.bc.gov.geomark.web.domain.GeomarkConstants;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactory;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.And;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Equal;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;

public class BaseServlet extends HttpServlet implements GeomarkConstants {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public static String getAbsoluteUrl(final HttpServletRequest request, final String url) {
    if (url == null) {
      return null;
    } else if (url.startsWith("/")) {
      final String serverUrl = getServerUrl(request);
      final String contextPath = request.getContextPath();
      return serverUrl + contextPath + url;
    } else {
      return url;
    }
  }

  public static boolean getBooleanParameter(final HttpServletRequest request,
    final String paramName) {
    final String value = request.getParameter(paramName);
    if (Property.hasValue(value)) {
      return Boolean.parseBoolean(value);
    }
    return false;
  }

  public static String getParameter(final HttpServletRequest request, final String paramName,
    final String defaultValue) {
    final String value = request.getParameter(paramName);
    if (Property.hasValue(value)) {
      return value;
    } else {
      return defaultValue;
    }
  }

  public static String getServerUrl(final HttpServletRequest request) {
    return GeomarkConfig.getConfig().getServerUrl(request);
  }

  public static boolean isInAGeomarkGroup(final RecordStore recordStore, final String geomarkId) {
    final Equal condition = Q.equal(GeomarkConstants.GEOMARK_ID, geomarkId);
    final Query query = new Query(GeomarkConstants.GEOMARK_GROUP_XREF, condition);
    final Record result = recordStore.getRecords(query).getFirst();
    return result != null;
  }

  protected final GeomarkConfig GEOMARK_CONFIG = GeomarkConfig.getConfig();

  protected RecordStore recordStore = this.GEOMARK_CONFIG.getRecordStore();

  protected boolean addGeomarkToGroup(final RecordWriter writer, final String geomarkGroupId,
    final String geomarkId) {
    final Record geomark = this.recordStore.getRecord(GEOMARK_POLY, geomarkId);
    if (geomark == null) {
      return false;
    } else {
      if (!GeomarkConstants.isExpired(geomark)) {
        final Query query = new Query(GeomarkConstants.GEOMARK_GROUP_XREF);
        query.setWhereCondition(new And(Q.equal(GeomarkConstants.GEOMARK_ID, geomarkId),
          Q.equal(GeomarkConstants.GEOMARK_GROUP_ID, geomarkGroupId)));
        if (this.recordStore.getRecords(query).getFirst() == null) {
          final Record xref = this.recordStore.newRecord(GeomarkConstants.GEOMARK_GROUP_XREF);
          final Identifier xrefId = Identifier.newIdentifier("gx-" + newUuid());
          xref.setIdentifier(xrefId);
          xref.setValue(GeomarkConstants.GEOMARK_ID, geomarkId);
          xref.setValue(GeomarkConstants.GEOMARK_GROUP_ID, geomarkGroupId);
          xref.setValue(GeomarkConstants.WHEN_CREATED, new Date(System.currentTimeMillis()));
          writer.write(xref);
        }
        final java.util.Date expiryDate = geomark.getValue(GeomarkConstants.EXPIRY_DATE);
        if (expiryDate.compareTo(GeomarkConstants.MAX_DATE) < 0) {
          geomark.setValue(GeomarkConstants.MIN_EXPIRY_DATE, expiryDate);
          geomark.setValue(GeomarkConstants.EXPIRY_DATE, GeomarkConstants.MAX_DATE);
          writer.write(geomark);
        }
      }
    }
    return true;
  }

  protected void deleteGeomarkFromGroup(final RecordWriter writer, final String geomarkGroupId,
    final String geomarkId, int expiryDays) {
    final int maxExpiryDays = GeomarkConfig.getConfig().getInt("maxGeomarkAgeDays");
    final Condition filter = Q.and(Q.equal(GeomarkConstants.GEOMARK_ID, geomarkId),
      Q.equal(GeomarkConstants.GEOMARK_GROUP_ID, geomarkGroupId));
    final Query query = new Query(GeomarkConstants.GEOMARK_GROUP_XREF, filter);
    this.recordStore.deleteRecords(query);
    final Record geomark = this.recordStore.getRecord(GeomarkConstants.GEOMARK_POLY, geomarkId);
    if (geomark != null) {
      java.util.Date minExpiry = geomark.getValue(GeomarkConstants.MIN_EXPIRY_DATE);
      if (expiryDays > 0) {
        expiryDays = Math.min(maxExpiryDays, expiryDays);
        final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("PST"));
        cal.add(Calendar.DATE, expiryDays);
        final Date newMinExpiry = new java.sql.Date(cal.getTimeInMillis());
        final int compare = newMinExpiry.compareTo(minExpiry);
        if (compare > 0) {
          minExpiry = newMinExpiry;
        }
      }
      geomark.setValue(GeomarkConstants.MIN_EXPIRY_DATE, minExpiry);
      if (!isInAGeomarkGroup(this.recordStore, geomarkId)) {
        geomark.setValue(GeomarkConstants.EXPIRY_DATE, minExpiry);
      }
      writer.write(geomark);
    }
  }

  protected void deleteRecord(final HttpServletResponse response, final PathName pathName,
    final String identifier) throws IOException {
    this.recordStore.deleteRecord(pathName, identifier);
    writeDeleted(response);
  }

  protected void forward(final HttpServletRequest request, final HttpServletResponse response,
    final String path) throws ServletException, IOException {
    forward(request, response, path, MapEx.EMPTY);
  }

  protected void forward(final HttpServletRequest request, final HttpServletResponse response,
    final String path, final Map<String, Object> parameters) throws ServletException, IOException {
    final String fullPath = UrlUtil.getUrl(path, parameters);
    final RequestDispatcher requestDispatcher = request.getRequestDispatcher(fullPath);
    requestDispatcher.forward(request, response);
  }

  protected String newUuid() {
    return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
  }

  protected String[] pathParts(final HttpServletRequest request) {
    final String path = request.getPathInfo();
    if (path == null || path.length() == 1) {
      return new String[0];
    } else {
      return path.substring(1).split("/");
    }
  }

  protected JsonObject readJsonMap(final HttpServletRequest request) throws IOException {
    try (
      BufferedReader reader = request.getReader()) {
      return Json.toMap(reader);
    }
  }

  protected void writeDeleted(final HttpServletResponse response) throws IOException {
    writeJsonMap(response, new LinkedHashMapEx("deleted", true));
  }

  protected void writeError(final HttpServletResponse response, final String message)
    throws IOException {
    writeJsonMap(response, new LinkedHashMapEx("rest_api_error", message));
  }

  protected void writeJsonListOfMaps(final HttpServletResponse response,
    final MapEx writeProperties, final List<? extends MapEx> maps) throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    final MapWriterFactory writerFactory = IoFactory.factoryByFileExtension(MapWriterFactory.class,
      "json");
    final OutputStream body = response.getOutputStream();
    try (
      final MapWriter writer = writerFactory.newMapWriter(body, StandardCharsets.UTF_8)) {
      writer.setProperties(writeProperties);
      for (final MapEx map : maps) {
        writer.write(map);
      }
    }
  }

  protected void writeJsonMap(final HttpServletResponse response, final MapEx record)
    throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    final MapWriterFactory writerFactory = IoFactory.factoryByFileExtension(MapWriterFactory.class,
      "json");
    final OutputStream body = response.getOutputStream();
    try (
      final MapWriter writer = writerFactory.newMapWriter(body, StandardCharsets.UTF_8)) {
      writer.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, true);
      writer.write(record);
    }
  }

  protected void writeJsonRecord(final HttpServletResponse response, final Record record)
    throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    final RecordWriterFactory writerFactory = IoFactory
      .factoryByFileExtension(RecordWriterFactory.class, "json");
    final OutputStream body = response.getOutputStream();
    try (
      final RecordWriter writer = writerFactory.newRecordWriter("record.json", record, body,
        StandardCharsets.UTF_8)) {
      writer.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, true);
      writer.write(record);
    }
  }

  protected void writeJsonRecords(final HttpServletResponse response,
    final JsonObject writerProperties, final RecordDefinitionProxy recordDefinition,
    final Iterable<Record> reader) throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    final RecordWriterFactory writerFactory = IoFactory
      .factoryByFileExtension(RecordWriterFactory.class, "json");
    final OutputStream body = response.getOutputStream();
    try (
      final RecordWriter writer = writerFactory.newRecordWriter("records.json", recordDefinition,
        body, StandardCharsets.UTF_8)) {
      writer.setProperties(writerProperties);
      writer.writeAll(reader);
    }
  }

  protected void writeJsonRecords(final HttpServletResponse response, final Query query)
    throws IOException {
    try (
      RecordReader reader = this.recordStore.getRecords(query)) {
      writeJsonRecords(response, reader);
    }
  }

  protected void writeJsonRecords(final HttpServletResponse response, final RecordReader reader)
    throws IOException {
    writeJsonRecords(response, JsonObject.EMPTY, reader, reader);
  }

  protected void writeJsonRecordsPage(final HttpServletRequest request,
    final HttpServletResponse response, final Query query) throws IOException {
    final String sort = request.getParameter("sort");
    final RecordDefinition recordDefinition = query.getRecordDefinition();
    if (sort != null && recordDefinition != null && recordDefinition.hasField(sort)) {
      final boolean ascending = "asc".equals(request.getParameter("order"));
      query.addOrderBy(sort, ascending);
    }

    final int recordCount = this.recordStore.getRecordCount(query);
    final JsonObject parameters = JsonObject.hash("extraProperties",
      JsonObject.hash("resultCount", recordCount));
    final int pageSize = Integer.parseInt(getParameter(request, "pageSize", "10"));
    final int pageNumber = Integer.parseInt(getParameter(request, "page", "0"));
    query.setLimit(pageSize);
    query.setOffset(pageNumber * pageSize);
    try (
      RecordReader reader = this.recordStore.getRecords(query)) {
      writeJsonRecords(response, parameters, reader, reader);
    }
  }
}
