package com.revolsys.record.io.format.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

public class QNameFactory {

  private final String namespaceUri;

  private final Map<String, QName> qNames = new HashMap<>();

  public QNameFactory(final String namespaceUri) {
    this.namespaceUri = namespaceUri;
  }

  public QName getQName(final String localPart) {
    QName qName = this.qNames.get(localPart);
    if (qName == null) {
      qName = new QName(this.namespaceUri, localPart);
      this.qNames.put(localPart, qName);
    }
    return qName;
  }
}
