package ca.bc.gov.geomark.client.api;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.bc.gov.geomark.client.util.JsonParser;
import ca.bc.gov.geomark.client.util.UrlUtil;

/**
 * <p>
 * The Geomark Java (11+) client allows applications to use the <a
 * href="../rest-api/">Geomark Web Service REST API</a> to create
 * geomarks, get geomark info, download geomarks and manage geomark groups.
 * </p>
 */
public class GeomarkClient {
  /** The format parameter name. */
  public static final String FORMAT = "format";

  /** The srid parameter name. */
  public static final String SRID = "srid";

  /** The geometryType parameter name. */
  public static final String GEOMETRY_TYPE = "geometryType";

  /** The multiple parameter name. */
  public static final String MULTIPLE = "multiple";

  /** The bufferMetres parameter name. */
  public static final String BUFFER_METRES = "bufferMetres";

  /** The bufferSegments parameter name. */
  public static final String BUFFER_SEGMENTS = "bufferSegments";

  /** The mitreLimit parameter name. */
  public static final String MITRE_LIMIT = "mitreLimit";

  /** The bufferCap parameter name. */
  public static final String BUFFER_CAP = "bufferCap";

  /** The bufferJoin parameter name. */
  public static final String BUFFER_JOIN = "bufferJoin";

  /** The base URL to the geomark server. */
  private String serverUrl;

  /**
   * <p>Construct a new Geomark client that is not connected to a specific
   * Geomark server. This client can only be used to get the geomark info, or
   * geometry using full geomark URL's. It cannot be used to add geomarks or
   * manage groups.</p>
   */
  public GeomarkClient() {
  }

  /**
   * <p>
   * Construct a new Geomark client that is connected to a specific Geomark web
   * service (e.g. https://apps.gov.bc.ca/pub/geomark). This client can also get
   * the geomark info, or download geomark geometry using full geomark URL's
   * from other servers.
   * </p>
   *
   * @param serverUrl The base URL to the geomark server.
   */
  public GeomarkClient(final String serverUrl) {
    this.serverUrl = UrlUtil.cleanPath(serverUrl.replaceAll("/+$", ""));
  }

  /**
   * <p>
   * Construct a new new geomark from an InputStream containing the geometry using the
   * <a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.createGeomark">
   * Create Geomark</a> REST API.
   * </p>
   * <p>
   * The extended parameters to the method are passed in using a Java (map). See
   * the <a
   * href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.createGeomarkCopy"
   * >Create Geomark Copy</a> REST API for the list of supported parameters.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * InputStream geometry = new FileInputStream(&quot;...&quot;);
   *
   * Map&lt;String, Object&gt; parameters = new HashMap&lt;String, Object&gt;();
   * parameters.put(GeomarkClient.SRID, 4326);
   * parameters.put(GeomarkClient.FORMAT, &quot;wkt&quot;);
   *
   * String geomarkUrl = client.addGeomark(geometry, parameters);
   * System.out.println(geomarkUrl);
   * </code></pre>
   *
   * @param geometry The text of geometry to create the geomark for.
   * @param parameters The parameters used to create the geomark.
   * @return The URL to the geomark info resource.
   */
  public String addGeomark(final InputStream geometry,
    final Map<String, ? extends Object> parameters) {
    final Map<String, Object> mergedParameters = new LinkedHashMap<String, Object>(parameters);
    mergedParameters.put("body", geometry);
    mergedParameters.put("resultFormat", "json");

    final Map<String, Object> response = UrlUtil
      .postMultipartJsonResponse(this.serverUrl + "/geomarks/new", mergedParameters);
    return getGeomarkUrl(response);
  }

