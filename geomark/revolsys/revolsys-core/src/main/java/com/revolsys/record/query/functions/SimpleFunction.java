package com.revolsys.record.query.functions;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.query.AbstractMultiQueryValue;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Strings;

public class SimpleFunction extends AbstractMultiQueryValue implements Function {

  private final String name;

  public SimpleFunction(final String name, final int expectedParameterCount,
    final List<QueryValue> parameters) {
    super(parameters);
    this.name = name;
    final int parameterCount = parameters.size();
    if (expectedParameterCount != parameterCount) {
      throw new IllegalArgumentException("SimpleFunction " + name + " requires "
        + expectedParameterCount + " parameters not " + parameterCount + ": " + parameters);
    }
  }

  public SimpleFunction(final String name, final int expectedParameterCount,
    final QueryValue... parameters) {
    this(name, expectedParameterCount, Arrays.asList(parameters));
  }

  public SimpleFunction(final String name, final List<QueryValue> parameters) {
    super(parameters);
    this.name = name;
  }

  public SimpleFunction(final String name, final QueryValue... parameters) {
    this(name, Arrays.asList(parameters));
  }

  public void add(final QueryValue value) {
    addValue(value);
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    buffer.append(this.name);
    buffer.append("(");
    boolean first = true;
    for (final QueryValue parameter : getParameters()) {
      if (first) {
        first = false;
      } else {
        buffer.append(", ");
      }
      parameter.appendSql(query, recordStore, buffer);
    }
    buffer.append(")");
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    for (final QueryValue value : getParameters()) {
      index = value.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public SimpleFunction clone() {
    final SimpleFunction clone = (SimpleFunction)super.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof SimpleFunction) {
      final SimpleFunction value = (SimpleFunction)obj;
      if (DataType.equal(getName(), value.getName())) {
        final List<QueryValue> parameters1 = getParameters();
        final List<QueryValue> parameters2 = value.getParameters();
        if (parameters1.size() == parameters2.size()) {
          for (int i = 0; i < parameters1.size(); i++) {
            final QueryValue value1 = parameters1.get(i);
            final QueryValue value2 = parameters2.get(i);
            if (!DataType.equal(value1, value2)) {
              return false;
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public int getParameterCount() {
    return getParameters().size();
  }

  @Override
  public List<QueryValue> getParameters() {
    return Collections.unmodifiableList(getQueryValues());
  }

  public String getParameterStringValue(final int index, final MapEx record) {
    final QueryValue parameter = getParameter(index);
    if (parameter == null) {
      return null;
    } else {
      return parameter.getStringValue(record);
    }
  }

  public <V> V getParameterValue(final int index, final MapEx record) {
    final QueryValue parameter = getParameter(index);
    if (parameter == null) {
      return null;
    } else {
      final V value = parameter.getValue(record);
      return value;
    }
  }

  public <V> V getParameterValue(final int index, final MapEx record, final DataType dataType) {
    final QueryValue parameter = getParameter(index);
    if (parameter == null) {
      return null;
    } else {
      return parameter.getValue(record, dataType);
    }
  }

  @Override
  public <V> V getValue(final MapEx record) {
    return null;
  }

  @Override
  public String toString() {
    final List<QueryValue> parameters = getParameters();
    return getName() + "(" + Strings.toString(", ", parameters) + ")";
  }
}
