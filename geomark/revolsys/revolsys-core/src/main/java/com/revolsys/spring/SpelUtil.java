package com.revolsys.spring;

import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpelUtil {
  public static final SpelParserConfiguration CONFIGURATION = new SpelParserConfiguration(true,
    true);

  public static final SpelExpressionParser PARSER = new SpelExpressionParser(CONFIGURATION);

  @SuppressWarnings("unchecked")
  public static <V> V getValue(final Expression expression, final Object object,
    final Map<String, Object> parameters) {
    final StandardEvaluationContext context = new StandardEvaluationContext(object);
    if (object instanceof Map) {
      @SuppressWarnings("rawtypes")
      final Map map = (Map)object;
      context.setVariables(map);
    }
    if (parameters != null) {
      context.setVariables(parameters);
    }
    return (V)expression.getValue(context);
  }

  @SuppressWarnings("unchecked")
  public static <V> V getValue(final String expressionString, final Object object) {
    return (V)getValue(expressionString, object, null);
  }

  @SuppressWarnings("unchecked")
  public static <V> V getValue(final String expressionString, final Object object,
    final Map<String, Object> parameters) {
    final Expression expression = parse(expressionString);
    return (V)getValue(expression, object, parameters);
  }

  public static Expression parse(final String expressionString) {
    final Expression expression = PARSER.parseExpression(expressionString);
    return expression;
  }
}