  /**
   * <p>
   * Construct a new new geomark from a string containg a KML, GML, or WKT geometry
   * using the <a
   * href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.createGeomark">Create
   * Geomark</a> REST API.
   * </p>
   * <p>
   * The extended parameters to the method are passed in using a Java (map). See
   * the <a
   * href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.createGeomarkCopy"
   * >Create Geomark Copy</a> REST API for the list of supported parameters.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * String geometry = &quot;POLYGON((-112 47,-144 47,-144 60,-112 60,-112 47))&quot;;
   *
   * Map&lt;String, Object&gt; parameters = new HashMap&lt;String, Object&gt;();
   * parameters.put(GeomarkClient.SRID, 4326);
   * parameters.put(GeomarkClient.FORMAT, &quot;wkt&quot;);
   *
   * String geomarkUrl = client.addGeomark(geometry, parameters);
   * System.out.println(geomarkUrl);
   * </code></pre>
   *
   * @param geometry The text of geometry to create the geomark for.
   * @param parameters The parameters used to create the geomark.
   * @return The URL to the geomark info resource.
   */
  public String addGeomark(final String geometry, final Map<String, ? extends Object> parameters) {
    final Map<String, Object> mergedParameters = new LinkedHashMap<String, Object>(parameters);
    mergedParameters.put("body", geometry);
    mergedParameters.put("resultFormat", "json");

    final Map<String, Object> response = UrlUtil
      .postRequestJsonResponse(this.serverUrl + "/geomarks/new", mergedParameters);
    return getGeomarkUrl(response);
  }

  /**
   * <p>
   * Add the geomarks to the geomark group.
   * </p>
   * *
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   * String geomarkGroupId = &quot;...&quot;;
   * String geomarkGroupSecretKey = &quot;...&quot;;
   *
   * Map&lt;String, Object&gt; response = client.addGeomarksToGroup(geomarkGroupId,
   *   geomarkGroupSecretKey, &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;);
   * System.out.println(response);
   * </code></pre>
   *
   * <div class="title">Result Attributes</div>
   * <p>
   * The method returns a map with the following attributes.
   * </p>
   * <div class="simpleDataTable">
   * <table class="data">
   * <caption>Result Attributes</caption>
   * <thead>
   * <tr>
   * <th>Name</th>
   * <th>Type</th>
   * <th>Description</th>
   * </tr>
   * </thead> <tbody>
   * <tr>
   * <td class="name">status</td>
   * <td class="type"><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a></td>
   * <td class="description">The status of the request Added, AddedAndNotFound,
   * or NotFound.</td>
   * </tr>
   * <tr>
   * <td class="name">geomarkGroupId</td>
   * <td class="type"><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a></td>
   * <td class="description">The geomark group identifier.</td>
   * </tr>
   * <tr>
   * <td class="name">geomarkIds</td>
   * <td class="type "><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/List.html?is-external=true"
   * >List</a>&lt;<a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a>&gt;</td>
   * <td class="description">The list of geomark identifiers that were added to
   * the group.</td>
   * </tr>
   * <tr>
   * <td class="name">notFoundGeomarkIds</td>
   * <td class="type "><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/List.html?is-external=true"
   * >List</a>&lt;<a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a>&gt;</td>
   * <td class="description">The list of geomark identifiers that could not be
   * found and were NOT added to the group.</td>
   * </tr>
   * </tbody>
   * </table>
   * </div>
   *
   * @param geomarkGroupId The geomark group identifier.
   * @param geomarkGroupSecretKey The geomark group secret key used to sign the
   *          request.
   * @param geomarkIds The collection (e.g. List) of geomark URLs or
   *          identifiers.
   * @return The response map.
   */
  public Map<String, Object> addGeomarksToGroup(final String geomarkGroupId,
    final String geomarkGroupSecretKey, final Iterable<String> geomarkIds) {
    final String path = "/geomarkGroups/" + geomarkGroupId + "/geomarks/add";
    final Map<String, Object> parameters = new TreeMap<String, Object>();
    parameters.put("geomarkId", geomarkIds);
    return UrlUtil.signedPostRequestJsonResponse(geomarkGroupSecretKey, this.serverUrl, path,
      parameters);
  }

