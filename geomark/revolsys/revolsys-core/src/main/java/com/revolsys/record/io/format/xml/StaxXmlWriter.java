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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * The StaxXmlWriter is a wrapper around an {@link XmlWriter} that implements
 * the {@link XMLStreamWriter} interface from the StAX API.
 *
 * @author Paul Austin
 */
public class StaxXmlWriter implements XMLStreamWriter {
  /** The namespace context. */
  private NamespaceContext namespaceContext;

  /** The underlying XmlWriter. */
  private final XmlWriter writer;

  /**
   * @param writer
   */
  public StaxXmlWriter(final XmlWriter writer) {
    this.writer = writer;
  }

  /**
   * <p>
   * Close this writer and free any resources associated with the writer.
   * <b>Note: this underlying writer closes the stream so this method breaks
   * from the StAX specification which says it must not close te underlying
   * output stream</b>.
   * </p>
   *
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void close() throws XMLStreamException {
    this.writer.close();
  }

  /**
   * Write any cached data to the underlying output mechanism.
   *
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void flush() throws XMLStreamException {
    this.writer.flush();
  }

  /**
   * Get the current namespace context.
   *
   * @return The namespace context.
   */
  @Override
  public NamespaceContext getNamespaceContext() {
    return this.namespaceContext;
  }

  /**
   * Get the prefix for the XML Namespace URI.
   *
   * @param namespaceUri The XML Namespace URI.
   * @return The prefix.
   * @throws XMLStreamException If there was an exception getting the prefix.
   */
  @Override
  public String getPrefix(final String namespaceUri) throws XMLStreamException {
    return this.writer.getPrefix(namespaceUri);
  }

  /**
   * Get the value of a feature/property from the underlying implementation.
   *
   * @param name The name of the property, may not be null.
   * @return The value of the property.
   */
  @Override
  public Object getProperty(final String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    throw new IllegalArgumentException("Property " + name + " is not supported");
  }

  /**
   * Bind the namespace URI to the default namespace.
   *
   * @param namespaceUri The XML Namespace URI.
   * @throws XMLStreamException If there was an exception setting the prefix.
   */
  @Override
  public void setDefaultNamespace(final String namespaceUri) throws XMLStreamException {
  }

  /**
   * Set the current namespace context.
   *
   * @param namespaceContext The namespace context.
   * @throws XMLStreamException If there was an error setting the context.
   */
  @Override
  public void setNamespaceContext(final NamespaceContext namespaceContext)
    throws XMLStreamException {
    this.namespaceContext = namespaceContext;
  }

  /**
   * Set the prefix for the XML Namespace URI.
   *
   * @param prefix The prefix.
   * @param namespaceUri The XML Namespace URI.
   * @throws XMLStreamException If there was an exception setting the prefix.
   */
  @Override
  public void setPrefix(final String prefix, final String namespaceUri) throws XMLStreamException {
    this.writer.setPrefix(prefix, namespaceUri);
  }

  /**
   * Writes an attribute to the current start tag.
   *
   * @param name The local name of the element.
   * @param value The value of the attribute
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void writeAttribute(final String name, final String value) throws XMLStreamException {
    this.writer.attribute(new QName(name), value);
  }

  /**
   * Writes an attribute to the current start tag.
   *
   * @param name The local name of the element.
   * @param namespaceUri The XML Namespace URI of the element.
   * @param value The value of the attribute
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void writeAttribute(final String namespaceUri, final String name, final String value)
    throws XMLStreamException {
    this.writer.attribute(new QName(namespaceUri, name), value);
  }

  /**
   * Writes an attribute to the current start tag.
   *
   * @param prefix The XML Namespace prefix of the element.
   * @param name The local name of the element.
   * @param namespaceUri The XML Namespace URI of the element.
   * @param value The value of the attribute
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void writeAttribute(final String prefix, final String namespaceUri, final String name,
    final String value) throws XMLStreamException {
    this.writer.attribute(new QName(namespaceUri, name, prefix), value);
  }

  /**
   * Write the contents in a CDATA section. No escaping will be done on the data
   * the text must not contain "]]>".
   *
   * @param text The text to wrap in a CDATA section
   * @throws XMLStreamException If there was a problem writing the text
   */
  @Override
  public void writeCData(final String text) throws XMLStreamException {
    this.writer.cdata(text);
  }

  /**
   * Write a portion of the character buffer to the outpu, escaping special
   * characters.
   *
   * @param buffer The buffer to write.
   * @param offset The starting offset in the buffer.
   * @param length The number of characters to write.
   * @throws XMLStreamException If there was a problem writing the text.
   */
  @Override
  public void writeCharacters(final char[] buffer, final int offset, final int length)
    throws XMLStreamException {
    this.writer.text(buffer, offset, length);
  }

  /**
   * Write the text string to the output, escaping special characters.
   *
   * @param text The text to write
   * @throws XMLStreamException If there was a problem writing the text
   */
  @Override
  public void writeCharacters(final String text) throws XMLStreamException {
    this.writer.text(text);
  }

  /**
   * Write an XML comment. The comment should not contain the string '--'.
   *
   * @param comment The comment to write
   * @throws XMLStreamException If there was a problem writing the comment
   */
  @Override
  public void writeComment(final String comment) throws XMLStreamException {
    this.writer.comment(comment);
  }

