package com.revolsys.record.io.format.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.jeometry.common.data.type.AbstractDataType;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.file.Paths;
import com.revolsys.io.map.MapReader;
import com.revolsys.io.map.MapReaderFactory;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public class Json extends AbstractIoFactory
  implements MapReaderFactory, MapWriterFactory, RecordWriterFactory {
  private static class JsonDataType extends AbstractDataType {

    public JsonDataType() {
      super("JsonType", JsonType.class, true);
    }

    @Override
    public boolean equalsNotNull(final Object object1, final Object object2) {
      return object1.equals(object2);
    }

    @Override
    protected boolean equalsNotNull(final Object object1, final Object object2,
      final Collection<? extends CharSequence> exclude) {
      final JsonType json = (JsonType)object1;
      return json.equals(object2, exclude);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object toObjectDo(final Object value) {
      if (value instanceof JsonType) {
        return value;
      } else if (value instanceof Jsonable) {
        return ((Jsonable)value).asJson();
      } else if (value instanceof Map) {
        return new JsonObjectHash((Map<? extends String, ? extends Object>)value);
      } else if (value instanceof List) {
        return JsonList.array((List<?>)value);
      } else if (value instanceof String) {
        final Object read = JsonParser.read((String)value);
        if (read instanceof JsonType) {
          return read;
        } else {
          return value;
        }
      } else {
        return value;
      }
    }

    @Override
    protected String toStringDo(final Object value) {
      if (value instanceof Jsonable) {
        final Jsonable json = (Jsonable)value;
        return json.toJsonString(true);
      } else {
        return Json.toString(value);
      }
    }
  }

  private static class JsonListDataType extends AbstractDataType {

    public JsonListDataType() {
      super("JsonList", JsonList.class, true);
    }

    @Override
    public boolean equalsNotNull(final Object object1, final Object object2) {
      return object1.equals(object2);
    }

    @Override
    protected boolean equalsNotNull(final Object object1, final Object object2,
      final Collection<? extends CharSequence> exclude) {
      final JsonList list1 = (JsonList)object1;
      return list1.equals(object2);
    }

    @Override
    protected Object toObjectDo(final Object value) {
      if (value instanceof JsonList) {
        return value;
      } else if (value instanceof Jsonable) {
        return ((Jsonable)value).asJson();
      } else if (value instanceof Collection<?>) {
        return JsonList.array((Collection<?>)value);
      } else {
        final Object json = JsonParser.read(value);
        if (json instanceof JsonList) {
          return json;
        } else {
          return JsonList.array(json);
        }
      }
    }

    @Override
    protected String toStringDo(final Object value) {
      if (value instanceof JsonList) {
        return ((JsonList)value).toJsonString();
      } else if (value instanceof List<?>) {
        return Json.toString(value);
      } else if (value == null) {
        return null;
      } else {
        return value.toString();
      }
    }
  }

  private static class JsonObjectDataType extends AbstractDataType {

    public JsonObjectDataType(final String name, final Class<?> javaClass) {
      super(name, javaClass, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equalsNotNull(final Object object1, final Object object2) {
      final Map<Object, Object> map1 = (Map<Object, Object>)object1;
      final Map<Object, Object> map2 = (Map<Object, Object>)object2;
      if (map1.size() == map2.size()) {
        final Set<Object> keys1 = map1.keySet();
        final Set<Object> keys2 = map2.keySet();
        if (keys1.equals(keys2)) {
          for (final Object key : keys1) {
            final Object value1 = map1.get(key);
            final Object value2 = map2.get(key);
            if (!DataType.equal(value1, value2)) {
              return false;
            }
          }
        }
        return true;
      } else {
        return false;
      }
    }

    @SuppressWarnings({
      "unchecked"
    })
    @Override
    protected boolean equalsNotNull(final Object object1, final Object object2,
      final Collection<? extends CharSequence> exclude) {
      final Map<Object, Object> map1 = (Map<Object, Object>)object1;
      final Map<Object, Object> map2 = (Map<Object, Object>)object2;
      final Set<Object> keys = new TreeSet<>();
      keys.addAll(map1.keySet());
      keys.addAll(map2.keySet());
      keys.removeAll(exclude);

      for (final Object key : keys) {
        final Object value1 = map1.get(key);
        final Object value2 = map2.get(key);
        if (!DataType.equal(value1, value2, exclude)) {
          return false;
        }
      }
      return true;
    }

    protected JsonObject toJsonObject(final Map<? extends String, ? extends Object> map) {
      return new JsonObjectHash(map);
    }

    @SuppressWarnings({
      "unchecked"
    })
    @Override
    protected Object toObjectDo(final Object value) {
      if (value instanceof JsonObject) {
        return toJsonObject((JsonObject)value);
      } else if (value instanceof Jsonable) {
        return ((Jsonable)value).asJson();
      } else if (value instanceof Map) {
        final Map<? extends String, ? extends Object> map = (Map<? extends String, ? extends Object>)value;
        return toJsonObject(map);
      } else if (value instanceof String) {
        final JsonObject map = Json.toObjectMap((String)value);
        if (map == null) {
          return null;
        } else {
          return toJsonObject(map);
        }
      } else {
        return toJsonObject(JsonParser.read(value));
      }
    }

    @SuppressWarnings({
      "unchecked"
    })
    @Override
    protected String toStringDo(final Object value) {
      if (value instanceof Jsonable) {
        return ((Jsonable)value).toJsonString();
      } else if (value instanceof Map) {
        final Map<? extends String, ? extends Object> map = (Map<? extends String, ? extends Object>)value;
        return Json.toString(map);
      } else if (value == null) {
        return null;
      } else {
        return value.toString();
      }
    }
  }

  private static class JsonObjectTreeDataType extends JsonObjectDataType {
    public JsonObjectTreeDataType() {
      super("JsonObjectTree", JsonObjectTree.class);
    }

    @Override
    protected JsonObject toJsonObject(final Map<? extends String, ? extends Object> map) {
      if (map instanceof JsonObjectTree) {
        return (JsonObjectTree)map;
      } else {
        return new JsonObjectTree(map);
      }
    }
  }

  public static final String FILE_EXTENSION = "json";

  public static final String MIME_TYPE = "application/json";

  public static final DataType JSON_OBJECT = new JsonObjectDataType("JsonObject", JsonObject.class);

  public static final DataType JSON_OBJECT_TREE = new JsonObjectTreeDataType();

  public static final DataType JSON_TYPE = new JsonDataType();

  public static DataType JSON_LIST = new JsonListDataType();

  static {
    DataTypes.registerDataTypes(Json.class);
  }

  public static JsonObject clone(final JsonObject object) {
    if (object == null) {
      return null;
    } else {
      return object.clone();
    }
  }

  public static Map<String, Object> getMap(final Map<String, Object> record,
    final String fieldName) {
    final String value = (String)record.get(fieldName);
    return toObjectMap(value);
  }

  public static JsonObject toMap(final File directory, final String path) {
    if (directory == null || path == null) {
      return new JsonObjectHash();
    } else {
      final File file = FileUtil.getFile(directory, path);
      if (file.exists() && !file.isDirectory()) {
        final PathResource resource = new PathResource(file);
        return toMap(resource);
      } else {
        return new JsonObjectHash();
      }
    }
  }

  public static JsonObject toMap(final Object source) {
    final Resource resource = Resource.getResource(source);
    return toMap(resource);
  }

  public static JsonObject toMap(final Path directory, final String path) {
    if (directory == null || path == null) {
      return new JsonObjectHash();
    } else {
      final Path file = directory.resolve(path);
      if (Paths.exists(file) && !Files.isDirectory(file)) {
        final PathResource resource = new PathResource(file);
        return toMap(resource);
      } else {
        return new JsonObjectHash();
      }
    }
  }

  public static JsonObject toMap(final Reader in) {
    try (
      Reader inClosable = in;
      final JsonMapIterator iterator = new JsonMapIterator(in, true)) {
      if (iterator.hasNext()) {
        return iterator.next();
      } else {
        return null;
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read JSON map", e);
    }
  }

  public static final JsonObject toMap(final Resource resource) {
    if (resource != null && (!(resource instanceof PathResource) || resource.exists())) {
      final Reader reader = resource.newBufferedReader();
      return toMap(reader);
    }
    return new JsonObjectHash();
  }

  public static Map<String, String> toMap(final String string) {
    final JsonObject map = toObjectMap(string);
    if (map.isEmpty()) {
      return new LinkedHashMap<>();
    } else {
      final Map<String, String> stringMap = new LinkedHashMap<>();
      for (final Entry<String, Object> entry : map.entrySet()) {
        final String key = entry.getKey();
        final Object value = entry.getValue();
        if (value == null) {
          stringMap.put(key, null);
        } else {
          stringMap.put(key, value.toString());
        }
      }
      return stringMap;
    }
  }

  public static final List<JsonObject> toMapList(final Object source) {
    final Resource resource = Resource.getResource(source);
    if (resource != null && (!(resource instanceof PathResource) || resource.exists())) {
      try (
        final BufferedReader in = resource.newBufferedReader();
        final JsonObjectReader jsonReader = new JsonObjectReader(in)) {
        return jsonReader.toList();
      } catch (final IOException e) {
        Exceptions.throwUncheckedException(e);
      }
    }
    return new ArrayList<>();
  }

  public static List<JsonObject> toMapList(final String string) {
    final StringReader in = new StringReader(string);
    try (
      final JsonObjectReader reader = new JsonObjectReader(in)) {
      return reader.toList();
    }
  }

  public static JsonObject toObjectMap(final String string) {
    if (Property.hasValue(string)) {
      final StringReader in = new StringReader(string);
      try (
        final JsonObjectReader reader = new JsonObjectReader(in, true)) {
        for (final JsonObject object : reader) {
          return object;
        }
      }
    }
    return new JsonObjectHash();
  }

  public static final Record toRecord(final RecordDefinition recordDefinition,
    final String string) {
    final StringReader in = new StringReader(string);
    final JsonRecordIterator iterator = new JsonRecordIterator(recordDefinition, in, true);
    try {
      if (iterator.hasNext()) {
        return iterator.next();
      } else {
        return null;
      }
    } finally {
      iterator.close();
    }
  }

  public static List<Record> toRecordList(final RecordDefinition recordDefinition,
    final String string) {
    final StringReader in = new StringReader(string);
    final JsonRecordIterator iterator = new JsonRecordIterator(recordDefinition, in);
    try {
      final List<Record> objects = new ArrayList<>();
      while (iterator.hasNext()) {
        final Record object = iterator.next();
        objects.add(object);
      }
      return objects;
    } finally {
      iterator.close();
    }
  }

  public static String toString(final List<? extends Map<String, Object>> list) {
    return toString(list, false);
  }

  public static String toString(final List<? extends Map<String, Object>> list,
    final boolean indent) {
    final StringWriter writer = new StringWriter();
    final JsonMapWriter mapWriter = new JsonMapWriter(writer, indent);
    for (final Map<String, Object> map : list) {
      mapWriter.write(map);
    }
    mapWriter.close();
    return writer.toString();
  }

  public static String toString(final Map<String, ? extends Object> values) {
    final StringWriter writer = new StringWriter();
    try (
      final JsonWriter jsonWriter = new JsonWriter(writer, false)) {
      jsonWriter.write(values);
    }
    return writer.toString();
  }

  public static String toString(final Map<String, ? extends Object> values, final boolean indent) {
    final StringWriter writer = new StringWriter();
    try (
      final JsonWriter jsonWriter = new JsonWriter(writer, indent)) {
      jsonWriter.write(values);
    }
    return writer.toString();
  }

  public static String toString(final Object value) {
    return toString(value, true);
  }

  public static String toString(final Object value, final boolean indent) {
    final StringWriter stringWriter = new StringWriter();
    try (
      JsonWriter jsonWriter = new JsonWriter(stringWriter, indent)) {
      jsonWriter.value(value);
    }
    return stringWriter.toString();
  }

  public static final String toString(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    final StringWriter writer = new StringWriter();
    final JsonRecordWriter recordWriter = new JsonRecordWriter(recordDefinition, writer);
    recordWriter.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, Boolean.TRUE);
    recordWriter.write(object);
    recordWriter.close();
    return writer.toString();
  }

  public static String toString(final RecordDefinition recordDefinition,
    final List<? extends Map<String, Object>> list) {
    final StringWriter writer = new StringWriter();
    final JsonRecordWriter recordWriter = new JsonRecordWriter(recordDefinition, writer);
    for (final Map<String, Object> map : list) {
      final Record object = new ArrayRecord(recordDefinition);
      object.setValues(map);
      recordWriter.write(object);
    }
    recordWriter.close();
    return writer.toString();
  }

  public static String toString(final RecordDefinition recordDefinition,
    final Map<String, ? extends Object> parameters) {
    final Record object = new ArrayRecord(recordDefinition);
    object.setValues(parameters);
    return toString(object);
  }

  public static void writeMap(final Map<String, ? extends Object> object, final Object target) {
    writeMap(object, target, true);
  }

  public static void writeMap(final Map<String, ? extends Object> object, final Object target,
    final boolean indent) {
    final Resource resource = Resource.getResource(target);
    try (
      final Writer writer = resource.newWriter()) {
      writeMap(writer, object, indent);
    } catch (final IOException e) {
    }
  }

  public static void writeMap(final Writer writer, final Map<String, ? extends Object> object) {
    writeMap(writer, object, true);
  }

  public static void writeMap(final Writer writer, final Map<String, ? extends Object> object,
    final boolean indent) {
    try (
      final JsonMapWriter out = new JsonMapWriter(writer, indent)) {
      out.setSingleObject(true);
      out.write(object);
    } catch (final RuntimeException | Error e) {
      throw e;
    }
  }

  public Json() {
    super("JSON");
    addMediaTypeAndFileExtension(MIME_TYPE, FILE_EXTENSION);
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public MapReader newMapReader(final Resource resource) {
    return new JsonMapReader(resource.getInputStream());
  }

  @Override
  public MapWriter newMapWriter(final OutputStream out, final Charset charset) {
    return newMapWriter(out);
  }

  @Override
  public MapWriter newMapWriter(final Writer out) {
    return new JsonMapWriter(out);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.newUtf8Writer(outputStream);
    return new JsonRecordWriter(recordDefinition, writer);
  }
}
