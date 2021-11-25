/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.wkb;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.impl.PointDouble;

/**
 * Converts a geometry in Well-Known Text format to a {@link Geometry}.
 * <p>
 * <code>WKTReader</code> supports
 * extracting <code>Geometry</code> objects from either {@link Reader}s or
 *  {@link String}s. This allows it to function as a parser to read <code>Geometry</code>
 *  objects from text blocks embedded in other data formats (e.g. XML). <P>
 * <p>
 *  A <code>WKTReader</code> is parameterized by a <code>GeometryFactory</code>,
 *  to allow it to create <code>Geometry</code> objects of the appropriate
 *  implementation. In particular, the <code>GeometryFactory</code>
 *  determines the <code>PrecisionModel</code> and <code>SRID</code> that is
 *  used. <P>
 *
 *  The <code>WKTReader</code> converts all input numbers to the precise
 *  internal representation.
 *
 * <h3>Notes:</h3>
 * <ul>
 * <li>Keywords are case-insensitive.
 * <li>The reader supports non-standard "LINEARRING" tags.
 * <li>The reader uses <tt>Double.parseDouble</tt> to perform the conversion of ASCII
 * numbers to floating point.  This means it supports the Java
 * syntax for floating point literals (including scientific notation).
 * </ul>
 *
 * <h3>Syntax</h3>
 * The following syntax specification describes the version of Well-Known Text
 * supported by JTS.
 * (The specification uses a syntax language similar to that used in
 * the C and Java language specifications.)
 * <p>
 *
 * <blockquote><pre>
 * <i>WKTGeometry:</i> one of<i>
 *
 *       WKTPoint  WKTLineString  WKTLinearRing  WKTPolygon
 *       WKTMultiPoint  WKTMultiLineString  WKTMultiPolygon
 *       WKTGeometryCollection</i>
 *
 * <i>WKTPoint:</i> <b>POINT ( </b><i>Coordinate</i> <b>)</b>
 *
 * <i>WKTLineString:</i> <b>LINESTRING</b> <i>LineString</i>
 *
 * <i>WKTLinearRing:</i> <b>LINEARRING</b> <i>LineString</i>
 *
 * <i>WKTPolygon:</i> <b>POLYGON</b> <i>CoordinateSequenceList</i>
 *
 * <i>WKTMultiPoint:</i> <b>MULTIPOINT</b> <i>CoordinateSingletonList</i>
 *
 * <i>WKTMultiLineString:</i> <b>MULTILINESTRING</b> <i>CoordinateSequenceList</i>
 *
 * <i>WKTMultiPolygon:</i>
 *         <b>MULTIPOLYGON (</b> <i>CoordinateSequenceList {</i> , <i>CoordinateSequenceList }</i> <b>)</b>
 *
 * <i>WKTGeometryCollection: </i>
 *         <b>GEOMETRYCOLLECTION (</b> <i>WKTGeometry {</i> , <i>WKTGeometry }</i> <b>)</b>
 *
 * <i>CoordinateSingletonList:</i>
 *         <b>(</b> <i>CoordinateSingleton {</i> <b>,</b> <i>CoordinateSingleton }</i> <b>)</b>
 *         | <b>EMPTY</b>
 *
 * <i>CoordinateSingleton:</i>
 *         <b>(</b> <i>Coordinate <b>)</b>
 *         | <b>EMPTY</b>
 *
 * <i>CoordinateSequenceList:</i>
 *         <b>(</b> <i>LineString {</i> <b>,</b> <i>LineString }</i> <b>)</b>
 *         | <b>EMPTY</b>
 *
 * <i>LineString:</i>
 *         <b>(</b> <i>Coordinate {</i> , <i>Coordinate }</i> <b>)</b>
 *         | <b>EMPTY</b>
 *
 * <i>Coordinate:
 *         Number Number Number<sub>opt</sub></i>
 *
 * <i>Number:</i> A Java-style floating-point number (including <tt>NaN</tt>, with arbitrary case)
 *
 * </pre></blockquote>
 *
 *
 *@version 1.7
 */
public class WKTReader {
  private static final boolean ALLOW_OLD_JTS_MULTIPOINT_SYNTAX = true;

  private static final String COMMA = ",";

  private static final String EMPTY = "EMPTY";

  private static final String L_PAREN = "(";

  private static final String NAN_SYMBOL = "NaN";

  private static final String R_PAREN = ")";

  private final GeometryFactory geometryFactory;

  private StreamTokenizer tokenizer;

  /**
   * Creates a reader that creates objects using the default {@link GeometryFactory}.
   */
  public WKTReader() {
    this(GeometryFactory.DEFAULT_3D);
  }

