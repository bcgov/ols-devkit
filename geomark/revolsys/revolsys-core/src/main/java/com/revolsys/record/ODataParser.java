package com.revolsys.record;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jeometry.coordinatesystem.util.Hex;

import com.revolsys.collection.map.Maps;
import com.revolsys.collection.set.Sets;
import com.revolsys.record.query.Add;
import com.revolsys.record.query.And;
import com.revolsys.record.query.CollectionValue;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Divide;
import com.revolsys.record.query.Equal;
import com.revolsys.record.query.GreaterThan;
import com.revolsys.record.query.GreaterThanEqual;
import com.revolsys.record.query.In;
import com.revolsys.record.query.IsNotNull;
import com.revolsys.record.query.IsNull;
import com.revolsys.record.query.LessThan;
import com.revolsys.record.query.LessThanEqual;
import com.revolsys.record.query.Mod;
import com.revolsys.record.query.Multiply;
import com.revolsys.record.query.Negate;
import com.revolsys.record.query.Not;
import com.revolsys.record.query.NotEqual;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Subtract;
import com.revolsys.record.query.TableReference;
import com.revolsys.record.query.Value;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.query.functions.Lower;
import com.revolsys.record.query.functions.Upper;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.util.Pair;
import com.revolsys.util.Strings;

public class ODataParser {

  public enum AggregateFunction {
    none, any, all
  }

  private static class ExpressionToken extends Token {

    public final QueryValue expression;

    private final List<Token> tokens;

    public ExpressionToken(final QueryValue expression, final List<Token> tokens) {
      super(TokenType.EXPRESSION, null);
      this.expression = expression;
      this.tokens = tokens;
    }

    @Override
    public String toString() {
      return Strings.toString("", this.tokens);
    }
  }

  private static class Methods {

    public static final String CAST = "cast";

    public static final String ISOF = "isof";

    public static final String ENDSWITH = "endswith";

    public static final String STARTSWITH = "startswith";

    public static final String SUBSTRINGOF = "substringof";

    public static final String INDEXOF = "indexof";

    public static final String REPLACE = "replace";

    public static final String TOLOWER = "tolower";

    public static final String TOUPPER = "toupper";

    public static final String TRIM = "trim";

    public static final String SUBSTRING = "substring";

    public static final String CONCAT = "concat";

    public static final String LENGTH = "length";

    public static final String YEAR = "year";

    public static final String MONTH = "month";

    public static final String DAY = "day";

    public static final String HOUR = "hour";

    public static final String MINUTE = "minute";

    public static final String SECOND = "second";

    public static final String ROUND = "round";

    public static final String FLOOR = "floor";

    public static final String CEILING = "ceiling";

    public static final String GEO_INTERSECTS = "geo.intersects";
  };

  public static class Token {

    public final TokenType type;

    public final String value;

    public Token(final TokenType type, final String value) {
      this.type = type;
      this.value = value;
    }

    @Override
    public String toString() {
      return "[" + this.value + "] " + this.type;
    }
  }

  public static enum TokenType {
    UNKNOWN, WHITESPACE, QUOTED_STRING, WORD, SYMBOL, NUMBER, OPENPAREN, CLOSEPAREN, EXPRESSION;
  }

  private static Set<String> METHODS = Sets.newHash(Methods.CAST, Methods.ISOF, Methods.ENDSWITH,
    Methods.STARTSWITH, Methods.SUBSTRINGOF, Methods.INDEXOF, Methods.REPLACE, Methods.TOLOWER,
    Methods.TOUPPER, Methods.TRIM, Methods.SUBSTRING, Methods.CONCAT, Methods.LENGTH, Methods.YEAR,
    Methods.MONTH, Methods.DAY, Methods.HOUR, Methods.MINUTE, Methods.SECOND, Methods.ROUND,
    Methods.FLOOR, Methods.CEILING, Methods.GEO_INTERSECTS);

  private static final Map<String, Function<List<QueryValue>, QueryValue>> METHOD_FACTORIES = Maps
    .<String, Function<List<QueryValue>, QueryValue>> buildHash()//
    .add(Methods.TOUPPER, Upper::new)
    .add(Methods.TOLOWER, Lower::new)
    .add(Methods.GEO_INTERSECTS, F::envelopeIntersects)
    .getMap();

