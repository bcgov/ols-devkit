package com.revolsys.record.io.format.html;

import java.io.Writer;
import java.net.URI;
import java.util.List;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class XhtmlRecordWriter extends AbstractRecordWriter {

  private String cssClass;

  private boolean opened = false;

  /** The writer */
  private XmlWriter out;

  private boolean singleObject;

  private String title;

  private boolean wrap = true;

  public XhtmlRecordWriter(final RecordDefinitionProxy recordDefinition, final Writer out) {
    super(recordDefinition);
    this.out = new XmlWriter(out);
  }

  /**
   * Closes the underlying writer.
   */
  @Override
  public void close() {
    if (this.out != null) {
      try {
        writeFooter();
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
    super.setProperty(name, value);
    if (value != null) {
      if (name.equals(IoConstants.WRAP_PROPERTY)) {
        this.wrap = Boolean.valueOf(value.toString());
      } else if (name.equals(IoConstants.TITLE_PROPERTY)) {
        this.title = value.toString();
      } else if (name.equals(IoConstants.CSS_CLASS)) {
        this.cssClass = value.toString();
      }
    }
  }

  @Override
  public void write(final Record record) {
    if (!this.opened) {
      writeHeader();
    }
    if (this.singleObject) {
      for (final FieldDefinition fieldDefinition : getFieldDefinitions()) {
        final String fieldName = fieldDefinition.getName();
        final Object value = record.getValue(fieldName);
        if (isValueWritable(value)) {
          this.out.startTag(HtmlElem.TR);
          this.out.element(HtmlElem.TH, CaseConverter.toCapitalizedWords(fieldName));
          this.out.startTag(HtmlElem.TD);
          if (value == null) {
            this.out.text("-");
          } else if (value instanceof URI) {
            HtmlUtil.serializeA(this.out, null, value, value);
          } else {
            writeValue(fieldDefinition, value);
          }
          this.out.endTag(HtmlElem.TD);
          this.out.endTag(HtmlElem.TR);
        }
      }
    } else {
      this.out.startTag(HtmlElem.TR);
      for (final FieldDefinition fieldDefinition : getFieldDefinitions()) {
        final String fieldName = fieldDefinition.getName();
        final Object value;
        if (isWriteCodeValues()) {
          value = record.getCodeValue(fieldName);
        } else {
          value = record.getValue(fieldName);
        }
        this.out.startTag(HtmlElem.TD);
        if (value == null) {
          this.out.text("-");
        }
        if (value instanceof URI) {
          HtmlUtil.serializeA(this.out, null, value, value);
        } else {
          writeValue(fieldDefinition, value);
        }
        this.out.endTag(HtmlElem.TD);
      }
      this.out.endTag(HtmlElem.TR);

    }
  }

  private void writeFooter() {
    if (this.opened) {
      this.out.endTag(HtmlElem.TBODY);
      this.out.endTag(HtmlElem.TABLE);
      this.out.endTag(HtmlElem.DIV);
      this.out.endTag(HtmlElem.DIV);
      if (this.wrap) {
        this.out.endTag(HtmlElem.BODY);
        this.out.endTag(HtmlElem.HTML);
      }
    }
  }

  @SuppressWarnings("rawtypes")
  private void writeHeader() {
    setIndent(isIndent());
    if (this.wrap) {
      this.out.startDocument("UTF-8", "1.0");
      this.out.startTag(HtmlElem.HTML);

      this.out.startTag(HtmlElem.HEAD);

      this.out.startTag(HtmlElem.META);
      this.out.attribute(HtmlAttr.HTTP_EQUIV, "Content-Type");
      this.out.attribute(HtmlAttr.CONTENT, "text/html; charset=utf-8");
      this.out.endTag(HtmlElem.META);

      if (Property.hasValue(this.title)) {
        this.out.element(HtmlElem.TITLE, this.title);
      }

      final Object style = getProperty("htmlCssStyleUrl");
      if (style instanceof String) {
        final String styleUrl = (String)style;
        this.out.startTag(HtmlElem.LINK);
        this.out.attribute(HtmlAttr.HREF, styleUrl);
        this.out.attribute(HtmlAttr.REL, "stylesheet");
        this.out.attribute(HtmlAttr.TYPE, "text/css");
        this.out.endTag(HtmlElem.LINK);
      } else if (style instanceof List) {
        final List styleUrls = (List)style;
        for (final Object styleUrl : styleUrls) {
          this.out.startTag(HtmlElem.LINK);
          this.out.attribute(HtmlAttr.HREF, styleUrl);
          this.out.attribute(HtmlAttr.REL, "stylesheet");
          this.out.attribute(HtmlAttr.TYPE, "text/css");
          this.out.endTag(HtmlElem.LINK);
        }
      }

      this.out.endTag(HtmlElem.HEAD);

      this.out.startTag(HtmlElem.BODY);
    }
    this.out.startTag(HtmlElem.DIV);
    this.out.attribute(HtmlAttr.CLASS, this.cssClass);
    if (this.title != null) {
      this.out.element(HtmlElem.H1, this.title);
    }
    this.singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (this.singleObject) {
      this.out.startTag(HtmlElem.DIV);
      this.out.attribute(HtmlAttr.CLASS, "objectView");
      this.out.startTag(HtmlElem.TABLE);
      this.out.attribute(HtmlAttr.CLASS, "data");
      this.out.startTag(HtmlElem.TBODY);
    } else {
      this.out.startTag(HtmlElem.DIV);
      this.out.attribute(HtmlAttr.CLASS, "objectList");
      this.out.startTag(HtmlElem.TABLE);
      this.out.attribute(HtmlAttr.CLASS, "data");

      this.out.startTag(HtmlElem.THEAD);
      this.out.startTag(HtmlElem.TR);
      for (final String name : getFieldNames()) {
        this.out.element(HtmlElem.TH, name);
      }
      this.out.endTag(HtmlElem.TR);
      this.out.endTag(HtmlElem.THEAD);

      this.out.startTag(HtmlElem.TBODY);
    }
    this.opened = true;
  }

  private void writeValue(final FieldDefinition field, final Object value) {
    final String stringValue = field.toString(value);
    this.out.text(stringValue);
  }
}
