/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

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
package com.revolsys.record.io.format.saif.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.EnumerationDataType;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.FileUtil;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.saif.SaifConstants;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public class OsnSerializer {
  private static final String ATTRIBUTE_SCOPE = "attribute";

  private static final String COLLECTION_SCOPE = "collection";

  private static final String DATE = "/Date";

  private static final String DOCUMENT_SCOPE = "document";

  private static final String SPATIAL_OBJECT = "/SpatialObject";

  private final OsnConverterRegistry converters;

  private boolean endElement = false;

  private File file;

  private String indent = "";

  private boolean indentEnabled = true;

  private short index = 0;

  private final String lineSeparator;

  private long maxSize = Long.MAX_VALUE;

  private OutputStream out;

  private final String path;

  private final String prefix;

  private final LinkedList<Object> scope = new LinkedList<>();

  private int size = 0;

  public OsnSerializer(final String path, final File file, final long maxSize,
    final OsnConverterRegistry converters) throws IOException {
    this.path = path;
    this.file = file;
    this.maxSize = maxSize;
    this.converters = converters;
    this.prefix = ObjectSetUtil.getObjectSubsetPrefix(file);
    openFile();
    this.scope.addLast(DOCUMENT_SCOPE);
    this.lineSeparator = "\r\n";
  }

  public void attribute(final String name, final double value, final boolean endLine)
    throws IOException {
    attribute(name, new BigDecimal(value), endLine);
  }

  public void attribute(final String name, final Object value, final boolean endLine)
    throws IOException {
    fieldName(name);
    attributeValue(value);
    if (endLine || this.indentEnabled) {
      endLine();
    }

  }

  public void attributeEnum(final String name, final String value, final boolean endLine)
    throws IOException {
    fieldName(name);
    write(value);
    endAttribute();
    if (endLine || this.indentEnabled) {
      endLine();
    }

  }

  public void attributeValue(final Object value) throws IOException {
    serializeValue(value);
    endAttribute();
  }

  public void close() throws IOException {
    while (!this.scope.isEmpty()) {
      final Object scope = this.scope.getLast();
      if (scope == COLLECTION_SCOPE) {
        endCollection();
      } else if (scope == ATTRIBUTE_SCOPE) {
        if (this.indentEnabled) {
          endLine();
        }
        this.scope.removeLast();
      } else if (scope != DOCUMENT_SCOPE && (scope instanceof Record || scope instanceof String)) {
        endObject();
      } else {
        if (this.indentEnabled) {
          endLine();
        }
        this.scope.removeLast();
      }
    }
    write('\n');
    this.out.close();
  }

  private void decreaseIndent() {
    if (this.indentEnabled) {
      this.indent = this.indent.substring(1);
    }
  }

  public void endAttribute() {
    this.scope.removeLast();
  }

  public void endCollection() throws IOException {
    this.endElement = true;
    decreaseIndent();
    serializeIndent();
    write('}');
    if (this.indentEnabled) {
      endLine();
    }
    endAttribute();
  }

  public void endLine() throws IOException {
    write(this.lineSeparator);
  }

  public void endObject() throws IOException {
    this.endElement = true;
    decreaseIndent();
    serializeIndent();
    write(')');
    if (this.indentEnabled) {
      endLine();
    }
    endAttribute();
  }

  public void fieldName(final String name) throws IOException {
    this.endElement = false;
    serializeIndent();
    write(name + ":");
    this.scope.addLast(ATTRIBUTE_SCOPE);
  }

  private void increaseIndent() {
    if (this.indentEnabled) {
      this.indent += '\t';
    }
  }

  public boolean isIndentEnabled() {
    return this.indentEnabled;
  }

  private void openFile() throws IOException {
    Logs.debug(this, "Creating object subset '" + FileUtil.getFileName(this.file) + "'");
    this.out = new BufferedOutputStream(new FileOutputStream(this.file), 4096);
  }

  private void openNextFile() throws IOException {
    this.out.flush();
    this.out.close();
    this.index++;
    final String fileName = ObjectSetUtil.getObjectSubsetName(this.prefix, this.index);
    this.file = new File(this.file.getParentFile(), fileName);
    this.size = 0;
    openFile();
  }

  private void serialize(final Date date) throws IOException {
    startObject(DATE);
    if (date.equals(DateConverter.NULL_DATE)) {
      attribute("year", new BigDecimal(0), false);
    } else {
      final GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(date);
      final int day = cal.get(Calendar.DAY_OF_MONTH);

      if (day < 10) {
        fieldName("day");
        write("0" + day);
        endAttribute();
        endLine();
      } else {
        attribute("day", new BigDecimal(day), true);
      }

      final int month = cal.get(Calendar.MONTH) + 1;
      if (month < 10) {
        fieldName("month");
        write("0" + month);
        endAttribute();
        endLine();
      } else {
        attribute("month", new BigDecimal(month), true);
      }

      final int year = cal.get(Calendar.YEAR);
      attribute("year", new BigDecimal(year), true);
    }
    endObject();
  }

  private void serialize(final Geometry geometry) throws IOException {
    final String type = Property.getSimple(geometry, "osnGeometryType");
    OsnConverter converter = this.converters.getConverter(type);
    if (converter == null) {
      if (geometry instanceof Point) {
        if (converter == null) {
          converter = this.converters.getConverter(SaifConstants.POINT);
        }

      } else if (geometry instanceof LineString) {
        if (converter == null) {
          converter = this.converters.getConverter(SaifConstants.ARC);
        }
      }
    }
    converter.write(this, geometry);
  }

  public void serialize(final List<Object> list) throws IOException {
    serializeCollection("List", list);
  }

  public void serialize(final Record object) throws IOException {
    serializeStartObject(object);
    serializeAttributes(object);
    endObject();
  }

  public void serialize(final Set<Object> set) throws IOException {
    serializeCollection("Set", set);
  }

  public void serialize(final String string) throws IOException {
    write('"');
    String escapedString = string.replaceAll("\\\\", "\\\\\\\\");
    escapedString = escapedString.replaceAll("(\\\\)?\\x22", "\\\\\"");
    write(escapedString);
    write('"');
  }

  public void serializeAttribute(final String name, final Object value) throws IOException {
    fieldName(name);
    if (value instanceof Geometry && name.equals("position")) {
      startObject(SPATIAL_OBJECT);
      fieldName("geometry");
      attributeValue(value);
      endAttribute();
      endObject();
    } else {
      attributeValue(value);
    }
  }

  public void serializeAttributes(final Record object) throws IOException {
    final RecordDefinition type = object.getRecordDefinition();
    final int attributeCount = type.getFieldCount();
    for (int i = 0; i < attributeCount; i++) {
      final Object value = object.getValue(i);
      if (value != null) {
        final String name = type.getFieldName(i);
        final DataType dataType = type.getFieldType(i);
        if (dataType instanceof EnumerationDataType) {
          fieldName(name);
          write(value.toString());
          endAttribute();
        } else {
          serializeAttribute(name, value);
        }
        if (this.indentEnabled) {
          endLine();
        }
        if (!this.endElement) {
          if (i < attributeCount - 1) {
            endLine();
          } else if (type.getName().equals("/Coord3D")) {
            for (final Object parent : this.scope) {
              if (parent instanceof Record) {
                final Record parentObject = (Record)parent;
                if (parentObject.getRecordDefinition()
                  .getName()
                  .equals(SaifConstants.TEXT_ON_CURVE)) {
                  endLine();
                }
              }
            }
          } else {
            endLine();
          }
        }
      }
    }
  }

  private void serializeCollection(final String name, final Collection<Object> collection)
    throws IOException {
    startCollection(name);
    for (final Object value : collection) {
      serializeValue(value);
      if (this.indentEnabled || !this.endElement) {
        endLine();
      }
    }
    endCollection();
  }

  public void serializeIndent() throws IOException {
    if (this.indentEnabled) {
      write(this.indent);
    }
  }

  public void serializeRecord(final Record object) throws IOException {
    if (this.size >= this.maxSize) {
      openNextFile();
      this.size = 0;
    }
    serialize(object);
  }

  public void serializeStartObject(final Record object) throws IOException {
    final RecordDefinition type = object.getRecordDefinition();
    final String path = type.getPath();
    startObject(path);
  }

  @SuppressWarnings("unchecked")
  public void serializeValue(final Object value) throws IOException {
    if (this.scope.getLast() == COLLECTION_SCOPE) {
      serializeIndent();
    }
    if (value == null) {
      write("nil");
    } else {
      if (value instanceof List) {
        serialize((List<Object>)value);
      } else if (value instanceof Set) {
        serialize((Set<Object>)value);
      } else if (value instanceof String) {
        serialize((String)value);
      } else if (value instanceof Record) {
        serialize((Record)value);
      } else if (value instanceof Date) {
        serialize((Date)value);
      } else if (value instanceof Geometry) {
        final Geometry geometry = (Geometry)value;
        serialize(geometry);
      } else {
        write(value.toString());
      }
    }
  }

  public void setIndentEnabled(final boolean indentEnabled) {
    this.indentEnabled = indentEnabled;
  }

  public void startCollection(final String name) throws IOException {
    this.endElement = false;
    write(name);
    write('{');
    if (this.indentEnabled) {
      endLine();
    }
    increaseIndent();
    this.scope.addLast(COLLECTION_SCOPE);
  }

  public void startObject(final String path) throws IOException {
    this.endElement = false;
    final String[] elements = path.replaceAll("^/+", "").split("/");
    if (elements.length == 1) {
      write(elements[0]);
    } else {
      final String typeName = elements[1];
      final String schema = elements[0];
      write(typeName);
      write("::");
      write(schema);
    }
    write('(');
    if (this.indentEnabled) {
      endLine();
    }
    increaseIndent();
    this.scope.addLast(path);
  }

  @Override
  public String toString() {
    return this.path.toString();
  }

  public void write(final byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  public void write(final byte[] b, final int off, final int len) throws IOException {
    this.out.write(b, off, len);
    this.size += len;
  }

  public void write(final int b) throws IOException {
    this.out.write(b);
    this.size += 1;
  }

  public void write(final String s) throws IOException {
    final byte[] bytes = s.getBytes();
    write(bytes, 0, bytes.length);
  }
}