  // Order by preference
  private static final Map<String, BiFunction<QueryValue, QueryValue, QueryValue>> BINARY_OPERATOR_FACTORIES = Maps
    .<String, BiFunction<QueryValue, QueryValue, QueryValue>> buildLinkedHash()//
    .add("or", Or::new)
    .add("and", And::new)
    .add("eq", (left, right) -> {
      if (right instanceof Value) {
        final Value value = (Value)right;
        if (value.getValue() == null) {
          return new IsNull(left);
        }
      }
      return new Equal(left, right);
    })
    .add("ne", (left, right) -> {
      if (right instanceof Value) {
        final Value value = (Value)right;
        if (value.getValue() == null) {
          return new IsNotNull(left);
        }
      }
      return new NotEqual(left, right);
    })
    .add("lt", LessThan::new)
    .add("gt", GreaterThan::new)
    .add("le", LessThanEqual::new)
    .add("ge", GreaterThanEqual::new)
    .add("add", Add::new)
    .add("sub", Subtract::new)
    .add("mul", Multiply::new)
    .add("div", Divide::new)
    .add("mod", Mod::new)
    .add("in", In::new)
    .getMap();

  // Order by preference
  private static final Map<String, Pair<Boolean, Function<QueryValue, QueryValue>>> UNARY_OPERATOR_FACTORIES = Maps
    .<String, Pair<Boolean, Function<QueryValue, QueryValue>>> buildLinkedHash()//
    .add("not", new Pair<>(true, Not::new))
    .add("-", new Pair<>(false, Negate::new))
    .getMap();

