package com.revolsys.spring;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeometry.common.logging.Logs;
import org.springframework.util.Assert;

import com.revolsys.util.Property;

public class StringTemplate implements Serializable {
  /**
   * Static inner class to parse URI template strings into a matching regular
   * expression.
   */
  private static class Parser {

    private final StringBuilder patternBuilder = new StringBuilder();

    private final List<String> variableNames = new LinkedList<>();

    private Parser(final String uriTemplate) {
      Assert.hasText(uriTemplate, "'template' must not be null");
      final Matcher m = NAMES_PATTERN.matcher(uriTemplate);
      int end = 0;
      while (m.find()) {
        this.patternBuilder.append(quote(uriTemplate, end, m.start()));
        this.patternBuilder.append(VALUE_REGEX);
        this.variableNames.add(m.group(1));
        end = m.end();
      }
      this.patternBuilder.append(quote(uriTemplate, end, uriTemplate.length()));
      final int lastIdx = this.patternBuilder.length() - 1;
      if (lastIdx >= 0 && this.patternBuilder.charAt(lastIdx) == '/') {
        this.patternBuilder.deleteCharAt(lastIdx);
      }
    }

    private List<String> getVariableNames() {
      return Collections.unmodifiableList(this.variableNames);
    }

    private String quote(final String fullPath, final int start, final int end) {
      if (start == end) {
        return "";
      }
      return Pattern.quote(fullPath.substring(start, end));
    }
  }

  private static final Pattern NAMES_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

  private static final long serialVersionUID = 1L;

  private static final String VALUE_REGEX = "(.*)";

  private final String template;

  private List<String> variableNames;

  public StringTemplate(final String template) {
    this.template = template;
    if (Property.hasValue(template)) {
      try {
        final Parser parser = new Parser(template);
        this.variableNames = parser.getVariableNames();
      } catch (final Throwable e) {
        Logs.error(this, "Invalid Template:" + template, e);
      }
    }
  }

  public String expand(Map<String, ?> uriVariables) {
    if (this.variableNames == null) {
      return this.template;
    } else {
      if (uriVariables == null) {
        uriVariables = Collections.emptyMap();
      }
      final Object[] values = new Object[this.variableNames.size()];
      if (uriVariables != null) {
        for (int i = 0; i < this.variableNames.size(); i++) {
          final String name = this.variableNames.get(i);
          if (uriVariables.containsKey(name)) {
            values[i] = uriVariables.get(name);

          }
        }
      }
      return expand(values);
    }
  }

  private String expand(final Object... uriVariableValues) {
    final Matcher matcher = NAMES_PATTERN.matcher(this.template);
    final StringBuilder buffer = new StringBuilder();
    int i = 0;
    while (matcher.find()) {
      final Object uriVariable = uriVariableValues[i++];
      String replacement;
      if (uriVariable == null) {
        replacement = Matcher.quoteReplacement("null");
      } else {
        replacement = Matcher.quoteReplacement(uriVariable.toString());
      }
      matcher.appendReplacement(buffer, replacement);
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

  public List<String> getVariableNames() {
    return this.variableNames;
  }

  @Override
  public String toString() {
    return this.template;
  }

}
