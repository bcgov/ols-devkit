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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Stack;
import java.util.zip.ZipFile;

import com.revolsys.gis.parser.ParseException;

public class OsnIterator implements Iterator<Object> {
  public static final Object BOOLEAN_VALUE = 9;

  public static final Object END_DOCUMENT = 1;

  public static final Object END_LIST = 12;

  public static final Object END_OBJECT = 3;

  public static final Object END_RELATION = 16;

  public static final Object END_SET = 14;

  public static final Object ENUM_TAG = 8;

  private static final Object IN_ATTRIBUTE = "attribute";

  private static final Object IN_DOCUMENT = "document";

  private static final Object IN_LIST = "list";

  private static final Object IN_OBJECT = "object";

  private static final Object IN_RELATION = "relation";

  private static final Object IN_SET = "set";

  private static final boolean[] IS_DIGIT_CHARACTER = new boolean[256];

  private static final boolean[] IS_LOWER_CASE_CHARACTER = new boolean[256];

  private static final boolean[] IS_NAME_CHARACTER = new boolean[256];

  private static final boolean[] IS_NUMBER_CHARACTER = new boolean[256];

  private static final boolean[] IS_UPPER_CASE_CHARACTER = new boolean[256];

  private static final boolean[] IS_WHITESPACE_CHARACTER = new boolean[256];

  public static final Object NULL_VALUE = 10;

  public static final Object NUMERIC_VALUE = 5;

  public static final Object START_ATTRIBUTE = 4;

  public static final Object START_DEFINITION = 2;

  public static final Object START_DOCUMENT = 0;

  public static final Object START_LIST = 11;

  public static final Object START_RELATION = 15;

  public static final Object START_SET = 13;

  public static final Object TEXT_VALUE = 7;

  public static final Object UNKNOWN = -1;

  static {
    for (int c = 'a'; c <= 'z'; c++) {
      IS_LOWER_CASE_CHARACTER[c] = true;
      IS_NAME_CHARACTER[c] = true;
    }
    for (int c = 'A'; c <= 'Z'; c++) {
      IS_UPPER_CASE_CHARACTER[c] = true;
      IS_NAME_CHARACTER[c] = true;
    }
    for (int c = '0'; c <= '9'; c++) {
      IS_DIGIT_CHARACTER[c] = true;
      IS_NAME_CHARACTER[c] = true;
      IS_NUMBER_CHARACTER[c] = true;
    }
    IS_NUMBER_CHARACTER['+'] = true;
    IS_NUMBER_CHARACTER['-'] = true;
    IS_NUMBER_CHARACTER['.'] = true;
    IS_NAME_CHARACTER['_'] = true;
    IS_WHITESPACE_CHARACTER[0] = true;
    IS_WHITESPACE_CHARACTER[' '] = true;
    IS_WHITESPACE_CHARACTER['\t'] = true;
    IS_WHITESPACE_CHARACTER['\n'] = true;
    IS_WHITESPACE_CHARACTER['\r'] = true;
    IS_WHITESPACE_CHARACTER['/'] = true;
  }

  private final byte[] buffer = new byte[4096];

  private int bufferIndex = 0;

  private int bufferLength = 0;

  private int columnNumber = 1;

  // private StringBuilder buffer = new StringBuilder();

  private int currentCharacter;

  private int currentColumnNumber = 0;

  private int currentLineNumber = 1;

  private Object eventType = START_DOCUMENT;

  private final String fileName;

  private final InputStream in;

  private int lineNumber = 0;

  private final Stack<Object> scopeStack = new Stack<>();

  private Object value;

  public OsnIterator(final File directory, final String fileName) throws IOException {
    this(fileName, new ObjectSetInputStream(directory, fileName));
  }

  public OsnIterator(final String fileName, final InputStream in) throws IOException {
    this.in = new BufferedInputStream(in);
    this.fileName = fileName;
    this.scopeStack.push(IN_DOCUMENT);
  }

  public OsnIterator(final ZipFile zipFile, final String fileName) throws IOException {
    this(fileName, new ObjectSetInputStream(zipFile, fileName));
  }

  private void checkStartCollection(final String name) throws IOException {
    skipWhitespace();
    if (!isNextCharacter('{')) {
      throw new IllegalStateException("Expecting a '{' to start a " + name);
    }
  }

  private Object checkStartObject() throws IOException {
    skipWhitespace();
    if (isNextCharacter('(')) {
      this.scopeStack.push(IN_OBJECT);
      return START_DEFINITION;
    } else {
      return UNKNOWN;
    }
  }

  public void close() throws IOException {
    this.in.close();
  }

