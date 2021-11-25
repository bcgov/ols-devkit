package ca.bc.gov.geomark.web.servlet.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.logging.Logs;
import org.jeometry.coordinatesystem.model.CoordinateSystem;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.NamedLinkedHashMapEx;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.io.GeometryReaderFactory;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineCap;
import com.revolsys.geometry.model.LineJoin;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.geometry.operation.buffer.BufferParameters;
import com.revolsys.geometry.operation.valid.GeometryValidationError;
import com.revolsys.geometry.operation.valid.IsValidOp;
import com.revolsys.geometry.precision.MinimumClearance;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactory;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.kml.Kml;
import com.revolsys.record.io.format.kml.Kml22Constants;
import com.revolsys.record.io.format.kml.KmlRecordWriter;
import com.revolsys.record.io.format.kml.KmzRecordWriter;
import com.revolsys.record.query.And;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.Value;
import com.revolsys.record.query.functions.GeometryEqual2d;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;
import com.revolsys.util.UrlUtil;

import ca.bc.gov.geomark.web.domain.GeomarkConfig;
import ca.bc.gov.geomark.web.domain.GeomarkConstants;
import ca.bc.gov.geomark.web.domain.GeomarkRecordDefinitions;
import ca.bc.gov.geomark.web.servlet.BaseServlet;

@WebServlet(urlPatterns = "/api/geomarks/*", loadOnStartup = 1)
@MultipartConfig()
public class GeomarkServlet extends BaseServlet {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final Function<Geometry, Geometry> toPoint = this::toPoint;

  private final Function<Geometry, Geometry> toFeature = this::toFeature;

  private final Function<Geometry, Geometry> toBBox = this::toBoundingBox;

  /**
   * Add an alternate link.
   *
   * @param resourceLinks The list of alternate links.
   * @param format The file format extension.
   * @param mediaType The media type.
   * @param title The title of the link.
   * @param url The base url.
   * @param resourceName The resource to link to.
   * @param coordinateSystem The coordinate system.
   */
  private void addDownloadGeomarkResourceLink(final ArrayList<Map<String, Object>> resourceLinks,
    final String format, final String mediaType, final String title, final String url,
    final String resourceName, final CoordinateSystem coordinateSystem) {
    final String resourceUrl = url + "/" + resourceName + "." + format;
    addResourceLink(resourceLinks, title, resourceUrl, resourceName, format, mediaType,
      coordinateSystem);
  }

  /**
   * Add an alternate link.
   *
   * @param resourceLinks The list of alternate links.
   * @param title The title of the link.
   * @param resourceUrl The resource url.
   * @param resourceName The resource to link to.
   * @param mediaType The media type.
   * @param mediaType2
   * @param coordinateSystem The coordinate system.
   */
  private void addResourceLink(final ArrayList<Map<String, Object>> resourceLinks, String title,
    final Object resourceUrl, final String resourceName, final String format,
    final Object mediaType, final CoordinateSystem coordinateSystem) {
    String url = resourceUrl.toString();
    title = CaseConverter.toSentence(resourceName) + " " + title;
    int csId = 0;
    if (coordinateSystem != null) {
      csId = coordinateSystem.getCoordinateSystemId();
      url += "?srid=" + csId;
      title += " in coordinate system " + coordinateSystem.getCoordinateSystemName() + " (" + csId
        + ")";

    }
    final MapEx resourceLink = new NamedLinkedHashMapEx("resourceLink");
    resourceLink.put("title", title);
    resourceLink.put("href", url);
    resourceLink.put("resource", resourceName);
    resourceLink.put("format", format);
    resourceLink.put("mediaType", mediaType);
    if (csId > 0) {
      resourceLink.put("srid", csId);
    }
    resourceLinks.add(resourceLink);
  }

