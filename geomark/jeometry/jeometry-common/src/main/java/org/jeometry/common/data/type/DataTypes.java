package org.jeometry.common.data.type;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.date.Dates;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;
import org.jeometry.common.net.UrlProxy;
import org.slf4j.LoggerFactory;

// TODO manage data types by classloader and allow unloading of registered classes.
public final class DataTypes {

  private static final Map<String, DataType> CLASS_TYPE_MAP = new HashMap<>();

  private static final Map<String, DataType> NAME_TYPE_MAP = new HashMap<>();

  public static final DataType ANY_URI = new FunctionDataType("anyURI", URI.class, value -> {
    try {
      if (value instanceof URL) {
        final URL url = (URL)value;
        return url.toURI();
      } else if (value instanceof UrlProxy) {
        final UrlProxy proxy = (UrlProxy)value;
        return proxy.getUri();
      } else if (value instanceof File) {
        final File file = (File)value;
        return file.toURI();
      } else if (value instanceof Path) {
        final Path path = (Path)value;
        return path.toUri();
      } else {
        final String string = DataTypes.toString(value);
        try {
          return new URI(string);
        } catch (final URISyntaxException e) {
          throw new IllegalArgumentException("Unknown URI: " + string, e);
        }
      }
    } catch (final Throwable e) {
      throw Exceptions.wrap(e);
    }
  });

  public static final DataType BASE64_BINARY = new SimpleDataType("base64Binary", byte[].class);

  public static final DataType BIG_INTEGER = new BigIntegerDataType();

  public static final DataType BLOB = new SimpleDataType("blob", Blob.class);