  private String findClassName() throws IOException {
    final String className = findUpperName(true);
    // If the class name is fullowed by '::' get and return the schema name
    if (this.currentCharacter == ':' && getNextCharacter() == ':') {
      getNextCharacter();
      final String schemaName = findUpperName(false);
      return (className + "::" + schemaName).intern();
    } else {
      return className;
    }
  }

  private Object findEndCollection() throws IOException {
    if (isNextCharacter('}')) {
      final Object scope = this.scopeStack.pop();
      if (scope == IN_LIST) {
        return END_LIST;
      } else if (scope == IN_SET) {
        return END_SET;
      } else if (scope == IN_RELATION) {
        return END_RELATION;
      } else {
        return UNKNOWN;
      }
    } else {
      return UNKNOWN;
    }
  }

  private Object findEndObject() throws IOException {
    if (isNextCharacter(')')) {
      this.scopeStack.pop();
      return END_OBJECT;
    } else {
      return UNKNOWN;
    }
  }

  private Object findExpression() throws IOException {
    Object eventType = UNKNOWN;
    final int c = this.currentCharacter;
    if (IS_NUMBER_CHARACTER[c]) {
      eventType = processDigitString();
    } else if (c == '"') {
      eventType = processTextString();
    } else if (IS_LOWER_CASE_CHARACTER[c]) {
      final String name = findLowerName(true);
      if (name.equals("true")) {
        this.value = Boolean.TRUE;
        eventType = BOOLEAN_VALUE;
      } else if (name.equals("false")) {
        this.value = Boolean.FALSE;
        eventType = BOOLEAN_VALUE;
      } else if (name.equals("nil")) {
        this.value = null;
        eventType = NULL_VALUE;
      } else {
        this.value = name;
        eventType = ENUM_TAG;
      }
    } else if (IS_UPPER_CASE_CHARACTER[c]) {
      final String name = findClassName();
      if (name.equals("List")) {
        checkStartCollection(name);
        eventType = START_LIST;
        this.scopeStack.push(IN_LIST);
      } else if (name.equals("Set")) {
        checkStartCollection(name);
        eventType = START_SET;
        this.scopeStack.push(IN_SET);
      } else if (name.equals("Relation")) {
        checkStartCollection(name);
        eventType = START_RELATION;
        this.scopeStack.push(IN_RELATION);
      } else {
        this.value = name;
        eventType = checkStartObject();
        if (eventType == UNKNOWN) {
          throwParseError("Expecting a '('");
        }
      }
    }
    return eventType;
  }

  private Object findFieldName() throws IOException {
    this.value = findLowerName(true);
    if (this.value == null) {
      return UNKNOWN;
    } else {
      skipWhitespace();
      if (isNextCharacter(':')) {
        this.scopeStack.push(IN_ATTRIBUTE);
        return START_ATTRIBUTE;
      } else {
        return UNKNOWN;
      }
    }
  }

  private String findLowerName(final boolean tokenStart) throws IOException {
    if (IS_LOWER_CASE_CHARACTER[this.currentCharacter]) {
      return findName(tokenStart);
    } else {
      return null;
    }
  }

  private String findName(final boolean tokenStart) throws IOException {
    if (tokenStart) {
      this.lineNumber = this.currentLineNumber;
      this.columnNumber = this.currentColumnNumber;
    }
    final StringBuilder name = new StringBuilder();
    int c = this.currentCharacter;
    while (c != -1 && IS_NAME_CHARACTER[c]) {
      name.append((char)c);
      c = getNextCharacter();
    }
    return name.toString().intern();
  }

  private Object findStartObject() throws IOException {
    this.value = findClassName();
    if (this.value == null) {
      return UNKNOWN;
    } else {
      return checkStartObject();
    }
  }

  private String findUpperName(final boolean tokenStart) throws IOException {
    if (IS_UPPER_CASE_CHARACTER[this.currentCharacter]) {
      return findName(tokenStart);
    } else {
      return null;
    }
  }

  public Boolean getBooleanValue() {
    if (this.value == null) {
      return null;
    } else if (this.value instanceof Boolean) {
      return (Boolean)this.value;

    } else {
      return Boolean.valueOf(this.value.toString());
    }
  }

  public double getDoubleValue() {
    return ((BigDecimal)this.value).doubleValue();
  }

  public Object getEventType() {
    return this.eventType;
  }

  public float getFloatValue() {
    return ((BigDecimal)this.value).floatValue();
  }

  public int getIntegerValue() {
    return ((BigDecimal)this.value).intValue();
  }

