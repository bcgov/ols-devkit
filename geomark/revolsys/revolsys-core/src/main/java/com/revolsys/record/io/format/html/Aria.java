package com.revolsys.record.io.format.html;

import com.revolsys.record.io.format.xml.XmlWriter;

public class Aria {
  public static void controls(final XmlWriter out, final String value) {
    out.attribute("aria-controls", value);
  }

  public static void expanded(final XmlWriter out, final boolean expanded) {
    out.attribute("aria-expanded", expanded);
  }

  public static void label(final XmlWriter out, final String label) {
    out.attribute("aria-label", label);
  }

  public static void labelledby(final XmlWriter out, final String value) {
    out.attribute("aria-labelledby", value);
  }
}
