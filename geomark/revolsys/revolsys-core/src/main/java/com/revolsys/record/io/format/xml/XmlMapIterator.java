package com.revolsys.record.io.format.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamConstants;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.NamedLinkedHashMapEx;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public class XmlMapIterator extends AbstractIterator<MapEx> {

  /** Flag indicating if there are more records to be read. */
  private boolean hasNext = false;

  private StaxReader in;

  private Resource resource;

  private final boolean single;

  public XmlMapIterator(final Resource resource) {
    this(resource, false);
  }

  public XmlMapIterator(final Resource resource, final boolean single) {
    this.resource = resource;
    this.single = single;
  }

  @Override
  protected void closeDo() {
    super.closeDo();
    if (this.in != null) {
      this.in.close();
      this.in = null;
    }
    this.resource = null;
  }

  @Override
  protected MapEx getNext() throws NoSuchElementException {
    if (this.hasNext) {
      final MapEx map = readMap();
      if (this.in.skipToStartElement()) {
        this.hasNext = false;
      }
      return map;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  protected void initDo() {
    super.initDo();
    this.in = StaxReader.newXmlReader(this.resource);
    if (this.in.skipToStartElement()) {
      if (this.single) {
        this.hasNext = true;
      } else {
        if (this.in.skipToStartElement()) {
          this.hasNext = true;
        }
      }
    }

  }

  @SuppressWarnings("unchecked")
  private Object readElement() {
    final String name = this.in.getLocalName();
    final MapEx map = new NamedLinkedHashMapEx(name);
    int textIndex = 0;
    int elementIndex = 0;
    while (this.in.next() != XMLStreamConstants.END_ELEMENT) {
      switch (this.in.getEventType()) {
        case XMLStreamConstants.CDATA:
        case XMLStreamConstants.CHARACTERS:
          final String text = this.in.getText();
          if (Property.hasValue(text)) {
            map.put("xmlText" + ++textIndex, text);
          }
        break;
        case XMLStreamConstants.SPACE:
        break;
        case XMLStreamConstants.START_ELEMENT:
          elementIndex++;
          final String tagName = this.in.getLocalName();
          final Object value = readElement();
          final Object oldValue = map.get(tagName);
          if (oldValue == null) {
            map.put(tagName, value);
          } else {
            List<Object> list;
            if (oldValue instanceof List) {
              list = (List<Object>)oldValue;
            } else {
              list = new ArrayList<>();
              list.add(oldValue);
              map.put(tagName, list);
            }
            list.add(value);

          }
        break;
        case XMLStreamConstants.COMMENT:
        break;
        default:
          System.err.println(this.in.getEventType() + " " + this.in.getText());
        break;
      }
    }
    if (elementIndex == 0) {
      if (textIndex > 0) {
        final StringBuilder fullText = new StringBuilder();
        for (final Object text : map.values()) {
          fullText.append(text);
        }
        return fullText.toString();
      }
    }
    return map;
  }

  @SuppressWarnings("unchecked")
  private MapEx readMap() {
    final String name = this.in.getLocalName();
    final MapEx map = new NamedLinkedHashMapEx(name);
    int textIndex = 0;
    while (this.in.next() != XMLStreamConstants.END_ELEMENT) {
      switch (this.in.getEventType()) {
        case XMLStreamConstants.CDATA:
        case XMLStreamConstants.CHARACTERS:
          final String text = this.in.getText();
          if (Property.hasValue(text)) {
            map.put("xmlText" + ++textIndex, text);
          }
        break;
        case XMLStreamConstants.SPACE:
        break;
        case XMLStreamConstants.START_ELEMENT:
          final String tagName = this.in.getLocalName();
          final Object value = readElement();
          final Object oldValue = map.get(tagName);
          if (oldValue == null) {
            map.put(tagName, value);
          } else {
            List<Object> list;
            if (oldValue instanceof List) {
              list = (List<Object>)oldValue;
            } else {
              list = new ArrayList<>();
              list.add(oldValue);
              map.put(tagName, list);
            }
            list.add(value);

          }
        break;
        default:
        break;
      }
    }
    return map;
  }
}