  /**
   * Writes a default namespace to the output stream.
   *
   * @param namespaceUri The XML Namespace URI.
   * @throws XMLStreamException If there was a problem writing the namespace.
   */
  @Override
  public void writeDefaultNamespace(final String namespaceUri) throws XMLStreamException {
    setPrefix("", namespaceUri);
  }

  /**
   * Write a DTD section. This string represents the entire doctypedecl
   * production from the XML 1.0 specification.
   *
   * @param dtd The DTD to be written
   * @throws XMLStreamException If there was a problem writing the declaration
   */
  @Override
  public void writeDTD(final String dtd) throws XMLStreamException {
    this.writer.docType(dtd);
  }

  /**
   * Writes an empty tag to the output.
   *
   * @param name The local name of the element.
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void writeEmptyElement(final String name) throws XMLStreamException {
    this.writer.emptyTag(new QName(name));
  }

  /**
   * Writes an empty tag to the output.
   *
   * @param name The local name of the element.
   * @param namespaceUri The XML Namespace URI of the element.
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void writeEmptyElement(final String namespaceUri, final String name)
    throws XMLStreamException {
    this.writer.emptyTag(new QName(namespaceUri, name));
  }

  /**
   * Writes an empty tag to the output.
   *
   * @param prefix The XML Namespace prefix of the element.
   * @param name The local name of the element.
   * @param namespaceUri The XML Namespace URI of the element.
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void writeEmptyElement(final String prefix, final String name, final String namespaceUri)
    throws XMLStreamException {
    this.writer.emptyTag(new QName(namespaceUri, name, prefix));
  }

  /**
   * End a document, closing all tags and flushing the output.
   *
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void writeEndDocument() throws XMLStreamException {
    this.writer.endDocument();
  }

  /**
   * Writes an end tag to the output.
   *
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void writeEndElement() throws XMLStreamException {
    this.writer.endTag();
  }

  /**
   * Write the entity reference with the specified text.
   *
   * @param name The name of the entity
   * @throws XMLStreamException If there was a problem writing the entity
   */
  @Override
  public void writeEntityRef(final String name) throws XMLStreamException {
    this.writer.entityRef(name);
  }

  /**
   * Writes a namespace to the output stream.
   *
   * @param prefix The prefix.
   * @param namespaceUri The XML Namespace URI.
   * @throws XMLStreamException If there was a problem writing the namespace.
   */
  @Override
  public void writeNamespace(final String prefix, final String namespaceUri)
    throws XMLStreamException {
    setPrefix(prefix, namespaceUri);

  }

  /**
   * Write an XML processing instruction.
   *
   * @param target The PI Target (must not be xml)
   * @throws XMLStreamException If there was a problem writing the comment
   */
  @Override
  public void writeProcessingInstruction(final String target) throws XMLStreamException {
    this.writer.processingInstruction(target, null);
  }

  /**
   * Write an XML processing instruction.
   *
   * @param target The PI Target (must not be xml)
   * @param value The value of the processing instruction
   * @throws XMLStreamException If there was a problem writing the comment
   */
  @Override
  public void writeProcessingInstruction(final String target, final String value)
    throws XMLStreamException {
    this.writer.processingInstruction(target, value);
  }

  /**
   * Start a document with an XML Declaration.
   *
   * @throws XMLStreamException If there was a problem writing the XML
   *           Declaration.
   */
  @Override
  public void writeStartDocument() throws XMLStreamException {
    this.writer.startDocument();
  }

  /**
   * Start a document with an XML Declaration.
   *
   * @param encoding The encoding for the XML declaration.
   * @throws XMLStreamException If there was a problem writing the XML
   *           Declaration.
   */
  @Override
  public void writeStartDocument(final String encoding) throws XMLStreamException {
    this.writer.startDocument(encoding);
  }

  /**
   * Start a document with an XML Declaration.
   *
   * @param encoding The encoding for the XML declaration.
   * @param version
   * @throws XMLStreamException If there was a problem writing the XML
   *           Declaration.
   */
  @Override
  public void writeStartDocument(final String encoding, final String version)
    throws XMLStreamException {
    this.writer.startDocument(encoding, version);
  }

  /**
   * Writes a start tag to the output.
   *
   * @param name The local name of the element.
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void writeStartElement(final String name) throws XMLStreamException {
    this.writer.startTag(new QName(name));
  }

  /**
   * Writes a start tag to the output.
   *
   * @param namespaceUri The XML Namespace URI of the element.
   * @param name The local name of the element.
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void writeStartElement(final String namespaceUri, final String name)
    throws XMLStreamException {
    this.writer.startTag(new QName(name));
  }

  /**
   * Writes a start tag to the output.
   *
   * @param prefix The XML Namespace prefix of the element.
   * @param name The local name of the element.
   * @param namespaceUri The XML Namespace URI of the element.
   * @throws XMLStreamException If an exception writing the XML occurs.
   */
  @Override
  public void writeStartElement(final String prefix, final String name, final String namespaceUri)
    throws XMLStreamException {
    this.writer.startTag(new QName(namespaceUri, name, prefix));
  }

}
