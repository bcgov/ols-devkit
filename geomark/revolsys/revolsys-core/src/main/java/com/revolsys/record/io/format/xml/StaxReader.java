package com.revolsys.record.io.format.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.util.StreamReaderDelegate;

import org.apache.commons.io.input.XmlStreamReader;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.BaseCloseable;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public class StaxReader extends StreamReaderDelegate implements BaseCloseable {
  private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

  public static StaxReader newXmlReader(final InputStream inputStream) {
    return newXmlReader(FACTORY, inputStream);
  }

  public static StaxReader newXmlReader(final Reader reader) {
    return newXmlReader(FACTORY, reader);
  }

  public static StaxReader newXmlReader(final Resource resource) {
    final InputStream inputStream = resource.getInputStream();
    return newXmlReader(inputStream);
  }

  public static StaxReader newXmlReader(final XMLInputFactory factory,
    final InputStream inputStream) {
    try {
      final XmlStreamReader reader = new XmlStreamReader(inputStream);
      return newXmlReader(factory, reader);
    } catch (final IOException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  public static StaxReader newXmlReader(final XMLInputFactory factory, final Reader reader) {
    try {
      final XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
      return new StaxReader(xmlReader);
    } catch (final Throwable e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  private int depth = 0;

  public StaxReader(final XMLStreamReader reader) {
    super(reader);
  }

  private void appendAttribute(final StringBuilder builder, final int index) {
    final String prefix = getAttributePrefix(index);
    final String namespace = getAttributeNamespace(index);
    final String localName = getAttributeLocalName(index);
    final String value = getAttributeValue(index);
    builder.append(' ');
    appendName(builder, prefix, namespace, localName);
    builder.append("='" + value + "'");
  }

  private void appendAttributes(final StringBuilder builder) {
    for (int i = 0; i < getAttributeCount(); i++) {
      appendAttribute(builder, i);
    }
  }

  private void appendName(final StringBuilder builder) {
    if (hasName()) {
      final String prefix = getPrefix();
      final String uri = getNamespaceURI();
      final String localName = getLocalName();
      appendName(builder, prefix, uri, localName);
    }
  }

  private void appendName(final StringBuilder builder, final String prefix, final String uri,
    final String localName) {
    if (uri != null && !"".equals(uri)) {
      builder.append("['");
      builder.append(uri);
      builder.append("']:");
    }
    if (prefix != null) {
      builder.append(prefix + ":");
    }
    if (localName != null) {
      builder.append(localName);
    }
  }

  private void appendNamespace(final StringBuilder builder, final int index) {
    final String prefix = getNamespacePrefix(index);
    final String uri = getNamespaceURI(index);
    builder.append(' ');
    if (prefix == null) {
      builder.append("xmlns='" + uri + "'");
    } else {
      builder.append("xmlns:" + prefix + "='" + uri + "'");
    }
  }

  private void appendNamespaces(final StringBuilder builder) {
    for (int i = 0; i < getNamespaceCount(); i++) {
      appendNamespace(builder, i);
    }
  }

  @Override
  public void close() {
    try {
      super.close();
    } catch (final XMLStreamException e) {
    }
  }

  public String getAttribute(final QName typePath) {
    final String namespaceURI = typePath.getNamespaceURI();
    final String localPart = typePath.getLocalPart();
    final String value = getAttributeValue(namespaceURI, localPart);
    return value;
  }

  public boolean getBooleanAttribute(final String namespaceUri, final String name) {
    final String value = getAttributeValue(namespaceUri, name);
    if (value != null) {
      return Boolean.parseBoolean(value);
    } else {
      return false;
    }
  }

  public int getDepth() {
    return this.depth;
  }

  public double getDoubleAttribute(final String namespaceUri, final String name) {
    final String value = getAttributeValue(namespaceUri, name);
    if (value != null) {
      return Double.parseDouble(value);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public String getElementText() {
    final StringBuilder text = new StringBuilder();
    if (getEventType() == XMLStreamConstants.START_ELEMENT) {
      final int depth = this.depth;
      while (next() != XMLStreamConstants.END_ELEMENT && getDepth() >= depth) {
        switch (getEventType()) {
          case XMLStreamConstants.CHARACTERS:
            text.append(getText());
          break;
        }
      }
    }
    return text.toString();
  }

  public double getElementTextDouble(final double defaultValue) {
    final String text = getElementText();
    if (Property.hasValue(text)) {
      try {
        return Double.parseDouble(text);
      } catch (final Throwable e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  public int getElementTextInt(final int defaultValue) {
    final String text = getElementText();
    if (Property.hasValue(text)) {
      try {
        return Integer.parseInt(text);
      } catch (final Throwable e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  public int getIntAttribute(final String namespaceUri, final String name) {
    final String value = getAttributeValue(namespaceUri, name);
    if (value != null) {
      return Integer.parseInt(value);
    } else {
      return 0;
    }
  }

  public String getLocalPart() {
    final QName name = getName();
    return name.getLocalPart();
  }

  public long getLongAttribute(final String namespaceUri, final String name) {
    final String value = getAttributeValue(namespaceUri, name);
    if (value != null) {
      return Long.parseLong(value);
    } else {
      return 0;
    }
  }

  public QName getQNameAttribute(final QName fieldName) {
    final String value = getAttribute(fieldName);
    final NamespaceContext namespaceContext = getNamespaceContext();
    final QName qName = getXmlQName(namespaceContext, value);
    return qName;
  }

  public QName getXmlQName(final NamespaceContext context, final String value) {
    if (value == null) {
      return null;
    } else {
      final int colonIndex = value.indexOf(':');
      if (colonIndex == -1) {
        return new QName(value);
      } else {
        final String prefix = value.substring(0, colonIndex);
        final String name = value.substring(colonIndex + 1);
        final String namespaceUri = context.getNamespaceURI(prefix);
        return new QName(namespaceUri, name, prefix);
      }
    }
  }

  public boolean isEndElementLocalName(final QName name) {
    if (isEndElement()) {
      final String currentLocalName = getLocalName();
      final String requiredLocalName = name.getLocalPart();
      if (currentLocalName.equals(requiredLocalName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check that the parser is currently at the specified XML element.
   *
   * @param parser The STAX XML
   * @param element The expected XML element.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public boolean isStartElementLocalName(final QName name) {
    final String requiredLocalName = name.getLocalPart();
    return isStartElementLocalName(requiredLocalName);
  }

  public boolean isStartElementLocalName(final String requiredLocalName) {
    if (isStartElement()) {
      final String currentLocalName = getLocalName();
      if (currentLocalName.equals(requiredLocalName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int next() {
    try {
      final int next = super.next();
      switch (next) {
        case XMLStreamConstants.START_ELEMENT:
          this.depth++;
        break;
        case XMLStreamConstants.END_ELEMENT:
          this.depth--;
        break;

        default:
        break;
      }
      return next;
    } catch (final XMLStreamException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public int nextTag() {
    try {
      return super.nextTag();
    } catch (final XMLStreamException e) {
      return (Integer)Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public void require(final int type, final String namespaceURI, final String localPart) {
    try {
      super.require(type, namespaceURI, localPart);
    } catch (final XMLStreamException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  /**
   * Check that the parser is currently at the specified XML element.
   *
   * @param parser The STAX XML
   * @param element The expected XML element.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public void require(final QName element) {
    final String namespaceURI = element.getNamespaceURI();
    final String localPart = element.getLocalPart();
    require(XMLStreamConstants.START_ELEMENT, namespaceURI, localPart);
  }

  /**
   * Check that the parser is currently at the specified XML element.
   *
   * @param parser The STAX XML
   * @param element The expected XML element.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public void requireLocalName(final QName element) {
    final String localPart = element.getLocalPart();
    requireLocalName(localPart);
  }

  public void requireLocalName(final String localPart) {
    require(XMLStreamConstants.START_ELEMENT, null, localPart);
  }

  /**
   * Skip all elements and content until the end of the current element.
   *
   * @param parser The STAX XML
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public void skipSubTree() {
    require(XMLStreamConstants.START_ELEMENT, null, null);
    int level = 1;
    while (level > 0) {
      final int eventType = next();
      if (eventType == XMLStreamConstants.END_ELEMENT) {
        --level;
      } else if (eventType == XMLStreamConstants.START_ELEMENT) {
        ++level;
      }
    }
  }

  /**
   * Skip all events until the next start element which is a child of the
   * current element has one of the elementNames.
   *
   * @param parser The STAX XML
   * @param elementNames The names of the elements to find
   * @return True if one of the elements was found.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public boolean skipToChildStartElements(final Collection<QName> elementNames) {
    int count = 0;
    QName elementName = null;
    if (isEndElement()) {
      elementName = getName();
      if (elementNames.contains(elementName)) {
        nextTag();
      } else {
        return false;
      }
    }
    if (isStartElement()) {
      elementName = getName();
      if (elementNames.contains(elementName)) {
        return true;
      }
    }
    do {
      while (next() != XMLStreamConstants.START_ELEMENT) {
        if (getEventType() == XMLStreamConstants.END_DOCUMENT) {
          return false;
        } else if (isEndElement()) {
          if (count == 0) {
            return false;
          }
          count--;
        }
      }
      count++;
      elementName = getName();
    } while (!elementNames.contains(elementName));
    return true;
  }

  /**
   * Skip all events until the next start element which is a child of the
   * current element has one of the elementNames.
   *
   * @param parser The STAX XML
   * @param elementNames The names of the elements to find
   * @return True if one of the elements was found.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public boolean skipToChildStartElements(final QName... elementNames) {
    final List<QName> names = Arrays.asList(elementNames);
    return skipToChildStartElements(names);
  }

  /**
   * Skip all events until the next end element event.
   *
   * @param parser The STAX XML
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public int skipToEndElement() {
    while (next() != XMLStreamConstants.END_ELEMENT) {
      if (getEventType() == XMLStreamConstants.END_DOCUMENT) {
        return getEventType();
      }
    }
    return getEventType();
  }

  /**
   * Skip all events until the next end element event.
   *
   * @param parser The STAX XML
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public void skipToEndElement(final QName name) {
    while (!isEndElement() || !getName().equals(name)) {
      next();
    }
    next();
  }

  /**
   * Skip all events until the next end element event.
   *
   * @param parser The STAX XML
   */
  public void skipToEndElementByLocalName(final QName name) {
    while (!isEndElement() || !getName().getLocalPart().equals(name.getLocalPart())) {
      next();
      if (getEventType() == XMLStreamConstants.START_ELEMENT
        || getEventType() == XMLStreamConstants.END_ELEMENT) {
      }
    }
    skipWhitespace();
  }

  /**
   * Skip all events until the next start element event.
   *
   * @param parser The STAX XML
   */
  public boolean skipToStartElement() {
    while (next() != XMLStreamConstants.START_ELEMENT) {
      if (getEventType() == XMLStreamConstants.END_DOCUMENT) {
        return false;
      }
    }
    return true;
  }

  public boolean skipToStartElement(final int depth, final QName elementName) {
    while (this.depth >= depth) {
      next();
      if (this.depth < depth) {
        return false;
      } else if (getEventType() == XMLStreamConstants.END_DOCUMENT) {
        return false;
      } else if (getEventType() == XMLStreamConstants.START_ELEMENT) {
        final QName currentElement = getName();
        if (currentElement.equals(elementName)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean skipToStartElement(final int depth, final String localName) {
    while (this.depth >= depth) {
      next();
      if (this.depth < depth) {
        return false;
      } else if (getEventType() == XMLStreamConstants.END_DOCUMENT) {
        return false;
      } else if (getEventType() == XMLStreamConstants.START_ELEMENT) {
        final QName currentElement = getName();
        if (isStartElementLocalName(localName)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean skipToStartElement(final String localName) {
    String currentName = null;
    do {
      while (next() != XMLStreamConstants.START_ELEMENT) {
        if (getEventType() == XMLStreamConstants.END_DOCUMENT) {
          return false;
        }
      }
      currentName = getLocalName();
    } while (!currentName.equals(localName));
    return true;
  }

  public boolean skipToStartElements(final int depth, final Collection<QName> elementNames) {
    try {
      while (this.depth >= depth) {
        next();
        if (this.depth < depth) {
          return false;
        } else if (getEventType() == END_DOCUMENT) {
          return false;
        } else if (getEventType() == START_ELEMENT) {
          final QName elementName = getName();
          if (elementNames.contains(elementName)) {
            return true;
          }
        }
      }
    } catch (final NoSuchElementException e) {
    }
    return false;
  }

  public boolean skipToStartElements(final int depth, final QName... elementNames) {
    return skipToStartElements(depth, Arrays.asList(elementNames));
  }

  public void skipToStartOrEndElement() {
    require(XMLStreamConstants.END_ELEMENT, null, null);
    while (true) {
      final int eventType = next();
      if (eventType == XMLStreamConstants.END_ELEMENT) {
        return;
      } else if (eventType == XMLStreamConstants.START_ELEMENT) {
        return;
      } else if (eventType == XMLStreamConstants.END_DOCUMENT) {
        return;
      }
    }
  }

  /**
   * Skip any whitespace until an start or end of element is found.
   *
   * @param parser The STAX XML
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public int skipWhitespace() {
    while (next() == XMLStreamConstants.CHARACTERS && isWhiteSpace()) {
      switch (getEventType()) {
        case XMLStreamConstants.END_DOCUMENT:
        case XMLStreamConstants.START_ELEMENT:
          return getEventType();
      }
    }
    return getEventType();
  }

  public void startElement(final XMLStreamWriter writer, final QName element) {
    try {
      writer.writeStartElement(element.getPrefix(), element.getLocalPart(),
        element.getNamespaceURI());
    } catch (final XMLStreamException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public String toString() {
    final int eventType = getEventType();
    final StringBuilder builder = new StringBuilder();
    switch (eventType) {
      case START_ELEMENT:
        builder.append("<");
        appendName(builder);
        appendNamespaces(builder);
        appendAttributes(builder);
        builder.append(">");
      break;
      case END_ELEMENT:
        builder.append("</");
        appendName(builder);
        builder.append(">");
      break;
      case SPACE:
      case CHARACTERS:
        int start = getTextStart();
        int length = getTextLength();
        builder.append(new String(getTextCharacters(), start, length));
      break;
      case PROCESSING_INSTRUCTION:
        builder.append("<?");
        if (hasText()) {
          builder.append(getText());
        }
        builder.append("?>");
      break;
      case CDATA:
        builder.append("<![CDATA[");
        start = getTextStart();
        length = getTextLength();
        builder.append(new String(getTextCharacters(), start, length));
        builder.append("]]>");
      break;
      case COMMENT:
        builder.append("<!--");
        if (hasText()) {
          builder.append(getText());
        }
        builder.append("-->");
      break;
      case ENTITY_REFERENCE:
        builder.append(getLocalName() + "=");
        if (hasText()) {
          builder.append("[" + getText() + "]");
        }
      break;
      case START_DOCUMENT:
        builder.append("<?xml");
        builder.append(" version='" + getVersion() + "'");
        builder.append(" encoding='" + getCharacterEncodingScheme() + "'");
        if (isStandalone()) {
          builder.append(" standalone='yes'");
        } else {
          builder.append(" standalone='no'");
        }
        builder.append("?>");
      break;

      case ATTRIBUTE:
        return "";
      case DTD:
        return "";
      case END_DOCUMENT:
        return "";
      case ENTITY_DECLARATION:
        return "";
      case NAMESPACE:
        return "";
      case NOTATION_DECLARATION:
        return "";
      default:
        return "Unknown XML event: " + eventType;
    }
    return builder.toString();
  }
}
