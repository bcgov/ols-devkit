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
package com.revolsys.record.io.format.xml;

import javax.xml.namespace.QName;

/**
 * The XmlConstants class defines some useful constants for XML namespaces.
 *
 * @author Paul Austin
 * @version 1.0
 */
public interface XmlConstants {

  /** The XML Namespace prefix for XML Namespaces. */
  String XMLNS_NS_PREFIX = "xmlns";

  /** The XML Namespace prefix for XML. */
  String XML_NS_PREFIX = "xml";

  /** The XML Namespace URI for XML. */
  String XML_NS_URI = "http://www.w3.org/XML/1998/namespace";

  /** The XML Namespace prefix for XML Namespaces. */
  String XML_SCHEMA_NAMESPACE_PREFIX = "xs";

  /** The XML Namespace URI for XML Namespaces. */
  String XML_SCHEMA_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";

  /** The XML Namespace URI for XML Namespaces. */
  String XMLNS_NS_URI = "http://www.w3.org/2000/xmlns/";

  QName XML_LANG = new QName(XML_NS_URI, "lang", XML_NS_PREFIX);

  QName XML_SCHEMA = new QName(XML_SCHEMA_NAMESPACE_URI, "schema", XML_SCHEMA_NAMESPACE_PREFIX);

  QName XS_BOOLEAN = new QName(XML_SCHEMA_NAMESPACE_URI, "boolean", XML_SCHEMA_NAMESPACE_PREFIX);

  QName XS_BYTE = new QName(XML_SCHEMA_NAMESPACE_URI, "byte", XML_SCHEMA_NAMESPACE_PREFIX);

  QName XS_DATE = new QName(XML_SCHEMA_NAMESPACE_URI, "date", XML_SCHEMA_NAMESPACE_PREFIX);

  QName XS_DECIMAL = new QName(XML_SCHEMA_NAMESPACE_URI, "decimal", XML_SCHEMA_NAMESPACE_PREFIX);

  QName XS_DOUBLE = new QName(XML_SCHEMA_NAMESPACE_URI, "double", XML_SCHEMA_NAMESPACE_PREFIX);

  QName XS_FLOAT = new QName(XML_SCHEMA_NAMESPACE_URI, "float", XML_SCHEMA_NAMESPACE_PREFIX);

  QName XS_INT = new QName(XML_SCHEMA_NAMESPACE_URI, "int", XML_SCHEMA_NAMESPACE_PREFIX);

  QName XS_INTEGER = new QName(XML_SCHEMA_NAMESPACE_URI, "integer", XML_SCHEMA_NAMESPACE_PREFIX);

  QName XS_LONG = new QName(XML_SCHEMA_NAMESPACE_URI, "long", XML_SCHEMA_NAMESPACE_PREFIX);

  QName XS_SHORT = new QName(XML_SCHEMA_NAMESPACE_URI, "short", XML_SCHEMA_NAMESPACE_PREFIX);

  QName XS_STRING = new QName(XML_SCHEMA_NAMESPACE_URI, "string", XML_SCHEMA_NAMESPACE_PREFIX);

}
