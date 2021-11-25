package com.revolsys.record.query.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.record.code.CodeTable;
import com.revolsys.record.query.Add;
import com.revolsys.record.query.And;
import com.revolsys.record.query.Cast;
import com.revolsys.record.query.CollectionValue;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.ColumnReference;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Divide;
import com.revolsys.record.query.Equal;
import com.revolsys.record.query.GreaterThanEqual;
import com.revolsys.record.query.ILike;
import com.revolsys.record.query.In;
import com.revolsys.record.query.IsNotNull;
import com.revolsys.record.query.IsNull;
import com.revolsys.record.query.LessThan;
import com.revolsys.record.query.LessThanEqual;
import com.revolsys.record.query.Mod;
import com.revolsys.record.query.Multiply;
import com.revolsys.record.query.Not;
import com.revolsys.record.query.NotEqual;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Subtract;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.util.cnfexpression.MultiAndExpression;
import net.sf.jsqlparser.util.cnfexpression.MultiOrExpression;
import net.sf.jsqlparser.util.cnfexpression.MultipleExpression;

public class JSqlParser extends AbstractSqlParser {

  private final Map<Class<? extends Expression>, Function<Expression, QueryValue>> converters = new HashMap<>();

  public JSqlParser(final RecordDefinition recordDefinition) {
    super(recordDefinition);

    this.converters.put(Parenthesis.class, this::convertParenthesis);

    this.converters.put(Addition.class, convertBinaryExpression(Add::new));
    this.converters.put(Subtraction.class, convertBinaryExpression(Subtract::new));
    this.converters.put(Division.class, convertBinaryExpression(Divide::new));
    this.converters.put(Multiplication.class, convertBinaryExpression(Multiply::new));
    this.converters.put(Modulo.class, convertBinaryExpression(Mod::new));

    this.converters.put(AndExpression.class, convertBinaryExpression(And::new));
    this.converters.put(MultiAndExpression.class, convertMultipleExpression(And::new));
    this.converters.put(OrExpression.class, convertBinaryExpression(Or::new));
    this.converters.put(MultiOrExpression.class, convertMultipleExpression(Or::new));

    this.converters.put(EqualsTo.class, convertBinaryExpression(Equal::new));
    this.converters.put(GreaterThan.class,
      convertBinaryExpression(com.revolsys.record.query.GreaterThan::new));
    this.converters.put(GreaterThanEquals.class, convertBinaryExpression(GreaterThanEqual::new));
    this.converters.put(MinorThan.class, convertBinaryExpression(LessThan::new));
    this.converters.put(MinorThanEquals.class, convertBinaryExpression(LessThanEqual::new));
    this.converters.put(NotEqualsTo.class, convertBinaryExpression(NotEqual::new));
    this.converters.put(LikeExpression.class, convertBinaryExpression(ILike::new));

    this.converters.put(StringValue.class,
      convertValue((final StringValue value) -> value.getNotExcapedValue()));
    this.converters.put(DoubleValue.class,
      convertValue((final DoubleValue value) -> value.getValue()));
    this.converters.put(LongValue.class, convertValue((final LongValue value) -> value.getValue()));
    this.converters.put(NullValue.class, convertValue((final NullValue value) -> null));
    this.converters.put(DateValue.class, convertValue((final DateValue value) -> value.getValue()));
    this.converters.put(TimestampValue.class,
      convertValue((final TimestampValue value) -> value.getValue()));
    this.converters.put(TimeValue.class, convertValue((final TimeValue value) -> value.getValue()));

    this.converters.put(Between.class, this::convertBetween);
    this.converters.put(CastExpression.class, this::convertCastExpression);
    this.converters.put(net.sf.jsqlparser.schema.Column.class, this::convertColumn);
    this.converters.put(InExpression.class, this::convertInExpression);
    this.converters.put(IsNullExpression.class, this::convertIsNullExpression);
    this.converters.put(NotExpression.class, this::convertNotExpression);

    this.converters.put(net.sf.jsqlparser.expression.Function.class, this::convertFunction);

  }

