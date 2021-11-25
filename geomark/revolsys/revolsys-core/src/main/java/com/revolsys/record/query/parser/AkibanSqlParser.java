package com.revolsys.record.query.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jeometry.common.logging.Logs;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.BetweenOperatorNode;
import com.akiban.sql.parser.BinaryArithmeticOperatorNode;
import com.akiban.sql.parser.BinaryLogicalOperatorNode;
import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.CastNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.ConstantNode;
import com.akiban.sql.parser.CursorNode;
import com.akiban.sql.parser.InListOperatorNode;
import com.akiban.sql.parser.IsNullNode;
import com.akiban.sql.parser.JavaToSQLValueNode;
import com.akiban.sql.parser.JavaValueNode;
import com.akiban.sql.parser.LikeEscapeOperatorNode;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.NotNode;
import com.akiban.sql.parser.NumericConstantNode;
import com.akiban.sql.parser.ResultSetNode;
import com.akiban.sql.parser.RowConstructorNode;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SQLParserException;
import com.akiban.sql.parser.SQLToJavaValueNode;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.SimpleStringOperatorNode;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.StaticMethodCallNode;
import com.akiban.sql.parser.UserTypeConstantNode;
import com.akiban.sql.parser.ValueNode;
import com.akiban.sql.parser.ValueNodeList;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.query.And;
import com.revolsys.record.query.Between;
import com.revolsys.record.query.Cast;
import com.revolsys.record.query.CollectionValue;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.ILike;
import com.revolsys.record.query.In;
import com.revolsys.record.query.IsNotNull;
import com.revolsys.record.query.IsNull;
import com.revolsys.record.query.Not;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;
import com.revolsys.record.query.functions.GetMapValue;
import com.revolsys.record.query.functions.SimpleFunction;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class AkibanSqlParser implements SqlParser {
  /** Must be in upper case */
  public static final List<String> SUPPORTED_BINARY_OPERATORS = Arrays.asList("AND", "OR", "+", "-",
    "/", "*", "=", "<>", "<", "<=", ">", ">=", "LIKE", "+", "-", "/", "*", "%", "MOD");

  @SuppressWarnings("unchecked")
  public static <V extends QueryValue> V toQueryValue(final RecordDefinition recordDefinition,
    final ValueNode expression) {
    if (expression instanceof BetweenOperatorNode) {
      final BetweenOperatorNode betweenExpression = (BetweenOperatorNode)expression;
      final ValueNode leftValueNode = betweenExpression.getLeftOperand();
      final ValueNodeList rightOperandList = betweenExpression.getRightOperandList();
      final ValueNode betweenExpressionStart = rightOperandList.get(0);
      final ValueNode betweenExpressionEnd = rightOperandList.get(1);
      if (!(leftValueNode instanceof ColumnReference)) {
        throw new IllegalArgumentException(
          "Between operator must use a column name not: " + leftValueNode);
      }

      if (!(betweenExpressionStart instanceof NumericConstantNode)) {
        throw new IllegalArgumentException(
          "Between min value must be a number not: " + betweenExpressionStart);
      }
      if (!(betweenExpressionEnd instanceof NumericConstantNode)) {
        throw new IllegalArgumentException(
          "Between max value must be a number not: " + betweenExpressionEnd);
      }
      final Column column = toQueryValue(recordDefinition, leftValueNode);
      final Value min = toQueryValue(recordDefinition, betweenExpressionStart);
      final Value max = toQueryValue(recordDefinition, betweenExpressionEnd);
      if (recordDefinition != null) {
        final FieldDefinition field = recordDefinition.getField(column.getName());
        min.convert(field);
        max.convert(field);
      }
      return (V)new Between(column, min, max);
    } else if (expression instanceof BinaryLogicalOperatorNode) {
      final BinaryLogicalOperatorNode binaryOperatorNode = (BinaryLogicalOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator().toUpperCase();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      final Condition leftCondition = toQueryValue(recordDefinition, leftValueNode);
      final Condition rightCondition = toQueryValue(recordDefinition, rightValueNode);
      if ("AND".equals(operator)) {
        return (V)new And(leftCondition, rightCondition);
      } else if ("OR".equals(operator)) {
        return (V)new Or(leftCondition, rightCondition);
      } else {
        throw new IllegalArgumentException(
          "Binary logical operator " + operator + " not supported.");
      }
    } else if (expression instanceof BinaryOperatorNode) {
      final BinaryOperatorNode binaryOperatorNode = (BinaryOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      if (SUPPORTED_BINARY_OPERATORS.contains(operator.toUpperCase())) {
        final QueryValue leftCondition = toQueryValue(recordDefinition, leftValueNode);
        QueryValue rightCondition = toQueryValue(recordDefinition, rightValueNode);

        // TODO!!!!!!!
        if (leftCondition instanceof com.revolsys.record.query.ColumnReference) {
          if (rightCondition instanceof Value) {
            final com.revolsys.record.query.ColumnReference column = (com.revolsys.record.query.ColumnReference)leftCondition;

            final String name = column.getName();
            final Object value = ((Value)rightCondition).getValue();
            if (value == null) {
              throw new IllegalArgumentException(
                "Values can't be null for " + operator + " use IS NULL or IS NOT NULL instead.");
            } else if (recordDefinition != null) {
              final FieldDefinition fieldDefinition = recordDefinition.getField(name);
              final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(name);
              if (codeTable == null || fieldDefinition == recordDefinition.getIdField()) {
                final Object convertedValue = fieldDefinition.toFieldValueException(value);
                if (convertedValue == null) {
                  throw new IllegalArgumentException("Values can't be null for " + operator
                    + " use IS NULL or IS NOT NULL instead.");
                } else {
                  rightCondition = Value.newValue(fieldDefinition, convertedValue);
                }
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
                  rightCondition = Value.newValue(fieldDefinition, id);
                }
              }
            }
          }
        }
        if (expression instanceof BinaryArithmeticOperatorNode) {
          final QueryValue arithmaticCondition = Q.arithmatic(leftCondition, operator,
            rightCondition);
          return (V)arithmaticCondition;
        } else {
          final Condition binaryCondition = Q.binary(leftCondition, operator, rightCondition);
          return (V)binaryCondition;
        }
      } else {
        throw new IllegalArgumentException("Unsupported binary operator " + operator);
      }
    } else if (expression instanceof ColumnReference) {
      final ColumnReference column = (ColumnReference)expression;
      String columnName = column.getColumnName();
      columnName = columnName.replaceAll("\"", "");
      if (recordDefinition == null) {
        return (V)new Column(columnName);
      } else {
        final FieldDefinition fieldDefinition = recordDefinition.getField(columnName);
        if (fieldDefinition == null) {
          recordDefinition.getField(columnName);
          throw new IllegalArgumentException("Invalid column name " + columnName);
        } else {
          return (V)fieldDefinition;
        }
      }
    } else if (expression instanceof LikeEscapeOperatorNode) {
      final LikeEscapeOperatorNode likeEscapeOperatorNode = (LikeEscapeOperatorNode)expression;
      final ValueNode leftValueNode = likeEscapeOperatorNode.getReceiver();
      final ValueNode rightValueNode = likeEscapeOperatorNode.getLeftOperand();
      final QueryValue leftCondition = toQueryValue(recordDefinition, leftValueNode);
      final QueryValue rightCondition = toQueryValue(recordDefinition, rightValueNode);
      return (V)new ILike(leftCondition, rightCondition);
    } else if (expression instanceof NotNode) {
      final NotNode notNode = (NotNode)expression;
      final ValueNode operand = notNode.getOperand();
      final Condition condition = toQueryValue(recordDefinition, operand);
      return (V)new Not(condition);
    } else if (expression instanceof InListOperatorNode) {
      final InListOperatorNode inListOperatorNode = (InListOperatorNode)expression;
      final ValueNode leftOperand = inListOperatorNode.getLeftOperand();
      final QueryValue leftCondition = toQueryValue(recordDefinition, leftOperand);

      final List<QueryValue> conditions = new ArrayList<>();
      final RowConstructorNode itemsList = inListOperatorNode.getRightOperandList();
      for (final ValueNode itemValueNode : itemsList.getNodeList()) {
        final QueryValue itemCondition = toQueryValue(recordDefinition, itemValueNode);
        conditions.add(itemCondition);
      }
      return (V)new In(leftCondition, new CollectionValue(conditions));
    } else if (expression instanceof IsNullNode) {
      final IsNullNode isNullNode = (IsNullNode)expression;
      final ValueNode operand = isNullNode.getOperand();
      final QueryValue value = toQueryValue(recordDefinition, operand);
      if (isNullNode.getNodeType() == NodeTypes.IS_NOT_NULL_NODE) {
        return (V)new IsNotNull(value);
      } else {
        return (V)new IsNull(value);
      }
      // } else if (expression instanceof Parenthesis) {
      // final Parenthesis parenthesis = (Parenthesis)expression;
      // final ValueNode parenthesisValueNode = parenthesis.getExpression();
      // final Condition condition = toCondition(parenthesisExpression);
      // final ParenthesisCondition parenthesisCondition = new
      // ParenthesisCondition(
      // condition);
      // if (parenthesis.isNot()) {
      // return (V)Q.not(parenthesisCondition);
      // } else {
      // return (V)parenthesisCondition;
      // }
    } else if (expression instanceof RowConstructorNode) {
      final RowConstructorNode rowConstructorNode = (RowConstructorNode)expression;
      final ValueNodeList values = rowConstructorNode.getNodeList();
      final ValueNode valueNode = values.get(0);
      return (V)toQueryValue(recordDefinition, valueNode);
    } else if (expression instanceof UserTypeConstantNode) {
      final UserTypeConstantNode constant = (UserTypeConstantNode)expression;
      final Object objectValue = constant.getObjectValue();
      return (V)Value.newValue(objectValue);
    } else if (expression instanceof ConstantNode) {
      final ConstantNode constant = (ConstantNode)expression;
      final Object value = constant.getValue();
      return (V)Value.newValue(value);
    } else if (expression instanceof SimpleStringOperatorNode) {
      final SimpleStringOperatorNode operatorNode = (SimpleStringOperatorNode)expression;
      final String functionName = operatorNode.getMethodName().toUpperCase();
      final ValueNode operand = operatorNode.getOperand();
      final QueryValue condition = toQueryValue(recordDefinition, operand);
      return (V)new SimpleFunction(functionName, condition);
    } else if (expression instanceof CastNode) {
      final CastNode castNode = (CastNode)expression;
      final String typeName = castNode.getType().getSQLstring();
      final ValueNode operand = castNode.getCastOperand();
      final QueryValue condition = toQueryValue(recordDefinition, operand);
      return (V)new Cast(condition, typeName);
    } else if (expression instanceof JavaToSQLValueNode) {
      final JavaToSQLValueNode node = (JavaToSQLValueNode)expression;
      final JavaValueNode javaValueNode = node.getJavaValueNode();
      if (javaValueNode instanceof StaticMethodCallNode) {
        final StaticMethodCallNode methodNode = (StaticMethodCallNode)javaValueNode;
        final List<QueryValue> parameters = new ArrayList<>();

        final String methodName = methodNode.getMethodName();
        for (final JavaValueNode parameter : methodNode.getMethodParameters()) {
          if (parameter instanceof SQLToJavaValueNode) {
            final SQLToJavaValueNode sqlNode = (SQLToJavaValueNode)parameter;
            final QueryValue param = toQueryValue(recordDefinition, sqlNode.getSQLValueNode());
            parameters.add(param);
          }
        }
        if (methodName.equals("get_map_value")) {
          return (V)new GetMapValue(parameters);
        }

      }
      return null;
    } else if (expression == null) {
      return null;
    } else {
      throw new IllegalArgumentException(
        "Unsupported expression: " + expression.getClass() + " " + expression);
    }
  }

  private final RecordDefinition recordDefinition;

  private final String sqlPrefix;

  public AkibanSqlParser(final RecordDefinition recordDefinition) {
    super();
    this.recordDefinition = recordDefinition;

    String tableName;
    if (this.recordDefinition == null) {
      tableName = "Unknown";
    } else {
      tableName = this.recordDefinition.getPath().substring(1).replace('/', '.');
    }
    this.sqlPrefix = "SELECT * FROM " + tableName + " WHERE";

  }

  @Override
  public String getSqlPrefix() {
    return this.sqlPrefix;
  }

  @SuppressWarnings("unchecked")
  private <V extends QueryValue> V toQueryValue(final ValueNode expression) {
    if (expression instanceof BetweenOperatorNode) {
      final BetweenOperatorNode betweenExpression = (BetweenOperatorNode)expression;
      final ValueNode leftValueNode = betweenExpression.getLeftOperand();
      final ValueNodeList rightOperandList = betweenExpression.getRightOperandList();
      final ValueNode betweenExpressionStart = rightOperandList.get(0);
      final ValueNode betweenExpressionEnd = rightOperandList.get(1);
      if (!(leftValueNode instanceof ColumnReference)) {
        throw new IllegalArgumentException(
          "Between operator must use a column name not: " + leftValueNode);
      }

      if (!(betweenExpressionStart instanceof NumericConstantNode)) {
        throw new IllegalArgumentException(
          "Between min value must be a number not: " + betweenExpressionStart);
      }
      if (!(betweenExpressionEnd instanceof NumericConstantNode)) {
        throw new IllegalArgumentException(
          "Between max value must be a number not: " + betweenExpressionEnd);
      }
      final Column column = toQueryValue(leftValueNode);
      final Value min = toQueryValue(betweenExpressionStart);
      final Value max = toQueryValue(betweenExpressionEnd);
      if (this.recordDefinition != null) {
        final FieldDefinition fieldDefinition = this.recordDefinition.getField(column.getName());
        min.convert(fieldDefinition);
        max.convert(fieldDefinition);
      }
      return (V)new Between(column, min, max);
    } else if (expression instanceof BinaryLogicalOperatorNode) {
      final BinaryLogicalOperatorNode binaryOperatorNode = (BinaryLogicalOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator().toUpperCase();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      final QueryValue leftValue = toQueryValue(leftValueNode);
      if (leftValue instanceof Condition) {
        final Condition leftCondition = (Condition)leftValue;
        final QueryValue rightValue = toQueryValue(rightValueNode);
        if (rightValue instanceof Condition) {
          final Condition rightCondition = (Condition)rightValue;
          if ("AND".equals(operator)) {
            return (V)new And(leftCondition, rightCondition);
          } else if ("OR".equals(operator)) {
            return (V)new Or(leftCondition, rightCondition);
          } else {
            throw new IllegalArgumentException(
              "Binary logical operator " + operator + " not supported.");
          }
        } else {
          throw new IllegalArgumentException("Right side of " + operator
            + " must be a condition (e.g. column_name = 'value') not: " + rightValue);
        }
      } else {
        throw new IllegalArgumentException("Left side of " + operator
          + " must be a condition (e.g. column_name = 'value') not: " + leftValue);
      }
    } else if (expression instanceof BinaryOperatorNode) {
      final BinaryOperatorNode binaryOperatorNode = (BinaryOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      if (AkibanSqlParser.SUPPORTED_BINARY_OPERATORS.contains(operator.toUpperCase())) {
        final QueryValue leftCondition = toQueryValue(leftValueNode);
        QueryValue rightCondition = toQueryValue(rightValueNode);

        if (leftCondition instanceof com.revolsys.record.query.ColumnReference) {
          if (rightCondition instanceof Value) {
            final Object value = ((Value)rightCondition).getValue();
            if (value == null) {
              throw new IllegalArgumentException(
                "Values can't be null for " + operator + " use IS NULL or IS NOT NULL instead.");
            } else {
              final com.revolsys.record.query.ColumnReference column = (com.revolsys.record.query.ColumnReference)leftCondition;

              final String name = column.getName();
              if (this.recordDefinition != null) {
                final FieldDefinition fieldDefinition = this.recordDefinition.getField(name);
                final CodeTable codeTable = this.recordDefinition.getCodeTableByFieldName(name);
                if (codeTable == null || fieldDefinition == this.recordDefinition.getIdField()) {
                  try {
                    final Object convertedValue = fieldDefinition.toFieldValueException(value);
                    if (convertedValue == null) {
                      throw new IllegalArgumentException("Values can't be null for " + operator
                        + " use IS NULL or IS NOT NULL instead.");
                    } else {
                      rightCondition = Value.newValue(fieldDefinition, convertedValue);
                    }
                  } catch (final Throwable t) {
                    throw new IllegalArgumentException(name + "='" + value + "' is not a valid "
                      + fieldDefinition.getDataType().getValidationName());
                  }
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
                    rightCondition = Value.newValue(fieldDefinition, id);
                  }
                }
              }
            }
          }
        }
        if (expression instanceof BinaryArithmeticOperatorNode) {
          final QueryValue arithmaticCondition = Q.arithmatic(leftCondition, operator,
            rightCondition);
          return (V)arithmaticCondition;
        } else {
          final Condition binaryCondition = Q.binary(leftCondition, operator, rightCondition);
          return (V)binaryCondition;
        }

      } else {
        throw new IllegalArgumentException("Unsupported binary operator " + operator);
      }
    } else if (expression instanceof ColumnReference) {
      final ColumnReference column = (ColumnReference)expression;
      String columnName = column.getColumnName();
      columnName = columnName.replaceAll("\"", "");
      if (this.recordDefinition == null) {
        return (V)new Column(columnName);
      } else {
        final FieldDefinition fieldDefinition = this.recordDefinition.getField(columnName);
        if (fieldDefinition == null) {
          throw new IllegalArgumentException("Invalid field name " + columnName);
        } else {
          return (V)fieldDefinition;
        }
      }
    } else if (expression instanceof LikeEscapeOperatorNode) {
      final LikeEscapeOperatorNode likeEscapeOperatorNode = (LikeEscapeOperatorNode)expression;
      final ValueNode leftValueNode = likeEscapeOperatorNode.getReceiver();
      final ValueNode rightValueNode = likeEscapeOperatorNode.getLeftOperand();
      final QueryValue leftCondition = toQueryValue(leftValueNode);
      final QueryValue rightCondition = toQueryValue(rightValueNode);
      return (V)new ILike(leftCondition, rightCondition);
    } else if (expression instanceof NotNode) {
      final NotNode notNode = (NotNode)expression;
      final ValueNode operand = notNode.getOperand();
      final Condition condition = toQueryValue(operand);
      return (V)new Not(condition);
    } else if (expression instanceof InListOperatorNode) {
      final InListOperatorNode inListOperatorNode = (InListOperatorNode)expression;
      final ValueNode leftOperand = inListOperatorNode.getLeftOperand();
      final QueryValue leftCondition = toQueryValue(leftOperand);

      final List<QueryValue> conditions = new ArrayList<>();
      final RowConstructorNode itemsList = inListOperatorNode.getRightOperandList();
      for (final ValueNode itemValueNode : itemsList.getNodeList()) {
        final QueryValue itemCondition = toQueryValue(itemValueNode);
        conditions.add(itemCondition);
      }
      return (V)new In(leftCondition, new CollectionValue(conditions));
    } else if (expression instanceof IsNullNode) {
      final IsNullNode isNullNode = (IsNullNode)expression;
      final ValueNode operand = isNullNode.getOperand();
      final QueryValue value = toQueryValue(operand);
      if (isNullNode.getNodeType() == NodeTypes.IS_NOT_NULL_NODE) {
        return (V)new IsNotNull(value);
      } else {
        return (V)new IsNull(value);
      }
      // } else if (expression instanceof Parenthesis) {
      // final Parenthesis parenthesis = (Parenthesis)expression;
      // final ValueNode parenthesisValueNode = parenthesis.getExpression();
      // final Condition condition = toCondition(parenthesisExpression);
      // final ParenthesisCondition parenthesisCondition = new
      // ParenthesisCondition(
      // condition);
      // if (parenthesis.isNot()) {
      // return (V)Q.not(parenthesisCondition);
      // } else {
      // return (V)parenthesisCondition;
      // }
    } else if (expression instanceof RowConstructorNode) {
      final RowConstructorNode rowConstructorNode = (RowConstructorNode)expression;
      final ValueNodeList values = rowConstructorNode.getNodeList();
      final ValueNode valueNode = values.get(0);
      return (V)toQueryValue(valueNode);
    } else if (expression instanceof UserTypeConstantNode) {
      final UserTypeConstantNode constant = (UserTypeConstantNode)expression;
      final Object objectValue = constant.getObjectValue();
      return (V)Value.newValue(objectValue);
    } else if (expression instanceof ConstantNode) {
      final ConstantNode constant = (ConstantNode)expression;
      final Object value = constant.getValue();
      return (V)Value.newValue(value);
    } else if (expression instanceof SimpleStringOperatorNode) {
      final SimpleStringOperatorNode operatorNode = (SimpleStringOperatorNode)expression;
      final String functionName = operatorNode.getMethodName().toUpperCase();
      final ValueNode operand = operatorNode.getOperand();
      final QueryValue condition = toQueryValue(operand);
      return (V)new SimpleFunction(functionName, condition);
    } else if (expression instanceof CastNode) {
      final CastNode castNode = (CastNode)expression;
      final String typeName = castNode.getType().getSQLstring();
      final ValueNode operand = castNode.getCastOperand();
      final QueryValue condition = toQueryValue(operand);
      return (V)new Cast(condition, typeName);
    } else if (expression == null) {
      return null;
    } else if (expression instanceof JavaToSQLValueNode) {
      final JavaToSQLValueNode node = (JavaToSQLValueNode)expression;
      final JavaValueNode javaValueNode = node.getJavaValueNode();
      if (javaValueNode instanceof StaticMethodCallNode) {
        final StaticMethodCallNode methodNode = (StaticMethodCallNode)javaValueNode;
        final List<QueryValue> parameters = new ArrayList<>();

        final String methodName = methodNode.getMethodName();
        for (final JavaValueNode parameter : methodNode.getMethodParameters()) {
          if (parameter instanceof SQLToJavaValueNode) {
            final SQLToJavaValueNode sqlNode = (SQLToJavaValueNode)parameter;
            final QueryValue param = toQueryValue(sqlNode.getSQLValueNode());
            parameters.add(param);
          }
        }
        if (methodName.equals("get_map_value")) {
          return (V)new GetMapValue(parameters);
        }

      }
      return null;
    } else {
      throw new IllegalArgumentException(
        "Unsupported expression: " + expression.getClass() + " " + expression);
    }
  }

  @Override
  public Condition whereToCondition(final String whereClause) {
    final String sql = "SELECT * FROM X WHERE " + "\n" + whereClause;
    try {
      final StatementNode statement = new SQLParser().parseStatement(sql);
      if (statement instanceof CursorNode) {
        final CursorNode selectStatement = (CursorNode)statement;
        final ResultSetNode resultSetNode = selectStatement.getResultSetNode();
        if (resultSetNode instanceof SelectNode) {
          final SelectNode selectNode = (SelectNode)resultSetNode;
          final ValueNode where = selectNode.getWhereClause();
          final QueryValue queryValue = toQueryValue(where);
          if (queryValue instanceof Condition) {
            return (Condition)queryValue;
          }
        }
      }
    } catch (final SQLParserException e) {
      final int offset = e.getErrorPosition();
      throw new IllegalArgumentException(
        "Error parsing SQL at " + (offset - this.sqlPrefix.length()) + ": ", e);
    } catch (final StandardException e) {
      Logs.error(this, "Error parsing SQL: " + whereClause, e);
    }
    return null;
  }
}
