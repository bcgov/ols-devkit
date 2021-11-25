package com.revolsys.record.query.functions;

import java.util.Collections;
import java.util.List;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.query.AbstractUnaryQueryValue;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.schema.RecordStore;

public abstract class UnaryFunction extends AbstractUnaryQueryValue implements Function {

  private final String name;

  public UnaryFunction(final String name, final List<QueryValue> parameters) {
    this.name = name;
    final int parameterCount = parameters.size();
    if (parameterCount == 1) {
      final QueryValue parameter = parameters.get(0);
      setValue(parameter);
    } else {
      throw new IllegalArgumentException(
        "UnaryFunction " + name + " requires 1 argument not " + parameterCount + ": " + parameters);
    }
  }

  public UnaryFunction(final String name, final QueryValue parameter) {
    super(parameter);
    this.name = name;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    buffer.append(getName());
    buffer.append("(");
    super.appendDefaultSql(query, recordStore, buffer);
    buffer.append(")");
  }

  @Override
  public UnaryFunction clone() {
    return (UnaryFunction)super.clone();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof UnaryFunction) {
      final UnaryFunction function = (UnaryFunction)other;
      if (DataType.equal(function.getName(), getName())) {
        return super.equals(function);
      }
    }
    return false;
  }

  @Override
  public String getName() {
    return this.name;
  }

  public QueryValue getParameter() {
    return super.getValue();
  }

  @Override
  public int getParameterCount() {
    return 1;
  }

  @Override
  public List<QueryValue> getParameters() {
    final QueryValue parameter = getParameter();
    return Collections.singletonList(parameter);
  }

  @Override
  public String toString() {
    return getName() + "(" + super.toString() + ")";
  }
}
