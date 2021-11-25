package com.revolsys.record.io.format.xml;

import java.util.function.Consumer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.revolsys.util.Property;

public interface XmlUtil {

  static void forEachElement(final Element parentElement, final Consumer<Element> action) {
    final NodeList childNodes = parentElement.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      final Node childNode = childNodes.item(i);
      if (childNode instanceof Element) {
        final Element element = (Element)childNode;
        action.accept(element);
      }
    }
  }

  static void forEachElement(final Element parentElement, final String elementName,
    final Consumer<Element> action) {
    final NodeList childNodes = parentElement.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      final Node childNode = childNodes.item(i);
      if (childNode instanceof Element) {
        final Element element = (Element)childNode;
        final String tagName = element.getTagName();
        if (elementName.equals(tagName)) {
          action.accept(element);
        }
      }
    }
  }

  static void forFirstElement(final Element parentElement, final String elementName,
    final Consumer<Element> action) {
    final Element element = getFirstElement(parentElement, elementName);
    if (element != null) {
      action.accept(element);
    }
  }

  static double getAttributeDouble(final Element element, final String attributeName,
    final double defaultValue) {
    final String text = element.getAttribute(attributeName);
    if (Property.isEmpty(text)) {
      return defaultValue;
    } else {
      return Double.parseDouble(text);
    }
  }

  static int getAttributeInt(final Element element, final String attributeName,
    final int defaultValue) {
    final String text = element.getAttribute(attributeName);
    if (Property.isEmpty(text)) {
      return defaultValue;
    } else {
      return Integer.parseInt(text);
    }
  }

  static Element getFirstElement(final Element parentElement, final String elementName) {
    final NodeList childNodes = parentElement.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      final Node childNode = childNodes.item(i);
      if (childNode instanceof Element) {
        final Element element = (Element)childNode;
        final String tagName = element.getTagName();
        if (elementName.equals(tagName)) {
          return element;
        }
      }
    }
    return null;
  }

  static String getFirstElementAttribute(final Element parentElement, final String elementName,
    final String attributeName) {
    final Element element = getFirstElement(parentElement, elementName);
    if (element == null) {
      return null;
    } else {
      return element.getAttribute(attributeName);
    }
  }

  static String getFirstElementAttribute(final Element parentElement, final String elementName,
    final String attributeNamespace, final String attributeName) {
    final Element element = getFirstElement(parentElement, elementName);
    if (element == null) {
      return null;
    } else {
      return element.getAttributeNS(attributeNamespace, attributeName);
    }
  }

  static Double getFirstElementDouble(final Element parentElement, final String elementName) {
    final String text = getFirstElementText(parentElement, elementName);
    if (text == null) {
      return null;
    } else {
      return Double.parseDouble(text);
    }
  }

  static double getFirstElementDouble(final Element parentElement, final String elementName,
    final double defaultValue) {
    final String text = getFirstElementText(parentElement, elementName);
    if (text == null) {
      return defaultValue;
    } else {
      return Double.parseDouble(text);
    }
  }

  static Integer getFirstElementInt(final Element parentElement, final String elementName) {
    final String text = getFirstElementText(parentElement, elementName);
    if (text == null) {
      return null;
    } else {
      return Integer.parseInt(text);
    }
  }

  static int getFirstElementInt(final Element parentElement, final String elementName,
    final int defaultValue) {
    final String text = getFirstElementText(parentElement, elementName);
    if (text == null) {
      return defaultValue;
    } else {
      return Integer.parseInt(text);
    }
  }

  static String getFirstElementText(final Element parentElement, final String elementName) {
    final Element element = getFirstElement(parentElement, elementName);
    if (element == null) {
      return null;
    } else {
      return element.getTextContent();
    }
  }

  static QName getXmlQName(final NamespaceContext context, final String value) {
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
}
