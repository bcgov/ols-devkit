package com.revolsys.oracle.recordstore;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.Maps;
import com.revolsys.jdbc.io.AbstractJdbcDatabaseFactory;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

/**
 * jdbc:oracle:thin:@//[host]:[port]/[ServiceName]
 * jdbc:oracle:thin:@[host]:[port]:[sid] jdbc:oracle:oci:@[tnsname]
 * jdbc:oracle:thin
 * :@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCPS)(HOST=[host])(PORT=[port
 * ]))(CONNECT_DATA=(SERVICE_NAME=[service])))
 */
public class Oracle extends AbstractJdbcDatabaseFactory {
  private static final String REGEX_NAME = "[a-zA-Z0-9_\\$#\\.\\-]+";

  private static final String REGEX_URL_PREFIX_USER_PASSWORD = "jdbc:oracle:(?:thin|oci):" //
    + "(" + REGEX_NAME + ")?" // Optional user name
    + "(?:/([^@]+))?" // Optional password
    + "@";

  private static final Pattern URL_TNS_PATTERN = Pattern.compile(REGEX_URL_PREFIX_USER_PASSWORD + //
    "(" + REGEX_NAME + ")" // TNS Name
  );

  private static final Pattern URL_HOST_PATTERN = Pattern
    .compile(REGEX_URL_PREFIX_USER_PASSWORD + "(?://)?" //
      + "([a-zA-Z-0-9][a-zA-Z-0-9\\.\\-]*)" // Host
      + "(?::(\\d+))?" // Optional Port Number
      + "[/:]" // Separator
      + "(" + REGEX_NAME + "+)" // SID or ArcGisRestService Name
    );

  private static final List<FieldDefinition> CONNECTION_FIELD_DEFINITIONS = Arrays.asList( //
    new FieldDefinition("host", DataTypes.STRING, 50, true) //
      .setDefaultValue("localhost")
      //
      .addProperty(URL_FIELD, true), //
    new FieldDefinition("port", DataTypes.INT, false) //
      .setMinValue(0)
      //
      .setMaxValue(65535)
      //
      .setDefaultValue(1521)
      //
      .addProperty(URL_FIELD, true), //
    new FieldDefinition("database", DataTypes.STRING, 64, true) //
      .addProperty(URL_FIELD, true), //
    new FieldDefinition("user", DataTypes.STRING, 30, false), //
    new FieldDefinition("password", DataTypes.STRING, 30, false) //
  );

  public static List<String> getTnsConnectionNames() {
    File tnsFile = new File(System.getProperty("oracle.net.tns_admin"), "tnsnames.ora");
    if (!tnsFile.exists()) {
      final String tnsAdmin = System.getenv("TNS_ADMIN");
      if (tnsAdmin != null) {
        tnsFile = new File(tnsAdmin, "tnsnames.ora");
      }
      if (!tnsFile.exists()) {
        final String oracleHome = System.getenv("ORACLE_HOME");
        if (oracleHome != null) {
          tnsFile = new File(oracleHome + "/network/admin", "tnsnames.ora");
        }
        if (!tnsFile.exists()) {
          if (oracleHome != null) {
            tnsFile = new File(oracleHome + "/NETWORK/ADMIN", "tnsnames.ora");
          }
        }
      }
    }
    if (tnsFile.exists()) {
      try {
        final FileReader reader = new FileReader(tnsFile);
        final Class<?> parserClass = Class.forName("oracle.net.jdbc.nl.NLParamParser");
        final Constructor<?> constructor = parserClass.getConstructor(Reader.class);
        final Object parser = constructor.newInstance(reader);
        final Method method = parserClass.getMethod("getNLPAllNames");
        final List<String> names = new ArrayList<>();
        for (final String name : (String[])method.invoke(parser)) {
          names.add(name.toLowerCase());
        }
        return names;
      } catch (final NoSuchMethodException e) {
      } catch (final ClassNotFoundException e) {
      } catch (final InvocationTargetException e) {
        Logs.debug(Oracle.class, "Error reading: " + tnsFile, e.getCause());
      } catch (final Throwable e) {
        Logs.debug(Oracle.class, "Error reading: " + tnsFile, e.getCause());
      }
    }
    return Collections.emptyList();
  }

