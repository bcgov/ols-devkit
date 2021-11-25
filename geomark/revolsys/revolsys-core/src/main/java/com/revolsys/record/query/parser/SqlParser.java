package com.revolsys.record.query.parser;

import com.revolsys.record.query.Condition;

public interface SqlParser {

  String getSqlPrefix();

  Condition whereToCondition(String where);
}
