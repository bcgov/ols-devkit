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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.Location;

/**
 * <p>
 * The SimpleXmlProcessorContext class is a simple implementation of
 * {@link XmlProcessorContext} that can be used by {@link XmlProcessor}
 * implementations to record errors in processing an XML document and to share
 * attributes (objects) between the process methods.
 * </p>
 *
 * @author Paul Austin
 */
public class SimpleXmlProcessorContext implements XmlProcessorContext {
  /** The map of attribute names to values. */
  private final Map attributes = new HashMap();

  /** The list of errors. */
  private final List errors = new ArrayList();

  /**
   * Add the error to the list of errors in processing the XML Document.
   *
   * @param message The error message.
   * @param relatedInformation Additional information about the error.
   * @param location The location of the error.
   */
  @Override
  public void addError(final String message, final Object relatedInformation,
    final Location location) {
    addError(new XmlProcessorError(message, relatedInformation, location));
  }

  /**
   * Add the error to the list of errors in processing the XML Document.
   *
   * @param error The error.
   */
  @Override
  public void addError(final XmlProcessorError error) {
    this.errors.add(error);
  }

  /**
   * Get the value of an attribute.
   *
   * @param name The name of the attribute.
   * @return The attribute value.
   */
  @Override
  public Object getAttribute(final String name) {
    return this.attributes.get(name);
  }

  /**
   * Get the list of {@link XmlProcessorError}s in processing the XML Document.
   *
   * @return The list of {@link XmlProcessorError}s in processing the XML
   *         Document.
   */
  @Override
  public Collection getErrors() {
    return this.errors;
  }

  /**
   * Add the error to the list of errors in processing the XML Document.
   *
   * @param message The error message.
   * @param errorType The type of error.
   * @param relatedInformation Additional information about the error.
   * @param location The location of the error.
   */
  @Override
  public void report(final String message, final String errorType, final Object relatedInformation,
    final Location location) {
    addError(new XmlProcessorError(message, relatedInformation, location));
  }

  /**
   * Set the value of an attribute. Attributes can be used to share objects
   * between the methods
   *
   * @param name The name of the attribute.
   * @param value The value of the attribute.
   */
  @Override
  public void setAttribute(final String name, final Object value) {
    this.attributes.put(name, value);
  }
}
