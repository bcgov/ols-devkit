package ca.bc.gov.geomark.web.domain;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.jeometry.common.logging.Logs;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.io.GeometryReaderFactory;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.IoFactory;
import com.revolsys.jdbc.io.JdbcDatabaseFactory;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.json.JsonList;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.property.ShortNameProperty;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.ManifestUtil;

public class GeomarkConfig extends BaseObjectWithProperties {

  private static GeomarkConfig config;

  /**
   * The list of file format extensions supported by the create Geomark page.
   */
  public static final List<String> GEOMARK_INPUT_FORMATS = Arrays.asList("kml", "kmz", "shpz",
    "shp", "geojson", "gpkg", "gml", "wkt");

  /**
   * The list of file format extensions supported by the Geomark info page.
   */
  public static final List<String> GEOMARK_OUTPUT_FORMATS = Arrays.asList("kml", "kmz", "shpz",
    "geojson", "gpkg", "gml", "wkt");

  /** The precision model for geographics geometries. */
  public static final GeometryFactory GEOGRAPHICS_GEOMETRY_FACTORY = GeometryFactory
    .fixed2d(1000000.0, 1000000.0);

  /** The precision model for projected geometries. */
  public static final GeometryFactory PROJECTED_GEOMETRY_FACTORY = GeometryFactory.fixed2d(1000.0,
    1000.0);

  public synchronized static GeomarkConfig getConfig() {
    if (config == null) {
      config = new GeomarkConfig();
    }
    return config;
  }

  private MapEx footerLinks = MapEx.EMPTY;

  private final Map<String, Supplier<String>> defaultValues = new TreeMap<>();

  /** The geometry factory used to create geomark geometries. */
  private GeometryFactory geometryFactory = GeometryFactory.fixed2d(3005, 1000.0, 1000.0);

  /** The area the Geomark geometries must intersect. */
  private Polygon area = getAreaGeometryFactory().geometry(
    "SRID=3005;POLYGON((945180.187 1749393.307,507921.103 1780303.17,-12733.53 1877610.815,-43173.406 1693239.207,705286.362 287361.156,1192363.698 288027.848,1281604.564 369771.209,1909580.226 450780.25,1946331.003 538377.603,1856319.15 710793.626,1471065.921 1035179.445,1389273.7 1762816.63,945180.187 1749393.307))");

  private boolean useXForwardFilter = true;

  /** The coordinate systems supported by the Geomark service. */
  private List<CoordinateSystem> coordinateSystems = Arrays.asList(
    EpsgCoordinateSystems.getCoordinateSystem(4326),
    EpsgCoordinateSystems.getCoordinateSystem(3005),
    EpsgCoordinateSystems.getCoordinateSystem(3857),
    EpsgCoordinateSystems.getCoordinateSystem(26907),
    EpsgCoordinateSystems.getCoordinateSystem(26908),
    EpsgCoordinateSystems.getCoordinateSystem(26909),
    EpsgCoordinateSystems.getCoordinateSystem(26910),
    EpsgCoordinateSystems.getCoordinateSystem(26911));

  /** The default geometry factory to use if none specified in the request. */
  private GeometryFactory defaultGeometryFactory = GeometryFactory.wgs84();

  /** Labels for the file formats. */
  private final Map<String, String> fileFormatLabels = Maps.<String, String> buildHash()//
    .add("gpkg", "GeoPackage")//
    .add("kmz", "Google Earth")//
    .add("kml", "Google Earth")//
    .add("gml", "Geography Markup Language")//
    .add("geojson", "GeoJSON")//
    .add("shpz", "ESRI Shapefile inside a ZIP archive")//
    .add("shp", "ESRI Shapefile")//
    .add("wkt", "Well-Known Text Geometry")//
    .getMap();

  private final Map<String, String> fileFormatHints = Maps.<String, String> buildHash()//
    .add("shpz", ".shpz is a ZIP with shp, shx, dbf and prj files")//
    .getMap();

  private final JsonList fileFormats = JsonList.array();

  /** The record definition for the geomark. */
  private RecordDefinitionImpl recordDefinition;

  /** The record store used to interact with the Geomark database. */
  private RecordStore recordStore;

  private GeomarkConfig() {
    GeomarkConfig.config = this;

    addDefaultValues();
    init();
  }

  private void addDefaultValue(final String name, final String value) {
    this.defaultValues.put(name, () -> value);
  }

  private void addDefaultValueDeveloperUrl(final String name, final String path) {
    this.defaultValues.put(name, () -> getString("developerDocsUrl") + path);
  }