  /**
   * <p>
   * Add the geomarks to the geomark group.
   * </p>
   * *
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   * String geomarkGroupId = &quot;...&quot;;
   * String geomarkGroupSecretKey = &quot;...&quot;;
   *
   * Map&lt;String, Object&gt; response = client.addGeomarksToGroup(geomarkGroupId,
   *   geomarkGroupSecretKey, &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;);
   * System.out.println(response);
   * </code></pre>
   *
   * <div class="title">Result Attributes</div>
   * <p>
   * The method returns a map with the following attributes.
   * </p>
   * <div class="simpleDataTable">
   * <table class="data">
   * <caption>Result Attributes</caption>
   * <thead>
   * <tr>
   * <th>Name</th>
   * <th>Type</th>
   * <th>Description</th>
   * </tr>
   * </thead> <tbody>
   * <tr>
   * <td class="name">status</td>
   * <td class="type"><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a></td>
   * <td class="description">The status of the request Added, AddedAndNotFound,
   * or NotFound.</td>
   * </tr>
   * <tr>
   * <td class="name">geomarkGroupId</td>
   * <td class="type"><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a></td>
   * <td class="description">The geomark group identifier.</td>
   * </tr>
   * <tr>
   * <td class="name">geomarkIds</td>
   * <td class="type "><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/List.html?is-external=true"
   * >List</a>&lt;<a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a>&gt;</td>
   * <td class="description">The list of geomark identifiers that were added to
   * the group.</td>
   * </tr>
   * <tr>
   * <td class="name">notFoundGeomarkIds</td>
   * <td class="type "><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/List.html?is-external=true"
   * >List</a>&lt;<a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a>&gt;</td>
   * <td class="description">The list of geomark identifiers that could not be
   * found and were NOT added to the group.</td>
   * </tr>
   * </tbody>
   * </table>
   * </div>
   *
   * @param geomarkGroupId The geomark group identifier.
   * @param geomarkGroupSecretKey The geomark group secret key used to sign the
   *          request.
   * @param geomarkIds The array of geomark URLs or identifiers.
   * @return The response map.
   */
  public Map<String, Object> addGeomarksToGroup(final String geomarkGroupId,
    final String geomarkGroupSecretKey, final String... geomarkIds) {
    return addGeomarksToGroup(geomarkGroupId, geomarkGroupSecretKey, Arrays.asList(geomarkIds));
  }

  /**
   * <p>
   * Construct a new new geomark by copying the geometries from one or more existing
   * geomarks using the <a
   * href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.createGeomarkCopy"
   * >Create Geomark Copy</a> REST API.
   * </p>
   * <p>
   * The extended parameters to the method are passed in using a Java (map). See
   * the <a
   * href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.createGeomarkCopy"
   * >Create Geomark Copy</a> REST API for the list of supported parameters.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   * Map&lt;String, Object&gt; parameters = new HashMap&lt;String, Object&gt;();
   * parameter.put(GeomarkClient.BUFFER_METRES, 10);
   *
   * List&lt;String&gt; geomarks = Arrays.asList(&quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;);
   *
   * String geomarkUrl = client.copyGeomark(parameters, geomarks);
   * </code></pre>
   *
   * @param parameters The parameters used to create the request.
   * @param geomarks The collection (e.g. List) of geomark URLs or identifiers
   *          to create the new geomark from.
   * @return The URL to the geomark info resource.
   */
  public String copyGeomark(final Map<String, ? extends Object> parameters,
    final Iterable<String> geomarks) {
    final Map<String, Object> mergedParameters = new LinkedHashMap<String, Object>(parameters);
    mergedParameters.put("geomarkUrl", geomarks);
    mergedParameters.put("resultFormat", "json");

    final Map<String, Object> response = UrlUtil
      .postRequestJsonResponse(this.serverUrl + "/geomarks/copy", mergedParameters);
    return getGeomarkUrl(response);
  }

