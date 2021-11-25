package com.revolsys.oracle.recordstore.field;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

import oracle.sql.ROWID;

public class OracleJdbcRowIdFieldDefinition extends JdbcFieldDefinition {
  public OracleJdbcRowIdFieldDefinition() {
    super("rowid", "ORACLE_ROWID", DataTypes.STRING, Types.ROWID, 18, 0, true, "Row identifier",
      Collections.emptyMap());
  }

  @Override
  public void addInsertStatementPlaceHolder(final StringBuilder sql, final boolean generateKeys) {
  }

  @Override
  public void addStatementPlaceHolder(final StringBuilder sql) {
    sql.append("chartorowid(");
    super.addStatementPlaceHolder(sql);
    sql.append(")");
  }

  @Override
  public void appendSelect(final Query query, final RecordStore recordStore, final Appendable sql) {
    try {
      sql.append(" \"ORACLE_ROWID\"");
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    final ROWID rowId = (ROWID)resultSet.getRowId(indexes.incrementAndGet());
    if (rowId == null) {
      return null;
    } else {
      return rowId.stringValue();
    }
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    return parameterIndex;
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record record) throws SQLException {
    return parameterIndex;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      final String string = value.toString();
      statement.setString(parameterIndex, string);
    }
    return parameterIndex + 1;
  }

  @Override
  public Object validate(final Record record, final Object value) {
    if (record.getState() == RecordState.NEW) {
      return true;
    } else {
      return super.validate(record, value);
    }
  }
}