  private void addDefaultValues() {
    addDefaultValue("businessHomeUrl",
      "https://www2.gov.bc.ca/gov/content?id=F6BAF45131954020BCFD2EBCC456F084");
    addDefaultValue("developerDocsUrl", "https://pauldaustin.github.io/geomark/");
    addDefaultValue("mapLayerUrl",
      "https://maps.gov.bc.ca/arcgis/services/province/roads_wm/MapServer/WMSServer");
    addDefaultValueDeveloperUrl("kmlStyleUrl", "kml/geomarkStyle.kml#geomarkDefault");
    addDefaultValueDeveloperUrl("glossaryUrl", "glossary.html");
    addDefaultValueDeveloperUrl("createFromClipboardTutorialUrl",
      "tutorial/create_geomark_from_clipboard_tutorial.pdf");
    addDefaultValueDeveloperUrl("createFromGeomarkTutorialUrl",
      "tutorial/create_from_geomarks_tutorial.pdf");
    addDefaultValueDeveloperUrl("createFromFileTutorialUrl",
      "tutorial/create_geomark_from_file_tutorial.pdf");
    addDefaultValueDeveloperUrl("googleEarthTurorialUrl",
      "tutorial/create_geomark_in_googleearth_tutorial.pdf");

    addDefaultValue("mapLayerMaxZoom", "17");
    addDefaultValue("maxGeomarkAgeDays", "90");
    addDefaultValue("maxVertices", "10000");
    addDefaultValue("kmlLookAtMinRange", "1000");
    addDefaultValue("kmlLookAtMaxRange", "1826047");
  }

  /**
   * Destory the instance, closing all resources and setting them to null.
   */
  public void destroy() {
    this.area = null;
    this.coordinateSystems = null;
    this.recordStore = null;
    this.defaultGeometryFactory = null;
    this.geometryFactory = null;
    this.recordDefinition = null;
  }

  /**
   * Get the area the Geomark geometries must intersect.
   *
   * @return The area the Geomark geometries must intersect.
   */
  public Geometry getArea() {
    return this.area;
  }

  private GeometryFactory getAreaGeometryFactory() {
    return this.geometryFactory.toFloating2d();
  }

  /**
   * Get the coordinate systems supported by the Geomark service.
   *
   * @return The coordinate systems supported by the Geomark service.
   */
  public List<CoordinateSystem> getCoordinateSystems() {
    return this.coordinateSystems;
  }

  public DecimalFormat getDecimalFormat() {
    final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
    decimalFormatSymbols.setGroupingSeparator(' ');

    /** The formatter for decimals. */
    final DecimalFormat decimalFormat = new DecimalFormat("#,##0.#######################");
    decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
    return decimalFormat;
  }

  public GeometryFactory getDefaultGeometryFactory() {
    return this.defaultGeometryFactory;
  }

  public Map<String, String> getFileFormatLabels() {
    return this.fileFormatLabels;
  }

  public JsonList getFileFormats() {
    return this.fileFormats;
  }