  /**
   * <p>
   * Construct a new new geomark by copying the geometries from one or more existing
   * geomarks using the <a
   * href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.createGeomarkCopy"
   * >Create Geomark Copy</a> REST API.
   * </p>
   * <p>
   * The extended parameters to the method are passed in using a Java (map). See
   * the <a
   * href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.createGeomarkCopy"
   * >Create Geomark Copy</a> REST API for the list of supported parameters.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   * Map&lt;String, Object&gt; parameters = new HashMap&lt;String, Object&gt;();
   * parameter.put(GeomarkClient.BUFFER_METRES, 10);
   *
   * String geomarkUrl = client.copyGeomark(parameters,
   *   &quot;gm-abcdefghijklmnopqrstuvwxyz0000bc&quot;);
   * </code></pre>
   *
   * @param parameters The parameters used to create the request.
   * @param geomarks The array of geomark URLs or identifiers to create the new
   *          geomark from.
   * @return The URL to the geomark info resource.
   */
  public String copyGeomark(final Map<String, ? extends Object> parameters,
    final String... geomarks) {
    return copyGeomark(parameters, Arrays.asList(geomarks));
  }

  /**
   * <p>
   * Delete the geomarks from the geomark group.The method does not check if the
   * geomark was a member of the group before deleting, this allows the method
   * to be called multiple times without failure.
   * </p>
   * *
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   * String geomarkGroupId = &quot;...&quot;;
   * String geomarkGroupSecretKey = &quot;...&quot;;
   *
   * Map&lt;String, Object&gt; response = client.deleteGeomarksFromGroup(geomarkGroupId,
   *   geomarkGroupSecretKey, 90, &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;);
   * System.out.println(response);
   * </code></pre>
   *
   * <div class="title">Result Attributes</div>
   * <p>
   * The method returns a map with the following attributes.
   * </p>
   * <div class="simpleDataTable">
   * <table class="data">
   * <caption>Result Attributes</caption>
   * <thead>
   * <tr>
   * <th>Name</th>
   * <th>Type</th>
   * <th>Description</th>
   * </tr>
   * </thead> <tbody>
   * <tr>
   * <td class="name">status</td>
   * <td class="type"><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a></td>
   * <td class="description">The status of the request Deleted.</td>
   * </tr>
   * <tr>
   * <td class="name">geomarkGroupId</td>
   * <td class="type"><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a></td>
   * <td class="description">The geomark group identifier.</td>
   * </tr>
   * <tr>
   * <td class="name">geomarkIds</td>
   * <td class="type "><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/List.html?is-external=true"
   * >List</a>&lt;<a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a>&gt;</td>
   * <td class="description">The list of geomark identifiers that were deleted
   * from the group.</td>
   * </tr>
   * </tbody>
   * </table>
   * </div>
   *
   * @param geomarkGroupId The geomark group identifier.
   * @param geomarkGroupSecretKey The geomark group secret key used to sign the
   *          request.
   * @param expiryDays The number of days from today before the geomark will
   *          expire. Can be null to not change the expiry.
   * @param geomarkIds The collection (e.g. List) of geomark URLs or
   *          identifiers.
   * @return The response map.
   */
  public Map<String, Object> deleteGeomarksFromGroup(final String geomarkGroupId,
    final String geomarkGroupSecretKey, final Integer expiryDays,
    final Iterable<String> geomarkIds) {
    final String path = "/geomarkGroups/" + geomarkGroupId + "/geomarks/delete";
    final Map<String, Object> parameters = new TreeMap<String, Object>();
    parameters.put("geomarkId", geomarkIds);
    if (expiryDays != null) {
      parameters.put("expiryDays", expiryDays);
    }
    return UrlUtil.signedPostRequestJsonResponse(geomarkGroupSecretKey, this.serverUrl, path,
      parameters);
  }

