package com.revolsys.record.io.format.xml;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.NameProxy;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.record.Record;
import com.revolsys.record.property.RecordProperties;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class XmlRecordWriter extends AbstractRecordWriter {

  private boolean opened;

  private XmlWriter out;

  private boolean singleObject;

  boolean startAttribute;

  public XmlRecordWriter(final RecordDefinitionProxy recordDefinition, final java.io.Writer out) {
    super(recordDefinition);
    if (out instanceof XmlWriter) {
      this.out = (XmlWriter)out;
    } else {
      this.out = new XmlWriter(out);
    }
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    if (this.out != null) {
      try {
        if (this.opened) {
          if (!this.singleObject) {
            this.out.endTag();
          }
          this.out.endDocument();
        }
      } finally {
        FileUtil.closeSilent(this.out);
        this.out = null;
      }
    }
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  @SuppressWarnings("unchecked")
  private void list(final List<? extends Object> list) {
    for (final Object value : list) {
      if (value instanceof Map) {
        final Map<String, ?> map = (Map<String, ?>)value;
        map(map);
      } else if (value instanceof List) {
        final List<?> subList = (List<?>)value;
        list(subList);
      } else {
        this.out.startTag(new QName("item"));
        this.out.text(value);
        this.out.endTag();
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void map(final Map<String, ? extends Object> values) {
    if (values instanceof NameProxy) {
      final NameProxy namedObject = (NameProxy)values;
      this.out.startTag(new QName(namedObject.getName()));
    } else {
      this.out.startTag(new QName("item"));
    }

    for (final Entry<String, ? extends Object> field : values.entrySet()) {
      final Object key = field.getKey();
      final Object value = field.getValue();
      final QName tagName = new QName(key.toString());
      if (value instanceof Map) {
        final Map<String, ?> map = (Map<String, ?>)value;
        this.out.startTag(tagName);
        map(map);
        this.out.endTag();
      } else if (value instanceof List) {
        final List<?> list = (List<?>)value;
        this.out.startTag(tagName);
        list(list);
        this.out.endTag();
      } else {
        this.out.nillableElement(tagName, value);
      }
    }
    this.out.endTag();
  }

  @Override
  public String toString() {
    return getPathName().toString();
  }

  @Override
  public void write(final Record record) {
    if (!this.opened) {
      writeHeader();
    }
    final RecordDefinition recordDefinition = getRecordDefinition();
    QName qualifiedName = recordDefinition.getProperty(RecordProperties.QUALIFIED_NAME);
    if (qualifiedName == null) {
      qualifiedName = new QName(recordDefinition.getName());
    }

    this.out.startTag(qualifiedName);

    for (final FieldDefinition field : getFieldDefinitions()) {
      final int fieldIndex = field.getIndex();
      final Object value;
      if (isWriteCodeValues()) {
        value = record.getCodeValue(fieldIndex);
      } else {
        value = record.getValue(fieldIndex);
      }
      if (isValueWritable(value)) {
        final String name = field.getName();
        final QName tagName = new QName(name);
        if (value instanceof Map) {
          @SuppressWarnings("unchecked")
          final Map<String, ?> map = (Map<String, ?>)value;
          this.out.startTag(tagName);
          map(map);
          this.out.endTag();
        } else if (value instanceof List) {
          final List<?> list = (List<?>)value;
          this.out.startTag(tagName);
          list(list);
          this.out.endTag();
        } else {
          final DataType dataType = field.getDataType();
          final String string = dataType.toString(value);
          this.out.nillableElement(tagName, string);
        }
      }
    }
    this.out.endTag();
  }

  private void writeHeader() {
    setIndent(isIndent());
    this.out.startDocument("UTF-8", "1.0");
    this.singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (!this.singleObject) {
      this.out.startTag(new QName("items"));
    }
    this.opened = true;
  }
}
