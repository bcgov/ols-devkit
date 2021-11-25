package com.revolsys.geopackage;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.jeometry.common.logging.Logs;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.LockingMode;
import org.sqlite.SQLiteConnection;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteJDBCLoader;
import org.sqlite.SQLiteOpenMode;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.FileUtil;
import com.revolsys.io.file.Paths;
import com.revolsys.jdbc.io.AbstractJdbcDatabaseFactory;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.FileRecordStoreFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.format.OutputStreamRecordWriter;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;
import com.revolsys.util.RsCoreDataTypes;

/**
 * jdbc:sqlite:[file]
 */
public class GeoPackage extends AbstractJdbcDatabaseFactory
  implements RecordReaderFactory, RecordWriterFactory, FileRecordStoreFactory {
  private static final boolean AVAILABLE;

  private static final List<FieldDefinition> CONNECTION_FIELD_DEFINITIONS = Arrays.asList( //
    new FieldDefinition("file", RsCoreDataTypes.FILE, 50, true) //
  );

  public static final String DESCRIPTION = "GeoPackage Database";

  public static final String FILE_EXTENSION = "gpkg";

  private static final List<String> FILE_EXTENSIONS = Collections.singletonList(FILE_EXTENSION);

  public static final String JDBC_PREFIX = "jdbc:sqlite:";

  public static final String MIME_TYPE = "application/geopackage+vnd.sqlite3";

  private static final List<Pattern> URL_PATTERNS = Arrays.asList(Pattern.compile("jdbc:sqlite:.+"),
    Pattern.compile("[^(file:)].+\\.gpkg"), Pattern.compile("file:(/(//)?)?.+\\.gpkg"),
    Pattern.compile("folderconnection:/(//)?.*.gpkg"));

  static {
    boolean available = true;
    try {
      SQLiteJDBCLoader.initialize();
    } catch (final Throwable e) {
      available = false;
    }
    AVAILABLE = available;
  }

  public static GeoPackageRecordStore createRecordStore(final Object source) {
    return createRecordStore(source, MapEx.EMPTY);
  }

  public static GeoPackageRecordStore createRecordStore(final Object source,
    final MapEx properties) {
    final GeoPackageRecordStore recordStore = openRecordStore(source);
    if (recordStore != null) {
      recordStore.setCreateMissingRecordStore(true);
      recordStore.setCreateMissingTables(true);
      recordStore.initialize();
    }
    return recordStore;
  }

  public static GeoPackageRecordStore openRecordStore(final Object source) {
    return openRecordStore(source, MapEx.EMPTY);
  }

  public static GeoPackageRecordStore openRecordStore(final Object source, final MapEx properties) {
    if (source == null) {
      return null;
    } else {
      final Resource resource = Resource.getResource(source);
      final String fileName = resource.getOrDownloadFile().toPath().toAbsolutePath().toString();
      final MapEx properties2 = new LinkedHashMapEx().add("url", JDBC_PREFIX + fileName);
      return new GeoPackage().newRecordStore(properties2);
    }
  }

  @Override
  public boolean canOpenPath(final Path path) {
    if (isAvailable()) {
      final String fileNameExtension = Paths.getFileNameExtension(path);
      return getRecordStoreFileExtensions().contains(fileNameExtension);
    } else {
      return false;
    }
  }

  @Override
  public boolean canOpenUrl(final String url) {
    if (isAvailable()) {
      for (final Pattern pattern : getUrlPatterns()) {
        if (pattern.matcher(url).matches()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public List<FieldDefinition> getConnectionFieldDefinitions() {
    return CONNECTION_FIELD_DEFINITIONS;
  }

  @Override
  public String getConnectionValidationQuery() {
    return null;
  }

  @Override
  public String getDriverClassName() {
    return "org.sqlite.JDBC";
  }

  @Override
  public String getFileExtension(final String mediaType) {
    if (MIME_TYPE.equals(mediaType) || FILE_EXTENSION.equals(mediaType)) {
      return FILE_EXTENSION;
    } else {
      return null;
    }
  }

  @Override
  public List<String> getFileExtensions() {
    return FILE_EXTENSIONS;
  }

  @Override
  public String getMediaType(final String fileExtension) {
    if (fileExtension.equals(fileExtension)) {
      return MIME_TYPE;
    } else {
      return null;
    }
  }

  @Override
  public Set<String> getMediaTypes() {
    return Collections.singleton(MIME_TYPE);
  }

  @Override
  public String getName() {
    return DESCRIPTION;
  }

  @Override
  public String getProductName() {
    return "GeoPackage";
  }

  @Override
  public List<String> getRecordStoreFileExtensions() {
    return FILE_EXTENSIONS;
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return JdbcRecordStore.class;
  }

  @Override
  public List<Pattern> getUrlPatterns() {
    return URL_PATTERNS;
  }

  @Override
  public String getVendorName() {
    return "sqlite";
  }

  @Override
  public boolean isAvailable() {
    return AVAILABLE;
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public DataSource newDataSource(final Map<String, ? extends Object> config) {
    final MapEx newConfig = new LinkedHashMapEx(config);
    String url = (String)newConfig.remove("url");
    if (!Property.hasValue(url)) {
      throw new IllegalArgumentException("jdbc url required");
    }

    if (!url.startsWith("jdbc")) {
      try {
        final UrlResource resource = new UrlResource(url);
        final File file = resource.getFile();
        final String newUrl = JDBC_PREFIX + FileUtil.getCanonicalPath(file);
        url = newUrl;
      } catch (final Exception e) {
        throw new IllegalArgumentException(url + " must be a file", e);
      }
    }
    newConfig.put("enable_load_extension", true);

    try {
      // final String user = (String)newConfig.remove("user");
      // String password = (String)newConfig.remove("password");
      // if (Property.hasValue(password)) {
      // password = PasswordUtil.decrypt(password);
      // }
      final SQLiteConfig sqliteConfig = new SQLiteConfig();
      sqliteConfig.setBusyTimeout(60000);
      sqliteConfig.setLockingMode(LockingMode.NORMAL);
      sqliteConfig.setOpenMode(SQLiteOpenMode.FULLMUTEX);
      for (final Entry<String, Object> property : newConfig.entrySet()) {
        final String name = property.getKey();
        final Object value = property.getValue();
        try {
          Property.setSimple(sqliteConfig, name, value);
        } catch (final Throwable t) {
          Logs.debug(this,
            "Unable to set data source property " + name + " = " + value + " for " + url, t);
        }
      }

      final SQLiteDataSource dataSource = new SQLiteDataSource(sqliteConfig) {
        @Override
        public SQLiteConnection getConnection(final String username, final String password)
          throws SQLException {
          final SQLiteConnection connection = super.getConnection(username, password);
          connection.setAutoCommit(false);
          return connection;
        }
      };
      dataSource.setUrl(url);

      return dataSource;
    } catch (final Throwable e) {
      throw new IllegalArgumentException("Unable to create data source for " + config, e);
    }
  }

  @Override
  public RecordReader newRecordReader(final Resource resource,
    final RecordFactory<? extends Record> factory, final MapEx properties) {
    return new GeopackageFileRecordReader(resource, factory, properties);
  }

  @Override
  public GeoPackageRecordStore newRecordStore(final DataSource dataSource) {
    throw new UnsupportedOperationException(
      "GeoPackage record store cannot be created from a dataSource");
  }

  @Override
  public GeoPackageRecordStore newRecordStore(
    final Map<String, ? extends Object> connectionProperties) {
    return new GeoPackageRecordStore(this, connectionProperties);
  }

  @Override
  public RecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition,
    final Resource resource) {
    if (resource.isFile()) {
      final GeoPackageRecordStore recordStore = GeoPackage.createRecordStore(resource);
      if (recordStore == null) {
        return null;
      } else {
        return new GeoPackageRecordWriter(recordStore, recordDefinition);
      }
    } else {
      final OutputStream out = resource.newBufferedOutputStream();
      final String baseName = resource.getBaseName();
      return newRecordWriter(baseName, recordDefinition, out);
    }
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    return new OutputStreamRecordWriter(recordDefinition, baseName, GeoPackage.FILE_EXTENSION,
      outputStream);
  }

  @Override
  public Map<String, Object> parseUrl(final String url) {
    if (url != null && url.startsWith(JDBC_PREFIX)) {
      final Map<String, Object> parameters = new LinkedHashMap<>();
      final String fileName = url.substring(JDBC_PREFIX.length());
      parameters.put("recordStoreType", getName());
      parameters.put("file", fileName);
    }
    return Collections.emptyMap();
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public String toUrl(final Map<String, Object> urlParameters) {
    final StringBuilder url = new StringBuilder(JDBC_PREFIX);
    final String file = Maps.getString(urlParameters, "file");
    url.append(file);
    return url.toString().toLowerCase();
  }
}
