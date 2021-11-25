package com.revolsys.record.query.functions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.RecordDefinition;

public class ToChar extends SimpleFunction {

  public static final String NAME = "TO_CHAR";

  public ToChar(final List<QueryValue> parameters) {
    super(NAME, 2, parameters);
  }

  public ToChar(final QueryValue value, final String format) {
    super(NAME, value, Value.newValue(format));
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    return resultSet.getString(indexes.incrementAndGet());
  }
}
