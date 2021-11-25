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
package com.revolsys.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlExpression;
import org.jeometry.common.logging.Logs;

/**
 * The JexlUtil is a utility class for processing strings that contain patterns
 * from the Jakarta Commons Jexl library.
 *
 * @author Paul Austin
 */
public final class JexlUtil {
  /** The default expression pattern matching expressions in the form ${el}. */
  public static final String DEFAULT_EXPRESSION_PATTERN = "\\$\\{([^\\}]+)\\}";

  /**
   * Add the text to the Jexl expression, wrapping the text in a '' string.
   *
   * @param jexlExpression The expression to add the test to.
   * @param text The text to add.
   */
  private static void addText(final StringBuilder jexlExpression, final String text) {
    jexlExpression.append("'").append(text.replaceAll("'", "' + \"'\" + '")).append("'");
  }

  public static Object evaluateExpression(final JexlContext context,
    final JexlExpression expression) {
    try {
      return expression.evaluate(context);
    } catch (final Exception e) {
      Logs.error(JexlUtil.class,
        "Unable to evaluate expression '" + expression.getSourceText() + "': " + e.getMessage(), e);
      return null;
    }
  }

  /**
   * Convert expressions into valid JexlExpressions, if the string does not
   * contain any expressions in the form ${el} then null will be returned and
   * the caller can use the raw string.
   *
   * @param expression The string containing expressions.
   * @return The expression object for the string expression.
   * @throws Exception If there was an error creating the expression.
   */
  public static JexlExpression newExpression(final String expression) throws Exception {
    return newExpression(expression, DEFAULT_EXPRESSION_PATTERN);
  }

  /**
   * <p>
   * Convert expressions into valid JexlExpressions, if the string does not
   * contain any expressions that match the expressionPattern then null will be
   * returned and the caller can use the raw string.
   * </p>
   * <p>
   * The expressionPattern can be used to define an alternate pattern than the
   * {@link #DEFAULT_EXPRESSION_PATTERN} that defines expressions in the form
   * ${el}. The pattern is defined as a Java Regular expression. The contents of
   * the expression part of the pattern must be enclosed in () to define the
   * group. The characters outside the first group will be removed from the
   * string and the expression portion will be added to the expression.
   * </p>
   *
   * @param expression The string containing expressions.
   * @param expressionPattern The regular expression pattern used to identify
   *          expressions in the string. The first group in the expression will
   *          be used as the expression.
   * @return The expression object for the string expression.
   * @throws Exception If there was an error creating the expression.
   */
  public static JexlExpression newExpression(final String expression,
    final String expressionPattern) throws Exception {
    final String newExpression = expression.replaceAll("\n", "");
    // Wrap the entires expression in '' and replace the expressions in the
    // form "${expr)" to ' + expr + '
    final Pattern compiledPattern = Pattern.compile(expressionPattern);
    final Matcher matcher = compiledPattern.matcher(newExpression);
    int lastEnd = 0;
    if (matcher.find()) {
      final StringBuilder jexlExpression = new StringBuilder();
      do {
        final int startIndex = matcher.start();
        if (startIndex != lastEnd) {
          final String text = newExpression.substring(lastEnd, startIndex);
          addText(jexlExpression, text);
          jexlExpression.append(" + ");
        }
        final String matchedExpression = matcher.group(1);
        jexlExpression.append(matchedExpression).append(" + ");
        lastEnd = matcher.end();
      } while (matcher.find());
      addText(jexlExpression, newExpression.substring(lastEnd));

      // Remove any empty strings from the expression to improve
      // performance
      String expr = jexlExpression.toString();
      expr = expr.replaceAll(" \\+ '' \\+ ", " + ");
      expr = expr.replaceAll("^'' \\+ ", "");
      expr = expr.replaceAll("\\+ ''$", "");
      return new JexlBuilder().create().createExpression(expr);
    } else {
      return null;
    }
  }

  /**
   * Construct a new JexlUtil.
   */
  private JexlUtil() {
  }

}