  public MapEx getFooterLinks() {
    return this.footerLinks;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public int getInt(final String name) {
    final String value = getString(name);
    return Integer.parseInt(value);
  }

  public long getLong(final String name) {
    final String value = getString(name);
    return Long.parseLong(value);
  }

  public RecordDefinitionImpl getRecordDefinition() {
    return this.recordDefinition;
  }

  /**
   * Get the record store used to interact with the Geomark database.
   *
   * @return The data record used to interact with the Geomark database.
   */
  public RecordStore getRecordStore() {
    return this.recordStore;
  }

  public String getServerUrl(final HttpServletRequest request) {
    if (isUseXForwardFilter()) {
      String serverName = request.getHeader("x-forwarded-host");
      if (serverName == null) {
        serverName = request.getServerName();
      }
      return "https://" + serverName;
    } else {
      final String serverName = request.getServerName();
      final String scheme = request.getScheme();
      final int port = request.getServerPort();

      String serverUrl = scheme + "://" + serverName;
      if (port != 80 || port != 443) {
        serverUrl += ":" + port;
      }
      return serverUrl;
    }
  }

  public String getString(final String name) {
    final Object property = getProperty(name);
    if (property == null) {
      final Supplier<String> supplier = this.defaultValues.get(name);
      if (supplier == null) {
        return null;
      } else {
        return supplier.get();
      }
    } else {
      return property.toString();
    }
  }

  public String getVersion() {
    return ManifestUtil.getImplementationVersion("Geomark Java Client API");
  }

  /**
   * Initialize the outputFormat configuration.
   */
  private void init() {
    initPropertiesFile();

    final MapEx connectionConfig = new LinkedHashMapEx() //
      .add("url", getString("geomarkDataSourceUrl")) //
      .add("user", "proxy_geomark_web") //
      .add("password", getString("geomarkDataSourcePassword")) //
      .add("minPoolSize", 0) //
      .add("maxPoolSize", 100) //
    ;

    final JdbcDatabaseFactory databaseFactory = JdbcDatabaseFactory
      .databaseFactory(connectionConfig);
    this.recordStore = databaseFactory.newRecordStore(connectionConfig);
    this.recordStore.initialize();
    final RecordDefinition configPropertyRecordDefinition = this.recordStore
      .getRecordDefinition(GeomarkConstants.CONFIG_PROPERTY);

    final ShortNameProperty cpShortName = new ShortNameProperty();
    cpShortName.setShortName("GMK_CP");
    cpShortName.setRecordDefinition(configPropertyRecordDefinition);

    initPropertiesDatabase();

    initFileFormats();

    this.recordDefinition = (RecordDefinitionImpl)this.recordStore
      .getRecordDefinition(GeomarkConstants.GEOMARK_POLY);
    this.recordDefinition.setIdFieldName("GEOMARK_ID");
  }

  private void initFileFormats() {
    final Set<String> allFormats = new LinkedHashSet<>(GEOMARK_INPUT_FORMATS);
    allFormats.addAll(GEOMARK_OUTPUT_FORMATS);

    for (final String fileExtension : allFormats) {
      final String description = this.fileFormatLabels.get(fileExtension) + " (" + fileExtension
        + ")";
      final RecordWriterFactory recordWriterFactory = IoFactory
        .factoryByFileExtension(RecordWriterFactory.class, fileExtension);
      final boolean input = IoFactory.factoryByFileExtension(GeometryReaderFactory.class,
        fileExtension) != null;
      final boolean output = recordWriterFactory != null;
      final String hint = this.fileFormatHints.get(fileExtension);
      final List<Integer> coordinateSystemIds = new ArrayList<>();
      for (final CoordinateSystem coordinateSystem : this.coordinateSystems) {
        if (recordWriterFactory.isCoordinateSystemSupported(coordinateSystem)) {
          coordinateSystemIds.add(coordinateSystem.getHorizontalCoordinateSystemId());
        }
      }
      final JsonObject format = JsonObject.hash() //
        .add("fileExtension", fileExtension) //
        .add("description", description) //
        .add("coordinateSystemIds", coordinateSystemIds) //
        .add("text", !recordWriterFactory.isBinary()) //
        .add("input", input) //
        .add("output", output) //
        .add("hint", hint) //
      ;
      this.fileFormats.add(format);
    }
  }

  private void initPropertiesDatabase() {
    try (
      RecordReader reader = this.recordStore.getRecords(GeomarkConstants.CONFIG_PROPERTY)) {
      for (final Record configRecord : reader) {
        try {
          String name = configRecord.getString(GeomarkConstants.PROPERTY_NAME);
          name = name
            .replaceAll("^(geomarkService|geomarkConfig|geomarkSiteminderUserDetailsService).", "");

          String value = configRecord.getString(GeomarkConstants.PROPERTY_VALUE);
          if (value != null) {
            value = value.trim();
            if (value.length() == 0) {
              value = null;
            }
          }
          setProperty(name, value);
        } catch (final Exception e) {
          Logs.error(this, "Invalid config property :" + configRecord);
        }
      }
    }
  }

  private void initPropertiesFile() {
    for (final String filePath : Arrays.asList("/apps/config/geomark/geomark.properties")) {
      final Path file = Paths.get(filePath);
      if (Files.exists(file)) {
        try {
          final Properties properties = new Properties();
          try (
            FileReader reader = new FileReader(file.toFile())) {
            properties.load(reader);
            for (final Object key : properties.keySet()) {
              final String name = key.toString();
              final Object value = properties.get(name);
              if (value != null) {
                setProperty(name, value);
              }
            }
          }
        } catch (final Exception e) {
          Logs.error(this, "Properties invalid: " + file, e);
        }
      }
    }
    for (final String filePath : Arrays.asList("conf/geomark.json", "../../src/config/geomark.json",
      "src/config/geomark.json", "/apps/config/geomark/geomark.json")) {
      final Path file = Paths.get(filePath);
      if (Files.exists(file)) {
        try {
          final JsonObject properties = Json.toMap(file);
          if (properties != null) {
            for (final Object key : properties.keySet()) {
              final String name = key.toString();
              final Object value = properties.get(name);
              if (value != null) {
                setProperty(name, value);
              }
            }
          }
        } catch (final Exception e) {
          Logs.error(this, "Properties invalid: " + file, e);
        }
      }
    }
  }

  public boolean isUseXForwardFilter() {
    return this.useXForwardFilter;
  }

  /**
   * Set the area the Geomark polygons must intersect.
   *
   * @param area The area the Geomark polygons must intersect.
   */
  public void setArea(final Polygon area) {
    this.area = getAreaGeometryFactory().polygon(area);
  }

  public void setFooterLinks(final MapEx footerLinks) {
    this.footerLinks = footerLinks;
  }

  /**
   * Set the record store used to interact with the Geomark database.
   *
   * @param recordStore The record store used to interact with the Geomark database.
   */
  public void setRecordStore(final RecordStore recordStore) {
    this.recordStore = recordStore;
  }

  public void setUseXForwardFilter(final boolean useXForwardFilter) {
    this.useXForwardFilter = useXForwardFilter;
  }
}