  /**
   * <p>
   * Delete the geomarks from the geomark group.The method does not check if the
   * geomark was a member of the group before deleting, this allows the method
   * to be called multiple times without failure.
   * </p>
   * *
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   * String geomarkGroupId = &quot;...&quot;;
   * String geomarkGroupSecretKey = &quot;...&quot;;
   *
   * Map&lt;String, Object&gt; response = client.deleteGeomarksFromGroup(geomarkGroupId,
   *   geomarkGroupSecretKey, 90, &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;);
   * System.out.println(response);
   * </code></pre>
   *
   * <div class="title">Result Attributes</div>
   * <p>
   * The method returns a map with the following attributes.
   * </p>
   * <div class="simpleDataTable">
   * <table class="data">
   * <caption>Result Attributes</caption>
   * <thead>
   * <tr>
   * <th>Name</th>
   * <th>Type</th>
   * <th>Description</th>
   * </tr>
   * </thead> <tbody>
   * <tr>
   * <td class="name">status</td>
   * <td class="type"><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a></td>
   * <td class="description">The status of the request Deleted.</td>
   * </tr>
   * <tr>
   * <td class="name">geomarkGroupId</td>
   * <td class="type"><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a></td>
   * <td class="description">The geomark group identifier.</td>
   * </tr>
   * <tr>
   * <td class="name">geomarkIds</td>
   * <td class="type "><a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/List.html?is-external=true"
   * >List</a>&lt;<a class="" href=
   * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html?is-external=true"
   * >String</a>&gt;</td>
   * <td class="description">The list of geomark identifiers that were deleted
   * from the group.</td>
   * </tr>
   * </tbody>
   * </table>
   * </div>
   *
   * @param geomarkGroupId The geomark group identifier.
   * @param geomarkGroupSecretKey The geomark group secret key used to sign the
   *          request.
   * @param expiryDays The number of days from today before the geomark will
   *          expire. Can be null to not change the expiry.
   * @param geomarkIds The array of geomark URLs or identifiers.
   * @return The response map.
   */
  public Map<String, Object> deleteGeomarksFromGroup(final String geomarkGroupId,
    final String geomarkGroupSecretKey, final Integer expiryDays, final String... geomarkIds) {
    return deleteGeomarksFromGroup(geomarkGroupId, geomarkGroupSecretKey, expiryDays,
      Arrays.asList(geomarkIds));
  }

  /**
   * <p>
   * Get an InputStream to the <a
   * href="../featureAttributes.html">geomark bounding box</a>
   * using the <a href=
   * "../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkBoundingBox">Get
   * Geomark Bounding Box</a> REST API.
   * </p>
   * <p>
   * The caller must close the stream when the resource is downloaded. Use a
   * try/finally block to ensure the stream is closed.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * String geomarkId = &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;;
   *
   * InputStream in = client.getGeomarkBoundingBoxStream(geomarkId, &quot;kml&quot;, 4326);
   * try {
   *   // Read the stream
   * } finally {
   *   in.close();
   * }
   * </code></pre>
   *
   * @param geomarkId The geomark URL or identifier.
   * @param format The file extension or MIME media type of the file format for
   *          result.
   * @param srid The EPSG coordinate system id to use for the geometry.
   * @return An InputStream to the geomark bounding box resource.
   */
  public InputStream getGeomarkBoundingBoxStream(final String geomarkId, final String format,
    final Integer srid) {
    final String urlString = getGeomarkBoundingBoxUrl(geomarkId, format, srid);
    return UrlUtil.getInputStream(urlString);
  }

  /**
   * <p>
   * Get the URL to the <a
   * href="../featureAttributes.html">geomark bounding box</a>
   * using the <a href=
   * "../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkBoundingBox">Get
   * Geomark Bounding Box</a> REST API.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * String geomarkId = &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;;
   *
   * String geomarkUrl = client.getGeomarkBoundingBoxUrl(geomarkId, &quot;kml&quot;, 4326);
   * System.out.println(geomarkUrl);
   * </code></pre>
   *
   * @param geomarkId The geomark URL or identifier.
   * @param format The file extension or MIME media type of the file format for
   *          result.
   * @param srid The EPSG coordinate system id to use for the geometry. * @return
   *          The URL to the geomark bounding box resource.
   * @return The URL to the geomark bounding box resource.
   */
  public String getGeomarkBoundingBoxUrl(final String geomarkId, final String format,
    final Integer srid) {
    return getResourceUrl(geomarkId, "boundingBox", format, srid);
  }

