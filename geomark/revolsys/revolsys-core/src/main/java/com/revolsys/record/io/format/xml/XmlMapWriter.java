package com.revolsys.record.io.format.xml;

import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import com.revolsys.collection.NameProxy;
import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;

public class XmlMapWriter extends AbstractMapWriter {

  private boolean opened;

  /** The writer */
  private XmlWriter out;

  private boolean singleObject;

  public XmlMapWriter(final Writer out) {
    this.out = new XmlWriter(out);
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

  private void list(final Collection<? extends Object> list) {
    for (final Object value : list) {
      if (value instanceof Map) {
        final Map<String, ?> map = (Map<String, ?>)value;
        map(map);
      } else if (value instanceof Collection) {
        final Collection<?> subList = (Collection<?>)value;
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
      } else if (value instanceof Collection) {
        final Collection<?> list = (Collection<?>)value;
        this.out.startTag(tagName);
        list(list);
        this.out.endTag();
      } else if (isWritable(value)) {
        this.out.nillableElement(tagName, value);
      }
    }
    this.out.endTag();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    super.setProperty(name, value);
    if (name.equals(IoConstants.INDENT)) {
      this.out.setIndent((Boolean)value);
    }
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    if (!this.opened) {
      writeHeader();
      this.opened = true;
    }
    map(values);
  }

  private void writeHeader() {
    this.out.startDocument("UTF-8", "1.0");
    this.singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (!this.singleObject) {
      this.out.startTag(new QName("items"));
    }
  }
}
