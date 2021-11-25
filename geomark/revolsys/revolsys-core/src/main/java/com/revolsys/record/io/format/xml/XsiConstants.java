package com.revolsys.record.io.format.xml;

import javax.xml.namespace.QName;

public class XsiConstants {
  private static final String _NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";

  private static final String _PREFIX = "xsi";

  public static final String NAMESPACE_URI = _NAMESPACE_URI;

  public static final QName NIL = new QName(_NAMESPACE_URI, "nil", _PREFIX);

  public static final QName NO_NAMESPACE_SCHEMA_LOCATION = new QName(_NAMESPACE_URI,
    "noNamespaceSchemaLocation", _PREFIX);

  public static final String PREFIX = _PREFIX;

  public static final QName TYPE = new QName(_NAMESPACE_URI, "type", _PREFIX);
}
