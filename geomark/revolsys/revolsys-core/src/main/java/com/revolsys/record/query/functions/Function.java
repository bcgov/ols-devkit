package com.revolsys.record.query.functions;

import java.util.List;

import com.revolsys.record.query.QueryValue;

public interface Function extends QueryValue {

  String getName();

  default QueryValue getParameter(final int index) {
    if (index >= 0 && index < getParameterCount()) {
      final List<QueryValue> parameters = getParameters();
      return parameters.get(index);
    } else {
      return null;
    }
  }

  int getParameterCount();

  List<QueryValue> getParameters();

}