  public static final DataType BOOLEAN = new FunctionDataType("boolean", Boolean.class, value -> {
    if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      final String string = DataTypes.toString(value);
      if ("1".equals(string)) {
        return true;
      } else if ("Y".equalsIgnoreCase(string)) {
        return true;
      } else if ("on".equals(string)) {
        return true;
      } else if ("true".equalsIgnoreCase(string)) {
        return true;
      } else if ("0".equals(string)) {
        return false;
      } else if ("N".equalsIgnoreCase(string)) {
        return false;
      } else if ("off".equals(string)) {
        return false;
      } else if ("false".equalsIgnoreCase(string)) {
        return false;
      } else {
        throw new IllegalArgumentException(string + " is not a valid boolean");
      }
    }
  });

  public static final DataType BYTE = new ByteDataType();

  public static final ClobDataType CLOB = new ClobDataType();

  public static final DataType CODE = new CodeDataType();

  public static final DataType COLOR = new FunctionDataType("color", Color.class,
    value -> WebColors.toColor(value), WebColors::toString);

  public static final DataType UTIL_DATE = new FunctionDataType("utilDate", java.util.Date.class,
    value -> Dates.getDate(value), Dates::toDateTimeIsoString, Dates::equalsNotNull);

  public static final DataType DATE_TIME = new FunctionDataType("dateTime", Timestamp.class,
    value -> Dates.getTimestamp(value), Dates::toTimestampIsoString, Dates::equalsNotNull);

  public static final DataType DECIMAL = new BigDecimalDataType();

  public static final DataType DOUBLE = new DoubleDataType();

  public static final DataType FLOAT = new FloatDataType();

  public static final DataType IDENTIFIER = new FunctionDataType("identifier", Identifier.class,
    Identifier::newIdentifier);

  public static final IntegerDataType INT = new IntegerDataType();

  public static final LongDataType LONG = new LongDataType();

  @SuppressWarnings({
    "rawtypes",
  })
  public static final DataType MAP = new FunctionDataType("Map", Map.class, value -> {
    if (value instanceof Map) {
      return (Map)value;
    } else {
      return value;
    }
  }, FunctionDataType.MAP_EQUALS, FunctionDataType.MAP_EQUALS_EXCLUDES);

  public static final DataType OBJECT = new ObjectDataType();

  public static final DataType PATH_NAME = new FunctionDataType("pathName", PathName.class,
    PathName::newPathName);

  public static final DataType QNAME = new SimpleDataType("QName", QName.class);

  public static final DataType SHORT = new ShortDataType();

  public static final DataType SQL_DATE = new FunctionDataType("date", java.sql.Date.class,
    value -> Dates.getSqlDate(value), Dates::toSqlDateString, Dates::equalsNotNull);

  public static final DataType STRING = new FunctionDataType("string", String.class,
    DataTypes::toString);

  public static final DataType DURATION = new SimpleDataType("duration", String.class);

  public static final DataType TIME = new SimpleDataType("time", Time.class);

  public static final DataType TIMESTAMP = new FunctionDataType("timestamp", Timestamp.class,
    value -> Dates.getTimestamp(value), Dates::toTimestampIsoString, Dates::equalsNotNull);

  public static final DataType INSTANT = new FunctionDataType("instant", Instant.class,
    value -> Dates.getInstant(value), Dates::toInstantIsoString, Object::equals);

  public static final DataType LOCAL_DATE = new FunctionDataType("localDate", LocalDate.class,
    value -> Dates.getLocalDate(value), Dates::toLocalDateIsoString, Object::equals);

  public static final DataType URL = new FunctionDataType("url", java.net.URL.class, value -> {
    if (value instanceof URL) {
      return (URL)value;
    } else if (value instanceof URI) {
      final URI uri = (URI)value;
      try {
        return uri.toURL();
      } catch (final MalformedURLException e) {
        throw new IllegalArgumentException("Cannot get url " + uri, e);
      }
    } else if (value instanceof UrlProxy) {
      final UrlProxy proxy = (UrlProxy)value;
      return proxy.getUrl();
    } else if (value instanceof File) {
      final File file = (File)value;
      try {
        final URI uri = file.toURI();
        return uri.toURL();
      } catch (final MalformedURLException e) {
        throw new IllegalArgumentException("Cannot get url " + file, e);
      }
    } else if (value instanceof Path) {
      final Path path = (Path)value;
      try {
        return path.toUri().toURL();
      } catch (final MalformedURLException e) {
        throw new IllegalArgumentException("Cannot get url " + path, e);
      }
    } else {
      final String string = DataTypes.toString(value);
      try {
        return new URL(string);
      } catch (final MalformedURLException e) {
        throw new IllegalArgumentException("Unknown URL", e);
      }
    }
  });

  public static final DataType UUID = new FunctionDataType("uuid", UUID.class, (value) -> {
    if (value instanceof UUID) {
      return (UUID)value;
    } else {
      return java.util.UUID.fromString(value.toString());
    }
  }, Object::toString);

  public static final DataType XML = new FunctionDataType("xml", String.class, Object::toString);

  public static final DataType COLLECTION = new CollectionDataType("Collection", Collection.class,
    OBJECT);

  public static final DataType LIST = new ListDataType(List.class, OBJECT);

  public static final DataType RELATION = new CollectionDataType("Relation", Collection.class,
    OBJECT);

  public static final DataType SET = new SetDataType(Set.class, OBJECT);

  static {
    registerDataTypes(DataTypes.class);

    register(Boolean.TYPE, BOOLEAN);
    register(Byte.TYPE, BYTE);
    register(Short.TYPE, SHORT);
    register(Integer.TYPE, INT);
    register(Long.TYPE, LONG);
    register(Float.TYPE, FLOAT);
    register(Double.TYPE, DOUBLE);
  }

  public static DataType getDataType(final Class<?> clazz) {
    if (clazz == null) {
      return DataTypes.OBJECT;
    } else {
      DataType dataType = CLASS_TYPE_MAP.get(clazz.getName());
      if (dataType == null) {
        final Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces != null) {
          for (final Class<?> inter : interfaces) {
            dataType = getDataType(inter);
            if (dataType != null && dataType != DataTypes.OBJECT) {
              return dataType;
            }
          }
        }
        return getDataType(clazz.getSuperclass());
      } else {
        return dataType;
      }
    }
  }

  public static DataType getDataType(final Object object) {
    if (object == null) {
      return DataTypes.OBJECT;
    } else if (object instanceof DataTypeProxy) {
      final DataTypeProxy proxy = (DataTypeProxy)object;
      return proxy.getDataType();
    } else if (object instanceof DataType) {
      final DataType type = (DataType)object;
      return type;
    } else {
      final Class<?> clazz = object.getClass();
      return getDataType(clazz);
    }
  }

  public static DataType getDataType(final String name) {
    if (name == null) {
      return DataTypes.OBJECT;
    } else {
      final DataType type = NAME_TYPE_MAP.get(name.toLowerCase());
      if (type == null) {
        return DataTypes.OBJECT;
      } else {
        return type;
      }
    }
  }

  public static DataType getDataType(final Type type) {
    if (type instanceof Class) {
      final Class<?> clazz = (Class<?>)type;
      return getDataType(clazz);
    } else {
      throw new IllegalArgumentException("Cannot get dataType for: " + type);
    }
  }

  public static void register(final Class<?> typeClass, final DataType type) {
    final String typeClassName = typeClass.getName();
    if (!CLASS_TYPE_MAP.containsKey(typeClassName)) {
      CLASS_TYPE_MAP.put(typeClassName, type);
    }
  }

  public static void register(final DataType type) {
    final String name = type.getName().toLowerCase();
    if (!NAME_TYPE_MAP.containsKey(name)) {
      NAME_TYPE_MAP.put(name, type);
    }
    final Class<?> typeClass = type.getJavaClass();
    register(typeClass, type);
  }

  public static void register(final String name, final Class<?> javaClass) {
    final DataType type = new SimpleDataType(name, javaClass);
    register(type);
  }

  /**
   * <p> Register the data types specified as public static fields (constants) on the registry class.</p>
   *
   * <pre>public static final DataType CUSTOM_DATA_TYPE = ...;</pre>
   *
   * @param registryClass The class containing the data type constants.
   */
  public static void registerDataTypes(final Class<?> registryClass) {
    final Field[] fields = registryClass.getDeclaredFields();
    for (final Field field : fields) {
      if (Modifier.isStatic(field.getModifiers())) {
        if (DataType.class.isAssignableFrom(field.getType())) {
          try {
            final DataType type = (DataType)field.get(null);
            register(type);
          } catch (final Throwable e) {
            LoggerFactory.getLogger(registryClass)
              .error("Error registering type " + field.getName(), e);
          }
        }
      }
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public static <V> V toObject(final Class<?> clazz, final Object value) {
    // TODO enum
    if (clazz == null) {
      return (V)value;
    } else if (value == null) {
      return null;
    } else if (clazz.isAssignableFrom(value.getClass())) {
      return (V)value;
    } else {
      if (clazz.isEnum()) {
        try {
          return (V)Enum.valueOf((Class<Enum>)clazz, value.toString());
        } catch (final Throwable e) {
        }
      }
      final DataType dataType = getDataType(clazz);
      if (dataType == null) {
        return (V)value;
      } else {
        return dataType.toObject(value);
      }
    }
  }

  public static String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof String) {
      return (String)value;
    } else {
      final Class<?> valueClass = value.getClass();
      final DataType dataType = getDataType(valueClass);
      if (dataType == null) {
        return value.toString();
      } else {
        return dataType.toString(value);
      }
    }
  }

  private DataTypes() {
  }
}