  // <li>AllComparisonExpression</li>
  // <li>AnalyticExpression</li>
  // <li>AnyComparisonExpression</li>
  // <li>ArrayExpression</li>
  // <li>BitwiseAnd</li>
  // <li>BitwiseLeftShift</li>
  // <li>BitwiseOr</li>
  // <li>BitwiseRightShift</li>
  // <li>BitwiseXor</li>
  // <li>CaseExpression</li>
  // <li>CollateExpression</li>
  // <li>Concat</li>
  // <li>DateTimeLiteralExpression</li>
  // <li>ExistsExpression</li>
  // <li>ExtractExpression</li>
  // <li>FullTextSearch</li>
  // <li>SimpleFunction</li>
  // <li>HexValue</li>
  // <li>IntegerDivision</li>
  // <li>IntervalExpression</li>
  // <li>IsBooleanExpression</li>
  // <li>JdbcNamedParameter</li>
  // <li>JdbcParameter</li>
  // <li>JsonExpression</li>
  // <li>JsonOperator</li>
  // <li>KeepExpression</li>
  // <li>Matches</li>
  // <li>MySQLGroupConcat</li>
  // <li>NextValExpression</li>
  // <li>NumericBind</li>
  // <li>OldOracleJoinBinaryExpression</li>
  // <li>OracleHierarchicalExpression</li>
  // <li>OracleHint</li>
  // <li>RegExpMatchOperator</li>
  // <li>RegExpMySQLOperator</li>
  // <li>RowConstructor</li>
  // <li>SignedExpression</li>
  // <li>SimilarToExpression</li>
  // <li>SubSelect</li>
  // <li>TimeKeyExpression</li>
  // <li>UserVariable</li>
  // <li>ValueListExpression</li>
  // <li>WhenClause</li>

  /**
   * <li>Between</li>
   */
  private Condition convertBetween(final Expression expression) {
    final Between between = (Between)expression;
    final Expression columnExpression = between.getLeftExpression();
    final Column column = convertExpression(columnExpression);
    final Expression startExpression = between.getBetweenExpressionStart();
    final Value startValue = convertExpression(startExpression);
    final Expression endExpression = between.getBetweenExpressionEnd();
    final Value endValue = convertExpression(endExpression);
    final com.revolsys.record.query.Between betweenQuery = new com.revolsys.record.query.Between(
      column, startValue, endValue);
    if (between.isNot()) {
      return Q.not(betweenQuery);
    } else {
      return betweenQuery;
    }
  }

  private Function<Expression, QueryValue> convertBinaryExpression(
    final BiFunction<QueryValue, QueryValue, QueryValue> constructor) {
    return (final Expression expression) -> {
      final BinaryExpression binaryExpression = (BinaryExpression)expression;
      QueryValue leftValue = convertLeftExpression(binaryExpression);
      QueryValue rightValue = convertRightExpression(binaryExpression);
      rightValue = setFieldDefinition(leftValue, rightValue);
      leftValue = setFieldDefinition(rightValue, leftValue);

      return constructor.apply(leftValue, rightValue);
    };
  }

  private Cast convertCastExpression(final Expression expression) {
    final CastExpression castExpression = (CastExpression)expression;
    final Expression leftExpression = castExpression.getLeftExpression();
    final QueryValue leftValue = convertExpression(leftExpression);
    final ColDataType type = castExpression.getType();
    final String dataType = type.getDataType();
    return new Cast(leftValue, dataType);
  }

  private ColumnReference convertColumn(final Expression expression) {
    final net.sf.jsqlparser.schema.Column column = (net.sf.jsqlparser.schema.Column)expression;
    String columnName = column.getColumnName();
    columnName = columnName.replaceAll("\"", "");
    final FieldDefinition fieldDefinition = this.recordDefinition.getField(columnName);
    if (fieldDefinition == null) {
      throw new IllegalArgumentException("Invalid field name " + columnName);
    } else {
      return fieldDefinition;
    }
  }

  @SuppressWarnings("unchecked")
  private <V extends QueryValue> V convertExpression(final Expression expression) {
    if (expression == null) {
      return null;
    } else {
      Class<?> clazz = expression.getClass();
      do {
        final Function<Expression, QueryValue> converter = this.converters.get(clazz);
        if (converter == null) {
          clazz = clazz.getSuperclass();
        } else {
          return (V)converter.apply(expression);
        }
      } while (clazz != Object.class);
      throw new IllegalArgumentException("No converter for: " + expression);
    }
  }

  private QueryValue convertFunction(final Expression expression) {
    final net.sf.jsqlparser.expression.Function function = (net.sf.jsqlparser.expression.Function)expression;
    final String name = function.getName();
    final ExpressionList expressions = function.getParameters();
    final List<QueryValue> parameters = new ArrayList<>();
    if (expressions != null) {
      for (final Expression parameter : expressions.getExpressions()) {
        final QueryValue parameterVaue = convertExpression(parameter);
        parameters.add(parameterVaue);
      }
    }
    return newFunction(name, parameters);
  }

  private Condition convertInExpression(final Expression expression) {
    final InExpression inExpression = (InExpression)expression;
    final Expression leftExpression = inExpression.getLeftExpression();
    final QueryValue leftValue = convertExpression(leftExpression);
    final ExpressionList expressions = (ExpressionList)inExpression.getRightItemsList();
    final ArrayList<Object> values = new ArrayList<>();
    for (final Expression subExpression : expressions.getExpressions()) {
      final QueryValue subCondition = convertExpression(subExpression);
      values.add(subCondition);
    }
    final CollectionValue collectionValue = new CollectionValue(values);
    final In in = new In(leftValue, collectionValue);
    if (inExpression.isNot()) {
      return Q.not(in);
    } else {
      return in;
    }
  }

