/*
 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import com.revolsys.record.io.format.xml.XmlWriter;

public final class HtmlUtil {
  public static void elementWithId(final XmlWriter writer, final QName tag, final String id,
    final Object content) {
    writer.startTag(tag);
    if (Property.hasValue(id)) {
      writer.attribute(HtmlAttr.ID, id.replaceAll("[^A-Za-z0-9\\-:.]", "_"));
    }
    writer.text(content);
    writer.endTag(tag);
  }

  public static void serializeA(final XmlWriter out, final String cssClass, final Object url,
    final Object content) {
    if (url != null) {
      out.startTag(HtmlElem.A);
      if (cssClass != null) {
        out.attribute(HtmlAttr.CLASS, cssClass);
      }
      out.attribute(HtmlAttr.HREF, url);
    }
    out.text(content);
    if (url != null) {
      out.endTag(HtmlElem.A);
    }
  }

  public static void serializeB(final XmlWriter out, final String content) {
    out.startTag(HtmlElem.B);
    out.text(content);
    out.endTag(HtmlElem.B);
  }

  public static void serializeButton(final XmlWriter out, final String name, final String type,
    final Object value, final String text, final String cssClass) {
    out.startTag(HtmlElem.BUTTON);
    out.attribute(HtmlAttr.NAME, name);
    out.attribute(HtmlAttr.TYPE, type);
    out.attribute(HtmlAttr.VALUE, value);
    out.attribute(HtmlAttr.CLASS, cssClass);
    if (Property.hasValue(text)) {
      out.text(text);
    }
    out.endTag(HtmlElem.BUTTON);
  }

  public static void serializeButtonInput(final XmlWriter out, final String value,
    final String onClick) {
    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.TYPE, "button");
    out.attribute(HtmlAttr.VALUE, value);
    out.attribute(HtmlAttr.ON_CLICK, onClick);

    out.endTag(HtmlElem.INPUT);

  }

  public static void serializeCheckBox(final XmlWriter out, final String name, final String value,
    final boolean selected, final String onClick) {
    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.ID, name);
    out.attribute(HtmlAttr.NAME, name);
    out.attribute(HtmlAttr.TYPE, "checkbox");
    out.attribute(HtmlAttr.CLASS, "input-sm");
    if (selected) {
      out.attribute(HtmlAttr.CHECKED, "checked");
    }
    if (value != null) {
      out.attribute(HtmlAttr.VALUE, value);
    }
    if (onClick != null) {
      out.attribute(HtmlAttr.ON_CLICK, onClick);
    }
    out.endTag(HtmlElem.INPUT);
  }

  public static void serializeCss(final XmlWriter writer, final Iterable<String> urls) {
    for (final String url : urls) {
      serializeCss(writer, url);
    }
  }

  public static void serializeCss(final XmlWriter writer, final String... urls) {
    for (final String url : urls) {
      serializeCss(writer, url);
    }
  }

  public static void serializeCss(final XmlWriter out, final String url) {
    out.startTag(HtmlElem.LINK);
    out.attribute(HtmlAttr.HREF, url);
    out.attribute(HtmlAttr.REL, "stylesheet");
    out.attribute(HtmlAttr.TYPE, "text/css");
    out.endTagLn(HtmlElem.LINK);
  }

  public static void serializeDiv(final XmlWriter out, final String cssClass,
    final Object content) {
    if (content != null) {
      final String text = content.toString().trim();
      if (text.length() > 0) {
        out.startTag(HtmlElem.DIV);
        if (cssClass != null) {
          out.attribute(HtmlAttr.CLASS, cssClass);
        }
        out.text(text);
        out.endTag(HtmlElem.DIV);
      }
    }
  }

  public static void serializeFileInput(final XmlWriter out, final String name) {
    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.NAME, name);
    out.attribute(HtmlAttr.TYPE, "file");
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");

    out.endTag(HtmlElem.INPUT);
  }

  public static void serializeFileInput(final XmlWriter out, final String name,
    final Object value) {
    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.NAME, name);
    out.attribute(HtmlAttr.TYPE, "file");
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");

    if (value != null) {
      out.attribute(HtmlAttr.VALUE, value);
    }

    out.endTag(HtmlElem.INPUT);

  }

  public static void serializeHiddenInput(final XmlWriter out, final String name,
    final Object value) {

    String stringValue = null;
    if (value != null) {
      stringValue = value.toString();
    }
    serializeHiddenInput(out, name, stringValue);
  }

  public static void serializeHiddenInput(final XmlWriter out, final String name,
    final String value) {
    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.NAME, name);
    out.attribute(HtmlAttr.TYPE, "hidden");
    if (value != null) {
      out.attribute(HtmlAttr.VALUE, value);
    }
    out.endTag(HtmlElem.INPUT);
  }

  public static void serializeImage(final XmlWriter out, final String src, final String title) {
    if (Property.hasValue(src)) {
      out.startTag(HtmlElem.IMG);
      out.attribute(HtmlAttr.SRC, src);
      out.attribute(HtmlAttr.ALT, title);
      out.attribute(HtmlAttr.TITLE, title);
      out.endTag();
    }
  }

  public static void serializeImage(final XmlWriter out, final String src, final String title,
    final String cssClass) {
    if (Property.hasValue(src)) {
      out.startTag(HtmlElem.IMG);
      out.attribute(HtmlAttr.SRC, src);
      if (title != null) {
        out.attribute(HtmlAttr.ALT, title);
        out.attribute(HtmlAttr.TITLE, title);
      }
      out.attribute(HtmlAttr.CLASS, cssClass);
      out.endTag();
    }
  }

  public static void serializeP(final XmlWriter out, final String cssClass, final String text) {
    if (Property.hasValue(text)) {
      out.startTag(HtmlElem.P);
      out.attribute(HtmlAttr.CLASS, cssClass);
      out.text(text);
      out.endTag(HtmlElem.P);
    }
  }

  public static void serializePre(final XmlWriter out, final String text) {
    out.startTag(HtmlElem.PRE);
    out.text(text);
    out.endTag(HtmlElem.PRE);
  }

  public static void serializeScript(final XmlWriter out, final String script) {
    out.startTag(HtmlElem.SCRIPT);
    out.attribute(HtmlAttr.TYPE, "text/javascript");
    out.closeStartTagLn();
    out.text(script);
    out.endTagLn(HtmlElem.SCRIPT);
  }

  public static void serializeScriptLink(final XmlWriter writer, final String... urls) {
    for (final String url : urls) {
      serializeScriptLink(writer, url);
    }
  }

  public static void serializeScriptLink(final XmlWriter out, final String url) {
    out.startTag(HtmlElem.SCRIPT);
    out.attribute(HtmlAttr.TYPE, "text/javascript");
    out.attribute(HtmlAttr.SRC, url);
    out.closeStartTagLn();
    out.endTagLn(HtmlElem.SCRIPT);
  }

  public static void serializeSelect(final XmlWriter out, final String name,
    final Object selectedValue, final boolean optional, final List<? extends Object> values) {
    out.startTag(HtmlElem.SELECT);
    out.attribute(HtmlAttr.NAME, name);
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");
    if (optional) {
      out.startTag(HtmlElem.OPTION);
      out.attribute(HtmlAttr.VALUE, "");
      out.text("-");
      out.endTag(HtmlElem.OPTION);
    }
    if (values != null) {
      for (final Object value : values) {

        out.startTag(HtmlElem.OPTION);
        if (selectedValue != null && selectedValue.equals(value)) {
          out.attribute(HtmlAttr.SELECTED, "true");
        }
        out.text(value);

        out.endTag(HtmlElem.OPTION);

      }
    }
    out.endTag(HtmlElem.SELECT);

  }

  public static void serializeSelect(final XmlWriter out, final String name,
    final Object selectedValue, final boolean optional, final Map<?, ?> values) {
    out.startTag(HtmlElem.SELECT);
    out.attribute(HtmlAttr.NAME, name);
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");
    if (optional) {
      out.startTag(HtmlElem.OPTION);
      out.attribute(HtmlAttr.VALUE, "");
      out.text("-");
      out.endTag(HtmlElem.OPTION);
    }
    if (values != null) {
      for (final Entry<?, ?> entry : values.entrySet()) {
        final Object value = entry.getKey();
        final Object text = entry.getValue();
        out.startTag(HtmlElem.OPTION);
        if (selectedValue != null && selectedValue.equals(value)) {
          out.attribute(HtmlAttr.SELECTED, "true");
        }
        out.attribute(HtmlAttr.VALUE, value);
        out.text(text);
        out.endTag(HtmlElem.OPTION);

      }
    }
    out.endTag(HtmlElem.SELECT);

  }

  public static void serializeSelect(final XmlWriter out, final String name,
    final Object selectedValue, final boolean optional, final Object... values) {
    serializeSelect(out, name, selectedValue, false, Arrays.asList(values));

  }

  public static void serializeSpan(final XmlWriter out, final String cssClass,
    final Object content) {
    if (content != null) {
      final String text = content.toString().trim();
      if (text.length() > 0) {
        out.startTag(HtmlElem.SPAN);
        if (cssClass != null) {
          out.attribute(HtmlAttr.CLASS, cssClass);
        }
        out.text(text);
        out.endTag(HtmlElem.SPAN);
      }
    }
  }

  public static void serializeStyle(final XmlWriter out, final String style) {
    out.startTag(HtmlElem.STYLE);
    out.attribute(HtmlAttr.TYPE, "text/css");
    out.newLine();
    out.text(style);
    out.endTagLn(HtmlElem.STYLE);
  }

  public static void serializeStyleLink(final XmlWriter out, final String url) {
    out.startTag(HtmlElem.LINK);
    out.attribute(HtmlAttr.REL, "stylesheet");
    out.attribute(HtmlAttr.TYPE, "text/css");
    out.attribute(HtmlAttr.HREF, url);
    out.endTag(HtmlElem.LINK);
  }

  public static void serializeSubmitInput(final XmlWriter out, final String name,
    final Object value) {
    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.NAME, name);
    out.attribute(HtmlAttr.TYPE, "submit");

    if (value != null) {
      out.attribute(HtmlAttr.VALUE, value);
    }
    out.endTag(HtmlElem.INPUT);

  }

  public static void serializeTag(final XmlWriter out, final QName tag, final String content) {
    out.startTag(tag);
    out.text(content);
    out.endTag(tag);
  }

  public static void serializeTextInput(final XmlWriter out, final String name, final Object value,
    final int size, final int maxLength) {
    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.NAME, name);
    out.attribute(HtmlAttr.TYPE, "text");
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");
    out.attribute(HtmlAttr.SIZE, size);
    out.attribute(HtmlAttr.MAX_LENGTH, maxLength);
    if (value != null) {
      out.attribute(HtmlAttr.VALUE, value);
    }
    out.endTag(HtmlElem.INPUT);

  }

  /**
   * Construct a new HtmlUtil.
   */
  private HtmlUtil() {
  }
}