  /**
   *  Creates a reader that creates objects using the given
   *  {@link GeometryFactory}.
   *
   *@param  geometryFactory  the factory used to create <code>Geometry</code>s.
   */
  public WKTReader(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  /**
   * Returns the next array of <code>Coordinate</code>s in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next element returned by the stream should be L_PAREN (the
   *      beginning of "(x1 y1, x2 y2, ..., xn yn)") or EMPTY.
   *@return                  the next array of <code>Coordinate</code>s in the
   *      stream, or an empty array if EMPTY is the next element returned by
   *      the stream.
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private Point[] getCoordinates() throws IOException, ParseException {
    String nextToken = getNextEmptyOrOpener();
    if (nextToken.equals(EMPTY)) {
      return new Point[] {};
    }
    final List<Point> coordinates = new ArrayList<>();
    coordinates.add(getPreciseCoordinate());
    nextToken = getNextCloserOrComma();
    while (nextToken.equals(COMMA)) {
      coordinates.add(getPreciseCoordinate());
      nextToken = getNextCloserOrComma();
    }
    final Point[] array = new Point[coordinates.size()];
    return coordinates.toArray(array);
  }

  private Point[] getCoordinatesNoLeftParen() throws IOException, ParseException {
    String nextToken = null;
    final ArrayList coordinates = new ArrayList();
    coordinates.add(getPreciseCoordinate());
    nextToken = getNextCloserOrComma();
    while (nextToken.equals(COMMA)) {
      coordinates.add(getPreciseCoordinate());
      nextToken = getNextCloserOrComma();
    }
    final Point[] array = new Point[coordinates.size()];
    return (Point[])coordinates.toArray(array);
  }

  /**
   *  Returns the next R_PAREN in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next token must be R_PAREN.
   *@return                  the next R_PAREN in the stream
   *@throws  ParseException  if the next token is not R_PAREN
   *@throws  IOException     if an I/O error occurs
   */
  private String getNextCloser() throws IOException, ParseException {
    final String nextWord = getNextWord();
    if (nextWord.equals(R_PAREN)) {
      return nextWord;
    }
    parseErrorExpected(R_PAREN);
    return null;
  }

  /**
   *  Returns the next R_PAREN or COMMA in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next token must be R_PAREN or COMMA.
   *@return                  the next R_PAREN or COMMA in the stream
   *@throws  ParseException  if the next token is not R_PAREN or COMMA
   *@throws  IOException     if an I/O error occurs
   */
  private String getNextCloserOrComma() throws IOException, ParseException {
    final String nextWord = getNextWord();
    if (nextWord.equals(COMMA) || nextWord.equals(R_PAREN)) {
      return nextWord;
    }
    parseErrorExpected(COMMA + " or " + R_PAREN);
    return null;
  }

  /**
   *  Returns the next EMPTY or L_PAREN in the stream as uppercase text.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next token must be EMPTY or L_PAREN.
   *@return                  the next EMPTY or L_PAREN in the stream as uppercase
   *      text.
   *@throws  ParseException  if the next token is not EMPTY or L_PAREN
   *@throws  IOException     if an I/O error occurs
   */
  private String getNextEmptyOrOpener() throws IOException, ParseException {
    final String nextWord = getNextWord();
    if (nextWord.equals(EMPTY) || nextWord.equals(L_PAREN)) {
      return nextWord;
    }
    parseErrorExpected(EMPTY + " or " + L_PAREN);
    return null;
  }

  /**
   * Parses the next number in the stream.
   * Numbers with exponents are handled.
   * <tt>NaN</tt> values are handled correctly, and
   * the case of the "NaN" symbol is not significant.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next token must be a number.
   *@return                  the next number in the stream
   *@throws  ParseException  if the next token is not a valid number
   *@throws  IOException     if an I/O error occurs
   */
  private double getNextNumber() throws IOException, ParseException {
    final int type = this.tokenizer.nextToken();
    switch (type) {
      case StreamTokenizer.TT_WORD: {
        if (this.tokenizer.sval.equalsIgnoreCase(NAN_SYMBOL)) {
          return Double.NaN;
        } else {
          try {
            return Double.parseDouble(this.tokenizer.sval);
          } catch (final NumberFormatException ex) {
            parseErrorWithLine("Invalid number: " + this.tokenizer.sval);
          }
        }
      }
    }
    parseErrorExpected("number");
    return 0.0;
  }

  /**
   *  Returns the next word in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next token must be a word.
   *@return                  the next word in the stream as uppercase text
   *@throws  ParseException  if the next token is not a word
   *@throws  IOException     if an I/O error occurs
   */
  private String getNextWord() throws IOException, ParseException {
    final int type = this.tokenizer.nextToken();
    switch (type) {
      case StreamTokenizer.TT_WORD:

        final String word = this.tokenizer.sval;
        if (word.equalsIgnoreCase(EMPTY)) {
          return EMPTY;
        }
        return word;

      case '(':
        return L_PAREN;
      case ')':
        return R_PAREN;
      case ',':
        return COMMA;
    }
    parseErrorExpected("word");
    return null;
  }

  private Point getPreciseCoordinate() throws IOException, ParseException {
    final double x = this.geometryFactory.makePrecise(0, getNextNumber());
    final double y = this.geometryFactory.makePrecise(1, getNextNumber());
    final Point coord;
    if (isNumberNext()) {
      final double z = getNextNumber();
      coord = new PointDouble(x, y, z);
    } else {
      coord = new PointDouble(x, y);
    }
    return coord;
  }

  private boolean isNumberNext() throws IOException {
    final int type = this.tokenizer.nextToken();
    this.tokenizer.pushBack();
    return type == StreamTokenizer.TT_WORD;
  }

  /**
   *  Returns the next word in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next token must be a word.
   *@return                  the next word in the stream as uppercase text
   *@throws  ParseException  if the next token is not a word
   *@throws  IOException     if an I/O error occurs
   */
  private String lookaheadWord() throws IOException, ParseException {
    final String nextWord = getNextWord();
    this.tokenizer.pushBack();
    return nextWord;
  }

  /**
   * Throws a formatted ParseException reporting that the current token
   * was unexpected.
   *
   * @param expected a description of what was expected
   * @throws ParseException
   */
  private void parseErrorExpected(final String expected) throws ParseException {
    // throws Asserts for tokens that should never be seen
    if (this.tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
      throw new IllegalStateException("Unexpected NUMBER token");
    }
    if (this.tokenizer.ttype == StreamTokenizer.TT_EOL) {
      throw new IllegalStateException("Unexpected EOL token");
    }

    final String tokenStr = tokenString();
    parseErrorWithLine("Expected " + expected + " but found " + tokenStr);
  }

  private void parseErrorWithLine(final String msg) throws ParseException {
    throw new ParseException(msg + " (line " + this.tokenizer.lineno() + ")");
  }

  /**
   * Reads a Well-Known Text representation of a {@link Geometry}
   * from a {@link Reader}.
   *
   *@param  reader           a Reader which will return a <Geometry Tagged Text>
   *      string (see the OpenGIS Simple Features Specification)
   *@return                  a <code>Geometry</code> read from <code>reader</code>
   *@throws  ParseException  if a parsing problem occurs
   */
  public Geometry read(final Reader reader) throws ParseException {
    this.tokenizer = new StreamTokenizer(reader);
    // set tokenizer to NOT parse numbers
    this.tokenizer.resetSyntax();
    this.tokenizer.wordChars('a', 'z');
    this.tokenizer.wordChars('A', 'Z');
    this.tokenizer.wordChars(128 + 32, 255);
    this.tokenizer.wordChars('0', '9');
    this.tokenizer.wordChars('-', '-');
    this.tokenizer.wordChars('+', '+');
    this.tokenizer.wordChars('.', '.');
    this.tokenizer.whitespaceChars(0, ' ');
    this.tokenizer.commentChar('#');

    try {
      return readGeometryTaggedText();
    } catch (final IOException e) {
      throw new ParseException(e.toString());
    }
  }

  /**
   * Reads a Well-Known Text representation of a {@link Geometry}
   * from a {@link String}.
   *
   * @param wellKnownText
   *            one or more <Geometry Tagged Text>strings (see the OpenGIS
   *            Simple Features Specification) separated by whitespace
   * @return a <code>Geometry</code> specified by <code>wellKnownText</code>
   * @throws ParseException
   *             if a parsing problem occurs
   */
  public Geometry read(final String wellKnownText) throws ParseException {
    final StringReader reader = new StringReader(wellKnownText);
    try {
      return read(reader);
    } finally {
      reader.close();
    }
  }

  /**
   *  Creates a <code>GeometryCollection</code> using the next token in the
   *  stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;GeometryCollection Text&gt;.
   *@return                  a <code>GeometryCollection</code> specified by the
   *      next token in the stream
   *@throws  ParseException  if the coordinates used to Construct a new <code>Polygon</code>
   *      shell and holes do not form closed linestrings, or if an unexpected
   *      token was encountered
   *@throws  IOException     if an I/O error occurs
   */
  private Geometry readGeometryCollectionText() throws IOException, ParseException {
    String nextToken = getNextEmptyOrOpener();
    if (nextToken.equals(EMPTY)) {
      return this.geometryFactory.geometryCollection();
    } else {
      final List<Geometry> geometries = new ArrayList<>();
      Geometry geometry = readGeometryTaggedText();
      geometries.add(geometry);
      nextToken = getNextCloserOrComma();
      while (nextToken.equals(COMMA)) {
        geometry = readGeometryTaggedText();
        geometries.add(geometry);
        nextToken = getNextCloserOrComma();
      }
      return this.geometryFactory.geometry(geometries);
    }
  }

  /**
   *  Creates a <code>Geometry</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;Geometry Tagged Text&gt;.
   *@return                  a <code>Geometry</code> specified by the next token
   *      in the stream
   *@throws  ParseException  if the coordinates used to Construct a new <code>Polygon</code>
   *      shell and holes do not form closed linestrings, or if an unexpected
   *      token was encountered
   *@throws  IOException     if an I/O error occurs
   */
  private Geometry readGeometryTaggedText() throws IOException, ParseException {
    String type = null;

    try {
      type = getNextWord();
    } catch (final IOException e) {
      return null;
    } catch (final ParseException e) {
      return null;
    }

    if (type.equalsIgnoreCase("POINT")) {
      return readPointText();
    } else if (type.equalsIgnoreCase("LINESTRING")) {
      return readLineStringText();
    } else if (type.equalsIgnoreCase("LINEARRING")) {
      return readLinearRingText();
    } else if (type.equalsIgnoreCase("POLYGON")) {
      return readPolygonText();
    } else if (type.equalsIgnoreCase("MULTIPOINT")) {
      return readMultiPointText();
    } else if (type.equalsIgnoreCase("MULTILINESTRING")) {
      return readMultiLineStringText();
    } else if (type.equalsIgnoreCase("MULTIPOLYGON")) {
      return readMultiPolygonText();
    } else if (type.equalsIgnoreCase("GEOMETRYCOLLECTION")) {
      return readGeometryCollectionText();
    }
    parseErrorWithLine("Unknown geometry type: " + type);
    // should never reach here
    return null;
  }

  /**
   *  Creates a <code>LinearRing</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;LineString Text&gt;.
   *@return                  a <code>LinearRing</code> specified by the next
   *      token in the stream
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if the coordinates used to create the <code>LinearRing</code>
   *      do not form a closed linestring, or if an unexpected token was
   *      encountered
   */
  private LinearRing readLinearRingText() throws IOException, ParseException {
    return this.geometryFactory.linearRing(getCoordinates());
  }

  /**
   *  Creates a <code>LineString</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;LineString Text&gt;.
   *@return                  a <code>LineString</code> specified by the next
   *      token in the stream
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private LineString readLineStringText() throws IOException, ParseException {
    return this.geometryFactory.lineString(getCoordinates());
  }

  /*
   * private MultiPoint OLDreadMultiPointText() throws IOException,
   * ParseException { return
   * geometryFactory.multiPoint(toPoints(getCoordinates())); }
   */

  /**
   *  Creates a <code>MultiLineString</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;MultiLineString Text&gt;.
   *@return                  a <code>MultiLineString</code> specified by the
   *      next token in the stream
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private Lineal readMultiLineStringText() throws IOException, ParseException {
    String nextToken = getNextEmptyOrOpener();
    if (nextToken.equals(EMPTY)) {
      return this.geometryFactory.lineString();
    }
    final ArrayList lineStrings = new ArrayList();
    LineString lineString = readLineStringText();
    lineStrings.add(lineString);
    nextToken = getNextCloserOrComma();
    while (nextToken.equals(COMMA)) {
      lineString = readLineStringText();
      lineStrings.add(lineString);
      nextToken = getNextCloserOrComma();
    }
    final LineString[] array = new LineString[lineStrings.size()];
    return this.geometryFactory.lineal((LineString[])lineStrings.toArray(array));
  }

  /**
   *  Creates a <code>MultiPoint</code> using the next tokens in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;MultiPoint Text&gt;.
   *@return                  a <code>MultiPoint</code> specified by the next
   *      token in the stream
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private Punctual readMultiPointText() throws IOException, ParseException {
    String nextToken = getNextEmptyOrOpener();
    if (nextToken.equals(EMPTY)) {
      return this.geometryFactory.punctual(new Point[0]);
    }

    // check for old-style JTS syntax and parse it if present
    // MD 2009-02-21 - this is only provided for backwards compatibility for a
    // few versions
    if (ALLOW_OLD_JTS_MULTIPOINT_SYNTAX) {
      final String nextWord = lookaheadWord();
      if (nextWord != L_PAREN) {
        return this.geometryFactory.punctual(toPoints(getCoordinatesNoLeftParen()));
      }
    }

    final List<Point> points = new ArrayList<>();
    Point point = readPointText();
    points.add(point);
    nextToken = getNextCloserOrComma();
    while (nextToken.equals(COMMA)) {
      point = readPointText();
      points.add(point);
      nextToken = getNextCloserOrComma();
    }
    return this.geometryFactory.punctual(points);
  }

  /**
   *  Creates a <code>MultiPolygon</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;MultiPolygon Text&gt;.
   *@return                  a <code>MultiPolygon</code> specified by the next
   *      token in the stream, or if if the coordinates used to create the
   *      <code>Polygon</code> shells and holes do not form closed linestrings.
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private Polygonal readMultiPolygonText() throws IOException, ParseException {
    String nextToken = getNextEmptyOrOpener();
    if (nextToken.equals(EMPTY)) {
      return this.geometryFactory.polygonal(new Polygon[] {});
    }
    final ArrayList polygons = new ArrayList();
    Polygon polygon = readPolygonText();
    polygons.add(polygon);
    nextToken = getNextCloserOrComma();
    while (nextToken.equals(COMMA)) {
      polygon = readPolygonText();
      polygons.add(polygon);
      nextToken = getNextCloserOrComma();
    }
    final Polygon[] array = new Polygon[polygons.size()];
    return this.geometryFactory.polygonal((Polygon[])polygons.toArray(array));
  }

  /**
   *  Creates a <code>Point</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;Point Text&gt;.
   *@return                  a <code>Point</code> specified by the next token in
   *      the stream
   *@throws  IOException     if an I/O error occurs
   *@throws  ParseException  if an unexpected token was encountered
   */
  private Point readPointText() throws IOException, ParseException {
    final String nextToken = getNextEmptyOrOpener();
    if (nextToken.equals(EMPTY)) {
      return this.geometryFactory.point((Point)null);
    }
    final Point point = this.geometryFactory.point(getPreciseCoordinate());
    getNextCloser();
    return point;
  }

  /**
   *  Creates a <code>Polygon</code> using the next token in the stream.
   *
   *@param  tokenizer        tokenizer over a stream of text in Well-known Text
   *      format. The next tokens must form a &lt;Polygon Text&gt;.
   *@return                  a <code>Polygon</code> specified by the next token
   *      in the stream
   *@throws  ParseException  if the coordinates used to create the <code>Polygon</code>
   *      shell and holes do not form closed linestrings, or if an unexpected
   *      token was encountered.
   *@throws  IOException     if an I/O error occurs
   */
  private Polygon readPolygonText() throws IOException, ParseException {
    String nextToken = getNextEmptyOrOpener();
    if (nextToken.equals(EMPTY)) {
      return this.geometryFactory.polygon();
    }
    final List<LinearRing> rings = new ArrayList<>();
    final LinearRing shell = readLinearRingText();
    rings.add(shell);
    nextToken = getNextCloserOrComma();
    while (nextToken.equals(COMMA)) {
      final LinearRing hole = readLinearRingText();
      rings.add(hole);
      nextToken = getNextCloserOrComma();
    }
    return this.geometryFactory.polygon(rings);
  }

  /**
   * Gets a description of the current token
   *
   * @return a description of the current token
   */
  private String tokenString() {
    switch (this.tokenizer.ttype) {
      case StreamTokenizer.TT_NUMBER:
        return "<NUMBER>";
      case StreamTokenizer.TT_EOL:
        return "End-of-Line";
      case StreamTokenizer.TT_EOF:
        return "End-of-Stream";
      case StreamTokenizer.TT_WORD:
        return "'" + this.tokenizer.sval + "'";
    }
    return "'" + (char)this.tokenizer.ttype + "'";
  }

  /**
   *  Creates an array of <code>Point</code>s having the given <code>Coordinate</code>
   *  s.
   *
   *@param  coordinates  the <code>Coordinate</code>s with which to create the
   *      <code>Point</code>s
   *@return              <code>Point</code>s created using this <code>WKTReader</code>
   *      s <code>GeometryFactory</code>
   */
  private Point[] toPoints(final Point[] coordinates) {
    final ArrayList points = new ArrayList();
    for (final Point coordinate : coordinates) {
      points.add(this.geometryFactory.point(coordinate));
    }
    return (Point[])points.toArray(new Point[] {});
  }

}
