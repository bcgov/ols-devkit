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

import java.util.Collection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLReporter;

/**
 * <p>
 * The XmlProcessorContext interface is used by {@link XmlProcessor}
 * implementations to record errors in processing an XML document and to share
 * attributes (objects) between the process methods. The interface also acts as
 * an {@link XMLReporter} for recording errors from the
 * {@link javax.xml.stream.XMLStreamReader}.
 * </p>
 *
 * @author Paul Austin
 */
public interface XmlProcessorContext extends XMLReporter {
  /**
   * Add the error to the list of errors in processing the XML Document.
   *
   * @param message The error message.
   * @param relatedInformation Additional information about the error.
   * @param location The location of the error.
   */
  void addError(final String message, final Object relatedInformation, final Location location);

  /**
   * Add the error to the list of errors in processing the XML Document.
   *
   * @param error The error.
   */
  void addError(final XmlProcessorError error);

  /**
   * Get the value of an attribute.
   *
   * @param name The name of the attribute.
   * @return The attribute value.
   */
  Object getAttribute(final String name);

  /**
   * Get the list of {@link XmlProcessorError}s in processing the XML Document.
   *
   * @return The list of {@link XmlProcessorError}s in processing the XML
   *         Document.
   */
  Collection getErrors();

  /**
   * Set the value of an attribute. Attributes can be used to share objects
   * between the methods
   *
   * @param name The name of the attribute.
   * @param value The value of the attribute.
   */
  void setAttribute(final String name, final Object value);
}
