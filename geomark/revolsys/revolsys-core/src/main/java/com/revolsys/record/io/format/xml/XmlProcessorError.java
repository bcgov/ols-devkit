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

import javax.xml.stream.Location;

/**
 * The XmlProcessorError class is used to record the information about an error
 * in processing an XML document by the {@link XmlProcessor} class.
 *
 * @author Paul Austin
 */
public class XmlProcessorError {
  /** The exception that caused the error. */
  private Throwable exception;

  /** The location of the error. */
  private final Location location;

  /** The error message. */
  private final String message;

  /** Additional information about the error. */
  private final Object relatedInformation;

  /**
   * Construct a new XmlProcessorError.
   *
   * @param message The error message.
   * @param relatedInformation Additional information about the error.
   * @param location The location of the error.
   */
  public XmlProcessorError(final String message, final Object relatedInformation,
    final Location location) {
    this.message = message;
    this.relatedInformation = relatedInformation;
    if (relatedInformation instanceof Throwable) {
      this.exception = (Throwable)relatedInformation;
    }
    this.location = location;
  }

  /**
   * Get the exception that caused the error. This method will return the
   * relatedInformation if it is an instance of Throwable.
   *
   * @return The exception that caused the error.
   */
  public final Throwable getException() {
    return this.exception;
  }

  /**
   * Get the location of the error.
   *
   * @return The location of the error.
   */
  public final Location getLocation() {
    return this.location;
  }

  /**
   * Get the error message.
   *
   * @return The error message.
   */
  public final String getMessage() {
    return this.message;
  }

  /**
   * Get additional information about the error.
   *
   * @return Additional information about the error.
   */
  public final Object getRelatedInformation() {
    return this.relatedInformation;
  }

  /**
   * Get the string representation of the error.
   *
   * @return The string representation of the error.
   */
  @Override
  public String toString() {
    if (this.location != null) {
      return "[" + this.location.getLineNumber() + ", " + this.location.getColumnNumber() + "] "
        + this.message;
    } else {
      return this.message;

    }
  }
}
