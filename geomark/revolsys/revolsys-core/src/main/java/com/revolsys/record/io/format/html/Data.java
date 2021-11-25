package com.revolsys.record.io.format.html;

import com.revolsys.record.io.format.xml.XmlWriter;

public class Data {
  public static void parent(final XmlWriter out, final String value) {
    out.attribute("data-parent", value);
  }

  public static void target(final XmlWriter out, final String value) {
    out.attribute("data-target", value);
  }

  public static void toggle(final XmlWriter out, final String value) {
    out.attribute("data-toggle", value);
  }

}