  private static QueryValue methodCall(final String methodName,
    final List<QueryValue> methodArguments) {
    final Function<List<QueryValue>, QueryValue> function = METHOD_FACTORIES.get(methodName);
    if (function != null) {
      return function.apply(methodArguments);
    }
    throw new IllegalArgumentException(methodName + " not supported");
    // if (methodName.equals(Methods.CAST) && methodArguments.size() == 1) {
    // final QueryValue arg = methodArguments.get(0);
    // assertType(arg, StringLiteral.class);
    // final String type = ((StringLiteral)arg).getValue();
    // return Expression.cast(type);
    // } else if (methodName.equals(Methods.CAST) && methodArguments.size() ==
    // 2) {
    // final QueryValue arg1 = methodArguments.get(0);
    // final QueryValue arg2 = methodArguments.get(1);
    // assertType(arg2, StringLiteral.class);
    // final String type = ((StringLiteral)arg2).getValue();
    // return Expression.cast(arg1, type);
    // } else if (methodName.equals(Methods.ISOF) && methodArguments.size() ==
    // 1) {
    // final QueryValue arg = methodArguments.get(0);
    // assertType(arg, StringLiteral.class);
    // final String type = ((StringLiteral)arg).getValue();
    // return Expression.isof(type);
    // } else if (methodName.equals(Methods.ISOF) && methodArguments.size() ==
    // 2) {
    // final QueryValue arg1 = methodArguments.get(0);
    // final QueryValue arg2 = methodArguments.get(1);
    // assertType(arg2, StringLiteral.class);
    // final String type = ((StringLiteral)arg2).getValue();
    // return Expression.isof(arg1, type);
    // } else if (methodName.equals(Methods.ENDSWITH) && methodArguments.size()
    // == 2) {
    // final QueryValue arg1 = methodArguments.get(0);
    // final QueryValue arg2 = methodArguments.get(1);
    // return Expression.endsWith(arg1, arg2);
    // } else if (methodName.equals(Methods.STARTSWITH) &&
    // methodArguments.size() == 2) {
    // final QueryValue arg1 = methodArguments.get(0);
    // final QueryValue arg2 = methodArguments.get(1);
    // return Expression.startsWith(arg1, arg2);
    // } else if (methodName.equals(Methods.SUBSTRINGOF) &&
    // methodArguments.size() == 1) {
    // final QueryValue arg1 = methodArguments.get(0);
    // return Expression.substringOf(arg1);
    // } else if (methodName.equals(Methods.SUBSTRINGOF) &&
    // methodArguments.size() == 2) {
    // final QueryValue arg1 = methodArguments.get(0);
    // final QueryValue arg2 = methodArguments.get(1);
    // return Expression.substringOf(arg1, arg2);
    // } else if (methodName.equals(Methods.INDEXOF) && methodArguments.size()
    // == 2) {
    // final QueryValue arg1 = methodArguments.get(0);
    // final QueryValue arg2 = methodArguments.get(1);
    // return Expression.indexOf(arg1, arg2);
    // } else if (methodName.equals(Methods.REPLACE) && methodArguments.size()
    // == 3) {
    // final QueryValue arg1 = methodArguments.get(0);
    // final QueryValue arg2 = methodArguments.get(1);
    // final QueryValue arg3 = methodArguments.get(2);
    // return Expression.replace(arg1, arg2, arg3);
    // } else if (methodName.equals(Methods.TRIM) && methodArguments.size() ==
    // 1) {
    // final QueryValue arg1 = methodArguments.get(0);
    // return Expression.trim(arg1);
    // } else if (methodName.equals(Methods.SUBSTRING) && methodArguments.size()
    // == 2) {
    // final QueryValue arg1 = methodArguments.get(0);
    // final QueryValue arg2 = methodArguments.get(1);
    // return Expression.substring(arg1, arg2);
    // } else if (methodName.equals(Methods.SUBSTRING) && methodArguments.size()
    // == 3) {
    // final QueryValue arg1 = methodArguments.get(0);
    // final QueryValue arg2 = methodArguments.get(1);
    // final QueryValue arg3 = methodArguments.get(2);
    // return Expression.substring(arg1, arg2, arg3);
    // } else if (methodName.equals(Methods.CONCAT) && methodArguments.size() ==
    // 2) {
    // final QueryValue arg1 = methodArguments.get(0);
    // final QueryValue arg2 = methodArguments.get(1);
    // return Expression.concat(arg1, arg2);
    // } else if (methodName.equals(Methods.LENGTH) && methodArguments.size() ==
    // 1) {
    // final QueryValue arg1 = methodArguments.get(0);
    // return Expression.length(arg1);
    // } else if (methodName.equals(Methods.YEAR) && methodArguments.size() ==
    // 1) {
    // final QueryValue arg1 = methodArguments.get(0);
    // return Expression.year(arg1);
    // } else if (methodName.equals(Methods.MONTH) && methodArguments.size() ==
    // 1) {
    // final QueryValue arg1 = methodArguments.get(0);
    // return Expression.month(arg1);
    // } else if (methodName.equals(Methods.DAY) && methodArguments.size() == 1)
    // {
    // final QueryValue arg1 = methodArguments.get(0);
    // return Expression.day(arg1);
    // } else if (methodName.equals(Methods.HOUR) && methodArguments.size() ==
    // 1) {
    // final QueryValue arg1 = methodArguments.get(0);
    // return Expression.hour(arg1);
    // } else if (methodName.equals(Methods.MINUTE) && methodArguments.size() ==
    // 1) {
    // final QueryValue arg1 = methodArguments.get(0);
    // return Expression.minute(arg1);
    // } else if (methodName.equals(Methods.SECOND) && methodArguments.size() ==
    // 1) {
    // final QueryValue arg1 = methodArguments.get(0);
    // return Expression.second(arg1);
    // } else if (methodName.equals(Methods.CEILING) && methodArguments.size()
    // == 1) {
    // final QueryValue arg1 = methodArguments.get(0);
    // return Expression.ceiling(arg1);
    // } else if (methodName.equals(Methods.FLOOR) && methodArguments.size() ==
    // 1) {
    // final QueryValue arg1 = methodArguments.get(0);
    // return Expression.floor(arg1);
    // } else if (methodName.equals(Methods.ROUND) && methodArguments.size() ==
    // 1) {
    // final QueryValue arg1 = methodArguments.get(0);
    // return Expression.round(arg1);
    // } else {
    // throw new RuntimeException("Implement method " + methodName);
    // }
  }