  /**
  * <li>IsNullExpression</li>
  * @param expression
  * @return
  */
  private Condition convertIsNullExpression(final Expression expression) {
    final IsNullExpression isNullExpression = (IsNullExpression)expression;
    final Expression leftExpression = isNullExpression.getLeftExpression();
    final QueryValue leftValue = convertExpression(leftExpression);
    if (isNullExpression.isNot()) {
      return new IsNotNull(leftValue);
    } else {
      return new IsNull(leftValue);
    }
  }

  public <V extends QueryValue> V convertLeftExpression(final BinaryExpression binaryExpression) {
    final Expression expression = binaryExpression.getLeftExpression();
    return convertExpression(expression);
  }

  private Function<Expression, QueryValue> convertMultipleExpression(
    final Function<List<Condition>, QueryValue> constructor) {
    return (final Expression expression) -> {
      final MultipleExpression multiExpression = (MultipleExpression)expression;
      final List<Condition> conditions = new ArrayList<>();
      for (final Expression subExpression : multiExpression.getList()) {
        final Condition condition = convertExpression(subExpression);
        conditions.add(condition);
      }
      return constructor.apply(conditions);
    };
  }

  /**
   * <li>NotExpression</li>
   */
  private Not convertNotExpression(final Expression expression) {
    final NotExpression notExpression = (NotExpression)expression;
    final Expression subExpression = notExpression.getExpression();
    final Condition condition = convertExpression(subExpression);
    return Q.not(condition);
  }

  private com.revolsys.record.query.Parenthesis convertParenthesis(final Expression expression) {
    final Parenthesis castExpression = (Parenthesis)expression;
    final Expression leftExpression = castExpression.getExpression();
    final QueryValue leftValue = convertExpression(leftExpression);
    return new com.revolsys.record.query.Parenthesis(leftValue);
  }

  public <V extends QueryValue> V convertRightExpression(final BinaryExpression binaryExpression) {
    final Expression expression = binaryExpression.getRightExpression();
    return convertExpression(expression);
  }

  @SuppressWarnings("unchecked")
  private <V> Function<Expression, QueryValue> convertValue(final Function<V, Object> converter) {
    return (final Expression expression) -> {
      final V expressionValue = (V)expression;
      final Object value = converter.apply(expressionValue);
      return Value.newValue(value);
    };
  }

  @Override
  public String getSqlPrefix() {
    return this.sqlPrefix;
  }

  private QueryValue setFieldDefinition(final QueryValue value1, final QueryValue value2) {
    if (this.recordDefinition != null) {
      if (value1 instanceof ColumnReference) {
        if (value2 instanceof Value) {
          final ColumnReference column = (ColumnReference)value1;

          final String name = column.getName();
          final Object value = ((Value)value2).getValue();
          final FieldDefinition fieldDefinition = this.recordDefinition.getField(name);
          final CodeTable codeTable = this.recordDefinition.getCodeTableByFieldName(name);
          if (codeTable == null || fieldDefinition == this.recordDefinition.getIdField()) {
            final Object convertedValue = fieldDefinition.toFieldValueException(value);
            return Value.newValue(fieldDefinition, convertedValue);
          } else {
            Object id;
            if (value instanceof String) {
              final String string = (String)value;
              final String[] values = string.split(":");
              id = codeTable.getIdentifier((Object[])values);
            } else {
              id = codeTable.getIdentifier(value);
            }
            if (id == null) {
              throw new IllegalArgumentException(name + "='" + value
                + "' could not be found in the code table " + codeTable.getName());
            } else {
              return Value.newValue(fieldDefinition, id);
            }
          }
        }
      }
    }
    return value2;
  }

  @Override
  public Condition whereToCondition(final String whereClause) {
    if (Property.hasValue(whereClause)) {
      final String sql = this.sqlPrefix + " (" + "\n" + whereClause + "\n)";
      try {
        final Statement statement = CCJSqlParserUtil.parse(sql);
        if (statement instanceof Select) {
          final Select select = (Select)statement;
          final SelectBody selectBody = select.getSelectBody();
          if (selectBody instanceof PlainSelect) {
            final PlainSelect plainSelect = (PlainSelect)selectBody;
            final Expression where = plainSelect.getWhere();
            if (where instanceof Parenthesis) {
              final Parenthesis parenthesis = (Parenthesis)where;
              final Expression expression = parenthesis.getExpression();
              final Condition condition = convertExpression(expression);
              return condition;
            }
          }
        }

      } catch (final Exception e) {
        throw Exceptions.wrap("Error parsing SQL condition: " + whereClause, e);
      }
    }
    return null;
  }
}