  /**
   * <p>
   * Get an InputStream to the <a
   * href="../featureAttributes.html">geomark feature</a> using
   * the <a
   * href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkFeature"
   * >Get Geomark Feature</a> REST API.
   * </p>
   * <p>
   * The caller must close the stream when the resource is downloaded. Use a
   * try/finally block to ensure the stream is closed.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * String geomarkId = &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;;
   *
   * InputStream in = client.getGeomarkFeatureStream(geomarkId, &quot;kml&quot;, 4326);
   * try {
   *   // Read the stream
   * } finally {
   *   in.close();
   * }
   * </code></pre>
   *
   * @param geomarkId The geomark URL or identifier.
   * @param format The file extension or MIME media type of the file format for
   *          result.
   * @param srid The EPSG coordinate system id to use for the geometry.
   * @return An InputStream to the geomark feature resource.
   */
  public InputStream getGeomarkFeatureStream(final String geomarkId, final String format,
    final Integer srid) {
    final String urlString = getGeomarkFeatureUrl(geomarkId, format, srid);
    return UrlUtil.getInputStream(urlString);
  }

  /**
   * <p>
   * Get the URL to the <a
   * href="../featureAttributes.html">geomark feature</a> using
   * the <a
   * href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkFeature"
   * >Get Geomark Feature</a> REST API.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * String geomarkId = &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;;
   *
   * String geomarkUrl = client.getGeomarkFeatureUrl(geomarkId, &quot;kml&quot;, 4326);
   * System.out.println(geomarkUrl);
   * </code></pre>
   *
   * @param geomarkId The geomark URL or identifier.
   * @param format The file extension or MIME media type of the file format for
   *          result.
   * @param srid The EPSG coordinate system id to use for the geometry.
   * @return The URL to the geomark feature resource.
   */
  public String getGeomarkFeatureUrl(final String geomarkId, final String format,
    final Integer srid) {
    return getResourceUrl(geomarkId, "feature", format, srid);
  }