  public static QueryValue parseFilter(final TableReference table, final String value) {
    final List<Token> tokens = tokenize(value);

    return readExpression(table, tokens);
  }

  private static QueryValue processBinaryExpression(final TableReference table,
    final List<Token> tokens, final String op) {

    final int ts = tokens.size();
    for (int i = 0; i < ts; i++) {
      final Token t = tokens.get(i);
      if (i < ts - 2) {
        if (t.type == TokenType.WHITESPACE && tokens.get(i + 2).type == TokenType.WHITESPACE
          && tokens.get(i + 1).type == TokenType.WORD) {
          final Token wordToken = tokens.get(i + 1);
          if (wordToken.value.equals(op)) {
            final BiFunction<QueryValue, QueryValue, QueryValue> fn = BINARY_OPERATOR_FACTORIES
              .get(op);
            final QueryValue lhs = readExpression(table, tokens.subList(0, i));
            final QueryValue rhs = readExpression(table, tokens.subList(i + 3, ts));
            if (lhs instanceof FieldDefinition) {
              rhs.setFieldDefinition((FieldDefinition)lhs);
            }
            return fn.apply(lhs, rhs);
          }
        }
      }
    }
    return null;
  }

  private static List<Token> processParentheses(final TableReference table,
    final List<Token> tokens) {

    final List<Token> rt = new ArrayList<>();

    for (int i = 0; i < tokens.size(); i++) {
      final Token openToken = tokens.get(i);
      if (openToken.type == TokenType.OPENPAREN) {
        int afterParenIdx = i + 1;
        // is this a method call or any/all aggregate function?
        String methodName = null;
        String aggregateSource = null;
        String aggregateVariable = null;
        AggregateFunction aggregateFunction = AggregateFunction.none;
        int k = i - 1;
        while (k > 0 && tokens.get(k).type == TokenType.WHITESPACE) {
          k--;
        }
        if (k >= 0) {
          final Token methodNameToken = tokens.get(k);
          if (methodNameToken.type == TokenType.WORD) {
            final String methodTokenValue = methodNameToken.value;
            if (METHODS.contains(methodTokenValue)) {
              methodName = methodTokenValue;

              // this isn't strictly correct. I think the parser has issues
              // with sequences of WORD, WHITESPACE, WORD, etc. I'm not sure
              // I've
              // ever seen a token type of WHITESPACE producer by a lexer..
            } else if (methodTokenValue.endsWith("/any") || methodTokenValue.endsWith("/all")) {
              aggregateSource = methodTokenValue.substring(0, methodTokenValue.length() - 4);
              aggregateFunction = Enum.valueOf(AggregateFunction.class,
                methodTokenValue.substring(methodTokenValue.length() - 3));
              // to get things rolling I'm going to lookahead and require a very
              // strict
              // sequence of tokens:
              // i + 1 must be a WORD
              // i + 2 must be a SYMBOL ':'
              // or, for any, i + 1 can be CLOSEPAREN
              int ni = i + 1;
              Token ntoken = ni < tokens.size() ? tokens.get(ni) : null;
              if (ntoken == null
                || aggregateFunction == AggregateFunction.all && ntoken.type != TokenType.WORD
                || aggregateFunction == AggregateFunction.any && ntoken.type != TokenType.WORD
                  && ntoken.type != TokenType.CLOSEPAREN) {
                throw new RuntimeException(
                  "unexpected token: " + (ntoken == null ? "eof" : ntoken.toString()));
              }
              if (ntoken.type == TokenType.WORD) {
                aggregateVariable = ntoken.value;
                ni += 1;
                ntoken = ni < tokens.size() ? tokens.get(ni) : null;
                if (ntoken == null || ntoken.type != TokenType.SYMBOL
                  || !ntoken.value.equals(":")) {
                  throw new RuntimeException(
                    "expected ':', found: " + (ntoken == null ? "eof" : ntoken.toString()));
                }
                // now we can parse the predicate, starting after the ':'
                afterParenIdx = ni + 1;
              } else {
                // any(), easiest to early out here
                final List<Token> tokensIncludingParens = tokens.subList(k, ni + 1);
                final QueryValue any = null;// Expression.any(Expression.simpleProperty(aggregateSource));

                final ExpressionToken et = new ExpressionToken(any, tokensIncludingParens);
                rt.subList(rt.size() - (i - k), rt.size()).clear();
                rt.add(et);
                return rt;
              }

            }
          }
        }

        // find matching close paren
        int stack = 0;
        int start = i;
        final List<QueryValue> methodArguments = new ArrayList<>();
        for (int j = afterParenIdx; j < tokens.size(); j++) {
          final Token closeToken = tokens.get(j);
          if (closeToken.type == TokenType.OPENPAREN) {
            stack++;
          } else if (methodName != null && stack == 0 && closeToken.type == TokenType.SYMBOL
            && closeToken.value.equals(",")) {
            final List<Token> tokensInsideComma = tokens.subList(start + 1, j);
            final QueryValue expressionInsideComma = readExpression(table, tokensInsideComma);
            methodArguments.add(expressionInsideComma);
            start = j;
          } else if (closeToken.type == TokenType.CLOSEPAREN) {
            if (stack > 0) {
              stack--;
              continue;
            }

            if (methodName != null) {
              final List<Token> tokensIncludingParens = tokens.subList(k, j + 1);
              final List<Token> tokensInsideParens = tokens.subList(start + 1, j);
              final QueryValue expressionInsideParens = readExpression(table, tokensInsideParens);
              methodArguments.add(expressionInsideParens);

              // method call expression: replace t mn ( t t , t t ) t with t et
              // t
              final QueryValue methodCall = methodCall(methodName, methodArguments);

              final ExpressionToken et = new ExpressionToken(methodCall, tokensIncludingParens);
              rt.subList(rt.size() - (i - k), rt.size()).clear();
              rt.add(et);

            } else if (aggregateVariable != null) {
              final List<Token> tokensIncludingParens = tokens.subList(k, j + 1);
              final List<Token> tokensInsideParens = tokens.subList(afterParenIdx, j);
              final QueryValue expressionInsideParens = readExpression(table, tokensInsideParens);
              if (!(expressionInsideParens instanceof Condition)) {
                throw new RuntimeException("illegal any predicate");
              }
              // final QueryValue any = Expression.aggregate(aggregateFunction,
              // Expression.simpleProperty(aggregateSource), aggregateVariable,
              // (BoolQueryValue)expressionInsideParens);
              //
              // final ExpressionToken et = new ExpressionToken(any,
              // tokensIncludingParens);
              // rt.subList(rt.size() - (i - k), rt.size()).clear();
              // rt.add(et);
            } else {

              final List<Token> tokensIncludingParens = tokens.subList(i, j + 1);
              final List<Token> tokensInsideParens = tokens.subList(i + 1, j);
              final QueryValue expressionInsideParens = readExpression(table, tokensInsideParens);

              final ExpressionToken et = new ExpressionToken(expressionInsideParens,
                tokensIncludingParens);
              rt.add(et);
            }

            i = j;
          }
        }
      } else {
        rt.add(openToken);
      }
    }

    return rt;

  }