  protected void addCacheProperty(final Map<String, Object> config, final String key,
    final Properties cacheProperties, final String propertyName, final Object defaultValue,
    final DataType dataType) {
    Object value = config.remove(key);
    if (value == null) {
      value = config.get(propertyName);
    }
    cacheProperties.put(propertyName, String.valueOf(defaultValue));
    if (value != null) {
      try {
        final Object value1 = value;
        final Object propertyValue = dataType.toObject(value1);
        final String stringValue = String.valueOf(propertyValue);
        cacheProperties.put(propertyName, stringValue);
      } catch (final Throwable e) {
      }
    }
  }

  @Override
  public List<FieldDefinition> getConnectionFieldDefinitions() {
    return CONNECTION_FIELD_DEFINITIONS;
  }

  @Override
  public Map<String, String> getConnectionUrlMap() {
    final Map<String, String> connectionMap = new TreeMap<>();
    for (final String connectionName : getTnsConnectionNames()) {
      final String connectionUrl = "jdbc:oracle:thin:@" + connectionName;
      connectionMap.put(connectionName, connectionUrl);
    }
    return connectionMap;
  }

  @Override
  public String getConnectionValidationQuery() {
    return "SELECT 1 FROM DUAL";
  }

  @Override
  public String getDriverClassName() {
    return "oracle.jdbc.OracleDriver";
  }

  @Override
  public String getName() {
    return "Oracle Database";
  }

  @Override
  public String getProductName() {
    return "Oracle";
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return JdbcRecordStore.class;
  }

  @Override
  public String getVendorName() {
    return "oracle";
  }

  @Override
  public JdbcRecordStore newRecordStore(final DataSource dataSource) {
    return new OracleRecordStore(dataSource);
  }

  @Override
  public JdbcRecordStore newRecordStore(final Map<String, ? extends Object> connectionProperties) {
    return new OracleRecordStore(this, connectionProperties);
  }

  @Override
  public Map<String, Object> parseUrl(final String url) {
    if (url != null && url.startsWith("jdbc:oracle")) {
      final Matcher hostMatcher = URL_HOST_PATTERN.matcher(url);
      final Map<String, Object> parameters = new LinkedHashMap<>();
      if (hostMatcher.matches()) {
        parameters.put("recordStoreType", getName());
        final String user = hostMatcher.group(1);
        if (Property.hasValue(user)) {
          parameters.put("user", user.toLowerCase());
        }
        final String password = hostMatcher.group(2);
        if (Property.hasValue(password)) {
          parameters.put("password", password);
        }
        final String host = hostMatcher.group(3);
        parameters.put("host", host.toLowerCase());
        final String port = hostMatcher.group(4);
        parameters.put("port", port);
        final String database = hostMatcher.group(5);
        parameters.put("database", Strings.lowerCase(database));
        parameters.put("namedConnection", null);
        return parameters;
      }
      final Matcher tnsmatcher = URL_TNS_PATTERN.matcher(url);
      if (tnsmatcher.matches()) {
        parameters.put("recordStoreType", getProductName());
        final String user = tnsmatcher.group(1);
        if (Property.hasValue(user)) {
          parameters.put("user", user.toLowerCase());
        }
        final String password = tnsmatcher.group(2);
        if (Property.hasValue(password)) {
          parameters.put("password", password);
        }
        parameters.put("host", null);
        parameters.put("port", null);
        final String tnsname = tnsmatcher.group(3).toLowerCase();
        if (getTnsConnectionNames().contains(tnsname)) {
          parameters.put("namedConnection", tnsname);
          parameters.put("database", null);
        } else {
          parameters.put("database", tnsname);
          parameters.put("namedConnection", null);
        }
        return parameters;
      }
    }
    return Collections.emptyMap();
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public String toUrl(final Map<String, Object> urlParameters) {
    final StringBuilder url = new StringBuilder("jdbc:oracle:thin:@");
    final String namedConnection = Maps.getString(urlParameters, "namedConnection");
    if (Property.hasValue(namedConnection)) {
      url.append(namedConnection.toLowerCase());
    } else {
      final String host = Maps.getString(urlParameters, "host");
      final Integer port = Maps.getInteger(urlParameters, "port");
      final String database = Maps.getString(urlParameters, "database");

      final boolean hasHost = Property.hasValue(host);
      final boolean hasPort = port != null;
      if (hasHost || hasPort) {
        url.append("//");
        if (hasHost) {
          url.append(host);
        }
        if (hasPort) {
          url.append(':');
          url.append(port);
        }
        url.append('/');
      }
      if (Property.hasValue(database)) {
        url.append(database);
      }
    }
    return url.toString().toLowerCase();
  }
}