  /**
   * <p>
   * Construct a new new geomark for the specified geometry.
   * </p>
   * <p>
   * The geometry will be checked to ensure that it intersects the Province of
   * British Columbia and is valid for the current coordinate system.
   * </p>
   * @throws IOException
   * @throws ServletException
   */
  private void createGeomark(final HttpServletRequest request, final HttpServletResponse response,
    final List<Geometry> geometries, final String geometryFieldName, final String geometryType,
    final JsonObject errors) throws IOException, ServletException {
    final List<Geometry> bufferedGeometries = getBufferedGeometries(request, geometries,
      geometryFieldName, errors);
    final boolean hasBuffer = bufferedGeometries != geometries;
    final String resultFormat = getResultFormat(request, null);
    final boolean allowOverlap = getBooleanParameter(request, "allowOverlap");
    final String successRedirectUrl = request.getParameter("successRedirectUrl");
    Geometry geometry = null;
    try {
      geometry = this.GEOMARK_CONFIG.getGeometryFactory().geometry(geometries);
    } catch (final TopologyException e) {
      Logs.error(this, "Invalid Topology\n" + Strings.toString("\n", geometries), e);
      final String message = "Multi-" + geometryType + " has overlap which is not supported. "
        + e.getMessage();
      errors.add(geometryFieldName, message);
    }

    if (geometryType != null && geometryType.equals("Any")
      && geometry.getDataType().equals(GeometryDataTypes.GEOMETRY_COLLECTION)) {
      if (!hasBuffer) {
        errors.add(geometryFieldName, "A buffer must be specified for geometryType=Any");
      }

    }
    if (geometry.isEmpty()) {
      if (errors.isEmpty()) {
        if (geometryType.equals("Polygon")) {
          final String message = "Couldn't create geomark. Possible reasons:\n"
            + "1. No Polygon, Multi-Polygon, or folder(set) of Polygons provided.\n"
            + "2. A point or line was provided without a buffer width.";
          errors.add(geometryFieldName, message);
        } else {
          final String message = "Couldn't create geomark. No " + geometryType + ", Multi-"
            + geometryType + ", or folder(set) of " + geometryType + "s provided.";
          errors.add(geometryFieldName, message);
        }
      }
    } else {
      if (!allowOverlap && hasOverlap(geometry)) {
        if (hasBuffer) {
          final String message = "Buffered multi-geometry has overlap which is not supported";
          errors.add(geometryFieldName, message);
        } else {
          final String message = "Multi-geometry has overlap which is not supported";
          errors.add(geometryFieldName, message);
        }
      } else if (geometry.getVertexCount() > this.GEOMARK_CONFIG.getInt("maxVertices")) {
        final String message = "Geomark " + geometryType + " must not have > "
          + this.GEOMARK_CONFIG.getInt("maxVertices") + " vertices";
        errors.add(geometryFieldName, message);
      } else if (!intersectsGeomarkValidArea(geometry)) {
        final String message = "Geomark must intersect the Province of British Columbia";
        errors.add(geometryFieldName, message);
      } else if (errors.isEmpty()) {
        Record geomark;
        String geomarkId;
        final RecordStore recordStore = this.GEOMARK_CONFIG.getRecordStore();
        try (
          Transaction transaction = recordStore.newTransaction()) {
          geomark = getGeomarkRecentWithGeometry(geometry);
          if (geomark == null) {
            geomarkId = "gm-" + newUuid();
            final RecordDefinition recordDefinition = recordStore
              .getRecordDefinition(GeomarkConstants.GEOMARK_POLY);
            geomark = new ArrayRecord(recordDefinition);
            geomark.setIdentifier(Identifier.newIdentifier(geomarkId));
            final Timestamp date = new Timestamp(System.currentTimeMillis());
            final Date expiryDate = getExpiryDate(date);
            geomark.setValue(GeomarkConstants.EXPIRY_DATE, expiryDate);
            geomark.setValue(GeomarkConstants.MIN_EXPIRY_DATE, expiryDate);
            geomark.setValue(GeomarkConstants.WHEN_CREATED, date);
            geomark.setGeometryValue(geometry);
            try (
              final RecordWriter writer = recordStore.newRecordWriter()) {
              writer.write(geomark);
            }
          } else {
            geomarkId = geomark.getString(GeomarkConstants.GEOMARK_ID);
          }
        }
        if (isGoogleEarth(request)) {
          final Map<String, Object> successParameters = new LinkedHashMap<>();
          successParameters.put("geomarkId", geomarkId);
          successParameters.put("geomarkUrl", request.getAttribute("serverUrl")
            + request.getContextPath() + "/geomarks/" + geomarkId);
          forward(request, response, "/geomarks/created.kml", successParameters);
        } else {
          final String geomarkUrl = getAbsoluteUrl(request, "/geomarks/" + geomarkId);
          if (successRedirectUrl != null && successRedirectUrl.trim().length() > 0) {
            final MapEx successParameters = new LinkedHashMapEx();
            successParameters.put("geomarkId", geomarkId);
            successParameters.put("geomarkUrl", geomarkUrl);
            final String location = UrlUtil.getUrl(successRedirectUrl, successParameters);
            response.sendRedirect(location);
          } else if (resultFormat == null || resultFormat.equals("html")) {
            response.sendRedirect(geomarkUrl);
          } else {
            final MapEx geomarkInfo = getGeomarkInfo(request, geomarkId, geomark);
            writeMap(request, response, geomarkInfo, resultFormat);
          }
        }
      }
    }
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {

    final String path = request.getPathInfo();
    if (path == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } else if (path.startsWith("/new-ajax")) {
      doPostCreateGeomark(request, response, path);
    } else if (path.equals("/copy-ajax")) {
      doPostCreateGeomarkCopy(request, response);
    } else if (!path.startsWith("/gm-")) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } else {
      final int slashIndex = path.indexOf('/', 1);
      if (slashIndex == -1) {
        final String geomarkRef = path.substring(1);
        doGetGeomarkInfo(request, response, geomarkRef);
      } else {
        final String typeRef = path.substring(slashIndex + 1);
        final String geomarkId = path.substring(1, slashIndex);
        final Record geomark = getGeomark(geomarkId);
        if (geomark == null) {
          writeGeomarkNotFound(response);
        } else {
          String type;
          String fileExtension = null;
          final int dotIndex = typeRef.indexOf('.');
          if (dotIndex == -1) {
            type = typeRef;
          } else {
            fileExtension = typeRef.substring(dotIndex + 1);
            type = typeRef.substring(0, dotIndex);
          }
          if ("point".equals(type)) {
            doGetGeomarkRecord(request, response, geomark, fileExtension,
              GeomarkRecordDefinitions.POINT, this.toPoint);
          } else if ("boundingBox".equals(type)) {
            doGetGeomarkRecord(request, response, geomark, fileExtension,
              GeomarkRecordDefinitions.BOUNDING_BOX, this.toBBox);
          } else if ("feature".equals(type)) {
            doGetGeomarkRecord(request, response, geomark, fileExtension,
              GeomarkRecordDefinitions.GEOMARK, this.toFeature);
          } else if ("parts".equals(type)) {
            doGetGeomarkParts(request, response, geomark, fileExtension);
          } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
          }
        }
      }
    }
  }

  private void doGetGeomarkInfo(final HttpServletRequest request,
    final HttpServletResponse response, final String geomarkRef)
    throws IOException, ServletException {
    String geomarkId;
    String fileExtension = null;
    final int dotIndex = geomarkRef.indexOf('.');
    if (dotIndex == -1) {
      geomarkId = geomarkRef;
    } else {
      fileExtension = geomarkRef.substring(dotIndex + 1);
      geomarkId = geomarkRef.substring(0, dotIndex);
    }

    if (fileExtension == null) {
      forward(request, response, "/index");
    } else {
      final Record geomark = getGeomark(geomarkId);
      if (geomark == null) {
        writeGeomarkNotFound(response);
      } else {
        final MapEx geomarkInfo = getGeomarkInfo(request, geomarkId, geomark);
        writeMap(request, response, geomarkInfo, fileExtension);
      }
    }
  }

  private void doGetGeomarkParts(final HttpServletRequest request,
    final HttpServletResponse response, final Record geomark, final String fileExtension)
    throws IOException {
    try (
      RecordWriter writer = newRecordWriter(request, response, fileExtension,
        GeomarkRecordDefinitions.PART, geomark)) {
      if (writer != null) {
        final RecordDefinition recordDefinition = writer.getRecordDefinition();
        final Geometry geometry = geomark.getGeometry();
        final Geometry projectedGeometry = getProjectedGeometry(geometry, writer, false);

        final int geometryCount = projectedGeometry.getGeometryCount();

        if (geometryCount == 1) {
          request.setAttribute(IoConstants.SINGLE_OBJECT_PROPERTY, true);
        }
        for (int i = 0; i < geometryCount; i++) {
          final Geometry geometryPart = projectedGeometry.getGeometry(i);
          writeGeomarkRecord(request, writer, recordDefinition, geomark, geometryPart, i);
        }
      }
    }
  }

  private void doGetGeomarkRecord(final HttpServletRequest request,
    final HttpServletResponse response, final Record geomark, final String fileExtension,
    final GeomarkRecordDefinitions recordDefinitions,
    final Function<Geometry, Geometry> convertGeometry) throws IOException {
    try (
      RecordWriter writer = newRecordWriter(request, response, fileExtension, recordDefinitions,
        geomark)) {
      if (writer != null) {
        final RecordDefinition recordDefinition = writer.getRecordDefinition();
        final Geometry geometry = geomark.getGeometry();
        final Geometry projectedGeometry = getProjectedGeometry(geometry, writer, false);
        final Geometry geometryPart = convertGeometry.apply(projectedGeometry);

        writeGeomarkRecord(request, writer, recordDefinition, geomark, geometryPart, null);
      }
    }
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final String path = request.getPathInfo();
    if (path.startsWith("/new")) {
      doPostCreateGeomark(request, response, path);
    } else if (path.startsWith("/copy")) {
      doPostCreateGeomarkCopy(request, response);
    } else {
      doGet(request, response);
    }
  }

  /**
    * Construct a new new geomark from the geometries read from the 'body' parameter or
   * file. If the multiple parameter is specified then all geometries will be
   * read, otherwise only the first geometry will be used.
   *
   * @param request The HTTP request.
   * @param response The HTTP response
   * @param path
   * @throws ServletException
   * @throws IOException
    */
  private void doPostCreateGeomark(final HttpServletRequest request,
    final HttpServletResponse response, final String path) throws IOException, ServletException {
    final JsonObject errors = JsonObject.hash();
    final String format = request.getParameter("format");
    int srid = 0;
    final String sridString = request.getParameter("srid");
    if (sridString != null) {
      srid = Integer.parseInt(sridString);
    }

    List<Geometry> geometries = null;
    final String geometryType = request.getParameter("geometryType");
    final GeometryReaderFactory factory = IoFactory
      .factoryByFileExtension(GeometryReaderFactory.class, format);
    if (factory == null) {
      errors.add("format", "File format=" + format + " not supported");
    } else {
      try (
        InputStream in = getInputStream(request);
        final GeometryReader geometryReader = factory.newGeometryReader(in);) {
        geometryReader.setProperty("geometryFactory", GeometryFactory.floating2d(srid));

        final boolean multiple = getBooleanParameter(request, "multiple");

        try {
          if (geometryType == null || geometryType.equals("Any")) {
            geometries = getGeometries(errors, geometryReader);
          } else if (geometryType.equals("Polygon")) {
            geometries = getGeometries(errors, geometryReader, Polygon.class, multiple);
          } else if (geometryType.equals("LineString") || geometryType.equals("Line")) {
            geometries = getGeometries(errors, geometryReader, LineString.class, multiple);
          } else if (geometryType.equals("Point")) {
            geometries = getGeometries(errors, geometryReader, Point.class, multiple);
          } else {
            errors.add("geometryType", "Unknown geometryType=" + geometryType);
          }
        } catch (final Exception e) {
          final String message = "Unable to read geometry. Check the file is a valid format="
            + factory.getName() + " file.";
          Logs.error(this, message, e);
          errors.add("body", message);
        }
        if (errors.isEmpty() && geometries == null) {
          errors.add("body", "File contains no geometryies where geometryType=" + geometryType);
        }
      }
      if (errors.isEmpty()) {
        createGeomark(request, response, geometries, "body", geometryType, errors);
      }
    }
    writeErrors(request, response, errors);

  }

  /**
   * Construct a new new geomark by copying the geometries from one or more existing
   * geomarks from the current server.
   *
   * @param request The HTTP request.
   * @param response The HTTP response
   * @param path
   * @throws ServletException
   * @throws IOException
   */
  private void doPostCreateGeomarkCopy(final HttpServletRequest request,
    final HttpServletResponse response) throws IOException, ServletException {
    final JsonObject errors = JsonObject.hash();
    final String geometryFieldName = "geomarkUrls";
    final String[] geomarkUrls = request.getParameterValues("geomarkUrl");

    final Pattern idPattern = Pattern
      .compile("(?:http(s)?://" + request.getServerName() + "/pub/geomark/geomarks/)?(gm-[^/]+)/?");
    final List<Geometry> geometries = new ArrayList<>();
    for (String geomarkUrl : geomarkUrls) {
      geomarkUrl = geomarkUrl.trim();
      if (Property.hasValue(geomarkUrl)) {
        final Matcher matcher = idPattern.matcher(geomarkUrl);
        if (matcher.matches()) {
          final String geomarkId = matcher.group(2);
          final Record geomark = this.recordStore.getRecord(GeomarkConstants.GEOMARK_POLY,
            geomarkId);
          if (geomark == null) {
            final String message = "Geomark not found: " + geomarkUrl;
            errors.add(geometryFieldName, message);
          } else {
            final Geometry geometry = geomark.getGeometry();
            for (int i = 0; i < geometry.getGeometryCount(); i++) {
              final Geometry part = geometry.getGeometry(i);
              geometries.add(part);
            }
          }
        } else {
          final String message = "Only geomarks from the current server are supported: "
            + geomarkUrl;
          errors.add(geometryFieldName, message);
        }
      }
    }
    if (errors.isEmpty()) {
      createGeomark(request, response, geometries, geometryFieldName, "Any", errors);
    }
    writeErrors(request, response, errors);
  }

  /**
   * Get new geometries with the buffer applied.
   *
   * @param request The http request.
   * @param geometries The geometries to buffer.
   * @param geometryFieldName The name of the geometry field.
   * @param errors Any errors in the buffer parameters
   * @return The buffered geometries.
   */
  private List<Geometry> getBufferedGeometries(final HttpServletRequest request,
    final List<Geometry> geometries, final String geometryFieldName, final JsonObject errors) {
    String bufferMetres = request.getParameter("bufferMetres");
    double buffer = 0;
    if (Property.hasValue(bufferMetres)) {
      bufferMetres = bufferMetres.replaceAll("[^0-9\\.]", "");
      try {
        buffer = Double.valueOf(bufferMetres);
      } catch (final Throwable t) {
        errors.add("bufferMetres", "The parameter bufferMetres must be a valid number");
        return null;
      }
    }
    if (buffer > 0.0) {
      double mitreLimit = 5;
      final String bufferJoin = getParameter(request, "bufferJoin", "ROUND");
      final String bufferCap = getParameter(request, "bufferCap", "ROUND");

      String bufferMitreLimit = getParameter(request, "bufferMitreLimit", "5");
      String bufferSegments = getParameter(request, "bufferSegments", "5");
      if (Property.hasValue(bufferMitreLimit)) {
        bufferMitreLimit = bufferMitreLimit.replaceAll("[^0-9\\.]", "");
        try {
          mitreLimit = Double.valueOf(bufferMitreLimit);
          if (mitreLimit <= 0) {
            errors.add("bufferMitreLimit", "The parameter bufferMitreLimit must be null or > 0");
            return null;
          }
        } catch (final Throwable t) {
          errors.add("bufferMitreLimit", "The parameter bufferMitreLimit must be a valid number");
          return null;
        }
      }
      int segments = 8;
      if (Property.hasValue(bufferSegments)) {
        bufferSegments = bufferSegments.replaceAll("[^0-9\\.]", "");
        try {
          segments = Integer.valueOf(bufferSegments);
          if (segments <= 0) {
            errors.add("bufferSegments", "The parameter bufferSegments must be null or > 0");
            return null;
          }
        } catch (final Throwable t) {
          errors.add("bufferSegments", "The parameter bufferSegments must be null or > 0");
          return null;
        }
      }

      try {
        final List<Geometry> bufferedGeometries = new ArrayList<>();
        for (final Geometry geometry : geometries) {
          final Polygon polygon = getBufferedGeometry(geometry, buffer, bufferJoin, bufferCap,
            mitreLimit, segments);
          if (polygon != null && !polygon.isEmpty()) {
            bufferedGeometries.add(polygon);
          }
        }
        return bufferedGeometries;
      } catch (final Exception e) {
        errors.add(geometryFieldName, "Error buffering geometry");
        return null;
      }
    } else {
      return geometries;
    }

  }

  /**
   * Get a new geometry with the buffer applied.
   *
   * @param geometry The geometry to buffer.
   * @param buffer The number of metres to buffer the geometry.
   * @param bufferJoin The buffer join style used to buffer the geometry.
   * @param bufferCap The buffer cap style used to buffer the geometry.
   * @param bufferMitreLimit The mitre limit used to buffer the geometry.
   * @param bufferSegments The number segments used to buffer the geometry.
   * @return The buffered geometries.
   */
  private Polygon getBufferedGeometry(final Geometry geometry, final double buffer,
    final String bufferJoin, final String bufferCap, final double bufferMitreLimit,
    final int bufferSegments) {
    final Geometry albersGeometry = this.GEOMARK_CONFIG.getGeometryFactory().geometry(geometry);
    final BufferParameters params = new BufferParameters();
    if (Property.hasValue(bufferCap)) {
      if (bufferCap.equalsIgnoreCase("FLAT")) {
        if (geometry instanceof Point || geometry.getLength() == 0) {
          params.setEndCapStyle(LineCap.SQUARE);
        } else {
          params.setEndCapStyle(LineCap.BUTT);
        }
      } else if (bufferCap.equalsIgnoreCase("SQUARE")) {
        params.setEndCapStyle(LineCap.SQUARE);
      } else {
        params.setEndCapStyle(LineCap.ROUND);
      }
    }
    if (Property.hasValue(bufferJoin)) {
      if (bufferJoin.equalsIgnoreCase("MITRE")) {
        params.setJoinStyle(LineJoin.MITER);
      } else if (bufferJoin.equalsIgnoreCase("BEVEL")) {
        params.setJoinStyle(LineJoin.BEVEL);
      } else {
        params.setJoinStyle(LineJoin.ROUND);
      }
    }
    params.setMitreLimit(bufferMitreLimit);
    params.setQuadrantSegments(bufferSegments);
    return albersGeometry.buffer(buffer, params);
  }

  private int getCoordinateSystemId(final HttpServletRequest request) {
    int srid = 4326;
    try {
      final String sridString = request.getParameter("srid");
      if (sridString != null) {
        srid = Integer.parseInt(sridString);
      }
    } catch (final Exception e) {
    }
    return srid;
  }

  /**
   * Get the Geomark expiry date for a given date
   *
   * @param date The date.
   * @return The expiry date.
   */
  private Date getExpiryDate(final java.util.Date date) {
    final Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    calendar.add(Calendar.DAY_OF_MONTH, +this.GEOMARK_CONFIG.getInt("maxGeomarkAgeDays"));
    final Date expiryDate = new Date(calendar.getTimeInMillis());
    return expiryDate;
  }

  /**
   * Get the Geomark record from the database, if it has not expired.
   *
   * @param geomarkId The unique identifier for the geomark (e.g. gm-abcdefghijklmnopqrstuv0bcislands).
   * @return The Geomark record.
   */
  private Record getGeomark(final String geomarkId) {
    final RecordStore recordStore = this.GEOMARK_CONFIG.getRecordStore();
    final Record geomark = recordStore.getRecord(GeomarkConstants.GEOMARK_POLY, geomarkId);
    final boolean expired = GeomarkConstants.isExpired(geomark);
    if (expired) {
      return null;
    } else {
      return geomark;
    }
  }

  private MapEx getGeomarkInfo(final HttpServletRequest request, final String geomarkId,
    final Record geomark) {
    Geometry geometry = geomark.getGeometry();
    final int srid = GeomarkConstants.EPSG_4326;
    final RecordDefinition recordDefinition = GeomarkRecordDefinitions.GEOMARK
      .getRecordDefinition(srid);
    geometry = recordDefinition.convertGeometry(geometry);
    final String geomarkUrl = getGeomarkUrl(request, geomarkId);
    final Record geomarkPart = getGeomarkPart(geomark, geometry, recordDefinition, geomarkUrl,
      null);
    final MapEx geomarkMap = new NamedLinkedHashMapEx("GeomarkInfo");
    for (final String name : recordDefinition.getFieldNames()) {
      if (!name.equals("geometry")) {
        final Object value = geomarkPart.get(name);
        geomarkMap.put(name, value);
      }
    }
    geomarkMap.put("googleMapsUrl", "https://maps.google.ca/?q=" + geomarkUrl + "/parts.kml");
    geomarkMap.put("googleEarthUrl", geomarkUrl + "/parts.kml");
    geomarkMap.put("resourceLinks", getResourceLinks(geomarkId, geomarkUrl));
    return geomarkMap;
  }

  /**
   * Get the geomark part object for the geomark, setting the specified
   * geometry.
   *
   * @param geomark The geomark.
   * @param geometry The geometry.
   * @param recordDefinition The record definition to create the info record.
   * @param url
   * @param partIndex The index of the part or null if not split into parts.
   * @param srid The srid of the coordinate system the geometry should be converted to.
   * @return The geomark info object.
   */
  private Record getGeomarkPart(final Record geomark, final Geometry geometry,
    final RecordDefinition recordDefinition, final String url, final Integer partIndex) {
    final Identifier geomarkId = geomark.getIdentifier();

    final Record geomarkPart = new ArrayRecord(recordDefinition);
    if (partIndex == null) {
      geomarkPart.setValue("id", geomarkId);

    } else {
      geomarkPart.setValue("id", "#" + partIndex + " of " + geomarkId);
    }
    geomarkPart.setValue("url", url);

    final java.util.Date date = geomark.getValue("WHEN_CREATED");
    geomarkPart.setValue("createDate", new Date(date.getTime()));
    if (!hasSubscription(geomarkId)) {
      final java.util.Date expiryDate = geomark.getValue("EXPIRY_DATE");
      geomarkPart.setValue("expiryDate", new Date(expiryDate.getTime()));
    }

    geomarkPart.setGeometryValue(geometry);

    final BoundingBox envelope = geometry.getBoundingBox();
    geomarkPart.setValue("minX", envelope.getMinX());
    geomarkPart.setValue("minY", envelope.getMinY());
    geomarkPart.setValue("maxX", envelope.getMaxX());
    geomarkPart.setValue("maxY", envelope.getMaxY());

    final Point centroid = geometry.getCentroid();
    geomarkPart.setValue("centroidX", centroid.getX());
    geomarkPart.setValue("centroidY", centroid.getY());

    final int numGeometries = geometry.getGeometryCount();
    String geometryType = geometry.getGeometryType();
    if (numGeometries == 1) {
      geometryType = geometryType.replaceAll("Multi", "");
    }
    geomarkPart.setValue("geometryType", geometryType);
    geomarkPart.setValue("partIndex", partIndex);
    geomarkPart.setValue("numPolygons", numGeometries);
    geomarkPart.setValue("numParts", numGeometries);
    final int numPoints = geometry.getVertexCount();
    geomarkPart.setValue("numVertices", numPoints);
    final double length = geometry.getLength();
    geomarkPart.setValue("length", length);
    final double area = geometry.getArea();
    geomarkPart.setValue("area", (long)area);

    Geometry validationGeometry;
    if (geometry.getGeometryFactory().isGeographic()) {
      validationGeometry = geometry;
    } else {
      validationGeometry = geometry;
    }

    final IsValidOp validOp = new IsValidOp(validationGeometry);
    final boolean valid = validOp.isValid();
    geomarkPart.setValue("isValid", valid);
    if (!valid) {
      final GeometryValidationError validationError = validOp.getValidationError();
      final String message = validationError.getMessage();
      geomarkPart.setValue("validationError", message);
    }
    geomarkPart.setValue("isSimple", validationGeometry.isSimple());

    double minimumClearance = MinimumClearance.getDistance(validationGeometry);
    if (minimumClearance >= 0.001) {
      minimumClearance = Math.ceil(minimumClearance * 1000.0) / 1000.0;
    }
    geomarkPart.setValue("minimumClearance", minimumClearance);

    boolean isRobust = true;
    if (minimumClearance < 1 / validationGeometry.getGeometryFactory().getScaleXY()) {
      isRobust = false;
    }
    geomarkPart.setValue("isRobust", isRobust);

    return geomarkPart;
  }

  /**
   * Get the ID of a recent (same day) geomark that has the same geometry.
   *
   * @param geometry The geometry.
   * @return The geomark, or null if one was not found.
   */
  private Record getGeomarkRecentWithGeometry(final Geometry geometry) {
    final Query query = new Query(GeomarkConstants.GEOMARK_POLY);
    query.addOrderBy("WHEN_CREATED", false);

    final Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("PST"));
    calendar.set(Calendar.HOUR, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    final Date date = new Date(calendar.getTimeInMillis());
    final RecordDefinitionImpl recordDefinition = this.GEOMARK_CONFIG.getRecordDefinition();
    final FieldDefinition whenCreatedField = recordDefinition.getField("WHEN_CREATED");

    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    final GeometryEqual2d geometryEqual = new GeometryEqual2d(new Column(geometryField),
      Value.newValue(geometryField, geometry));

    query.setWhereCondition(new And(Q.greaterThanEqual(whenCreatedField, date), geometryEqual));

    try (
      RecordReader reader = this.GEOMARK_CONFIG.getRecordStore().getRecords(query)) {
      for (final Record geomark : reader) {
        return geomark;
      }
    }
    return null;
  }

  private String getGeomarkUrl(final HttpServletRequest request, final String geomarkId) {
    return getAbsoluteUrl(request, "/api/geomarks/" + geomarkId);
  }

  /**
   * Read all the geometries from the reader converting them to the correct
   * SRID.
   *
   * @param geometryReader The geometry reader.
   * @return The geometry or geometry collection.
   */
  private List<Geometry> getGeometries(final JsonObject errors,
    final GeometryReader geometryReader) {
    final List<Geometry> geometries = new ArrayList<>();
    for (final Geometry geometry : geometryReader) {
      for (int i = 0; i < geometry.getGeometryCount(); i++) {
        final Geometry part = geometry.getGeometry(i);
        if (!part.isEmpty()) {
          geometries.add(getGeometry(errors, part));
        }
      }
    }
    return geometries;
  }

  /**
   * Read all the geometries from the reader. The result will include the
   * geometries of the specified type.
   * @param errors
   *
   * @param geometryReader The geometry reader.
   * @param geometryClass The geometry class to return geometries for.
   * @param multiple True if multiple geometries should be returned, false for a
   *          single geometry.
   * @return The geometry or geometry collection.
   */
  private List<Geometry> getGeometries(final JsonObject errors, final GeometryReader geometryReader,
    final Class<? extends Geometry> geometryClass, final boolean multiple) {
    final List<Geometry> geometries = new ArrayList<>();
    for (final Geometry geometry : geometryReader) {
      for (int i = 0; i < geometry.getGeometryCount(); i++) {
        final Geometry part = geometry.getGeometry(i);
        if (!part.isEmpty()) {
          if (geometryClass.isAssignableFrom(part.getClass())) {
            geometries.add(getGeometry(errors, part));
            if (!multiple) {
              return geometries;
            }
          }
        }
      }
    }
    return geometries;
  }

  private Geometry getGeometry(final JsonObject errors, final Geometry part) {
    if (part == null) {
      return null;
    } else {
      final GeometryFactory sourceGeometryFactory = part.getGeometryFactory();
      final BoundingBox areaBoundingBox = sourceGeometryFactory.getAreaBoundingBox();
      if (areaBoundingBox.bboxCovers(part)) {
        return this.GEOMARK_CONFIG.getGeometryFactory().geometry(part);
      } else {
        errors.add("body",
          "Geometry is outside the valid area for coordinate system "
            + sourceGeometryFactory.getCoordinateSystemName() + " ("
            + +sourceGeometryFactory.getCoordinateSystemId()
            + "). Check the geometry and coordinate system.");
        return null;
      }
    }

  }

  private InputStream getInputStream(final HttpServletRequest request)
    throws IOException, ServletException {
    final String body = request.getParameter("body");
    if (body == null) {
      final Part part = request.getPart("body");
      if (part == null) {
        return null;
      } else {
        return part.getInputStream();
      }
    } else {
      return new ByteArrayInputStream(body.getBytes());
    }
  }

  /**
   * Get the geometry projected to the specified coordinate system.
   *
   * @param geometry The geometry.
   * @param srid The srid of the <a
   *          href="../coordinateSystems.html" >coordinate
   *          system</a> the geometry should be converted to.
   * @param useGeographicsPrecisionModel Use the precision model for geographics
   *          coordinates.
   * @return The projected geometry;
   */
  private Geometry getProjectedGeometry(final Geometry geometry, final RecordWriter writer,
    final boolean useGeographicsPrecisionModel) {
    if (geometry == null) {
      return null;
    } else {
      final GeometryFactory projectedPrecisionModel = GeomarkConfig.PROJECTED_GEOMETRY_FACTORY;
      GeometryFactory geometryFactory = writer.getGeometryFactory();
      if (geometryFactory == null) {
        geometryFactory = this.GEOMARK_CONFIG.getDefaultGeometryFactory();
      }
      final List<CoordinateSystem> coordinateSystems = this.GEOMARK_CONFIG.getCoordinateSystems();
      if (coordinateSystems.contains(geometryFactory.getHorizontalCoordinateSystem())) {
        final int csId = geometryFactory.getHorizontalCoordinateSystemId();
        final int axisCount = geometry.getGeometryFactory().getAxisCount();
        if (geometryFactory.isGeographic()) {
          if (useGeographicsPrecisionModel) {
            final GeometryFactory geographicsGeometryFactory = GeomarkConfig.GEOGRAPHICS_GEOMETRY_FACTORY;
            final double scaleX = geographicsGeometryFactory.getScaleX();
            final double scaleY = geographicsGeometryFactory.getScaleY();
            final double[] scales = projectedPrecisionModel.newScales(axisCount);
            scales[0] = scaleX;
            scales[1] = scaleY;
            geometryFactory = GeometryFactory.fixed(csId, axisCount, scales);
          } else {
            geometryFactory = GeometryFactory.floating(csId, axisCount);
          }
        } else {
          final double[] scales = projectedPrecisionModel.newScales(axisCount);
          geometryFactory = GeometryFactory.fixed(csId, axisCount, scales);
        }
        final Geometry projectedGeometry = geometry.convertGeometry(geometryFactory);
        return projectedGeometry;
      } else {
        throw new IllegalArgumentException(
          "Coordinate system with srid = " + writer.getCoordinateSystemId() + " is not supported");
      }
    }
  }

  /**
   * Get the HTML alternate links for the geomark.
   *
   * @param geomarkId The unique identifier for the geomark (e.g.
   *          gm-abcdefghijklmnopqrstuv0bcislands).
   * @param geomarkUrl The base URL to the geomark.
   * @return The list of alternate links.
   */
  private List<Map<String, Object>> getResourceLinks(final String geomarkId,
    final String geomarkUrl) {
    final ArrayList<Map<String, Object>> resourceLinks = new ArrayList<>();
    final String title = " representation of Geomark " + geomarkId;
    addResourceLink(resourceLinks, "HTML " + title, geomarkUrl, "info", "xhtml",
      "application/xhtml+xml", null);
    addResourceLink(resourceLinks, "XHTML " + title, geomarkUrl, "info", "html", "text/html", null);
    addResourceLink(resourceLinks, "XML " + title, geomarkUrl + ".xml", "info", "xml", "text/xml",
      null);
    addResourceLink(resourceLinks, "JSON " + title, geomarkUrl + ".json", "info", "json",
      "application/json", null);

    for (final String format : GeomarkConfig.GEOMARK_OUTPUT_FORMATS) {
      final GeometryReaderFactory reader = IoFactory
        .factoryByFileExtension(GeometryReaderFactory.class, format);
      final String mediaType = reader.getMediaType(format);
      final String formatLabel = this.GEOMARK_CONFIG.getFileFormatLabels().get(format);
      final String title2 = formatLabel + " representation of Geomark " + geomarkId;
      for (final CoordinateSystem coordinateSystem : this.GEOMARK_CONFIG.getCoordinateSystems()) {
        if (reader.isCoordinateSystemSupported(coordinateSystem)) {
          addDownloadGeomarkResourceLink(resourceLinks, format, mediaType, title2, geomarkUrl,
            "feature", coordinateSystem);
          addDownloadGeomarkResourceLink(resourceLinks, format, mediaType, title2, geomarkUrl,
            "parts", coordinateSystem);
          addDownloadGeomarkResourceLink(resourceLinks, format, mediaType, title2, geomarkUrl,
            "point", coordinateSystem);
          addDownloadGeomarkResourceLink(resourceLinks, format, mediaType, title2, geomarkUrl,
            "boundingBox", coordinateSystem);
        }
      }
    }
    return resourceLinks;
  }

  private String getResultFormat(final HttpServletRequest request, final String defaultValue) {
    final String format = request.getParameter("resultFormat");
    if (format == null) {
      final String pathInfo = request.getPathInfo();
      final int slashIndex = pathInfo.lastIndexOf('/');
      final int dotIndex = pathInfo.indexOf('.', slashIndex);
      if (dotIndex == -1) {
        return defaultValue;
      } else {
        return pathInfo.substring(dotIndex + 1);
      }
    }
    return format;
  }

  private boolean hasOverlap(final Geometry geometry) {
    final int geometryCount = geometry.getGeometryCount();
    if (geometryCount > 1) {
      for (int i = 0; i < geometryCount - 1; i++) {
        final Geometry geometry1 = geometry.getGeometry(i);
        for (int j = i + 1; j < geometryCount; j++) {
          final Geometry geometry2 = geometry.getGeometry(j);
          if (geometry1.intersects(geometry2)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Check to see if the Geomark has a subscription.
   *
   * @param geomarkId The unique identifier for the geomark (e.g.
   *          gm-abcdefghijklmnopqrstuv0bcislands).
   * @return True if the Geomark has a subscription false otherwise.
   */
  private boolean hasSubscription(final Identifier geomarkId) {
    final RecordStore recordStore = this.GEOMARK_CONFIG.getRecordStore();
    if (recordStore instanceof JdbcRecordStore) {
      final JdbcRecordStore jdbcRecordStore = (JdbcRecordStore)recordStore;
      try {
        jdbcRecordStore.selectString(
          "SELECT \"GEOMARK_GROUP_XREF_ID\" FROM \"GEOMARK\".\"GMK_GEOMARK_GROUP_XREF\" WHERE \"GEOMARK_ID\" = ?",
          geomarkId.toString());
        return true;
      } catch (final SQLException e) {
        Logs.error(this, "Unable to check Geomark group status " + geomarkId, e);
      } catch (final IllegalArgumentException e) {
      }
    }
    return false;
  }

  private boolean intersectsGeomarkValidArea(final Geometry geometry) {
    final Geometry area = this.GEOMARK_CONFIG.getArea();
    for (final Geometry part : geometry.geometries()) {
      try {
        if (area.intersects(part)) {
          return true;
        }
      } catch (final TopologyException e) {
        if (area.intersectsBbox(part.getBoundingBox())) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isGoogleEarth(final HttpServletRequest request) {
    boolean googleEarth = getBooleanParameter(request, "googleEarth");
    if ("created.kml".equals(request.getParameter("successRedirectUrl"))) {
      googleEarth = true;
    }
    return googleEarth;
  }

  private RecordWriter newRecordWriter(final HttpServletRequest request,
    final HttpServletResponse response, String fileExtension,
    final GeomarkRecordDefinitions recordDefinitions, final Record geomark) throws IOException {
    final int coordinateSystemId = getCoordinateSystemId(request);
    final RecordDefinition recordDefinition = recordDefinitions
      .getRecordDefinition(coordinateSystemId);
    final RecordWriterFactory writerFactory;
    if (fileExtension == null) {
      final String accept = request.getHeader("Accept");
      writerFactory = IoFactory.factoryByMediaType(RecordWriterFactory.class, accept);
      if (writerFactory != null) {
        fileExtension = writerFactory.getFileExtension(accept);
      }
    } else {
      writerFactory = IoFactory.factoryByFileExtension(RecordWriterFactory.class, fileExtension);
    }

    if (writerFactory == null) {
      writeError(response, "Download format not found");
      return null;
    } else {
      final String contentType = writerFactory.getMediaTypes().iterator().next();
      if (writerFactory.isBinary()) {
        response.setContentType(contentType);
      } else {
        response.setContentType(contentType + ";charset=UTF-8");
      }
      final String baseName = geomark.getString(GeomarkConstants.GEOMARK_ID);
      final String fileName = baseName + "." + fileExtension;
      response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

      final OutputStream body = response.getOutputStream();
      final RecordWriter writer = writerFactory.newRecordWriter(baseName, recordDefinition, body,
        StandardCharsets.UTF_8);
      String callback = request.getParameter("jsonp");
      if (callback == null) {
        callback = request.getParameter("callback");
      }
      if (callback != null) {
        writer.setProperty(IoConstants.JSONP_PROPERTY, callback);
      }

      writer.setProperty(Kml22Constants.SNIPPET_PROPERTY, "");
      final String kmlStyleUrl = this.GEOMARK_CONFIG.getString("kmlStyleUrl");
      writer.setProperty(Kml22Constants.STYLE_URL_PROPERTY, kmlStyleUrl);

      final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
      writer.setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
      return writer;
    }
  }

  private void setGeomarkRequestAttributes(final HttpServletRequest request,
    final RecordWriter writer, final Record geomark) {
    if (writer instanceof KmlRecordWriter || writer instanceof KmzRecordWriter) {
      final Identifier geomarkId = geomark.getIdentifier();
      final String documentName = "Geomark " + geomarkId;
      final String documentDescription = "<p>Click <a href=\"" + request.getAttribute("serverUrl")
        + "/pub/geomark/geomarks/gm-" + geomarkId + "\">here</a> for more information.</p>";

      final Geometry geometry = geomark.getGeometry();
      final BoundingBox boundingBox = geometry.getBoundingBox();
      final Point centre = boundingBox.getCentre();
      final long lookAtRange = Kml.getLookAtRange(boundingBox);
      final long kmlLookAtMinRange = this.GEOMARK_CONFIG.getLong("kmlLookAtMinRange");
      final long kmlLookAtMaxRange = this.GEOMARK_CONFIG.getLong("kmlLookAtMaxRange");

      writer.setProperty(Kml22Constants.LOOK_AT_POINT_PROPERTY, centre);
      writer.setProperty(Kml22Constants.LOOK_AT_RANGE_PROPERTY, lookAtRange);
      writer.setProperty(Kml22Constants.LOOK_AT_MIN_RANGE_PROPERTY, kmlLookAtMinRange);
      writer.setProperty(Kml22Constants.LOOK_AT_MAX_RANGE_PROPERTY, kmlLookAtMaxRange);
      writer.setProperty(Kml22Constants.DOCUMENT_DESCRIPTION_PROPERTY, documentDescription);
      writer.setProperty(Kml22Constants.DOCUMENT_NAME_PROPERTY, documentName);
    }
  }

  private Geometry toBoundingBox(final Geometry geometry) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return boundingBox.toPolygon(1, 1);
  }

  private Geometry toFeature(final Geometry geometry) {
    if (geometry.getGeometryCount() == 1) {
      return geometry.getGeometry(0);
    } else {
      return geometry;
    }
  }

  private Point toPoint(final Geometry geometry) {
    final Point point;
    final Geometry firstPart = geometry.getGeometry(0);
    if (firstPart instanceof Point) {
      point = (Point)firstPart;
    } else if (firstPart instanceof LineString) {
      final Point coordinates = firstPart.getPoint();
      point = this.GEOMARK_CONFIG.getGeometryFactory().point(coordinates);
    } else if (firstPart instanceof Polygon) {
      final Polygon polygon = (Polygon)firstPart;
      point = polygon.getPointWithin();
    } else {
      throw new IllegalArgumentException("Unknown geometry type " + firstPart.getGeometryType());
    }
    return point;
  }

  private void writeErrors(final HttpServletRequest request, final HttpServletResponse response,
    final JsonObject errors) throws ServletException, IOException {
    if (!errors.isEmpty()) {
      final String resultFormat = getResultFormat(request, "json");

      final String failureRedirectUrl = request.getParameter("failureRedirectUrl");

      final MapEx params = new LinkedHashMapEx(request.getParameterMap());
      final String formName = request.getParameter("formName");
      if (formName != null) {
        params.put("formName", formName);
      }
      params.remove("body");
      for (final String fieldName : errors.keySet()) {
        final String message = errors.getString(fieldName);
        params.put(fieldName + "_Error", message);
        params.add("error", message);
      }

      if (isGoogleEarth(request)) {
        forward(request, response, "/WEB-INF/jsp/geomarks/createKmlFailure.jsp", params);
      } else if (Property.hasValue(failureRedirectUrl)) {
        params.remove("body");
        final String url = UrlUtil.getUrl(failureRedirectUrl, params);
        try {
          response.sendRedirect(url);
        } catch (final IOException ioe) {
        }
      } else {
        writeMap(request, response, params, resultFormat);
      }
    }
  }

  private void writeGeomarkNotFound(final HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_NOT_FOUND,
      "The geomark could not be found. Check for extra characters on the end of the URL. The geomark may have expired and been deleted.");
  }

  private void writeGeomarkRecord(final HttpServletRequest request, final RecordWriter writer,
    final RecordDefinition recordDefinition, final Record geomark, final Geometry geometry,
    final Integer index) {
    final String geomarkId = geomark.getString(GeomarkConstants.GEOMARK_ID);
    final String url = getGeomarkUrl(request, geomarkId);
    final Record geomarkPart = getGeomarkPart(geomark, geometry, recordDefinition, url, index);
    setGeomarkRequestAttributes(request, writer, geomarkPart);
    writer.write(geomarkPart);
  }

  private void writeMap(final HttpServletRequest request, final HttpServletResponse response,
    final MapEx map, final String fileExtension) throws IOException {
    MapWriterFactory writerFactory;
    if (fileExtension == null) {
      writerFactory = IoFactory.factoryByMediaType(MapWriterFactory.class, fileExtension);
    } else {
      writerFactory = IoFactory.factoryByFileExtension(MapWriterFactory.class, fileExtension);
    }
    if (writerFactory == null) {
      writerFactory = IoFactory.factoryByFileExtension(MapWriterFactory.class, "json");
    }
    String contentType = writerFactory.getMediaTypes().iterator().next();
    if (!writerFactory.isBinary()) {
      contentType += ";charset=UTF-8";
    }
    response.setContentType(contentType);
    final OutputStream body = response.getOutputStream();
    try (
      final MapWriter writer = writerFactory.newMapWriter(body, StandardCharsets.UTF_8)) {
      writer.setProperty(IoConstants.INDENT, true);
      writer.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, true);
      String callback = request.getParameter("jsonp");
      if (callback == null) {
        callback = request.getParameter("callback");
      }
      if (callback != null) {
        writer.setProperty(IoConstants.JSONP_PROPERTY, callback);
      }
      final Object title = request.getAttribute(IoConstants.TITLE_PROPERTY);
      if (title != null) {
        writer.setProperty(IoConstants.TITLE_PROPERTY, title);
      }
      writer.write(map);
    }
  }

}
