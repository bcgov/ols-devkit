package com.revolsys.record.io.format.html;

import java.io.Writer;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.collection.NameProxy;
import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.HtmlUtil;

public class XhtmlMapWriter extends AbstractMapWriter {

  private boolean opened = false;

  /** The writer */
  private XmlWriter out;

  private String title = "Items";

  private boolean wrap = true;

  public XhtmlMapWriter(final Writer out) {
    this.out = new XmlWriter(out);

  }

  /**
   * Closes the underlying writer.
   */
  @Override
  public void close() {
    if (this.out != null) {
      try {
        if (this.opened) {
          this.out.endTag(HtmlElem.TABLE);
          this.out.endTag(HtmlElem.DIV);
          this.out.endTag(HtmlElem.DIV);
          if (this.wrap) {
            this.out.endTag(HtmlElem.BODY);
            this.out.endTag(HtmlElem.HTML);
          }
        }
        this.out.flush();
      } finally {
        if (this.wrap) {
          FileUtil.closeSilent(this.out);
        }
        this.out = null;
      }
    }
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if (name.equals(IoConstants.WRAP_PROPERTY)) {
      this.wrap = Boolean.valueOf(value.toString());
    } else if (name.equals(IoConstants.TITLE_PROPERTY)) {
      this.title = value.toString();
    }
    super.setProperty(name, value);
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    if (!this.opened) {
      if (this.title == null) {
        if (values instanceof NameProxy) {
          final String name = ((NameProxy)values).getName();
          if (name != null) {
            this.title = name;
          }
        }
      }
      if (this.wrap) {
        writeHeader();
      }
      this.out.startTag(HtmlElem.DIV);
      if (this.title != null) {
        this.out.element(HtmlElem.H1, this.title);
      }
      this.out.startTag(HtmlElem.DIV);
      this.out.attribute(HtmlAttr.CLASS, "objectView");
      this.out.startTag(HtmlElem.TABLE);
      this.out.attribute(HtmlAttr.CLASS, "data");
      this.opened = true;
    }
    this.out.startTag(HtmlElem.TBODY);

    for (final Entry<String, ? extends Object> field : values.entrySet()) {
      final Object key = field.getKey();
      final Object value = field.getValue();
      if (isWritable(value)) {
        this.out.startTag(HtmlElem.TR);
        // TODO case converter on key name
        this.out.element(HtmlElem.TH, CaseConverter.toCapitalizedWords(key.toString()));
        this.out.startTag(HtmlElem.TD);
        if (value instanceof URI) {
          HtmlUtil.serializeA(this.out, null, value, value);
        } else {
          this.out.text(value);
        }
        this.out.endTag(HtmlElem.TD);
        this.out.endTag(HtmlElem.TR);
      }
    }
    this.out.endTag(HtmlElem.TBODY);
  }

  private void writeHeader() {
    this.out.startTag(HtmlElem.HTML);

    this.out.startTag(HtmlElem.HEAD);
    this.out.element(HtmlElem.TITLE, this.title);

    this.out.endTag(HtmlElem.HEAD);

    this.out.startTag(HtmlElem.BODY);
  }
}