  private static QueryValue processUnaryExpression(final TableReference table,
    final List<Token> tokens, final String op) {
    final Pair<Boolean, Function<QueryValue, QueryValue>> config = UNARY_OPERATOR_FACTORIES.get(op);
    final boolean whitespaceRequired = config.getValue1();
    final int ts = tokens.size();
    for (int i = 0; i < ts; i++) {
      final Token t = tokens.get(i);
      if (i < ts - 1) {
        if ((t.type == TokenType.WORD || t.type == TokenType.SYMBOL)
          && (!whitespaceRequired || tokens.get(i + 1).type == TokenType.WHITESPACE)) {
          final Token wordToken = t;
          if (wordToken.value.equals(op)) {
            final QueryValue expression = readExpression(table,
              tokens.subList(i + (whitespaceRequired ? 2 : 1), ts));
            final Function<QueryValue, QueryValue> fn = config.getValue2();
            return fn.apply(expression);
          }
        }
      }
    }
    return null;
  }

  private static int readDigits(final String value, final int start) {
    int rt = start;
    while (rt < value.length() && Character.isDigit(value.charAt(rt))) {
      rt++;
    }
    return rt;
  }

  private static QueryValue readExpression(final TableReference table, List<Token> tokens) {
    tokens = trimWhitespace(tokens);
    tokens = processParentheses(table, tokens);
    if (tokens.size() == 2 && tokens.get(0).type == TokenType.WORD
      && tokens.get(1).type == TokenType.QUOTED_STRING) {
      final String word = tokens.get(0).value;
      final String value = unquote(tokens.get(1).value);
      if (word.equals("datetime")) {
        return Value.newValue(Instant.parse(value));
      } else if (word.equals("time")) {
        return Value.newValue(LocalTime.parse(value));
      } else if (word.equals("datetimeoffset")) {
        return Value.newValue(Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value)));
      } else if (word.equals("guid")) {
        // odata: dddddddd-dddd-dddd-dddddddddddd
        // java: dddddddd-dd-dd-dddd-dddddddddddd
        // value = value.substring(0, 11) + "-" + value.substring(11);
        return Value.newValue(UUID.fromString(value));
      } else if (word.equals("decimal")) {
        return Value.newValue(new BigDecimal(value));
      } else if (word.equals("X") || word.equals("binary")) {
        final byte[] bValue = Hex.toDecimalBytes(value);
        return Value.newValue(bValue);
      }
    }
    // long literal: 1234L
    if (tokens.size() == 2 && tokens.get(0).type == TokenType.NUMBER
      && tokens.get(1).type == TokenType.WORD && tokens.get(1).value.equalsIgnoreCase("L")) {
      final long longValue = Long.parseLong(tokens.get(0).value);
      return Value.newValue(longValue);
    }
    // single literal: 2f
    if (tokens.size() == 2 && tokens.get(0).type == TokenType.NUMBER
      && tokens.get(1).type == TokenType.WORD && tokens.get(1).value.equalsIgnoreCase("f")) {
      final float floatValue = Float.parseFloat(tokens.get(0).value);
      return Value.newValue(floatValue);
    }
    // single literal: 2.0f
    if (tokens.size() == 4 && tokens.get(0).type == TokenType.NUMBER
      && tokens.get(1).type == TokenType.SYMBOL && tokens.get(1).value.equals(".")
      && tokens.get(2).type == TokenType.NUMBER && tokens.get(3).value.equalsIgnoreCase("f")) {
      final float floatValue = Float.parseFloat(tokens.get(0).value + "." + tokens.get(2).value);
      return Value.newValue(floatValue);
    }
    // double literal: 2d
    if (tokens.size() == 2 && tokens.get(0).type == TokenType.NUMBER
      && tokens.get(1).type == TokenType.WORD && tokens.get(1).value.equalsIgnoreCase("d")) {
      final double doubleValue = Double.parseDouble(tokens.get(0).value);
      return Value.newValue(doubleValue);
    }
    // double literal: 2.0d
    if (tokens.size() == 4 && tokens.get(0).type == TokenType.NUMBER
      && tokens.get(1).type == TokenType.SYMBOL && tokens.get(1).value.equals(".")
      && tokens.get(2).type == TokenType.NUMBER && tokens.get(3).value.equalsIgnoreCase("d")) {
      final double doubleValue = Double
        .parseDouble(tokens.get(0).value + "." + tokens.get(2).value);
      return Value.newValue(doubleValue);
    }
    // double literal: 1E+10
    if (tokens.size() == 4 && tokens.get(0).type == TokenType.NUMBER
      && tokens.get(1).type == TokenType.WORD && tokens.get(1).value.equalsIgnoreCase("E")
      && tokens.get(2).type == TokenType.SYMBOL && tokens.get(2).value.equals("+")
      && tokens.get(3).type == TokenType.NUMBER) {
      final double doubleValue = Double
        .parseDouble(tokens.get(0).value + "E+" + tokens.get(3).value);
      return Value.newValue(doubleValue);
    }
    // double literal: 1E-10
    if (tokens.size() == 3 && tokens.get(0).type == TokenType.NUMBER
      && tokens.get(1).type == TokenType.WORD && tokens.get(1).value.equalsIgnoreCase("E")
      && tokens.get(2).type == TokenType.NUMBER) {
      final int e = Integer.parseInt(tokens.get(2).value);
      if (e < 1) {
        final double doubleValue = Double
          .parseDouble(tokens.get(0).value + "E" + tokens.get(2).value);
        return Value.newValue(doubleValue);
      }
    }
    // double literal: 1.2E+10
    if (tokens.size() == 6 && tokens.get(0).type == TokenType.NUMBER
      && tokens.get(1).type == TokenType.SYMBOL && tokens.get(1).value.equals(".")
      && tokens.get(2).type == TokenType.NUMBER && tokens.get(3).type == TokenType.WORD
      && tokens.get(3).value.equalsIgnoreCase("E") && tokens.get(4).type == TokenType.SYMBOL
      && tokens.get(4).value.equals("+") && tokens.get(5).type == TokenType.NUMBER) {
      final double doubleValue = Double
        .parseDouble(tokens.get(0).value + "." + tokens.get(2).value + "E+" + tokens.get(5).value);
      return Value.newValue(doubleValue);
    }
    // double literal: 1.2E-10
    if (tokens.size() == 5 && tokens.get(0).type == TokenType.NUMBER
      && tokens.get(1).type == TokenType.SYMBOL && tokens.get(1).value.equals(".")
      && tokens.get(2).type == TokenType.NUMBER && tokens.get(3).type == TokenType.WORD
      && tokens.get(3).value.equalsIgnoreCase("E") && tokens.get(4).type == TokenType.NUMBER) {
      final int e = Integer.parseInt(tokens.get(4).value);
      if (e < 1) {
        final double doubleValue = Double
          .parseDouble(tokens.get(0).value + "." + tokens.get(2).value + "E" + tokens.get(4).value);
        return Value.newValue(doubleValue);
      }
    }
    // decimal literal: 1234M or 1234m
    if (tokens.size() == 2 && tokens.get(0).type == TokenType.NUMBER
      && tokens.get(1).type == TokenType.WORD && tokens.get(1).value.equalsIgnoreCase("M")) {
      final BigDecimal decimalValue = new BigDecimal(tokens.get(0).value);
      return Value.newValue(decimalValue);
    }
    // decimal literal: 2.0m
    if (tokens.size() == 4 && tokens.get(0).type == TokenType.NUMBER
      && tokens.get(1).type == TokenType.SYMBOL && tokens.get(1).value.equals(".")
      && tokens.get(2).type == TokenType.NUMBER && tokens.get(3).value.equalsIgnoreCase("m")) {
      final BigDecimal decimalValue = new BigDecimal(
        tokens.get(0).value + "." + tokens.get(2).value);
      return Value.newValue(decimalValue);
    }
    // TODO literals: byteLiteral, sbyteliteral

    // single token expression
    if (tokens.size() == 1) {
      final Token token = tokens.get(0);
      if (token.type == TokenType.QUOTED_STRING) {
        return Value.newValue(unquote(token.value));
      } else if (token.type == TokenType.WORD) {
        if (token.value.equals("null")) {
          return Value.newValue(null);
        }
        if (token.value.equals("true")) {
          return Value.newValue(true);
        }
        if (token.value.equals("false")) {
          return Value.newValue(false);
        }
        try {
          return table.getColumn(token.value);
        } catch (final Exception e) {
          return new Column(token.value);
        }
      } else if (token.type == TokenType.NUMBER) {
        try {
          final int value = Integer.parseInt(token.value);
          return Value.newValue(value);
        } catch (final NumberFormatException e) {
          final long value = Long.parseLong(token.value);
          return Value.newValue(value);
        }
      } else if (token.type == TokenType.EXPRESSION) {
        return ((ExpressionToken)token).expression;
      } else {
        throw new RuntimeException("Unexpected");
      }
    }

    for (final String operator : BINARY_OPERATOR_FACTORIES.keySet()) {
      final QueryValue rt = processBinaryExpression(table, tokens, operator);
      if (rt != null) {
        return rt;
      }
    }

    for (final String operator : UNARY_OPERATOR_FACTORIES.keySet()) {
      final QueryValue rt = processUnaryExpression(table, tokens, operator);
      if (rt != null) {
        return rt;
      }
    }
    final List<Object> values = new ArrayList<>();
    for (final Token token : tokens) {
      if (token.type == TokenType.WHITESPACE) {
      } else if (token.type == TokenType.SYMBOL && token.value.equals(",")) {
      } else if (token.type == TokenType.QUOTED_STRING) {
        final String value = unquote(token.value);
        values.add(value);
      } else {
        throw new RuntimeException(
          "Unable to read expression with tokens: " + token + ":" + tokens);
      }
    }
    return new CollectionValue(values);
  }

  private static int readQuotedString(final String value, final int start) {
    int rt = start;
    while (value.charAt(rt) != '\'' || rt < value.length() - 1 && value.charAt(rt + 1) == '\'') {
      if (value.charAt(rt) != '\'') {
        rt++;
      } else {
        rt += 2;
      }
    }
    rt++;
    return rt;
  }

  private static int readWhitespace(final String value, final int start) {
    int rt = start;
    while (rt < value.length() && Character.isWhitespace(value.charAt(rt))) {
      rt++;
    }
    return rt;
  }

  private static int readWord(final String value, final int start) {
    int rt = start;
    while (rt < value.length()
      && (Character.isLetterOrDigit(value.charAt(rt)) || value.charAt(rt) == '/'
        || value.charAt(rt) == '_' || value.charAt(rt) == '.' || value.charAt(rt) == '*')) {
      rt++;
    }
    return rt;
  }

  // tokenizer
  private static List<Token> tokenize(final String value) {
    final List<Token> rt = new ArrayList<>();
    int current = 0;
    int end = 0;

    while (true) {
      if (current == value.length()) {
        return rt;
      }
      final char c = value.charAt(current);
      if (Character.isWhitespace(c)) {
        end = readWhitespace(value, current);
        rt.add(new Token(TokenType.WHITESPACE, value.substring(current, end)));
        current = end;
      } else if (c == '\'') {
        end = readQuotedString(value, current + 1);
        rt.add(new Token(TokenType.QUOTED_STRING, value.substring(current, end)));
        current = end;
      } else if (Character.isLetter(c) || c == '*') {
        end = readWord(value, current + 1);
        final String tokenString = value.substring(current, end);
        rt.add(new Token(TokenType.WORD, tokenString));
        current = end;
      } else if (Character.isDigit(c)) {
        end = readDigits(value, current + 1);
        rt.add(new Token(TokenType.NUMBER, value.substring(current, end)));
        current = end;
      } else if (c == '(') {
        rt.add(new Token(TokenType.OPENPAREN, Character.toString(c)));
        current++;
      } else if (c == ')') {
        rt.add(new Token(TokenType.CLOSEPAREN, Character.toString(c)));
        current++;
      } else if (c == '-') {
        if (Character.isDigit(value.charAt(current + 1))) {
          end = readDigits(value, current + 1);
          rt.add(new Token(TokenType.NUMBER, value.substring(current, end)));
          current = end;
        } else {
          rt.add(new Token(TokenType.SYMBOL, Character.toString(c)));
          current++;
        }
      } else if (",.+=:".indexOf(c) > -1) {
        rt.add(new Token(TokenType.SYMBOL, Character.toString(c)));
        current++;
      } else {
        throw new RuntimeException("Unable to tokenize: " + value + " current: " + current
          + " rem: " + value.substring(current));
      }
    }

  }

  private static List<Token> trimWhitespace(final List<Token> tokens) {
    int start = 0;
    while (tokens.get(start).type == TokenType.WHITESPACE) {
      start++;
    }
    int end = tokens.size() - 1;
    while (tokens.get(end).type == TokenType.WHITESPACE) {
      end--;
    }
    return tokens.subList(start, end + 1);

  }

  private static String unquote(final String singleQuotedValue) {
    return singleQuotedValue.substring(1, singleQuotedValue.length() - 1).replace("''", "'");
  }
}