  /**
   * <p>
   * Get a Map containing the <a
   * href="../infoAttributes.html">geomark info</a> using the <a
   * href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkInfo">Get
   * Geomark Info</a> REST API.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * String geomarkId = &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;;
   *
   * Map&lt;String, Object&gt; geomark = client.getGeomarkInfo(geomarkId);
   * System.out.println(geomark);
   * </code></pre>
   *
   * @param geomarkId The geomark URL or identifier.
   * @return The map containing the geomark info properties.
   */
  public Map<String, Object> getGeomarkInfo(final String geomarkId) {
    final String geomarkUrl = getGeomarkUrl(geomarkId) + ".json";
    try {
      final URL url = new URL(geomarkUrl);
      try (
        final InputStream in = url.openStream();
        final Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);) {
        return JsonParser.getMap(reader);
      }
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to get info from " + geomarkUrl, e);
    }
  }

  /**
   * <p>
   * Get an InputStream to the <a
   * href="../infoAttributes.html">geomark info</a> using the <a
   * href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkInfo">Get
   * Geomark Info</a> REST API.
   * </p>
   * <p>
   * The caller must close the stream when the resource is downloaded. Use a
   * try/finally block to ensure the stream is closed.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * String geomarkId = &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;;
   *
   * InputStream in = client.getGeomarkInfoStream(geomarkId, &quot;json&quot;);
   * try {
   *   // Read the stream
   * } finally {
   *   in.close();
   * }
   * </code></pre>
   *
   * @param geomarkId The geomark URL or identifier.
   * @param format The file extension or MIME media type of the file format for
   *          result.
   * @return An InputStream to the geomark info resource.
   */
  public InputStream getGeomarkInfoStream(final String geomarkId, final String format) {
    final String urlString = getGeomarkInfoUrl(geomarkId, format);
    return UrlUtil.getInputStream(urlString);
  }

  /**
   * <p>
   * Get the URL to the <a href="../infoAttributes.html">geomark
   * info</a> using the <a
   * href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkInfo">Get
   * Geomark Info</a> REST API.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * String geomarkId = &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;;
   *
   * String geomarkUrl = client.getGeomarkInfoUrl(geomarkId, &quot;json&quot;);
   * System.out.println(geomarkUrl);
   * </code></pre>
   *
   * @param geomarkId The geomark URL or identifier.
   * @param format The file extension or MIME media type of the file format for
   *          result.
   * @return The URL to the geomark info resource.
   */
  public String getGeomarkInfoUrl(final String geomarkId, final String format) {
    return getResourceUrl(geomarkId, "info", format, null);
  }

  /**
   * <p>
   * Get an InputStream to the <a
   * href="../featureAttributes.html">geomark parts</a> using the
   * <a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkParts">
   * Get Geomark Parts</a> REST API.
   * </p>
   * <p>
   * The caller must close the stream when the resource is downloaded. Use a
   * try/finally block to ensure the stream is closed.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * String geomarkId = &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;;
   *
   * InputStream in = client.getGeomarkPartsStream(geomarkId, &quot;kml&quot;, 4326);
   * try {
   *   // Read the stream
   * } finally {
   *   in.close();
   * }
   * </code></pre>
   *
   * @param geomarkId The geomark URL or identifier.
   * @param format The file extension or MIME media type of the file format for
   *          result.
   * @param srid The EPSG coordinate system id to use for the geometry.
   * @return An InputStream to the geomark parts resource.
   */
  public InputStream getGeomarkPartsStream(final String geomarkId, final String format,
    final Integer srid) {
    final String urlString = getGeomarkPartsUrl(geomarkId, format, srid);
    return UrlUtil.getInputStream(urlString);
  }

  /**
   * <p>
   * Get the URL to the <a
   * href="../featureAttributes.html">geomark parts</a> using the
   * <a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkParts">
   * Get Geomark Parts</a> REST API.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * String geomarkId = &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;;
   *
   * String geomarkUrl = client.getGeomarkPartsUrl(geomarkId, &quot;kml&quot;, 4326);
   * System.out.println(geomarkUrl);
   * </code></pre>
   *
   * @param geomarkId The geomark URL or identifier.
   * @param format The file extension or MIME media type of the file format for
   *          result.
   * @param srid The EPSG coordinate system id to use for the geometry.
   * @return The URL to the geomark parts resource.
   */
  public String getGeomarkPartsUrl(final String geomarkId, final String format,
    final Integer srid) {
    return getResourceUrl(geomarkId, "parts", format, srid);
  }

  /**
   * <p>
   * Get an InputStream to the <a
   * href="../featureAttributes.html">geomark point</a> using the
   * <a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkPoint">
   * Get Geomark Point</a> REST API.
   * </p>
   * <p>
   * The caller must close the stream when the resource is downloaded. Use a
   * try/finally block to ensure the stream is closed.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * String geomarkId = &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;;
   *
   * InputStream in = client.getGeomarkPointStream(geomarkId, &quot;kml&quot;, 4326);
   * try {
   *   // Read the stream
   * } finally {
   *   in.close();
   * }
   * </code></pre>
   *
   * @param geomarkId The geomark URL or identifier.
   * @param format The file extension or MIME media type of the file format for
   *          result.
   * @param srid The EPSG coordinate system id to use for the geometry.
   * @return An InputStream to the geomark point resource.
   */
  public InputStream getGeomarkPointStream(final String geomarkId, final String format,
    final Integer srid) {
    final String urlString = getGeomarkPointUrl(geomarkId, format, srid);
    return UrlUtil.getInputStream(urlString);
  }

  /**
   * <p>
   * Get the URL to the <a
   * href="../featureAttributes.html">geomark point</a> using the
   * <a href="../rest-api/#ca.bc.gov.geomark.web.rest.Geomark.getGeomarkPoint">
   * Get Geomark Point</a> REST API.
   * </p>
   * <p>
   * The following code fragment shows an example of using the API.
   * </p>
   *
   * <pre class="prettyprint"><code class="language-java">
   * String baseUrl = &quot;https://apps.gov.bc.ca/pub/geomark&quot;;
   * GeomarkClient client = new GeomarkClient(baseUrl);
   *
   * String geomarkId = &quot;gm-abcdefghijklmnopqrstuv0bcislands&quot;;
   *
   * String geomarkUrl = client.getGeomarkPointUrl(geomarkId, &quot;kml&quot;, 4326);
   * System.out.println(geomarkUrl);
   * </code></pre>
   *
   * @param geomarkId The geomark URL or identifier.
   * @param format The file extension or MIME media type of the file format for
   *          result.
   * @param srid The EPSG coordinate system id to use for the geometry.
   * @return The URL to the geomark point resource.
   */
  public String getGeomarkPointUrl(final String geomarkId, final String format,
    final Integer srid) {
    return getResourceUrl(geomarkId, "point", format, srid);
  }

  /**
   * Get the URL to the geomark from the response map.
   *
   * @param response The response returned from a create geomark resource.
   * @return The URL to the geomark info resource.
   * @throws IllegalArgumentException If an error was returned in the response.
   */
  private String getGeomarkUrl(final Map<String, Object> response) {
    final String error = (String)response.get("error");
    if (error == null) {
      final String geomarkUrl = (String)response.get("url");
      if (geomarkUrl == null) {
        throw new IllegalArgumentException("Invalid response: " + response);
      }
      return geomarkUrl;
    } else {
      throw new IllegalArgumentException(error);
    }
  }

  /**
   * Get the URL to the Geomark info resource from the geomarkId. If the
   * geomarkId is a http(s) URL then any trailing / characters, query string or
   * anchor is removed from the URL and the cleaned value is returned. Otherwise
   * the geomarkUrl is created from the serverUrl and the geomarkId.
   *
   * @param geomarkId The geomark URL or identifier.
   * @return The URL to the geomark info resource.
   */
  private String getGeomarkUrl(final String geomarkId) {
    if (geomarkId.startsWith("http")) {
      String geomarkUrl = geomarkId.replaceAll("\\?.*$", "");
      geomarkUrl = geomarkUrl.replaceAll("#.*$", "");
      geomarkUrl = geomarkUrl.replaceAll("/+$", "");
      return UrlUtil.cleanPath(geomarkUrl);
    } else if (this.serverUrl == null) {
      throw new IllegalArgumentException(
        "A http or https URL to the Geomark info resource must be specified.");
    } else {
      return this.serverUrl + "/geomarks/" + geomarkId;
    }
  }

  /**
   * Get the URL (href) to the resource link for the specified resource,
   * mediaType and srid parameters for the geomarkId. The method downloads the
   * Geomark info resource in JSON format and reads the resourceLinks to find a
   * match.
   *
   * @param geomarkId The geomark URL or identifier. The geomark identifier or
   *          URL.
   * @param resource The name of the resource (e.g. info, feature, parts, point,
   *          boundingBox).
   * @param format The file extension or MIME media type of the file format that
   *          the URL will return.
   * @param srid The EPSG coordinate system id used for the geometry. Must be
   *          null for the info resource.
   * @return The URL to the resource.
   * @throws IllegalArgumentException If the URL for the specified parameters
   *           could not be found.
   */
  private String getResourceUrl(final String geomarkId, final String resource, final String format,
    final Integer srid) {
    final Map<String, Object> geomarkInfo = getGeomarkInfo(geomarkId);

    @SuppressWarnings("unchecked")
    final List<Map<String, Object>> resourceLinks = (List<Map<String, Object>>)geomarkInfo
      .get("resourceLinks");
    if (resourceLinks != null) {
      for (final Map<String, Object> resourceLink : resourceLinks) {
        final String linkResource = (String)resourceLink.get("resource");
        if (resource.equals(linkResource)) {
          final String linkMimeType = (String)resourceLink.get("mediaType");
          final String linkFormat = (String)resourceLink.get("format");
          if (format.equals(linkMimeType) || format.equals(linkFormat)) {
            final Number linkSrid = (Number)resourceLink.get("srid");
            if (srid == null && linkSrid == null
              || srid != null && linkSrid != null && srid.equals(linkSrid.intValue())) {
              final String linkHref = (String)resourceLink.get("href");
              if (linkHref != null) {
                return linkHref;
              }
            }
          }
        }
      }
    }

    throw new IllegalArgumentException("URL for resource=" + resource + ",format=" + format
      + ",srid=" + srid + " not available for geomark " + geomarkId);
  }
}
