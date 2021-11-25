package com.revolsys.record.io.format.json;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class JsonSchemaWriter {
  private final JsonMapWriter writer;

  public JsonSchemaWriter(final Resource resource) {
    final Writer out = resource.newWriter();
    this.writer = new JsonMapWriter(out, true);
    this.writer.setListRoot(true);
  }

  @PreDestroy
  public void close() {
    this.writer.close();
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  private Collection<Object> toJsonList(final Collection<?> collection) {
    final List<Object> list = new ArrayList<>();
    for (final Object object : collection) {
      final Object jsonValue = toJsonValue(object);
      list.add(jsonValue);
    }
    return list;
  }

  public Map<String, Object> toJsonMap(final Map<String, ?> map) {
    final Map<String, Object> jsonMap = new LinkedHashMap<>();
    for (final Entry<String, ?> entry : map.entrySet()) {
      final String name = entry.getKey();
      final Object value = entry.getValue();
      final Object jsonValue = toJsonValue(value);
      if (jsonValue != null) {
        jsonMap.put(name, jsonValue);
      }
    }
    return jsonMap;
  }

  public Object toJsonValue(Object value) {
    Object jsonValue = null;
    if (value == null) {
      jsonValue = null;
    } else if (value instanceof Number) {
      jsonValue = value;
    } else if (value instanceof Boolean) {
      jsonValue = value;
    } else if (value instanceof CharSequence) {
      jsonValue = value;
    } else if (value instanceof Map) {
      final Map<String, Object> objectMap = (Map<String, Object>)value;
      value = toJsonMap(objectMap);
    } else if (value instanceof Collection) {
      final Collection<?> collection = (Collection<?>)value;
      value = toJsonList(collection);
    } else {
      jsonValue = DataTypes.toString(value);
    }
    return jsonValue;
  }

  public void write(final RecordDefinition recordDefinition) {
    final Map<String, Object> recordDefinitionMap = new LinkedHashMap<>();
    recordDefinitionMap.put("name", recordDefinition.getPath());

    final List<Map<String, Object>> fields = new ArrayList<>();
    recordDefinitionMap.put("fields", fields);
    for (final FieldDefinition attribute : recordDefinition.getFields()) {
      final Map<String, Object> field = new LinkedHashMap<>();
      final String name = attribute.getName();
      field.put("name", name);
      final DataType dataType = attribute.getDataType();
      final String dataTypeName = dataType.getName();
      field.put("type", dataTypeName);
      final int length = attribute.getLength();
      if (length > 0) {
        field.put("length", length);
      }
      final int scale = attribute.getScale();
      if (scale > 0) {
        field.put("scale", scale);
      }
      final boolean required = attribute.isRequired();
      field.put("required", required);
      final Map<String, ?> attributeProperties = attribute.getProperties();
      final Map<String, Object> fieldProperties = toJsonMap(attributeProperties);
      if (!fieldProperties.isEmpty()) {
        field.put("properties", fieldProperties);
      }
      fields.add(field);
    }

    this.writer.write(recordDefinitionMap);
  }
}
