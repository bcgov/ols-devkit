package com.revolsys.beans;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlExpression;

import com.revolsys.util.JexlUtil;

import tech.units.indriya.quantity.NumberQuantity;

public class ExpressionMeasurable<Q extends Quantity<Q>> extends NumberQuantity<Q> {

  private static final long serialVersionUID = 1L;

  private JexlContext context;

  private final JexlExpression expression;

  protected ExpressionMeasurable(final JexlExpression expression, final JexlContext context,
    final Unit<Q> unit) {
    super(0, unit);
    this.expression = expression;
    this.context = context;
  }

  public ExpressionMeasurable(final String expression, final Unit<Q> unit) {
    super(0, unit);
    try {
      this.expression = JexlUtil.newExpression(expression);
    } catch (final Exception e) {
      throw new IllegalArgumentException("Expression " + expression + " is not valid", e);
    }
  }

  @Override
  public Double getValue() {
    if (this.expression == null) {
      return Double.NaN;
    } else {
      try {
        return Double
          .valueOf(JexlUtil.evaluateExpression(this.context, this.expression).toString());
      } catch (final NullPointerException e) {
        return 0.0;
      }
    }
  }

}