  private int getNextCharacter() {
    if (this.bufferIndex == this.bufferLength) {
      try {
        this.bufferLength = this.in.read(this.buffer);
      } catch (final IOException e) {
        return -1;
      }
      if (this.bufferLength == -1) {
        return -1;
      } else {
        this.bufferIndex = 0;
      }
    }
    this.currentCharacter = this.buffer[this.bufferIndex];
    this.bufferIndex++;

    // currentCharacter = in.read();
    // line.append((char)currentCharacter);
    this.currentColumnNumber++;
    return this.currentCharacter;
  }

  public String getPathValue() {
    final String name = getStringValue();
    if (name != null) {
      return PathCache.getName(name);
    } else {
      return null;
    }
  }

  public String getStringValue() {
    if (this.value != null) {
      return this.value.toString();
    }
    return null;
  }

  public Object getValue() {
    return this.value;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  private boolean isNextCharacter(final int c) throws IOException {
    if (this.currentCharacter == c) {
      getNextCharacter();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Object next() {
    try {
      if (skipWhitespace() == -1) {
        return END_DOCUMENT;
      }
      this.eventType = UNKNOWN;
      this.value = null;
      final Object scope = this.scopeStack.peek();
      if (scope == IN_DOCUMENT) {
        processDocument();
      } else if (scope == IN_OBJECT) {
        processObject();
      } else if (scope == IN_ATTRIBUTE) {
        processAttribute();
      } else if (scope == IN_LIST) {
        processList();
      } else if (scope == IN_SET) {
        this.eventType = findExpression();
        processSet();
      } else if (scope == IN_RELATION) {
        processRelation();
      }
      return this.eventType;
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public Boolean nextBooleanAttribute(final String name) {
    final String fieldName = nextFieldName();
    if (fieldName == null || !fieldName.equals(name)) {
      throwParseError("Expecting attribute " + name);
    }
    return nextBooleanValue();
  }

  public Boolean nextBooleanValue() {
    if (this.eventType != OsnIterator.BOOLEAN_VALUE) {
      if (this.eventType == END_OBJECT) {
        return null;
      } else if (next() != OsnIterator.BOOLEAN_VALUE) {
        throwParseError("Excepecting an boolean value");
      }
    }
    return getBooleanValue();
  }

  public double nextDoubleAttribute(final String name) {
    final String fieldName = nextFieldName();
    if (fieldName == null || !fieldName.equals(name)) {
      throwParseError("Expecting attribute " + name);
    }
    return nextDoubleValue();
  }

  public double nextDoubleValue() {
    if (this.eventType != OsnIterator.NUMERIC_VALUE) {
      if (this.eventType == END_OBJECT) {
        return 0;
      } else if (next() != OsnIterator.NUMERIC_VALUE) {
        throwParseError("Excepecting an numeric value");
      }
    }
    return getDoubleValue();
  }

  public void nextEndObject() {
    if (next() != OsnIterator.END_OBJECT) {
      throwParseError("Expecting End Object");
    }
  }

  public String nextFieldName() {
    Object currentEventType = getEventType();
    if (currentEventType != OsnIterator.START_ATTRIBUTE) {
      currentEventType = next();
      if (currentEventType == OsnIterator.END_OBJECT) {
        return null;
      } else if (currentEventType != OsnIterator.START_ATTRIBUTE) {
        throwParseError("Excepecting an attribute name");
      }
    }
    return getStringValue();
  }

  public int nextIntValue() {
    if (this.eventType != OsnIterator.NUMERIC_VALUE) {
      if (this.eventType == END_OBJECT) {
        return 0;
      } else if (next() != OsnIterator.NUMERIC_VALUE) {
        throwParseError("Excepecting an numeric value");
      }
    }
    return getIntegerValue();
  }

  public String nextObjectName() {
    Object currentEventType = getEventType();
    if (currentEventType != OsnIterator.START_DEFINITION) {
      if (currentEventType == END_OBJECT) {
        return null;
      } else {
        currentEventType = next();
        if (currentEventType == OsnIterator.END_OBJECT) {
          return null;
        } else if (currentEventType != OsnIterator.START_DEFINITION) {
          throwParseError("Excepecting an attribute name");
        }
      }
    }
    return getPathValue();
  }

  public String nextStringAttribute(final String name) {
    final String fieldName = nextFieldName();
    if (fieldName == null) {
      return null;
    } else if (!fieldName.equals(name)) {
      throwParseError("Expecting attribute " + name);
    }
    return nextStringValue();
  }

  public String nextStringValue() {
    if (this.eventType != OsnIterator.TEXT_VALUE && this.eventType != OsnIterator.ENUM_TAG) {
      if (this.eventType == END_OBJECT) {
        return null;
      } else if (next() != OsnIterator.TEXT_VALUE && this.eventType != OsnIterator.ENUM_TAG) {
        throwParseError("Excepecting an text value");
      }
    }
    return getStringValue();
  }

  public Object nextValue() {
    if (this.eventType != OsnIterator.BOOLEAN_VALUE && this.eventType != OsnIterator.NUMERIC_VALUE
      && this.eventType != OsnIterator.TEXT_VALUE && this.eventType != OsnIterator.ENUM_TAG) {
      if (this.eventType == END_OBJECT) {
        return null;
      } else if (next() != OsnIterator.TEXT_VALUE && this.eventType != OsnIterator.NUMERIC_VALUE
        && this.eventType != OsnIterator.BOOLEAN_VALUE && this.eventType != OsnIterator.ENUM_TAG) {
        throwParseError("Excepecting a value");
      }
    }
    return getValue();
  }

  private void processAttribute() throws IOException {
    this.scopeStack.pop();
    this.eventType = findExpression();
    if (this.eventType == UNKNOWN) {
      throwParseError("Expecting an expression");
    }
  }

  private Object processDigitString() throws IOException {
    final StringBuilder number = new StringBuilder();
    int c = this.currentCharacter;
    while (IS_NUMBER_CHARACTER[(char)c]) {
      number.append((char)c);
      c = getNextCharacter();
    }
    if (number.length() > 0) {
      setNextToken(new BigDecimal(number.toString()));
      this.eventType = NUMERIC_VALUE;
    }
    return this.eventType;
  }

  private void processDocument() throws IOException {
    this.eventType = findStartObject();
    if (this.eventType == UNKNOWN) {
      throwParseError("Expecting start of an object definition");
    }
  }

  private void processList() throws IOException {
    this.eventType = findExpression();
    if (this.eventType == UNKNOWN) {
      this.eventType = findEndCollection();
      if (this.eventType == UNKNOWN) {
        throwParseError("Expecting an expression or end of a list");
      }
    }
  }

  private void processObject() throws IOException {
    skipWhitespace();
    this.eventType = findFieldName();
    if (this.eventType == UNKNOWN) {
      skipWhitespace();
      this.eventType = findEndObject();
      if (this.eventType == UNKNOWN) {
        throwParseError("Expecting start of an attribute definition or end of object definition");
      }
    }
  }

  private void processRelation() throws IOException {
    this.eventType = findStartObject();
    if (this.eventType == UNKNOWN) {
      this.eventType = findEndCollection();
      if (this.eventType == UNKNOWN) {
        throwParseError("Expecting an expression or end of a relation");
      }
    }
  }

  private void processSet() throws IOException {
    if (this.eventType == UNKNOWN) {
      this.eventType = findEndCollection();
      if (this.eventType == UNKNOWN) {
        throwParseError("Expecting an expression or end of a set");
      }
    }
  }

  private Object processTextString() throws IOException {
    this.lineNumber = this.currentLineNumber;
    this.columnNumber = this.currentColumnNumber;

    final StringBuilder text = new StringBuilder();
    char c = (char)getNextCharacter();
    while (c != '"') {
      if (c == '\\') {
        text.append((char)getNextCharacter());
      } else {
        text.append(c);
      }
      c = (char)getNextCharacter();
    }

    if (text.length() > 0 && text.charAt(text.length() - 1) == '\n') {
      text.deleteCharAt(text.length() - 1);
    }
    final String string = text.toString();
    setNextToken(string);
    this.eventType = TEXT_VALUE;
    getNextCharacter();
    return this.eventType;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  private void setNextToken(final Object token) {
    this.value = token;
  }

  public int skipWhitespace() {
    int c = this.currentCharacter;
    while (c != -1 && IS_WHITESPACE_CHARACTER[c]) {
      if (c == '\n') {
        // line.setLength(0);
        this.currentLineNumber++;
        this.currentColumnNumber = 1;
        c = getNextCharacter();
      } else if (c == '/') {
        c = getNextCharacter();
        if (c == '/') {
          do {
            c = getNextCharacter();
          } while (c != -1 && c != '\n');
        } else {
          return this.currentCharacter;
        }
      } else {
        c = getNextCharacter();
      }
    }
    return c;
  }

  public void throwParseError(final String message) {
    final int startIndex = Math.max(this.bufferIndex - 40, 0);
    final int endIndex = Math.min(80, this.bufferLength - 1 - startIndex);
    throw new ParseException(toString(), message + " got '" + (char)this.currentCharacter
      + "' context=" + new String(this.buffer, startIndex, endIndex));
  }

  @Override
  public String toString() {
    return this.fileName + "[" + this.lineNumber + "," + this.columnNumber + "]";
  }
}
